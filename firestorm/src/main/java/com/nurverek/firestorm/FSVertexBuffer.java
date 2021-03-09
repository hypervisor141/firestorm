package com.nurverek.firestorm;

import android.opengl.GLES32;

import vanguard.VLBuffer;
import vanguard.VLStringify;

public class FSVertexBuffer<BUFFER extends VLBuffer<?, ?>> implements VLStringify{

    private BUFFER buffer;

    private int target;
    private int accessmode;
    private int bindpoint;
    private int sizebytes;
    private int id;

    private boolean mapped;
    private boolean needsupdate;

    public FSVertexBuffer(int target, int accessmode){
        this.target = target;
        this.accessmode = accessmode;
        id = -1;
    }

    public FSVertexBuffer(int target, int accessmode, int bindpoint){
        this.target = target;
        this.accessmode = accessmode;
        this.bindpoint = bindpoint;

        id = -1;
    }


    public void initialize(){
        destroy();
        id = FSR.createBuffers(1)[0];
    }

    public void bind(){
        FSR.vertexBufferBind(target, id);
    }

    public void unbind(){
        FSR.vertexBufferBind(target, 0);
    }

    public void upload(){
        sizebytes = buffer.sizeBytes();
        bind();

        buffer.position(0);
        FSR.vertexBufferData(target, sizebytes, buffer.provider(), accessmode);

        needsupdate = false;
    }

    public void update(int offset, int size){
        bind();

        int bytes = buffer.getTypeBytes();
        buffer.position(offset);
        FSR.vertexBufferSubData(target, offset * bytes, size * bytes, buffer.provider());

        needsupdate = false;
    }

    public void update(){
        bind();
        FSTools.checkGLError();

        buffer.position(0);
        FSR.vertexBufferSubData(target, 0, sizebytes, buffer.provider());

        needsupdate = false;
    }

    public void updateIfNeeded(){
        if(needsupdate){
            update();
        }
    }

    public void map(int offset, int size){
        int bytes = buffer.getTypeBytes();

        bind();
        buffer.initialize(FSR.mapBufferRange(target, offset * bytes, size * bytes, GLES32.GL_MAP_READ_BIT | GLES32.GL_MAP_WRITE_BIT));

        needsupdate = false;
        mapped = true;
    }

    public void map(){
        map(0, sizebytes);
    }

    public void flushMap(int offset, int size){
        int bytes = buffer.getTypeBytes();
        FSR.flushMapBuffer(target, offset * bytes, size * bytes);

        needsupdate = false;
    }

    public void flushMap(){
        FSR.flushMapBuffer(target, 0, sizeBytes());
        needsupdate = false;
    }

    public void flushMapIfNeeded(){
        if(needsupdate){
            flushMap();
        }
    }

    public BUFFER unMap(){
        FSR.unMapBuffer(target);

        mapped = false;
        needsupdate = false;

        return buffer;
    }

    public void markForUpdate(){
        needsupdate = true;
    }

    public void bindBufferBase(){
        FSR.vertexBufferBindBase(target, bindpoint, id);
    }

    public void bindPoint(int newbindpoint){
        bindpoint = newbindpoint;
    }

    public void releaseClientBuffer(){
        buffer.release();
    }

    public void provider(BUFFER buffer){
        this.buffer = buffer;
    }

    public void setTarget(int s){
        target = s;
    }

    public void setID(int s){
        id = s;
    }

    public void setAccessMode(int s){
        accessmode = s;
    }

    public BUFFER provider(){
        return buffer;
    }

    public int getTarget(){
        return target;
    }

    public int getBufferID(){
        return id;
    }

    public int getAccessMode(){
        return accessmode;
    }

    public boolean isMapped(){
        return mapped;
    }

    public boolean needsUpdate(){
        return needsupdate;
    }

    public int bindPoint(){
        return bindpoint;
    }

    public void resize(int size){
        buffer.resize(size);
        upload();
    }

    public int sizeBytes(){
        return sizebytes;
    }

    @Override
    public void stringify(StringBuilder src, Object hint){
        src.append("[VertexBuffer] backBuffer[ ");
        buffer.stringify(src, hint);
        src.append(" ]");
    }

    public void destroy(){
        if(id != -1){
            FSR.deleteVertexBuffers(new int[]{ id });
            id = -1;
        }
    }
}
