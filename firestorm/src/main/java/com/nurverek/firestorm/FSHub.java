package com.nurverek.firestorm;

import android.app.Activity;
import android.opengl.GLES32;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLDebug;
import vanguard.VLListType;

public abstract class FSHub{

    public static final int ELEMENT_BYTES_MODEL = Float.SIZE / 8;
    public static final int ELEMENT_BYTES_POSITION = Float.SIZE / 8;
    public static final int ELEMENT_BYTES_COLOR = Float.SIZE / 8;
    public static final int ELEMENT_BYTES_TEXCOORD = Float.SIZE / 8;
    public static final int ELEMENT_BYTES_NORMAL = Float.SIZE / 8;
    public static final int ELEMENT_BYTES_INDEX = Short.SIZE / 8;

    public static final int UNIT_SIZE_MODEL = 16;
    public static final int UNIT_SIZE_POSITION = 4;
    public static final int UNIT_SIZE_COLOR = 4;
    public static final int UNIT_SIZE_TEXCOORD = 2;
    public static final int UNIT_SIZE_NORMAL = 3;
    public static final int UNIT_SIZE_INDEX = 1;

    public static final int UNIT_BYTES_MODEL = UNIT_SIZE_MODEL * ELEMENT_BYTES_MODEL;
    public static final int UNIT_BYTES_POSITION = UNIT_SIZE_POSITION * ELEMENT_BYTES_POSITION;
    public static final int UNIT_BYTES_COLOR = UNIT_SIZE_COLOR * ELEMENT_BYTES_COLOR;
    public static final int UNIT_BYTES_TEXCOORD = UNIT_SIZE_TEXCOORD * ELEMENT_BYTES_TEXCOORD;
    public static final int UNIT_BYTES_NORMAL = UNIT_SIZE_NORMAL * ELEMENT_BYTES_NORMAL;
    public static final int UNIT_BYTES_INDEX = UNIT_SIZE_INDEX * ELEMENT_BYTES_INDEX;

    public static final int ELEMENT_GLDATA_TYPE_MODEL = GLES32.GL_FLOAT;
    public static final int ELEMENT_GLDATA_TYPE_POSITION = GLES32.GL_FLOAT;
    public static final int ELEMENT_GLDATA_TYPE_COLOR = GLES32.GL_FLOAT;
    public static final int ELEMENT_GLDATA_TYPE_TEXCOORD = GLES32.GL_FLOAT;
    public static final int ELEMENT_GLDATA_TYPE_NORMAL = GLES32.GL_FLOAT;
    public static final int ELEMENT_GLDATA_TYPE_INDEX = GLES32.GL_UNSIGNED_SHORT;

    public static final int ELEMENT_MODEL = 0;
    public static final int ELEMENT_POSITION = 1;
    public static final int ELEMENT_COLOR = 2;
    public static final int ELEMENT_TEXCOORD = 3;
    public static final int ELEMENT_NORMAL = 4;
    public static final int ELEMENT_INDEX = 5;

    public static final int ELEMENT_TOTAL_COUNT = 6;

    public static final int[] ELEMENTS_LIST_INSTANCE_BASED = new int[]{ ELEMENT_MODEL, ELEMENT_POSITION, ELEMENT_COLOR, ELEMENT_TEXCOORD, ELEMENT_NORMAL };
    public static final int[] ELEMENT_BYTES = new int[]{ ELEMENT_BYTES_MODEL, ELEMENT_BYTES_POSITION, ELEMENT_BYTES_COLOR, ELEMENT_BYTES_TEXCOORD, ELEMENT_BYTES_NORMAL, ELEMENT_BYTES_INDEX };
    public static final int[] UNIT_SIZES = new int[]{ UNIT_SIZE_MODEL, UNIT_SIZE_POSITION, UNIT_SIZE_COLOR, UNIT_SIZE_TEXCOORD, UNIT_SIZE_NORMAL, UNIT_SIZE_INDEX };
    public static final int[] UNIT_BYTES = new int[]{ UNIT_BYTES_MODEL, UNIT_BYTES_POSITION, UNIT_BYTES_COLOR, UNIT_BYTES_TEXCOORD, UNIT_BYTES_NORMAL, UNIT_BYTES_INDEX };
    public static final int[] ELEMENT_GLDATA_TYPES = new int[]{ ELEMENT_GLDATA_TYPE_MODEL, ELEMENT_GLDATA_TYPE_POSITION, ELEMENT_GLDATA_TYPE_COLOR, ELEMENT_GLDATA_TYPE_TEXCOORD, ELEMENT_GLDATA_TYPE_NORMAL, ELEMENT_GLDATA_TYPE_INDEX };
    public static final String[] ELEMENT_NAMES = new String[]{ "MODEL", "POSITION", "COLOR", "TEXCOORD", "NORMAL", "INDEX" };

    public FSHub(){

    }

    public void initialize(Activity act){
        assemble(act, FSR.getRenderPasses());
    }

    public Automator createAutomator(int filecapacity, int scancapacity, int buffercapacity){
        return new Automator(filecapacity, scancapacity, buffercapacity);
    }

    protected abstract void assemble(Activity act, VLListType<FSRPass> targets);
    public abstract void destroy();

    public static class Automator{

        protected VLListType<FSM> files;
        protected VLListType<FSHScanner> entries;
        protected VLListType<FSVertexBuffer<?>> buffers;

        protected Automator(int filecapacity, int scancapacity, int buffercapacity){
            files = new VLListType<>(filecapacity, filecapacity);
            entries = new VLListType<>(scancapacity, scancapacity);
            buffers = new VLListType<>(buffercapacity, buffercapacity);
        }

        public void addFile(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize) throws IOException{
            FSM data = new FSM();
            data.loadFromFile(src, order, fullsizedposition, estimatedsize);

            files.add(data);
        }

        public void addScanner(FSHScanner entry){
            entries.add(entry);
        }

        public void addBuffer(FSVertexBuffer<?> buffer){
            buffers.add(buffer);
        }

        public void scan(int debug){
            int entrysize = entries.size();

            if(debug > FSControl.DEBUG_DISABLED){
                VLDebug.recreate();

                FSHScanner entry;
                boolean found;

                VLDebug.printDirect("[Assembler Check Stage]\n");

                for(int i = 0; i < entrysize; i++){
                    entry = entries.get(i);
                    entry.assembler.checkDebug();

                    VLDebug.append("Scanner[");
                    VLDebug.append(entry.name);
                    VLDebug.append("] [Invalid Assembler Configuration]\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(VLDebug.get(), null);
                }

                VLDebug.printDirect("\n[DONE]\n");
                VLDebug.printDirect("[Build Stage]\n");

                int filesize = files.size();

                for(int i = 0; i < filesize; i++){
                    VLListType<FSM.Data> data = files.get(i).data;
                    int datasize = data.size();

                    for(int i2 = 0; i2 < datasize; i2++){
                        FSM.Data d = data.get(i2);

                        for(int i3 = 0; i3 < entrysize; i3++){
                            entry = entries.get(i3);
                            FSMesh<?> mesh = entry.mesh;
                            found = false;

                            try{
                                if(entry.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                                    VLDebug.append("Built[");
                                    VLDebug.append(i2);
                                    VLDebug.append("] keyword[");
                                    VLDebug.append(entry.name);
                                    VLDebug.append("] name[");
                                    VLDebug.append(d.name);
                                    VLDebug.append("] ");

                                    found = true;
                                }

                                if(found && mesh.size() > 1){
                                    FSInstance instance1 = mesh.first();
                                    FSInstance instance2 = mesh.get(mesh.size() - 1);

                                    if(instance1.positions().size() != instance2.positions().size()){
                                        VLDebug.printD();
                                        VLDebug.append("[WARNING] [Attempting to do instancing on meshes with different vertex characteristics] [Instance1_Vertex_Size[");
                                        VLDebug.append(instance1.positions().size());
                                        VLDebug.append("] Instance2_Vertex_Size[");
                                        VLDebug.append(instance2.positions().size());
                                        VLDebug.append("]");
                                        VLDebug.printE();
                                    }
                                }

                            }catch(Exception ex){
                                VLDebug.append("Error building \"");
                                VLDebug.append(entry.name);
                                VLDebug.append("\"\n[Assembler Configuration]\n");

                                entry.assembler.stringify(VLDebug.get(), null);
                                VLDebug.printE();

                                throw new RuntimeException(ex);
                            }

                            if(found){
                                VLDebug.printD();
                            }
                        }
                    }
                }

                VLDebug.printDirect("[DONE]\n");
                VLDebug.printD();
                VLDebug.printDirect("[Checking Scan Results]\n");

                for(int i = 0; i < entrysize; i++){
                    entry = entries.get(i);

                    if(entry.mesh.size() == 0){
                        VLDebug.append("Scan incomplete : found no instance for mesh with keyword \"");
                        VLDebug.append(entry.name);
                        VLDebug.append("\".\n");
                        VLDebug.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(VLDebug.get(), null);

                        VLDebug.printE();
                        throw new RuntimeException("Mesh Scan Error");
                    }

                    entry.scanComplete();
                }

                VLDebug.printDirect("[DONE]\n");
                VLDebug.printD();

            }else{
                int filesize = files.size();

                for(int i = 0; i < filesize; i++){
                    VLListType<FSM.Data> datalist = files.get(i).data;
                    int datasize = datalist.size();

                    for(int i2 = 0; i2 < datasize; i2++){
                        FSM.Data data = datalist.get(i2);

                        for(int i3 = 0; i3 < entrysize; i3++){
                            entries.get(i3).scan(this, data);
                        }
                    }
                }

                for(int i = 0; i < entrysize; i++){
                    entries.get(i).scanComplete();
                }
            }
        }

        public void buffer(int debug){
            int size = entries.size();

            if(debug > FSControl.DEBUG_DISABLED){
                VLDebug.recreate();
                FSHScanner entry;

                int buffersize = buffers.size();

                VLDebug.printDirect("[Buffering Stage]\n");

                for(int i = 0; i < size; i++){
                    entry = entries.get(i);

                    VLDebug.append("accountingForMesh[");
                    VLDebug.append(i + 1);
                    VLDebug.append("/");
                    VLDebug.append(size);
                    VLDebug.append("]\n");

                    if(debug >= FSControl.DEBUG_FULL){
                        entry.debugInfo();
                    }

                    try{
                        entry.accountForBufferSize();

                    }catch(Exception ex){
                        VLDebug.append("Error accounting for buffer size \"");
                        VLDebug.append(entry.name);
                        VLDebug.append("\"\n");
                        VLDebug.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(VLDebug.get(), null);
                        VLDebug.printE();

                        throw new RuntimeException(ex);
                    }

                    VLDebug.printD();
                }

                try{
                    VLDebug.append("Initializing buffers...");
                    FSVertexBuffer<?> buffer;

                    for(int i = 0; i < buffersize; i++){
                        buffer = buffers.get(i);
                        buffer.provider().initialize(ByteOrder.nativeOrder());
                        buffer.initialize();
                    }

                    VLDebug.append("[SUCCESS]\n");

                }catch(Exception ex){
                    VLDebug.append("[FAILED]");
                    VLDebug.printE();
                    throw new RuntimeException("Failed to initialize buffers", ex);
                }

                for(int i = 0; i < size; i++){
                    entry = entries.get(i);

                    VLDebug.append("Buffering[");
                    VLDebug.append(i + 1);
                    VLDebug.append("/");
                    VLDebug.append(size);
                    VLDebug.append("]\n");

                    if(debug >= FSControl.DEBUG_FULL){
                        entry.debugInfo();
                    }

                    try{
                        entry.bufferDebugAndFinish();

                    }catch(Exception ex){
                        VLDebug.append("Error buffering \"");
                        VLDebug.append(entry.name);
                        VLDebug.append("\"\n");
                        VLDebug.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(VLDebug.get(), null);
                        VLDebug.printE();

                        throw new RuntimeException(ex);
                    }

                    VLDebug.printD();
                }

                try{
                    VLDebug.append("Uploading buffers...");

                    for(int i = 0; i < buffersize; i++){
                        buffers.get(i).upload();
                    }

                    VLDebug.append("[SUCCESS]\n");

                }catch(Exception ex){
                    VLDebug.append("[FAILED]");
                    VLDebug.printE();
                    throw new RuntimeException("Failed to upload buffers", ex);
                }

                for(int i = 0; i < size; i++){
                    entries.get(i).bufferComplete();
                }

                VLDebug.printDirect("[DONE]\n");
                VLDebug.printD();

            }else{
                int buffersize = buffers.size();

                for(int i = 0; i < size; i++){
                    entries.get(i).accountForBufferSize();
                }
                for(int i = 0; i < buffersize; i++){
                    FSVertexBuffer<?>  buffer = buffers.get(i);
                    buffer.provider().initialize(ByteOrder.nativeOrder());
                    buffer.initialize();
                }
                for(int i = 0; i < size; i++){
                    entries.get(i).bufferAndFinish();
                }
                for(int i = 0; i < buffersize; i++){
                    buffers.get(i).upload();
                }
                for(int i = 0; i < size; i++){
                    entries.get(i).bufferComplete();
                }
            }
        }
    }
}