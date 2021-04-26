package com.nurverek.firestorm;

import android.content.Context;
import android.opengl.GLES32;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLListType;
import vanguard.VLLog;

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

    public void initialize(){
        assemble(FSControl.getContext(), FSR.getRenderPasses());
    }

    public Automator createAutomator(int filecapacity, int scancapacity, int buffercapacity){
        return new Automator(filecapacity, scancapacity, buffercapacity);
    }

    protected abstract void assemble(Context context, VLListType<FSRPass> targets);

    public abstract void paused();
    public abstract void resumed();
    public abstract void destroy();

    public static class Automator{

        protected VLListType<FSM> files;
        protected VLListType<FSHScanner> entries;
        protected VLListType<FSVertexBuffer<?>> buffers;
        protected VLLog log;

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
                log = new VLLog(new String[]{
                        FSControl.LOGTAG, getClass().getSimpleName()
                });

                FSHScanner entry;
                boolean found;

                log.printInfo("[Assembler Check Stage]\n");

                for(int i = 0; i < entrysize; i++){
                    entry = entries.get(i);
                    entry.assembler.checkDebug();

                    log.append("Scanner[");
                    log.append(entry.name);
                    log.append("] [Invalid Assembler Configuration]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                }

                log.printInfo("\n[DONE]\n");
                log.printInfo("[Build Stage]\n");

                int filesize = files.size();

                for(int i = 0; i < filesize; i++){
                    VLListType<FSM.Data> data = files.get(i).data;
                    int datasize = data.size();

                    for(int i2 = 0; i2 < datasize; i2++){
                        FSM.Data d = data.get(i2);

                        for(int i3 = 0; i3 < entrysize; i3++){
                            entry = entries.get(i3);
                            FSMesh mesh = entry.mesh;
                            found = false;

                            try{
                                if(entry.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                                    log.append("Built[");
                                    log.append(i2);
                                    log.append("] keyword[");
                                    log.append(entry.name);
                                    log.append("] name[");
                                    log.append(d.name);
                                    log.append("] ");

                                    found = true;
                                }

                                if(found && mesh.size() > 1){
                                    FSInstance instance1 = mesh.first();
                                    FSInstance instance2 = mesh.get(mesh.size() - 1);

                                    if(instance1.positions().size() != instance2.positions().size()){
                                        log.printInfo();
                                        log.append("[WARNING] [Attempting to do instancing on meshes with different vertex characteristics] [Instance1_Vertex_Size[");
                                        log.append(instance1.positions().size());
                                        log.append("] Instance2_Vertex_Size[");
                                        log.append(instance2.positions().size());
                                        log.append("]");
                                        log.printError();
                                    }
                                }

                            }catch(Exception ex){
                                log.append("Error building \"");
                                log.append(entry.name);
                                log.append("\"\n[Assembler Configuration]\n");

                                entry.assembler.stringify(log.get(), null);
                                log.printError();

                                throw new RuntimeException(ex);
                            }

                            if(found){
                                log.printInfo();
                            }
                        }
                    }
                }

                log.printInfo("[DONE]\n");
                log.printInfo("[Checking Scan Results]\n");

                for(int i = 0; i < entrysize; i++){
                    entry = entries.get(i);

                    if(entry.mesh.size() == 0){
                        log.append("Scan incomplete : found no instance for mesh with keyword \"");
                        log.append(entry.name);
                        log.append("\".\n");
                        log.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(log.get(), null);

                        log.printError();
                        throw new RuntimeException("Mesh Scan Error");
                    }

                    entry.scanComplete();
                }

                log.printInfo("[DONE]\n");

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
                log = new VLLog(new String[]{
                        FSControl.LOGTAG, getClass().getSimpleName()
                });

                FSHScanner entry;

                int buffersize = buffers.size();

                log.printInfo("[Buffering Stage]\n");

                for(int i = 0; i < size; i++){
                    entry = entries.get(i);

                    log.append("accountingForMesh[");
                    log.append(i + 1);
                    log.append("/");
                    log.append(size);
                    log.append("]\n");

                    if(debug >= FSControl.DEBUG_FULL){
                        entry.debugInfo(log);
                    }

                    try{
                        entry.accountForBufferSize();

                    }catch(Exception ex){
                        log.append("Error accounting for buffer size \"");
                        log.append(entry.name);
                        log.append("\"\n");
                        log.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(log.get(), null);
                        log.printError();

                        throw new RuntimeException(ex);
                    }

                    log.printInfo();
                }

                try{
                    log.append("Initializing buffers...");
                    FSVertexBuffer<?> buffer;

                    for(int i = 0; i < buffersize; i++){
                        buffer = buffers.get(i);
                        buffer.provider().initialize(ByteOrder.nativeOrder());
                        buffer.initialize();
                    }

                    log.append("[SUCCESS]\n");

                }catch(Exception ex){
                    log.append("[FAILED]");
                    log.printError();
                    throw new RuntimeException("Failed to initialize buffers", ex);
                }

                for(int i = 0; i < size; i++){
                    entry = entries.get(i);

                    log.append("Buffering[");
                    log.append(i + 1);
                    log.append("/");
                    log.append(size);
                    log.append("]\n");

                    if(debug >= FSControl.DEBUG_FULL){
                        entry.debugInfo(log);
                    }

                    try{
                        entry.bufferDebugAndFinish(log);

                    }catch(Exception ex){
                        log.append("Error buffering \"");
                        log.append(entry.name);
                        log.append("\"\n");
                        log.append("[Assembler Configuration]\n");

                        entry.assembler.stringify(log.get(), null);
                        log.printError();

                        throw new RuntimeException(ex);
                    }

                    log.printInfo();
                }

                try{
                    log.append("Uploading buffers...");

                    for(int i = 0; i < buffersize; i++){
                        buffers.get(i).upload();
                    }

                    log.append("[SUCCESS]\n");

                }catch(Exception ex){
                    log.append("[FAILED]");
                    log.printError();
                    throw new RuntimeException("Failed to upload buffers", ex);
                }

                for(int i = 0; i < size; i++){
                    entries.get(i).bufferComplete();
                }

                log.printInfo("[DONE]\n");

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