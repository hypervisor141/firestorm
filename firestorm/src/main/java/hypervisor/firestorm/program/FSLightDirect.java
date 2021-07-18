package hypervisor.firestorm.program;

import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.math.VLMath;

public class FSLightDirect extends FSLight{

    protected VLArrayFloat position;
    protected VLArrayFloat center;
    protected VLArrayFloat direction;

    public FSLightDirect(VLArrayFloat position, VLArrayFloat center){
        this.position = position;
        this.center = center;

        direction = new VLArrayFloat(new float[]{ 0, 0, 0 });

        updateDirection();
    }

    public FSLightDirect(FSLightDirect src, long flags){
        copy(src, flags);
    }

    protected FSLightDirect(){

    }

    public void updateDirection(){
        float[] dir = direction().array;
        float[] pos = position().array;
        float[] cent = center().array;

        dir[0] = pos[0] - cent[0];
        dir[1] = pos[1] - cent[1];
        dir[2] = pos[2] - cent[2];

        VLMath.normalize(dir, 0, 3);
    }

    @Override
    public void copy(FSLight src, long flags){
        super.copy(src, flags);

        FSLightDirect target = (FSLightDirect)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            position = target.position;
            center = target.center;
            direction = target.direction;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            position = target.position.duplicate(FLAG_DUPLICATE);
            center = target.center.duplicate(FLAG_DUPLICATE);
            direction = target.direction.duplicate(FLAG_DUPLICATE);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }
    }

    @Override
    public FSLightDirect duplicate(long flags){
        return new FSLightDirect(this, flags);
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
}

