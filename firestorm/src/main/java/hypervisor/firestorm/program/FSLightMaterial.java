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

    public void sum(FSLightMaterial mat, FSLightMaterial target){
        sumAmbient(mat.ambient.provider(), target.ambient.provider());
        sumDiffuse(mat.diffuse.provider(), target.diffuse.provider());
        sumSpecular(mat.specular.provider(), target.specular.provider());
        sumShininess(mat.shininess.get(), mat.shininess.get());
    }

    public void deduct(FSLightMaterial mat, FSLightMaterial target){
        deductAmbient(mat.ambient.provider(), target.ambient.provider());
        deductDiffuse(mat.diffuse.provider(), target.diffuse.provider());
        deductSpecular(mat.specular.provider(), target.specular.provider());
        sumShininess(mat.shininess.get(), -mat.shininess.get());
    }

    public void multiply(FSLightMaterial mat, FSLightMaterial target){
        multiplyAmbient(mat.ambient.provider(), target.ambient.provider());
        multiplyDiffuse(mat.diffuse.provider(), target.diffuse.provider());
        multiplySpecular(mat.specular.provider(), target.specular.provider());
        multiplyShininess(mat.shininess.get(), mat.shininess.get());
    }

    public void divide(FSLightMaterial mat, FSLightMaterial target){
        divideAmbient(mat.ambient.provider(), target.ambient.provider());
        divideDiffuse(mat.diffuse.provider(), target.diffuse.provider());
        divideSpecular(mat.specular.provider(), target.specular.provider());
        multiplyShininess(mat.shininess.get(), 1F / mat.shininess.get());
    }

    public void map(FSLightMaterial mat, FSLightMaterial target, float ambientfactor, float diffusefactor, float specularfactor, float shinefactor){
        mapAmbient(mat.ambient.provider(), target.ambient.provider(), ambientfactor);
        mapDiffuse(mat.diffuse.provider(), target.diffuse.provider(), diffusefactor);
        mapSpecular(mat.specular.provider(), target.specular.provider(), specularfactor);
        mapShininess(mat.shininess.get(), target.shininess.get(), shinefactor);
    }

    public void map(FSLightMaterial mat, FSLightMaterial target, float factor){
        mapAmbient(mat.ambient.provider(), target.ambient.provider(), factor);
        mapDiffuse(mat.diffuse.provider(), target.diffuse.provider(), factor);
        mapSpecular(mat.specular.provider(), target.specular.provider(), factor);
        mapShininess(mat.shininess.get(), target.shininess.get(), factor);
    }

    public void sumAmbient(float[] array, float target){
        sumScalar(ambient.provider(), array, target);
    }

    public void sumDiffuse(float[] array, float target){
        sumScalar(diffuse.provider(), array, target);
    }

    public void sumSpecular(float[] array, float target){
        sumScalar(specular.provider(), array, target);
    }

    public void sumAmbient(float[] array, float[] target){
        sumArray(ambient.provider(), array, target);
    }

    public void sumDiffuse(float[] array, float[] target){
        sumArray(diffuse.provider(), array, target);
    }

    public void sumSpecular(float[] array, float[] target){
        sumArray(specular.provider(), array, target);
    }

    public void sumShininess(float value, float target){
        shininess.set(value + target);
    }

    public void deductAmbient(float[] array, float[] target){
        deductArray(ambient.provider(), array, target);
    }

    public void deductDiffuse(float[] array, float[] target){
        deductArray(diffuse.provider(), array, target);
    }

    public void deductSpecular(float[] array, float[] target){
        deductArray(specular.provider(), array, target);
    }

    public void multiplyAmbient(float[] array, float target){
        multiplyScalar(ambient.provider(), array, target);
    }

    public void multiplyDiffuse(float[] array, float target){
        multiplyScalar(diffuse.provider(), array, target);
    }

    public void multiplySpecular(float[] array, float target){
        multiplyScalar(specular.provider(), array, target);
    }

    public void multiplyShininess(float value, float target){
        shininess.set(value * target);
    }

    public void multiplyAmbient(float[] array, float[] target){
        divideArray(ambient.provider(), array, target);
    }

    public void multiplyDiffuse(float[] array, float[] target){
        multiplyArray(diffuse.provider(), array, target);
    }

    public void multiplySpecular(float[] array, float[] target){
        multiplyArray(specular.provider(), array, target);
    }

    public void divideAmbient(float[] array, float[] target){
        divideArray(ambient.provider(), array, target);
    }

    public void divideDiffuse(float[] array, float[] target){
        divideArray(diffuse.provider(), array, target);
    }

    public void divideSpecular(float[] array, float[] target){
        divideArray(specular.provider(), array, target);
    }

    public void mapAmbient(float[] array, float target, float factor){
        mapScalar(ambient.provider(), array, target, factor);
    }

    public void mapDiffuse(float[] array, float target, float factor){
        mapScalar(diffuse.provider(), array, target, factor);
    }

    public void mapSpecular(float[] array, float target, float factor){
        mapScalar(specular.provider(), array, target, factor);
    }

    public void mapShininess(float value, float target, float factor){
        shininess.set(value + (target - shininess.get()) * factor);
    }

    public void mapAmbient(float[] array, float[] target, float factor){
        mapArray(ambient.provider(), array, target, factor);
    }

    public void mapDiffuse(float[] array, float[] target, float factor){
        mapArray(diffuse.provider(), array, target, factor);
    }

    public void mapSpecular(float[] array, float[] target, float factor){
        mapArray(specular.provider(), array, target, factor);
    }

    private void sumArray(float[] results, float[] array, float[] target){
        results[0] = array[0] +  target[0];
        results[1] = array[1] + target[1];
        results[2] = array[2] + target[2];
    }

    private void sumScalar(float[] results, float[] array, float target){
        results[0] = array[0] +  target;
        results[1] = array[1] + target;
        results[2] = array[2] + target;
    }

    private void deductArray(float[] results, float[] array, float[] target){
        results[0] = array[0] - target[0];
        results[1] = array[1] - target[1];
        results[2] = array[2] - target[2];
    }

    private void multiplyArray(float[] results, float[] array, float[] target){
        results[0] = array[0] * target[0];
        results[1] = array[1] * target[1];
        results[2] = array[2] * target[2];
    }

    private void multiplyScalar(float[] results, float[] array, float target){
        results[0] = array[0] * target;
        results[1] = array[1] * target;
        results[2] = array[2] * target;
    }

    private void divideArray(float[] results, float[] array, float[] target){
        results[0] = array[0] / target[0];
        results[1] = array[1] / target[1];
        results[2] = array[2] / target[2];
    }

    private void mapArray(float[] results, float[] array, float[] target, float factor){
        float r = array[0];
        float g = array[1];
        float b = array[2];

        results[0] = r + (target[0] - r) * factor;
        results[1] = g + (target[1] - g) * factor;
        results[2] = b + (target[2] - b) * factor;
    }

    private void mapScalar(float[] results, float[] array, float target, float factor){
        float r = array[0];
        float g = array[1];
        float b = array[2];

        results[0] = r + (target - r) * factor;
        results[1] = g + (target - g) * factor;
        results[2] = b + (target - b) * factor;
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
