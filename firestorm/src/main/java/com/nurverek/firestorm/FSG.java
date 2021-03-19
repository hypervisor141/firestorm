package com.nurverek.firestorm;

import android.app.Activity;
import android.opengl.GLES32;

import vanguard.VLArrayFloat;
import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLVTypeManager;
import vanguard.VLVTypeRunner;

public abstract class FSG<MANAGER extends VLVTypeManager<? extends VLVTypeRunner>>{

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

    public static final int[] ELEMENT_BYTES = new int[]{ ELEMENT_BYTES_MODEL, ELEMENT_BYTES_POSITION, ELEMENT_BYTES_COLOR, ELEMENT_BYTES_TEXCOORD, ELEMENT_BYTES_NORMAL, ELEMENT_BYTES_INDEX };
    public static final int[] UNIT_SIZES = new int[]{ UNIT_SIZE_MODEL, UNIT_SIZE_POSITION, UNIT_SIZE_COLOR, UNIT_SIZE_TEXCOORD, UNIT_SIZE_NORMAL, UNIT_SIZE_INDEX };
    public static final int[] UNIT_BYTES = new int[]{ UNIT_BYTES_MODEL, UNIT_BYTES_POSITION, UNIT_BYTES_COLOR, UNIT_BYTES_TEXCOORD, UNIT_BYTES_NORMAL, UNIT_BYTES_INDEX };
    public static final int[] ELEMENT_GLDATA_TYPES = new int[]{ ELEMENT_GLDATA_TYPE_MODEL, ELEMENT_GLDATA_TYPE_POSITION, ELEMENT_GLDATA_TYPE_COLOR, ELEMENT_GLDATA_TYPE_TEXCOORD, ELEMENT_GLDATA_TYPE_NORMAL, ELEMENT_GLDATA_TYPE_INDEX };
    public static final String[] ELEMENT_NAMES = new String[]{ "MODEL", "POSITION", "COLOR", "TEXCOORD", "NORMAL", "INDEX" };

    private VLListType<FSP> programs;
    private VLListType<FSVertexBuffer<VLBuffer<?, ?>>> buffers;

    private MANAGER rootmanager;

    private long id;
    private boolean touchable;

    public FSG(int programcapacity, int buffercapacity, MANAGER rootmanager){
        programs = new VLListType<>(programcapacity, programcapacity);
        buffers = new VLListType<>(buffercapacity, buffercapacity);

        this.rootmanager = rootmanager;
        id = FSRFrames.getNextID();
        touchable = true;
    }

    public void initialize(Activity act){
        assemble(act, buffers, programs, FSR.getRenderPasses());
        layout(rootmanager);
    }

    protected abstract void assemble(Activity act, VLListType<FSVertexBuffer<VLBuffer<?, ?>>> buffers, VLListType<FSP> programs, VLListType<FSRPass> targets);
    protected abstract void layout(MANAGER rootmanager);

    protected VLArrayFloat createColorArray(float[] basecolor, int count){
        float[] colors = new float[count * UNIT_SIZE_COLOR];

        for(int i = 0; i < colors.length; i += UNIT_SIZE_COLOR){
            colors[i] = basecolor[0];
            colors[i + 1] = basecolor[1];
            colors[i + 2] = basecolor[2];
            colors[i + 3] = basecolor[3];
        }

        return new VLArrayFloat(colors);
    }

    public long id(){
        return id;
    }

    public void touchable(boolean t){
        touchable = t;
    }

    public VLListType<FSP> programs(){
        return programs;
    }

    public VLListType<FSVertexBuffer<VLBuffer<?, ?>>> buffers(){
        return buffers;
    }

    public MANAGER rootManager(){
        return rootmanager;
    }

    public boolean touchable(){
        return touchable;
    }

    public final void destroy(){
        destroyAssets();

        id = -1;
        touchable = false;

        int size = this.programs.size();

        for(int i = 0; i < size; i++){
            programs.get(i).destroy();
        }

        size = buffers.size();

        for(int i = 0; i < size; i++){
            buffers.get(i).destroy();
        }

        this.programs = null;
        buffers = null;
        rootmanager = null;
    }

    protected abstract void destroyAssets();
}