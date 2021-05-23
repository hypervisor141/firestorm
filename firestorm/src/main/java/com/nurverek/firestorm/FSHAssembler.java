package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLListFloat;
import vanguard.VLListType;
import vanguard.VLLog;
import vanguard.VLLoggable;
import vanguard.VLV;

public abstract class FSHAssembler implements VLLoggable{

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

    private final VLListType<BuildStep> firstInstanceSteps;
    private final VLListType<BuildStep> otherInstanceSteps;
    private final VLListType<BuildStep> customInstanceSteps;

    public FSHAssembler(int customstepcapacity){
        firstInstanceSteps = new VLListType<>(10, 20);
        otherInstanceSteps = new VLListType<>(10, 20);
        customInstanceSteps = new VLListType<>(customstepcapacity, customstepcapacity);

        setDefaultAll();
    }

    protected abstract void setup(VLListType<BuildStep> firstInstanceSteps, VLListType<BuildStep> otherInstanceSteps, VLListType<BuildStep> customInstanceSteps);

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
        otherInstanceSteps.clear();
        firstInstanceSteps.clear();
        customInstanceSteps.clear();

        setup(firstInstanceSteps, otherInstanceSteps, customInstanceSteps);

        configureModels();
        configurePositions();
        configureColors();
        configureTexCoords();
        configureNormals();
        configureIndices();
    }

    public VLListType<BuildStep> customSteps(){
        return customInstanceSteps;
    }

    private void configureModels(){
        if(LOAD_MODELS){
            firstInstanceSteps.add(MODEL_INITIALIZE);
            otherInstanceSteps.add(MODEL_INITIALIZE);
        }
    }

    private void configurePositions(){
        if(LOAD_POSITIONS){
            firstInstanceSteps.add(POSITION_SET);

            if(!DRAW_MODE_INDEXED){
                firstInstanceSteps.add(POSITION_UNINDEX);
            }

            firstInstanceSteps.add(POSITION_INIT_SCHEMATICS);

            if(CONVERT_POSITIONS_TO_MODELARRAYS){
                firstInstanceSteps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
            }

            if(INSTANCE_SHARE_POSITIONS){
                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    otherInstanceSteps.add(POSITION_SET);

                    if(!DRAW_MODE_INDEXED){
                        otherInstanceSteps.add(POSITION_UNINDEX);
                    }

                    otherInstanceSteps.add(POSITION_INIT_SCHEMATICS);
                    otherInstanceSteps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
                }

                otherInstanceSteps.add(POSITION_SHARED);

            }else{
                otherInstanceSteps.add(POSITION_SET);

                if(!DRAW_MODE_INDEXED){
                    otherInstanceSteps.add(POSITION_UNINDEX);
                }

                otherInstanceSteps.add(POSITION_INIT_SCHEMATICS);

                if(CONVERT_POSITIONS_TO_MODELARRAYS){
                    otherInstanceSteps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
                }
            }
        }
    }

    private void configureColors(){
        if(LOAD_COLORS){
            firstInstanceSteps.add(COLOR_FILE_SET);

            if(!DRAW_MODE_INDEXED){
                firstInstanceSteps.add(COLOR_FILE_LOADED_NONE_INDEXED);
            }

            if(INSTANCE_SHARE_COLORS){
                otherInstanceSteps.add(COLOR_SHARED);

            }else{
                otherInstanceSteps.add(COLOR_FILE_SET);

                if(!DRAW_MODE_INDEXED){
                    otherInstanceSteps.add(COLOR_FILE_LOADED_NONE_INDEXED);
                }
            }
        }
    }

    private void configureTexCoords(){
        if(LOAD_TEXCOORDS){
            firstInstanceSteps.add(TEXTURE_SET);

            if(FLIP_TEXTURE_U){
                firstInstanceSteps.add(TEXTURE_FLIP_U);
            }
            if(FLIP_TEXTURE_V){
                firstInstanceSteps.add(TEXTURE_FLIP_V);
            }
            if(!DRAW_MODE_INDEXED){
                firstInstanceSteps.add(TEXTURE_UNINDEX);
            }

            if(INSTANCE_SHARE_TEXCOORDS){
                otherInstanceSteps.add(TEXTURE_SHARED);

            }else{
                otherInstanceSteps.add(TEXTURE_SET);

                if(FLIP_TEXTURE_U){
                    otherInstanceSteps.add(TEXTURE_FLIP_U);
                }
                if(FLIP_TEXTURE_V){
                    otherInstanceSteps.add(TEXTURE_FLIP_V);
                }
                if(!DRAW_MODE_INDEXED){
                    otherInstanceSteps.add(TEXTURE_UNINDEX);
                }
            }
        }
    }

    private void configureNormals(){
        if(LOAD_NORMALS){
            firstInstanceSteps.add(NORMAL_SET);

            if(!DRAW_MODE_INDEXED){
                firstInstanceSteps.add(NORMAL_UNINDEX);
            }

            if(INSTANCE_SHARE_NORMALS){
                otherInstanceSteps.add(NORMAL_SHARED);

            }else{
                otherInstanceSteps.add(NORMAL_SET);

                if(!DRAW_MODE_INDEXED){
                    otherInstanceSteps.add(NORMAL_UNINDEX);
                }
            }
        }
    }

    private void configureIndices(){
        if(LOAD_INDICES){
            firstInstanceSteps.add(INDICES_SET);

            if(INSTANCE_SHARE_INDICES){
                otherInstanceSteps.add(INDICES_SHARE);

            }else{
                otherInstanceSteps.add(INDICES_SET);
            }
        }
    }

    private void buildModelMatrixFromSchematics(FSInstance instance){
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

    private void unIndexPositions(FSInstance instance){
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

    private void unIndexColors(FSInstance instance){
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

    private void unIndexTexCoords(FSInstance instance){
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

    private void unIndexNormals(FSInstance instance){
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
        buildTarget(firstInstanceSteps, instance, scanner, data);
        buildTarget(customInstanceSteps, instance, scanner, data);
    }

    protected final void buildRest(FSInstance instance, FSHScanner scanner, FSM.Data data){
        buildTarget(otherInstanceSteps, instance, scanner, data);
        buildTarget(customInstanceSteps, instance, scanner, data);
    }

    private void buildTarget(VLListType<BuildStep> funcs, FSInstance instance, FSHScanner scanner, FSM.Data data){
        FSMesh mesh = scanner.mesh;
        int funcsize = funcs.size();

        FSElementStore store = instance.storage();

        for(int i = 0; i < funcsize; i++){
            funcs.get(i).process(this, mesh, instance, store, data);
        }
    }

    protected void checkDebug(){
        if(firstInstanceSteps.size() == 0){
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
            store.add(FSGlobal.ELEMENT_INDEX, new FSElement.ShortArray(FSGlobal.ELEMENT_INDEX, new VLArrayShort(data.indices.array())));
            store.activate(FSGlobal.ELEMENT_INDEX, 0);
        }
    };

    private static final BuildStep INDICES_SHARE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_INDEX, 1, 0);
            store.add(FSGlobal.ELEMENT_INDEX, new FSElement.ShortArray(FSGlobal.ELEMENT_INDEX, new VLArrayShort(mesh.first().indices().provider())));
            store.activate(FSGlobal.ELEMENT_INDEX, 0);
        }
    };

    private static final BuildStep MODEL_INITIALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_MODEL, 1, 0);
            store.add(FSGlobal.ELEMENT_MODEL, new FSElement.FloatArray(FSGlobal.ELEMENT_MODEL, new FSArrayModel()));
            store.activate(FSGlobal.ELEMENT_MODEL, 0);

            instance.modelMatrix(new FSMatrixModel(2, 10));
        }
    };

    private static final BuildStep POSITION_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_POSITION, 1, 0);
            store.add(FSGlobal.ELEMENT_POSITION, new FSElement.FloatArray(FSGlobal.ELEMENT_POSITION, new VLArrayFloat(data.positions.array())));
            store.activate(FSGlobal.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_POSITION, 1, 0);
            store.add(FSGlobal.ELEMENT_POSITION, new FSElement.FloatArray(FSGlobal.ELEMENT_POSITION, new VLArrayFloat(mesh.first().positions().provider())));
            store.activate(FSGlobal.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexPositions(instance);
        }
    };
    private static final BuildStep POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            assembler.buildModelMatrixFromSchematics(instance);
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

    private static final BuildStep COLOR_FILE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_COLOR, 1, 0);
            store.add(FSGlobal.ELEMENT_COLOR, new FSElement.FloatArray(FSGlobal.ELEMENT_COLOR, new VLArrayFloat(data.colors.array())));
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
            store.add(FSGlobal.ELEMENT_COLOR, new FSElement.FloatArray(FSGlobal.ELEMENT_COLOR, new VLArrayFloat(mesh.first().colors().provider())));
            store.activate(FSGlobal.ELEMENT_COLOR, 0);
        }
    };


    private static final BuildStep TEXTURE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_TEXCOORD, 1, 0);
            store.add(FSGlobal.ELEMENT_TEXCOORD, new FSElement.FloatArray(FSGlobal.ELEMENT_TEXCOORD, new VLArrayFloat(data.texcoords.array())));
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
            store.add(FSGlobal.ELEMENT_TEXCOORD, new FSElement.FloatArray(FSGlobal.ELEMENT_TEXCOORD, new VLArrayFloat(mesh.first().texCoords().provider())));
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

            for(int i = 1; i < size; i += jumps){
                array[i] = 1F - array[i];
            }
        }
    };

    private static final BuildStep NORMAL_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSGlobal.ELEMENT_NORMAL, 1, 0);
            store.add(FSGlobal.ELEMENT_NORMAL, new FSElement.FloatArray(FSGlobal.ELEMENT_NORMAL, new VLArrayFloat(data.normals.array())));
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
            store.add(FSGlobal.ELEMENT_NORMAL, new FSElement.FloatArray(FSGlobal.ELEMENT_NORMAL, new VLArrayFloat(mesh.first().normals().provider())));
            store.activate(FSGlobal.ELEMENT_NORMAL, 0);
        }
    };

    public interface BuildStep{

        void process(FSHAssembler assembler, FSMesh mesh, FSInstance instance, FSElementStore store, FSM.Data data);
    }
}
