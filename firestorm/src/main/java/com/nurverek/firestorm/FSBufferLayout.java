package com.nurverek.firestorm;

import java.nio.Buffer;

import vanguard.VLArrayFloat;
import vanguard.VLBuffer;
import vanguard.VLBufferTrackerDetailed;
import vanguard.VLDebug;
import vanguard.VLListType;

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

    public FSBufferLayout(){
        layouts = new VLListType<>(FSG.ELEMENT_TOTAL_COUNT, FSG.ELEMENT_TOTAL_COUNT);
    }

    public Layout add(VLBuffer<?, ?> buffer, int capacity){
        Layout layout = new Layout(buffer, capacity);
        layouts.add(layout);

        return layout;
    }

    public void accountFor(FSMesh target){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).accountFor(target);
        }
    }

    public void buffer(FSMesh target){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).buffer(target);
        }
    }

    public void bufferDebug(FSMesh target){
        int size = layouts.size();

        VLDebug.append("[BufferLayout]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("Layout[");
            VLDebug.append(i);
            VLDebug.append("] ");
            VLDebug.printD();

            try{
                layouts.get(i).bufferDebug(target);

            }catch(Exception ex){
                VLDebug.append(" [FAILED]\n");
                throw ex;
            }

            VLDebug.append(" [SUCCESS]\n");
        }

        VLDebug.append("\n");
    }


    public static abstract class Entry{

        public Mechanism mechanism;

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;
        public int strideadjustment;

        public Entry(Mechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount, int strideadjustment){
            this.mechanism = mechanism;
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
            this.strideadjustment = strideadjustment;
        }

        public int strideAdjustment(){
            return strideadjustment;
        }

        public int bufferSizeAdjustment(FSMesh mesh){
            return (mechanism.getTargetSize(this, mesh) / unitsize) * strideadjustment;
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
            VLDebug.append("] strideAdjustment[");
            VLDebug.append(strideadjustment);
            VLDebug.append("]]");
        }
    }

    public static class EntryElement extends Entry{

        public EntryElement(Mechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount, int stride){
            super(mechanism, element, unitoffset, unitsize, unitsubcount, stride);
        }

        public EntryElement(Mechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            super(mechanism, element, unitoffset, unitsize, unitsubcount, unitsubcount);
        }

        public EntryElement(Mechanism mechanism, int element){
            super(mechanism, element, 0, FSG.UNIT_SIZES[element], FSG.UNIT_SIZES[element], FSG.UNIT_SIZES[element]);
        }
    }

    public static class EntryLink extends Entry{

        public EntryLink(Mechanism mechanism, int linkindex, int unitoffset, int unitsize, int unitsubcount, int stride){
            super(mechanism, linkindex, unitoffset, unitsize, unitsubcount, stride);
        }
    }

    public static final class Layout{

        protected VLListType<Entry> entries;
        protected VLBuffer<?, ?> buffer;

        protected int totalstride;

        private Layout(VLBuffer<?, ?> buffer, int capacity){
            this.buffer = buffer;
            entries = new VLListType<>(capacity, capacity / 2);
        }

        public void accountFor(FSMesh target){
            int size = entries.size();

            for(int i = 0; i < size; i++){
                buffer.adjustPreInitCapacity(entries.get(i).bufferSizeAdjustment(target));
            }
        }

        public Layout addElement(EntryElement entry){
            totalstride += entry.strideAdjustment();
            entries.add(entry);

            return this;
        }

        public Layout addLink(EntryLink entry){
            totalstride += entry.strideAdjustment();
            entries.add(entry);

            return this;
        }

        protected void buffer(FSMesh target){
            int size = entries.size();
            Entry entry;

            for(int i = 0; i < size - 1; i++){
                entry = entries.get(i);
                buffer.position(entry.mechanism.buffer(target, entry, buffer, totalstride));
            }

            entry = entries.get(entries.size() - 1);
            entry.mechanism.buffer(target, entry, buffer, totalstride);
        }

        protected void bufferDebug(FSMesh target){
            int size = entries.size();
            Entry e;

            if(totalstride <= 0){
                VLDebug.append("Invalid stride[");
                VLDebug.append(totalstride);
                VLDebug.append("]");

                throw new RuntimeException();
            }

            VLDebug.append("stride[");
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

            buffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            buffer(target);
        }
    }

    public interface Mechanism{

        <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride);
        int getTargetSize(Entry entry, FSMesh mesh);
    }

    private static final class ElementSequentialInstanced implements Mechanism{

        private ElementSequentialInstanced(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            VLBufferTrackerDetailed<VLBuffer<ELEMENT, BUFFER>> tracker;

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                tracker = new VLBufferTrackerDetailed<>();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instance.bufferBindings().add(element, tracker);
            }

            return buffer.position();
        }

        public int getTargetSize(Entry entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.instance(i).element(entry.element).size();
            }

            return total;
        }
    }

    private static final class ElementInterleavedInstanced implements Mechanism{

        private ElementInterleavedInstanced(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position();

            VLBufferTrackerDetailed<VLBuffer<ELEMENT, BUFFER>> tracker;

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                tracker = new VLBufferTrackerDetailed<>();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instance.bufferBindings().add(element, tracker);
            }

            return mainoffset + entry.unitsubcount;
        }

        public int getTargetSize(Entry entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.instance(i).element(entry.element).size();
            }

            return total;
        }
    }

    private static final class ElementSequentialSingular implements Mechanism{

        private ElementSequentialSingular(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            VLListType<FSInstance> instances = mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            VLBufferTrackerDetailed<VLBuffer<ELEMENT, BUFFER>> tracker;

            instance = instances.get(0);
            array = instance.element(element);

            for(int i = 0; i < size; i++){
                tracker = new VLBufferTrackerDetailed<>();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instances.get(i).bufferBindings().add(element, tracker);
            }

            return buffer.position();
        }

        public int getTargetSize(Entry entry, FSMesh mesh){
            return mesh.instance(0).element(entry.element).size();
        }
    }

    private static final class ElementInterleavedSingular implements Mechanism{

        private ElementInterleavedSingular(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            VLListType<FSInstance> instances = mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position();

            instance = instances.get(0);
            array = instance.element(element);

            VLBufferTrackerDetailed<VLBuffer<ELEMENT, BUFFER>> tracker = new VLBufferTrackerDetailed<>();
            buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            for(int i = 0; i < size; i++){
                instances.get(i).bufferBindings().add(element, tracker);
            }

            return mainoffset + entry.unitsubcount;
        }

        public int getTargetSize(Entry entry, FSMesh mesh){
            return mesh.instance(0).element(entry.element).size();
        }
    }

    private static final class ElementSequentialIndices implements Mechanism{

        private ElementSequentialIndices(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            int element = entry.element;

            VLBufferTrackerDetailed<VLBuffer<ELEMENT, BUFFER>> tracker = new VLBufferTrackerDetailed<>();
            buffer.put(tracker, mesh.indices.provider(), 0, mesh.indices.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                mesh.instance(i).bufferBindings().add(element, tracker);
            }

            return buffer.position();
        }

        public int getTargetSize(Entry entry, FSMesh mesh){
            return mesh.indices().size();
        }
    }

    private static final class LinkSequentialSingular implements Mechanism{

        private LinkSequentialSingular(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            ((FSLinkBuffered<?, VLBuffer<ELEMENT, BUFFER>, ?>)mesh.link(entry.element)).buffer(buffer, entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return buffer.position();
        }

        @Override
        public int getTargetSize(Entry entry, FSMesh mesh){
            return (((FSLinkBuffered<?, ?, ?>)mesh.link(entry.element)).size() / entry.unitsize) * entry.unitsubcount;
        }
    }

    private static final class LinkInterleavedSingular implements Mechanism{

        private LinkInterleavedSingular(){

        }

        @Override
        public <ELEMENT extends Number, BUFFER extends Buffer> int buffer(FSMesh mesh, Entry entry, VLBuffer<ELEMENT, BUFFER> buffer, int stride){
            int firstpos = buffer.position();
            ((FSLinkBuffered<?, VLBuffer<ELEMENT, BUFFER>, ?>)mesh.link(entry.element)).buffer(buffer, entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return firstpos + entry.unitsubcount;
        }

        @Override
        public int getTargetSize(Entry entry, FSMesh mesh){
            return ((FSLinkBuffered<?, ?, ?>)mesh.link(entry.element)).size();
        }
    }
}
