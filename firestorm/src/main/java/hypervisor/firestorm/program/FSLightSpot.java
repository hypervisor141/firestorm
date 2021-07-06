package hypervisor.firestorm.program;

import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.primitive.VLFloat;

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

    public FSLightSpot(FSLightSpot src, long flags){
        copy(src, flags);
    }

    protected FSLightSpot(){

    }

    public void updateDirection(){
        float[] dir = direction().array;
        float[] pos = position().array;
        float[] cent = center().array;

        dir[0] = cent[0] - pos[0];
        dir[1] = cent[1] - pos[1];
        dir[2] = cent[2] - pos[2];
    }

    @Override
    public void copy(FSLight src, long flags){
        super.copy(src, flags);

        FSLightSpot target = (FSLightSpot)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            position = target.position;
            center = target.center;
            direction = target.direction;
            cutoff = target.cutoff;
            outercutoff = target.outercutoff;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            position = target.position.duplicate(FLAG_DUPLICATE);
            center = target.center.duplicate(FLAG_DUPLICATE);
            direction = target.direction.duplicate(FLAG_DUPLICATE);
            cutoff = target.cutoff.duplicate(FLAG_DUPLICATE);
            outercutoff = target.outercutoff.duplicate(FLAG_DUPLICATE);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }
    }

    @Override
    public FSLightSpot duplicate(long flags){
        return new FSLightSpot(this, flags);
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
