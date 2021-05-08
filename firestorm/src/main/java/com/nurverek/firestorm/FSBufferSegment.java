package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    private static final int BUFFER_PRINT_LIMIT = 100;

    protected VLListType<EntryType<BUFFER>> entries;

    protected int totalstride;
    protected boolean interleaved;

    private boolean debuggedsegmentstructure;

    public FSBufferSegment(boolean interleaved, int capacity){
        this.interleaved = interleaved;

        debuggedsegmentstructure = false;
        entries = new VLListType<>(capacity, capacity / 2);
    }

    public void accountFor(FSMesh target, BUFFER buffer){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            buffer.adjustPreInitCapacity(entries.get(i).calculateNeededSize(target));
        }
    }

    public FSBufferSegment<BUFFER> add(EntryType<BUFFER> entry){
        totalstride += entry.practicalUnitSize();
        entries.add(entry);

        return this;
    }

    protected void buffer(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer){
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

    protected void bufferDebug(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, VLLog log){
        int size = entries.size();
        EntryType<BUFFER> entry;

        if(!debuggedsegmentstructure){
            if(totalstride <= 0){
                log.append("[Invalid stride value] stride[");
                log.append(totalstride);
                log.append("]");

                throw new RuntimeException();
            }
            if(interleaved){
                if(size <= 1){
                    log.append("[Segment is set to interleaved mode but there is less than 1 entry]");
                    throw new RuntimeException();
                }
            }

            debuggedsegmentstructure = true;
        }

        if(interleaved){
            entry = entries.get(0);
            int unitsizetomatch = entry.getInterleaveDebugunitsizetomatch(target);

            log.append("[Checking for mismatch between buffer target unit counts for interleaving] entry[");
            log.append(entry.getClass().getSimpleName());
            log.append("]");

            for(int i = 0; i < size; i++){
                entry = entries.get(i);
                entry.checkForInterleavingErrors(target, unitsizetomatch, log);
            }
        }

        log.append("stride[");
        log.append(totalstride);
        log.append("] entries[");

        for(int i = 0; i < size; i++){
            entries.get(i).debugInfo(log);

            if(i < size - 1){
                log.append(", ");
            }
        }

        log.append("] ");

        buffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
        buffer(target, buffer, vbuffer);
    }

    public interface EntryType<BUFFER extends VLBuffer<?, ?>>{

        int calculateNeededSize(FSMesh target);
        int getInterleaveDebugunitsizetomatch(FSMesh target);
        void bufferSequential(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void bufferInterleaved(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        int practicalUnitSize();
        void checkForInterleavingErrors(FSMesh target, int unitsizetomatch, VLLog log);
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
            int size = instancecount < 0 ? target.size() - instanceoffset : instanceoffset + instancecount;

            for(int i = instanceoffset; i < size; i++){
                total += calculateNeededSize(target.get(i));
            }

            return (total / unitsize) * unitsubcount;
        }

        protected abstract int calculateNeededSize(FSInstance instance);

        @Override
        public int getInterleaveDebugunitsizetomatch(FSMesh target){
            return calculateNeededSize(target.first()) / unitsize;
        }

        @Override
        public void bufferSequential(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            int size = instancecount < 0 ? target.size() - instanceoffset : instanceoffset + instancecount;

            for(int i = instanceoffset; i < size; i++){
                bufferSequential(target, i, buffer, vbuffer, stride);
            }
        }

        @Override
        public void bufferInterleaved(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            int size = instancecount < 0 ? target.size() - instanceoffset : instanceoffset + instancecount;

            for(int i = instanceoffset; i < size; i++){
                bufferInterleaved(target, i, buffer, vbuffer, stride);
            }
        }

        public abstract void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        public abstract void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);

        @Override
        public void checkForInterleavingErrors(FSMesh target, int unitsizetomatch, VLLog log){
            int size = instancecount < 0 ? target.size() - instanceoffset : instanceoffset + instancecount;

            for(int i = instanceoffset; i < size; i++){
                checkForInterleavingErrors(target.get(i), unitsizetomatch, log);
            }
        }

        public abstract void checkForInterleavingErrors(FSInstance instance, int unitsizetomatch, VLLog log);

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
            int size = storecount < 0 ? store.size() - storeoffset : storeoffset + storecount;

            for(int i = storeoffset; i < size; i++){
                total += store.get(i).size();
            }

            return total;
        }

        @Override
        public void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storecount < 0 ? list.size() - storeoffset : storeoffset + storecount;

            for(int i = storeoffset; i < size; i++){
                ((FSElement<?, BUFFER>)list.get(i)).buffer(vbuffer, buffer);
                target.bufferComplete(instance, instanceindex, element, i);
            }
        }

        @Override
        public void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storecount < 0 ? list.size() - storeoffset : storeoffset + storecount;

            for(int i = storeoffset; i < size; i++){
                ((FSElement<?, BUFFER>)list.get(i)).buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);
                target.bufferComplete(instance, instanceindex, element, i);
            }
        }

        public void checkForInterleavingErrors(FSInstance instance, int unitsizetomatch, VLLog log){
            VLListType<FSElement<?, ?>> list = instance.store.get(element);
            int size = storecount < 0 ? list.size() - storeoffset : storeoffset + storecount;

            for(int i = storeoffset; i < size; i++){
                if(list.get(i).size() != unitsizetomatch){
                    log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes]");
                    log.append("target[");
                    log.append(unitsizetomatch);
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
        public void bufferSequential(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            ((FSElement<?, BUFFER>)instance.element(element)).buffer(vbuffer, buffer);
            target.bufferComplete(instance, instanceindex, element, instance.store.active[element]);
        }

        @Override
        public void bufferInterleaved(FSMesh target, int instanceindex, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSInstance instance = target.get(instanceindex);
            ((FSElement<?, BUFFER>)instance.element(element)).buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);
            target.bufferComplete(instance, instanceindex, element, instance.store.active[element]);
        }

        public void checkForInterleavingErrors(FSInstance instance, int unitsizetomatch, VLLog log){
            if(instance.store.get(element).size() != unitsizetomatch){
                log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes]");
                log.append("target[");
                log.append(unitsizetomatch);
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
