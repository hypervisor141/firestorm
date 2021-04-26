package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import vanguard.VLArray;
import vanguard.VLArrayFloat;
import vanguard.VLArrayInt;
import vanguard.VLArrayUtils;
import vanguard.VLBufferTracker;
import vanguard.VLFloat;
import vanguard.VLInt;
import vanguard.VLListType;
import vanguard.VLLog;
import vanguard.VLStringify;

public abstract class FSP{

    private static final int BUFFER_PRINT_LIMIT = 50;

    protected VLListType<FSShader> shaders;
    protected VLListType<FSMesh> meshes;

    protected CoreConfig coreconfigs;

    protected final VLLog log;
    protected int program;
    protected int debug;
    protected int uniformlocation;

    public FSP(int shadercapacity, int meshcapacity, int debugmode){
        program = -1;
        debug = debugmode;

        shaders = new VLListType<>(shadercapacity, shadercapacity);
        meshes = new VLListType<>(meshcapacity, meshcapacity);

        uniformlocation = 0;

        log = new VLLog(FSControl.LOGTAG, 2);
    }

    protected abstract CoreConfig customize(VLListType<FSMesh> meshes, int debug);

    public VLListType<FSMesh> meshes(){
        return meshes;
    }

    public CoreConfig coreConfigs(){
        return coreconfigs;
    }

    public VLLog log(){
        return log;
    }

    public int id(){
        return program;
    }

    public FSP build(){
        log.tag(1, getClass().getSimpleName());
        log.reset();

        coreconfigs = customize(meshes, debug);

        int size = meshes.size();

        for(int i = 0; i < size; i++){
            meshes.get(i).programPreBuild(this, coreconfigs, debug);
        }

        log.printInfo();

        program = GLES32.glCreateProgram();

        log.tag(1, getClass().getSimpleName() + "-" + program);

        size = shaders.size();

        for(int i = 0; i < size; i++){
            FSShader shader = shaders.get(i);
            shader.buildSource();
            shader.compile();
            shader.attach();

            if(debug > FSControl.DEBUG_DISABLED){
                log.append("[Compiling and attaching shader] type[");
                log.append(shader.type);
                log.append("] ");

                shader.stringify(log.get(), null);
            }

            shader.debugInfo(this, log, debug);
        }

        log.printInfo();

        GLES32.glLinkProgram(program);
        FSTools.checkGLError();

        for(int i = 0; i < size; i++){
            shaders.get(i).detach();
        }

        int[] results = new int[1];
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, results, 0);
        FSTools.checkGLError();

        if(results[0] != GLES32.GL_TRUE){
            String info = GLES32.glGetProgramInfoLog(program);
            FSTools.checkGLError();

            size = shaders.size();

            for(int i = 0; i < size; i++){
                FSShader shader = shaders.get(i);

                log.append("[");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]");
                log.append(" shaderType[");
                log.append(shader.type);
                log.append("]");
                log.printError();

                shader.stringify(log.get(), null);
            }

            log.append("Program[");
            log.append(program);
            log.append("] program build failure : ");
            log.append(info);
            log.printError();

            throw new RuntimeException();
        }

        if(debug > FSControl.DEBUG_DISABLED){
            try{
                if(coreconfigs.setupconfig != null){
                    log.append("[Notifying program built for SetupConfig]\n");
                    coreconfigs.setupconfig.notifyProgramBuilt(this);
                }
                if(coreconfigs.meshconfig != null){
                    log.append("[Notifying program built for MeshConfig]\n");
                    coreconfigs.meshconfig.notifyProgramBuilt(this);
                }
                if(coreconfigs.postdrawconfig != null){
                    log.append("[Notifying program built for PostDrawConfig]\n");
                    coreconfigs.postdrawconfig.notifyProgramBuilt(this);
                }

            }catch(Exception ex){
                log.append("Failed.\n");
                log.printError();

                throw new RuntimeException("Error during program configuration setup", ex);
            }

            log.printInfo();

        }else{
            if(coreconfigs.setupconfig != null){
                coreconfigs.setupconfig.notifyProgramBuilt(this);
            }
            if(coreconfigs.meshconfig != null){
                coreconfigs.meshconfig.notifyProgramBuilt(this);
            }
            if(coreconfigs.postdrawconfig != null){
                coreconfigs.postdrawconfig.notifyProgramBuilt(this);
            }
        }

        return this;
    }

    public void releaseShaders(){
        int size = shaders.size();

        for(int i = 0; i < size; i++){
            shaders.get(i).delete();
        }

        shaders = null;
    }

    public void draw(FSRPass pass, int passindex){
        if(debug >= FSControl.DEBUG_NORMAL){
            try{
                FSTools.checkGLError();

            }catch(Exception ex){
                throw new RuntimeException("Pre-program-run error (there is an unchecked error somewhere before in the code)", ex);
            }
        }

        use();

        int meshsize = meshes.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log.reset();
            log.append("------- PROGRAM[");
            log.append(program);
            log.append("] -------");
            log.printInfo();

            if(coreconfigs.setupconfig != null){
                log.append("[SetupConfig]");
                log.printInfo();

                coreconfigs.setupconfig.runDebug(pass, this, null, -1, passindex, log, debug);

                log.printInfo();
            }

            if(coreconfigs.meshconfig != null){
                for(int i = 0; i < meshsize; i++){
                    FSMesh mesh = meshes.get(i);

                    log.append("[InternalMeshConfig] [");
                    log.append(i + 1);
                    log.append("/");
                    log.append(meshsize);
                    log.append("] [");
                    log.append(mesh.name());
                    log.append("]");
                    log.printInfo();

                    mesh.configureDebug(pass, this, i, passindex, log, debug);

                    log.append("[MeshConfig] [");
                    log.append(i + 1);
                    log.append("/");
                    log.append(meshsize);
                    log.append("] [");
                    log.append(mesh.name());
                    log.append("]");
                    log.printInfo();

                    coreconfigs.meshconfig.runDebug(pass, this, mesh, i, passindex, log, debug);

                    log.printInfo();
                }

                log.append("[PostDrawConfig]");
                log.printInfo();
            }

            if(coreconfigs.postdrawconfig != null){
                coreconfigs.postdrawconfig.runDebug(pass, this, null, -1, passindex, log, debug);

                log.printInfo();
            }

        }else{
            if(coreconfigs.setupconfig != null){
                coreconfigs.setupconfig.run(pass, this, null, -1, passindex);
            }

            if(coreconfigs.meshconfig != null){
                for(int i = 0; i < meshsize; i++){
                    FSMesh mesh = meshes.get(i);
                    mesh.configure(pass, this, i, passindex);
                    coreconfigs.meshconfig.run(pass, this, mesh, i, passindex);
                }
            }

            if(coreconfigs.postdrawconfig != null){
                coreconfigs.postdrawconfig.run(pass, this, null, -1, passindex);
            }
        }
    }

    public void postFrame(FSRPass pass, int passindex){
        if(coreconfigs.postframeconfig != null){
            if(debug >= FSControl.DEBUG_NORMAL){
                try{
                    FSTools.checkGLError();

                }catch(Exception ex){
                    throw new RuntimeException("Pre-program-run error (there is an unchecked error somewhere before in the code)", ex);
                }
            }

            use();

            if(debug > FSControl.DEBUG_DISABLED){
                log.reset();
                log.append("------- PROGRAM[");
                log.append(program);
                log.append("] -------");
                log.printInfo();

                log.append("[PostFrameConfig]");
                log.printInfo();

                coreconfigs.postframeconfig.runDebug(pass, this, null, -1, passindex, log, debug);

                log.printInfo();

            }else{
                coreconfigs.postframeconfig.run(pass, this, null, -1, passindex);
            }
        }
    }

    public void use(){
        GLES32.glUseProgram(program);

        if(debug >= FSControl.DEBUG_NORMAL){
            try{
                FSTools.checkGLError();

            }catch(Exception ex){
                log.append("Error on program activation program[");
                log.append(program);
                log.append("]");
                log.printError();

                throw new RuntimeException(ex);
            }
        }
    }

    protected int nextUniformLocation(int glslsize){
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

        coreconfigs = null;

        meshes.clear();
    }

    public static class CoreConfig{
        
        public FSConfigGroup setupconfig;
        public FSConfigGroup meshconfig;
        public FSConfigGroup postdrawconfig;
        public FSConfigGroup postframeconfig;

        public CoreConfig(FSConfigGroup setupconfig, FSConfigGroup meshconfig, FSConfigGroup postdrawconfig, FSConfigGroup postframeconfig){
            this.setupconfig = setupconfig;
            this.meshconfig = meshconfig;
            this.postdrawconfig = postdrawconfig;
            this.postframeconfig = postframeconfig;
        }
    }

    public static class Clear extends FSConfig{

        public int flag;

        public Clear(Mode mode, int flag){
            super(mode);
            this.flag = flag;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glClear(flag);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append(" flag[");
            log.append(flag);
            log.append("]");
        }
    }

    public static class ClearColor extends FSConfig{

        public float[] color;

        public ClearColor(Mode mode, float[] color){
            super(mode);
            this.color = color;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glClearColor(color[0], color[1], color[2], color[3]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append(" color[");
            log.append(Arrays.toString(color));
            log.append("]");
        }
    }

    public static class ViewPort extends FSConfig{

        public FSView config;
        public int x;
        public int y;
        public int width;
        public int height;

        public ViewPort(Mode mode, FSView config, int x, int y, int width, int height){
            super(mode);

            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.config = config;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            config.viewPort(x, y, width, height);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x);
            log.append("], y[");
            log.append(y);
            log.append("], width[");
            log.append(width);
            log.append("], height[");
            log.append(height);
        }
    }

    public static class DepthMask extends FSConfig{

        public boolean mask;

        public DepthMask(Mode mode, boolean mask){
            super(mode);
            this.mask = mask;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDepthMask(mask);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("mask[");
            log.append(mask);
            log.append("] ");
        }
    }

    public static class CullFace extends FSConfig{

        public int cullmode;

        public CullFace(Mode mode, int cullmode){
            super(mode);
            this.cullmode = cullmode;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glCullFace(cullmode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("mode[");
            log.append(cullmode);
            log.append("] ");
        }
    }

    public static class AttribDivisor extends FSConfigLocated{

        public int divisor;

        public AttribDivisor(Mode mode, int divisor){
            super(mode);
            this.divisor = divisor;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribDivisor(location, divisor);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("divisor[");
            log.append(divisor);
            log.append("] ");
        }
    }

    public static class ReadBuffer extends FSConfig{

        public int readmode;

        public ReadBuffer(Mode mode, int readmode){
            super(mode);
            this.readmode = readmode;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glReadBuffer(readmode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("mode[");
            log.append(readmode);
            log.append("] ");
        }
    }

    public static class AttribEnable extends FSConfigLocated{

        public AttribEnable(Mode mode, int location){
            super(mode, location);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glEnableVertexAttribArray(location);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }
    }

    public static class AttribDisable extends FSConfigLocated{

        public AttribDisable(Mode mode, int location){
            super(mode, location);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDisableVertexAttribArray(location);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }
    }

    public abstract static class Array<TYPE extends VLArray<?, ?>> extends FSConfigLocated{

        protected TYPE array;
        protected int offset;
        protected int count;

        public Array(Mode mode, TYPE array, int offset, int count){
            super(mode);

            this.array = array;
            this.offset = offset;
            this.count = count;
        }

        public final void offset(int s){
            offset = s;
        }

        public final void count(int s){
            offset = s;
        }

        public final void array(TYPE array){
            this.array = array;
        }


        public final int offset(){
            return offset;
        }

        public final int count(){
            return count;
        }

        public int instance(){
            return -1;
        }

        public int element(){
            return -1;
        }

        public final TYPE array(){
            return array;
        }

        @Override
        public abstract void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug);

        @Override
        public int getGLSLSize(){
            return count;
        }
    }

    public abstract static class ArrayDirect<TYPE extends VLArray<?, ?>> extends Array<TYPE>{

        public ArrayDirect(Mode mode, TYPE array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            log.append("offset[");
            log.append(offset);
            log.append("] count[");
            log.append(count);
            log.append("] array[");

            array.stringify(log.get(), null);

            log.append("]");
        }
    }

    public abstract static class ArrayElement<TYPE extends VLArray<?, ?>> extends Array<TYPE>{

        private int instance;
        private int element;

        public ArrayElement(Mode mode, int element, int instance, int offset, int count){
            super(mode, null, offset, count);

            this.element = element;
            this.instance = instance;
        }

        public final void set(int instance, int element){
            this.element = element;
            this.instance = instance;
        }

        public final int element(){
            return element;
        }

        public final int instance(){
            return instance;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            array = (TYPE)mesh.get(instance).element(element);
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            log.append("] instance[");
            log.append(instance);
            log.append("] element[");
            log.append(FSHub.ELEMENT_NAMES[element]);
            log.append("] array[");

            if(array == null){
                mesh.get(instance).element(element).stringify(log.get(), null);

            }else{
                array.stringify(log.get(), null);
            }

            log.append("]");
        }
    }

    public static class AttribPointer extends FSConfigLocated{

        public int element;
        public int unitsize;
        public int bindingindex;
        public boolean normalized;

        public AttribPointer(Mode mode, int element, int unitsize, int bindingindex, boolean normalized){
            super(mode);

            this.element = element;
            this.unitsize = unitsize;
            this.bindingindex = bindingindex;
            this.normalized = normalized;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(this.element, this.bindingindex);
            VLBufferTracker tracker = binding.tracker;

            int bytesize = tracker.typebytesize;

            binding.vbuffer.bind();

            GLES32.glVertexAttribPointer(location, unitsize, FSHub.ELEMENT_GLDATA_TYPES[element], normalized, tracker.stride * bytesize, tracker.offset * bytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("element[");
            log.append(element);
            log.append("bindingIndex[");
            log.append(bindingindex);
            log.append("] normalized[");
            log.append(normalized);
            log.append("] tracker[");

            try{
                FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(this.element, this.bindingindex);

                binding.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                binding.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class AttribPointerLink extends FSConfigLocated{

        public int linkindex;
        public int unitsize;
        public boolean normalized;

        public AttribPointerLink(Mode mode, int linkindex, int unitsize, boolean normalized){
            super(mode);

            this.linkindex = linkindex;
            this.unitsize = unitsize;
            this.normalized = normalized;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSLinkGLBuffered<?, ?> link = (FSLinkGLBuffered<?, ?>)mesh.links.get(linkindex);
            VLBufferTracker tracker = link.tracker;
            int bytesize = tracker.typebytesize;

            link.vbuffer.bind();

            GLES32.glVertexAttribPointer(location, unitsize, link.gldatatype, normalized, tracker.stride * bytesize, tracker.offset * bytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("linkIndex[");
            log.append(linkindex);
            log.append("] normalized[");
            log.append(normalized);
            log.append("] tracker[");

            try{
                FSLinkGLBuffered<?, ?> link = (FSLinkGLBuffered<?, ?>)mesh.links.get(linkindex);

                link.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                link.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class AttribIPointer extends FSConfigLocated{

        public int element;
        public int unitsize;
        public int bindingindex;

        public AttribIPointer(Mode mode, int element, int unitsize, int bindingindex){
            super(mode);

            this.element = element;
            this.bindingindex = bindingindex;
            this.unitsize = unitsize;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(this.element, this.bindingindex);
            VLBufferTracker tracker = (VLBufferTracker)binding.tracker;

            int bytesize = tracker.typebytesize;

            binding.vbuffer.bind();

            GLES32.glVertexAttribIPointer(location, unitsize, FSHub.ELEMENT_GLDATA_TYPES[element], tracker.stride * bytesize, tracker.offset * bytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("element[");
            log.append(element);
            log.append("bindingIndex[");
            log.append(bindingindex);
            log.append("] tracker[");

            try{
                FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(this.element, this.bindingindex);

                binding.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                binding.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class AttribIPointerLink extends FSConfigLocated{

        public int linkindex;
        public int unitsize;

        public AttribIPointerLink(Mode mode, int linkindex, int unitsize){
            super(mode);

            this.linkindex = linkindex;
            this.unitsize = unitsize;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSLinkGLBuffered<?, ?> link = (FSLinkGLBuffered<?, ?>)mesh.links.get(linkindex);
            VLBufferTracker tracker = link.tracker;
            int bytesize = tracker.typebytesize;

            link.vbuffer.bind();

            GLES32.glVertexAttribIPointer(location, unitsize, link.gldatatype, tracker.stride * bytesize, tracker.offset * bytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("linkIndex[");
            log.append(linkindex);
            log.append("] tracker[");

            try{
                FSLinkGLBuffered<?, ?> link = (FSLinkGLBuffered<?, ?>)mesh.links.get(linkindex);

                link.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                link.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class UniformMatrix4fvd extends ArrayDirect<VLArrayFloat>{

        public UniformMatrix4fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class UniformMatrix4fve extends ArrayElement<VLArrayFloat>{

        public UniformMatrix4fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class Uniform4fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform4fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4fve extends ArrayElement<VLArrayFloat>{

        public Uniform4fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);

            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform3fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fve extends ArrayElement<VLArrayFloat>{

        public Uniform3fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform2fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fve extends ArrayElement<VLArrayFloat>{

        public Uniform2fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform1fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fve extends ArrayElement<VLArrayFloat>{

        public Uniform1fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform1fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4f extends FSConfigLocated{

        public VLFloat x;
        public VLFloat y;
        public VLFloat z;
        public VLFloat w;

        public Uniform4f(Mode mode, VLFloat x, VLFloat y, VLFloat z, VLFloat w){
            super(mode);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4f(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("], z[");
            log.append(z.get());
            log.append("], w[");
            log.append(w.get());
            log.append("] ");
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

        public Uniform3f(Mode mode, VLFloat x, VLFloat y, VLFloat z){
            super(mode);

            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3f(location, x.get(), y.get(), z.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("], z[");
            log.append(z.get());
            log.append("] ");
        }
    }

    public static class Uniform2f extends FSConfigLocated{

        public VLFloat x;
        public VLFloat y;

        public Uniform2f(Mode mode, VLFloat x, VLFloat y){
            super(mode);

            this.x = x;
            this.y = y;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2f(location, x.get(), y.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("] ");
        }
    }

    public static class Uniform1f extends FSConfigLocated{

        public VLFloat x;

        public Uniform1f(Mode mode, VLFloat x){
            super(mode);
            this.x = x;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1f(location, x.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("] ");
        }
    }

    public static class Uniform4ivd extends ArrayDirect<VLArrayInt>{

        public Uniform4ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4ive extends ArrayElement<VLArrayInt>{

        public Uniform4ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ivd extends ArrayDirect<VLArrayInt>{

        public Uniform3ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ive extends ArrayElement<VLArrayInt>{

        public Uniform3ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ivd extends ArrayDirect<VLArrayInt>{

        public Uniform2ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ive extends ArrayElement<VLArrayInt>{

        public Uniform2ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ivd extends ArrayDirect<VLArrayInt>{

        public Uniform1ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ive extends ArrayElement<VLArrayInt>{

        public Uniform1ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glUniform1iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;
        public VLInt w;

        public Uniform4i(Mode mode, VLInt x, VLInt y, VLInt z, VLInt w){
            super(mode);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4i(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("], z[");
            log.append(z.get());
            log.append("], w[");
            log.append(w.get());
            log.append("] ");
        }
    }

    public static class Uniform3i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;

        public Uniform3i(Mode mode, VLInt x, VLInt y, VLInt z){
            super(mode);

            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3i(location, x.get(), y.get(), z.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("], z[");
            log.append(z.get());
            log.append("] ");
        }
    }

    public static class Uniform2i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;

        public Uniform2i(Mode mode, VLInt x, VLInt y){
            super(mode);

            this.x = x;
            this.y = y;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2i(location, x.get(), y.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("] ");
        }
    }

    public static class Uniform1i extends FSConfigLocated{

        public VLInt x;

        public Uniform1i(Mode mode, VLInt x){
            super(mode);
            this.x = x;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1i(location, x.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("] ");
        }
    }

    public static class Attrib4fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib4fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib4fve extends ArrayElement<VLArrayFloat>{

        public Attrib4fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib3fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 3);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fve extends ArrayElement<VLArrayFloat>{

        public Attrib3fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 3);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib2fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 2);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fve extends ArrayElement<VLArrayFloat>{

        public Attrib2fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 2);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib1fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 1);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib1fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fve extends ArrayElement<VLArrayFloat>{

        public Attrib1fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 1);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib1fv(location, array().provider(), offset());
        }
    }

    public static class AttribI4i extends FSConfigLocated{

        public VLInt x;
        public VLInt y;
        public VLInt z;
        public VLInt w;

        public AttribI4i(Mode mode, VLInt x, VLInt y, VLInt z, VLInt w){
            super(mode);

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4i(location, x.get(), y.get(), z.get(), w.get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("x[");
            log.append(x.get());
            log.append("], y[");
            log.append(y.get());
            log.append("], z[");
            log.append(z.get());
            log.append("], w[");
            log.append(w.get());
            log.append("] ");
        }
    }

    public static class AttribI4ivd extends ArrayDirect<VLArrayInt>{

        public AttribI4ivd(Mode mode, VLArrayInt array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4ive extends ArrayElement<VLArrayInt>{

        public AttribI4ive(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uivd extends ArrayDirect<VLArrayInt>{

        public AttribI4uivd(Mode mode, VLArrayInt array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uive extends ArrayElement<VLArrayInt>{

        public AttribI4uive(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(pass, program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class UniformBlockElement extends FSConfigLocated{

        public int element;
        public int bindingindex;

        protected String name;

        public UniformBlockElement(Mode mode, int element, String name, int bindingindex){
            super(mode);

            this.element = element;
            this.bindingindex = bindingindex;
            this.name = name;
        }

        @Override
        protected void notifyProgramBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSVertexBuffer<?> buffer = mesh.first().bufferBindings().get(element, bindingindex).vbuffer;

            program.uniformBlockBinding(location, buffer.bindPoint());
            buffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("element[");
            log.append(FSHub.ELEMENT_NAMES[element]);
            log.append("] bindingIndex[");
            log.append(bindingindex);
            log.append("] tracker[");

            try{
                FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(this.element, this.bindingindex);

                binding.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                binding.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class UniformBlockData extends FSConfigLocated{

        public FSVertexBuffer<?> vbuffer;
        protected String name;

        public UniformBlockData(Mode mode, FSVertexBuffer<?> vbuffer, String name){
            super(mode);

            this.vbuffer = vbuffer;
            this.name = name;
        }

        @Override
        protected void notifyProgramBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            program.uniformBlockBinding(location, vbuffer.bindPoint());
            vbuffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("element[NONE] ");

            vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);

            log.append("] ");
        }
    }

    public static class TextureBind extends FSConfig{

        public FSTexture texture;

        public TextureBind(Mode mode, FSTexture texture){
            super(mode);
            this.texture = texture;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            texture.activateUnit();
            texture.bind();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);
            log.append("[DYNAMIC]");
        }
    }

    public static class TextureColorBind extends FSConfig{

        public TextureColorBind(Mode mode){
            super(mode);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSTexture t = mesh.first().colortexture;
            t.activateUnit();
            t.bind();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);
            log.append("[DYNAMIC]");
        }
    }

    public static class TextureColorUnit extends FSConfigLocated{

        public TextureColorUnit(Mode mode){
            super(mode);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1i(location, mesh.first().colortexture.unit().get());
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);
            log.append("[DYNAMIC]");
        }
    }

    public static class UniformBlockLink extends FSConfigLocated{

        public int linkindex;
        protected String name;

        public UniformBlockLink(Mode mode, String name, int linkindex){
            super(mode);

            this.name = name;
            this.linkindex = linkindex;
            this.name = name;
        }

        @Override
        protected void notifyProgramBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSVertexBuffer<?> buffer = ((FSLinkGLBuffered<?, ?>)mesh.getLink(linkindex)).vbuffer;

            program.uniformBlockBinding(location, buffer.bindPoint());
            buffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("linkIndex[");
            log.append(linkindex);
            log.append("] link[");
            log.append(mesh.getLink(linkindex));
            log.append("] buffer[");

            try{
                FSLinkGLBuffered<?, ?> link = (FSLinkGLBuffered<?, ?>)mesh.links.get(linkindex);

                link.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                link.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class DrawArrays extends FSConfig{

        public DrawArrays(Mode mode){
            super(mode);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDrawArrays(mesh.drawmode, 0, mesh.first().vertexSize());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("drawMode[");
            log.append(mesh.drawmode);
            log.append("] indexCount[");
            log.append(mesh.first().vertexSize());
            log.append("] ");
        }
    }

    public static class DrawArraysInstanced extends FSConfig{

        public DrawArraysInstanced(Mode mode){
            super(mode);
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDrawArraysInstanced(mesh.drawmode, 0, mesh.first().vertexSize(), mesh.size());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("drawMode[");
            log.append(mesh.drawmode);
            log.append("] indexCount[");
            log.append(mesh.first().vertexSize());
            log.append("] instanceCount[");
            log.append(mesh.size());
            log.append("] ");
        }
    }

    public static class DrawElements extends FSConfig{

        public int bindingindex;

        public DrawElements(Mode mode, int bindingindex){
            super(mode);
            this.bindingindex = bindingindex;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bindingindex);
            VLBufferTracker tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawElements(mesh.drawmode, tracker.count, FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("element[");
            log.append(FSHub.ELEMENT_NAMES[FSHub.ELEMENT_INDEX]);
            log.append("] bindingIndex[");
            log.append(bindingindex);
            log.append("] buffer[");

            mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bindingindex).vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);

            log.append("] ");
        }
    }

    public static class DrawElementsInstanced extends FSConfig{

        public int bindingindex;

        public DrawElementsInstanced(Mode mode, int bindingindex){
            super(mode);
            this.bindingindex = bindingindex;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bindingindex);
            VLBufferTracker tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawElementsInstanced(mesh.drawmode, tracker.count, FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX], mesh.size());
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("drawMode[");
            log.append(mesh.drawmode);
            log.append("] instanceCount[");
            log.append(mesh.size());
            log.append("] bindingIndex[");
            log.append(bindingindex);
            log.append("] tracker[");

            try{
                FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, this.bindingindex);

                binding.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                binding.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
        }
    }

    public static class DrawRangeElements extends FSConfig{

        public int start;
        public int end;
        public int count;
        public int bindingindex;

        public DrawRangeElements(Mode mode, int start, int end, int count, int bindingindex){
            super(mode);

            this.start = start;
            this.end = end;
            this.count = count;
            this.bindingindex = bindingindex;
        }

        @Override
        public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bindingindex);
            VLBufferTracker tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawRangeElements(mesh.drawmode, start, end, count, FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
            super.debugInfo(pass, program, mesh, log, debug);

            log.append("drawMode[");
            log.append(mesh.drawmode);
            log.append("] start[");
            log.append(start);
            log.append("] end[");
            log.append(end);
            log.append("] count[");
            log.append(count);
            log.append("] bindingIndex[");
            log.append(bindingindex);
            log.append("] tracker[");

            try{
                FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, this.bindingindex);

                binding.tracker.stringify(log.get(), null);
                log.append("] buffer[");

                binding.vbuffer.stringify(log.get(), BUFFER_PRINT_LIMIT);
                log.append("] ");

            }catch(Exception ex){
                log.append("PRINT FAILED[");
                log.append(ex.getMessage());
                log.append("]");
            }
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
            src.append("[countRow : ");
            src.append(length[0]);
            src.append(" size : ");
            src.append(size[0]);
            src.append(" type : ");
            src.append(type[0]);
            src.append(" name : ");
            src.append(new String(VLArrayUtils.slice(name, 0, length[0]), StandardCharsets.UTF_8));
            src.append(" ]");

        }
    }
}
