package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLCopyable;
import vanguard.VLFloat;

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
