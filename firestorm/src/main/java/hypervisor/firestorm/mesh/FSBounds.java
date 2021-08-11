package hypervisor.firestorm.mesh;

import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.utils.VLUpdater;

public abstract class FSBounds implements VLCopyable<FSBounds>{

    public static final long FLAG_DUPLICATE_POINTS = 0x1L;
    public static final long FLAG_REFERENCE_POINTS = 0x2L;

    public static final CalculationMethod MODE_X_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelSpaceWidth() / 100f;
        }
    };
    public static final CalculationMethod MODE_Y_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelSpaceHeight() / 100f;
        }
    };
    public static final CalculationMethod MODE_Z_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelSpaceDepth() / 100f;
        }
    };
    public static final CalculationMethod MODE_X_RELATIVE = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceLeft() + coefficient;
        }
    };
    public static final CalculationMethod MODE_Y_RELATIVE = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceBottom() + coefficient;
        }
    };
    public static final CalculationMethod MODE_Z_RELATIVE = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceBack() + coefficient;
        }
    };
    public static final CalculationMethod MODE_X_RELATIVE_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceLeft() + coefficient * schematics.modelSpaceWidth() / 100f;
        }
    };
    public static final CalculationMethod MODE_Y_RELATIVE_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceBottom() + coefficient * schematics.modelSpaceHeight() / 100f;
        }
    };
    public static final CalculationMethod MODE_Z_RELATIVE_VOLUMETRIC = new CalculationMethod(){

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelSpaceBack() + coefficient * schematics.modelSpaceDepth() / 100f;
        }
    };
    public static final CalculationMethod MODE_DIRECT_VALUE = new DirectMethod();

    private static final VLUpdater<FSBounds> UPDATE = new VLUpdater<FSBounds>(){
        @Override
        public void update(FSBounds target){
            target.update();
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
        requestUpdate();
    }

    @Override
    public void copy(FSBounds src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            points = src.points;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            points = src.points.duplicate(VLCopyable.FLAG_DUPLICATE);

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_REFERENCE_POINTS) == FLAG_REFERENCE_POINTS){
                points = src.points.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);

            }else if((flags & FLAG_DUPLICATE_POINTS) == FLAG_DUPLICATE_POINTS){
                points = src.points.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);

            }else{
                Helper.throwMissingSubFlags("FLAG_CUSTOM", "FLAG_REFERENCE_POINTS", "FLAG_DUPLICATE_POINTS");
            }

        }else{
            Helper.throwMissingDefaultFlags();
        }

        schematics = src.schematics;
        updater = src.updater;
    }

    @Override
    public abstract FSBounds duplicate(long flags);

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

    public void requestUpdate(){
        updater = UPDATE;
    }

    public final void checkForUpdates(){
        updater.update(this);
    }

    protected abstract void notifyBasePointsUpdated();

    protected final void update(){
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
        updater = VLUpdater.UPDATE_NOTHING;
    }

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

        protected CalculationMethod[] modes;
        protected float[] coefficients;
        protected float[] coordinates;

        public Point(CalculationMethod modeX, CalculationMethod modeY, CalculationMethod modeZ, float coefficientX, float coefficientY, float coefficientZ){
            this.modes = new CalculationMethod[]{
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

        public CalculationMethod[] modes(){
            return modes;
        }

        public float[] coefficients(){
            return coefficients;
        }

        public float[] coordinates(){
            return coordinates;
        }
    }

    public interface CalculationMethod{

        float calculate(FSSchematics schematics, float coefficient);
    }

    protected static final class DirectMethod implements CalculationMethod{

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