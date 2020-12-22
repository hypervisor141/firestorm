package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;

public final class FSBufferLayout{

    private static final int BUFFER_PRINT_LIMIT = 100;

    public static final Mechanism ELEMENT_SEQUENTIAL_INSTANCED = new ElementSequentialInstanced();
    public static final Mechanism ELEMENT_INTERLEAVED_INSTANCED = new ElementInterleavedInstanced();

    public static final Mechanism ELEMENT_SEQUENTIAL_SINGULAR = new ElementSequentialSingular();
    public static final Mechanism ELEMENT_INTERLEAVED_SINGULAR = new ElementInterleavedSingular();

    public static final Mechanism ELEMENT_SEQUENTIAL_INDICES = new ElementSequentialIndices();

    public static final Mechanism LINK_SEQUENTIAL_SINGULAR = new LinkSequentialSingular();
    public static final Mechanism LINK_INTERLEAVED_SINGULAR = new LinkInterleavedSingular();

    protected VLListType<Layout> layouts;
    protected FSGAssembler assembler;
    protected FSMesh targetmesh;

    public FSBufferLayout(FSMesh mesh, FSGAssembler assembler){
        this.targetmesh = mesh;
        this.assembler = assembler;

        layouts = new VLListType<>(FSG.ELEMENT_TOTAL_COUNT, FSG.ELEMENT_TOTAL_COUNT);
    }

    public Layout add(FSBufferManager buffer, int bufferindex, int capacity){
        Layout layout = new Layout(buffer, bufferindex, capacity);
        layouts.add(layout);

        return layout;
    }

    public void buffer(){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).buffer();
        }
    }

    public void bufferDebug(FSGScanner scanner){
        int size = layouts.size();

        VLDebug.append("BufferLayout[");
        VLDebug.append(targetmesh.name);
        VLDebug.append("]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("Layout[");
            VLDebug.append(i);
            VLDebug.append("] ");
            VLDebug.printD();

            try{
                layouts.get(i).bufferDebug(scanner);

            }catch(Exception ex){
                VLDebug.append(" [FAILED]\n");
                throw ex;
            }

            VLDebug.append(" [SUCCESS]\n");
        }

        VLDebug.append("\n");
    }


    public static abstract class EntryType{

        public Mechanism mechanism;

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;
        public int stride;

        public EntryType(Mechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount, int stride){
            this.mechanism = mechanism;
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
            this.stride = stride;
        }

        public int strideAdjustment(){
            return stride;
        }

        public int bufferSizeAdjustment(FSMesh mesh){
            return (mechanism.getTargetSize(this, mesh) / unitsize) * stride;
        }

        public void debugInfo(){
            VLDebug.append("[");
            VLDebug.append(getClass().getSimpleName());
            VLDebug.append("] element[");
            VLDebug.append(element);
            VLDebug.append("] mechanism[");
            VLDebug.append(mechanism.getClass().getSimpleName());
            VLDebug.append("] unitOffset[");
            VLDebug.append(unitoffset);
            VLDebug.append("] unitSize[");
            VLDebug.append(unitsize);
            VLDebug.append("] unitSubCount[");
            VLDebug.append(unitsubcount);
            VLDebug.append("]]");
        }
    }

    public static class EntryElement extends EntryType{

        public EntryElement(Mechanism<EntryElement> mechanism, int element, int unitoffset, int unitsize, int unitsubcount, int stride){
            super(mechanism, element, unitoffset, unitsize, unitsubcount, stride);
        }

        public EntryElement(Mechanism<EntryElement> mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            super(mechanism, element, unitoffset, unitsize, unitsubcount, unitsubcount);
        }

        public EntryElement(Mechanism<EntryElement> mechanism, int element){
            super(mechanism, element, 0, FSG.UNIT_SIZES[element], FSG.UNIT_SIZES[element], FSG.UNIT_SIZES[element]);
        }
    }

    public static class EntryLink extends EntryType{

        public EntryLink(Mechanism<EntryElement> mechanism, int linkindex, int unitoffset, int unitsize, int unitsubcount, int stride){
            super(mechanism, linkindex, unitoffset, unitsize, unitsubcount, stride);
        }
    }

    public final class Layout{

        protected VLListType<EntryType> entries;
        protected FSBufferManager buffer;

        protected int bufferindex;
        protected int totalstride;

        private Layout(FSBufferManager buffer, int bufferindex, int capacity){
            this.buffer = buffer;
            this.bufferindex = bufferindex;

            entries = new VLListType<>(capacity, capacity / 2);
        }

        public Layout addElement(EntryElement entry){
            totalstride += entry.strideAdjustment();
            entries.add(entry);

            buffer.adjustCapacity(bufferindex, entry.bufferSizeAdjustment(targetmesh));

            return this;
        }

        public Layout addLink(EntryLink entry){
            totalstride += entry.strideAdjustment();
            entries.add(entry);

            buffer.adjustCapacity(bufferindex, entry.bufferSizeAdjustment(targetmesh));

            return this;
        }

        protected void buffer(){
            int size = entries.size();
            VLBuffer b = buffer.get(bufferindex).buffer();
            EntryType entry;

            for(int i = 0; i < size - 1; i++){
                entry = entries.get(i);
                b.position(entry.mechanism.buffer(assembler, targetmesh, entry, buffer, bufferindex, totalstride));
            }

            entry = entries.get(entries.size() - 1);
            entry.mechanism.buffer(assembler, targetmesh, entry, buffer, bufferindex, totalstride);
        }

        protected void bufferDebug(FSGScanner scanner){
            int size = entries.size();
            EntryType e;

            if(totalstride <= 0){
                VLDebug.append("Invalid stride[");
                VLDebug.append(totalstride);
                VLDebug.append("]");

                throw new RuntimeException();
            }

            VLDebug.append("bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] stride[");
            VLDebug.append(totalstride);
            VLDebug.append("] entries[");

            for(int i = 0; i < size; i++){
                e = entries.get(i);
                e.debugInfo();

                if(i < size - 1){
                    VLDebug.append(", ");
                }
            }

            VLDebug.append("] ");

            buffer.get(bufferindex).buffer().stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            buffer();
        }
    }

    public abstract static interface Mechanism<ENTRY extends EntryType>{

        public abstract int buffer(FSGAssembler assembler, FSMesh mesh, ENTRY entry, FSBufferManager buffer, int bufferindex, int stride);

        public abstract int getTargetSize(ENTRY entry, FSMesh mesh);
    }

    private static final class ElementSequentialInstanced implements Mechanism<EntryType>{

        private ElementSequentialInstanced(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            FSBufferAddress address;

            FSGAssembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                address = new FSBufferAddress();
                step.process(address, buffer, bufferindex, array, 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instance.bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }

        public int getTargetSize(EntryType entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.instance(i).element(entry.element).size();
            }

            return total;
        }
    }

    private static final class ElementInterleavedInstanced implements Mechanism<EntryType>{

        private ElementInterleavedInstanced(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            FSGAssembler.BufferStep step = assembler.bufferFunc(element);
            FSBufferAddress address;

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                address = new FSBufferAddress();
                step.process(address, buffer, bufferindex, array, 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);
                instance.bufferTracker().add(element, address);
            }

            return mainoffset + entry.unitsubcount;
        }

        public int getTargetSize(EntryType entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.instance(i).element(entry.element).size();
            }

            return total;
        }
    }

    private static final class ElementSequentialSingular implements Mechanism<EntryType>{

        private ElementSequentialSingular(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            FSBufferAddress address;

            instance = instances.get(0);
            array = instance.element(element);

            for(int i = 0; i < size; i++){
                address = new FSBufferAddress();
                assembler.bufferFunc(element).process(address, buffer, bufferindex, array, 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);
                instances.get(i).bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }

        public int getTargetSize(EntryType entry, FSMesh mesh){
            return mesh.instance(0).element(entry.element).size();
        }
    }

    private static final class ElementInterleavedSingular implements Mechanism<EntryType>{

        private ElementInterleavedSingular(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            instance = instances.get(0);
            array = instance.element(element);

            FSBufferAddress address = new FSBufferAddress();
            assembler.bufferFunc(element).process(address, buffer, bufferindex, array,
                    0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            for(int i = 0; i < size; i++){
                instances.get(i).bufferTracker().add(element, address);
            }

            return mainoffset + entry.unitsubcount;
        }

        public int getTargetSize(EntryType entry, FSMesh mesh){
            return mesh.instance(0).element(entry.element).size();
        }
    }

    private static final class ElementSequentialIndices implements Mechanism<EntryType>{

        private ElementSequentialIndices(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            int element = entry.element;

            FSBufferAddress address = new FSBufferAddress();
            assembler.bufferFunc(element).process(address, buffer, bufferindex, mesh.indices, 0,
                    mesh.indices.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                mesh.instance(i).bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }

        public int getTargetSize(EntryType entry, FSMesh mesh){
            return mesh.indices().size();
        }
    }

    private static final class LinkSequentialSingular implements Mechanism<EntryType>{

        private LinkSequentialSingular(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryType entry, FSBufferManager buffer, int bufferindex, int stride){
            FSLinkBufferedType link = (FSLinkBufferedType)mesh.link(entry.element);
            link.buffer(buffer, bufferindex, 0, link.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return buffer.position(bufferindex);
        }

        @Override
        public int getTargetSize(EntryType entry, FSMesh mesh){
            return (((FSLinkBufferedType)mesh.link(entry.element)).size() / entry.unitsize) * entry.unitsubcount;
        }
    }

    private static final class LinkInterleavedSingular implements Mechanism<EntryLink>{

        private LinkInterleavedSingular(){}

        @Override
        public int buffer(FSGAssembler assembler, FSMesh mesh, EntryLink entry, FSBufferManager buffer, int bufferindex, int stride){
            int firstpos = buffer.position(bufferindex);
            FSLinkBufferedType link = (FSLinkBufferedType)mesh.link(entry.element);

            link.buffer(buffer, bufferindex, 0, link.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return firstpos + entry.unitsubcount;
        }

        @Override
        public int getTargetSize(EntryLink entry, FSMesh mesh){
            return ((FSLinkBufferedType)mesh.link(entry.element)).size();
        }
    }
}
