package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLBuffer;
import vanguard.VLBufferTrackerDetailed;
import vanguard.VLListType;

public interface FSBufferMechanism{

    <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride);
    int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh);

    class ElementSequentialInstanced implements FSBufferMechanism{

        public ElementSequentialInstanced(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            VLListType<FSInstance> instances = (VLListType<FSInstance>)mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            VLBufferTrackerDetailed tracker;

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                tracker = new VLBufferTrackerDetailed();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return buffer.position();
        }

        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.get(i).element(entry.element).size();
            }

            return total;
        }
    }

    class ElementInterleavedInstanced implements FSBufferMechanism{

        public ElementInterleavedInstanced(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            VLListType<FSInstance> instances = (VLListType<FSInstance>)mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position();

            VLBufferTrackerDetailed tracker;

            for(int i = 0; i < size; i++){
                instance = instances.get(i);
                array = instance.element(element);

                tracker = new VLBufferTrackerDetailed();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instance.bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return mainoffset + entry.unitsubcount;
        }

        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            int size = mesh.size();
            int total = 0;

            for(int i = 0; i < size; i++){
                total += mesh.get(i).element(entry.element).size();
            }

            return total;
        }
    }

    class ElementSequentialSingular implements FSBufferMechanism{

        public ElementSequentialSingular(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            VLListType<FSInstance> instances = (VLListType<FSInstance>)mesh.instances;

            int element = entry.element;
            int size = instances.size();

            FSInstance instance;
            VLArrayFloat array;
            VLBufferTrackerDetailed tracker;

            instance = instances.get(0);
            array = instance.element(element);

            for(int i = 0; i < size; i++){
                tracker = new VLBufferTrackerDetailed();
                buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

                instances.get(i).bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return buffer.position();
        }

        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            return mesh.get(0).element(entry.element).size();
        }
    }

    class ElementInterleavedSingular implements FSBufferMechanism{

        public ElementInterleavedSingular(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            VLListType<FSInstance> instances = (VLListType<FSInstance>)mesh.instances;
            FSInstance instance;
            VLArrayFloat array;

            int size = instances.size();
            int element = entry.element;
            int mainoffset = buffer.position();

            instance = instances.get(0);
            array = instance.element(element);

            VLBufferTrackerDetailed tracker = new VLBufferTrackerDetailed();
            buffer.put(tracker, array.provider(), 0, array.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            for(int i = 0; i < size; i++){
                instances.get(i).bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return mainoffset + entry.unitsubcount;
        }

        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            return mesh.get(0).element(entry.element).size();
        }
    }

    class ElementSequentialIndices implements FSBufferMechanism{

        public ElementSequentialIndices(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            int element = entry.element;

            VLBufferTrackerDetailed tracker = new VLBufferTrackerDetailed();
            buffer.put(tracker, mesh.indices.provider(), 0, mesh.indices.size(), entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                mesh.get(i).bufferBindings().add(element, new FSBufferBindings.Binding<BUFFER>(tracker, buffer, vbuffer));
            }

            return buffer.position();
        }

        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            return mesh.indices().size();
        }
    }

    class LinkSequentialSingular implements FSBufferMechanism{

        public LinkSequentialSingular(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            FSLinkBuffered<?, BUFFER, ?> link = ((FSLinkBuffered<?, BUFFER, ?>)mesh.getLink(entry.element));
            link.setBuffer(buffer);
            link.setVertexBuffer(vbuffer);
            link.buffer(entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return buffer.position();
        }

        @Override
        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            return (((FSLinkBuffered<?, ?, ?>)mesh.getLink(entry.element)).size() / entry.unitsize) * entry.unitsubcount;
        }
    }

    class LinkInterleavedSingular implements FSBufferMechanism{

        public LinkInterleavedSingular(){

        }

        @Override
        public <BUFFER extends VLBuffer<?, ?>> int buffer(FSMesh mesh, FSBufferLayout.Entry entry, FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int stride){
            int firstpos = buffer.position();

            FSLinkBuffered<?, BUFFER, ?> link = ((FSLinkBuffered<?, BUFFER, ?>)mesh.getLink(entry.element));
            link.setBuffer(buffer);
            link.setVertexBuffer(vbuffer);
            link.buffer(entry.unitoffset, entry.unitsize, entry.unitsubcount, stride);

            return firstpos + entry.unitsubcount;
        }

        @Override
        public int calculateNeededSize(FSBufferLayout.Entry entry, FSMesh mesh){
            return ((FSLinkBuffered<?, ?, ?>)mesh.getLink(entry.element)).size();
        }
    }
}
