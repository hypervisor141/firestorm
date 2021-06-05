package hypervisor.firestorm.mesh;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.engine.FSElements;
import hypervisor.firestorm.engine.FSView;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.utils.VLUpdater;

public class FSSchematics implements VLCopyable<FSSchematics>{

    protected static final VLUpdater<FSSchematics> UPDATE_CENTROID = new VLUpdater<FSSchematics>(){

        @Override
        public void update(FSSchematics s){
            s.updateCentroid();
            s.centroidupdater = UPDATE_NOTHING;
        }
    };
    protected static final VLUpdater<FSSchematics> UPDATE_MODEL = new VLUpdater<FSSchematics>(){

        @Override
        public void update(FSSchematics s){
            s.updateModels();
            s.modelupdater = UPDATE_NOTHING;
        }
    };
    protected static final VLUpdater<FSSchematics> UPDATE_MVP = new VLUpdater<FSSchematics>(){

        @Override
        public void update(FSSchematics s){
            s.updateMVPs();
            s.mvpupdater = UPDATE_NOTHING;
        }
    };

    protected static final FSBounds.Collision COLLISIONCACHE = new FSBounds.Collision();

    protected FSInstance instance;

    protected VLUpdater<FSSchematics> centroidupdater;
    protected VLUpdater<FSSchematics> modelupdater;
    protected VLUpdater<FSSchematics> mvpupdater;

    protected VLListType<FSBounds> mainbounds;
    protected VLListType<FSBounds> inputbounds;

    protected float[] centroid;
    protected float[] centroidmodel;
    protected float[] centroidmvp;

    protected float[] bounds;
    protected float[] boundsmodel;
    protected float[] boundsmvp;

    public FSSchematics(FSInstance instance){
        this.instance = instance;

        mainbounds = new VLListType<>(0, 10);
        inputbounds = new VLListType<>(0, 10);
    }

    public FSSchematics(FSSchematics src, long flags){
        copy(src, flags);
    }

    protected FSSchematics(){

    }

    @Override
    public void copy(FSSchematics src, long flags){
        this.instance = src.instance;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            mainbounds = src.mainbounds;
            inputbounds = src.inputbounds;
            centroid = src.centroid;
            centroidmodel = src.centroidmodel;
            centroidmvp = src.centroidmvp;
            bounds = src.bounds;
            boundsmodel = src.boundsmodel;
            boundsmvp = src.boundsmvp;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            mainbounds = src.mainbounds.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            inputbounds = src.inputbounds.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            centroid = src.centroid.clone();
            centroidmodel = src.centroidmodel.clone();
            centroidmvp = src.centroidmvp.clone();
            bounds = src.bounds.clone();
            boundsmodel = src.boundsmodel.clone();
            boundsmvp = src.boundsmvp.clone();

        }else{
            Helper.throwMissingDefaultFlags();
        }

        centroidupdater = src.centroidupdater;
        modelupdater = src.modelupdater;
        mvpupdater = src.mvpupdater;
    }

    @Override
    public FSSchematics duplicate(long flags){
        return new FSSchematics(this, flags);
    }

    public void initialize(){
        bounds = new float[FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION] * 2];
        centroid = new float[4];
        centroidmodel = new float[4];
        centroidmvp = new float[4];
        boundsmodel = new float[bounds.length];
        boundsmvp = new float[bounds.length];

        centroidupdater = UPDATE_CENTROID;
        modelupdater = UPDATE_MODEL;
        mvpupdater = UPDATE_MVP;
    }

    public void updateBoundaries(){
        float maxx = -Float.MAX_VALUE;
        float minx = Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxz = -Float.MAX_VALUE;
        float minz = Float.MAX_VALUE;

        float[] vertices = instance.positions().provider();
        int size = vertices.length;

        float x, y, z;

        centroid[0] = 0;
        centroid[1] = 0;
        centroid[2] = 0;

        for(int index = 0; index < size; index += FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION]){
            x = vertices[index];
            y = vertices[index + 1];
            z = vertices[index + 2];

            centroid[0] += x;
            centroid[1] += y;
            centroid[2] += z;

            if(minx > x){
                minx = x;
                bounds[0] = x;
            }
            if(miny > y){
                miny = y;
                bounds[1] = y;
            }
            if(minz > z){
                minz = z;
                bounds[2] = z;
            }

            if(maxx < x){
                maxx = x;
                bounds[4] = x;
            }
            if(maxy < y){
                maxy = y;
                bounds[5] = y;
            }
            if(maxz < z){
                maxz = z;
                bounds[6] = z;
            }
        }

        int pointcount = size / FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];

        centroid[0] /= pointcount;
        centroid[1] /= pointcount;
        centroid[2] /= pointcount;

        float b;

        for(int i = 0; i < bounds.length; i++){
            b = bounds[i];

            boundsmodel[i] = b;
            boundsmvp[i] = b;
        }

        markForNewUpdates();
    }

    private void updateCentroid(){
        instance.model().transformPoint(centroidmodel, 0, centroid, 0);
        FSControl.getView().multiplyViewPerspective(centroidmvp, 0, centroidmodel, 0);
    }

    private void updateModels(){
        FSArrayModel model = instance.model();

        model.transformPoint(boundsmodel, 0, bounds, 0);
        model.transformPoint(boundsmodel, 4, bounds, 4);
    }

    private void updateMVPs(){
        FSView config = FSControl.getView();

        config.multiplyViewPerspective(boundsmvp, 0, boundsmodel, 0);
        config.multiplyViewPerspective(boundsmvp, 4, boundsmodel, 4);
    }

    public void checkCollision(InstanceCollision results, FSInstance target){
        int index = -1;
        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            index = target.schematics.checkCollision(COLLISIONCACHE, mainbounds.get(i));

            if(index != -1){
                results.collision = new FSBounds.Collision(COLLISIONCACHE);
                results.sourceindex = i;
                results.targetindex = index;

                return;
            }
        }
    }

    public int checkCollision(FSBounds.Collision results, FSBounds target){
        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            mainbounds.get(i).check(results, target);

            if(results.collided){
                return 1;
            }
        }

        return -1;
    }

    public int checkPointCollision(FSBounds.Collision results, float[] point){
        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            mainbounds.get(i).checkPoint(results, point);

            if(results.collided){
                return i;
            }
        }

        return -1;
    }

    public FSBounds.Collision checkInputCollision(float[] near, float[] far){
        int size = inputbounds.size();

        for(int i = 0; i < size; i++){
            inputbounds.get(i).checkInput(COLLISIONCACHE, near, far);

            if(COLLISIONCACHE.collided){
                COLLISIONCACHE.boundsindex = i;
                return new FSBounds.Collision(COLLISIONCACHE);
            }
        }

        return null;
    }

    public void markForNewUpdates(){
        centroidupdater = UPDATE_CENTROID;
        modelupdater = UPDATE_MODEL;
        mvpupdater = UPDATE_MVP;

        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            mainbounds.get(i).markForUpdate();
        }

        size = inputbounds.size();

        for(int i = 0; i < size; i++){
            inputbounds.get(i).markForUpdate();
        }
    }

    public float rawX(){
        return rawRight();
    }

    public float rawY(){
        return rawTop();
    }

    public float rawZ(){
        return rawFront();
    }

    public float rawCentroidX(){
        centroidupdater.update(this);
        return centroid[0];
    }

    public float rawCentroidY(){
        centroidupdater.update(this);
        return centroid[1];
    }

    public float rawCentroidZ(){
        centroidupdater.update(this);
        return centroid[2];
    }

    public float[] rawCentroid(){
        centroidupdater.update(this);
        return centroid;
    }

    public float rawBoundCenterX(){
        return (rawLeft() + rawRight()) / 2f;
    }

    public float rawBoundCenterY(){
        return (rawBottom() + rawTop()) / 2f;
    }

    public float rawBoundCenterZ(){
        return (rawFront() + rawBack()) / 2f;
    }

    public float rawWidth(){
        return Math.abs(rawRight() - rawLeft());
    }

    public float rawHeight(){
        return Math.abs(rawTop() - rawBottom());
    }

    public float rawDepth(){
        return Math.abs(rawFront() - rawBack());
    }

    public float rawLeft(){
        return bounds[0];
    }

    public float rawBottom(){
        return bounds[1];
    }

    public float rawBack(){
        return bounds[2];
    }

    public float rawRight(){
        return bounds[4];
    }

    public float rawTop(){
        return bounds[5];
    }

    public float rawFront(){
        return bounds[6];
    }

    public void rawBoundCenterPoint(float[] results){
        results[0] = rawBoundCenterX();
        results[1] = rawBoundCenterY();
        results[2] = rawBoundCenterZ();
    }

    public void rawBoundCenterDistanceToPoint(float[] results, float[] point){
        results[0] = rawBoundCenterX() - point[0];
        results[1] = rawBoundCenterY() - point[1];
        results[2] = rawBoundCenterZ() - point[2];
    }

    public float rawBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(rawBoundCenterX(), 2) + Math.pow(rawBoundCenterY(), 2) + Math.pow(rawBoundCenterZ(), 2));
    }

    public float rawBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(rawBoundCenterX() - point[0], 2) + Math.pow(rawBoundCenterY() - point[1], 2) + Math.pow(rawBoundCenterZ() - point[2], 2));
    }

    public float modelBoundCenterX(){
        return (modelLeft() + modelRight()) / 2f;
    }

    public float modelBoundCenterY(){
        return (modelBottom() + modelTop()) / 2f;
    }

    public float modelBoundCenterZ(){
        return (modelBack() + modelFront()) / 2f;
    }

    public float modelCentroidX(){
        centroidupdater.update(this);
        return centroidmodel[0];
    }

    public float modelCentroidY(){
        centroidupdater.update(this);
        return centroidmodel[1];
    }

    public float modelCentroidZ(){
        centroidupdater.update(this);
        return centroidmodel[2];
    }

    public float[] modelCentroid(){
        centroidupdater.update(this);
        return centroidmodel;
    }

    public float modelWidth(){
        return Math.abs(modelRight() - modelLeft());
    }

    public float modelHeight(){
        return Math.abs(modelTop() - modelBottom());
    }

    public float modelDepth(){
        return Math.abs(modelBack() - modelFront());
    }

    public float modelLeft(){
        modelupdater.update(this);
        return boundsmodel[0];
    }

    public float modelBottom(){
        modelupdater.update(this);
        return boundsmodel[1];
    }

    public float modelBack(){
        modelupdater.update(this);
        return boundsmodel[2];
    }

    public float modelRight(){
        modelupdater.update(this);
        return boundsmodel[4];
    }

    public float modelTop(){
        modelupdater.update(this);
        return boundsmodel[5];
    }

    public float modelFront(){
        modelupdater.update(this);
        return boundsmodel[6];
    }

    public void modelBoundCenterPoint(float[] results){
        results[0] = modelBoundCenterX();
        results[1] = modelBoundCenterY();
        results[2] = modelBoundCenterZ();
    }

    public void modelBoundCenterDistanceFromPoint(float[] results, float[] point){
        results[0] = modelBoundCenterX() - point[0];
        results[1] = modelBoundCenterY() - point[1];
        results[2] = modelBoundCenterZ() - point[2];
    }

    public float modelBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(modelBoundCenterX() - point[0], 2) + Math.pow(modelBoundCenterY() - point[1], 2) + Math.pow(modelBoundCenterZ() - point[2], 2));
    }

    public float modelBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(modelBoundCenterX(), 2) + Math.pow(modelBoundCenterY(), 2) + Math.pow(modelBoundCenterZ(), 2));
    }

    public float mvpBoundCenterX(){
        return (mvpLeft() + mvpRight()) / 2f;
    }

    public float mvpBoundCenterY(){
        return (mvpBottom() + mvpTop()) / 2f;
    }

    public float mvpBoundCenterZ(){
        return (mvpBack() + mvpFront()) / 2f;
    }

    public float mvpCentroidX(){
        centroidupdater.update(this);
        return centroidmodel[0];
    }

    public float mvpCentroidY(){
        centroidupdater.update(this);
        return centroidmodel[1];
    }

    public float mvpCentroidZ(){
        centroidupdater.update(this);
        return centroidmodel[2];
    }

    public float[] mvpCentroid(){
        centroidupdater.update(this);
        return centroidmvp;
    }

    public float mvpWidth(){
        return Math.abs(mvpRight() - mvpLeft());
    }

    public float mvpHeight(){
        return Math.abs(mvpTop() - mvpBottom());
    }

    public float mvpDepth(){
        return Math.abs(mvpFront() - mvpBack());
    }

    public float mvpLeft(){
        mvpupdater.update(this);
        return boundsmvp[0];
    }

    public float mvpBottom(){
        mvpupdater.update(this);
        return boundsmvp[1];
    }

    public float mvpBack(){
        mvpupdater.update(this);
        return boundsmvp[2];
    }

    public float mvpRight(){
        mvpupdater.update(this);
        return boundsmvp[4];
    }

    public float mvpTop(){
        mvpupdater.update(this);
        return boundsmvp[5];
    }

    public float mvpFront(){
        mvpupdater.update(this);
        return boundsmvp[6];
    }

    public void mvpBoundCenterPoint(float[] results){
        results[0] = mvpBoundCenterX();
        results[1] = mvpBoundCenterY();
        results[2] = mvpBoundCenterZ();
    }

    public void mvpBoundCenterDistanceFromPoint(float[] results, float[] point){
        results[0] = mvpBoundCenterX() - point[0];
        results[1] = mvpBoundCenterY() - point[1];
        results[2] = mvpBoundCenterZ() - point[2];
    }

    public float mvpBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(mvpBoundCenterX() - point[0], 2) + Math.pow(mvpBoundCenterY() - point[1], 2) + Math.pow(mvpBoundCenterZ() - point[2], 2));
    }

    public float mvpBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(mvpBoundCenterX(), 2) + Math.pow(mvpBoundCenterY(), 2) + Math.pow(mvpBoundCenterZ(), 2));
    }

    public FSInstance instance(){
        return instance;
    }

    public VLListType<FSBounds> mainBounds(){
        return mainbounds;
    }

    public VLListType<FSBounds> inputBounds(){
        return inputbounds;
    }

    public static final class InstanceCollision{

        public FSBounds.Collision collision;
        public int sourceindex;
        public int targetindex;

        public InstanceCollision(FSBounds.Collision collision, int sourceindex, int targetindex){
            this.collision = collision;
            this.sourceindex = sourceindex;
            this.targetindex = targetindex;
        }

        public InstanceCollision(){

        }
    }
}
