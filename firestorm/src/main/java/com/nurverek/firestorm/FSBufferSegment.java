package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLDebug;
import vanguard.VLListType;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    private static final int BUFFER_PRINT_LIMIT = 100;

    public static final FSBufferMechanism ELEMENT_SEQUENTIAL_INSTANCED = new FSBufferMechanism.ElementSequentialInstanced();
    public static final FSBufferMechanism ELEMENT_INTERLEAVED_INSTANCED = new FSBufferMechanism.ElementInterleavedInstanced();

    public static final FSBufferMechanism ELEMENT_SEQUENTIAL_SINGULAR = new FSBufferMechanism.ElementSequentialSingular();
    public static final FSBufferMechanism ELEMENT_INTERLEAVED_SINGULAR = new FSBufferMechanism.ElementInterleavedSingular();

    public static final FSBufferMechanism ELEMENT_SEQUENTIAL_INDICES = new FSBufferMechanism.ElementSequentialIndices();

    public static final FSBufferMechanism LINK_SEQUENTIAL_SINGULAR = new FSBufferMechanism.LinkSequentialSingular();
    public static final FSBufferMechanism LINK_INTERLEAVED_SINGULAR = new FSBufferMechanism.LinkInterleavedSingular();

    protected VLListType<Entry> entries;

    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    protected int totalstride;

    public FSBufferSegment(FSVertexBuffer<BUFFER> vbuffer, int capacity){
        this.vbuffer = vbuffer;
        this.buffer = vbuffer.provider();

        entries = new VLListType<>(capacity, capacity / 2);
    }

    public FSBufferSegment(BUFFER buffer, int capacity){
        this.buffer = buffer;
        entries = new VLListType<>(capacity, capacity / 2);
    }

    public void accountFor(FSMesh target){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            buffer.adjustPreInitCapacity(entries.get(i).calculateNeededSize(target));
        }
    }

    public FSBufferSegment<BUFFER> addElement(EntryElement entry){
        totalstride += entry.unitsubcount;
        entries.add(entry);

        return this;
    }

    public FSBufferSegment<BUFFER> addLink(EntryLink entry){
        totalstride += entry.unitsubcount;
        entries.add(entry);

        return this;
    }

    protected void buffer(FSMesh target){
        int size = entries.size();
        Entry entry;

        for(int i = 0; i < size - 1; i++){
            entry = entries.get(i);
            buffer.position(entry.mechanism.buffer(target, entry, vbuffer, buffer, totalstride));
        }

        entry = entries.get(entries.size() - 1);
        entry.mechanism.buffer(target, entry, vbuffer, buffer, totalstride);
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

    public static abstract class Entry{

        public FSBufferMechanism mechanism;

        public int element;
        public int unitoffset;
        public int unitsize;
        public int unitsubcount;

        public Entry(FSBufferMechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            this.mechanism = mechanism;
            this.element = element;
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
        }

        public int calculateNeededSize(FSMesh mesh){
            return (mechanism.calculateNeededSize(this, mesh) / unitsize) * unitsubcount;
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
            VLDebug.append("]");
        }
    }

    public static class EntryElement extends Entry{

        public EntryElement(FSBufferMechanism mechanism, int element, int unitoffset, int unitsize, int unitsubcount){
            super(mechanism, element, unitoffset, unitsize, unitsubcount);
        }

        public EntryElement(FSBufferMechanism mechanism, int element){
            super(mechanism, element, 0, FSHub.UNIT_SIZES[element], FSHub.UNIT_SIZES[element]);
        }
    }

    public static class EntryLink extends Entry{

        public EntryLink(FSBufferMechanism mechanism, int linkindex, int unitoffset, int unitsize, int unitsubcount){
            super(mechanism, linkindex, unitoffset, unitsize, unitsubcount);
        }
    }
}
