package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLStringify;

public class FSVertexBuffer<BUFFER extends VLBuffer<?, ?>> implements VLStringify{

    public static int CURRENT_ACTIVE_BUFFER = 0;

    private BUFFER buffer;

    private int target;
    private int accessmode;
    private int bindpoint;
    private int sizebytes;
    private int id;

    private boolean mapped;
    private boolean needsupdate;

    public FSVertexBuffer(BUFFER buffer, int target, int accessmode){
        this.buffer = buffer;
        this.target = target;
        this.accessmode = accessmode;

        id = -1;
    }

    public FSVertexBuffer(BUFFER buffer, int target, int accessmode, int bindpoint){
        this.buffer = buffer;
        this.target = target;
        this.accessmode = accessmode;
        this.bindpoint = bindpoint;

        id = -1;
    }

    public FSVertexBuffer(BUFFER buffer, int id, int target, int accessmode, int bindpoint){
        this.buffer = buffer;
        this.id = id;
        this.target = target;
        this.accessmode = accessmode;
        this.bindpoint = bindpoint;
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
        sizebytes = buffer.sizeBytes();
        bind();

        buffer.position(0);
        GLES32.glBufferData(target, sizebytes, buffer.provider(), accessmode);

        needsupdate = false;
    }

    public void update(int offset, int count){
        bind();

        int bytes = buffer.getTypeBytes();

        buffer.position(offset);
        GLES32.glBufferSubData(target, offset * bytes, count * bytes, buffer.provider());

        needsupdate = false;
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
        FSCache.INT1[0] = id;
        GLES32.glDeleteBuffers(1, FSCache.INT1, 0);

        id = -1;
    }
}
