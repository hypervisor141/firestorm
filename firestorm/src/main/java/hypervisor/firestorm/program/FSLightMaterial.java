package hypervisor.firestorm.program;

import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.array.VLArrayUtils;
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

    public FSLightMaterial map(FSLightMaterial factor){
        divide(factor.ambient.provider(), factor.diffuse.provider(), factor.specular.provider(), factor.shininess.get());
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

    public FSLightMaterial map(float[] ambienttarget, float ambientfactor, float[] diffusetarget, float diffusefactor,
                               float[] speculartarget, float specularfactor, float shinetarget, float shinefactor){
        mapAmbient(ambienttarget, ambientfactor);
        mapDiffuse(diffusetarget, diffusefactor);
        mapSpecular(speculartarget, specularfactor);
        mapShininess(shinetarget, shinefactor);

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
        sum(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial sumDiffuse(float factor){
        sum(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial sumSpecular(float factor){
        sum(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial sumAmbient(float[] factor){
        sum(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial sumDiffuse(float[] factor){
        sum(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial sumSpecular(float[] factor){
        sum(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial sumShininess(float factor){
        shininess.set(shininess.get() + factor);
        return this;
    }

    public FSLightMaterial deductAmbient(float[] factor){
        deduct(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial deductDiffuse(float[] factor){
        deduct(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial deductSpecular(float[] factor){
        deduct(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplyAmbient(float factor){
        multiply(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplyDiffuse(float factor){
        multiply(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplySpecular(float factor){
        multiply(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplyShininess(float factor){
        shininess.set(shininess.get() * factor);
        return this;
    }

    public FSLightMaterial multiplyAmbient(float[] factor){
        divide(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplyDiffuse(float[] factor){
        multiply(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial multiplySpecular(float[] factor){
        multiply(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial divideAmbient(float[] factor){
        divide(ambient.provider(), factor);
        return this;
    }

    public FSLightMaterial divideDiffuse(float[] factor){
        divide(diffuse.provider(), factor);
        return this;
    }

    public FSLightMaterial divideSpecular(float[] factor){
        divide(specular.provider(), factor);
        return this;
    }

    public FSLightMaterial mapAmbient(float target, float factor){
        map(ambient.provider(), target, factor);
        return this;
    }

    public FSLightMaterial mapDiffuse(float target, float factor){
        map(diffuse.provider(), target, factor);
        return this;
    }

    public FSLightMaterial mapSpecular(float target, float factor){
        map(specular.provider(), target, factor);
        return this;
    }

    public FSLightMaterial mapShininess(float target, float factor){
        shininess.set(shininess.get() + (target - shininess.get()) * factor);
        return this;
    }

    public FSLightMaterial mapAmbient(float[] target, float factor){
        map(ambient.provider(), target, factor);
        return this;
    }

    public FSLightMaterial mapDiffuse(float[] target, float factor){
        map(diffuse.provider(), target, factor);
        return this;
    }

    public FSLightMaterial mapSpecular(float[] target, float factor){
        map(specular.provider(), target, factor);
        return this;
    }

    private void sum(float[] array, float[] target){
        array[0] += target[0];
        array[1] += target[1];
        array[2] += target[2];
    }

    private void sum(float[] array, float target){
        array[0] += target;
        array[1] += target;
        array[2] += target;
    }

    private void deduct(float[] array, float[] target){
        array[0] -= target[0];
        array[1] -= target[1];
        array[2] -= target[2];
    }

    private void multiply(float[] array, float[] target){
        array[0] *= target[0];
        array[1] *= target[1];
        array[2] *= target[2];
    }

    private void multiply(float[] array, float target){
        array[0] *= target;
        array[1] *= target;
        array[2] *= target;
    }

    private void divide(float[] array, float[] target){
        array[0] /= target[0];
        array[1] /= target[1];
        array[2] /= target[2];
    }

    private void map(float[] array, float[] target, float factor){
        float r = array[0];
        float g = array[1];
        float b = array[2];

        array[0] = r + (r - target[0]) * factor;
        array[1] = g + (g - target[1]) * factor;
        array[2] = b + (b - target[2]) * factor;
    }

    private void map(float[] array, float target, float factor){
        float r = array[0];
        float g = array[1];
        float b = array[2];

        array[0] = r + (r - target) * factor;
        array[1] = g + (g - target) * factor;
        array[2] = b + (b - target) * factor;
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
