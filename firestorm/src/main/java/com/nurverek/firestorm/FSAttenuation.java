package com.nurverek.firestorm;

import vanguard.VLFloat;

public abstract class FSAttenuation{

    public FSAttenuation(){

    }

    public static class Distance extends FSAttenuation{

        protected VLFloat constant;
        protected VLFloat linear;
        protected VLFloat quadratic;

        public Distance(VLFloat constant, VLFloat linear, VLFloat quadratic){
            this.constant = constant;
            this.linear = linear;
            this.quadratic = quadratic;
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
    }

    public static class Radius extends FSAttenuation{

        protected VLFloat radius;

        public Radius(VLFloat radius){
            this.radius = radius;
        }

        public VLFloat radius(){
            return radius;
        }
    }
}
