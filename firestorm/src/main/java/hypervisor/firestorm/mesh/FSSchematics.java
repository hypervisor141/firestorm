package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.engine.FSElements;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.utils.VLUpdater;

public class FSSchematics implements VLCopyable<FSSchematics>{

    protected static final VLUpdater<FSSchematics> UPDATE_CENTROID = new VLUpdater<FSSchematics>(){

        @Override
        public void update(FSSchematics target){
            target.directUpdateModelSpaceCentroid();
        }
    };
    protected static final VLUpdater<FSSchematics> UPDATE_MODEL = new VLUpdater<FSSchematics>(){

        @Override
        public void update(FSSchematics target){
            target.directUpdateModelSpaceBounds();
        }
    };

    protected FSTypeInstance instance;

    protected VLUpdater<FSSchematics> modelspaceupdate;
    protected VLUpdater<FSSchematics> centroidupdater;

    protected VLListType<FSBounds> collisionbounds;
    protected VLListType<InputEntry> inputbounds;

    protected int[] boundsindices;
    protected float[] boundslocalspace;
    protected float[] boundsmodelspace;

    protected float[] centroidlocalspace;
    protected float[] centroidmodelspace;

    public FSSchematics(){

    }

    public FSSchematics(FSSchematics src, long flags){
        copy(src, flags);
    }

    @Override
    public void copy(FSSchematics src, long flags){
        this.instance = src.instance;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            collisionbounds = src.collisionbounds;
            inputbounds = src.inputbounds;
            boundsindices = src.boundsindices;
            boundslocalspace = src.boundslocalspace;
            boundsmodelspace = src.boundsmodelspace;
            centroidlocalspace = src.centroidlocalspace;
            centroidmodelspace = src.centroidmodelspace;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            collisionbounds = src.collisionbounds.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);
            inputbounds = src.inputbounds.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);

            boundsindices = src.boundsindices.clone();
            boundslocalspace = src.boundslocalspace.clone();
            boundsmodelspace = src.boundsmodelspace.clone();
            centroidlocalspace = src.centroidlocalspace.clone();
            centroidmodelspace = src.centroidmodelspace.clone();

        }else{
            Helper.throwMissingDefaultFlags();
        }

        modelspaceupdate = src.modelspaceupdate;
        centroidupdater = src.centroidupdater;
    }

    @Override
    public FSSchematics duplicate(long flags){
        return new FSSchematics(this, flags);
    }

    public void initialize(FSTypeInstance instance){
        this.instance = instance;

        collisionbounds = new VLListType<>(0, 10);
        inputbounds = new VLListType<>(0, 10);

        boundsindices = new int[6];
        boundslocalspace = new float[8];
        boundsmodelspace = new float[boundslocalspace.length];
        centroidlocalspace = new float[4];
        centroidmodelspace = new float[4];

        modelspaceupdate = VLUpdater.UPDATE_NOTHING;
        centroidupdater = VLUpdater.UPDATE_NOTHING;
    }

    public void rebuild(){
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float minz = Float.MAX_VALUE;

        float maxx = -Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        float maxz = -Float.MAX_VALUE;

        centroidlocalspace[0] = 0;
        centroidlocalspace[1] = 0;
        centroidlocalspace[2] = 0;

        int unitsize = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];
        float[] positions = instance.positions().array;
        int size = positions.length;

        for(int index = 0; index < size; index += unitsize){
            float x = positions[index];
            float y = positions[index + 1];
            float z = positions[index + 2];

            centroidlocalspace[0] += x;
            centroidlocalspace[1] += y;
            centroidlocalspace[2] += z;

            if(minx > x){
                minx = x;
                boundsindices[0] = index;
                boundslocalspace[0] = x;
            }
            if(miny > y){
                miny = y;
                boundsindices[1] = index + 1;
                boundslocalspace[1] = y;
            }
            if(minz > z){
                minz = z;
                boundsindices[2] = index + 2;
                boundslocalspace[2] = z;
            }
            if(maxx < x){
                maxx = x;
                boundsindices[3] = index + 1;
                boundslocalspace[4] = x;
            }
            if(maxy < y){
                maxy = y;
                boundsindices[4] = index + 2;
                boundslocalspace[5] = y;
            }
            if(maxz < z){
                maxz = z;
                boundsindices[5] = index + 3;
                boundslocalspace[6] = z;
            }
        }

        int vertexcount = size / unitsize;
        centroidlocalspace[0] /= vertexcount;
        centroidlocalspace[1] /= vertexcount;
        centroidlocalspace[2] /= vertexcount;

        if(!checkFixLocalSpaceFlatness()){
            requestFullUpdate();
        }
    }

    public boolean checkFixLocalSpaceFlatness(){
        boolean updated = false;

        float[] positions = instance.positions().array;

        float width = localSpaceWidth();
        float height = localSpaceHeight();
        float depth = localSpaceDepth();

        if(width == 0){
            if(depth != 0){
                boundsindices[0] = boundsindices[2];
                boundsindices[3] = boundsindices[5];
                boundslocalspace[0] = positions[boundsindices[2]];
                boundslocalspace[4] = positions[boundsindices[5]];

                updated = true;

            }else if(height != 0){
                boundsindices[0] = boundsindices[1];
                boundsindices[3] = boundsindices[4];
                boundslocalspace[0] = positions[boundsindices[1]];
                boundslocalspace[4] = positions[boundsindices[4]];

                updated = true;
            }
        }
        if(height == 0){
            if(width != 0){
                boundsindices[1] = boundsindices[0];
                boundsindices[4] = boundsindices[3];
                boundslocalspace[1] = positions[boundsindices[1]];
                boundslocalspace[5] = positions[boundsindices[4]];

                updated = true;

            }else if(depth != 0){
                boundsindices[1] = boundsindices[2];
                boundsindices[4] = boundsindices[5];
                boundslocalspace[1] = positions[boundsindices[2]];
                boundslocalspace[5] = positions[boundsindices[5]];

                updated = true;
            }
        }
        if(depth == 0){
            if(height != 0){
                boundsindices[2] = boundsindices[1];
                boundsindices[5] = boundsindices[4];
                boundslocalspace[2] = positions[boundsindices[1]];
                boundslocalspace[6] = positions[boundsindices[4]];

                updated = true;

            }else if(width != 0){
                boundsindices[2] = boundsindices[0];
                boundsindices[5] = boundsindices[3];
                boundslocalspace[2] = positions[boundsindices[0]];
                boundslocalspace[6] = positions[boundsindices[3]];

                updated = true;
            }
        }

        if(updated){
            requestFullUpdate();
        }

        return updated;
    }

    public boolean refillLocalSpaceBounds(){
        float[] positions = instance.positions().array;
        int size = boundsindices.length;

        for(int i = 0; i < size; i++){
            boundslocalspace[i] = positions[boundsindices[i]];
        }

        return checkFixLocalSpaceFlatness();
    }

    public void rebuildLocalSpaceCentroid(){
        centroidlocalspace[0] = 0F;
        centroidlocalspace[1] = 0F;
        centroidlocalspace[2] = 0F;

        int unitsize = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];
        float[] positions = instance.positions().array;
        int size = positions.length;

        for(int index = 0; index < size; index += unitsize){
            centroidlocalspace[0] += positions[index];
            centroidlocalspace[1] += positions[index + 1];
            centroidlocalspace[2] += positions[index + 2];
        }

        int vertexcount = size / unitsize;
        centroidlocalspace[0] /= vertexcount;
        centroidlocalspace[1] /= vertexcount;
        centroidlocalspace[2] /= vertexcount;
        centroidlocalspace[3] = 1F;

        requestUpdateModelSpaceCentroid();
    }

    public boolean checkSortLocalSpaceBounds(){
        boolean reordered = false;

        for(int i = 0; i < 3; i++){
            int offset = i + 4;
            float cache = boundslocalspace[i];

            if(cache > boundslocalspace[offset]){
                boundslocalspace[i] = boundslocalspace[offset];
                boundslocalspace[offset] = cache;

                int offsetindices = i + 3;
                int cachei = boundsindices[i];

                boundsindices[i] = boundsindices[offsetindices];
                boundsindices[offsetindices] = cachei;

                reordered = true;
            }
        }

        if(reordered){
            requestFullUpdate();
        }

        return reordered;
    }

    public boolean checkSortModelSpaceBounds(){
        boolean reordered = false;

        for(int i = 0; i < 3; i++){
            int offset = i + 4;
            float cache = boundsmodelspace[i];

            if(cache > boundsmodelspace[offset]){
                boundsmodelspace[i] = boundsmodelspace[offset];
                boundsmodelspace[offset] = cache;

                reordered = true;
            }
        }

        if(reordered){
            requestUpdateModelSpaceCentroid();
            requestUpdateCollisionBounds();
            requestUpdateInputBounds();
        }

        return reordered;
    }

    public void requestFullUpdate(){
        requestUpdateModelSpaceBounds();
        requestUpdateModelSpaceCentroid();
        requestUpdateCollisionBounds();
        requestUpdateInputBounds();
    }

    public void requestUpdateModelSpaceBounds(){
        modelspaceupdate = UPDATE_MODEL;
    }

    public void requestUpdateModelSpaceCentroid(){
        centroidupdater = UPDATE_CENTROID;
    }

    public void requestUpdateCollisionBounds(){
        int size = collisionbounds.size();

        for(int i = 0; i < size; i++){
            collisionbounds.get(i).requestUpdate();
        }
    }

    public void requestUpdateInputBounds(){
        int size = inputbounds.size();

        for(int i = 0; i < size; i++){
            inputbounds.get(i).bounds.requestUpdate();
        }
    }

    public void directReloadLocalSpaceBounds(){
        float[] positions = instance.positions().array;

        boundslocalspace[0] = positions[boundsindices[0]];
        boundslocalspace[1] = positions[boundsindices[1]];
        boundslocalspace[2] = positions[boundsindices[2]];
        boundslocalspace[4] = positions[boundsindices[3]];
        boundslocalspace[5] = positions[boundsindices[4]];
        boundslocalspace[6] = positions[boundsindices[5]];

        checkFixLocalSpaceFlatness();

        if(!checkSortLocalSpaceBounds()){
            requestFullUpdate();
        }
    }

    public void directUpdateModelSpaceBounds(){
        FSArrayModel modelSpace = instance.model();

        modelSpace.transformPoint(boundsmodelspace, 0, boundslocalspace, 0);
        modelSpace.transformPoint(boundsmodelspace, 4, boundslocalspace, 4);

        if(!checkSortModelSpaceBounds()){
            requestUpdateModelSpaceCentroid();
            requestUpdateCollisionBounds();
            requestUpdateInputBounds();
        }

        modelspaceupdate = VLUpdater.UPDATE_NOTHING;
    }

    public void directUpdateModelSpaceCentroid(){
        instance.model().transformPoint(centroidmodelspace, 0, centroidlocalspace, 0);
        centroidupdater = VLUpdater.UPDATE_NOTHING;
    }

    public void directUpdateCollisionBounds(){
        int size = collisionbounds.size();

        for(int i = 0; i < size; i++){
            collisionbounds.get(i).update();
        }
    }

    public void directUpdateInputBounds(){
        int size = inputbounds.size();

        for(int i = 0; i < size; i++){
            inputbounds.get(i).bounds.update();
        }
    }

    public float localSpaceBoundCenterX(){
        return (localSpaceLeft() + localSpaceRight()) / 2f;
    }

    public float localSpaceBoundCenterY(){
        return (localSpaceBottom() + localSpaceTop()) / 2f;
    }

    public float localSpaceBoundCenterZ(){
        return (localSpaceBack() + localSpaceFront()) / 2f;
    }

    public float localSpaceWidth(){
        return localSpaceRight() - localSpaceLeft();
    }

    public float localSpaceHeight(){
        return localSpaceTop() - localSpaceBottom();
    }

    public float localSpaceDepth(){
        return localSpaceFront() - localSpaceBack();
    }

    public float localSpaceLeft(){
        return boundslocalspace[0];
    }

    public float localSpaceBottom(){
        return boundslocalspace[1];
    }

    public float localSpaceBack(){
        return boundslocalspace[2];
    }

    public float localSpaceRight(){
        return boundslocalspace[4];
    }

    public float localSpaceTop(){
        return boundslocalspace[5];
    }

    public float localSpaceFront(){
        return boundslocalspace[6];
    }

    public void localSpaceBoundCenterPoint(float[] results){
        results[0] = localSpaceBoundCenterX();
        results[1] = localSpaceBoundCenterY();
        results[2] = localSpaceBoundCenterZ();
    }

    public void localSpaceBoundCenterDistanceToPoint(float[] results, float[] point){
        results[0] = localSpaceBoundCenterX() - point[0];
        results[1] = localSpaceBoundCenterY() - point[1];
        results[2] = localSpaceBoundCenterZ() - point[2];
    }

    public float localSpaceBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(localSpaceBoundCenterX(), 2) + Math.pow(localSpaceBoundCenterY(), 2) + Math.pow(localSpaceBoundCenterZ(), 2));
    }

    public float localSpaceBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(localSpaceBoundCenterX() - point[0], 2) + Math.pow(localSpaceBoundCenterY() - point[1], 2) + Math.pow(localSpaceBoundCenterZ() - point[2], 2));
    }

    public float localSpaceCentroidX(){
        centroidupdater.update(this);
        return centroidlocalspace[0];
    }

    public float localSpaceCentroidY(){
        centroidupdater.update(this);
        return centroidlocalspace[1];
    }

    public float localSpaceCentroidZ(){
        centroidupdater.update(this);
        return centroidlocalspace[2];
    }

    public float modelSpaceBoundCenterX(){
        return (modelSpaceLeft() + modelSpaceRight()) / 2f;
    }

    public float modelSpaceBoundCenterY(){
        return (modelSpaceBottom() + modelSpaceTop()) / 2f;
    }

    public float modelSpaceBoundCenterZ(){
        return (modelSpaceBack() + modelSpaceFront()) / 2f;
    }

    public float modelSpaceCentroidX(){
        centroidupdater.update(this);
        return centroidmodelspace[0];
    }

    public float modelSpaceCentroidY(){
        centroidupdater.update(this);
        return centroidmodelspace[1];
    }

    public float modelSpaceCentroidZ(){
        centroidupdater.update(this);
        return centroidmodelspace[2];
    }

    public float[] modelSpaceCentroid(){
        centroidupdater.update(this);
        return centroidmodelspace;
    }

    public float modelSpaceWidth(){
        return modelSpaceRight() - modelSpaceLeft();
    }

    public float modelSpaceHeight(){
        return modelSpaceTop() - modelSpaceBottom();
    }

    public float modelSpaceDepth(){
        return modelSpaceFront() - modelSpaceBack();
    }

    public float modelSpaceLeft(){
        modelspaceupdate.update(this);
        return boundsmodelspace[0];
    }

    public float modelSpaceBottom(){
        modelspaceupdate.update(this);
        return boundsmodelspace[1];
    }

    public float modelSpaceBack(){
        modelspaceupdate.update(this);
        return boundsmodelspace[2];
    }

    public float modelSpaceRight(){
        modelspaceupdate.update(this);
        return boundsmodelspace[4];
    }

    public float modelSpaceTop(){
        modelspaceupdate.update(this);
        return boundsmodelspace[5];
    }

    public float modelSpaceFront(){
        modelspaceupdate.update(this);
        return boundsmodelspace[6];
    }

    public void modelSpaceBoundCenterPoint(float[] results){
        results[0] = modelSpaceBoundCenterX();
        results[1] = modelSpaceBoundCenterY();
        results[2] = modelSpaceBoundCenterZ();
    }

    public void modelSpaceBoundCenterDistanceFromPoint(float[] results, float[] point){
        results[0] = modelSpaceBoundCenterX() - point[0];
        results[1] = modelSpaceBoundCenterY() - point[1];
        results[2] = modelSpaceBoundCenterZ() - point[2];
    }

    public float modelSpaceBoundCenterLengthFromPoint(float[] point){
        return (float)Math.sqrt(Math.pow(modelSpaceBoundCenterX() - point[0], 2) + Math.pow(modelSpaceBoundCenterY() - point[1], 2) + Math.pow(modelSpaceBoundCenterZ() - point[2], 2));
    }

    public float modelSpaceBoundCenterVectorLength(){
        return (float)Math.sqrt(Math.pow(modelSpaceBoundCenterX(), 2) + Math.pow(modelSpaceBoundCenterY(), 2) + Math.pow(modelSpaceBoundCenterZ(), 2));
    }

    public void checkCollision(FSBounds.Collision results, FSTypeInstance target){
        int size = collisionbounds.size();

        for(int i = 0; i < size; i++){
            int index = target.schematics().checkCollision(results, collisionbounds.get(i));

            if(index != -1){
                results.initiatorboundsindex = i;
                results.targetboundsindex = index;

                return;
            }
        }
    }

    public int checkCollision(FSBounds.Collision results, FSBounds target){
        int size = collisionbounds.size();

        for(int i = 0; i < size; i++){
            collisionbounds.get(i).check(results, target);

            if(results.collided){
                return 1;
            }
        }

        return -1;
    }

    public int checkPointCollision(FSBounds.Collision results, float[] point){
        int size = collisionbounds.size();

        for(int i = 0; i < size; i++){
            collisionbounds.get(i).checkPoint(results, point);

            if(results.collided){
                return i;
            }
        }

        return -1;
    }

    public void checkInputCollision(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
        int size = inputbounds.size();
        FSBounds.Collision results = new FSBounds.Collision();

        for(int i = 0; i < size; i++){
            results.initiatorboundsindex = i;
            inputbounds.get(i).check(results, e1, e2, f1, f2, near, far);
        }
    }

    public FSTypeInstance instance(){
        return instance;
    }

    public int[] localSpaceBoundsIndices(){
        return boundsindices;
    }

    public float[] localSpaceBounds(){
        return boundslocalspace;
    }

    public float[] centroidLocalSpace(){
        return centroidlocalspace;
    }

    public float[] modelSpaceBounds(){
        modelspaceupdate.update(this);
        return boundsmodelspace;
    }

    public float[] centroidModelSpace(){
        centroidupdater.update(this);
        return centroidmodelspace;
    }

    public VLListType<FSBounds> collisionBounds(){
        return collisionbounds;
    }

    public VLListType<InputEntry> inputBounds(){
        return inputbounds;
    }

    public static class InputEntry implements VLCopyable<InputEntry>{

        protected FSBounds bounds;
        protected InputProcessor processor;

        public InputEntry(FSBounds bounds, InputProcessor processor){
            this.bounds = bounds;
            this.processor = processor;
        }

        public InputEntry(InputEntry src, long flags){
            copy(src, flags);
        }

        protected InputEntry(){

        }

        public void check(FSBounds.Collision results, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            bounds.checkInput(results, near, far);

            if(results.collided){
                processor.activated(results, e1, e2, f1, f2, near, far);
            }
        }

        @Override
        public void copy(InputEntry src, long flags){
            if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
                bounds = src.bounds;

            }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
                bounds = src.bounds.duplicate(FSBounds.FLAG_DUPLICATE_POINTS);

            }else{
                Helper.throwMissingDefaultFlags();
            }

            processor = src.processor;
        }

        @Override
        public InputEntry duplicate(long flags){
            return new InputEntry(this, flags);
        }
    }

    public interface InputProcessor{

        void activated(FSBounds.Collision results, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }
}
