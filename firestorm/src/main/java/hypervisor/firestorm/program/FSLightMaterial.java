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
        sumAmbient(mat.ambient.array, target.ambient.array);
        sumDiffuse(mat.diffuse.array, target.diffuse.array);
        sumSpecular(mat.specular.array, target.specular.array);
        sumShininess(mat.shininess.value, mat.shininess.value);
    }

    public void deduct(FSLightMaterial mat, FSLightMaterial target){
        deductAmbient(mat.ambient.array, target.ambient.array);
        deductDiffuse(mat.diffuse.array, target.diffuse.array);
        deductSpecular(mat.specular.array, target.specular.array);
        sumShininess(mat.shininess.value, -mat.shininess.value);
    }

    public void multiply(FSLightMaterial mat, FSLightMaterial target){
        multiplyAmbient(mat.ambient.array, target.ambient.array);
        multiplyDiffuse(mat.diffuse.array, target.diffuse.array);
        multiplySpecular(mat.specular.array, target.specular.array);
        multiplyShininess(mat.shininess.value, mat.shininess.value);
    }

    public void divide(FSLightMaterial mat, FSLightMaterial target){
        divideAmbient(mat.ambient.array, target.ambient.array);
        divideDiffuse(mat.diffuse.array, target.diffuse.array);
        divideSpecular(mat.specular.array, target.specular.array);
        multiplyShininess(mat.shininess.value, 1F / mat.shininess.value);
    }

    public void map(FSLightMaterial mat, FSLightMaterial target, float ambientfactor, float diffusefactor, float specularfactor, float shinefactor){
        mapAmbient(mat.ambient.array, target.ambient.array, ambientfactor);
        mapDiffuse(mat.diffuse.array, target.diffuse.array, diffusefactor);
        mapSpecular(mat.specular.array, target.specular.array, specularfactor);
        mapShininess(mat.shininess.value, target.shininess.value, shinefactor);
    }

    public void map(FSLightMaterial mat, FSLightMaterial target, float factor){
        mapAmbient(mat.ambient.array, target.ambient.array, factor);
        mapDiffuse(mat.diffuse.array, target.diffuse.array, factor);
        mapSpecular(mat.specular.array, target.specular.array, factor);
        mapShininess(mat.shininess.value, target.shininess.value, factor);
    }

    public void sumAmbient(float[] array, float target){
        sumScalar(ambient.array, array, target);
    }

    public void sumDiffuse(float[] array, float target){
        sumScalar(diffuse.array, array, target);
    }

    public void sumSpecular(float[] array, float target){
        sumScalar(specular.array, array, target);
    }

    public void sumAmbient(float[] array, float[] target){
        sumArray(ambient.array, array, target);
    }

    public void sumDiffuse(float[] array, float[] target){
        sumArray(diffuse.array, array, target);
    }

    public void sumSpecular(float[] array, float[] target){
        sumArray(specular.array, array, target);
    }

    public void sumShininess(float value, float target){
        shininess.value = value + target;
    }

    public void deductAmbient(float[] array, float[] target){
        deductArray(ambient.array, array, target);
    }

    public void deductDiffuse(float[] array, float[] target){
        deductArray(diffuse.array, array, target);
    }

    public void deductSpecular(float[] array, float[] target){
        deductArray(specular.array, array, target);
    }

    public void multiplyAmbient(float[] array, float target){
        multiplyScalar(ambient.array, array, target);
    }

    public void multiplyDiffuse(float[] array, float target){
        multiplyScalar(diffuse.array, array, target);
    }

    public void multiplySpecular(float[] array, float target){
        multiplyScalar(specular.array, array, target);
    }

    public void multiplyShininess(float value, float target){
        shininess.value = value * target;
    }

    public void multiplyAmbient(float[] array, float[] target){
        divideArray(ambient.array, array, target);
    }

    public void multiplyDiffuse(float[] array, float[] target){
        multiplyArray(diffuse.array, array, target);
    }

    public void multiplySpecular(float[] array, float[] target){
        multiplyArray(specular.array, array, target);
    }

    public void divideAmbient(float[] array, float[] target){
        divideArray(ambient.array, array, target);
    }

    public void divideDiffuse(float[] array, float[] target){
        divideArray(diffuse.array, array, target);
    }

    public void divideSpecular(float[] array, float[] target){
        divideArray(specular.array, array, target);
    }

    public void mapAmbient(float[] array, float target, float factor){
        mapScalar(ambient.array, array, target, factor);
    }

    public void mapDiffuse(float[] array, float target, float factor){
        mapScalar(diffuse.array, array, target, factor);
    }

    public void mapSpecular(float[] array, float target, float factor){
        mapScalar(specular.array, array, target, factor);
    }

    public void mapShininess(float value, float target, float factor){
        shininess.value = value + (target - shininess.value) * factor;
    }

    public void mapAmbient(float[] array, float[] target, float factor){
        mapArray(ambient.array, array, target, factor);
    }

    public void mapDiffuse(float[] array, float[] target, float factor){
        mapArray(diffuse.array, array, target, factor);
    }

    public void mapSpecular(float[] array, float[] target, float factor){
        mapArray(specular.array, array, target, factor);
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
