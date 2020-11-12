package com.nurverek.firestorm;

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
    private FSActivity activity;

    private boolean isDestroyed;



    protected FSSurface(FSActivity activity){
        super(activity.getApplicationContext());

        this.activity = activity;

        FSControl.initialize(activity, this, null);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        gesture = new GestureDetectorCompat(activity, this);
        choreographer = Choreographer.getInstance();

        isDestroyed = false;
    }

    protected void postFrame(){
        choreographer.postFrameCallback(this);
    }

    public boolean isDestroyed(){
        return isDestroyed;
    }

    public FSActivity getActivity(){
        return activity;
    }





    @Override
    public void surfaceCreated(SurfaceHolder holder){
        FSControl.SCONFIG.setTouchable(true);
        boolean continuing = FSControl.SCONFIG.getKeepAlive();

        FSControl.EVENTS.GLPreSurfaceCreate(continuing);

        FSRenderer.startRenderer();
        FSRenderer.RENDERTHREAD.assign(FSRenderer.RenderThread.INITIALIZE_GL, continuing)
                .assign(FSRenderer.RenderThread.SURFACE_CREATED, continuing);

        FSControl.EVENTS.GLPostSurfaceCreate(continuing);
        choreographer.postFrameCallback(this);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        FSControl.EVENTS.GLPreSurfaceChange(width, height);
        FSRenderer.RENDERTHREAD.assign(FSRenderer.RenderThread.SURFACE_CHANGED, new int[]{ width, height });
        FSControl.EVENTS.GLPostSurfaceChange(width, height);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        FSControl.EVENTS.GLPreSurfaceDestroy();
        destroy();

        if(FSControl.EVENTS != null){
            FSControl.EVENTS.GLPostSurfaceDestroy();
        }
    }


    @Override
    public void doFrame(long frameTimeNanos){
        FSRenderer.RENDERTHREAD.assign(FSRenderer.RenderThread.DRAW_FRAME, null);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e){
        gesture.onTouchEvent(e);

        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_TOUCH, e, null, -1, -1);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_DOWN, e, null, -1, -1);
        }

        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SINGLETAP, e, null, -1, -1);
        }

        return false;
    }


    @Override
    public void onLongPress(MotionEvent e){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_LONGPRESS, e, null, -1, -1);
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, final float distanceY){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SCROLL, e1, e2, distanceX, distanceY);
        }

        return true;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_FLING, e1, e2, velocityX, velocityY);
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e){
        if(FSRenderer.isInitialized && FSControl.SCONFIG.getTouchable()){
            FSInput.checkInput(FSInput.TYPE_SHOWPRESS, e, null, -1, -1);
        }
    }

    private void destroy(){
        isDestroyed = false;

        FSControl.destroy();

        if(!FSControl.SCONFIG.getKeepAlive()){
            getHolder().removeCallback(this);

            FSControl.SCONFIG = null;
            gesture = null;
            choreographer = null;
            isDestroyed = true;
            activity = null;
        }
    }
}
