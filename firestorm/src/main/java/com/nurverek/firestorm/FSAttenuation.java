package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLFloat;

public abstract class FSAttenuation implements VLCopyable<FSAttenuation>{

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

        public Distance(Distance src, long flags){
            copy(src, flags);
        }

        @Override
        public void copy(FSAttenuation src, long flags){
            Distance target = (Distance)src;

            if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
                constant = target.constant;
                linear = target.linear;
                quadratic = target.quadratic;

            }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
                constant = target.constant.duplicate(FLAG_MAX_DEPTH);
                linear = target.linear.duplicate(FLAG_MAX_DEPTH);
                quadratic = target.quadratic.duplicate(FLAG_MAX_DEPTH);

            }else{
                throw new RuntimeException("Invalid flags : " + flags);
            }
        }

        @Override
        public Distance duplicate(long flags){
            return new Distance(this, flags);
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

        public Radius(Radius src, long flags){
            copy(src, flags);
        }

        @Override
        public void copy(FSAttenuation src, long flags){
            Radius target = (Radius)src;

            if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
                radius = target.radius;

            }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
                radius = target.radius.duplicate(FLAG_MAX_DEPTH);

            }else{
                throw new RuntimeException("Invalid flags : " + flags);
            }
        }

        @Override
        public Radius duplicate(long flags){
            return new Radius(this, flags);
        }

        public VLFloat radius(){
            return radius;
        }
    }
}
