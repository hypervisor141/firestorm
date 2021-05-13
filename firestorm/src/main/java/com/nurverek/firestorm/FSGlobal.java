package com.nurverek.firestorm;

import android.opengl.GLES32;

public class FSGlobal{

    protected FSGlobal(){}

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

        register("model", ELEMENT_MODEL, GLES32.GL_FLOAT, Float.SIZE / 8, 16);
        register("position", ELEMENT_POSITION, GLES32.GL_FLOAT, Float.SIZE / 8, 4);
        register("color", ELEMENT_COLOR, GLES32.GL_FLOAT, Float.SIZE / 8, 4);
        register("texturecoordinates", ELEMENT_TEXCOORD, GLES32.GL_FLOAT, Float.SIZE / 8, 2);
        register("normal", ELEMENT_NORMAL, GLES32.GL_FLOAT, Float.SIZE / 8, 3);
        register("index", ELEMENT_INDEX, GLES32.GL_UNSIGNED_SHORT, Short.SIZE / 8, 1);
    }

    public static void register(String name, int element, int gltype, int bytes, int unitsize){
        NAMES[element] = name;
        ELEMENTS[element] = element;
        GLTYPES[element] = gltype;
        BYTES[element] = bytes;
        UNIT_SIZES[element] = unitsize;
        UNIT_BYTES[element] = unitsize * bytes;
    }

    protected static void destroy(){
        if(!FSControl.getDestroyOnPause()){
            NAMES = null;
            ELEMENTS = null;
            GLTYPES = null;
            BYTES = null;
            UNIT_SIZES = null;
            UNIT_BYTES = null;

            COUNT = -1;
        }
    }
}
