package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayInt;
import com.nurverek.vanguard.VLArrayUtils;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLInt;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLStringify;

import java.io.UnsupportedEncodingException;

public final class FSP{

    private static final int BUFFER_PRINT_LIMIT = 50;

    private VLListType<FSShader> shaders;

    protected FSShader vertexshader;
    protected FSShader geometryshader;
    protected FSShader tesselevalshader;
    protected FSShader tesselcontrolshader;
    protected FSShader fragmentshader;
    protected FSShader computeshader;

    protected VLListType<FSConfig> setupconfigs;
    protected VLListType<FSConfig> meshconfigs;
    protected VLListType<FSConfig> postdrawconfigs;
    protected VLListType<FSMesh> meshes;

    private int program;
    protected int debug;

    protected int uniformlocation;

    public FSP(int debugmode){
        program = -1;
        debug = debugmode;

        shaders = new VLListType<>(5, 5);
        setupconfigs = new VLListType<>(10, 10);
        meshconfigs = new VLListType<>(20, 20);
        postdrawconfigs = new VLListType<>(10, 10);
        meshes = new VLListType<>(10, 50);

        uniformlocation = 0;

        initializeVertexShader();
        initializeFragmentShader();
    }


    public void addMesh(FSMesh mesh){
        meshes.add(mesh);
    }

    public FSShader vertexShader(){
        return vertexshader;
    }

    public FSShader geometryShader(){
        return geometryshader;
    }

    public FSShader tesselControlShader(){
        return tesselcontrolshader;
    }

    public FSShader tesselEvalShader(){
        return tesselevalshader;
    }

    public FSShader fragmentShader(){
        return fragmentshader;
    }

    public FSShader computeShader(){
        return computeshader;
    }

    public VLListType<FSConfig> setupConfigs(){
        return setupconfigs;
    }

    public VLListType<FSConfig> meshConfigs(){
        return meshconfigs;
    }

    public VLListType<FSConfig> postDrawConfigs(){
        return postdrawconfigs;
    }

    public VLListType<FSMesh> meshes(){
        return meshes;
    }

    public int id(){
        return program;
    }


    public void initializeVertexShader(FSShader shader){
        if(vertexshader == null){
            vertexshader = shader;
            shaders.add(shader);
        }
    }

    public void initializeGeometryShader(FSShader shader){
        if(geometryshader == null){
            geometryshader = shader;
            shaders.add(shader);
        }
    }

    public void initializeTesselControlShader(FSShader shader){
        if(tesselcontrolshader == null){
            tesselcontrolshader = shader;
            shaders.add(shader);
        }
    }

    public void initializeTesselEvalShader(FSShader shader){
        if(tesselevalshader == null){
            tesselevalshader = shader;
            shaders.add(shader);
        }
    }

    public void initializeFragmentShader(FSShader shader){
        if(fragmentshader == null){
            fragmentshader = shader;
            shaders.add(shader);
        }
    }

    public void initializeComputeShader(FSShader shader){
        if(computeshader == null){
            computeshader = shader;
            shaders.add(shader);
        }
    }

    public FSShader initializeVertexShader(){
        if(vertexshader == null){
            vertexshader = new FSShader(GLES32.GL_VERTEX_SHADER);
            shaders.add(vertexshader);
        }

        return vertexshader;
    }

    public FSShader initializeGeometryShader(){
        if(geometryshader == null){
            geometryshader = new FSShader(GLES32.GL_GEOMETRY_SHADER);
            shaders.add(geometryshader);
        }

        return geometryshader;
    }

    public FSShader initializeTesselEvalShader(){
        if(tesselevalshader == null){
            tesselevalshader = new FSShader(GLES32.GL_TESS_EVALUATION_SHADER);
            shaders.add(tesselevalshader);
        }

        return tesselevalshader;
    }

    public FSShader initializeTesselControlShader(){
        if(tesselcontrolshader == null){
            tesselcontrolshader = new FSShader(GLES32.GL_TESS_CONTROL_SHADER);
            shaders.add(tesselcontrolshader);
        }

        return tesselcontrolshader;
    }

    public FSShader initializeFragmentShader(){
        if(fragmentshader == null){
            fragmentshader = new FSShader(GLES32.GL_FRAGMENT_SHADER);
            shaders.add(fragmentshader);
        }

        return fragmentshader;
    }

    public FSShader initializeComputeShader(){
        if(computeshader == null){
            computeshader = new FSShader(GLES32.GL_COMPUTE_SHADER);
            shaders.add(computeshader);
        }

        return computeshader;
    }


    public void modify(FSPMod mod, FSConfig.Policy policy){
        mod.modify(this);
    }

    public void addSetupConfig(FSConfig config){
        setupconfigs.add(config);
    }

    public void addSetupConfig(VLListType<FSConfig> configs){
        setupconfigs.add(configs);
    }

    public void addMeshConfig(FSConfig config){
        meshconfigs.add(config);
    }

    public void addMeshConfig(VLListType<FSConfig> configs){
        meshconfigs.add(configs);
    }

    public void addPostDrawConfig(FSConfig config){
        postdrawconfigs.add(config);
    }

    public void addPostDrawConfig(VLListType<FSConfig> configs){
        postdrawconfigs.add(configs);
    }


    public void registerAttributeLocation(FSShader shader, FSConfig config){
        checkRegistration(config);
        config.location(shader.nextAttribLocation(config.getGLSLSize()));
    }

    public void registerUniformLocation(FSShader shader, FSConfig config){
        checkRegistration(config);
        config.location(nextUniformLocation(config.getGLSLSize()));
    }

    public void registerAttributeArrayLocation(FSShader shader, int arraysize, FSConfig config){
        checkRegistration(config);
        config.location(shader.nextAttribLocation(config.getGLSLSize() * arraysize));
    }

    public void registerUniformArrayLocation(FSShader shader, int arraysize, FSConfig config){
        checkRegistration(config);
        config.location(nextUniformLocation(config.getGLSLSize() * arraysize));
    }

    private void checkRegistration(FSConfig config){
        if(!(config instanceof FSConfigLocated)){
            throw new RuntimeException("All location-based configs need to be a subclass of FSConfigLocated.");
        }
    }


    public FSP build(){
        VLDebug.recreate();

        program = GLES32.glCreateProgram();

        FSShader s;
        String src;
        int size = shaders.size();

        for(int i = 0; i < size; i++){
            s = shaders.get(i);
            s.buildSource();
            s.compile();
            s.attach(this);

            if(FSControl.DEBUG_GLOBALLY && debug > FSControl.DEBUG_DISABLED){
                VLDebug.append("Compiling and attaching shader type ");
                VLDebug.append(s.type);
                VLDebug.append(" for program id ");
                VLDebug.append(program);
                VLDebug.append(" : \n");

                s.stringify(VLDebug.get(), null);
            }

            s.logDebugInfo(this);
        }

        GLES32.glLinkProgram(program);
        FSTools.checkGLError();

        for(int i = 0; i < size; i++){
            shaders.get(i).detach(this);
        }

        int[] results = new int[1];
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, results, 0);
        FSTools.checkGLError();

        if(results[0] != GLES32.GL_TRUE){
            String info = GLES32.glGetProgramInfoLog(program);
            FSTools.checkGLError();

            size = shaders.size();

            for(int i = 0; i < size; i++){
                s = shaders.get(i);

                VLDebug.append("[");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(size);
                VLDebug.append("]");
                VLDebug.append(" shaderType[");
                VLDebug.append(s.type);
                VLDebug.append("]");
                VLDebug.printE();

                s.stringify(VLDebug.get(), null);
            }

            VLDebug.append("Program[");
            VLDebug.append(program);
            VLDebug.append("] program build failure : ");
            VLDebug.append(info);
            VLDebug.printE();

            throw new RuntimeException();
        }

        if(FSControl.DEBUG_GLOBALLY && debug > FSControl.DEBUG_DISABLED){
            try{
                size = setupconfigs.size();
                VLDebug.append("Running programBuilt() for SetupConfigs\n");

                for(int i = 0; i < size; i++){
                    VLDebug.append("SetupConfig(");
                    VLDebug.append(i);
                    VLDebug.append(") : ");

                    setupconfigs.get(i).programBuilt(this);

                    VLDebug.append("Complete.\n");
                }

                size = meshconfigs.size();
                VLDebug.append("Running programBuilt() for MeshConfigs\n");

                for(int i = 0; i < size; i++){
                    VLDebug.append("MeshConfig(");
                    VLDebug.append(i);
                    VLDebug.append(") : ");

                    meshconfigs.get(i).programBuilt(this);

                    VLDebug.append("Complete.\n");
                }

                size = postdrawconfigs.size();
                VLDebug.append("Running programBuilt() for PostDrawConfigs\n");

                for(int i = 0; i < size; i++){
                    VLDebug.append("PostDrawConfig(");
                    VLDebug.append(i);
                    VLDebug.append(") : ");

                    postdrawconfigs.get(i).programBuilt(this);

                    VLDebug.append("Complete.\n");
                }

            }catch(Exception ex){
                VLDebug.append("Failed.\n");
                VLDebug.printE();

                throw new RuntimeException("Error during program configuration setup", ex);
            }

            VLDebug.append("ProgramID[");
            VLDebug.append(program);
            VLDebug.append("]\n");
            VLDebug.printD();

        }else{
            size = setupconfigs.size();

            for(int i = 0; i < size; i++){
                setupconfigs.get(i).programBuilt(this);
            }

            size = meshconfigs.size();

            for(int i = 0; i < size; i++){
                meshconfigs.get(i).programBuilt(this);
            }

            size = postdrawconfigs.size();

            for(int i = 0; i < size; i++){
                postdrawconfigs.get(i).programBuilt(this);
            }
        }

        return this;
    }

    public void releaseShaders(){
        int size = shaders.size();

        for(int i = 0; i < size; i++){
            shaders.get(i).delete();
        }

        shaders.clear();

        vertexshader = null;
        geometryshader = null;
        tesselcontrolshader = null;
        tesselevalshader = null;
        fragmentshader = null;
        computeshader = null;
    }

    public void draw(int passindex){
        if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
            try{
                FSTools.checkGLError();

            }catch(Exception ex){
                throw new RuntimeException("Pre-program-run error (there is an unchecked error somewhere before in the code)", ex);
            }
        }

        use();

        int meshsize = meshes.size();
        int setupconfigsize = setupconfigs.size();
        int meshconfigsize = meshconfigs.size();
        int postdrawconfigsize = postdrawconfigs.size();

        FSMesh mesh;
        FSConfig config;

        if(FSControl.DEBUG_GLOBALLY && debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();

            VLDebug.append("PROGRAM[");
            VLDebug.append(program);
            VLDebug.append("] [SetupConfig] size[");
            VLDebug.append(setupconfigsize);
            VLDebug.append("]");
            VLDebug.printD();

            for(int i = 0; i < setupconfigsize; i++){
                VLDebug.append("[");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(setupconfigsize);
                VLDebug.append("] ");

                config = setupconfigs.get(i);
                config.policy().configureDebug(config, this, null, -1, passindex);
            }

            for(int i = 0; i < meshsize; i++){
                mesh = meshes.get(i);

                VLDebug.append("PROGRAM[");
                VLDebug.append(program);
                VLDebug.append("] [MeshConfig] [");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(meshsize);
                VLDebug.append("] [");
                VLDebug.append(mesh.name());
                VLDebug.append("] size[");
                VLDebug.append(meshconfigsize);
                VLDebug.append("]");
                VLDebug.printD();

                for(int i2 = 0; i2 < meshconfigsize; i2++){
                    VLDebug.append("[");
                    VLDebug.append(i2 + 1);
                    VLDebug.append("/");
                    VLDebug.append(meshconfigsize);
                    VLDebug.append("] ");

                    config = meshconfigs.get(i2);
                    config.policy().configureDebug(config, this, mesh, i, passindex);
                }
            }

            VLDebug.append("PROGRAM[");
            VLDebug.append(program);
            VLDebug.append("] [PostDrawConfig] size[");
            VLDebug.append(postdrawconfigsize);
            VLDebug.append("]\n");
            VLDebug.printD();

            for(int i = 0; i < postdrawconfigsize; i++){
                VLDebug.append("[");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(postdrawconfigsize);
                VLDebug.append("] ");

                config = postdrawconfigs.get(i);
                config.policy().configureDebug(config, this, null, -1, passindex);
            }

            VLDebug.printD();

        }else{
            for(int i = 0; i < setupconfigsize; i++){
                config = setupconfigs.get(i);
                config.policy().configure(config, this, null, -1, passindex);
            }

            for(int i = 0; i < meshsize; i++){
                mesh = meshes.get(i);

                for(int i2 = 0; i2 < meshconfigsize; i2++){
                    config = meshconfigs.get(i2);
                    config.policy().configure(config, this, mesh, i, passindex);
                }
            }

            for(int i = 0; i < postdrawconfigsize; i++){
                config = postdrawconfigs.get(i);
                config.policy().configure(config, this, null, -1, passindex);
            }
        }
    }

    public void use(){
        GLES32.glUseProgram(program);

        if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
            try{
                FSTools.checkGLError();
            }catch(Exception ex){
                VLDebug.append("Error on program activation program[");
                VLDebug.append(program);
                VLDebug.append("]");
                VLDebug.printE();

                throw new RuntimeException(ex);
            }
        }
    }

    public int nextUniformLocation(int glslsize){
        int location = uniformlocation;
        uniformlocation += glslsize;

        return location;
    }

    public void unuse(){
        GLES32.glUseProgram(0);
    }

    public void bindAttribLocation(int index, String name){
        GLES32.glBindAttribLocation(program, index, name);
    }

    public void uniformBlockBinding(int location, int bindpoint){
        GLES32.glUniformBlockBinding(program, location, bindpoint);
    }

    public void shaderStorageBlockBinding(int location, int bindpoint){
        throw new RuntimeException("GLES 3.2 does not allow dynamic shader storage buffer index binding.");
    }

    public int getAttribLocation(String name){
        return GLES32.glGetAttribLocation(program, name);
    }

    public int getUniformLocation(String name){
        return GLES32.glGetUniformLocation(program, name);
    }

    public int getUniformBlockIndex(String name){
        return GLES32.glGetUniformBlockIndex(program, name);
    }

    public int getProgramResourceIndex(String name, int resourcetype){
        return GLES32.glGetProgramResourceIndex(program, resourcetype, name);
    }

    public QueryResults[] getAttributeList(int count){
        int[] ids = new int[count];
        GLES32.glGetProgramiv(program, GLES32.GL_ACTIVE_UNIFORMS, ids, 0);

        QueryResults[] data = new QueryResults[count];

        for(int i = 0; i < count; i++){
            data[i] = new QueryResults();

            GLES32.glGetActiveAttrib(program, i, QueryResults.BUFFER_SIZE, data[i].length, 0, data[i].size, 0, data[i].type, 0, data[i].name, 0);
        }

        return data;
    }

    public QueryResults[] getUniformList(int count){
        int[] ids = new int[count];
        GLES32.glGetProgramiv(program, GLES32.GL_ACTIVE_UNIFORMS, ids, 0);

        QueryResults[] data = new QueryResults[count];

        for(int i = 0; i < count; i++){
            GLES32.glGetActiveUniform(program, i, QueryResults.BUFFER_SIZE, data[i].length, 0, data[i].size, 0, data[i].type, 0, data[i].name, 0);
        }

        return data;
    }

    public void destroy(){
        GLES32.glDeleteProgram(program);

        releaseShaders();

        setupconfigs.clear();
        meshconfigs.clear();
        postdrawconfigs.clear();
        meshes.clear();
    }


    public static class MaterialDynamic extends FSConfigLocated{

        private int glslsize;

        public MaterialDynamic(Policy policy, int glslsize){
            super(policy);
            this.glslsize = glslsize;
        }

        public MaterialDynamic(int glslsize){
            this.glslsize = glslsize;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSLightMaterial mat = mesh.first().lightmaterial;

            mat.location(location);
            mat.configure(program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
            appendDebugHeader(program, mesh);

            FSLightMaterial mat = mesh.first().lightmaterial;

            mat.location(location);
            mat.configureDebug(program, mesh, meshindex, passindex);
        }

        @Override
        public int getGLSLSize(){
            return glslsize;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);
            mesh.first().lightmaterial.debugInfo(program, mesh, debug);
        }
    }

    public static class LightMapDynamic extends FSConfigLocated{

        private int glslsize;

        public LightMapDynamic(Policy policy, int glslsize){
            super(policy);
            this.glslsize = glslsize;
        }

        public LightMapDynamic(int glslsize){
            this.glslsize = glslsize;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSLightMap map = mesh.first().lightmap;

            map.location(location);
            map.configure(program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
            appendDebugHeader(program, mesh);

            FSLightMap map = mesh.first().lightmap;

            map.location(location);
            map.configureDebug(program, mesh, meshindex, passindex);
        }

        @Override
        public int getGLSLSize(){
            return glslsize;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);
            mesh.first().lightmaterial.debugInfo(program, mesh, debug);
        }

    }

    public static class MaterialDynamicInstanced extends FSConfigLocated{

        private int glslsize;
        private int instancecount;

        public MaterialDynamicInstanced(Policy policy, int glslsize, int instancecount){
            super(policy);

            this.glslsize = glslsize;
            this.instancecount = instancecount;
        }

        public MaterialDynamicInstanced(int glslsize, int instancecount){
            this.glslsize = glslsize;
            this.instancecount = instancecount;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            int size = mesh.size();
            FSLightMaterial mat;

            for(int i = 0; i < size; i++){
                mat = mesh.instance(i).lightmaterial;
                mat.location(location);
                mat.configure(program, mesh, meshindex, passindex);
            }
        }

        @Override
        public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
            appendDebugHeader(program, mesh);

            int size = mesh.size();
            FSLightMaterial mat;

            for(int i = 0; i < size; i++){
                mat = mesh.instance(i).lightmaterial;
                mat.location(location);
                mat.configureDebug(program, mesh, meshindex, passindex);
            }
        }

        @Override
        public int getGLSLSize(){
            return glslsize * instancecount;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                VLDebug.append("Instance[i] [LightMaterial] ");
                mesh.instance(i).lightmaterial.debugInfo(program, mesh, debug);
            }
        }
    }

    public static class LightMapDynamicInstanced extends FSConfigLocated{

        private int glslsize;
        private int instancecount;

        public LightMapDynamicInstanced(Policy policy, int glslsize, int instancecount){
            super(policy);

            this.glslsize = glslsize;
            this.instancecount = instancecount;
        }

        public LightMapDynamicInstanced(int glslsize, int instancecount){
            this.glslsize = glslsize;
            this.instancecount = instancecount;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            int size = mesh.size();
            FSLightMap map;

            for(int i = 0; i < size; i++){
                map = mesh.instance(i).lightmap;
                map.location(location);
                map.configure(program, mesh, meshindex, passindex);
            }
        }

        @Override
        public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
            appendDebugHeader(program, mesh);

            int size = mesh.size();
            FSLightMap map;

            for(int i = 0; i < size; i++){
                map = mesh.instance(i).lightmap;
                map.location(location);
                map.configureDebug(program, mesh, meshindex, passindex);
            }
        }

        @Override
        public int getGLSLSize(){
            return glslsize * instancecount;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            int size = mesh.size();

            for(int i = 0; i < size; i++){
                VLDebug.append("Instance[i] [LightMap] ");
                mesh.instance(i).lightMap().debugInfo(program, mesh, debug);
            }
        }
    }

    public static class ViewPort extends FSConfig{

        public FSViewConfig config;
        public int x;
        public int y;
        public int width;
        public int height;

        public ViewPort(Policy policy, FSViewConfig config, int x, int y, int width, int height){
            super(policy);

            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.config = config;
        }

        public ViewPort(FSViewConfig config, int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.config = config;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            config.viewPort(x, y, width, height);
            config.updateViewPort();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x);
            VLDebug.append("], y[");
            VLDebug.append(y);
            VLDebug.append("], width[");
            VLDebug.append(width);
            VLDebug.append("], height[");
            VLDebug.append(height);
        }
    }

    public static class DepthMask extends FSConfig{

        public boolean mask;

        public DepthMask(Policy policy, boolean mask){
            super(policy);

            this.mask = mask;
        }

        public DepthMask(boolean mask){
            this.mask = mask;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSR.depthMask(mask);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("mask[");
            VLDebug.append(mask);
            VLDebug.append("] ");
        }
    }

    public static class CullFace extends FSConfig{

        public int mode;

        public CullFace(Policy policy, int mode){
            super(policy);
            this.mode = mode;
        }

        public CullFace(int mode){
            this.mode = mode;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSR.cullFace(mode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("mode[");
            VLDebug.append(mode);
            VLDebug.append("] ");
        }
    }

    public static class AttribDivisor extends FSConfigLocated{

        public int divisor;

        public AttribDivisor(Policy policy, int divisor){
            super(policy);
            this.divisor = divisor;
        }

        public AttribDivisor(int divisor){
            this.divisor = divisor;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribDivisor(location, divisor);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("divisor[");
            VLDebug.append(divisor);
            VLDebug.append("] ");
        }
    }

    public static class ReadBuffer extends FSConfig{

        public int mode;

        public ReadBuffer(Policy policy, int mode){
            super(policy);
            this.mode = mode;
        }

        public ReadBuffer(int mode){
            this.mode = mode;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSR.readBuffer(mode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("mode[");
            VLDebug.append(mode);
            VLDebug.append("] ");
        }
    }

    public static class AttribEnable extends FSConfigLocated{

        public AttribEnable(Policy policy, int location){
            super(policy, location);
        }

        public AttribEnable(int location){
            super(location);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glEnableVertexAttribArray(location);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }
    }

    public static class AttribDisable extends FSConfigLocated{

        public AttribDisable(Policy policy, int location){
            super(policy, location);
        }

        public AttribDisable(int location){
            super(location);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDisableVertexAttribArray(location);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }
    }

    public static class AttribPointer extends FSConfigLocated{

        public int element;
        public int bufferaddressindex;

        public AttribPointer(Policy policy, int element, int bufferaddressindex){
            super(policy);

            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
        }

        public AttribPointer(int element, int bufferaddressindex){
            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferAddress o = mesh.first().bufferTracker().get(element, bufferaddressindex);

            int databytesize = FSG.ELEMENT_BYTES[element];

            o.bind();
            GLES32.glVertexAttribPointer(location, o.unitSize(), FSG.ELEMENT_GLDATA_TYPES[element], false, o.stride() * databytesize, o.offset() * databytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSG.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(element, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class AttribIPointer extends FSConfigLocated{

        public int element;
        public int bufferaddressindex;

        public AttribIPointer(Policy policy, int element){
            super(policy);

            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
        }

        public AttribIPointer(int element){
            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferAddress o = mesh.first().bufferTracker().get(element, bufferaddressindex);

            int databytesize = FSG.ELEMENT_BYTES[element];

            o.bind();
            GLES32.glVertexAttribIPointer(location, o.unitSize(), FSG.ELEMENT_GLDATA_TYPES[element], o.stride() * databytesize, o.offset() * databytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSG.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(element, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class UniformMatrix4fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public UniformMatrix4fvd(Policy policy, VLArrayFloat array, int offset, int count){
            super(policy, array, offset, count);
        }

        public UniformMatrix4fvd(VLArrayFloat array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class UniformMatrix4fve extends FSConfigArrayElement<VLArrayFloat>{

        public UniformMatrix4fve(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public UniformMatrix4fve(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class Uniform4fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Uniform4fvd(Policy policy, VLArrayFloat array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform4fvd(VLArrayFloat array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4fve extends FSConfigArrayElement<VLArrayFloat>{

        public Uniform4fve(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform4fve(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);

            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Uniform3fvd(Policy policy, VLArrayFloat array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform3fvd(VLArrayFloat array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fve extends FSConfigArrayElement<VLArrayFloat>{

        public Uniform3fve(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform3fve(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Uniform2fvd(Policy policy, VLArrayFloat array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform2fvd(VLArrayFloat array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fve extends FSConfigArrayElement<VLArrayFloat>{

        public Uniform2fve(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform2fve(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Uniform1fvd(Policy policy, VLArrayFloat array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform1fvd(VLArrayFloat array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fve extends FSConfigArrayElement<VLArrayFloat>{

        public Uniform1fve(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform1fve(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform1fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4f extends FSConfigLocated{

        public VLFloat x;
        public VLFloat y;
        public VLFloat z;
        public VLFloat w;

        public Uniform4f(Policy policy, VLFloat x, VLFloat y, VLFloat z, VLFloat w){
            super(policy);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public Uniform4f(VLFloat x, VLFloat y, VLFloat z, VLFloat w){
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4f(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("], z[");
            VLDebug.append(z.get());
            VLDebug.append("], w[");
            VLDebug.append(w.get());
            VLDebug.append("] ");
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }
    }

    public static class Uniform3f extends FSConfigLocated{

        public VLFloat x;
        public VLFloat y;
        public VLFloat z;

        public Uniform3f(Policy policy, VLFloat x, VLFloat y, VLFloat z){
            super(policy);

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Uniform3f(VLFloat x, VLFloat y, VLFloat z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3f(location, x.get(), y.get(), z.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("], z[");
            VLDebug.append(z.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform2f extends FSConfigLocated{

        public VLFloat x;
        public VLFloat y;

        public Uniform2f(Policy policy, VLFloat x, VLFloat y){
            super(policy);

            this.x = x;
            this.y = y;
        }

        public Uniform2f(VLFloat x, VLFloat y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2f(location, x.get(), y.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform1f extends FSConfigLocated{

        public VLFloat x;

        public Uniform1f(Policy policy, VLFloat x){
            super(policy);
            this.x = x;
        }

        public Uniform1f(VLFloat x){
            this.x = x;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1f(location, x.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform4ivd extends FSConfigArrayDirect<VLArrayInt>{

        public Uniform4ivd(Policy policy, VLArrayInt array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform4ivd(VLArrayInt array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4ive extends FSConfigArrayElement<VLArrayInt>{

        public Uniform4ive(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform4ive(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ivd extends FSConfigArrayDirect<VLArrayInt>{

        public Uniform3ivd(Policy policy, VLArrayInt array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform3ivd(VLArrayInt array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ive extends FSConfigArrayElement<VLArrayInt>{

        public Uniform3ive(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform3ive(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ivd extends FSConfigArrayDirect<VLArrayInt>{

        public Uniform2ivd(Policy policy, VLArrayInt array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform2ivd(VLArrayInt array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ive extends FSConfigArrayElement<VLArrayInt>{

        public Uniform2ive(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform2ive(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ivd extends FSConfigArrayDirect<VLArrayInt>{

        public Uniform1ivd(Policy policy, VLArrayInt array, int offset, int count){
            super(policy, array, offset, count);
        }

        public Uniform1ivd(VLArrayInt array, int offset, int count){
            super(array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ive extends FSConfigArrayElement<VLArrayInt>{

        public Uniform1ive(Policy policy, int instance, int element, int offset, int count){
            super(policy, element, instance, offset, count);
        }

        public Uniform1ive(int instance, int element, int offset, int count){
            super(element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform1iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;
        public VLInt w;

        public Uniform4i(Policy policy, VLInt x, VLInt y, VLInt z, VLInt w){
            super(policy);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public Uniform4i(VLInt x, VLInt y, VLInt z, VLInt w){
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4i(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("], z[");
            VLDebug.append(z.get());
            VLDebug.append("], w[");
            VLDebug.append(w.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform3i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;

        public Uniform3i(Policy policy, VLInt x, VLInt y, VLInt z){
            super(policy);

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Uniform3i(VLInt x, VLInt y, VLInt z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3i(location, x.get(), y.get(), z.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("], z[");
            VLDebug.append(z.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform2i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;

        public Uniform2i(Policy policy, VLInt x, VLInt y){
            super(policy);

            this.x = x;
            this.y = y;
        }

        public Uniform2i(VLInt x, VLInt y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2i(location, x.get(), y.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("] ");
        }
    }

    public static class Uniform1i extends FSConfigLocated{

        public VLInt x;

        public Uniform1i(Policy policy, VLInt x){
            super(policy);
            this.x = x;
        }

        public Uniform1i(VLInt x){
            this.x = x;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1i(location, x.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("] ");
        }
    }

    public static class Attrib4fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Attrib4fvd(Policy policy, VLArrayFloat array, int offset){
            super(policy, array, offset, 4);
        }

        public Attrib4fvd(VLArrayFloat array, int offset){
            super(array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib4fve extends FSConfigArrayElement<VLArrayFloat>{

        public Attrib4fve(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 4);
        }

        public Attrib4fve(int instance, int element, int offset){
            super(element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Attrib3fvd(Policy policy, VLArrayFloat array, int offset){
            super(policy, array, offset, 3);
        }

        public Attrib3fvd(VLArrayFloat array, int offset){
            super(array, offset, 3);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fve extends FSConfigArrayElement<VLArrayFloat>{

        public Attrib3fve(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 3);
        }

        public Attrib3fve(int instance, int element, int offset){
            super(element, instance, offset, 3);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Attrib2fvd(Policy policy, VLArrayFloat array, int offset){
            super(policy, array, offset, 2);
        }

        public Attrib2fvd(VLArrayFloat array, int offset){
            super(array, offset, 2);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fve extends FSConfigArrayElement<VLArrayFloat>{

        public Attrib2fve(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 2);
        }

        public Attrib2fve(int instance, int element, int offset){
            super(element, instance, offset, 2);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fvd extends FSConfigArrayDirect<VLArrayFloat>{

        public Attrib1fvd(Policy policy, VLArrayFloat array, int offset){
            super(policy, array, offset, 1);
        }

        public Attrib1fvd(VLArrayFloat array, int offset){
            super(array, offset, 1);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib1fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fve extends FSConfigArrayElement<VLArrayFloat>{

        public Attrib1fve(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 1);
        }

        public Attrib1fve(int instance, int element, int offset){
            super(element, instance, offset, 1);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib1fv(location, array().provider(), offset());
        }
    }

    public static class AttribI4i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;
        public VLInt w;

        public AttribI4i(Policy policy, VLInt x, VLInt y, VLInt z, VLInt w){
            super(policy);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public AttribI4i(VLInt x, VLInt y, VLInt z, VLInt w){
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4i(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("x[");
            VLDebug.append(x.get());
            VLDebug.append("], y[");
            VLDebug.append(y.get());
            VLDebug.append("], z[");
            VLDebug.append(z.get());
            VLDebug.append("], w[");
            VLDebug.append(w.get());
            VLDebug.append("] ");
        }
    }

    public static class AttribI4ivd extends FSConfigArrayDirect<VLArrayInt>{

        public AttribI4ivd(Policy policy, VLArrayInt array, int offset){
            super(policy, array, offset, 4);
        }

        public AttribI4ivd(VLArrayInt array, int offset){
            super(array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4ive extends FSConfigArrayElement<VLArrayInt>{

        public AttribI4ive(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 4);
        }

        public AttribI4ive(int instance, int element, int offset){
            super(element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uivd extends FSConfigArrayDirect<VLArrayInt>{

        public AttribI4uivd(Policy policy, VLArrayInt array, int offset){
            super(policy, array, offset, 4);
        }

        public AttribI4uivd(VLArrayInt array, int offset){
            super(array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uive extends FSConfigArrayElement<VLArrayInt>{

        public AttribI4uive(Policy policy, int instance, int element, int offset){
            super(policy, element, instance, offset, 4);
        }

        public AttribI4uive(int instance, int element, int offset){
            super(element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class UniformBlockElement extends FSConfigLocated{

        public int element;
        public int bufferaddressindex;

        protected String name;

        public UniformBlockElement(Policy policy, int element, String name, int bufferaddressindex){
            super(policy);

            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
            this.name = name;
        }

        public UniformBlockElement(int element, String name, int bufferaddressindex){
            this.element = element;
            this.bufferaddressindex = bufferaddressindex;
            this.name = name;
        }

        @Override
        protected void programBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSVertexBuffer buffer = mesh.first().bufferTracker().get(element, bufferaddressindex).target().vertexBuffer();

            program.uniformBlockBinding(location, buffer.bindPoint());
            buffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSG.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(element, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class UniformBlockData extends FSConfigLocated{

        public FSBufferAddress address;
        protected String name;

        public UniformBlockData(Policy policy, FSBufferAddress address, String name){
            super(policy);

            this.address = address;
            this.name = name;
        }

        public UniformBlockData(FSBufferAddress address, String name){
            this.address = address;
            this.name = name;
        }

        @Override
        protected void programBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSVertexBuffer buffer = address.target().vertexBuffer();
            program.uniformBlockBinding(location, buffer.bindPoint());
            buffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[NONE] ");
            address.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);
            VLDebug.append("] ");
        }
    }

    public static class TextureBind extends FSConfig{

        public FSTexture texture;

        public TextureBind(Policy policy, FSTexture texture){
            super(policy);
            this.texture = texture;
        }

        public TextureBind(FSTexture texture){
            this.texture = texture;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            texture.activateUnit();
            texture.bind();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);
            VLDebug.append("[DYNAMIC]");
        }
    }

    public static class TextureColorBind extends FSConfig{

        public TextureColorBind(Policy policy){
            super(policy);
        }

        public TextureColorBind(){

        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSTexture t = mesh.first().colortexture;
            t.activateUnit();
            t.bind();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);
            VLDebug.append("[DYNAMIC]");
        }
    }

    public static class TextureColorUnit extends FSConfigLocated{

        public TextureColorUnit(Policy policy){
            super(policy);
        }

        public TextureColorUnit(){

        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1i(location, mesh.first().colortexture.unit().get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);
            VLDebug.append("[DYNAMIC]");
        }
    }

    public static class DrawArrays extends FSConfig{

        public DrawArrays(Policy policy){
            super(policy);
        }

        public DrawArrays(){

        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDrawArrays(mesh.drawmode, 0, mesh.first().vertexSize());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("drawMode[");
            VLDebug.append(mesh.drawmode);
            VLDebug.append("] indexCount[");
            VLDebug.append(mesh.first().vertexSize());
            VLDebug.append("] ");
        }
    }

    public static class DrawArraysInstanced extends FSConfig{

        public DrawArraysInstanced(Policy policy){
            super(policy);
        }

        public DrawArraysInstanced(){

        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDrawArraysInstanced(mesh.drawmode, 0, mesh.first().vertexSize(), mesh.size());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("drawMode[");
            VLDebug.append(mesh.drawmode);
            VLDebug.append("] indexCount[");
            VLDebug.append(mesh.first().vertexSize());
            VLDebug.append("] instanceCount[");
            VLDebug.append(mesh.size());
            VLDebug.append("] ");
        }
    }

    public static class DrawElements extends FSConfig{

        public int bufferaddressindex;

        public DrawElements(Policy policy, int bufferaddressindex){
            super(policy);
            this.bufferaddressindex = bufferaddressindex;
        }

        public DrawElements(int bufferaddressindex){
            this.bufferaddressindex = bufferaddressindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferAddress address = mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex);

            address.bind();
            GLES32.glDrawElements(mesh.drawmode, address.count(), FSG.ELEMENT_GLDATA_TYPES[FSG.ELEMENT_INDEX], address.offset() * FSG.ELEMENT_BYTES[FSG.ELEMENT_INDEX]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSG.ELEMENT_NAMES[FSG.ELEMENT_INDEX]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class DrawElementsInstanced extends FSConfig{

        public int bufferaddressindex;

        public DrawElementsInstanced(Policy policy, int bufferaddressindex){
            super(policy);
            this.bufferaddressindex = bufferaddressindex;
        }

        public DrawElementsInstanced(int bufferaddressindex){
            this.bufferaddressindex = bufferaddressindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferAddress address = mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex);

            address.bind();
            GLES32.glDrawElementsInstanced(mesh.drawmode, address.count(), FSG.ELEMENT_GLDATA_TYPES[FSG.ELEMENT_INDEX], address.offset() * FSG.ELEMENT_BYTES[FSG.ELEMENT_INDEX], mesh.size());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("drawMode[");
            VLDebug.append(mesh.drawmode);
            VLDebug.append("] instanceCount[");
            VLDebug.append(mesh.size());
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class DrawRangeElements extends FSConfig{

        public int start;
        public int end;
        public int count;
        public int bufferaddressindex;

        public DrawRangeElements(Policy policy, int start, int end, int count, int bufferaddressindex){
            super(policy);

            this.start = start;
            this.end = end;
            this.count = count;
            this.bufferaddressindex = bufferaddressindex;
        }

        public DrawRangeElements(int start, int end, int count, int bufferaddressindex){
            this.start = start;
            this.end = end;
            this.count = count;
            this.bufferaddressindex = bufferaddressindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferAddress address = mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex);

            address.bind();
            GLES32.glDrawRangeElements(mesh.drawmode, start, end, count, FSG.ELEMENT_GLDATA_TYPES[FSG.ELEMENT_INDEX], address.offset() * FSG.ELEMENT_BYTES[FSG.ELEMENT_INDEX]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("drawMode[");
            VLDebug.append(mesh.drawmode);
            VLDebug.append("] start[");
            VLDebug.append(start);
            VLDebug.append("] end[");
            VLDebug.append(end);
            VLDebug.append("] count[");
            VLDebug.append(count);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferaddressindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferTracker().get(FSG.ELEMENT_INDEX, bufferaddressindex).stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class QueryResults implements VLStringify{

        public static int BUFFER_SIZE = 30;

        public int[] length = new int[1];
        public int[] size = new int[1];
        public int[] type = new int[1];
        public byte[] name = new byte[BUFFER_SIZE];

        private QueryResults(){

        }

        @Override
        public void stringify(StringBuilder src, Object hint){
            try{
                src.append("[countRow : ");
                src.append(length[0]);
                src.append(" size : ");
                src.append(size[0]);
                src.append(" type : ");
                src.append(type[0]);
                src.append(" name : ");
                src.append(new String(VLArrayUtils.slice(name, 0, length[0]), "UTF-8"));
                src.append(" ]");

            }catch(UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
    }
}
