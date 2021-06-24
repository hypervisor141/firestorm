package hypervisor.firestorm.engine;

import android.content.Context;
import android.view.SurfaceHolder;

import hypervisor.vanguard.concurrency.VLThread;
import hypervisor.vanguard.concurrency.VLThreadTaskType;

public class FSRThread extends VLThread{

    public FSRThread(){
        super(10);
        setPriority(MAX_PRIORITY);
    }

    public static class TaskCreateContext implements VLThreadTaskType{

        protected SurfaceHolder holder;
        protected int[] attributes;
        protected boolean continuing;

        protected TaskCreateContext(SurfaceHolder holder, int[] attributes, boolean continuing){
            this.holder = holder;
            this.attributes = attributes;
            this.continuing = continuing;
        }

        protected TaskCreateContext(){

        }

        @Override
        public void run(VLThread thread){
            FSCEGL.initialize(holder, attributes, continuing);
            holder = null;
        }
    }

    public static class TaskSignalSurfaceCreated implements VLThreadTaskType{

        protected FSSurface surface;
        protected Context context;

        private boolean continuing;

        protected TaskSignalSurfaceCreated(FSSurface surface, Context context, boolean continuing){
            this.surface = surface;
            this.context = context;
            this.continuing = continuing;
        }

        protected TaskSignalSurfaceCreated(){

        }

        @Override
        public void run(VLThread thread){
            FSR.surfaceCreated(surface, context, continuing);
        }
    }

    public static class TaskSignalSurfaceChanged implements VLThreadTaskType{

        protected FSSurface surface;
        protected Context context;

        protected int format;
        protected int width;
        protected int height;

        protected TaskSignalSurfaceChanged(FSSurface surface, Context context, int format, int width, int height){
            this.surface = surface;
            this.context = context;
            this.format = format;
            this.width = width;
            this.height = height;
        }

        protected TaskSignalSurfaceChanged(){

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
