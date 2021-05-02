package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLBuffer;
import vanguard.VLBufferFloat;
import vanguard.VLBufferShort;
import vanguard.VLBufferTracker;
import vanguard.VLListType;

public abstract class FSElement<DATA, BUFFER extends VLBuffer<?, ?>>{

    public DATA data;
    public VLListType<FSBufferBinding<BUFFER>> bindings;

    public FSElement(DATA data){
        this.data = data;
        bindings = new VLListType<>(1, 5);
    }

    public abstract int size();
    public abstract void buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer);
    public abstract void buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride);
    public abstract void updateBuffer(int index);

    public void updateBuffer(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            updateBuffer(i);
        }
    }

    public void updateVertexBuffer(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            bindings.get(i).updateVertexBuffer();
        }
    }

    public void updateVertexBufferStrict(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            bindings.get(i).updateVertexBufferStrict();
        }
    }

    public void updateBufferPipeline(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            updateBuffer(i);
            updateVertexBuffer(i);
        }
    }

    public void updateBufferPipelineStrict(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            updateBuffer(i);
            updateVertexBufferStrict(i);
        }
    }

    public void updateVertexBuffer(int index){
        bindings.get(index).updateVertexBuffer();
    }

    public void updateVertexBufferStrict(int index){
        bindings.get(index).updateVertexBufferStrict();
    }

    public void updateBufferPipeline(int index){
        updateBuffer(index);
        updateVertexBuffer(index);
    }

    public void updateBufferPipelineStrict(int index){
        updateBuffer(index);
        updateVertexBufferStrict(index);
    }

    public static class Short extends FSElement<VLArrayShort, VLBufferShort>{

        public Short(VLArrayShort data){
            super(data);
        }

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public void buffer(FSVertexBuffer<VLBufferShort> vbuffer, VLBufferShort buffer){
            VLBufferTracker tracker = new VLBufferTracker();
            buffer.put(tracker, data.provider());

            bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));
        }

        @Override
        public void buffer(FSVertexBuffer<VLBufferShort> vbuffer, VLBufferShort buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = new VLBufferTracker();
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));
        }

        @Override
        public void updateBuffer(int index){
            FSBufferBinding<VLBufferShort> binding = bindings.get(index);
            binding.buffer.update(binding.tracker, (short[])data.provider());
        }
    }

    public static class Float extends FSElement<VLArrayFloat, VLBufferFloat>{

        public Float(VLArrayFloat data){
            super(data);
        }

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public void buffer(FSVertexBuffer<VLBufferFloat> vbuffer, VLBufferFloat buffer){
            VLBufferTracker tracker = new VLBufferTracker();
            buffer.put(tracker, data.provider());

            bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));
        }

        @Override
        public void buffer(FSVertexBuffer<VLBufferFloat> vbuffer, VLBufferFloat buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = new VLBufferTracker();
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));
        }

        @Override
        public void updateBuffer(int index){
            FSBufferBinding<VLBufferFloat> binding = bindings.get(index);
            binding.buffer.update(binding.tracker, (float[])data.provider());
        }
    }
}
