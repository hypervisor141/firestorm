package com.nurverek.firestorm;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public abstract class FSActivity extends AppCompatActivity implements View.OnClickListener, FSEvents{

    protected RelativeLayout BASE;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        BASE = new RelativeLayout(this);

        FSSurface surface = FSControl.initialize(this, this);
        surface.setId(View.generateViewId());
        surface.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surface.setX(0);
        surface.setY(0);

        BASE.addView(surface, 0);

        modifyUI(BASE);
        setContentView(BASE);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    protected abstract void modifyUI(RelativeLayout base);

    public RelativeLayout getBaseLayout(){
        return BASE;
    }

    protected void destroy(){
        BASE.removeAllViews();
        BASE = null;
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
}