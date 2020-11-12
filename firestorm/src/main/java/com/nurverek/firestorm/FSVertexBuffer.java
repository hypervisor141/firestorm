package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLBufferDirect;
import com.nurverek.vanguard.VLStringify;
import com.nurverek.vanguard.VLSyncer;

public class FSVertexBuffer extends VLSyncer.Syncable implements VLStringify {

    private VLBufferDirect buffer;

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
        id = FSRenderer.createBuffers(1)[0];
    }

    public void bind(){
        FSRenderer.vertexBufferBind(target, id);
    }

    public void unbind(){
        FSRenderer.vertexBufferBind(target, 0);
    }

    public void upload(){
        sizebytes = buffer.sizeBytes();
        buffer.position(0);

        bind();
        FSRenderer.vertexBufferData(target, sizebytes, (java.nio.Buffer)buffer.provider(), accessmode);

        needsupdate = false;
    }

    public void update(int offset, int size){
        bind();

        int bytes = buffer.getTypeBytes();
        buffer.position(offset);
        FSRenderer.vertexBufferSubData(target, offset * bytes, size * bytes, (java.nio.Buffer)buffer.provider());

        needsupdate = false;
    }

    public void update(){
        bind();
        buffer.position(0);
        FSRenderer.vertexBufferSubData(target, 0, buffer.sizeBytes(), (java.nio.Buffer)buffer.provider());

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
        buffer.initialize(FSRenderer.mapBufferRange(target, offset * bytes, size * bytes, GLES32.GL_MAP_READ_BIT | GLES32.GL_MAP_WRITE_BIT));

        needsupdate = false;
        mapped = true;
    }

    public void map(){
        map(0, sizebytes);
    }

    public void flushMap(int offset, int size){
        int bytes = buffer.getTypeBytes();
        FSRenderer.flushMapBuffer(target, offset * bytes, size * bytes);

        needsupdate = false;
    }

    public void flushMap(){
        FSRenderer.flushMapBuffer(target, 0, sizeBytes());
        needsupdate = false;
    }

    public void flushMapIfNeeded(){
        if(needsupdate){
            flushMap();
        }
    }

    public VLBufferDirect unMap(){
        FSRenderer.unMapBuffer(target);

        mapped = false;
        needsupdate = false;

        return buffer;
    }

    public void bindBufferBase(){
        FSRenderer.vertexBufferBindBase(target, bindpoint, id);
    }

    public void bindPoint(int newbindpoint){
        bindpoint = newbindpoint;
    }

    public void releaseClientBuffer(){
        buffer.release();
    }

    public void provider(VLBufferDirect buffer){
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

    public VLBufferDirect provider(){
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
            FSRenderer.deleteVertexBuffers(new int[]{ id });
            id = -1;
        }
    }

    public static final class Definition extends VLSyncer.Definition<VLSyncer.Syncable, FSVertexBuffer>{

        public Definition(FSVertexBuffer target){
            super(target);
        }

        @Override
        protected void sync(VLSyncer.Syncable source, FSVertexBuffer target){
            target.needsupdate = true;
        }
    }
}
