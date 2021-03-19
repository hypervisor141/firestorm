package com.nurverek.firestorm;

import vanguard.VLArrayFloat;

public class FSLightPoint extends FSLight{

    protected FSAttenuation attenuation;
    protected VLArrayFloat position;

    public FSLightPoint(FSAttenuation attenuation, VLArrayFloat position){
        this.attenuation = attenuation;
        this.position = position;
    }

    public FSAttenuation attenuation(){
        return attenuation;
    }

    @Override
    public VLArrayFloat position(){
        return position;
    }
}
