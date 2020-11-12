package com.nurverek.firestorm;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public abstract class FSActivity extends Activity implements View.OnClickListener, FSEvents {

    protected int WIDTH, HEIGHT, REALWIDTH, REALHEIGHT;
    protected float DENSITY;
    protected FSSurface SURFACE;

    protected RelativeLayout BASE;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        initialize();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    protected void initialize(){
        DisplayMetrics metris = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();

        display.getMetrics(metris);
        WIDTH = metris.widthPixels;
        HEIGHT = metris.heightPixels;

        display.getRealMetrics(metris);
        REALWIDTH = metris.widthPixels;
        REALHEIGHT = metris.heightPixels;

        DENSITY = getResources().getDisplayMetrics().density;

        BASE = new RelativeLayout(this);
        modifyUI(BASE);

        FSControl.setMainDimensions(WIDTH, HEIGHT);
        FSControl.setRealDimensions(REALWIDTH, REALHEIGHT);

        createSurface();
        setContentView(BASE);
    }

    private void createSurface(){
        SURFACE = new FSSurface(this);
        SURFACE.setId(View.generateViewId());
        SURFACE.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        SURFACE.setX(0);
        SURFACE.setY(0);

        BASE.addView(SURFACE, 0);
    }

    protected abstract void modifyUI(RelativeLayout base);

    public RelativeLayout getBaseLayout(){
        return BASE;
    }

    protected void destroy(){
        BASE.removeAllViews();

        BASE = null;
        SURFACE = null;

        WIDTH = 0;
        HEIGHT = 0;
        DENSITY = 0;
    }




    @Override
    public void GLPreSurfaceCreate(boolean continuing){

    }

    @Override
    public void GLPostSurfaceCreate(boolean continuing){

    }

    @Override
    public void GLPreSurfaceChange(int width, int height){

    }

    @Override
    public void GLPostSurfaceChange(int width, int height){

    }

    @Override
    public void GLPreSurfaceDestroy(){

    }

    @Override
    public void GLPostSurfaceDestroy(){

    }

    @Override
    public void GLPreCreated(boolean continuing){

    }

    @Override
    public void GLPostCreated(boolean continuing){

    }

    @Override
    public void GLPreChange(int width, int height){

    }

    @Override
    public void GLPostChange(int width, int height){

    }

    @Override
    public void GLPreDraw(){

    }

    @Override
    public void GLPostDraw(){

    }

    @Override
    public void GLPreAdvancement(){

    }

    @Override
    public void GLPostAdvancement(long changes){

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    public void onClick(View v){

    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}