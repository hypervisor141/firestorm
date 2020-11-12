package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLFloat;

public abstract class FSLight extends FSConfigSequence{

    protected long id;

    public FSLight(){
        id = FSControl.getNextID();
    }


    public void updateDirection(){
        float[] dir = direction().provider();
        float[] pos = position().provider();
        float[] cent = center().provider();

        dir[0] = cent[0] - pos[0];
        dir[1] = cent[1] - pos[1];
        dir[2] = cent[2] - pos[2];
    }

    public abstract String[] getStructMembers();

    public abstract String getLightFunction();

    public VLArrayFloat position(){
        return null;
    }

    public VLArrayFloat center(){
        return null;
    }

    public VLArrayFloat direction(){
        return null;
    }

    public FSAttenuation attenuation(){
        return null;
    }

    public VLFloat cutOff(){
        return null;
    }

    public VLFloat outerCutOff(){
        return null;
    }

    public long id(){
        return id;
    }
}
