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
    public static final Mechanism LINK_INTERLEAVED_SINGULAR= new LinkInterleavedSingular();

    protected VLListType<Layout> layouts;
    protected FSG.Assembler assembler;
    protected FSMesh targetmesh;

    public FSBufferLayout(FSMesh mesh, FSG.Assembler assembler){
        this.targetmesh = mesh;
        this.assembler = assembler;

        layouts = new VLListType<>(FSG.ELEMENT_TOTAL_COUNT, FSG.ELEMENT_TOTAL_COUNT);
    }

    public Layout add(FSBufferManager buffer, int bufferindex, int capacity){
        Layout layout = new Layout(buffer, bufferindex, capacity);
        layouts.add(layout);

        return layout;
    }

    protected void adjustCapacity(int element, int count){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).adjustCapacity(element, count);
        }
    }

    protected void adjustCapacityForLinks(){
        int size = layouts.size();
        int size2 = targetmesh.sizeLinks();

        Layout layout;

        for(int i = 0; i < size; i++){
            layout = layouts.get(i);

            for(int i2 = 0; i2 < size2; i2++){
                layout.adjustCapacity(i2, targetmesh.link(i2).size());
            }
        }
    }

    public void buffer(){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).buffer();
        }
    }

    public void bufferDebug(FSG.Scanner scanner){
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

        public EntryType(Mechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            this.mechanism = mechanism;
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
        }

        public int strideAdjustment(){
            return unitsubcount;
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

    public static class EntryBasic extends EntryType{

        public EntryBasic(Mechanism<EntryBasic> mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            super(mechanism, element, unitoffset, unitsize, unitsubcount);
        }

        public EntryBasic(Mechanism<EntryBasic> mechanism, int element){
            super(mechanism, element, 0, FSG.UNIT_SIZES[element], FSG.UNIT_SIZES[element]);
        }
    }

    public final class Layout{

        protected VLListType<EntryType> entries;
        protected FSBufferManager buffer;

        protected int bufferindex;
        protected int stride;

        private Layout(FSBufferManager buffer, int bufferindex, int capacity){
            this.buffer = buffer;
            this.bufferindex = bufferindex;

            entries = new VLListType<>(capacity, capacity / 2);
        }

        public Layout add(EntryType entry){
            stride += entry.strideAdjustment();
            entries.add(entry);

            return this;
        }

        protected void adjustCapacity(int element, int count){
            int size = entries.size();
            int total = 0;

            EntryType e;

            for(int i = 0; i < size; i++){
                e = entries.get(i);

                if(e.element == element){
                    total += (count / e.unitsize) * e.unitsubcount;
                }
            }

            buffer.adjustCapacity(bufferindex, total);
        }

        protected void buffer(){
            int size = entries.size();
            VLBuffer b = buffer.get(bufferindex).buffer();
            EntryType entry;

            for(int i = 0; i < size - 1; i++){
                entry = entries.get(i);
                b.position(entry.mechanism.buffer(assembler, targetmesh, entry, buffer, bufferindex, stride));
            }

            entry = entries.get(entries.size() - 1);
            entry.mechanism.buffer(assembler, targetmesh, entry, buffer, bufferindex, stride);
        }

        protected void bufferDebug(FSG.Scanner scanner){
            int size = entries.size();
            EntryType e;

            if(stride <= 0){
                VLDebug.append("Invalid stride[");
                VLDebug.append(stride);
                VLDebug.append("]");

                throw new RuntimeException();
            }

            VLDebug.append("bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] stride[");
            VLDebug.append(stride);
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

        public abstract int buffer(FSG.Assembler assembler, FSMesh mesh, ENTRY entry, FSBufferManager buffer, int bufferindex, int stride);
    }

    private static final class ElementSequentialInstanced implements Mechanism<EntryBasic>{

        private ElementSequentialInstanced(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            FSBufferAddress address;

            FSG.Assembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                address = new FSBufferAddress();
                step.process(address, buffer, bufferindex, array, entry.unitsubcount, stride);

                instance.bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ElementInterleavedInstanced implements Mechanism<EntryBasic>{

        private ElementInterleavedInstanced(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            FSG.Assembler.BufferStep step = assembler.bufferFunc(element);
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
    }

    private static final class ElementSequentialSingular implements Mechanism<EntryBasic>{

        private ElementSequentialSingular(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
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
                assembler.bufferFunc(element).process(address, buffer, bufferindex, array, entry.unitsubcount, stride);
                instances.get(i).bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ElementInterleavedSingular implements Mechanism<EntryBasic>{

        private ElementInterleavedSingular(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
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
    }

    private static final class ElementSequentialIndices implements Mechanism<EntryBasic>{

        private ElementSequentialIndices(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
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
    }

    private static final class LinkSequentialSingular implements Mechanism<EntryBasic>{

        private LinkSequentialSingular(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            mesh.link(entry.element).buffer(buffer, bufferindex, entry.unitoffset, entry.unitsubcount);
            return buffer.position(bufferindex);
        }
    }

    private static final class LinkInterleavedSingular implements Mechanism<EntryBasic>{

        private LinkInterleavedSingular(){}

        @Override
        public int buffer(FSG.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            int firstpos = buffer.position(bufferindex);
            FSConfigLink link = mesh.link(entry.element);
            link.buffer(buffer, bufferindex, 0, link.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return firstpos + entry.unitsubcount;
        }
    }
}
