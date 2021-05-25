package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLLog;
import vanguard.VLLoggable;

public class FSVertexBuffer<BUFFER extends VLBuffer<?, ?>> implements VLLoggable{

    public static int CURRENT_ACTIVE_BUFFER = 0;

    protected BUFFER buffer;

    protected int target;
    protected int accessmode;
    protected int bindpoint;
    protected int sizebytes;
    protected int id;

    protected boolean mapped;
    protected boolean needsupdate;
    protected boolean uploaded;

    public FSVertexBuffer(BUFFER buffer, int target, int accessmode){
        this.buffer = buffer;
        this.target = target;
        this.accessmode = accessmode;

        mapped = false;
        needsupdate = false;
        uploaded = false;
        bindpoint = -1;
        sizebytes = -1;
        id = -1;
    }

    public FSVertexBuffer(BUFFER buffer, int target, int accessmode, int bindpoint){
        this.buffer = buffer;
        this.target = target;
        this.accessmode = accessmode;
        this.bindpoint = bindpoint;

        mapped = false;
        needsupdate = false;
        uploaded = false;
        sizebytes = -1;
        id = -1;
    }

    protected FSVertexBuffer(){

    }

    public void initialize(){
        destroy();

        GLES32.glGenBuffers(1, FSCache.INT1, 0);
        id = FSCache.INT1[0];
    }

    public void bind(){
        if(CURRENT_ACTIVE_BUFFER != id){
            GLES32.glBindBuffer(target, id);
            CURRENT_ACTIVE_BUFFER = id;
        }
    }

    public void unbind(){
        GLES32.glBindBuffer(target, 0);
        CURRENT_ACTIVE_BUFFER = 0;
    }

    public void upload(){
        if(!uploaded){
            sizebytes = buffer.sizeBytes();
            bind();

            buffer.position(0);
            GLES32.glBufferData(target, sizebytes, buffer.provider(), accessmode);

            needsupdate = false;
            uploaded = true;
        }
    }

    public void update(int offset, int count){
        bind();

        int bytes = buffer.getTypeBytes();

        buffer.position(offset);
        GLES32.glBufferSubData(target, offset * bytes, count * bytes, buffer.provider());

        needsupdate = false;
        uploaded = true;
    }

    public void update(){
        bind();
        FSTools.checkGLError();

        buffer.position(0);
        GLES32.glBufferSubData(target, 0, sizebytes, buffer.provider());

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

        ByteBuffer b = (ByteBuffer)GLES32.glMapBufferRange(target, offset * bytes, size * bytes, GLES32.GL_MAP_READ_BIT | GLES32.GL_MAP_WRITE_BIT);
        b.order(ByteOrder.nativeOrder());

        buffer.initialize(b);

        needsupdate = false;
        uploaded = true;
        mapped = true;
    }

    public void map(){
        map(0, sizebytes);
    }

    public void flushMap(int offset, int size){
        int bytes = buffer.getTypeBytes();
        GLES32.glFlushMappedBufferRange(target, offset * bytes, size * bytes);

        needsupdate = false;
    }

    public void flushMap(){
        GLES32.glFlushMappedBufferRange(target, 0, sizeBytes());
        needsupdate = false;
    }

    public void flushMapIfNeeded(){
        if(needsupdate){
            flushMap();
        }
    }

    public BUFFER unMap(){
        GLES32.glUnmapBuffer(target);

        mapped = false;
        needsupdate = false;

        return buffer;
    }

    public void markForUpdate(){
        needsupdate = true;
    }

    public void allowUpload(){
        uploaded = false;
    }

    public void bindBufferBase(){
        GLES32.glBindBufferBase(target, bindpoint, id);
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

    public boolean uploaded(){
        return uploaded;
    }

    public int bindPoint(){
        return bindpoint;
    }

    public int sizeBytes(){
        return sizebytes;
    }

    @Override
    public void log(VLLog log, Object data){
        log.append("id[");
        log.append(id);
        log.append("] accessMode[");
        log.append(accessmode);
        log.append("] sizeBytes[");
        log.append(sizebytes);
        log.append("] bindPoint[");
        log.append(bindpoint);
        log.append("] backingBuffer[");
        log.append(buffer.getClass().getSimpleName());
        log.append("]");
    }

    public void destroy(){
        FSCache.INT1[0] = id;
        GLES32.glDeleteBuffers(1, FSCache.INT1, 0);

        id = -1;
    }
}
