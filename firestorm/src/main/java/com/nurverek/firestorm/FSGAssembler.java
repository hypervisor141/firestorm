package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArray;
import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLListFloat;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLStringify;
import com.nurverek.vanguard.VLV;

public class FSGAssembler implements VLStringify{

    public boolean SYNC_MODELMATRIX_AND_MODELARRAY = false;
    public boolean SYNC_MODELARRAY_AND_BUFFER = false;
    public boolean SYNC_MODELARRAY_AND_SCHEMATICS = false;
    public boolean SYNC_POSITION_AND_BUFFER = false;
    public boolean SYNC_POSITION_AND_SCHEMATICS = false;
    public boolean SYNC_COLOR_AND_BUFFER = false;
    public boolean SYNC_TEXCOORD_AND_BUFFER = false;
    public boolean SYNC_NORMAL_AND_BUFFER = false;
    public boolean SYNC_INDICES_AND_BUFFER = false;

    public boolean LOAD_MODELS = false;
    public boolean LOAD_POSITIONS = false;
    public boolean LOAD_TEXCOORDS = false;
    public boolean LOAD_COLORS = false;
    public boolean LOAD_NORMALS = false;
    public boolean LOAD_INDICES = false;

    public boolean INSTANCE_SHARE_POSITIONS = false;
    public boolean INSTANCE_SHARE_TEXCOORDS = false;
    public boolean INSTANCE_SHARE_COLORS = false;
    public boolean INSTANCE_SHARE_NORMALS = false;

    public boolean CONVERT_POSITIONS_TO_MODELARRAYS = false;
    public boolean DRAW_MODE_INDEXED = false;

    protected BufferStep[] buffersteps;
    private final VLListType<BuildStep> firstfuncs;
    private final VLListType<BuildStep> restfuncs;

    public FSGAssembler(){
        firstfuncs = new VLListType<>(10, 20);
        restfuncs = new VLListType<>(10, 20);
        buffersteps = new BufferStep[FSG.ELEMENT_TOTAL_COUNT];

        setDefaultAll();
    }


    public void setDefaultAll(){
        for(int i = 0; i < buffersteps.length; i++){
            buffersteps[i] = BUFFER_NO_SYNC;
        }

        SYNC_MODELMATRIX_AND_MODELARRAY = false;
        SYNC_MODELARRAY_AND_BUFFER = true;
        SYNC_MODELARRAY_AND_SCHEMATICS = true;
        SYNC_POSITION_AND_SCHEMATICS = true;
        SYNC_POSITION_AND_BUFFER = true;
        SYNC_COLOR_AND_BUFFER = true;
        SYNC_TEXCOORD_AND_BUFFER = true;
        SYNC_NORMAL_AND_BUFFER = true;
        SYNC_INDICES_AND_BUFFER = true;

        LOAD_MODELS = true;
        LOAD_POSITIONS = true;
        LOAD_COLORS = true;
        LOAD_TEXCOORDS = true;
        LOAD_NORMALS = true;
        LOAD_INDICES = true;

        INSTANCE_SHARE_POSITIONS = false;
        INSTANCE_SHARE_COLORS = false;
        INSTANCE_SHARE_TEXCOORDS = false;
        INSTANCE_SHARE_NORMALS = false;

        CONVERT_POSITIONS_TO_MODELARRAYS = true;

        DRAW_MODE_INDEXED = true;
    }

    public void configure(){
        restfuncs.clear();
        firstfuncs.clear();

        configureModels();
        configurePositions();
        configureColors();
        configureTexCoords();
        configureNormals();
    }

    private void configureModels(){
        if(LOAD_MODELS){
            firstfuncs.add(MODEL_INITIALIZE);
            restfuncs.add(MODEL_INITIALIZE);

            if(SYNC_MODELMATRIX_AND_MODELARRAY){
                firstfuncs.add(MODEL_SYNC_MODELMATRIX_AND_MODELARRAY);
                restfuncs.add(MODEL_SYNC_MODELMATRIX_AND_MODELARRAY);
            }
            if(SYNC_MODELARRAY_AND_BUFFER){
                buffersteps[FSG.ELEMENT_MODEL] = BUFFER_SYNC;
            }
            if(SYNC_MODELARRAY_AND_SCHEMATICS){
                firstfuncs.add(MODEL_SYNC_MODELARRAY_AND_SCHEMATICS);
                restfuncs.add(MODEL_SYNC_MODELARRAY_AND_SCHEMATICS);
            }
        }
    }

    private void configurePositions(){
        if(LOAD_POSITIONS){
            firstfuncs.add(POSITION_MATRIX_DEFAULT);
            firstfuncs.add(POSITION_INIT_SCHEMATICS);

            if(CONVERT_POSITIONS_TO_MODELARRAYS){
                firstfuncs.add(POSITION_BUILD_MODELMATRIX_AND_ALL_ELSE);
            }

            if(INSTANCE_SHARE_POSITIONS){
                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    restfuncs.add(POSITION_MATRIX_DEFAULT);

                    if(!DRAW_MODE_INDEXED){
                        restfuncs.add(POSITION_UNINDEX);
                    }

                    restfuncs.add(POSITION_INIT_SCHEMATICS);
                    restfuncs.add(POSITION_BUILD_MODELMATRIX);
                }

                restfuncs.add(POSITION_MATRIX_SHARED);
                restfuncs.add(POSITION_SHARE_SCHEMATICS);

            }else{
                restfuncs.add(POSITION_MATRIX_DEFAULT);
                restfuncs.add(POSITION_INIT_SCHEMATICS);

                if(!DRAW_MODE_INDEXED){
                    restfuncs.add(POSITION_UNINDEX);
                }
                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    restfuncs.add(POSITION_BUILD_MODELMATRIX_AND_ALL_ELSE);
                }
            }
        }

        if(SYNC_POSITION_AND_BUFFER){
            buffersteps[FSG.ELEMENT_POSITION] = BUFFER_SYNC;
        }
        if(SYNC_POSITION_AND_SCHEMATICS){
            firstfuncs.add(POSITION_SYNC_WITH_SCHEMATICS);
            restfuncs.add(POSITION_SYNC_WITH_SCHEMATICS);
        }
    }

    private void configureColors(){
        if(LOAD_COLORS){
            BuildStep step;

            if(DRAW_MODE_INDEXED){
                step = COLOR_FILE_LOADED_INDEXED;

            }else{
                step = COLOR_FILE_LOADED_NONE_INDEXED;
            }

            firstfuncs.add(step);

            if(INSTANCE_SHARE_COLORS){
                restfuncs.add(COLOR_SHARE);

            }else{
                restfuncs.add(step);
            }
        }

        if(SYNC_COLOR_AND_BUFFER){
            buffersteps[FSG.ELEMENT_COLOR] = BUFFER_SYNC;
        }
    }

    private void configureTexCoords(){
        if(LOAD_TEXCOORDS){
            BuildStep step;

            if(DRAW_MODE_INDEXED){
                step = TEXTURE_INDEXED;

            }else{
                step = TEXTURE_NONE_INDEXED;
            }

            firstfuncs.add(step);

            if(INSTANCE_SHARE_TEXCOORDS){
                restfuncs.add(TEXTURE_SHARE);

            }else{
                restfuncs.add(step);
            }
        }

        if(SYNC_TEXCOORD_AND_BUFFER){
            buffersteps[FSG.ELEMENT_TEXCOORD] = BUFFER_SYNC;
        }
    }

    private void configureNormals(){
        if(LOAD_NORMALS){
            BuildStep step;

            if(DRAW_MODE_INDEXED){
                step = NORMAL_INDEXED;

            }else{
                step = NORMAL_NONE_INDEXED;
            }

            firstfuncs.add(step);

            if(INSTANCE_SHARE_NORMALS){
                restfuncs.add(NORMAL_SHARE);

            }else{
                restfuncs.add(step);
            }
        }

        if(SYNC_NORMAL_AND_BUFFER){
            buffersteps[FSG.ELEMENT_NORMAL] = BUFFER_SYNC;
        }
    }

    private void buildmodelClusterFromSchematics(FSInstance instance){
        FSSchematics schematics = instance.schematics;
        FSMatrixModel set = instance.modelMatrix();

        set.addRowTranslation(0, new VLV(schematics.rawCentroidX()), new VLV(schematics.rawCentroidY()), new VLV(schematics.rawCentroidZ()));
        set.sync();
    }

    private void centralizePositions(FSInstance instance){
        float[] positions = instance.positions().provider();
        FSSchematics schematics = instance.schematics;

        float x = schematics.rawCentroidX();
        float y = schematics.rawCentroidY();
        float z = schematics.rawCentroidZ();

        int size = positions.length;

        for(int i = 0; i < size; i += FSG.UNIT_SIZE_POSITION){
            positions[i] = positions[i] - x;
            positions[i + 1] = positions[i + 1] - y;
            positions[i + 2] = positions[i + 2] - z;
        }
    }

    protected void unIndexPositions(FSInstance.Data data, short[] indices){
        float[] positions = data.positions().provider();
        VLListFloat converted = new VLListFloat(positions.length, positions.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int pindex = indices[i2] * FSG.UNIT_SIZE_POSITION;

            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
        }

        converted.restrictSize();
        data.positions().provider(converted.array());
    }

    protected void unIndexColors(FSInstance.Data data, short[] indices){
        float[] colors = data.colors().provider();
        VLListFloat converted = new VLListFloat(colors.length, colors.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int cindex = indices[i2] * FSG.UNIT_SIZE_COLOR;

            converted.add(colors[cindex]);
            converted.add(colors[cindex + 1]);
            converted.add(colors[cindex + 2]);
            converted.add(colors[cindex + 3]);
        }

        converted.restrictSize();
        data.colors().provider(converted.array());
    }

    protected void unIndexTexCoords(FSInstance.Data data, short[] indices){
        float[] texcoords = data.texCoords().provider();
        VLListFloat converted = new VLListFloat(texcoords.length, texcoords.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int tindex = indices[i2] * FSG.UNIT_SIZE_TEXCOORD;

            converted.add(texcoords[tindex]);
            converted.add(texcoords[tindex + 1]);
        }

        converted.restrictSize();
        data.texCoords().provider(converted.array());
    }

    protected void unIndexNormals(FSInstance.Data data, short[] indices){
        float[] normals = data.normals().provider();
        VLListFloat converted = new VLListFloat(normals.length, normals.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int nindex = indices[i2] * FSG.UNIT_SIZE_NORMAL;

            converted.add(normals[nindex]);
            converted.add(normals[nindex + 1]);
            converted.add(normals[nindex + 2]);
        }

        converted.restrictSize();
        data.normals().provider(converted.array());
    }

    protected final void buildFirst(FSInstance instance, FSGScanner scanner, FSM.Data fsm){
        operate(firstfuncs, instance, scanner, fsm);
    }

    protected final void buildRest(FSInstance instance, FSGScanner scanner, FSM.Data fsm){
        operate(restfuncs, instance, scanner, fsm);
    }

    protected final BufferStep bufferFunc(int element){
        return buffersteps[element];
    }

    private final void operate(VLListType<BuildStep> funcs, FSInstance instance, FSGScanner scanner, FSM.Data fsm){
        FSMesh mesh = scanner.mesh;
        FSBufferLayout layout = scanner.layout;
        VLArrayShort indices = mesh.indices;

        int newindex = mesh.size();
        int funcsize = funcs.size();

        FSInstance.Data data = instance.data;

        for(int i = 0; i < funcsize; i++){
            funcs.get(i).process(this, mesh, indices, instance, data, fsm, layout);
        }
    }

    protected final boolean checkDebug(){
        return firstfuncs.size() == 0;
    }

    public final void stringify(StringBuilder info, Object hint){
        info.append("SYNC_MODELMATRIX_TO_MODELARRAY[");
        info.append(SYNC_MODELMATRIX_AND_MODELARRAY);
        info.append("]\nSYNC_MODELARRAY_AND_BUFFER[");
        info.append(SYNC_MODELARRAY_AND_BUFFER);
        info.append("]\nSYNC_MODELARRAY_AND_SCHEMATICS[");
        info.append(SYNC_MODELARRAY_AND_SCHEMATICS);
        info.append("]\nSYNC_POSITION_AND_SCHEMATICS[");
        info.append(SYNC_POSITION_AND_SCHEMATICS);
        info.append("]\nSYNC_POSITION_AND_BUFFER[");
        info.append(SYNC_POSITION_AND_BUFFER);
        info.append("]\nSYNC_COLOR_AND_BUFFER[");
        info.append(SYNC_COLOR_AND_BUFFER);
        info.append("]\nSYNC_TEXCOORD_AND_BUFFER[");
        info.append(SYNC_TEXCOORD_AND_BUFFER);
        info.append("]\nSYNC_NORMAL_AND_BUFFER[");
        info.append(SYNC_NORMAL_AND_BUFFER);
        info.append("]\nSYNC_INDICES_AND_BUFFER[");
        info.append(SYNC_INDICES_AND_BUFFER);
        info.append("]\nLOAD_MODELS[");
        info.append(LOAD_MODELS);
        info.append("]\nLOAD_POSITIONS[");
        info.append(LOAD_POSITIONS);
        info.append("]\nLOAD_TEXCOORDS[");
        info.append(LOAD_TEXCOORDS);
        info.append("]\nLOAD_COLORS[");
        info.append(LOAD_COLORS);
        info.append("]\nLOAD_NORMALS[");
        info.append(LOAD_NORMALS);
        info.append("]\nLOAD_INDICES[");
        info.append(LOAD_INDICES);
        info.append("]\nINSTANCE_SHARE_POSITIONS[");
        info.append(INSTANCE_SHARE_POSITIONS);
        info.append("]\nINSTANCE_SHARE_TEXCOORDS[");
        info.append(INSTANCE_SHARE_TEXCOORDS);
        info.append("]\nINSTANCE_SHARE_COLORS[");
        info.append(INSTANCE_SHARE_COLORS);
        info.append("]\nINSTANCE_SHARE_NORMALS[");
        info.append(INSTANCE_SHARE_NORMALS);
        info.append("]\nCONVERT_POSITIONS_TO_MODELARRAYS[");
        info.append(CONVERT_POSITIONS_TO_MODELARRAYS);
        info.append("]\nDRAW_MODE_INDEXED[");
        info.append(DRAW_MODE_INDEXED);
        info.append("]\n");
    }


    private static final BuildStep MODEL_INITIALIZE = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            instance.data.model(new FSArrayModel());
            instance.modelMatrix(new FSMatrixModel(2, 10));
        }
    };
    private static final BuildStep MODEL_SYNC_MODELMATRIX_AND_MODELARRAY = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            instance.modelMatrix().SYNCER.add(new FSArrayModel.Definition(instance.model(), true));
        }
    };
    private static final BuildStep MODEL_SYNC_MODELARRAY_AND_SCHEMATICS = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            instance.model().SYNCER.add(new FSSchematics.DefinitionModel(instance.schematics));
        }
    };


    private static final BuildStep POSITION_MATRIX_DEFAULT = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.positions(new VLArrayFloat(fsm.positions.array()));
        }
    };
    private static final BuildStep POSITION_MATRIX_SHARED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.positions(new VLArrayFloat(fsm.positions.array()));
        }
    };
    private static final BuildStep POSITION_UNINDEX = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            assembler.unIndexPositions(data, indices.provider());
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            assembler.buildmodelClusterFromSchematics(instance);
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX_AND_ALL_ELSE = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            assembler.buildmodelClusterFromSchematics(instance);
            assembler.centralizePositions(instance);
            instance.schematics.updateBoundaries();
        }
    };
    private static final BuildStep POSITION_INIT_SCHEMATICS = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            FSSchematics schematics = instance.schematics;
            schematics.initialize();
            schematics.updateBoundaries();
        }
    };
    private static final BuildStep POSITION_SHARE_SCHEMATICS = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            instance.schematics.updateBoundaries(mesh.first().schematics);
        }
    };
    private static final BuildStep POSITION_SYNC_WITH_SCHEMATICS = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            instance.positions().SYNCER.add(new FSSchematics.DefinitionPosition(instance.schematics));
        }
    };

    private static final BuildStep COLOR_FILE_LOADED_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.colors(new VLArrayFloat(fsm.colors.array()));
        }
    };
    private static final BuildStep COLOR_FILE_LOADED_NONE_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.colors(new VLArrayFloat(fsm.colors.array()));
            assembler.unIndexColors(data, indices.provider());
        }
    };
    private static final BuildStep COLOR_SHARE = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.colors(new VLArrayFloat(mesh.instance(0).colors().provider()));
        }
    };


    private static final BuildStep TEXTURE_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.texCoords(new VLArrayFloat(fsm.texcoords.array()));
        }
    };
    private static final BuildStep TEXTURE_NONE_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.texCoords(new VLArrayFloat(fsm.texcoords.array()));
            assembler.unIndexTexCoords(data, indices.provider());
        }
    };
    private static final BuildStep TEXTURE_SHARE = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.texCoords(new VLArrayFloat(mesh.instance(0).texCoords().provider()));
        }
    };


    private static final BuildStep NORMAL_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.normals(new VLArrayFloat(fsm.normals.array()));
        }
    };
    private static final BuildStep NORMAL_NONE_INDEXED = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.normals(new VLArrayFloat(fsm.normals.array()));
            assembler.unIndexNormals(data, indices.provider());
        }
    };
    private static final BuildStep NORMAL_SHARE = new BuildStep(){

        @Override
        protected void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout){
            data.normals(new VLArrayFloat(mesh.instance(0).normals().provider()));
        }
    };


    protected  static final BufferStep BUFFER_NO_SYNC = new BufferStep(){

        @Override
        protected void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array){
            manager.buffer(results, index, array);
        }

        @Override
        protected void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            manager.buffer(results, index, array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }
    };
    protected static final BufferStep BUFFER_SYNC = new BufferStep(){

        @Override
        protected void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array){
            manager.bufferSync(results, index, array);
        }

        @Override
        protected void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride){
            manager.bufferSync(results, index, array, arrayoffset, arraycount, unitoffset, unitsize, unitsubcount, stride);
        }
    };


    private abstract static class BuildStep{

        protected abstract void process(FSGAssembler assembler, FSMesh mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data data, FSM.Data fsm, FSBufferLayout layout);
    }

    protected abstract static class BufferStep{

        protected abstract void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array);
        protected abstract void process(FSBufferAddress results, FSBufferManager manager, int index, VLArray array, int arrayoffset, int arraycount, int unitoffset, int unitsize, int unitsubcount, int stride);
    }
}
