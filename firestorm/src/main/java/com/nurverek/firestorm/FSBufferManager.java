package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLArray;
import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayInt;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLBufferDirect;
import com.nurverek.vanguard.VLBufferFloat;
import com.nurverek.vanguard.VLBufferInt;
import com.nurverek.vanguard.VLBufferShort;
import com.nurverek.vanguard.VLListType;

public class FSBufferManager{

    protected VLListType<Entry> entries;

    public FSBufferManager(int capacity){
        entries = new VLListType<>(capacity, capacity);
    }

    public int addShortBuffer(int target, int accessmode, int bindpoint){
        FSVertexBuffer vbuffer = new FSVertexBuffer(target, accessmode, bindpoint);
        vbuffer.provider(new VLBufferShort());
        entries.add(new EntryShort(vbuffer));

        return entries.size() - 1;
    }
    
    public int addIntBuffer(int target, int accessmode, int bindpoint){
        FSVertexBuffer vbuffer = new FSVertexBuffer(target, accessmode, bindpoint);
        vbuffer.provider(new VLBufferInt());
        entries.add(new EntryInt(vbuffer));

        return entries.size() - 1;
    }

    public int addFloatBuffer(int target, int accessmode, int bindpoint){
        FSVertexBuffer vbuffer = new FSVertexBuffer(target, accessmode, bindpoint);
        vbuffer.provider(new VLBufferFloat());
        entries.add(new EntryFloat(vbuffer));

        return entries.size() - 1;
    }

    public void increaseTargetCapacity(int index, int count){
        entries.get(index).increaseTargetCapacity(count);
    }

    public void initialize(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).initialize();
        }
    }

    public void initialize(int index){
        entries.get(index).initialize();
    }

    public void initializeLast(){
        entries.get(entries.size() - 1).initialize();
    }

    public int buffer(int index, VLArray array){
        return entries.get(index).put(array);
    }

    public int buffer(int index, VLArray array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
        return entries.get(index).put(array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
    }

    public int bufferSync(int index, VLArray array){
        return entries.get(index).putSync(array);
    }

    public int bufferSync(int index, VLArray array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
        return entries.get(index).putSync(array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
    }

    public void upload(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).vertexbuffer.upload();
        }
    }

    public void update(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).vertexbuffer.update();
        }
    }

    public void updateIfNeeded(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).vertexbuffer.updateIfNeeded();
        }
    }

    public void releaseClients(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).vertexbuffer.releaseClientBuffer();
        }
    }

    public void bind(int index){
        entries.get(index).vertexbuffer.bind();
    }

    public void unbindArrayBuffer(){
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
    }

    public void unbindElementBuffer(){
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int position(int index){
        return entries.get(index).directbuffer.position();
    }

    public int size(int index){
        return entries.get(index).directbuffer.size();
    }

    public FSVertexBuffer get(int index){
        return entries.get(index).vertexbuffer;
    }

    public FSVertexBuffer remove(int index){
        return entries.remove(index).vertexbuffer;
    }

    public int size(){
        return entries.size();
    }



    public static abstract class Entry<ARRAYTYPE extends VLArray>{

        public FSVertexBuffer vertexbuffer;

        protected VLBufferDirect directbuffer;
        protected int targetcapacity;

        protected Entry(FSVertexBuffer vertexbuffer){
            this.vertexbuffer = vertexbuffer;
            directbuffer = vertexbuffer.provider();
        }

        public void increaseTargetCapacity(int count){
            targetcapacity += count;
        }

        protected void initialize(){
            directbuffer.initialize(targetcapacity);
            vertexbuffer.initialize();
        }

        protected abstract int put(ARRAYTYPE array);

        protected abstract int put(ARRAYTYPE array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride);

        protected abstract int putSync(ARRAYTYPE array);

        protected abstract int putSync(ARRAYTYPE array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride);
    }

    protected static final class EntryShort extends Entry<VLArrayShort>{

        protected EntryShort(FSVertexBuffer vbuffer){
            super(vbuffer);
        }

        @Override
        protected int put(VLArrayShort array){
            directbuffer.put(array.provider());
            return directbuffer.position();
        }

        @Override
        protected int put(VLArrayShort array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return directbuffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        protected int putSync(VLArrayShort array){
            array.SYNCER.add(new VLBufferShort.DefinitionArray(directbuffer, directbuffer.position()));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array);
        }

        @Override
        protected int putSync(VLArrayShort array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            array.SYNCER.add(new VLBufferShort.DefinitionArrayInterleaved(directbuffer, directbuffer.position(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }
    }

    protected static final class EntryInt extends Entry<VLArrayInt>{

        protected EntryInt(FSVertexBuffer vbuffer){
            super(vbuffer);
        }

        @Override
        protected int put(VLArrayInt array){
            directbuffer.put(array.provider());
            return directbuffer.position();
        }

        @Override
        protected int put(VLArrayInt array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return directbuffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        protected int putSync(VLArrayInt array){
            array.SYNCER.add(new VLBufferInt.DefinitionArray(directbuffer, directbuffer.position()));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array);
        }

        @Override
        protected int putSync(VLArrayInt array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            array.SYNCER.add(new VLBufferInt.DefinitionArrayInterleaved(directbuffer, directbuffer.position(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

    }

    protected static final class EntryFloat extends Entry<VLArrayFloat>{

        protected EntryFloat(FSVertexBuffer vbuffer){
            super(vbuffer);
        }

        @Override
        protected int put(VLArrayFloat array){
            directbuffer.put(array.provider());
            return directbuffer.position();
        }

        @Override
        protected int put(VLArrayFloat array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            return directbuffer.putInterleaved(array.provider(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

        @Override
        protected int putSync(VLArrayFloat array){
            array.SYNCER.add(new VLBufferFloat.DefinitionArray(directbuffer, directbuffer.position()));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array);
        }

        @Override
        protected int putSync(VLArrayFloat array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            array.SYNCER.add(new VLBufferFloat.DefinitionArrayInterleaved(directbuffer, directbuffer.position(), arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride));
            array.SYNCER.add(new FSVertexBuffer.Definition(vertexbuffer));

            return put(array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }

    }
}
