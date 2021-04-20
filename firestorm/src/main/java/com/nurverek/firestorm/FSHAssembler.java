package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLListFloat;
import vanguard.VLListType;
import vanguard.VLStringify;
import vanguard.VLV;

public class FSHAssembler implements VLStringify{

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

    public boolean FLIP_TEXTURE_U = false;
    public boolean FLIP_TEXTURE_V = false;

    public boolean CONVERT_POSITIONS_TO_MODELARRAYS = false;
    public boolean DRAW_MODE_INDEXED = false;

    private final VLListType<BuildStep> firstfuncs;
    private final VLListType<BuildStep> restfuncs;
    private final VLListType<BuildStep> customfunc;

    public FSHAssembler(int customstepcapacity){
        firstfuncs = new VLListType<>(10, 20);
        restfuncs = new VLListType<>(10, 20);
        customfunc = new VLListType<>(customstepcapacity, customstepcapacity);

        setDefaultAll();
    }

    public void setDefaultAll(){
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

        FLIP_TEXTURE_U = false;
        FLIP_TEXTURE_V = false;

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

    public VLListType<BuildStep> customSteps(){
        return customfunc;
    }

    private void configureModels(){
        if(LOAD_MODELS){
            firstfuncs.add(MODEL_INITIALIZE);
            restfuncs.add(MODEL_INITIALIZE);
        }
    }

    private void configurePositions(){
        if(LOAD_POSITIONS){
            firstfuncs.add(POSITION_MATRIX_DEFAULT);
            firstfuncs.add(POSITION_INIT_SCHEMATICS);

            if(CONVERT_POSITIONS_TO_MODELARRAYS){
                firstfuncs.add(POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE);
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
                    restfuncs.add(POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE);
                }
            }
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
                restfuncs.add(COLOR_SHARED);

            }else{
                restfuncs.add(step);
            }
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

            if(FLIP_TEXTURE_U){
                firstfuncs.add(TEXTURE_FLIP_U);
            }
            if(FLIP_TEXTURE_V){
                firstfuncs.add(TEXTURE_FLIP_V);
            }

            if(INSTANCE_SHARE_TEXCOORDS){
                restfuncs.add(TEXTURE_SHARED);

            }else{
                restfuncs.add(step);

                if(FLIP_TEXTURE_U){
                    restfuncs.add(TEXTURE_FLIP_U);
                }
                if(FLIP_TEXTURE_V){
                    restfuncs.add(TEXTURE_FLIP_V);
                }
            }
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
                restfuncs.add(NORMAL_SHARED);

            }else{
                restfuncs.add(step);
            }
        }
    }

    private void buildModelClusterFromSchematics(FSInstance instance){
        FSSchematics schematics = instance.schematics;

        instance.modelMatrix().addRowTranslation(0, new VLV(schematics.rawCentroidX()), new VLV(schematics.rawCentroidY()), new VLV(schematics.rawCentroidZ()));
        instance.model().transform(0, instance.modelmatrix, true);
    }

    private void centralizePositions(FSInstance instance){
        float[] positions = instance.positions().provider();
        FSSchematics schematics = instance.schematics;

        float x = schematics.rawCentroidX();
        float y = schematics.rawCentroidY();
        float z = schematics.rawCentroidZ();

        int size = positions.length;

        for(int i = 0; i < size; i += FSHub.UNIT_SIZE_POSITION){
            positions[i] = positions[i] - x;
            positions[i + 1] = positions[i + 1] - y;
            positions[i + 2] = positions[i + 2] - z;
        }

        schematics.updateBoundaries();
    }

    protected void unIndexPositions(FSInstance.Data instancedata, short[] indices){
        float[] positions = instancedata.positions().provider();
        VLListFloat converted = new VLListFloat(positions.length, positions.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int pindex = indices[i2] * FSHub.UNIT_SIZE_POSITION;

            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
        }

        converted.restrictSize();
        instancedata.positions().provider(converted.array());
    }

    protected void unIndexColors(FSInstance.Data instancedata, short[] indices){
        float[] colors =instancedata.colors().provider();
        VLListFloat converted = new VLListFloat(colors.length, colors.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int cindex = indices[i2] * FSHub.UNIT_SIZE_COLOR;

            converted.add(colors[cindex]);
            converted.add(colors[cindex + 1]);
            converted.add(colors[cindex + 2]);
            converted.add(colors[cindex + 3]);
        }

        converted.restrictSize();
       instancedata.colors().provider(converted.array());
    }

    protected void unIndexTexCoords(FSInstance.Data instancedata, short[] indices){
        float[] texcoords =instancedata.texCoords().provider();
        VLListFloat converted = new VLListFloat(texcoords.length, texcoords.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int tindex = indices[i2] * FSHub.UNIT_SIZE_TEXCOORD;

            converted.add(texcoords[tindex]);
            converted.add(texcoords[tindex + 1]);
        }

        converted.restrictSize();
       instancedata.texCoords().provider(converted.array());
    }

    protected void unIndexNormals(FSInstance.Data instancedata, short[] indices){
        float[] normals =instancedata.normals().provider();
        VLListFloat converted = new VLListFloat(normals.length, normals.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int nindex = indices[i2] * FSHub.UNIT_SIZE_NORMAL;

            converted.add(normals[nindex]);
            converted.add(normals[nindex + 1]);
            converted.add(normals[nindex + 2]);
        }

        converted.restrictSize();
        instancedata.normals().provider(converted.array());
    }

    protected final void buildFirst(FSInstance instance, FSHScanner scanner, FSM.Data data){
        operate(firstfuncs, instance, scanner, data);
        operate(customfunc, instance, scanner, data);
    }

    protected final void buildRest(FSInstance instance, FSHScanner scanner, FSM.Data data){
        operate(restfuncs, instance, scanner, data);
        operate(customfunc, instance, scanner, data);
    }

    private void operate(VLListType<BuildStep> funcs, FSInstance instance, FSHScanner scanner, FSM.Data data){
        FSMesh<?> mesh = scanner.mesh;
        VLArrayShort indices = mesh.indices;

        int newindex = mesh.size();
        int funcsize = funcs.size();

        FSInstance.Data instancedata = instance.data;

        for(int i = 0; i < funcsize; i++){
            funcs.get(i).process(this, mesh, indices, instance, instancedata, data);
        }
    }

    protected final void checkDebug(){
        if(firstfuncs.size() == 0){
            throw new RuntimeException("[ERROR] [Assembler not configured]");
        }
    }

    public final void stringify(StringBuilder info, Object hint){
        info.append("LOAD_MODELS[");
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
        info.append("]");
    }


    private static final BuildStep MODEL_INITIALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            instance.data.model(new FSArrayModel());
            instance.modelMatrix(new FSMatrixModel(2, 10));
        }
    };

    private static final BuildStep POSITION_MATRIX_DEFAULT = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            instancedata.positions(new VLArrayFloat(data.positions.array()));
        }
    };
    private static final BuildStep POSITION_MATRIX_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            instancedata.positions(new VLArrayFloat(mesh.get(0).positions().provider()));
        }
    };
    private static final BuildStep POSITION_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            assembler.unIndexPositions(instancedata, indices.provider());
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            assembler.buildModelClusterFromSchematics(instance);
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            assembler.buildModelClusterFromSchematics(instance);
            assembler.centralizePositions(instance);
        }
    };
    private static final BuildStep POSITION_INIT_SCHEMATICS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            FSSchematics schematics = instance.schematics;
            schematics.initialize();
            schematics.updateBoundaries();
        }
    };
    private static final BuildStep POSITION_SHARE_SCHEMATICS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            instance.schematics.updateBoundaries(mesh.first().schematics);
        }
    };

    private static final BuildStep COLOR_FILE_LOADED_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.colors(new VLArrayFloat(data.colors.array()));
        }
    };
    private static final BuildStep COLOR_FILE_LOADED_NONE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.colors(new VLArrayFloat(data.colors.array()));
            assembler.unIndexColors(instancedata, indices.provider());
        }
    };
    private static final BuildStep COLOR_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.colors(new VLArrayFloat(mesh.get(0).colors().provider()));
        }
    };


    private static final BuildStep TEXTURE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.texCoords(new VLArrayFloat(data.texcoords.array()));
        }
    };
    private static final BuildStep TEXTURE_NONE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.texCoords(new VLArrayFloat(data.texcoords.array()));
            assembler.unIndexTexCoords(instancedata, indices.provider());
        }
    };
    private static final BuildStep TEXTURE_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.texCoords(new VLArrayFloat(mesh.get(0).texCoords().provider()));
        }
    };
    private static final BuildStep TEXTURE_FLIP_U = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            float[] array = instancedata.texCoords().provider();
            int size = array.length;

            for(int i = 0; i < size; i += 2){
                array[i] = 1F - array[i];
            }
        }
    };
    private static final BuildStep TEXTURE_FLIP_V = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
            float[] array = instancedata.texCoords().provider();
            int size = array.length;

            for(int i = 1; i < size; i += 2){
                array[i] = 1F - array[i];
            }
        }
    };


    private static final BuildStep NORMAL_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.normals(new VLArrayFloat(data.normals.array()));
        }
    };
    private static final BuildStep NORMAL_NONE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.normals(new VLArrayFloat(data.normals.array()));
            assembler.unIndexNormals(instancedata, indices.provider());
        }
    };
    private static final BuildStep NORMAL_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data){
           instancedata.normals(new VLArrayFloat(mesh.get(0).normals().provider()));
        }
    };

    public interface BuildStep{

        void process(FSHAssembler assembler, FSMesh<?> mesh, VLArrayShort indices, FSInstance instance, FSInstance.Data instancedata, FSM.Data data);
    }
}
