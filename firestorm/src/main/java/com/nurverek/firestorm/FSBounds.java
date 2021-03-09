package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLUpdater;

public abstract class FSBounds{

    protected static final float[] CACHE1 = new float[4];
    protected static final float[] CACHE2 = new float[4];

    public static final Mode MODE_X_VOLUMETRIC = new XVolumentricMode();
    public static final Mode MODE_Y_VOLUMETRIC = new YVolumetricMode();
    public static final Mode MODE_Z_VOLUMETRIC = new ZVolumetricMode();
    public static final Mode MODE_X_OFFSET = new XOffsetMode();
    public static final Mode MODE_Y_OFFSET = new YOffsetMode();
    public static final Mode MODE_Z_OFFSET = new ZOffsetMode();
    public static final Mode MODE_X_OFFSET_VOLUMETRIC = new XOffsetVolumeMode();
    public static final Mode MODE_Y_OFFSET_VOLUMETRIC = new YOffsetVolumeMode();
    public static final Mode MODE_Z_OFFSET_VOLUMETRIC = new ZOffsetVolumeMode();
    public static final Mode MODE_DIRECT_VALUE = new DirectMode();

    private static final VLUpdater<FSBounds> UPDATE = new VLUpdater<FSBounds>(){
        @Override
        public void update(FSBounds s){
            s.recalculate();
            s.updater = UPDATE_NOTHING;
        }
    };

    protected FSSchematics schematics;
    private VLUpdater<FSBounds> updater;

    protected Point offset;
    protected VLListType<Point> points;

    protected FSBounds(FSSchematics schematics){
        this.schematics = schematics;
    }

    protected final void initialize(Point offset, VLListType<Point> points){
        this.offset = offset;
        this.points = points;

        markForUpdate();
    }

    protected void markForUpdate(){
        updater = UPDATE;
    }

    public Point offset(){
        return offset;
    }

    public Point point(int index){
        return points.get(index);
    }

    public VLListType<Point> points(){
        return points;
    }

    public final void update(){
        updater.update(this);
    }

    protected final void recalculate(){
        offset.calculate(schematics);
        float[] offsetcoords = offset.coordinates;

        Point point;
        int size = points.size();

        for(int i = 0; i < size; i++){
            point = points.get(i);
            point.calculate(schematics);
            point.coordinates[0] += offsetcoords[0];
            point.coordinates[1] += offsetcoords[1];
            point.coordinates[2] += offsetcoords[2];
        }

        updateData();
    }

    protected abstract void updateData();

    protected void check(Collision results, FSBounds bounds){
        if(bounds instanceof FSBoundsSphere){
            check(results, (FSBoundsSphere)bounds);

        }else if(bounds instanceof FSBoundsCuboid){
            check(results, (FSBoundsCuboid)bounds);

        }else{
            throw new RuntimeException("Invalid bound type[" + bounds.getClass().getSimpleName() + "]");
        }
    }

    protected abstract void check(Collision results, FSBoundsSphere bounds);

    protected abstract void check(Collision results, FSBoundsCuboid bounds);

    public abstract void checkPoint(Collision results, float[] point);

    public abstract void checkInput(Collision results, float[] near, float[] far);



    public static final class Point{

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

        public void calculate(FSSchematics schematics){
            coordinates[0] = modes[0].calculate(schematics, coefficients[0]);
            coordinates[1] = modes[1].calculate(schematics, coefficients[1]);
            coordinates[2] = modes[2].calculate(schematics, coefficients[2]);
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

    protected static final class XVolumentricMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelWidth() / 100f;
        }
    }

    protected static final class YVolumetricMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelHeight() / 100f;
        }
    }

    protected static final class ZVolumetricMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient * schematics.modelDepth() / 100f;
        }
    }

    protected static final class XOffsetMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelLeft() + coefficient;
        }
    }

    protected static final class YOffsetMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBottom() + coefficient;
        }
    }

    protected static final class ZOffsetMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelFront() + coefficient;
        }
    }

    protected static final class XOffsetVolumeMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelLeft() + coefficient * schematics.modelWidth() / 100f;
        }
    }

    protected static final class YOffsetVolumeMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelBottom() + coefficient * schematics.modelHeight() / 100f;
        }
    }

    protected static final class ZOffsetVolumeMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return schematics.modelFront() + coefficient * schematics.modelDepth() / 100f;
        }
    }

    protected static final class DirectMode implements Mode{

        @Override
        public float calculate(FSSchematics schematics, float coefficient){
            return coefficient;
        }
    }

    public static final class Collision{

        public float distance;
        public boolean collided;

        public Collision(){

        }
    }
}