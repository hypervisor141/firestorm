package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListInt;
import com.nurverek.vanguard.VLListType;

public class FSBufferLayout{

    private static final int BUFFER_PRINT_LIMIT = 20;
    private static final VLListInt DEBUG_CACHE = new VLListInt(0, 5);

    public static final Mechanism MECHANISM_ELEMENTS_INSTANCED_SEQUENTIAL = new SequentialMechanism();
    public static final Mechanism MECHANISM_ELEMENTS_INSTANCED_COMPLEX = new ComplexMechanism();
    public static final Mechanism MECHANISM_ELEMENTS_SINGULAR_SEQUENTIAL = new SequentialSingularMechanism();
    public static final Mechanism MECHANISM_ELEMENTS_SINGULAR_COMPLEX = new ComplexSingularMechanism();
    public static final Mechanism MECHANISM_ELEMENTS_SINGULAR_INDICES = new IndicesSingularMechanism();
    public static final Mechanism MECHANISM_LINKS_SINGULAR_SEQUENTIAL = new LinksSequentialMechanism();
    public static final Mechanism MECHANISM_LINKS_SINGULAR_COMPLEX = new LinksComplexMechanism();

    protected VLListType<Layout> layouts;
    protected FSGenerator.Assembler assembler;
    protected FSMesh targetmesh;

    public FSBufferLayout(FSMesh mesh, FSGenerator.Assembler assembler){
        this.targetmesh = mesh;
        this.assembler = assembler;

        layouts = new VLListType<>(FSGenerator.ELEMENT_TOTAL_COUNT, FSGenerator.ELEMENT_TOTAL_COUNT);
    }


    public Layout add(FSBufferManager buffer, int bufferindex, Mechanism mechanism){
        Layout layout = new Layout(buffer, bufferindex, mechanism);
        layouts.add(layout);

        return layout;
    }

    public void increaseTargetCapacities(int element, int totalsize){
        int size = layouts.size();
        int unitcount = totalsize / FSGenerator.UNIT_SIZES[element];

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

    public void bufferDebug(FSGenerator.Scanner scanner){
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

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;

        public EntryType(int element, int unitoffset, int unitsize, int unitsubcount){
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
        }

        public int strideAdjustment(){
            return unitsubcount;
        }

        public void debugInfo(){
            VLDebug.append(FSGenerator.ELEMENT_NAMES[element]);
            VLDebug.append("[unitOffset[");
            VLDebug.append(unitoffset);
            VLDebug.append("] unitSize[");
            VLDebug.append(unitsize);
            VLDebug.append("] unitSubCount[");
            VLDebug.append(unitsubcount);
            VLDebug.append("]]");
        }
    }

    public static class EntryBasic extends EntryType{

        public EntryBasic(int element, int unitoffset, int unitsize, int unitsubcount){
            super(element, unitoffset, unitsize, unitsubcount);
        }
    }

    public final class Layout{

        protected VLListType<EntryType> entries;

        protected FSBufferManager buffer;
        protected Mechanism mechanism;

        protected int bufferindex;
        protected int stride;

        protected Layout(FSBufferManager buffer, int bufferindex, Mechanism mechanism){
            this.mechanism = mechanism;
            this.buffer = buffer;
            this.bufferindex = bufferindex;

            entries = new VLListType<>(FSGenerator.ELEMENT_TOTAL_COUNT, FSGenerator.ELEMENT_TOTAL_COUNT);
        }


        public Layout addElement(int element){
            return addElement(element, 0, FSGenerator.UNIT_SIZES[element]);
        }

        public Layout addElement(int element, int unitoffset, int unitsubcount){
            EntryType e = new EntryBasic(element, unitoffset, FSGenerator.UNIT_SIZES[element], unitsubcount);
            stride += e.strideAdjustment();
            entries.add(e);

            return this;
        }

        public Layout addLink(int linkindex, int unitoffset, int unitsize, int unitsubcount){
            EntryType e = new EntryBasic(linkindex, unitoffset, unitsize, unitsubcount);
            stride += e.strideAdjustment();
            entries.add(e);

            return this;
        }

        public Layout addCustom(EntryType entry){
            stride += entry.strideAdjustment();
            entries.add(entry);

            return this;
        }

        protected void increaseTargetCapacities(int element, int unitcount){
            int size = entries.size();
            int total = 0;

            EntryType e;

            for(int i = 0; i < size; i++){
                e = entries.get(i);

                if(e.element == element){
                    total += unitcount * e.unitsubcount;
                }
            }

            buffer.increaseTargetCapacity(bufferindex, total);
        }

        protected void buffer(){
            int size = entries.size();
            VLBuffer b = buffer.get(bufferindex).buffer();

            for(int i = 0; i < size - 1; i++){
                b.position(mechanism.buffer(assembler, targetmesh, entries.get(i), buffer, bufferindex, stride));
            }

            mechanism.buffer(assembler, targetmesh, entries.get(entries.size() - 1), buffer, bufferindex, stride);
        }

        protected void bufferDebug(FSGenerator.Scanner scanner){
            if(scanner instanceof FSGenerator.ScannerSingular && mechanism instanceof MechanismInstancedBase){
                VLDebug.append("[WARNING] [USING INSTANCED BUFFER MECHANISM FOR A SINGULAR SCANNER]");
                VLDebug.printE();

            }else if(scanner instanceof FSGenerator.ScannerInstanced && mechanism instanceof MechanismSingularBase){
                VLDebug.append("[WARNING] [USING SINGULAR BUFFER MECHANISM FOR AN INSTANCED SCANNER]");
                VLDebug.printE();
            }

            int size = entries.size();
            EntryType e;

            DEBUG_CACHE.virtualSize(0);

            for(int i = 0; i < size; i++){
                e = entries.get(i);

                if((e.unitoffset + e.unitsubcount) > FSGenerator.UNIT_SIZES[e.element]){
                    VLDebug.append("Invalid unitoffset[");
                    VLDebug.append(e.unitoffset);
                    VLDebug.append("] and/or unitsize[");
                    VLDebug.append(e.unitsubcount);
                    VLDebug.append("] for [");
                    VLDebug.append(FSGenerator.ELEMENT_NAMES[e.element]);
                    VLDebug.append("]");

                    throw new RuntimeException();
                }

                if(DEBUG_CACHE.indexOf(e.element) < 0){
                    DEBUG_CACHE.add(e.element);

                }else{
                    VLDebug.append("Duplicate entry for [");
                    VLDebug.append(FSGenerator.ELEMENT_NAMES[e.element]);
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
                            sizes[i] += targetmesh.instance(i2).element(e.element).size() / e.unitsubcount;
                        }
                    }

                }else{
                    for(int i = 0; i < size; i++){
                        e = entries.get(i);
                        sizes[i] += targetmesh.first().element(e.element).size() / e.unitsubcount;
                    }
                }

                for(int i = 1; i < size; i++){
                    if(sizes[i] != sizes[i - 1]){
                        VLDebug.append("Using a ComplexMechanism type but target mesh element vertex sizes do not match for [");
                        VLDebug.append(FSGenerator.ELEMENT_NAMES[entries.get(i).element]);
                        VLDebug.append("] and [");
                        VLDebug.append(FSGenerator.ELEMENT_NAMES[entries.get(i - 1).element]);
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

            buffer.get(bufferindex).buffer().stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            buffer();
        }
    }

    public abstract static class Mechanism<ENTRY extends EntryType>{

        private Mechanism(){}

        protected abstract int buffer(FSGenerator.Assembler assembler, FSMesh mesh, ENTRY entry, FSBufferManager buffer, int bufferindex, int stride);
    }

    public abstract static class MechanismSingularBase<ENTRY extends EntryType> extends Mechanism<ENTRY>{

        private MechanismSingularBase(){}
    }

    public abstract static class MechanismInstancedBase<ENTRY extends EntryType> extends Mechanism<ENTRY>{

        private MechanismInstancedBase(){}
    }

    private static final class SequentialMechanism extends MechanismInstancedBase<EntryBasic>{

        private SequentialMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;

            FSGenerator.Assembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                instance.buffers.add(element, step.process(buffer, bufferindex, array, entry.unitsubcount, stride));
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ComplexMechanism extends MechanismInstancedBase<EntryBasic>{

        private ComplexMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            FSGenerator.Assembler.BufferStep step = assembler.bufferFunc(element);

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                instance.buffers.add(element, step.process(buffer, bufferindex, array, 0,
                        array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride));
            }

            return mainoffset + entry.unitsubcount;
        }
    }

    private static final class SequentialSingularMechanism extends MechanismSingularBase<EntryBasic>{

        private SequentialSingularMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;

            instance = instances.get(0);
            array = instance.element(element);

            for(int i = 0; i < size; i++){
                instances.get(i).buffers.add(element, assembler.bufferFunc(element).process(buffer, bufferindex, array, entry.unitsubcount, stride));
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class ComplexSingularMechanism extends MechanismSingularBase<EntryBasic>{

        private ComplexSingularMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position(bufferindex);

            instance = instances.get(0);
            array = instance.element(element);

            FSBufferAddress address = assembler.bufferFunc(element).process(buffer, bufferindex, array,
                    0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            for(int i = 0; i < size; i++){
                instances.get(i).buffers.add(element, address);
            }

            return mainoffset + entry.unitsubcount;
        }
    }

    private static final class IndicesSingularMechanism extends MechanismSingularBase<EntryBasic>{

        private IndicesSingularMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            int element = entry.element;
            FSBufferAddress address = assembler.bufferFunc(element).process(buffer, bufferindex, mesh.indices, 0,
                    mesh.indices.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                mesh.instance(i).bufferTracker().add(element, address);
            }

            return buffer.position(bufferindex);
        }
    }

    private static final class LinksSequentialMechanism extends MechanismSingularBase<EntryBasic>{

        private LinksSequentialMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            mesh.link(entry.element).buffer(buffer, bufferindex, entry.unitoffset, entry.unitsubcount);
            return buffer.position(bufferindex);
        }
    }

    private static final class LinksComplexMechanism extends MechanismSingularBase<EntryBasic>{

        private LinksComplexMechanism(){}

        @Override
        protected int buffer(FSGenerator.Assembler assembler, FSMesh mesh, EntryBasic entry, FSBufferManager buffer, int bufferindex, int stride){
            int firstpos = buffer.position(bufferindex);
            FSLink link = mesh.link(entry.element);
            link.buffer(buffer, bufferindex, 0, link.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return firstpos + entry.unitsubcount;
        }
    }
}
