package com.nurverek.firestorm;

import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;
import vanguard.VLLoggable;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    protected VLListType<EntryType<BUFFER>> entries;

    protected int totalstride;
    protected int instanceoffset;
    protected int instancecount;

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

    public void prepare(FSMesh<?> target){
        int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);
        int size2 = entries.size();

        for(int i = 0; i < size; i++){
            FSInstance instance = target.get(i);

            for(int i2 = 0; i2 < size2; i2++){
                buffer.adjustPreInitCapacity(entries.get(i2).calculateNeededSize(instance));
            }
        }
    }

    public void prepareDebug(FSMesh<?> target, VLLog log){
        int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);
        int size2 = entries.size();

        log.addTag("Segment");

        for(int i = 0; i < size; i++){
            FSInstance instance = target.get(i);

            log.addTag(instance.name);
            log.addTag(String.valueOf(i));

            for(int i2 = 0; i2 < size2; i2++){
                log.addTag("Entry");
                log.addTag(String.valueOf(i2));

                try{
                    buffer.adjustPreInitCapacity(entries.get(i2).calculateNeededSize(instance));
                    log.append("[SUCCESS]\n");
                    log.printInfo();

                }catch(Exception ex){
                    log.append("[FAILED]\n");
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.removeLastTag();
                log.removeLastTag();
            }

            log.removeLastTag();
            log.removeLastTag();
        }

        log.removeLastTag();
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

    public void buffer(FSMesh<?> target){
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

    public void bufferDebug(FSMesh<?> target, VLLog log){
        int entrysize = entries.size();

        log.append("stride[");
        log.append(totalstride);
        log.append("] entrySize[");
        log.append(entrysize);
        log.append("] instanceOffset[");
        log.append(instanceoffset);
        log.append("] instanceCount[");
        log.append(instancecount < 0 ? "MAX" : instancecount);
        log.append("]\n");
        log.printInfo();

        if(!debuggedsegmentstructure){
            if(totalstride <= 0){
                log.append("[Invalid stride value] stride[");
                log.append(totalstride);
                log.append("]");
                log.printError();

                throw new RuntimeException();
            }
            if(instanceoffset < 0){
                log.append("[Invalid instance offset] offset[");
                log.append(instanceoffset);
                log.append("]");
                log.printError();

                throw new RuntimeException();
            }
            if(instancecount == 0 || instancecount > target.size()){
                log.append("[Invalid Instance Count] count[");
                log.append(instancecount);
                log.append("] meshInstanceCount[");
                log.append(target.size());
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
                }
            }
        }

        checkInitialize();
        int size = instanceoffset + (instancecount < 0 ? target.size() - instanceoffset : instancecount);

        if(interleaved){
            int size2 = entries.size() - 1;
            EntryType<BUFFER> entry;

            for(int i = 0; i < size; i++){
                FSInstance instance = target.get(i);

                log.addTag(instance.name);
                log.addTag(String.valueOf(i));

                for(int i2 = 0; i2 < size2; i2++){
                    entry = entries.get(i2);
                    int initialoffset = buffer.position();

                    log.addTag("Entry");
                    log.addTag(String.valueOf(i2));

                    log.append(" bufferOffset[");
                    log.append(buffer.position());
                    log.append("] bufferCapacity[");
                    log.append(buffer.size());
                    log.append("]");

                    try{
                        entry.bufferInterleaved(target, instance, buffer, vbuffer, totalstride);
                        buffer.position(initialoffset + entry.unitSizeOnBuffer());

                        log.append(" bufferPosition[");
                        log.append(buffer.position());
                        log.append("] [SUCCESS]\n");
                        log.printInfo();

                    }catch(Exception ex){
                        log.append("[FAILED]\n");

                        entry.log(log, null);
                        log.printError();

                        throw new RuntimeException("Buffering failed.", ex);
                    }

                    log.removeLastTag();
                    log.removeLastTag();
                }

                entry = entries.get(size2);

                log.addTag("Entry");
                log.addTag(String.valueOf(size2));

                log.append(" bufferOffset[");
                log.append(buffer.position());
                log.append("] bufferCapacity[");
                log.append(buffer.size());
                log.append("]");

                try{
                    entry.bufferInterleaved(target, instance, buffer, vbuffer, totalstride);

                    log.append(" bufferPosition[");
                    log.append(buffer.position());
                    log.append("] [SUCCESS]\n");
                    log.printInfo();

                }catch(Exception ex){
                    log.append(" [FAILED]\n");

                    entry.log(log, null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.removeLastTag();
                log.removeLastTag();
                log.removeLastTag();
                log.removeLastTag();
            }

        }else{
            int size2 = entries.size();

            for(int i = 0; i < size; i++){
                FSInstance instance = target.get(i);

                log.addTag(instance.name);
                log.addTag(String.valueOf(i));

                for(int i2 = 0; i2 < size2; i2++){
                    EntryType<BUFFER> entry = entries.get(i2);

                    log.addTag("Entry");
                    log.addTag(String.valueOf(i2));

                    log.append(" bufferOffset[");
                    log.append(buffer.position());
                    log.append("] bufferCapacity[");
                    log.append(buffer.size());
                    log.append("]");

                    try{
                        entry.bufferSequential(target, instance, buffer, vbuffer, totalstride);

                        log.append(" bufferPosition[");
                        log.append(buffer.position());
                        log.append("] [SUCCESS]\n");
                        log.printInfo();

                    }catch(Exception ex){
                        log.append(" [FAILED]\n");
                        entry.log(log, null);
                        log.printError();

                        throw new RuntimeException(ex);
                    }

                    log.removeLastTag();
                    log.removeLastTag();
                }

                log.removeLastTag();
                log.removeLastTag();
            }
        }
    }

    private void bufferFunc(){

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
        int calculateInterleaveDebugUnitCount(FSMesh<?> target, FSInstance instance);
        void bufferSequential(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void bufferInterleaved(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride);
        void checkForInterleavingErrors(FSMesh<?> target, FSInstance instance, int unitcountrequired, VLLog log);
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
        public int calculateInterleaveDebugUnitCount(FSMesh<?> target, FSInstance instance){
            return calculateNeededSize(instance) / unitsubcount;
        }

        @Override
        public void log(VLLog log, Object data){
            log.append("[");
            log.append(getClass().getSimpleName());
            log.append("] element[");
            log.append(FSElementRegisry.NAMES[element]);
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
            super(element, 0, FSElementRegisry.UNIT_SIZES[element], FSElementRegisry.UNIT_SIZES[element]);
            this.storeindex = storeindex;
        }

        public ElementStore(int element){
            super(element, 0, FSElementRegisry.UNIT_SIZES[element], FSElementRegisry.UNIT_SIZES[element]);
            this.storeindex = 0;
        }

        @Override
        public int calculateNeededSize(FSInstance instance){
            return (instance.store.get(element).get(storeindex).size() / unitsize) * unitsubcount;
        }

        @Override
        public void bufferSequential(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.store.get(element).get(storeindex);
            item.buffer(vbuffer, buffer);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            instance.bufferComplete();
        }

        @Override
        public void bufferInterleaved(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.store.get(element).get(storeindex);
            item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            instance.bufferComplete();
        }

        @Override
        public void checkForInterleavingErrors(FSMesh<?> target, FSInstance instance, int unitcountrequired, VLLog log){
            int size2 = instance.store.get(element).get(storeindex).size() / unitsize;

            if(size2 != unitcountrequired){
                log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes] element[");
                log.append(FSElementRegisry.NAMES[element]);
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
            super(element, 0, FSElementRegisry.UNIT_SIZES[element], FSElementRegisry.UNIT_SIZES[element]);
        }

        @Override
        public int calculateNeededSize(FSInstance instance){
            return (instance.element(element).size() / unitsize) * unitsubcount;
        }

        @Override
        public void bufferSequential(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            instance.bufferComplete();
        }

        @Override
        public void bufferInterleaved(FSMesh<?> target, FSInstance instance, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride){
            target.allocateBinding(element, 1, 5);

            FSElement<?, BUFFER> item = (FSElement<?, BUFFER>)instance.element(element);
            item.buffer(vbuffer, buffer, unitoffset, unitsize, unitsubcount, stride);

            target.bindManual(element, item.bindings.get(item.bindings.size() - 1));
            instance.bufferComplete();
        }

        @Override
        public void checkForInterleavingErrors(FSMesh<?> target, FSInstance instance, int unitcountrequired, VLLog log){
            int size = instance.element(element).size() / unitsize;

            if(size != unitcountrequired){
                log.append("[FAILED] [Segment is on interleaving mode but there is a mismatch between target unit sizes] element[");
                log.append(FSElementRegisry.NAMES[element]);
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
