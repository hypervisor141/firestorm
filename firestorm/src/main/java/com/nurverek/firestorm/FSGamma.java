package com.nurverek.firestorm;

import com.nurverek.vanguard.VLFloat;

public class FSGamma extends FSP.Uniform1f{

    public static final String FUNCTION =
            "void correctGamma(inout vec4 fragColor, float gamma){\n" +
                    "\tfragColor.rgb = pow(fragColor.rgb, vec3(1.0 / gamma));\n" +
                    "}";

    public FSGamma(VLFloat gamma){
        super(gamma);
    }

    public String getGammaFunction(){
        return FUNCTION;
    }
}

