package com.firestorm.program;

import android.opengl.GLES32;

import com.firestorm.engine.FSCache;

public class FSVertexArray{

    protected int id = 0;

    public FSVertexArray(int id){
        this.id = id;
    }

    public FSVertexArray(){

    }

    public void create(){
        GLES32.glGenVertexArrays(1, FSCache.INT1, 0);
        id = FSCache.INT1[0];
    }

    public void bind(){
        GLES32.glBindVertexArray(id);
    }

    public void unbind(){
        GLES32.glBindVertexArray(0);
    }

    public int id(){
        return id;
    }

    public void destroy(){
        id = -1;

        FSCache.INT1[0] = id;
        GLES32.glDeleteVertexArrays(1, FSCache.INT1, 0);
    }
}
