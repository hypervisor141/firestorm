package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLBufferDirect;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListInt;
import com.nurverek.vanguard.VLListType;

public class FSBufferLayout{

    private static final int BUFFER_PRINT_LIMIT = 20;
    private static final VLListInt DEBUG_CACHE = new VLListInt(0, 5);

    public static final Mechanism MECHANISM_SEQUENTIAL_INSTANCED = new SequentialMechanism();
    public static final Mechanism MECHANISM_COMPLEX_INSTANCED = new ComplexMechanism();
    public static final Mechanism MECHANISM_SEQUENTIAL_SINGULAR = new SequentialSingularMechanism();
    public static final Mechanism MECHANISM_COMPLEX_SINGULAR = new ComplexSingularMechanism();
    public static final Mechanism MECHANISM_INDICES_SINGULAR = new IndicesSingularMechanism();

    protected VLListType<Layout> layouts;
    protected FSLoader.Assembler assembler;
    protected FSMesh targetmesh;

    public FSBufferLayout(FSMesh mesh, FSLoader.Assembler assembler){
        this.targetmesh = mesh;
        this.assembler = assembler;

        layouts = new VLListType<>(FSLoader.ELEMENT_TOTAL_COUNT, FSLoader.ELEMENT_TOTAL_COUNT);
    }
    

    public Layout add(FSBufferManager buffer, int bufferindex, Mechanism mechanism){
        Layout layout = new Layout(buffer, bufferindex, mechanism);
        layouts.add(layout);

        return layout;
    }

    public void increaseTargetCapacities(int element, int totalsize){
        int size = layouts.size();
        int unitcount = totalsize / FSLoader.UNIT_SIZES[element];

        for(int i = 0; i < size; i++){
            layouts.get(i).increaseTargetCapacities(element, unitcount);
        }
    }

    public void buffer(){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).buffer();
        }
    }

    public void bufferDebug(FSLoader.Scanner scanner){
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



    protected static final class Entry{

        protected int element;
        protected int unitoffset;
        protected int unitsize;

        public Entry(int element, int unitoffset, int unitsize){
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
        }

        public void debugInfo(){
            VLDebug.append(FSLoader.ELEMENT_NAMES[element]);
            VLDebug.append("[unitOffset[");
            VLDebug.append(unitoffset);
            VLDebug.append("] unitsize[");
            VLDebug.append(unitsize);
            VLDebug.append("]]");
        }
    }

    public final class Layout{

        protected VLListType<Entry> entries;

        protected FSBufferManager buffer;
        protected Mechanism mechanism;

        protected int bufferindex;
        protected int stride;

        protected Layout(FSBufferManager buffer, int bufferindex, Mechanism mechanism){
            this.mechanism = mechanism;
            this.buffer = buffer;
            this.bufferindex = bufferindex;

            entries = new VLListType<>(FSLoader.ELEMENT_TOTAL_COUNT, FSLoader.ELEMENT_TOTAL_COUNT);
        }


        public Layout add(int element){
            return add(element, 0, FSLoader.UNIT_SIZES[element]);
        }

        public Layout add(int element, int unitoffset, int unitsize){
            stride += unitsize;
            entries.add(new Entry(element, unitoffset, unitsize));

            return this;
        }

        protected void increaseTargetCapacities(int element, int unitcount){
            int size = entries.size();
            int total = 0;

            Entry e;

            for(int i = 0; i < size; i++){
                e = entries.get(i);

                if(e.element == element){
                    total += unitcount * e.unitsize;
                }
            }

            buffer.increaseTargetCapacity(bufferindex, total);
        }

        protected void buffer(){
            int size = entries.size();
            VLBufferDirect b = buffer.get(bufferindex).provider();

            for(int i = 0; i < size - 1; i++){
                b.position(mechanism.buffer(assembler, targetmesh, entries.get(i), buffer, bufferindex, stride));
            }

            mechanism.buffer(assembler, targetmesh, entries.get(entries.size() - 1), buffer, bufferindex, stride);
        }

        protected void bufferDebug(FSLoader.Scanner scanner){
            if(scanner instanceof FSLoader.ScannerSingular && mechanism instanceof MechanismInstancedBase){
                VLDebug.append("[WARNING] [USING INSTANCED BUFFER MECHANISM FOR A SINGULAR SCANNER]");
                VLDebug.printE();

            }else if(scanner instanceof FSLoader.ScannerInstanced && mechanism instanceof MechanismSingularBase){
                VLDebug.append("[WARNING] [USING SINGULAR BUFFER MECHANISM FOR AN INSTANCED SCANNER]");
                VLDebug.printE();
            }

            int size = entries.size();
            Entry e;

            DEBUG_CACHE.virtualSize(0);

            for(int i = 0; i < size; i++){
                e = entries.get(i);

                if((e.unitoffset + e.unitsize) > FSLoader.UNIT_SIZES[e.element]){
                    VLDebug.append("Invalid unitoffset[");
                    VLDebug.append(e.unitoffset);
                    VLDebug.append("] and/or unitsize[");
                    VLDebug.append(e.unitsize);
                    VLDebug.append("] for [");
                    VLDebug.append(FSLoader.ELEMENT_NAMES[e.element]);
                    VLDebug.append("]");

                    throw new RuntimeException();
                }

                if(DEBUG_CACHE.indexOf(e.element) < 0){
                    DEBUG_CACHE.add(e.element);

                }else{
                    VLDebug.append("Duplicate entry for [");
                    VLDebug.append(FSLoader.ELEMENT_NAMES[e.element]);
                    VLDebug.append("]");

                    throw new RuntimeException();
                }
            }

            if(stride <= 0){
                VLDebug.append("Invalid stride[");
                VLDebug.append(stride);
                VLDebug.append("]");

                throw new RuntimeException();
            }

            if(mechanism instanceof ComplexMechanism || mechanism instanceof ComplexSingularMechanism){
                int[] sizes = new int[size];

                if(mechanism instanceof ComplexMechanism){
                    int size2 = targetmesh.size();

                    for(int i = 0; i < size; i++){
                        e = entries.get(i);

                        for(int i2 = 0; i2 < size2; i2++){
                            e = entries.get(i);
                            sizes[i] += targetmesh.get(i2).element(e.element).size() / e.unitsize;
                        }
                    }

                }else{
                    for(int i = 0; i < size; i++){
                        e = entries.get(i);
                        sizes[i] += targetmesh.first().element(e.element).size() / e.unitsize;
                    }
                }

                for(int i = 1; i < size; i++){
                    if(sizes[i] != sizes[i - 1]){
                        VLDebug.append("Using a ComplexMechanism type but target mesh element vertex sizes do not match for [");
                        VLDebug.append(FSLoader.ELEMENT_NAMES[entries.get(i).element]);
                        VLDebug.append("] and [");
                        VLDebug.append(FSLoader.ELEMENT_NAMES[entries.get(i - 1).element]);
                        VLDebug.append("]");

                        throw new RuntimeException();
                    }
                }
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

            buffer.get(bufferindex).provider().stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            buffer();
        }
    }

    public abstract static class Mechanism{

        protected abstract int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride);
    }

    public abstract static class MechanismSingularBase extends Mechanism{}

    public abstract static class MechanismInstancedBase extends Mechanism{}

    private static final class SequentialMechanism extends MechanismInstancedBase{

        private SequentialMechanism(){

        }

        @Override
        protected int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;

            FSLoader.Assembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                instance.buffers.add(element, new FSBufferAddress(buffer, bufferindex, buffer.position(bufferindex), entry.unitoffset,
                        entry.unitsize, stride, array.size() / entry.unitsize));

                step.process(buffer, bufferindex, array);
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ComplexMechanism extends MechanismInstancedBase{

        private ComplexMechanism(){
            
        }

        @Override
        protected int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            FSLoader.Assembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                instance.buffers.add(element, new FSBufferAddress(buffer, bufferindex, buffer.position(bufferindex), entry.unitoffset,
                        entry.unitsize, stride, array.size() / entry.unitsize));

                step.process(buffer, bufferindex, array, 0, array.size(), entry.unitoffset, FSLoader.UNIT_SIZES[element], entry.unitsize, stride);
            }

            return mainoffset + entry.unitsize;
        }
    }

    private static final class SequentialSingularMechanism extends MechanismSingularBase{

        private SequentialSingularMechanism(){

        }

        @Override
        protected int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;

            instance = instances.get(0);
            array = instance.element(element);

            FSBufferAddress address = new FSBufferAddress(buffer, bufferindex, buffer.position(bufferindex), entry.unitoffset,
                    entry.unitsize, stride, array.size() / entry.unitsize);

            assembler.bufferFunc(element).process(buffer, bufferindex, array);

            for(int i = 0; i < size; i++){
                instances.get(i).buffers.add(element, address);
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ComplexSingularMechanism extends MechanismSingularBase{

        private ComplexSingularMechanism(){

        }

        @Override
        protected int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            instance = instances.get(0);
            array = instance.element(element);

            FSBufferAddress address = new FSBufferAddress(buffer, bufferindex, buffer.position(bufferindex), entry.unitoffset,
                    entry.unitsize, stride, array.size() / entry.unitsize);

            assembler.bufferFunc(element).process(buffer, bufferindex, array, 0, array.size(), entry.unitoffset, FSLoader.UNIT_SIZES[element], entry.unitsize, stride);

            for(int i = 0; i < size; i++){
                instances.get(i).buffers.add(element, address);
            }

            return mainoffset + entry.unitsize;
        }
    }

    private static final class IndicesSingularMechanism extends MechanismSingularBase{

        private IndicesSingularMechanism(){

        }

        @Override
        protected int buffer(FSLoader.Assembler assembler, FSMesh mesh, Entry entry, FSBufferManager buffer, int bufferindex, int stride){
            int unitsize = entry.unitsize;
            int element = entry.element;

            FSBufferAddress indicesaddress = new FSBufferAddress(buffer, bufferindex, buffer.position(bufferindex), entry.unitoffset,
                    unitsize, stride, mesh.indices.size() / unitsize);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                mesh.get(i).bufferTracker().add(element, indicesaddress);
            }

            assembler.bufferFunc(element).process(buffer, bufferindex, mesh.indices, 0, mesh.indices.size(),
                    entry.unitoffset, FSLoader.UNIT_SIZES[FSLoader.ELEMENT_INDEX], unitsize, stride);

            return buffer.position(bufferindex);
        }
    }
}
