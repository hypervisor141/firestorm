package hypervisor.firestorm.engine;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.view.GestureDetectorCompat;

public class FSSurface extends SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener{

    protected GestureDetectorCompat gesture;
    protected Config config;
    protected FSEvents events;
    protected int[] eglconfig;

    public FSSurface(Context context, int[] eglconfig, FSEvents events){
        super(context);

        this.eglconfig = eglconfig;
        this.events = events;

        initializeFields();
    }

    public FSSurface(Context context, FSEvents events){
        super(context);

        this.eglconfig = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL14.EGL_LEVEL, 0,
                EGL14.EGL_SAMPLES, 1,
                EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE
        };
        this.events = events;

        initializeFields();
    }

    protected FSSurface(Context context){
        super(context);
    }

    private void initializeFields(){
        gesture = new GestureDetectorCompat(getContext(), this);
        config = new Config();

        getHolder().addCallback(this);
    }

    public Config config(){
        return config;
    }

    protected FSEvents events(){
        return events;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        config.setTouchable(true);

        boolean isalive = FSControl.isAlive();
        Context context = getContext();

        events.GLPreSurfaceCreate(this, context, isalive);

        FSR.requestStart();
        FSR.postRootTask(new FSRThread.TaskCreateContext(getHolder(), eglconfig, isalive));
        FSR.postRootTask(new FSRThread.TaskSignalSurfaceCreated(this, context, isalive));

        events.GLPostSurfaceCreate(this, context, isalive);

        FSControl.setAlive(true);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        Context context = getContext();

        events.GLPreSurfaceChange(this, context, format, width, height);

        FSR.postRootTask(new FSRThread.TaskSignalSurfaceChanged(this, context, format, width, height));

        events.GLPostSurfaceChange(this, context, format, width, height);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        Context context = getContext();

        events.GLPreSurfaceDestroy(this, context);

        destroy();

        if(events != null){
            events.GLPostSurfaceDestroy(this, context);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if(config.getTouchable()){
            return !FSCInput.TOUCH.trigger(e, null, -1F,-1F) || !gesture.onTouchEvent(e);
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent e){
        if(config.getTouchable()){
            return FSCInput.DOWN.trigger(e, null, -1F,-1F);
        }

        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e){
        if(config.getTouchable()){
            return FSCInput.SINGLETAP.trigger(e, null, -1F,-1F);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e){
        if(config.getTouchable()){
            FSCInput.LONGPRESS.trigger(e, null, -1F,-1F);
        }
    }

    @Override
    public void onShowPress(MotionEvent e){
        if(config.getTouchable()){
            FSCInput.SHOWPRESS.trigger(e, null, -1F, -1F);
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, final float distanceY){
        if(config.getTouchable()){
            return FSCInput.SCROLL.trigger(e1, e2, distanceX, distanceY);
        }

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY){
        if(config.getTouchable()){
            return FSCInput.FLING.trigger(e1, e2, velocityX, velocityY);
        }

        return false;
    }

    private void destroy(){
        FSControl.destroy();

        if(FSControl.getDestroyOnPause()){
            getHolder().removeCallback(this);

            gesture = null;
            config = null;
            events = null;
        }
    }

    public static class Config{

        protected boolean touchable;

        public Config(){
            touchable = true;
        }

        public void setTouchable(boolean s){
            touchable = s;
        }

        public boolean getTouchable(){
            return touchable;
        }
    }
}
