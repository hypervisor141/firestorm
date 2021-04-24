package com.nurverek.firestorm;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public abstract class FSActivity extends AppCompatActivity{

    protected RelativeLayout BASE;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        BASE = new RelativeLayout(this);

        FSSurface surface = createSurface();
        surface.setId(View.generateViewId());
        surface.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surface.setX(0);
        surface.setY(0);

        BASE.addView(surface, 0);

        modifyUI(BASE);
        setContentView(BASE);
    }

    protected abstract FSSurface createSurface();

    protected abstract void modifyUI(RelativeLayout base);

    public RelativeLayout getBaseLayout(){
        return BASE;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(BASE != null){
            BASE.removeAllViews();
            BASE = null;
        }
    }
}