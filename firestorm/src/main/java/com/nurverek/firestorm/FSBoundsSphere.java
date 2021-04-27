package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLMath;

public class FSBoundsSphere extends FSBounds {

    protected float radius;

    public FSBoundsSphere(FSSchematics schematics, float xoffset, float yoffset, float zoffset, Mode xmode, Mode ymode, Mode zmode, float radius, Mode radiusmode){
        super(schematics);

        VLListType<Point> points = new VLListType<>(2, 1);
        points.add(new Point(radiusmode, MODE_DIRECT_VALUE, MODE_DIRECT_VALUE, radius, 0, 0));

        initialize(new Point(xmode, ymode, zmode, xoffset, yoffset, 0), points);
    }

    @Override
    protected void updateData(){
        radius = VLMath.euclideanDistance(point(0).coordinates, 0, offset.coordinates, 0, 3);
    }

    @Override
    public void check(Collision results, FSBoundsSphere bounds){
        super.check(results, bounds);

        results.distance = VLMath.euclideanDistance(point(0).coordinates, 0, offset.coordinates, 0, 3) - radius - bounds.radius;
        results.collided = results.distance <= 0;
    }

    @Override
    public void check(Collision results, FSBoundsCuboid bounds){
        super.check(results, bounds);

        bounds.check(results, this);
    }

    @Override
    public void checkPoint(Collision results, float[] point){
        super.checkPoint(results, point);

        results.distance = VLMath.euclideanDistance(point, 0, offset.coordinates, 0, 3) - radius;
        results.collided = results.distance <= 0;
    }

    @Override
    public void checkInput(Collision results, float[] near, float[] far){
        super.checkInput(results, near, far);

        VLMath.closestPointOfRay(near, 0, far, 0, offset.coordinates, 0, CACHE2, 0);
        checkPoint(results, CACHE2);
    }
}
