package com.nurverek.firestorm;

import android.opengl.GLES32;

public final class FSVertexArray{

    private int id = 0;

    public FSVertexArray(int id){
        this.id = id;
    }

    public FSVertexArray(){

    }

    public void create(){
        GLES32.glGenVertexArrays(1, FSStatic.CACHE_INT, 0);
        id = FSStatic.CACHE_INT[0];
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

        FSStatic.CACHE_INT[0] = id;
        GLES32.glDeleteVertexArrays(1, FSStatic.CACHE_INT, 0);
    }
}
