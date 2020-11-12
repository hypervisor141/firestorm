package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public final class FSLightMap extends FSConfigSequence {

    public static final String[] STRUCT_MEMBERS = new String[]{
            "sampler2D diffuse",
            "sampler2D specular",
            "sampler2D normal"
    };

    protected FSTexture diffuse;
    protected FSTexture specular;
    protected FSTexture normal;

    public FSLightMap(FSTexture diffuse, FSTexture specular, FSTexture normal){
        this.diffuse = diffuse;
        this.specular = specular;
        this.normal = normal;
        
        update(new VLListType<>(new FSConfig[]{
                new FSP.Uniform1i(diffuse.unit()),
                new FSP.Uniform1i(specular.unit()),
                new FSP.Uniform1i(normal.unit()),
        }, 0));
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

    public String[] getStructMembers(){
        return STRUCT_MEMBERS;
    }
}

