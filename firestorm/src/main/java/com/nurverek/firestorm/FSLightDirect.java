package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLListType;

public class FSLightDirect extends FSLight{

    public static final String[] STRUCT_MEMBERS = new String[]{
            "vec3 direction"
    };

    public static final String FUNCTION =
            "vec3 directLight(DirectLight light, Material material, vec3 normal, vec3 cameraPos, vec3 lightdir, float shadow){\n" +
                    "\treturn material.ambient +\n" +
                    "\t       shadow * (material.diffuse * max(dot(normal, lightdir), 0.0) +\n" +
                    "\t       (material.specular * pow(max(dot(normal, normalize(lightdir + cameraPos)), 0.0), material.shininess)));\n" +
                    "}";

    protected VLArrayFloat position;
    protected VLArrayFloat center;
    protected VLArrayFloat direction;

    public FSLightDirect(VLArrayFloat position, VLArrayFloat center){
        this.position = position;
        this.center = center;

        direction = new VLArrayFloat(new float[]{ 0, 0, 0 });

        updateDirection();

        update(new VLListType<>(new FSConfig[]{
                new FSP.Uniform3fvd(position, 0, 1),
        }, 0));
    }

    public void updateDirection(){
        float[] dir = direction().provider();
        float[] pos = position().provider();
        float[] cent = center().provider();

        dir[0] = cent[0] - pos[0];
        dir[1] = cent[1] - pos[1];
        dir[2] = cent[2] - pos[2];
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

    @Override
    public String[] getStructMembers(){
        return STRUCT_MEMBERS;
    }

    @Override
    public String getLightFunction(){
        return FUNCTION;
    }
}

