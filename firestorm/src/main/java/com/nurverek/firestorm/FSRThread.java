package com.nurverek.firestorm;

import android.content.Context;
import android.view.SurfaceHolder;

import vanguard.VLThread;
import vanguard.VLThreadTaskType;

public class FSRThread extends VLThread{

    public FSRThread(){
        super(50);
    }

    public static class TaskCreateContext implements VLThreadTaskType{

        private SurfaceHolder holder;
        private final int[] attributes;
        private final boolean continuing;

        protected TaskCreateContext(SurfaceHolder holder, int[] attributes, boolean continuing){
            this.holder = holder;
            this.attributes = attributes;
            this.continuing = continuing;
        }

        @Override
        public void run(VLThread thread){
            FSCEGL.initialize(holder, attributes, continuing);
            holder = null;
        }
    }

    public static class TaskSignalSurfaceCreated implements VLThreadTaskType{

        private FSSurface surface;
        private Context context;

        private final boolean continuing;

        protected TaskSignalSurfaceCreated(FSSurface surface, Context context, boolean continuing){
            this.surface = surface;
            this.context = context;
            this.continuing = continuing;
        }

        @Override
        public void run(VLThread thread){
            FSR.surfaceCreated(surface, context, continuing);
        }
    }

    public static class TaskSignalSurfaceChanged implements VLThreadTaskType{

        private FSSurface surface;
        private Context context;

        private final int format;
        private final int width;
        private final int height;

        protected TaskSignalSurfaceChanged(FSSurface surface, Context context, int format, int width, int height){
            this.surface = surface;
            this.context = context;
            this.format = format;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run(VLThread thread){
            FSR.surfaceChanged(surface, context, format, width, height);
        }
    }

    public static class TaskSignalFrameDraw implements VLThreadTaskType{

        protected TaskSignalFrameDraw(){

        }

        @Override
        public void run(VLThread thread){
            FSR.drawFrame();
        }
    }
}
