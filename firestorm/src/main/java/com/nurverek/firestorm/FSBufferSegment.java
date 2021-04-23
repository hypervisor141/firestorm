package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLBuffer;
import vanguard.VLBufferTracker;
import vanguard.VLDebug;
import vanguard.VLListType;

public final class FSBufferSegment<BUFFER extends VLBuffer<?, ?>>{

    private static final int BUFFER_PRINT_LIMIT = 100;

    protected VLListType<Entry<BUFFER>> entries;

    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    protected int totalstride;
    protected boolean interleaved;

    private boolean debuggedsegmentstructure;

    public FSBufferSegment(FSVertexBuffer<BUFFER> vbuffer, boolean interleaved, int capacity){
        this.vbuffer = vbuffer;
        this.buffer = vbuffer.provider();
        this.interleaved = interleaved;

        debuggedsegmentstructure = false;
        entries = new VLListType<>(capacity, capacity / 2);
    }

    public FSBufferSegment(BUFFER buffer, boolean interleaved, int capacity){
        this.buffer = buffer;
        this.interleaved = interleaved;

        debuggedsegmentstructure = false;
        entries = new VLListType<>(capacity, capacity / 2);
    }

    public void accountFor(FSMesh target){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            buffer.adjustPreInitCapacity(entries.get(i).calculateNeededSize(target));
        }
    }

    public FSBufferSegment<BUFFER> add(Entry<BUFFER> entry){
        totalstride += entry.unitsubcount;
        entries.add(entry);

        return this;
    }

    protected void buffer(FSMesh target){
        int size = entries.size();

        for(int i = 0; i < size - 1; i++){
            buffer.position(entries.get(i).buffer(target, buffer, vbuffer, totalstride, interleaved));
        }

        entries.get(size - 1).buffer(target, buffer, vbuffer, totalstride, interleaved);
    }

    protected void bufferDebug(FSMesh target){
        int size = entries.size();
        Entry<BUFFER> entry;

        if(!debuggedsegmentstructure){
            if(totalstride <= 0){
                VLDebug.append("[Invalid stride value] stride[");
                VLDebug.append(totalstride);
                VLDebug.append("]");

                throw new RuntimeException();
            }
            if(interleaved){
                if(size <= 1){
                    VLDebug.append("[Segment is set to interleaved mode but there is less than 1 entry]");
                    throw new RuntimeException();
                }
            }

            debuggedsegmentstructure = true;
        }

        if(interleaved){
            entry = entries.get(0);
            int referencevalue = entry.getInterleaveDebugReferenceValue(target);

            VLDebug.append("[Testing reference value from first entry] entry[");
            VLDebug.append(entry.getClass().getSimpleName());
            VLDebug.append("]");

            for(int i = 0; i < size; i++){
                entry = entries.get(i);
                entry.debugInterleaved(target, referencevalue);
            }
        }

        VLDebug.append("stride[");
        VLDebug.append(totalstride);
        VLDebug.append("] entries[");

        for(int i = 0; i < size; i++){
            entries.get(i).debugInfo();

            if(i < size - 1){
                VLDebug.append(", ");
            }
        }

        VLDebug.append("] ");

        buffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

        buffer(target);
    }

    public static abstract class Entry<BUFFER extends VLBuffer<?, ?>>{

        public int unitoffset;
        public int unitsize;
        public int unitsubcount;
        public boolean instanced;

        public Entry(int unitoffset, int unitsize, int unitsubcount, boolean instanced){
            this.unitoffset = unitoffset;
            this.unitsize = unitsize;
            this.unitsubcount = unitsubcount;
            this.instanced = instanced;
        }

        public abstract int calculateNeededSize(FSMesh target);
        protected abstract int getInterleaveDebugReferenceValue(FSMesh target);
        public abstract int buffer(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride, boolean interleaved);
        public abstract void debugInterleaved(FSMesh target, int referencesize);

        public void debugInfo(){
            VLDebug.append("[");
            VLDebug.append(getClass().getSimpleName());
            VLDebug.append("] unitOffset[");
            VLDebug.append(unitoffset);
            VLDebug.append("] unitSize[");
            VLDebug.append(unitsize);
            VLDebug.append("] unitSubCount[");
            VLDebug.append(unitsubcount);
            VLDebug.append("]");
        }
    }

    public static class Element<BUFFER extends VLBuffer<?, ?>> extends Entry<BUFFER>{

        public int element;

        public Element(int element, int unitoffset, int unitsize, int unitsubcount, boolean instanced){
            super(unitoffset, unitsize, unitsubcount, instanced);
            this.element = element;
        }

        public Element(int element, boolean instanced){
            super(0, FSHub.UNIT_SIZES[element], FSHub.UNIT_SIZES[element], instanced);
            this.element = element;
        }

        @Override
        public int calculateNeededSize(FSMesh target){
            int total = 0;

            if(instanced){
                int size = target.size();

                for(int i = 0; i < size; i++){
                    total += target.get(i).element(element).size();
                }

            }else{
                total = target.first().element(element).size();
            }

            return (total / unitsize) * unitsubcount;
        }

        @Override
        protected int getInterleaveDebugReferenceValue(FSMesh target){
            return target.first().element(element).size() / unitsize;
        }

        @Override
        public int buffer(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride, boolean interleaved){
            if(interleaved){
                int initialoffset = buffer.position();

                if(instanced){
                    int size = target.size();
                    int buffersize = buffer.size();

                    for(int i = 0; i < size; i++){
                        FSInstance instance = target.get(i);
                        VLArrayFloat array = instance.element(element);
                        VLBufferTracker tracker = new VLBufferTracker();
                        int arraysize = array.size();

                        buffer.put(tracker, array.provider(), 0, arraysize, unitoffset, unitsize, unitsubcount, stride);
                        int newpos = buffer.position() + stride - unitsubcount;

                        if(arraysize <= unitsize && newpos < buffersize && i < size - 1){
                            buffer.position(newpos);
                        }

                        instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
                    }

                }else{
                    FSInstance instance = target.first();
                    VLArrayFloat array = instance.element(element);
                    VLBufferTracker tracker = new VLBufferTracker();

                    buffer.put(tracker, array.provider(), 0, array.size(), unitoffset, unitsize, unitsubcount, stride);
                    instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
                }

                return initialoffset + unitsubcount;

            }else{
                if(instanced){
                    int size = target.size();

                    for(int i = 0; i < size; i++){
                        FSInstance instance = target.get(i);
                        VLBufferTracker tracker = new VLBufferTracker();

                        buffer.put(tracker, instance.element(element).provider());
                        instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
                    }

                }else{
                    FSInstance instance = target.first();
                    VLBufferTracker tracker = new VLBufferTracker();

                    buffer.put(tracker, instance.element(element).provider());
                    instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
                }

                return buffer.position();
            }
        }

        @Override
        public void debugInterleaved(FSMesh target, int referencesize){
            if(instanced){
                int size = target.size();

                for(int i = 0; i < size; i++){
                    debugInstance(target, target.get(i), referencesize);
                }

            }else{
                debugInstance(target, target.first(), referencesize);
            }
        }

        private void debugInstance(FSMesh target, FSInstance instance, int referencesize){
            if((instance.element(element).size() / unitsize) != referencesize){
                VLDebug.append("[Segment is set to interleaved mode has an instance whose count does not match with other entries] element[");
                VLDebug.append(element);
                VLDebug.append("] instanceVertexSize[");
                VLDebug.append(instance.vertexSize());
                VLDebug.append("] referenceSize[");
                VLDebug.append(referencesize);
                VLDebug.append("] mesh[");
                VLDebug.append(target.name);
                VLDebug.append("] instance[");
                VLDebug.append(instance.name);
                VLDebug.append("]");

                throw new RuntimeException();
            }
        }
    }

    public static class Indices<BUFFER extends VLBuffer<?, ?>> extends Entry<BUFFER>{

        public Indices(){
            super(0, FSHub.UNIT_SIZES[FSHub.ELEMENT_INDEX], FSHub.UNIT_SIZES[FSHub.ELEMENT_INDEX], false);
        }

        @Override
        public int calculateNeededSize(FSMesh target){
            return target.indices.size();
        }

        @Override
        protected int getInterleaveDebugReferenceValue(FSMesh target){
            return target.indices.size();
        }

        @Override
        public int buffer(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride, boolean interleaved){
            int size = target.size();

            VLArrayShort array = target.indices;
            VLBufferTracker tracker = new VLBufferTracker();

            buffer.put(tracker, array.provider(), 0, array.size(), unitoffset, unitsize, unitsubcount, stride);

            for(int i = 0; i < size; i++){
                target.get(i).bufferBindings().add(FSHub.ELEMENT_INDEX, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return buffer.position();
        }

        @Override
        public void debugInterleaved(FSMesh target, int referencesize){
            VLDebug.append("[Can't interleave for Index element type]");
            throw new RuntimeException();
        }
    }

    public static class Link<BUFFER extends VLBuffer<?, ?>> extends Entry<BUFFER>{

        public int linkindex;

        public Link(int linkindex, int unitoffset, int unitsize, int unitsubcount){
            super(unitoffset, unitsize, unitsubcount, false);
            this.linkindex = linkindex;
        }

        @Override
        public int calculateNeededSize(FSMesh target){
            return (((FSLinkBuffered<?, BUFFER>)target.getLink(linkindex)).size() / unitsize) * unitsubcount;
        }

        @Override
        protected int getInterleaveDebugReferenceValue(FSMesh target){
            return ((FSLinkBuffered<?, BUFFER>)target.getLink(linkindex)).size() / unitsize;
        }

        @Override
        public int buffer(FSMesh target, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer, int stride, boolean interleaved){
            FSLinkBuffered<?, BUFFER> link = ((FSLinkBuffered<?, BUFFER>)target.getLink(linkindex));
            link.setBuffer(buffer);
            link.setVertexBuffer(vbuffer);

            if(interleaved){
                int firstpos = buffer.position();
                link.buffer(unitoffset, unitsize, unitsubcount, stride);

                return firstpos + unitsubcount;

            }else{
                link.buffer();
                return buffer.position();
            }
        }

        @Override
        public void debugInterleaved(FSMesh target, int referencesize){
            if((((FSLinkBuffered<?, BUFFER>)target.links().get(linkindex)).size() / unitsize) != referencesize){
                VLDebug.append("[Segment is set to interleaved mode has an link whose count does not match with other entries] linkindex[");
                VLDebug.append(linkindex);
                VLDebug.append("] linkSize[");
                VLDebug.append(getInterleaveDebugReferenceValue(target));
                VLDebug.append("] referenceSize[");
                VLDebug.append(referencesize);
                VLDebug.append("] mesh[");
                VLDebug.append(target.name);
                VLDebug.append("]");

                throw new RuntimeException();
            }
        }
    }
}
