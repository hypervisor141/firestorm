package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLFloat;

public final class FSLightMaterial{
    
    public static final String[] STRUCT_MEMBERS = new String[]{
            "vec3 ambient",
            "vec3 diffuse",
            "vec3 specular",
            "float shininess"
    };

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

    public FSLightMaterial(){
        ambient = new VLArrayFloat(3);
        specular = new VLArrayFloat(3);
        diffuse = new VLArrayFloat(3);
        shininess = new VLFloat(1);
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

    public String[] getStructMembers(){
        return STRUCT_MEMBERS;
    }
}
