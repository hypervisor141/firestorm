package com.nurverek.firestorm;

import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    private static final int BUFFER_PRINT_LIMIT = 100;

    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    protected VLListType<EntryType<BUFFER>> entries;

    protected int totalstride;
    protected boolean interleaved;
    protected boolean uploaded;

    private boolean debuggedsegmentstructure;

    public FSBufferSegment(FSVertexBuffer<BUFFER> vbuffer, boolean interleaved, int capacity){
        this.interleaved = interleaved;
        this.vbuffer = vbuffer;
        this.buffer = vbuffer.provider();

        entries = new VLListType<>(capacity, capacity / 2);

        debuggedsegmentstructure = false;
        uploaded = false;
    }

    public FSBufferSegment(BUFFER buffer, boolean interleaved, int capacity){
        this.interleaved = interleaved;
        this.buffer = buffer;

        entries = new VLListType<>(capacity, capacity / 2);

        debuggedsegmentstructure = false;
        uploaded = false;
    }

    public void prepare(FSMesh target){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            EntryType<BUFFER> entry = entries.get(i);

            buffer.adjustPreInitCapacity(entry.calculateNeededSize(target));
        }
    }

    public FSBufferSegment<BUFFER> add(EntryType<BUFFER> entry){
        totalstride += entry.practicalUnitSize();
        entries.add(entry);

        return this;
    }

    private void checkInitialize(){
        if(buffer.provider() == null){
            buffer.initialize(ByteOrder.nativeOrder());

            if(vbuffer != null && vbuffer.getBufferID() >= 0){
                vbuffer.initialize();
            }
        }
    }

    protected void buffer(FSMesh target){
        checkInitialize();

        int size = entries.size();

        if(interleaved){
            for(int i = 0; i < size - 1; i++){
                int initialoffset = buffer.position();

                EntryType<BUFFER> entry = entries.get(i);
                entry.bufferInterleaved(target, buffer, vbuffer, totalstride);

                buffer.position(initialoffset + entry.practicalUnitSize());
            }

            entries.get(size - 1).bufferInterleaved(target, buffer, vbuffer, totalstride);

        }else{
            for(int i = 0; i < size - 1; i++){
                entries.get(i).bufferSequential(target, buffer, vbuffer, totalstride);
            }

            entries.get(size - 1).bufferSequential(target, buffer, vbuffer, totalstride);
        }
    }

    protected void bufferDebug(FSMesh target, VLLog log){
        int size = entries.size();

        log.append("[");
        log.append(getClass().getSimpleName());
        log.append("] stride[");
        log.append(totalstride);
        log.append("]\n");

        if(!debuggedsegmentstructure){
            if(totalstride <= 0){
                log.append("[Invalid stride value] stride[");
                log.append(totalstride);
                log.append("]");
                log.printError();

                throw new RuntimeException();
            }
            if(interleaved){
                if(size <= 1){
                    log.append("[Segment is set to interleaved mode but there is less than 1 entry]");
                    log.printError();

                    throw new RuntimeException();
                }
            }

            debuggedsegmentstructure = true;
        }

        if(interleaved){
            EntryType<BUFFER> entry = entries.get(0);
            int unitcountrequired = entry.calculateInterleaveDebugUnitCount(target);

            log.append("[Checking for mismatch between buffer target unit counts for interleaving]\n");

            for(int i = 0; i < size; i++){
                entry = entries.get(i);
                entry.checkForInterleavingErrors(target, unitcountrequired, log);

                log.append("\n");
            }

            log.printInfo();
        }

        log.append("[Entries]");

        for(int i = 0; i < size; i++){
            entries.get(i).debugInfo(log);
            log.append("\n");
        }

        log.printInfo();

        buffer(target);
    }

    public void upload(){
        if(!uploaded && vbuffer != null){
            vbuffer.upload();
            uploaded = true;
        }
    }

    public interface EntryType<BUFFER extends VLBuffer<?, ?>>{

        int calculateNeededSize(FSMesh target);
        int calculateInterleaveDebugUnitCount(FSMesh target);
        void bufferSequential(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void bufferInterleaved(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        int practicalUnitSize();
        void checkForInterleavingErrors(FSMesh target, int unitcountrequired, VLLog log);
        void debugInfo(VLLog log);
    }

    public static abstract class ElementType<BUFFER extends VLBuffer<?, ?>> implements EntryType<BUFFER>{

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;
        public int instanceoffset;
        public int instancecount;

        public ElementType(int element, int unitoffset, int unitsize, int unitsubcount, int instanceoffset, int instancecount){
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
            this.instanceoffset = instanceoffset;
            this.instancecount = instancecount;
        }

        public ElementType(int element, int instanceoffset, int instancecount){
            this.element = element;
            this.instanceoffset = instanceoffset;
            this.instancecount = instancecount;
        }

        public ElementType(int element, int unitoffset, int unitsize, int unitsubcount){
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;

            this.instanceoffset = 0;
            this.instancecount = -1;
        }

        @Override
        public int practicalUnitSize(){
            return unitsubcount;
        }

        @Override
        public int calculateNeededSize(FSMesh target){
            int total = 0;
            int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

            for(int i = instanceoffset; i < size; i++){
                total += calculateNeededSize(target.get(i));
            }

            return (total / unitsize) * unitsubcount;
        }

        protected abstract int calculateNeededSize(FSInstance instance);

        @Override
        public int calculateInterleaveDebugUnitCount(FSMesh target){
            return calculateNeededSize(target.first()) / unitsize;
        }

        @Override
        public void bufferSequential(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

            for(int i = instanceoffset; i < size; i++){
                bufferSequential(target, i, buffer, vbuffer, stride);
            }
        }

        @Override
        public void bufferInterleaved(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

            for(int i = instanceoffset; i < size; i++){
                bufferInterleaved(target, i, buffer, vbuffer, stride);
            }
        }

        public abstract void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        public abstract void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);

        @Override
        public void checkForInterleavingErrors(FSMesh target, int unitcountrequired, VLLog log){
            int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

            for(int i = instanceoffset; i < size; i++){
                checkForInterleavingErrors(target.get(i), unitcountrequired, log);
            }
        }

        public abstract void checkForInterleavingErrors(FSInstance instance, int unitcountrequired, VLLog log);

        @Override
        public void debugInfo(VLLog log){
            log.append("[");
            log.append(getClass().getSimpleName());
            log.append("] element[");
            log.append(FSGlobal.NAMES[element]);
            log.append("] unitOffset[");
            log.append(unitoffset);
            log.append("] unitSize[");
            log.append(unitsize);
            log.append("] unitSubCount[");
            log.append(unitsubcount);
            log.append("] instanceOffset[");
            log.append(instanceoffset);
            log.append("] instanceCount[");
            log.append(instancecount < 0 ? "MAX" : instancecount);
            log.append("]");
        }
    }

    public static class ElementStoreSubset<BUFFER extends VLBuffer<?, ?>> extends ElementType<BUFFER>{

        public int storeoffset;
        public int storecount;

        public ElementStoreSubset(int element, int unitoffset, int unitsize, int unitsubcount, int instanceoffset, int instancecount, int storeoffset, int storecount){
            super(element, unitoffset, unitsize, unitsubcount, instanceoffset, instancecount);

            this.storeoffset = storeoffset;
            this.storecount = storecount;
        }

        public ElementStoreSubset(int element, int unitoffset, int unitsize, int unitsubcount, int storeoffset, int storecount){
            super(element, unitoffset, unitsize, unitsubcount);

            this.storeoffset = storeoffset;
            this.storecount = storecount;
        }

        public ElementStoreSubset(int element, int storeoffset, int storecount){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);

            this.storeoffset = storeoffset;
            this.storecount = storecount;
        }

        public ElementStoreSubset(int element){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);

            this.storeoffset = 0;
            this.storecount = -1;
        }

        protected int calculateNeededSize(FSInstance instance){
            VLListType<FSElement<?, ?>> store = instance.store.get(element);
            int total = 0;
            int size = storeoffset + (storecount < 0 ? store.size() - storeoffset : storecount);

            for(int i = storeoffset; i < size; i++){
                total += store.get(i).size();
            }

            return total;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storeoffset + (storecount < 0 ? list.size() - storeoffset : storecount);

            target.allocateBinding(element, storecount, storecount);

            for(int i = storeoffset; i < size; i++){
                FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)list.get(i);
                item.buffer(vbuffer, buffer);

                target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
                target.bufferComplete(instance, instanceindex, element, i);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storeoffset + (storecount < 0 ? list.size() - storeoffset : storecount);

            target.allocateBinding(element, storecount, storecount);

            for(int i = storeoffset; i < size; i++){
                FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)list.get(i);
                item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

                target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
                target.bufferComplete(instance, instanceindex, element, i);
            }
        }

        public void checkForInterleavingErrors(FSInstance instance, int unitcountrequired, VLLog log){
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storeoffset + (storecount < 0 ? list.size() - storeoffset : storecount);

            for(int i = storeoffset; i < size; i++){
                int size2 = list.get(i).size() / unitsize;

                if(size2 != unitcountrequired){
                    log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes]    element[");
                    log.append(FSGlobal.NAMES[element]);
                    log.append("] referenceSize[");
                    log.append(unitcountrequired);
                    log.append("] actualSize[");
                    log.append(size2);
                    log.append("] instance[");
                    log.append(instance.name());
                    log.append("] storeIndex[");
                    log.append(i);
                    log.append("]");

                    log.printError();

                    throw new RuntimeException("[Target unit count mismatch]");
                }
            }
        }

        @Override
        public void debugInfo(VLLog log){
            super.debugInfo(log);

            log.append(" storeOffset[");
            log.append(storeoffset);
            log.append("] storeCount[");
            log.append(storecount < 0 ? "MAX" : storecount);
            log.append("]");
        }
    }

    public static class ElementActive<BUFFER extends VLBuffer<?, ?>> extends ElementType<BUFFER>{

        public ElementActive(int element, int unitoffset, int unitsize, int unitsubcount, int instanceoffset, int instancecount){
            super(element, unitoffset, unitsize, unitsubcount, instanceoffset, instancecount);
        }

        public ElementActive(int element, int instanceoffset, int instancecount){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element], instanceoffset, instancecount);
        }

        public ElementActive(int element, int unitoffset, int unitsize, int unitsubcount){
            super(element, unitoffset, unitsize, unitsubcount);
        }

        public ElementActive(int element){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);
        }

        protected int calculateNeededSize(FSInstance instance){
            return instance.element(element).size();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, instanceindex, element, instance.store.active[element]);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, instanceindex, element, instance.store.active[element]);
        }

        public void checkForInterleavingErrors(FSInstance instance, int unitcountrequired, VLLog log){
            int size = instance.element(element).size() / unitsize;

            if(size != unitcountrequired){
                log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes] element[");
                log.append(FSGlobal.NAMES[element]);
                log.append("] referenceSize[");
                log.append(unitcountrequired);
                log.append("] actualSize[");
                log.append(size);
                log.append("] instance[");
                log.append(instance.name());
                log.append("] storeIndex[");
                log.append(instance.store.active[element]);
                log.append("]");

                log.printError();

                throw new RuntimeException("[Target unit count mismatch]");
            }
        }
    }
}
