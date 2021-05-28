package hypervisor.firestorm.program;

import hypervisor.vanguard.array.VLArrayFloat;

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

    protected FSLightPoint(){

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

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            attenuation = target.attenuation;
            position = target.position;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            attenuation = target.attenuation.duplicate(FLAG_DUPLICATE);
            position = target.position.duplicate(FLAG_DUPLICATE);

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public FSLightPoint duplicate(long flags){
        return new FSLightPoint(this, flags);
    }
}
