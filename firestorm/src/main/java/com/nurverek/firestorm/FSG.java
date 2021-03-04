package com.nurverek.firestorm;

import android.app.Activity;
import android.opengl.GLES32;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLSyncTree;
import com.nurverek.vanguard.VLSyncType;
import com.nurverek.vanguard.VLVManager;
import com.nurverek.vanguard.VLVTypeManager;
import com.nurverek.vanguard.VLVTypeRunner;

public abstract class FSG<MANAGER extends VLVTypeManager<? extends VLVTypeRunner>, SYNCER extends VLSyncTree<? extends VLSyncType<?>>, BUFFERMANAGER extends FSBufferManager>{

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

    private VLListType<VLListType<FSP>> programsets;
    private BUFFERMANAGER buffermanager;
    private MANAGER rootmanager;
    private SYNCER syncer;

    private long id;
    private boolean touchable;

    public FSG(int programsetsize, MANAGER rootmanager, SYNCER syncer, BUFFERMANAGER buffermanager){
        this.rootmanager = rootmanager;
        this.syncer = syncer;
        this.buffermanager = buffermanager;

        programsets = new VLListType<>(5, 20);
        id = FSRControl.getNextID();
        touchable = true;

        addProgramSets(programsetsize);
    }

    public void initialize(Activity act){
        assemble(act);
        layout(rootmanager, syncer);
    }

    protected abstract void assemble(Activity act);
    protected abstract void layout(MANAGER rootmanager, SYNCER syncer);
    protected abstract void update(int passindex, int programsetindex);

    public void draw(int passindex, int programsetindex){
        VLListType<FSP> p = programsets.get(programsetindex);
        int size = p.size();

        for(int i = 0; i < size; i++){
            p.get(i).draw(passindex);
        }
    }

    protected void postFramSwap(int passindex){}

    public int next(){
        return rootmanager.next(syncer);
    }

    public VLArrayFloat createColorArray(float[] basecolor, int count){
        float[] colors = new float[count * 4];

        for(int i = 0; i < colors.length; i += 4){
            colors[i] = basecolor[0];
            colors[i + 1] = basecolor[1];
            colors[i + 2] = basecolor[2];
            colors[i + 3] = basecolor[3];
        }

        return new VLArrayFloat(colors);
    }

    public void addProgramSets(int count){
        for(int i = 0; i < count; i++){
            programsets.add(new VLListType<FSP>(5, 10));
        }
    }

    public void touchable(boolean t){
        touchable = t;
    }

    public VLListType<FSP> programSet(int passindex){
        return programsets.get(passindex);
    }

    public VLListType<VLListType<FSP>> programSets(){
        return programsets;
    }

    public MANAGER rootManager(){
        return rootmanager;
    }

    public SYNCER syncer(){
        return syncer;
    }

    public BUFFERMANAGER bufferManager(){
        return buffermanager;
    }

    public long id(){
        return id;
    }

    public int programsSize(){
        return programsets.size();
    }

    public boolean touchable(){
        return touchable;
    }

    public final void destroy(){
        destroyAssets();

        VLListType<FSP> programs;

        for(int i = 0; i < programsets.size(); i++){
            programs = programsets.get(i);

            for(int i2 = 0; i2 < programs.size(); i2++){
                programs.get(i2).destroy();
            }
        }

        for(int i = 0; i < buffermanager.size(); i++){
            buffermanager.get(i).release();
        }

        programsets = null;
        buffermanager = null;
        rootmanager = null;

        touchable = false;
        id = -1;
    }

    protected abstract void destroyAssets();
}