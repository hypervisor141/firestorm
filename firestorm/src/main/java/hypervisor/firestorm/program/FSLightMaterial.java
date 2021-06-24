package hypervisor.firestorm.program;

import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.primitive.VLFloat;
import hypervisor.vanguard.utils.VLCopyable;

public final class FSLightMaterial implements VLCopyable<FSLightMaterial>{

    protected VLArrayFloat ambient;
    protected VLArrayFloat specular;
    protected VLArrayFloat diffuse;
    protected VLFloat shininess;

    public FSLightMaterial(VLArrayFloat ambient, VLArrayFloat diffuse, VLArrayFloat specular, VLFloat shininess){
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    public FSLightMaterial(VLArrayFloat sharedcolor, VLFloat shininess){
        this.ambient = sharedcolor;
        this.diffuse = sharedcolor;
        this.specular = sharedcolor;
        this.shininess = shininess;
    }

    public FSLightMaterial(FSLightMaterial src, long flags){
        copy(src, flags);
    }

    protected FSLightMaterial(){

    }

    public VLArrayFloat ambient(){
        return ambient;
    }

    public VLArrayFloat diffuse(){
        return diffuse;
    }

    public VLArrayFloat specular(){
        return specular;
    }

    public VLFloat shininess(){
        return shininess;
    }

    public FSLightMaterial multiply(float ambientfactor, float diffusefactor, float specularfactor, float shinefactor){
        float[] ambientf = ambient.provider();
        ambientf[0] *= ambientfactor;
        ambientf[1] *= ambientfactor;
        ambientf[2] *= ambientfactor;

        float[] diffusef = diffuse.provider();
        diffusef[0] *= diffusefactor;
        diffusef[1] *= diffusefactor;
        diffusef[2] *= diffusefactor;

        float[] specularf = specular.provider();
        specularf[0] *= specularfactor;
        specularf[1] *= specularfactor;
        specularf[2] *= specularfactor;

        shininess.set(shininess.get() * shinefactor);

        return this;
    }

    public FSLightMaterial multiplyAmbient(float factor){
        float[] ambientf = ambient.provider();
        ambientf[0] *= factor;
        ambientf[1] *= factor;
        ambientf[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplyDiffuse(float factor){
        float[] diffusef = diffuse.provider();
        diffusef[0] *= factor;
        diffusef[1] *= factor;
        diffusef[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplySpecular(float factor){
        float[] specularf = specular.provider();
        specularf[0] *= factor;
        specularf[1] *= factor;
        specularf[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplyShininess(float factor){
        shininess.set(shininess.get() * factor);
        return this;
    }

    @Override
    public void copy(FSLightMaterial src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            ambient = src.ambient;
            diffuse = src.diffuse;
            specular = src.specular;
            shininess = src.shininess;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            ambient = src.ambient.duplicate(FLAG_DUPLICATE);;
            diffuse = src.diffuse.duplicate(FLAG_DUPLICATE);;
            specular = src.specular.duplicate(FLAG_DUPLICATE);;
            shininess = src.shininess.duplicate(FLAG_DUPLICATE);;

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public FSLightMaterial duplicate(long flags){
        return new FSLightMaterial(this, flags);
    }
}
