package com.nurverek.firestorm;

import android.opengl.GLES32;

public class FSGlobal{

    private FSGlobal(){}

    public static final int ELEMENT_MODEL = 0;
    public static final int ELEMENT_POSITION = 1;
    public static final int ELEMENT_COLOR = 2;
    public static final int ELEMENT_TEXCOORD = 3;
    public static final int ELEMENT_NORMAL = 4;
    public static final int ELEMENT_INDEX = 5;

    public static String[] NAMES;
    public static int[] ELEMENTS;
    public static int[] GLTYPES;
    public static int[] BYTES;
    public static int[] UNIT_SIZES;
    public static int[] UNIT_BYTES;

    public static int DEFAULT_ELEMENT_COUNT = 6;
    public static int CUSTOM_ELEMENT_OFFSET = DEFAULT_ELEMENT_COUNT;
    public static int COUNT;

    protected static void initialize(int extraelementscount){
        COUNT = DEFAULT_ELEMENT_COUNT + extraelementscount;

        NAMES = new String[COUNT];
        ELEMENTS = new int[COUNT];
        GLTYPES = new int[COUNT];
        BYTES = new int[COUNT];
        UNIT_SIZES = new int[COUNT];
        UNIT_BYTES = new int[COUNT];

        register(ELEMENT_MODEL, 16, Float.SIZE / 8, GLES32.GL_FLOAT, "model");
        register(ELEMENT_POSITION, 4, Float.SIZE / 8, GLES32.GL_FLOAT, "position");
        register(ELEMENT_COLOR, 4, Float.SIZE / 8, GLES32.GL_FLOAT, "color");
        register(ELEMENT_TEXCOORD, 2, Float.SIZE / 8, GLES32.GL_FLOAT, "texturecoordinates");
        register(ELEMENT_NORMAL, 3, Float.SIZE / 8, GLES32.GL_FLOAT, "normal");
        register(ELEMENT_INDEX, 1, Short.SIZE / 8, GLES32.GL_UNSIGNED_SHORT, "index");
    }

    public static void register(int element, int unitsize, int bytes, int gltype, String name){
        NAMES[element] = name;
        GLTYPES[element] = gltype;
        BYTES[element] = bytes;
        UNIT_SIZES[element] = unitsize;
        UNIT_BYTES[element] = unitsize * bytes;
    }

    protected static void destroy(){
        if(FSControl.getDestroyOnPause()){
            NAMES = null;
            GLTYPES = null;
            BYTES = null;
            UNIT_SIZES = null;
            UNIT_BYTES = null;

            COUNT = -1;
        }
    }
}
