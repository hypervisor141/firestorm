package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLCopyable;
import vanguard.VLListFloat;
import vanguard.VLListType;
import vanguard.VLLog;
import vanguard.VLLoggable;
import vanguard.VLV;

public class FSHAssembler implements VLLoggable{

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
    public boolean INSTANCE_SHARE_INDICES = false;

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
        INSTANCE_SHARE_INDICES = false;

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
        configureIndices();
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
            firstfuncs.add(POSITION_SET);

            if(!DRAW_MODE_INDEXED){
                firstfuncs.add(POSITION_UNINDEX);
            }

            firstfuncs.add(POSITION_INIT_SCHEMATICS);

            if(CONVERT_POSITIONS_TO_MODELARRAYS){
                firstfuncs.add(POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE);
            }

            if(INSTANCE_SHARE_POSITIONS){
                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    restfuncs.add(POSITION_SET);

                    if(!DRAW_MODE_INDEXED){
                        restfuncs.add(POSITION_UNINDEX);
                    }

                    restfuncs.add(POSITION_INIT_SCHEMATICS);
                    restfuncs.add(POSITION_BUILD_MODELMATRIX);
                }

                restfuncs.add(POSITION_SHARED);
                restfuncs.add(POSITION_SHARE_SCHEMATICS);

            }else{
                restfuncs.add(POSITION_SET);

                if(!DRAW_MODE_INDEXED){
                    restfuncs.add(POSITION_UNINDEX);
                }

                restfuncs.add(POSITION_INIT_SCHEMATICS);

                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    restfuncs.add(POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE);
                }
            }
        }
    }

    private void configureColors(){
        if(LOAD_COLORS){
            firstfuncs.add(COLOR_FILE_SET);

            if(!DRAW_MODE_INDEXED){
                firstfuncs.add(COLOR_FILE_LOADED_NONE_INDEXED);
            }

            if(INSTANCE_SHARE_COLORS){
                restfuncs.add(COLOR_SHARED);

            }else{
                restfuncs.add(COLOR_FILE_SET);

                if(!DRAW_MODE_INDEXED){
                    restfuncs.add(COLOR_FILE_LOADED_NONE_INDEXED);
                }
            }
        }
    }

    private void configureTexCoords(){
        if(LOAD_TEXCOORDS){
            firstfuncs.add(TEXTURE_SET);

            if(FLIP_TEXTURE_U){
                firstfuncs.add(TEXTURE_FLIP_U);
            }
            if(FLIP_TEXTURE_V){
                firstfuncs.add(TEXTURE_FLIP_V);
            }
            if(!DRAW_MODE_INDEXED){
                firstfuncs.add(TEXTURE_UNINDEX);
            }

            if(INSTANCE_SHARE_TEXCOORDS){
                restfuncs.add(TEXTURE_SHARED);

            }else{
                restfuncs.add(TEXTURE_SET);

                if(FLIP_TEXTURE_U){
                    restfuncs.add(TEXTURE_FLIP_U);
                }
                if(FLIP_TEXTURE_V){
                    restfuncs.add(TEXTURE_FLIP_V);
                }
                if(!DRAW_MODE_INDEXED){
                    restfuncs.add(TEXTURE_UNINDEX);
                }
            }
        }
    }

    private void configureNormals(){
        if(LOAD_NORMALS){
            firstfuncs.add(NORMAL_SET);

            if(!DRAW_MODE_INDEXED){
                firstfuncs.add(NORMAL_UNINDEX);
            }

            if(INSTANCE_SHARE_NORMALS){
                restfuncs.add(NORMAL_SHARED);

            }else{
                restfuncs.add(NORMAL_SET);

                if(!DRAW_MODE_INDEXED){
                    restfuncs.add(NORMAL_UNINDEX);
                }
            }
        }
    }

    private void configureIndices(){
        if(LOAD_INDICES){
            firstfuncs.add(INDICES_SET);

            if(INSTANCE_SHARE_INDICES){
                restfuncs.add(INDICES_SHARE);

            }else{
                restfuncs.add(INDICES_SET);
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

        for(int i = 0; i < size; i += FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_POSITION]){
            positions[i] = positions[i] - x;
            positions[i + 1] = positions[i + 1] - y;
            positions[i + 2] = positions[i + 2] - z;
        }

        schematics.updateBoundaries();
    }

    protected void unIndexPositions(FSInstance instance){
        short[] indices = instance.indices().provider();
        float[] positions = instance.positions().provider();
        VLListFloat converted = new VLListFloat(positions.length, positions.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int pindex = indices[i2] * FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_POSITION];

            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
        }

        converted.restrictSize();
        instance.positions().provider(converted.array());
    }

    protected void unIndexColors(FSInstance instance){
        short[] indices = instance.indices().provider();
        float[] colors = instance.colors().provider();
        VLListFloat converted = new VLListFloat(colors.length, colors.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int cindex = indices[i2] * FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_COLOR];

            converted.add(colors[cindex]);
            converted.add(colors[cindex + 1]);
            converted.add(colors[cindex + 2]);
            converted.add(colors[cindex + 3]);
        }

        converted.restrictSize();
       instance.colors().provider(converted.array());
    }

    protected void unIndexTexCoords(FSInstance instance){
        short[] indices = instance.indices().provider();
        float[] texcoords = instance.texCoords().provider();
        VLListFloat converted = new VLListFloat(texcoords.length, texcoords.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int tindex = indices[i2] * FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_TEXCOORD];

            converted.add(texcoords[tindex]);
            converted.add(texcoords[tindex + 1]);
        }

        converted.restrictSize();
       instance.texCoords().provider(converted.array());
    }

    protected void unIndexNormals(FSInstance instance){
        short[] indices = instance.indices().provider();
        float[] normals = instance.normals().provider();
        VLListFloat converted = new VLListFloat(normals.length, normals.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int nindex = indices[i2] * FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_NORMAL];

            converted.add(normals[nindex]);
            converted.add(normals[nindex + 1]);
            converted.add(normals[nindex + 2]);
        }

        converted.restrictSize();
        instance.normals().provider(converted.array());
    }

    protected final void buildFirst(FSInstance instance, FSHScanner scanner, FSM.Data data){
        buildTarget(firstfuncs, instance, scanner, data);
        buildTarget(customfunc, instance, scanner, data);
    }

    protected final void buildRest(FSInstance instance, FSHScanner scanner, FSM.Data data){
        buildTarget(restfuncs, instance, scanner, data);
        buildTarget(customfunc, instance, scanner, data);
    }

    private void buildTarget(VLListType<BuildStep> funcs, FSInstance instance, FSHScanner scanner, FSM.Data data){
        FSMesh mesh = scanner.mesh;
        int funcsize = funcs.size();

        FSElementStore store = instance.storage();

        for(int i = 0; i < funcsize; i++){
            funcs.get(i).process(this, mesh, instance, store, data);
        }
    }

    protected final void checkDebug(){
        if(firstfuncs.size() == 0){
            throw new RuntimeException("[ERROR] [Assembler not configured]");
        }
    }

    @Override
    public void log(VLLog log, Object data){
        log.append("LOAD_MODELS[");
        log.append(LOAD_MODELS);
        log.append("]\nLOAD_POSITIONS[");
        log.append(LOAD_POSITIONS);
        log.append("]\nLOAD_TEXCOORDS[");
        log.append(LOAD_TEXCOORDS);
        log.append("]\nLOAD_COLORS[");
        log.append(LOAD_COLORS);
        log.append("]\nLOAD_NORMALS[");
        log.append(LOAD_NORMALS);
        log.append("]\nLOAD_INDICES[");
        log.append(LOAD_INDICES);
        log.append("]\nINSTANCE_SHARE_POSITIONS[");
        log.append(INSTANCE_SHARE_POSITIONS);
        log.append("]\nINSTANCE_SHARE_TEXCOORDS[");
        log.append(INSTANCE_SHARE_TEXCOORDS);
        log.append("]\nINSTANCE_SHARE_COLORS[");
        log.append(INSTANCE_SHARE_COLORS);
        log.append("]\nINSTANCE_SHARE_NORMALS[");
        log.append(INSTANCE_SHARE_NORMALS);
        log.append("]\nCONVERT_POSITIONS_TO_MODELARRAYS[");
        log.append(CONVERT_POSITIONS_TO_MODELARRAYS);
        log.append("]\nDRAW_MODE_INDEXED[");
        log.append(DRAW_MODE_INDEXED);
        log.append("]");
    }

    private static final BuildStep INDICES_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_INDEX, 1, 0);
            store.add(FSGlobal.ELEMENT_INDEX, new FSElement.Short(FSGlobal.ELEMENT_INDEX, new VLArrayShort(data.indices.array())));
            store.activate(FSGlobal.ELEMENT_INDEX, 0);
        }
    };

    private static final BuildStep INDICES_SHARE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_INDEX, 1, 0);
            store.add(FSGlobal.ELEMENT_INDEX, new FSElement.Short(FSGlobal.ELEMENT_INDEX, new VLArrayShort(mesh.first().indices().provider())));
            store.activate(FSGlobal.ELEMENT_INDEX, 0);
        }
    };

    private static final BuildStep MODEL_INITIALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_MODEL, 1, 0);
            store.add(FSGlobal.ELEMENT_MODEL, new FSElement.Float(FSGlobal.ELEMENT_MODEL, new FSArrayModel()));
            store.activate(FSGlobal.ELEMENT_MODEL, 0);

            instance.modelMatrix(new FSMatrixModel(2, 10));
        }
    };

    private static final BuildStep POSITION_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_POSITION, 1, 0);
            store.add(FSGlobal.ELEMENT_POSITION, new FSElement.Float(FSGlobal.ELEMENT_POSITION, new VLArrayFloat(data.positions.array())));
            store.activate(FSGlobal.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_POSITION, 1, 0);
            store.add(FSGlobal.ELEMENT_POSITION, new FSElement.Float(FSGlobal.ELEMENT_POSITION, new VLArrayFloat(mesh.first().positions().provider())));
            store.activate(FSGlobal.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexPositions(instance);
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.buildModelClusterFromSchematics(instance);
        }
    };
    private static final BuildStep POSITION_BUILD_MODELMATRIX_AND_CENTRALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.buildModelClusterFromSchematics(instance);
            assembler.centralizePositions(instance);
        }
    };
    private static final BuildStep POSITION_INIT_SCHEMATICS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            FSSchematics schematics = instance.schematics;
            schematics.initialize();
            schematics.updateBoundaries();
        }
    };
    private static final BuildStep POSITION_SHARE_SCHEMATICS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            instance.schematics = new FSSchematics(mesh.first().schematics, VLCopyable.FLAG_DUPLICATE);
        }
    };

    private static final BuildStep COLOR_FILE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_COLOR, 1, 0);
            store.add(FSGlobal.ELEMENT_COLOR, new FSElement.Float(FSGlobal.ELEMENT_COLOR, new VLArrayFloat(data.colors.array())));
            store.activate(FSGlobal.ELEMENT_COLOR, 0);
        }
    };
    private static final BuildStep COLOR_FILE_LOADED_NONE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexColors(instance);
        }
    };
    private static final BuildStep COLOR_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_COLOR, 1, 0);
            store.add(FSGlobal.ELEMENT_COLOR, new FSElement.Float(FSGlobal.ELEMENT_COLOR, new VLArrayFloat(mesh.first().colors().provider())));
            store.activate(FSGlobal.ELEMENT_COLOR, 0);
        }
    };


    private static final BuildStep TEXTURE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_TEXCOORD, 1, 0);
            store.add(FSGlobal.ELEMENT_TEXCOORD, new FSElement.Float(FSGlobal.ELEMENT_TEXCOORD, new VLArrayFloat(data.texcoords.array())));
            store.activate(FSGlobal.ELEMENT_TEXCOORD, 0);
        }
    };
    private static final BuildStep TEXTURE_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexTexCoords(instance);
        }
    };
    private static final BuildStep TEXTURE_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_TEXCOORD, 1, 0);
            store.add(FSGlobal.ELEMENT_TEXCOORD, new FSElement.Float(FSGlobal.ELEMENT_TEXCOORD, new VLArrayFloat(data.texcoords.array())));
            store.activate(FSGlobal.ELEMENT_TEXCOORD, 0);

        }
    };
    private static final BuildStep TEXTURE_FLIP_U = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.texCoords().provider();
            int size = array.length;
            int jumps = FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_TEXCOORD];

            for(int i = 0; i < size; i += jumps){
                array[i] = 1F - array[i];
            }
        }
    };
    private static final BuildStep TEXTURE_FLIP_V = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.texCoords().provider();
            int size = array.length;
            int jumps = FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_TEXCOORD];

            for(int i = 0; i < size; i += jumps){
                array[i] = 1F - array[i];
            }
        }
    };

    private static final BuildStep NORMAL_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_NORMAL, 1, 0);
            store.add(FSGlobal.ELEMENT_NORMAL, new FSElement.Float(FSGlobal.ELEMENT_NORMAL, new VLArrayFloat(data.normals.array())));
            store.activate(FSGlobal.ELEMENT_NORMAL, 0);
        }
    };
    private static final BuildStep NORMAL_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexNormals(instance);
        }
    };
    private static final BuildStep NORMAL_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_NORMAL, 1, 0);
            store.add(FSGlobal.ELEMENT_NORMAL, new FSElement.Float(FSGlobal.ELEMENT_NORMAL, new VLArrayFloat(mesh.first().normals().provider())));
            store.activate(FSGlobal.ELEMENT_NORMAL, 0);
        }
    };

    public interface BuildStep{

        void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data);
    }
}
