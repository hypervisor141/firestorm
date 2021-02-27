package com.nurverek.firestorm;

import android.opengl.GLES32;

public class FSFrameBuffer{

    protected int id;

    public FSFrameBuffer(){

    }

    public void initialize(){
        id = FSR.createFrameBuffer(1)[0];
    }


    public void checkStatus(){
        int status = FSR.checkFramebufferStatus(GLES32.GL_FRAMEBUFFER);

        if(status != GLES32.GL_FRAMEBUFFER_COMPLETE){
            throw new RuntimeException("Framebuffer incomplete with code : " + status);
        }
    }

    public void attachTexture(int attachment, int texture, int level){
        FSR.frameBufferTexture(GLES32.GL_FRAMEBUFFER, attachment, texture, level);
    }

    public void attachTexture2D(int attachment, int textarget, int texture, int level){
        FSR.frameBufferTexture2D(GLES32.GL_FRAMEBUFFER, attachment, textarget, texture, level);
    }

    public void attachTextureLayer(int attachment, int texture, int level, int layer){
        FSR.frameBufferTextureLayer(GLES32.GL_FRAMEBUFFER, attachment, texture, level, layer);
    }

    public void bind(){
        FSR.frameBufferBind(GLES32.GL_FRAMEBUFFER, id);
    }

    public void unbind(){
        FSR.frameBufferBind(GLES32.GL_FRAMEBUFFER, 0);
    }

    public int id(){
        return id;
    }

    public void destroy(){
        FSR.deleteFrameBuffer(new int[]{ id });
    }
}
