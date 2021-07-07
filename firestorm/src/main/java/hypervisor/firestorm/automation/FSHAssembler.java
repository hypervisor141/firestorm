package hypervisor.firestorm.automation;

import hypervisor.firestorm.engine.FSElements;
import hypervisor.firestorm.io.FSM;
import hypervisor.firestorm.mesh.FSArrayModel;
import hypervisor.firestorm.mesh.FSElement;
import hypervisor.firestorm.mesh.FSElementStore;
import hypervisor.firestorm.mesh.FSModelMatrix;
import hypervisor.firestorm.mesh.FSSchematics;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.array.VLArrayShort;
import hypervisor.vanguard.list.VLListFloat;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLLog;
import hypervisor.vanguard.utils.VLLoggable;
import hypervisor.vanguard.variable.VLV;

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
    public boolean FLIP_POSITION_X = false;
    public boolean FLIP_POSITION_Y = false;
    public boolean FLIP_POSITION_Z = false;
    public boolean FLIP_NORMAL_X = false;
    public boolean FLIP_NORMAL_Y = false;
    public boolean FLIP_NORMAL_Z = false;
    public boolean FLIP_INDICES_TRIANGLES = false;
    public boolean FLIP_INDICES_QUADS = false;

    public boolean CONVERT_POSITIONS_TO_MODELMATRIX = false;
    public boolean DRAW_MODE_INDEXED = false;

    protected VLListType<BuildStep> firststeps;
    protected VLListType<BuildStep> instancesteps;
    protected VLListType<BuildStep> customsteps;

    public FSHAssembler(int customstepcapacity){
        firststeps = new VLListType<>(10, 20);
        instancesteps = new VLListType<>(10, 20);
        customsteps = new VLListType<>(customstepcapacity, customstepcapacity);

        setDefaultAll();
    }

    protected FSHAssembler(){

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
        FLIP_POSITION_X = false;
        FLIP_POSITION_Y = false;
        FLIP_POSITION_Z = false;
        FLIP_NORMAL_X = false;
        FLIP_NORMAL_Y = false;
        FLIP_NORMAL_Z = false;
        FLIP_INDICES_TRIANGLES = false;
        FLIP_INDICES_QUADS = false;

        CONVERT_POSITIONS_TO_MODELMATRIX = true;
        DRAW_MODE_INDEXED = true;
    }

    public void configure(){
        firststeps.clear();
        instancesteps.clear();
        customsteps.clear();

        configureModels();
        configurePositions();
        configureColors();
        configureTexCoords();
        configureNormals();
        configureIndices();

        checkDebug();
    }

    public VLListType<BuildStep> customSteps(){
        return customsteps;
    }

    private void configureModels(){
        if(LOAD_MODELS){
            firststeps.add(MODEL_INITIALIZE);
            instancesteps.add(MODEL_INITIALIZE);
        }
    }

    private void configurePositions(){
        if(LOAD_POSITIONS){
            firststeps.add(POSITION_SET);

            if(FLIP_POSITION_X){
                firststeps.add(POSITION_FLIP_X);
            }
            if(FLIP_POSITION_Y){
                firststeps.add(POSITION_FLIP_Y);
            }
            if(FLIP_POSITION_Z){
                firststeps.add(POSITION_FLIP_Z);
            }
            if(!DRAW_MODE_INDEXED){
                firststeps.add(POSITION_UNINDEX);
            }

            firststeps.add(POSITION_INIT_SCHEMATICS);

            if(CONVERT_POSITIONS_TO_MODELMATRIX){
                firststeps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
            }

            if(INSTANCE_SHARE_POSITIONS){
                if(CONVERT_POSITIONS_TO_MODELMATRIX){
                    instancesteps.add(POSITION_SET);

                    if(!DRAW_MODE_INDEXED){
                        instancesteps.add(POSITION_UNINDEX);
                    }

                    instancesteps.add(POSITION_INIT_SCHEMATICS);
                    instancesteps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
                }

                instancesteps.add(POSITION_SHARED);

            }else{
                instancesteps.add(POSITION_SET);

                if(FLIP_POSITION_X){
                    instancesteps.add(POSITION_FLIP_X);
                }
                if(FLIP_POSITION_Y){
                    instancesteps.add(POSITION_FLIP_Y);
                }
                if(FLIP_POSITION_Z){
                    instancesteps.add(POSITION_FLIP_Z);
                }
                if(!DRAW_MODE_INDEXED){
                    instancesteps.add(POSITION_UNINDEX);
                }

                instancesteps.add(POSITION_INIT_SCHEMATICS);

                if(CONVERT_POSITIONS_TO_MODELMATRIX){
                    instancesteps.add(POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX);
                }
            }
        }
    }

    private void configureColors(){
        if(LOAD_COLORS){
            firststeps.add(COLOR_FILE_SET);

            if(!DRAW_MODE_INDEXED){
                firststeps.add(COLOR_FILE_LOADED_NONE_INDEXED);
            }

            if(INSTANCE_SHARE_COLORS){
                instancesteps.add(COLOR_SHARED);

            }else{
                instancesteps.add(COLOR_FILE_SET);

                if(!DRAW_MODE_INDEXED){
                    instancesteps.add(COLOR_FILE_LOADED_NONE_INDEXED);
                }
            }
        }
    }

    private void configureTexCoords(){
        if(LOAD_TEXCOORDS){
            firststeps.add(TEXTURE_SET);

            if(FLIP_TEXTURE_U){
                firststeps.add(TEXTURE_FLIP_U);
            }
            if(FLIP_TEXTURE_V){
                firststeps.add(TEXTURE_FLIP_V);
            }
            if(!DRAW_MODE_INDEXED){
                firststeps.add(TEXTURE_UNINDEX);
            }

            if(INSTANCE_SHARE_TEXCOORDS){
                instancesteps.add(TEXTURE_SHARED);

            }else{
                instancesteps.add(TEXTURE_SET);

                if(FLIP_TEXTURE_U){
                    instancesteps.add(TEXTURE_FLIP_U);
                }
                if(FLIP_TEXTURE_V){
                    instancesteps.add(TEXTURE_FLIP_V);
                }
                if(!DRAW_MODE_INDEXED){
                    instancesteps.add(TEXTURE_UNINDEX);
                }
            }
        }
    }

    private void configureNormals(){
        if(LOAD_NORMALS){
            firststeps.add(NORMAL_SET);

            if(FLIP_NORMAL_X){
                firststeps.add(NORMAL_FLIP_X);
            }
            if(FLIP_NORMAL_Y){
                firststeps.add(NORMAL_FLIP_Y);
            }
            if(FLIP_NORMAL_Z){
                firststeps.add(NORMAL_FLIP_Z);
            }
            if(!DRAW_MODE_INDEXED){
                firststeps.add(NORMAL_UNINDEX);
            }

            if(INSTANCE_SHARE_NORMALS){
                instancesteps.add(NORMAL_SHARED);

            }else{
                instancesteps.add(NORMAL_SET);

                if(FLIP_NORMAL_X){
                    instancesteps.add(NORMAL_FLIP_X);
                }
                if(FLIP_NORMAL_Y){
                    instancesteps.add(NORMAL_FLIP_Y);
                }
                if(FLIP_NORMAL_Z){
                    instancesteps.add(NORMAL_FLIP_Z);
                }
                if(!DRAW_MODE_INDEXED){
                    instancesteps.add(NORMAL_UNINDEX);
                }
            }
        }
    }

    private void configureIndices(){
        if(LOAD_INDICES){
            firststeps.add(INDICES_SET);

            if(FLIP_INDICES_TRIANGLES){
                firststeps.add(INDICES_FLIP_TRIANGLES);

            }else if(FLIP_INDICES_QUADS){
                firststeps.add(INDICES_FLIP_QUADS);
            }

            if(INSTANCE_SHARE_INDICES){
                instancesteps.add(INDICES_SHARE);

            }else{
                instancesteps.add(INDICES_SET);

                if(FLIP_INDICES_TRIANGLES){
                    instancesteps.add(INDICES_FLIP_TRIANGLES);

                }else if(FLIP_INDICES_QUADS){
                    instancesteps.add(INDICES_FLIP_QUADS);
                }
            }
        }
    }

    private void buildModelMatrixFromSchematics(FSTypeInstance instance){
        FSSchematics schematics = instance.schematics();

        instance.modelMatrix().addRowTranslation(0, new VLV(schematics.localSpaceCentroidX()), new VLV(schematics.localSpaceCentroidY()), new VLV(schematics.localSpaceCentroidZ()));
        instance.model().transform(0, instance.modelMatrix(), true);
    }

    private void centralizePositions(FSTypeInstance instance){
        float[] positions = instance.positions().array;
        FSSchematics schematics = instance.schematics();

        float x = schematics.localSpaceCentroidX();
        float y = schematics.localSpaceCentroidY();
        float z = schematics.localSpaceCentroidZ();

        int size = positions.length;

        for(int i = 0; i < size; i += FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION]){
            positions[i] = positions[i] - x;
            positions[i + 1] = positions[i + 1] - y;
            positions[i + 2] = positions[i + 2] - z;
        }

        schematics.rebuild();
    }

    private void unIndexPositions(FSTypeInstance instance){
        short[] indices = instance.indices().array;
        float[] positions = instance.positions().array;
        VLListFloat converted = new VLListFloat(positions.length, positions.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int pindex = indices[i2] * FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];

            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
            converted.add(positions[pindex]);
        }

        converted.restrictSize();
        instance.positions().array = converted.array();
    }

    private void unIndexColors(FSTypeInstance instance){
        short[] indices = instance.indices().array;
        float[] colors = instance.colors().array;
        VLListFloat converted = new VLListFloat(colors.length, colors.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int cindex = indices[i2] * FSElements.UNIT_SIZES[FSElements.ELEMENT_COLOR];

            converted.add(colors[cindex]);
            converted.add(colors[cindex + 1]);
            converted.add(colors[cindex + 2]);
            converted.add(colors[cindex + 3]);
        }

        converted.restrictSize();
        instance.colors().array = converted.array();
    }

    private void unIndexTexCoords(FSTypeInstance instance){
        short[] indices = instance.indices().array;
        float[] texcoords = instance.texCoords().array;
        VLListFloat converted = new VLListFloat(texcoords.length, texcoords.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int tindex = indices[i2] * FSElements.UNIT_SIZES[FSElements.ELEMENT_TEXCOORD];

            converted.add(texcoords[tindex]);
            converted.add(texcoords[tindex + 1]);
        }

        converted.restrictSize();
        instance.texCoords().array = converted.array();
    }

    private void unIndexNormals(FSTypeInstance instance){
        short[] indices = instance.indices().array;
        float[] normals = instance.normals().array;
        VLListFloat converted = new VLListFloat(normals.length, normals.length / 2);

        for(int i2 = 0; i2 < indices.length; i2++){
            int nindex = indices[i2] * FSElements.UNIT_SIZES[FSElements.ELEMENT_NORMAL];

            converted.add(normals[nindex]);
            converted.add(normals[nindex + 1]);
            converted.add(normals[nindex + 2]);
        }

        converted.restrictSize();
        instance.normals().array = converted.array();
    }

    final void buildFirst(FSTypeInstance instance, FSTypeMesh<FSTypeInstance> target, FSM.Data data){
        buildTarget(firststeps, instance, target, data);
        buildTarget(customsteps, instance, target, data);
    }

    final void buildRest(FSTypeInstance instance, FSTypeMesh<FSTypeInstance> target, FSM.Data data){
        buildTarget(instancesteps, instance, target, data);
        buildTarget(customsteps, instance, target, data);
    }

    final void buildTarget(VLListType<BuildStep> funcs, FSTypeInstance instance, FSTypeMesh<FSTypeInstance> target, FSM.Data data){
        int funcsize = funcs.size();
        FSElementStore store = instance.storage();

        for(int i = 0; i < funcsize; i++){
            funcs.get(i).process(this, target, instance, store, data);
        }
    }

    protected void checkDebug(){
        if(firststeps.size() == 0){
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
        log.append(CONVERT_POSITIONS_TO_MODELMATRIX);
        log.append("]\nDRAW_MODE_INDEXED[");
        log.append(DRAW_MODE_INDEXED);
        log.append("]");
    }

    private static final BuildStep INDICES_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_INDEX, 1, 0);
            store.add(FSElements.ELEMENT_INDEX, new FSElement.ShortArray(FSElements.ELEMENT_INDEX, new VLArrayShort(data.indices.array().clone())));
            store.activate(FSElements.ELEMENT_INDEX, 0);
        }
    };
    private static final BuildStep INDICES_FLIP_TRIANGLES = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            short[] array = instance.indices().array;
            int size = array.length;

            for(int i = 0 ; i < size; i += 3){
                short cache = array[i + 2];
                array[i + 2] = array[i];
                array[i] = cache;
            }
        }
    };
    private static final BuildStep INDICES_FLIP_QUADS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            short[] array = instance.indices().array;
            int size = array.length;

            for(int i = 0 ; i < size; i += 4){
                short cache = array[i + 3];
                array[i + 3] = array[i];
                array[i] = cache;

                cache = array[i + 2];
                array[i + 2] = array[i + 1];
                array[i + 1] = cache;
            }
        }
    };
    private static final BuildStep INDICES_SHARE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_INDEX, 1, 0);
            store.add(FSElements.ELEMENT_INDEX, new FSElement.ShortArray(FSElements.ELEMENT_INDEX, new VLArrayShort(mesh.first().indices().array)));
            store.activate(FSElements.ELEMENT_INDEX, 0);
        }
    };

    private static final BuildStep MODEL_INITIALIZE = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_MODEL, 1, 0);
            store.add(FSElements.ELEMENT_MODEL, new FSElement.FloatArray(FSElements.ELEMENT_MODEL, new FSArrayModel(FSElements.UNIT_SIZES[FSElements.ELEMENT_MODEL])));
            store.activate(FSElements.ELEMENT_MODEL, 0);

            instance.modelMatrix(new FSModelMatrix(2, 10));
        }
    };

    private static final BuildStep POSITION_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_POSITION, 1, 0);
            store.add(FSElements.ELEMENT_POSITION, new FSElement.FloatArray(FSElements.ELEMENT_POSITION, new VLArrayFloat(data.positions.array().clone())));
            store.activate(FSElements.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_FLIP_X = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.positions().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];

            for(int i = 0; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep POSITION_FLIP_Y = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.positions().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];

            for(int i = 1; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep POSITION_FLIP_Z = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.positions().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];

            for(int i = 2; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep POSITION_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_POSITION, 1, 0);
            store.add(FSElements.ELEMENT_POSITION, new FSElement.FloatArray(FSElements.ELEMENT_POSITION, new VLArrayFloat(mesh.first().positions().array)));
            store.activate(FSElements.ELEMENT_POSITION, 0);
        }
    };
    private static final BuildStep POSITION_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexPositions(instance);
        }
    };
    private static final BuildStep POSITION_CONVERT_POSITIONS_TO_MODEL_MATRIX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            assembler.buildModelMatrixFromSchematics(instance);
            assembler.centralizePositions(instance);
        }
    };
    private static final BuildStep POSITION_INIT_SCHEMATICS = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            FSSchematics schematics = instance.schematics();
            schematics.initialize(instance);
            schematics.rebuild();
        }
    };

    private static final BuildStep COLOR_FILE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_COLOR, 1, 0);
            store.add(FSElements.ELEMENT_COLOR, new FSElement.FloatArray(FSElements.ELEMENT_COLOR, new VLArrayFloat(data.colors.array().clone())));
            store.activate(FSElements.ELEMENT_COLOR, 0);
        }
    };
    private static final BuildStep COLOR_FILE_LOADED_NONE_INDEXED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexColors(instance);
        }
    };
    private static final BuildStep COLOR_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_COLOR, 1, 0);
            store.add(FSElements.ELEMENT_COLOR, new FSElement.FloatArray(FSElements.ELEMENT_COLOR, new VLArrayFloat(mesh.first().colors().array)));
            store.activate(FSElements.ELEMENT_COLOR, 0);
        }
    };


    private static final BuildStep TEXTURE_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_TEXCOORD, 1, 0);
            store.add(FSElements.ELEMENT_TEXCOORD, new FSElement.FloatArray(FSElements.ELEMENT_TEXCOORD, new VLArrayFloat(data.texcoords.array().clone())));
            store.activate(FSElements.ELEMENT_TEXCOORD, 0);
        }
    };
    private static final BuildStep TEXTURE_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexTexCoords(instance);
        }
    };
    private static final BuildStep TEXTURE_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_TEXCOORD, 1, 0);
            store.add(FSElements.ELEMENT_TEXCOORD, new FSElement.FloatArray(FSElements.ELEMENT_TEXCOORD, new VLArrayFloat(mesh.first().texCoords().array)));
            store.activate(FSElements.ELEMENT_TEXCOORD, 0);

        }
    };
    private static final BuildStep TEXTURE_FLIP_U = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.texCoords().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_TEXCOORD];

            for(int i = 0; i < size; i += jumps){
                array[i] = 1F - array[i];
            }
        }
    };
    private static final BuildStep TEXTURE_FLIP_V = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.texCoords().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_TEXCOORD];

            for(int i = 1; i < size; i += jumps){
                array[i] = 1F - array[i];
            }
        }
    };

    private static final BuildStep NORMAL_SET = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_NORMAL, 1, 0);
            store.add(FSElements.ELEMENT_NORMAL, new FSElement.FloatArray(FSElements.ELEMENT_NORMAL, new VLArrayFloat(data.normals.array().clone())));
            store.activate(FSElements.ELEMENT_NORMAL, 0);
        }
    };
    private static final BuildStep NORMAL_FLIP_X = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.normals().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_NORMAL];

            for(int i = 0; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep NORMAL_FLIP_Y = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.normals().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_NORMAL];

            for(int i = 1; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep NORMAL_FLIP_Z = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            float[] array = instance.normals().array;
            int size = array.length;
            int jumps = FSElements.UNIT_SIZES[FSElements.ELEMENT_NORMAL];

            for(int i = 2; i < size; i += jumps){
                array[i] = -array[i];
            }
        }
    };
    private static final BuildStep NORMAL_UNINDEX = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            assembler.unIndexNormals(instance);
        }
    };
    private static final BuildStep NORMAL_SHARED = new BuildStep(){

        @Override
        public void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data){
            store.allocateElement(FSElements.ELEMENT_NORMAL, 1, 0);
            store.add(FSElements.ELEMENT_NORMAL, new FSElement.FloatArray(FSElements.ELEMENT_NORMAL, new VLArrayFloat(mesh.first().normals().array)));
            store.activate(FSElements.ELEMENT_NORMAL, 0);
        }
    };

    public interface BuildStep{

        void process(FSHAssembler assembler, FSTypeMesh<FSTypeInstance> mesh, FSTypeInstance instance, FSElementStore store, FSM.Data data);
    }
}
