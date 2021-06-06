package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.engine.FSElements;
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
            s.updateModelBounds();
            s.modelupdater = UPDATE_NOTHING;
        }
    };

    protected FSTypeInstance instance;

    protected VLUpdater<FSSchematics> modelupdater;
    protected VLUpdater<FSSchematics> centroidupdater;

    protected VLListType<FSBounds> mainbounds;
    protected VLListType<InputChecker> inputcheckers;

    protected int[] boundsindices;
    protected float[] basebounds;
    protected float[] boundsmodel;

    protected float[] centroidbase;
    protected float[] centroidmodel;

    public FSSchematics(){

    }

    public FSSchematics(FSSchematics src, long flags){
        copy(src, flags);
    }

    @Override
    public void copy(FSSchematics src, long flags){
        this.instance = src.instance;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            mainbounds = src.mainbounds;
            inputcheckers = src.inputcheckers;
            boundsindices = src.boundsindices;
            basebounds = src.basebounds;
            boundsmodel = src.boundsmodel;
            centroidbase = src.centroidbase;
            centroidmodel = src.centroidmodel;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            mainbounds = src.mainbounds.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            inputcheckers = src.inputcheckers.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            boundsindices = src.boundsindices.clone();
            basebounds = src.basebounds.clone();
            boundsmodel = src.boundsmodel.clone();
            centroidbase = src.centroidbase.clone();
            centroidmodel = src.centroidmodel.clone();

        }else{
            Helper.throwMissingDefaultFlags();
        }

        modelupdater = src.modelupdater;
        centroidupdater = src.centroidupdater;
    }

    @Override
    public FSSchematics duplicate(long flags){
        return new FSSchematics(this, flags);
    }

    public void initialize(FSTypeInstance instance){
        this.instance = instance;

        mainbounds = new VLListType<>(0, 10);
        inputcheckers = new VLListType<>(0, 10);

        boundsindices = new int[6];
        basebounds = new float[8];
        boundsmodel = new float[basebounds.length];
        centroidbase = new float[4];
        centroidmodel = new float[4];

        modelupdater = UPDATE_MODEL;
        centroidupdater = UPDATE_CENTROID;
    }

    public void rebuild(){
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float minz = Float.MAX_VALUE;

        float maxx = -Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        float maxz = -Float.MAX_VALUE;

        centroidbase[0] = 0;
        centroidbase[1] = 0;
        centroidbase[2] = 0;

        int unitsize = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];
        float[] positions = instance.positions().provider();
        int size = positions.length;

        for(int index = 0; index < size; index += unitsize){
            float x = positions[index];
            float y = positions[index + 1];
            float z = positions[index + 2];

            centroidbase[0] += x;
            centroidbase[1] += y;
            centroidbase[2] += z;

            if(minx > x){
                minx = x;
                boundsindices[0] = index;
                basebounds[0] = x;
            }
            if(miny > y){
                miny = y;
                boundsindices[1] = index + 1;
                basebounds[1] = y;
            }
            if(minz > z){
                minz = z;
                boundsindices[2] = index + 2;
                basebounds[2] = z;
            }

            if(maxx < x){
                maxx = x;
                boundsindices[4] = index + 1;
                basebounds[4] = x;
            }
            if(maxy < y){
                maxy = y;
                boundsindices[5] = index + 2;
                basebounds[5] = y;
            }
            if(maxz < z){
                maxz = z;
                boundsindices[6] = index + 3;
                basebounds[6] = z;
            }
        }

        int vertexcount = size / unitsize;
        centroidbase[0] /= vertexcount;
        centroidbase[1] /= vertexcount;
        centroidbase[2] /= vertexcount;

        syncBaseBounds();
    }

    public boolean checkBaseBoundsUpdateRequirement(){
        float[] positions = instance.positions().provider();

        return positions[boundsindices[0]] != basebounds[0] ||
                positions[boundsindices[1]] != basebounds[1] ||
                positions[boundsindices[2]] != basebounds[2] ||
                positions[boundsindices[3]] != basebounds[4] ||
                positions[boundsindices[4]] != basebounds[5] ||
                positions[boundsindices[5]] != basebounds[6];
    }

    public void syncBaseBounds(){
        int size = basebounds.length;
        float[] positions = instance.positions().provider();

        for(int i = 0; i < size; i++){
            basebounds[i] = positions[boundsindices[i]];
        }

        boundsmodel = basebounds.clone();
    }

    public void updateBaseBoundsOrder(){
        int cachei;
        float cachef;

        for(int i = 0; i < 3; i++){
            int indicesoffset = i + 3;

            if(basebounds[i] > basebounds[indicesoffset]){
                int boundsoffset = i + 4;

                cachei = boundsindices[i];

                boundsindices[i] = boundsindices[indicesoffset];
                boundsindices[indicesoffset] = cachei;

                cachef = basebounds[i];
                basebounds[i] = basebounds[boundsoffset];
                basebounds[boundsoffset] = cachef;

                cachef = boundsmodel[i];
                boundsmodel[i] = boundsmodel[boundsoffset];
                boundsmodel[boundsoffset] = cachef;
            }
        }
    }

    private void updateModelBounds(){
        FSArrayModel model = instance.model();

        model.transformPoint(boundsmodel, 0, basebounds, 0);
        model.transformPoint(boundsmodel, 4, basebounds, 4);
    }

    private void updateCentroid(){
        instance.model().transformPoint(centroidmodel, 0, centroidbase, 0);
    }

    public void checkCollision(FSBounds.Collision results, FSTypeInstance target){
        int index = -1;
        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            index = target.schematics().checkCollision(results, mainbounds.get(i));

            if(index != -1){
                results.initiatorboundsindex = i;
                results.targetboundsindex = index;

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

    public void checkInputCollision(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
        int size = inputcheckers.size();
        FSBounds.Collision results = new FSBounds.Collision();

        for(int i = 0; i < size; i++){
            results.initiatorboundsindex = i;
            inputcheckers.get(i).check(results, e1, e2, f1, f2, near, far);
        }
    }

    public void markFullUpdate(){
        markModelBoundsForUpdate();
        markCentroidBoundsForUpdate();
        markCollisionBoundsForUpdate();
        markInputBoundsForUpdate();
    }

    public void markModelBoundsForUpdate(){
        modelupdater = UPDATE_MODEL;
    }

    public void markCentroidBoundsForUpdate(){
        centroidupdater = UPDATE_CENTROID;
    }

    public void markCollisionBoundsForUpdate(){
        int size = mainbounds.size();

        for(int i = 0; i < size; i++){
            mainbounds.get(i).markForUpdate();
        }
    }

    public void markInputBoundsForUpdate(){
        int size = inputcheckers.size();

        for(int i = 0; i < size; i++){
            inputcheckers.get(i).bounds.markForUpdate();
        }
    }

    public float baseX(){
        return baseRight();
    }

    public float baseY(){
        return baseTop();
    }

    public float baseZ(){
        return baseFront();
    }

    public float baseCentroidX(){
        centroidupdater.update(this);
        return centroidbase[0];
    }

    public float baseCentroidY(){
        centroidupdater.update(this);
        return centroidbase[1];
    }

    public float baseCentroidZ(){
        centroidupdater.update(this);
        return centroidbase[2];
    }

    public float[] baseCentroid(){
        centroidupdater.update(this);
        return centroidbase;
    }

    public float baseBoundCenterX(){
        return (baseLeft() + baseRight()) / 2f;
    }

    public float baseBoundCenterY(){
        return (baseBottom() + baseTop()) / 2f;
    }

    public float baseBoundCenterZ(){
        return (baseFront() + baseBack()) / 2f;
    }

    public float baseWidth(){
        return Math.abs(baseRight() - baseLeft());
    }

    public float baseHeight(){
        return Math.abs(baseTop() - baseBottom());
    }

    public float baseDepth(){
        return Math.abs(baseFront() - baseBack());
    }

    public float baseLeft(){
        return basebounds[0];
    }

    public float baseBottom(){
        return basebounds[1];
    }

    public float baseBack(){
        return basebounds[2];
    }

    public float baseRight(){
        return basebounds[4];
    }

    public float baseTop(){
        return basebounds[5];
    }

    public float baseFront(){
        return basebounds[6];
    }

    public void baseBoundCenterPoint(float[] results){
        results[0] = baseBoundCenterX();
        results[1] = baseBoundCenterY();
        results[2] = baseBoundCenterZ();
    }

    public void baseBoundCenterDistanceToPoint(float[] results, float[] point){
        results[0] = baseBoundCenterX() - point[0];
        results[1] = baseBoundCenterY() - point[1];
        results[2] = baseBoundCenterZ() - point[2];
    }

    public float baseBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(baseBoundCenterX(), 2) + Math.pow(baseBoundCenterY(), 2) + Math.pow(baseBoundCenterZ(), 2));
    }

    public float baseBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(baseBoundCenterX() - point[0], 2) + Math.pow(baseBoundCenterY() - point[1], 2) + Math.pow(baseBoundCenterZ() - point[2], 2));
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

    public FSTypeInstance instance(){
        return instance;
    }

    public int[] baseBoundsIndices(){
        return boundsindices;
    }

    public float[] baseBounds(){
        return basebounds;
    }

    public float[] modelBounds(){
        return boundsmodel;
    }

    public float[] centroidBase(){
        return centroidbase;
    }

    public float[] centroidModel(){
        return centroidmodel;
    }

    public VLListType<FSBounds> mainBounds(){
        return mainbounds;
    }

    public VLListType<InputChecker> inputCheckers(){
        return inputcheckers;
    }

    public static class InputChecker implements VLCopyable<InputChecker>{

        protected FSBounds bounds;
        protected InputProcessor processor;

        public InputChecker(FSBounds bounds, InputProcessor processor){
            this.bounds = bounds;
            this.processor = processor;
        }

        public InputChecker(InputChecker src, long flags){
            copy(src, flags);
        }

        protected InputChecker(){

        }

        public void check(FSBounds.Collision results, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            bounds.checkInput(results, near, far);

            if(results.collided){
                processor.activated(results, e1, e2, f1, f2, near, far);
            }
        }

        @Override
        public void copy(InputChecker src, long flags){
            if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
                bounds = src.bounds;

            }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
                bounds = src.bounds.duplicate(FSBounds.FLAG_FORCE_DUPLICATE_POINTS);

            }else{
                Helper.throwMissingDefaultFlags();
            }

            processor = src.processor;
        }

        @Override
        public InputChecker duplicate(long flags){
            return new InputChecker(this, flags);
        }
    }

    public interface InputProcessor{

        void activated(FSBounds.Collision results, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }
}
