package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLFloat;

public class FSLightSpot extends FSLight{

    protected VLArrayFloat position;
    protected VLArrayFloat center;
    protected VLArrayFloat direction;

    protected VLFloat cutoff;
    protected VLFloat outercutoff;

    public FSLightSpot(VLArrayFloat position, VLArrayFloat center, VLFloat cutoff, VLFloat outercutoff){
        this.position = position;
        this.center = center;
        this.cutoff = cutoff;
        this.outercutoff = outercutoff;

        this.direction = new VLArrayFloat(new float[]{ 0, 0, 0 });

        updateDirection();
    }

    public void updateDirection(){
        float[] dir = direction().provider();
        float[] pos = position().provider();
        float[] cent = center().provider();

        dir[0] = cent[0] - pos[0];
        dir[1] = cent[1] - pos[1];
        dir[2] = cent[2] - pos[2];
    }

    @Override
    public VLArrayFloat position(){
        return position;
    }

    public VLArrayFloat center(){
        return center;
    }

    public VLArrayFloat direction(){
        return direction;
    }

    public VLFloat cutOff(){
        return cutoff;
    }

    public VLFloat outerCutOff(){
        return outercutoff;
    }
}
