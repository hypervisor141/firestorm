package com.nurverek.firestorm;

import android.opengl.GLES32;

public class FSFrameBuffer{

    protected int id;

    public FSFrameBuffer(int id){
        this.id = id;
    }

    public FSFrameBuffer(){

    }

    public void initialize(){
        GLES32.glGenFramebuffers(1, FSCache.CACHE_INT,0);
        id = FSCache.CACHE_INT[0];
    }

    public void bind(){
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, id);
    }

    public void unbind(){
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
    }

    public void checkStatus(){
        int status = GLES32.glCheckFramebufferStatus(GLES32.GL_FRAMEBUFFER);

        if(status != GLES32.GL_FRAMEBUFFER_COMPLETE){
            throw new RuntimeException("Framebuffer incomplete with code : " + status);
        }
    }

    public void parameterI(int pname, int param){
        GLES32.glFramebufferParameteri(GLES32.GL_FRAMEBUFFER, pname, param);
    }

    public void attachTexture(int attachment, int texture, int level){
        GLES32.glFramebufferTexture(GLES32.GL_FRAMEBUFFER, attachment, texture, level);
    }

    public void attachTexture2D(int attachment, int textarget, int texture, int level){
        GLES32.glFramebufferTexture2D(GLES32.GL_FRAMEBUFFER, attachment, textarget, texture, level);
    }

    public void attachTextureLayer(int attachment, int texture, int level, int layer){
        GLES32.glFramebufferTextureLayer(GLES32.GL_FRAMEBUFFER, attachment, texture, level, layer);
    }

    public void attachRenderBuffer(int attachment, int renderbuffertarget, int renderbuffer){
        GLES32.glFramebufferRenderbuffer(GLES32.GL_FRAMEBUFFER, attachment, renderbuffertarget, renderbuffer);
    }

    public int id(){
        return id;
    }

    public void destroy(){
        FSCache.CACHE_INT[0] = id;
        GLES32.glDeleteFramebuffers(1, FSCache.CACHE_INT, 0);
    }
}
