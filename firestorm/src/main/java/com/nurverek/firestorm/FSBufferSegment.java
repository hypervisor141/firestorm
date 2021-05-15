package com.nurverek.firestorm;

import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;
import vanguard.VLLoggable;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    private static final int BUFFER_PRINT_LIMIT = 100;

    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    protected VLListType<EntryType<BUFFER>> entries;

    protected int totalstride;
    protected int instanceoffset;
    protected int instancecount;;

    protected boolean interleaved;
    protected boolean uploaded;

    private boolean debuggedsegmentstructure;

    public FSBufferSegment(FSVertexBuffer<BUFFER> vbuffer, boolean interleaved, int capacity){
        initialize(vbuffer, vbuffer.provider(), interleaved, 0, -1, capacity);
    }

    public FSBufferSegment(BUFFER buffer, boolean interleaved, int capacity){
        initialize(null, buffer, interleaved, 0, -1, capacity);
    }

    public FSBufferSegment(FSVertexBuffer<BUFFER> vbuffer, boolean interleaved, int instanceoffset, int instancecount, int capacity){
        initialize(vbuffer, vbuffer.provider(), interleaved, instanceoffset, instancecount, capacity);
    }

    public FSBufferSegment(BUFFER buffer, boolean interleaved, int instanceoffset, int instancecount, int capacity){
        initialize(null, buffer, interleaved, instanceoffset, instancecount, capacity);
    }

    private void initialize(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, boolean interleaved, int instanceoffset, int instancecount, int capacity){
        this.interleaved = interleaved;
        this.vbuffer = vbuffer;
        this.buffer = buffer;
        this.instanceoffset = instanceoffset;
        this.instancecount = instancecount;

        entries = new VLListType<>(capacity, capacity / 2);

        debuggedsegmentstructure = false;
        uploaded = false;
    }

    public void prepare(FSMesh target){
        int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);
        int size2 = entries.size();

        for(int i = 0; i < size; i++){
            FSInstance instance = target.get(i);

            for(int i2 = 0; i2 < size2; i2++){
                buffer.adjustPreInitCapacity(entries.get(i2).calculateNeededSize(instance));
            }
        }
    }

    public FSBufferSegment<BUFFER> add(EntryType<BUFFER> entry){
        totalstride += entry.unitSizeOnBuffer();
        entries.add(entry);

        return this;
    }

    private void checkInitialize(){
        if(buffer.provider() == null){
            buffer.initialize(ByteOrder.nativeOrder());

            if(vbuffer != null && vbuffer.getBufferID() < 0){
                vbuffer.initialize();
            }
        }
    }

    public void buffer(FSMesh target){
        checkInitialize();

        int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

        if(interleaved){
            int size2 = entries.size() - 1;

            for(int i = 0; i < size; i++){
                FSInstance instance = target.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    int initialoffset = buffer.position();

                    EntryType<BUFFER> entry = entries.get(i2);
                    entry.bufferInterleaved(target, instance, buffer, vbuffer, totalstride);

                    buffer.position(initialoffset + entry.unitSizeOnBuffer());
                }

                entries.get(size2).bufferInterleaved(target, instance, buffer, vbuffer, totalstride);
            }

        }else{
            int size2 = entries.size();

            for(int i = 0; i < size; i++){
                FSInstance instance = target.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    entries.get(i2).bufferSequential(target, instance, buffer, vbuffer, totalstride);
                }
            }
        }
    }

    public void bufferDebug(FSMesh target, VLLog log){
        int entrysize = entries.size();

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
                if(entrysize <= 1){
                    log.append("[Segment is set to interleaved mode but there is less than 1 entry]");
                    log.printError();

                    throw new RuntimeException();
                }
            }

            debuggedsegmentstructure = true;
        }

        if(interleaved){
            log.append("[Checking for mismatch between buffer target unit counts for interleaving]\n");
            int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

            for(int i = 0; i < entrysize; i++){
                EntryType<BUFFER> entry = entries.get(i);
                int unitcountrequired = entry.calculateInterleaveDebugUnitCount(target, target.first());

                for(int i2 = 0; i2 < size; i2++){
                    entry.checkForInterleavingErrors(target, target.get(i2), unitcountrequired, log);
                    log.append("\n");
                }
            }

            log.printInfo();
        }

        log.append("[Entries]\n");

        for(int i = 0; i < entrysize; i++){
            entries.get(i).log(log, null);
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

    public interface EntryType<BUFFER extends VLBuffer<?, ?>> extends VLLoggable{

        int unitSizeOnBuffer();
        int calculateNeededSize(FSInstance instance);
        int calculateInterleaveDebugUnitCount(FSMesh target, FSInstance instance);
        void bufferSequential(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void bufferInterleaved(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void checkForInterleavingErrors(FSMesh target, FSInstance instance, int unitcountrequired, VLLog log);
    }

    public static abstract class ElementType<BUFFER extends VLBuffer<?, ?>> implements EntryType<BUFFER>{

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;

        public ElementType(int element, int unitoffset, int unitsize, int unitsubcount){
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
        }

        public ElementType(int element){
            this.element = element;
        }

        @Override
        public int unitSizeOnBuffer(){
            return unitsubcount;
        }

        @Override
        public int calculateInterleaveDebugUnitCount(FSMesh target, FSInstance instance){
            return calculateNeededSize(instance) / unitsubcount;
        }

        @Override
        public void log(VLLog log, Object data){
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
            log.append("]");
        }
    }

    public static class ElementStore<BUFFER extends VLBuffer<?, ?>> extends ElementType<BUFFER>{

        public int storeindex;

        public ElementStore(int element, int unitoffset, int unitsize, int unitsubcount, int storeindex){
            super(element, unitoffset, unitsize, unitsubcount);
            this.storeindex = storeindex;
        }

        public ElementStore(int element, int storeindex, int storecount){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);
            this.storeindex = storeindex;
        }

        public ElementStore(int element){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);
            this.storeindex = 0;
        }

        @Override
        public int calculateNeededSize(FSInstance instance){
            return (instance.store.get(element).get(storeindex).size() / unitsize) * unitsubcount;
        }

        @Override
        public void bufferSequential(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.store.get(element).get(storeindex);
            item.buffer(vbuffer, buffer);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, element, storeindex);
        }

        @Override
        public void bufferInterleaved(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.store.get(element).get(storeindex);
            item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, element, storeindex);
        }

        @Override
        public void checkForInterleavingErrors(FSMesh target, FSInstance instance, int unitcountrequired, VLLog log){
            int size2 = instance.store.get(element).get(storeindex).size() / unitsize;

            if(size2 != unitcountrequired){
                log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes] element[");
                log.append(FSGlobal.NAMES[element]);
                log.append("] referenceSize[");
                log.append(unitcountrequired);
                log.append("] actualSize[");
                log.append(size2);
                log.append("] instance[");
                log.append(instance.name());
                log.append("] storeIndex[");
                log.append(storeindex);
                log.append("]");

                log.printError();

                throw new RuntimeException("[Target unit count mismatch]");
            }
        }

        @Override
        public void log(VLLog log, Object data){
            super.log(log, data);

            log.append(" storeIndex[");
            log.append(storeindex);
            log.append("]");
        }
    }

    public static class ElementActive<BUFFER extends VLBuffer<?, ?>> extends ElementType<BUFFER>{

        public ElementActive(int element, int unitoffset, int unitsize, int unitsubcount){
            super(element, unitoffset, unitsize, unitsubcount);
        }

        public ElementActive(int element){
            super(element, 0, FSGlobal.UNIT_SIZES[element], FSGlobal.UNIT_SIZES[element]);
        }

        @Override
        public int calculateNeededSize(FSInstance instance){
            return (instance.element(element).size() / unitsize) * unitsubcount;
        }

        @Override
        public void bufferSequential(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, element, instance.store.active[element]);
        }

        @Override
        public void bufferInterleaved(FSMesh target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            target.bufferComplete(instance, element, instance.store.active[element]);
        }

        @Override
        public void checkForInterleavingErrors(FSMesh target, FSInstance instance, int unitcountrequired, VLLog log){
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
