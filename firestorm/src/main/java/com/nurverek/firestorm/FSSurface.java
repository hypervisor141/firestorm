package com.nurverek.firestorm;

import android.app.Activity;
import android.view.Choreographer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.view.GestureDetectorCompat;


public final class FSSurface extends SurfaceView implements SurfaceHolder.Callback,
        Choreographer.FrameCallback, GestureDetector.OnGestureListener{

    private GestureDetectorCompat gesture;
    private Choreographer choreographer;
    private Config config;
    private FSEvents events;

    private boolean destroyed;

    protected FSSurface(Activity act, FSEvents events){
        super(act.getApplicationContext());

        this.events = events;
        destroyed = false;

        gesture = new GestureDetectorCompat(act, this);
        choreographer = Choreographer.getInstance();
        config = new Config();

        getHolder().addCallback(this);
    }

    protected void postFrame(){
        choreographer.postFrameCallback(this);
    }

    public Config config(){
        return config;
    }

    protected FSEvents events(){
        return events;
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        config.setTouchable(true);

        boolean resume = FSControl.getKeepAlive();

        events.GLPreSurfaceCreate(resume);

        FSRenderer.startRenderThread();
        FSRenderer.getRenderThread().task(FSRenderThread.CREATE_GL_CONTEXT, resume).task(FSRenderThread.SURFACE_CREATED, resume);

        events.GLPostSurfaceCreate(resume);
        choreographer.postFrameCallback(this);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        events.GLPreSurfaceChange(width, height);

        FSRenderer.getRenderThread().task(FSRenderThread.SURFACE_CHANGED, new int[]{ width, height });

        events.GLPostSurfaceChange(width, height);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        events.GLPreSurfaceDestroy();

        destroy();

        if(events != null){
            events.GLPostSurfaceDestroy();
        }
    }

    @Override
    public void doFrame(long frameTimeNanos){
        FSRenderer.getRenderThread().task(FSRenderThread.DRAW_FRAME, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        gesture.onTouchEvent(e);

        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_TOUCH, e, null, -1, -1);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_DOWN, e, null, -1, -1);
        }

        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SINGLETAP, e, null, -1, -1);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_LONGPRESS, e, null, -1, -1);
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, final float distanceY){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SCROLL, e1, e2, distanceX, distanceY);
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_FLING, e1, e2, velocityX, velocityY);
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e){
        if(FSRenderer.isInitialized && config.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SHOWPRESS, e, null, -1, -1);
        }
    }

    private void destroy(){
        FSControl.destroy();

        if(!FSControl.getKeepAlive()){
            getHolder().removeCallback(this);

            gesture = null;
            choreographer = null;
            config = null;
            events = null;

            destroyed = true;
        }
    }

    public static final class Config{

        private boolean dirtyrender;
        private boolean touchable;

        public Config(){
            dirtyrender = false;
            touchable = true;
        }

        public void setTouchable(boolean s){
            touchable = s;
        }

        public void setRenderContinuously(boolean s){
            dirtyrender = s;
        }

        public boolean getRenderContinuously(){
            return dirtyrender;
        }

        public boolean getTouchable(){
            return touchable;
        }
    }
}
