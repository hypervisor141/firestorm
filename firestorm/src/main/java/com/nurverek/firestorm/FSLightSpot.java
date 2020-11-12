package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLListType;

public class FSLightSpot extends FSLight {

    public static final String[] STRUCT_MEMBERS = new String[]{
            "vec3 position",
            "vec3 direction",
            "float cutoff",
            "float outercutoff"
    };

    public static final String FUNCTION =
            "vec3 spotLight(SpotLight light, Material material, vec3 normal, vec3 vertexpos, vec3 cameraPos, float shadow){\n" +
                    "\tvec3 lightdir = normalize(light.position - vertexpos);\n" +
                    "\n" +
                    "\tfloat distance = length(light.position - vertexpos);\n" +
                    "\tfloat attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));\n" +
                    "\tfloat intensity = clamp((dot(lightdir, normalize(-light.direction)) - light.outercutoff) / (light.cutoff - light.outercutoff)), 0.0, 1.0);\n" +
                    "\tfloat multiplier = attenuation * intensity;\n" +
                    "\n" +
                    "\treturn (material.ambient +\n" +
                    "\t       shadow * (material.diffuse * max(dot(normal, lightdir), 0.0)) +\n" +
                    "\t       material.specular * pow(max(dot(normal, normalize(lightdir + cameraPos)), 0.0), material.shininess)) * multiplier;\n" +
                    "}";

    protected VLArrayFloat position;
    protected VLArrayFloat center;
    protected VLArrayFloat direction;

    protected VLFloat cutoff;
    protected VLFloat outercutoff;

    public FSLightSpot(VLArrayFloat position, VLArrayFloat center, VLFloat cutoff, VLFloat outercutoff){
        this.position = position;
        this.center = center;
        this.cutoff = cutoff;
        this.outercutoff = outercutoff;

        this.direction = new VLArrayFloat(new float[]{ 0, 0, 0 });

        updateDirection();

        update(new VLListType<>(new FSConfig[]{
                new FSP.Uniform3fvd(position, 0, 1),
                new FSP.Uniform3fvd(direction, 0, 1),
                new FSP.Uniform1f(cutoff),
                new FSP.Uniform1f(outercutoff)
        }, 0));
    }

    @Override
    public VLArrayFloat position(){
        return position;
    }

    @Override
    public VLArrayFloat center(){
        return center;
    }

    @Override
    public VLArrayFloat direction(){
        return direction;
    }

    @Override
    public VLFloat cutOff(){
        return cutoff;
    }

    @Override
    public VLFloat outerCutOff(){
        return outercutoff;
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
