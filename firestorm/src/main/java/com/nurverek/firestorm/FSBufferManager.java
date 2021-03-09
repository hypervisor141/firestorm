package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayInt;
import vanguard.VLArrayShort;
import vanguard.VLBuffer;
import vanguard.VLBufferManagerBase;

public class FSBufferManager extends VLBufferManagerBase<FSEntryTypeVertexBuffer, FSBufferManager, FSBufferAddress>{

    public FSBufferManager(int capacity, int resizer){
        super(capacity, resizer);
    }

    @Override
    public FSEntryTypeVertexBuffer get(int index){
        return entries.get(index);
    }

    @Override
    public void remove(int index){
        entries.remove(index);
    }

    @Override
    public void fillBufferAddress(FSBufferAddress results, int bufferindex, int offset, int unitoffset, int unitsize, int stride, int count){
        results.fill(this, bufferindex, offset, unitoffset, unitsize, stride, count);
    }

    public void upload(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).vertexbuffer.upload();
        }
    }

    public void updateIfNeeded(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).vertexbuffer.updateIfNeeded();
        }
    }

    public static class EntryShort extends FSEntryTypeVertexBuffer<VLArrayShort>{

        public EntryShort(FSVertexBuffer vbuffer, VLBuffer buffer){
            super(vbuffer, buffer);
        }

        @Override
        protected int put(VLArrayShort array){
            buffer.put(array.provider());
            return buffer.position();
        }

        @Override
        protected int put(VLArrayShort array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return buffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        public void release(){
            super.release();
            vertexbuffer.destroy();
        }
    }

    public static class EntryInt extends FSEntryTypeVertexBuffer<VLArrayInt>{

        public EntryInt(FSVertexBuffer vbuffer, VLBuffer buffer){
            super(vbuffer, buffer);
        }

        @Override
        protected int put(VLArrayInt array){
            buffer.put(array.provider());
            return buffer.position();
        }

        @Override
        protected int put(VLArrayInt array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return buffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        public void release(){
            super.release();
            vertexbuffer.destroy();
        }
    }

    public static class EntryFloat extends FSEntryTypeVertexBuffer<VLArrayFloat>{

        public EntryFloat(FSVertexBuffer vbuffer, VLBuffer buffer){
            super(vbuffer, buffer);
        }

        @Override
        protected int put(VLArrayFloat array){
            buffer.put(array.provider());
            return buffer.position();
        }

        @Override
        protected int put(VLArrayFloat array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return buffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        public void release(){
            super.release();
            vertexbuffer.destroy();
        }
    }
}
