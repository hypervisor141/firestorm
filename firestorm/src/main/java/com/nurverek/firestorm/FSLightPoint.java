package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLCopyable;

public class FSLightPoint extends FSLight{

    protected FSAttenuation attenuation;
    protected VLArrayFloat position;

    public FSLightPoint(FSAttenuation attenuation, VLArrayFloat position){
        this.attenuation = attenuation;
        this.position = position;
    }

    public FSLightPoint(FSLightPoint src, long flags){
        copy(src, flags);
    }

    public FSAttenuation attenuation(){
        return attenuation;
    }

    @Override
    public VLArrayFloat position(){
        return position;
    }

    @Override
    public void copy(FSLight src, long flags){
        super.copy(src, flags);

        FSLightPoint target = (FSLightPoint)src;

        if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
            attenuation = target.attenuation;
            position = target.position;

        }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
            attenuation = target.attenuation.duplicate(FLAG_MAX_DEPTH);
            position = target.position.duplicate(FLAG_MAX_DEPTH);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }
    }

    @Override
    public FSLightPoint duplicate(long flags){
        return new FSLightPoint(this, flags);
    }
}
