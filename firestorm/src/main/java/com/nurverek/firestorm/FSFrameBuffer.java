package com.nurverek.firestorm;

import android.opengl.GLES32;

public class FSFrameBuffer{

    protected int id;

    public FSFrameBuffer(){

    }

    public void initialize(){
        id = FSRenderer.createFrameBuffer(1)[0];
    }


    public void checkStatus(){
        int status = FSRenderer.checkFramebufferStatus(GLES32.GL_FRAMEBUFFER);

        if(status != GLES32.GL_FRAMEBUFFER_COMPLETE){
            throw new RuntimeException("Framebuffer incomplete with code : " + status);
        }
    }

    public void attachTexture(int attachment, int texture, int level){
        FSRenderer.frameBufferTexture(GLES32.GL_FRAMEBUFFER, attachment, texture, level);
    }

    public void attachTexture2D(int attachment, int textarget, int texture, int level){
        FSRenderer.frameBufferTexture2D(GLES32.GL_FRAMEBUFFER, attachment, textarget, texture, level);
    }

    public void attachTextureLayer(int attachment, int texture, int level, int layer){
        FSRenderer.frameBufferTextureLayer(GLES32.GL_FRAMEBUFFER, attachment, texture, level, layer);
    }

    public void bind(){
        FSRenderer.frameBufferBind(GLES32.GL_FRAMEBUFFER, id);
    }

    public void unbind(){
        FSRenderer.frameBufferBind(GLES32.GL_FRAMEBUFFER, 0);
    }

    public int id(){
        return id;
    }

    public void destroy(){
        FSRenderer.deleteFrameBuffer(new int[]{ id });
    }
}
