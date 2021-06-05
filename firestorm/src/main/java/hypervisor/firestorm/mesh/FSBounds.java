package hypervisor.firestorm.mesh;

import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.utils.VLUpdater;

public abstract class FSBounds implements VLCopyable<FSBounds>{

    public static final long FLAG_FORCE_DUPLICATE_POINTS = 0x1F;
    public static final long FLAG_FORCE_REFERENCE_POINTS = 0x2F;

    public static final Mode MODE_X_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelWidth() / 100f;
        }
    };
    public static final Mode MODE_Y_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelHeight() / 100f;
        }
    };
    public static final Mode MODE_Z_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelDepth() / 100f;
        }
    };
    public static final Mode MODE_X_RELATIVE = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelLeft() + coefficient;
        }
    };
    public static final Mode MODE_Y_RELATIVE = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBottom() + coefficient;
        }
    };
    public static final Mode MODE_Z_RELATIVE = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBack() + coefficient;
        }
    };
    public static final Mode MODE_X_RELATIVE_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelLeft() + coefficient * schematics.modelWidth() / 100f;
        }
    };
    public static final Mode MODE_Y_RELATIVE_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBottom() + coefficient * schematics.modelHeight() / 100f;
        }
    };
    public static final Mode MODE_Z_RELATIVE_VOLUMETRIC = new Mode(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBack() + coefficient * schematics.modelDepth() / 100f;
        }
    };
    public static final Mode MODE_DIRECT_VALUE = new DirectMode();

    private static final VLUpdater<FSBounds> UPDATE = new VLUpdater<FSBounds>(){
        @Override
        public void update(FSBounds target){
            target.recalculate();
            target.updater = UPDATE_NOTHING;
        }
    };

    public static final int DEPTH_SHALLOW_POINTS = 1;

    protected FSSchematics schematics;
    protected VLUpdater<FSBounds> updater;
    protected VLListType<Point> points;

    protected FSBounds(FSSchematics schematics){
        this.schematics = schematics;
    }

    protected FSBounds(){

    }

    protected final void initialize(Point offset, int pointscapacity){
        this.points = new VLListType<>(pointscapacity, pointscapacity);

        points.add(offset);
        markForUpdate();
    }

    @Override
    public void copy(FSBounds src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            points = src.points;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            points = src.points.duplicate(VLCopyable.FLAG_DUPLICATE);

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_FORCE_REFERENCE_POINTS) == FLAG_FORCE_REFERENCE_POINTS){
                points = src.points.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_REFERENCE_ARRAY);

            }else if((flags & FLAG_FORCE_DUPLICATE_POINTS) == FLAG_FORCE_DUPLICATE_POINTS){
                points = src.points.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            }else{
                Helper.throwMissingSubFlags("FLAG_CUSTOM", "FLAG_FORCE_REFERENCE_POINTS", "FLAG_FORCE_DUPLICATE_POINTS");
            }

        }else{
            Helper.throwMissingDefaultFlags();
        }

        schematics = src.schematics;
        updater = src.updater;
    }

    @Override
    public abstract FSBounds duplicate(long flags);

    public void markForUpdate(){
        updater = UPDATE;
    }

    public void add(Point point){
        points.add(point);
    }

    public Point offset(){
        return points.get(0);
    }

    public Point point(int index){
        return points.get(index);
    }

    public VLListType<Point> points(){
        return points;
    }

    public int size(){
        return points.size();
    }

    public final void checkForUpdates(){
        updater.update(this);
    }

    protected final void recalculate(){
        Point offset = offset();
        offset.calculate(schematics);
        float[] offsetcoords = offset.coordinates;

        int size = points.size();

        for(int i = 1; i < size; i++){
            Point point = points.get(i);
            point.calculate(schematics);
            point.offsetBy(offsetcoords);
        }

        notifyBasePointsUpdated();
    }

    protected abstract void notifyBasePointsUpdated();

    protected void check(Collision results, FSBounds bounds){
        if(bounds instanceof FSBoundsSphere){
            check(results, (FSBoundsSphere)bounds);

        }else if(bounds instanceof FSBoundsCuboid){
            check(results, (FSBoundsCuboid)bounds);

        }else{
            throw new RuntimeException("Invalid bound type[" + bounds.getClass().getSimpleName() + "]");
        }
    }

    protected void check(Collision results, FSBoundsSphere bounds){
        checkForUpdates();
    }

    protected void check(Collision results, FSBoundsCuboid bounds){
        checkForUpdates();
    }

    public void checkPoint(Collision results, float[] point){
        checkForUpdates();
    }

    public void checkInput(Collision results, float[] near, float[] far){
        checkForUpdates();
    }

    public static final class Point implements VLCopyable<Point>{

        protected Mode[] modes;
        protected float[] coefficients;
        protected float[] coordinates;

        public Point(Mode modeX, Mode modeY, Mode modeZ, float coefficientX, float coefficientY, float coefficientZ){
            this.modes = new Mode[]{
                    modeX, modeY, modeZ
            };
            this.coefficients = new float[]{
                    coefficientX, coefficientY, coefficientZ
            };

            coordinates = new float[3];
        }

        public Point(Point src, long flags){
            copy(src, flags);
        }

        protected Point(){

        }

        @Override
        public void copy(Point src, long flags){
            if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
                modes = src.modes;
                coefficients = src.coefficients;
                coordinates = src.coordinates;

            }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
                modes = src.modes.clone();
                coefficients = src.coefficients.clone();
                coordinates = src.coordinates.clone();

            }else{
                Helper.throwMissingDefaultFlags();
            }
        }

        @Override
        public Point duplicate(long flags){
            return new Point(this, flags);
        }

        public void calculate(FSSchematics schematics){
            coordinates[0] = modes[0].calculate(schematics, coefficients[0]);
            coordinates[1] = modes[1].calculate(schematics, coefficients[1]);
            coordinates[2] = modes[2].calculate(schematics, coefficients[2]);
        }

        public void offsetBy(float[] offset){
            coordinates[0] += offset[0];
            coordinates[1] += offset[1];
            coordinates[2] += offset[2];
        }

        public Mode[] modes(){
            return modes;
        }

        public float[] coefficients(){
            return coefficients;
        }

        public float[] coordinates(){
            return coordinates;
        }
    }

    public static interface Mode{

        float calculate(FSSchematics schematics, float coefficient);
    }

    protected static final class DirectMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient;
        }
    }

    public static class Collision implements VLCopyable<Collision>{

        public boolean collided;
        public int initiatorboundsindex;
        public int targetboundsindex;
        public float distance;

        public Collision(){

        }

        public Collision(Collision src, long flags){
            copy(src, flags);
        }

        @Override
        public void copy(Collision src, long flags){
            collided = src.collided;
            initiatorboundsindex = src.initiatorboundsindex;
            targetboundsindex = src.targetboundsindex;
            distance = src.distance;
        }

        @Override
        public Collision duplicate(long flags){
            return new Collision(this, flags);
        }
    }
}