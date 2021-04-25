package com.nurverek.firestorm;

import vanguard.VLThread;
import vanguard.VLThreadTaskType;

public class FSRThread extends VLThread{

    public FSRThread(){
        super(50);
    }

    public static class TaskCreateContext implements VLThreadTaskType{

        private final boolean continuing;
        private final int[] attributes;

        protected TaskCreateContext(int[] attributes, boolean continuing){
            this.attributes = attributes;
            this.continuing = continuing;
        }

        @Override
        public void run(VLThread thread){
            FSCEGL.initialize(FSControl.getSurface().getHolder(), attributes, continuing);
        }

        @Override
        public void requestDestruction(){

        }
    }

    public static class TaskSignalSurfaceCreated implements VLThreadTaskType{

        private final boolean continuing;

        protected TaskSignalSurfaceCreated(boolean continuing){
            this.continuing = continuing;
        }

        @Override
        public void run(VLThread thread){
            FSR.surfaceCreated(continuing);
        }

        @Override
        public void requestDestruction(){

        }
    }

    public static class TaskSignalSurfaceChanged implements VLThreadTaskType{

        private final int format;
        private final int width;
        private final int height;

        protected TaskSignalSurfaceChanged(int format, int width, int height){
            this.format = format;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run(VLThread thread){
            FSR.surfaceChanged(format, width, height);
        }

        @Override
        public void requestDestruction(){

        }
    }

    public static class TaskSignalFrameDraw implements VLThreadTaskType{

        protected TaskSignalFrameDraw(){

        }

        @Override
        public void run(VLThread thread){
            FSR.drawFrame();
        }

        @Override
        public void requestDestruction(){

        }
    }
}
