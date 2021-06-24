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

    public FSLightMaterial sum(FSLightMaterial factor){
        sum(factor.ambient.provider(), factor.diffuse.provider(), factor.specular.provider(), factor.shininess.get());
        return this;
    }

    public FSLightMaterial deduct(FSLightMaterial factor){
        deduct(factor.ambient.provider(), factor.diffuse.provider(), factor.specular.provider(), factor.shininess.get());
        return this;
    }

    public FSLightMaterial multiply(FSLightMaterial factor){
        multiply(factor.ambient.provider(), factor.diffuse.provider(), factor.specular.provider(), factor.shininess.get());
        return this;
    }

    public FSLightMaterial divide(FSLightMaterial factor){
        divide(factor.ambient.provider(), factor.diffuse.provider(), factor.specular.provider(), factor.shininess.get());
        return this;
    }

    public FSLightMaterial sum(float ambientfactor, float diffusefactor, float specularfactor, float shinefactor){
        multiplyAmbient(ambientfactor);
        multiplyDiffuse(diffusefactor);
        multiplySpecular(specularfactor);
        multiplyShininess(shinefactor);

        return this;
    }

    public FSLightMaterial multiply(float ambientfactor, float diffusefactor, float specularfactor, float shinefactor){
        multiplyAmbient(ambientfactor);
        multiplyDiffuse(diffusefactor);
        multiplySpecular(specularfactor);
        multiplyShininess(shinefactor);

        return this;
    }

    public FSLightMaterial sumAmbient(float factor){
        float[] array = ambient.provider();
        array[0] += factor;
        array[1] += factor;
        array[2] += factor;

        return this;
    }

    public FSLightMaterial sumDiffuse(float factor){
        float[] array = diffuse.provider();
        array[0] += factor;
        array[1] += factor;
        array[2] += factor;

        return this;
    }

    public FSLightMaterial sumSpecular(float factor){
        float[] array = specular.provider();
        array[0] += factor;
        array[1] += factor;
        array[2] += factor;

        return this;
    }

    public FSLightMaterial sumShininess(float factor){
        shininess.set(shininess.get() + factor);

        return this;
    }

    public FSLightMaterial multiplyAmbient(float factor){
        float[] array = ambient.provider();
        array[0] *= factor;
        array[1] *= factor;
        array[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplyDiffuse(float factor){
        float[] array = diffuse.provider();
        array[0] *= factor;
        array[1] *= factor;
        array[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplySpecular(float factor){
        float[] array = specular.provider();
        array[0] *= factor;
        array[1] *= factor;
        array[2] *= factor;

        return this;
    }

    public FSLightMaterial multiplyShininess(float factor){
        shininess.set(shininess.get() * factor);
        return this;
    }

    public FSLightMaterial sum(float[] ambientfactor, float[] diffusefactor, float[] specularfactor, float shinefactor){
        sumAmbient(ambientfactor);
        sumDiffuse(diffusefactor);
        sumSpecular(specularfactor);
        sumShininess(shinefactor);

        return this;
    }

    public FSLightMaterial deduct(float[] ambientfactor, float[] diffusefactor, float[] specularfactor, float shinefactor){
        deductAmbient(ambientfactor);
        deductDiffuse(diffusefactor);
        deductSpecular(specularfactor);
        sumShininess(-shinefactor);

        return this;
    }

    public FSLightMaterial multiply(float[] ambientfactor, float[] diffusefactor, float[] specularfactor, float shinefactor){
        multiplyAmbient(ambientfactor);
        multiplyDiffuse(diffusefactor);
        multiplySpecular(specularfactor);
        multiplyShininess(shinefactor);

        return this;
    }

    public FSLightMaterial divide(float[] ambientfactor, float[] diffusefactor, float[] specularfactor, float shinefactor){
        divideAmbient(ambientfactor);
        divideDiffuse(diffusefactor);
        divideSpecular(specularfactor);
        multiplyShininess(1F / shinefactor);

        return this;
    }

    public FSLightMaterial sumAmbient(float[] factor){
        float[] array = ambient.provider();
        array[0] += factor[0];
        array[1] += factor[1];
        array[2] += factor[2];

        return this;
    }

    public FSLightMaterial sumDiffuse(float[] factor){
        float[] array = diffuse.provider();
        array[0] += factor[0];
        array[1] += factor[1];
        array[2] += factor[2];

        return this;
    }

    public FSLightMaterial sumSpecular(float[] factor){
        float[] array = specular.provider();
        array[0] += factor[0];
        array[1] += factor[1];
        array[2] += factor[2];

        return this;
    }

    public FSLightMaterial deductAmbient(float[] factor){
        float[] array = ambient.provider();
        array[0] -= factor[0];
        array[1] -= factor[1];
        array[2] -= factor[2];

        return this;
    }

    public FSLightMaterial deductDiffuse(float[] factor){
        float[] array = diffuse.provider();
        array[0] -= factor[0];
        array[1] -= factor[1];
        array[2] -= factor[2];

        return this;
    }

    public FSLightMaterial deductSpecular(float[] factor){
        float[] array = specular.provider();
        array[0] -= factor[0];
        array[1] -= factor[1];
        array[2] -= factor[2];

        return this;
    }

    public FSLightMaterial multiplyAmbient(float[] factor){
        float[] array = ambient.provider();
        array[0] *= factor[0];
        array[1] *= factor[1];
        array[2] *= factor[2];

        return this;
    }

    public FSLightMaterial multiplyDiffuse(float[] factor){
        float[] array = diffuse.provider();
        array[0] *= factor[0];
        array[1] *= factor[1];
        array[2] *= factor[2];

        return this;
    }

    public FSLightMaterial multiplySpecular(float[] factor){
        float[] array = specular.provider();
        array[0] *= factor[0];
        array[1] *= factor[1];
        array[2] *= factor[2];
        
        return this;
    }

    public FSLightMaterial divideAmbient(float[] factor){
        float[] array = ambient.provider();
        array[0] /= factor[0];
        array[1] /= factor[1];
        array[2] /= factor[2];

        return this;
    }

    public FSLightMaterial divideDiffuse(float[] factor){
        float[] array = diffuse.provider();
        array[0] /= factor[0];
        array[1] /= factor[1];
        array[2] /= factor[2];

        return this;
    }

    public FSLightMaterial divideSpecular(float[] factor){
        float[] array = specular.provider();
        array[0] /= factor[0];
        array[1] /= factor[1];
        array[2] /= factor[2];

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
