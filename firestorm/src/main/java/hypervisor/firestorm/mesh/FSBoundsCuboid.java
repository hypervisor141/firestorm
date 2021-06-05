package hypervisor.firestorm.mesh;

import hypervisor.firestorm.engine.FSCache;
import hypervisor.vanguard.math.VLMath;

public class FSBoundsCuboid extends FSBounds {

    protected float halfwidth;
    protected float halfheight;
    protected float halfdepth;
    protected float halfdiameter;

    public FSBoundsCuboid(FSSchematics schematics, float xoffset, float yoffset, float zoffset, Mode xmode, Mode ymode, Mode zmode,
                          float halfwidth, float halfheight, float halfdepth, Mode wmode, Mode hmode, Mode dmode){
        super(schematics);

        initialize(new Point(xmode, ymode, zmode, xoffset, yoffset, zoffset), 1);
        add(new Point(wmode, hmode, dmode, halfwidth, halfheight, halfdepth));
    }

    public FSBoundsCuboid(FSBoundsCuboid src, long flags){
        super(null);
        copy(src, flags);
    }

    protected FSBoundsCuboid(){

    }

    public float getHalfWidth(){
        return halfwidth;
    }

    public float getHalfHeight(){
        return halfheight;
    }

    public float getHalfDepth(){
        return halfdepth;
    }

    public float getHalfDiameter(){
        return halfdiameter;
    }

    @Override
    public void copy(FSBounds src, long flags){
        super.copy(src, flags);

        FSBoundsCuboid target = (FSBoundsCuboid)src;
        halfwidth = target.halfwidth;
        halfheight = target.halfheight;
        halfdepth = target.halfdepth;
        halfdiameter = target.halfdiameter;
    }

    @Override
    public FSBoundsCuboid duplicate(long flags){
        return new FSBoundsCuboid(this, flags);
    }

    @Override
    protected void notifyBasePointsUpdated(){
        float[] offsetcoords = offset().coordinates;
        float[] point1 = point(1).coordinates;

        halfwidth = (point1[0] - offsetcoords[0]);
        halfheight = (point1[1] - offsetcoords[1]);
        halfdepth = (point1[2] - offsetcoords[2]);

        FSCache.FLOAT4[0] = halfwidth;
        FSCache.FLOAT4[1] = halfheight;
        FSCache.FLOAT4[2] = halfheight;

        halfdiameter = VLMath.euclideanDistance(FSCache.FLOAT4, 0, offsetcoords, 0, 3);
    }

    @Override
    public void check(Collision results, FSBoundsSphere bounds){
        super.check(results, bounds);

        float[] coords = offset().coordinates;
        float[] targetcoords = bounds.offset().coordinates;

        VLMath.difference(coords, 0, targetcoords, 0, FSCache.FLOAT4, 0, 3);
        float origindistance = VLMath.length(FSCache.FLOAT4, 0, 3);

        FSCache.FLOAT4[0] = VLMath.clamp(FSCache.FLOAT4[0], -halfwidth, halfwidth);
        FSCache.FLOAT4[1] = VLMath.clamp(FSCache.FLOAT4[1], -halfheight, halfheight);
        FSCache.FLOAT4[2] = VLMath.clamp(FSCache.FLOAT4[2], -halfdepth, halfdepth);

        results.distance = origindistance - VLMath.length(FSCache.FLOAT4, 0, 3) - bounds.radius;
        results.collided = results.distance <= 0;
    }

    @Override
    public void check(Collision results, FSBoundsCuboid bounds){
        super.check(results, bounds);

        float[] coords = offset().coordinates;
        float[] targetcoords = bounds.offset().coordinates;

        FSCache.FLOAT4[0] = Math.abs(coords[0] - targetcoords[0]) - halfwidth - bounds.halfwidth;
        FSCache.FLOAT4[1] = Math.abs(coords[1] - targetcoords[1]) - halfheight - bounds.halfheight;
        FSCache.FLOAT4[2] = Math.abs(coords[2] - targetcoords[2]) - halfdepth - bounds.halfdepth;

        results.distance = VLMath.length(FSCache.FLOAT4, 0, 3);
        results.collided = FSCache.FLOAT4[0] <= 0 && FSCache.FLOAT4_2[1] <= 0 && FSCache.FLOAT4_2[2] <= 0;
    }

    @Override
    public void checkPoint(Collision results, float[] point){
        super.checkPoint(results, point);

        VLMath.difference(offset().coordinates, 0, point, 0, FSCache.FLOAT4, 0, 3);
        float length = VLMath.length(FSCache.FLOAT4, 0, 3);

        FSCache.FLOAT4[0] = VLMath.clamp(FSCache.FLOAT4[0], -halfwidth, halfwidth);
        FSCache.FLOAT4[1] = VLMath.clamp(FSCache.FLOAT4[1], -halfheight, halfheight);
        FSCache.FLOAT4[2] = VLMath.clamp(FSCache.FLOAT4[2], -halfdepth, halfdepth);

        results.distance = length - VLMath.length(FSCache.FLOAT4, 0, 3);
        results.collided = results.distance <= 0;
    }

    @Override
    public void checkInput(Collision results, float[] near, float[] far){
        super.checkInput(results, near, far);

        VLMath.closestPointOfRay(near, 0, far, 0, offset().coordinates, 0, FSCache.FLOAT4_2, 0);
        checkPoint(results, FSCache.FLOAT4_2);
    }
}