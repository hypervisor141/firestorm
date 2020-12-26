package com.nurverek.firestorm;

import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLListType;

public abstract class FSAttenuation extends FSConfigSequence{

    public FSAttenuation(){

    }

    public abstract String[] getStructMembers();

    public abstract String getFunction();

    public static final class Distance extends FSAttenuation{

        public static final String[] STRUCT_MEMBERS = new String[]{
                "float constant",
                "float linear",
                "float quadratic"
        };

        public static final String FUNCTION =
                "float attenuation(Attenuation attenuation, vec3 vertexpos){\n" +
                        "\tfloat distance = length(light.position - vertexpos);\n" +
                        "\treturn 1.0 / (attenuation.constant + attenuation.linear * distance + attenuation.quadratic * (distance * distance));\n" +
                        "}";

        protected VLFloat constant;
        protected VLFloat linear;
        protected VLFloat quadratic;

        public Distance(VLFloat constant, VLFloat linear, VLFloat quadratic){
            this.constant = constant;
            this.linear = linear;
            this.quadratic = quadratic;

            update(new VLListType<>(new FSConfig[]{ new FSP.Uniform1f(constant), new FSP.Uniform1f(linear), new FSP.Uniform1f(quadratic) }, 0));
        }

        public VLFloat constant(){
            return constant;
        }

        public VLFloat linear(){
            return linear;
        }

        public VLFloat quadratic(){
            return quadratic;
        }

        @Override
        public String[] getStructMembers(){
            return STRUCT_MEMBERS;
        }

        @Override
        public String getFunction(){
            return FUNCTION;
        }
    }

    public static final class Radius extends FSAttenuation{

        public static final String[] STRUCT_MEMBERS = new String[]{
                "float radius"
        };

        public static final String FUNCTION =
                "float attenuation(Attenuation attenuation, vec3 vertexpos){\n" +
                        "\treturn smoothstep(attenuation.radius, 0.0, length(light.position - vertexpos));\n" +
                        "}";

        protected VLFloat radius;

        public Radius(VLFloat radius){
            this.radius = radius;
            update(new VLListType<>(new FSConfig[]{ new FSP.Uniform1f(radius) }, 0));
        }

        public VLFloat radius(){
            return radius;
        }

        @Override
        public String[] getStructMembers(){
            return STRUCT_MEMBERS;
        }

        @Override
        public String getFunction(){
            return FUNCTION;
        }
    }
}
