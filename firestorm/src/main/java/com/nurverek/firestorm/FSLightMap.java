package com.nurverek.firestorm;

public final class FSLightMap{

    protected FSTexture diffuse;
    protected FSTexture specular;
    protected FSTexture normal;

    public FSLightMap(FSTexture diffuse, FSTexture specular, FSTexture normal){
        this.diffuse = diffuse;
        this.specular = specular;
        this.normal = normal;
    }

    public FSTexture diffuse(){
        return diffuse;
    }

    public FSTexture specular(){
        return specular;
    }

    public FSTexture normal(){
        return normal;
    }
}

