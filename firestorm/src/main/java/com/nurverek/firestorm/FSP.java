package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import vanguard.VLArray;
import vanguard.VLArrayFloat;
import vanguard.VLArrayInt;
import vanguard.VLArrayUtils;
import vanguard.VLBufferTrackerDetailed;
import vanguard.VLDebug;
import vanguard.VLFloat;
import vanguard.VLInt;
import vanguard.VLListType;
import vanguard.VLStringify;

public abstract class FSP{

    private static final int BUFFER_PRINT_LIMIT = 50;

    protected VLListType<FSShader> shaders;
    protected VLListType<FSMesh> meshes;

    protected CoreConfig coreconfigs;

    protected int program;
    protected int debug;
    protected int uniformlocation;

    public FSP(int shadercapacity, int meshcapacity, int debugmode){
        program = -1;
        debug = debugmode;

        shaders = new VLListType<>(shadercapacity, shadercapacity);
        meshes = new VLListType<>(meshcapacity, meshcapacity);

        uniformlocation = 0;
    }

    protected abstract CoreConfig customize(VLListType<FSMesh> meshes, int debug);

    public VLListType<FSMesh> meshes(){
        return meshes;
    }

    public CoreConfig coreConfigs(){
        return coreconfigs;
    }

    public int id(){
        return program;
    }

    public FSP build(){
        coreconfigs = customize(meshes, debug);

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

            if(debug > FSControl.DEBUG_DISABLED){
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

        if(debug > FSControl.DEBUG_DISABLED){
            try{
                if(coreconfigs.setupconfig != null){
                    VLDebug.append("[Notifying program built for SetupConfig]\n");
                    coreconfigs.setupconfig.notifyProgramBuilt(this);
                }
                if(coreconfigs.meshconfig != null){
                    VLDebug.append("[Notifying program built for MeshConfig]\n");
                    coreconfigs.meshconfig.notifyProgramBuilt(this);
                }
                if(coreconfigs.postdrawconfig != null){
                    VLDebug.append("[Notifying program built for PostDrawConfig]\n");
                    coreconfigs.postdrawconfig.notifyProgramBuilt(this);
                }

            }catch(Exception ex){
                VLDebug.append("Failed.\n");
                VLDebug.printE();

                throw new RuntimeException("Error during program configuration setup", ex);
            }

            VLDebug.printD();

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

    public void draw(int passindex){
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
            VLDebug.recreate();

            VLDebug.append("------- PROGRAM[");
            VLDebug.append(program);
            VLDebug.append("] -------");
            VLDebug.printD();

            if(coreconfigs.setupconfig != null){
                VLDebug.append("[SetupConfig]");
                VLDebug.printD();

                coreconfigs.setupconfig.runDebug(this, null, -1, passindex);

                VLDebug.printD();
            }

            if(coreconfigs.meshconfig != null){
                FSMesh mesh;

                for(int i = 0; i < meshsize; i++){
                    mesh = meshes.get(i);

                    VLDebug.append("[MeshConfig] [");
                    VLDebug.append(i + 1);
                    VLDebug.append("/");
                    VLDebug.append(meshsize);
                    VLDebug.append("] [");
                    VLDebug.append(mesh.name());
                    VLDebug.append("]");
                    VLDebug.printD();

                    coreconfigs.meshconfig.runDebug(this, meshes.get(i), i, passindex);

                    VLDebug.printD();
                }

                VLDebug.append("[PostDrawConfig]");
                VLDebug.printD();
            }

            if(coreconfigs.postdrawconfig != null){
                coreconfigs.postdrawconfig.runDebug(this, null, -1, passindex);

                VLDebug.printD();
            }

        }else{
            if(coreconfigs.setupconfig != null){
                coreconfigs.setupconfig.run(this, null, -1, passindex);
            }

            if(coreconfigs.meshconfig != null){
                for(int i = 0; i < meshsize; i++){
                    coreconfigs.meshconfig.run(this, meshes.get(i), i, passindex);
                }
            }

            if(coreconfigs.postdrawconfig != null){
                coreconfigs.postdrawconfig.run(this, null, -1, passindex);
            }
        }
    }

    public void postFrame(int passindex){
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
                VLDebug.recreate();

                VLDebug.append("------- PROGRAM[");
                VLDebug.append(program);
                VLDebug.append("] -------");
                VLDebug.printD();

                VLDebug.append("[PostFrameConfig]");
                VLDebug.printD();

                coreconfigs.postframeconfig.runDebug(this, null, -1, passindex);

                VLDebug.printD();

            }else{
                coreconfigs.postframeconfig.run(this, null, -1, passindex);
            }
        }
    }

    public void use(){
        GLES32.glUseProgram(program);

        if(debug >= FSControl.DEBUG_NORMAL){
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
        
        public FSConfig setupconfig;
        public FSConfig meshconfig;
        public FSConfig postdrawconfig;
        public FSConfig postframeconfig;

        public CoreConfig(FSConfig setupconfig, FSConfig meshconfig, FSConfig postdrawconfig, FSConfig postframeconfig){
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
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glClear(flag);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append(" flag[");
            VLDebug.append(flag);
            VLDebug.append("]");
        }
    }

    public static class ClearColor extends FSConfig{

        public float[] color;

        public ClearColor(Mode mode, float[] color){
            super(mode);
            this.color = color;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glClearColor(color[0], color[1], color[2], color[3]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append(" color[");
            VLDebug.append(Arrays.toString(color));
            VLDebug.append("]");
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
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            config.viewPort(x, y, width, height);
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

        public DepthMask(Mode mode, boolean mask){
            super(mode);
            this.mask = mask;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glDepthMask(mask);
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

        public int cullmode;

        public CullFace(Mode mode, int cullmode){
            super(mode);
            this.cullmode = cullmode;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glCullFace(cullmode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("mode[");
            VLDebug.append(cullmode);
            VLDebug.append("] ");
        }
    }

    public static class AttribDivisor extends FSConfigLocated{

        public int divisor;

        public AttribDivisor(Mode mode, int divisor){
            super(mode);
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

        public int readmode;

        public ReadBuffer(Mode mode, int readmode){
            super(mode);
            this.readmode = readmode;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glReadBuffer(readmode);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("mode[");
            VLDebug.append(readmode);
            VLDebug.append("] ");
        }
    }

    public static class AttribEnable extends FSConfigLocated{

        public AttribEnable(Mode mode, int location){
            super(mode, location);
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

        public AttribDisable(Mode mode, int location){
            super(mode, location);
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
        public abstract void debugInfo(FSP program, FSMesh mesh, int debug);

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
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            VLDebug.append("offset[");
            VLDebug.append(offset);
            VLDebug.append("] count[");
            VLDebug.append(count);
            VLDebug.append("] array[");

            array.stringify(VLDebug.get(), null);

            VLDebug.append("]");
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
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            array = (TYPE)mesh.instance(instance).element(element);
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            VLDebug.append("] instance[");
            VLDebug.append(instance);
            VLDebug.append("] element[");
            VLDebug.append(FSHub.ELEMENT_NAMES[element]);
            VLDebug.append("] array[");

            if(array == null){
                mesh.instance(instance).element(element).stringify(VLDebug.get(), null);

            }else{
                array.stringify(VLDebug.get(), null);
            }

            VLDebug.append("]");
        }
    }

    public static class AttribPointer extends FSConfigLocated{

        public int element;
        public int bufferindex;

        public AttribPointer(Mode mode, int element, int bufferindex){
            super(mode);

            this.element = element;
            this.bufferindex = bufferindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(element, bufferindex);
            VLBufferTrackerDetailed tracker = binding.tracker;

            int databytesize = FSHub.ELEMENT_BYTES[element];

            binding.vbuffer.bind();
            GLES32.glVertexAttribPointer(location, tracker.unitsize(), FSHub.ELEMENT_GLDATA_TYPES[element], false, tracker.stride() * databytesize, tracker.offset() * databytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSHub.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(element, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class AttribIPointer extends FSConfigLocated{

        public int element;
        public int bufferindex;

        public AttribIPointer(Mode mode, int element, int bufferindex){
            super(mode);

            this.element = element;
            this.bufferindex = bufferindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(element, bufferindex);
            VLBufferTrackerDetailed tracker = binding.tracker;

            int databytesize = FSHub.ELEMENT_BYTES[element];

            binding.vbuffer.bind();
            GLES32.glVertexAttribIPointer(location, tracker.unitsize(), FSHub.ELEMENT_GLDATA_TYPES[element], tracker.stride() * databytesize, tracker.offset() * databytesize);
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSHub.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(element, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class UniformMatrix4fvd extends ArrayDirect<VLArrayFloat>{

        public UniformMatrix4fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class UniformMatrix4fve extends ArrayElement<VLArrayFloat>{

        public UniformMatrix4fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniformMatrix4fv(location, count(), false, array().provider(), offset());
        }
    }

    public static class Uniform4fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform4fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4fve extends ArrayElement<VLArrayFloat>{

        public Uniform4fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);

            GLES32.glUniform4fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform3fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3fve extends ArrayElement<VLArrayFloat>{

        public Uniform3fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform3fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform2fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2fve extends ArrayElement<VLArrayFloat>{

        public Uniform2fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform2fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fvd extends ArrayDirect<VLArrayFloat>{

        public Uniform1fvd(Mode mode, VLArrayFloat array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1fv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1fve extends ArrayElement<VLArrayFloat>{

        public Uniform1fve(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
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

        public Uniform4f(Mode mode, VLFloat x, VLFloat y, VLFloat z, VLFloat w){
            super(mode);

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

        public Uniform3f(Mode mode, VLFloat x, VLFloat y, VLFloat z){
            super(mode);

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

        public Uniform2f(Mode mode, VLFloat x, VLFloat y){
            super(mode);

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

        public Uniform1f(Mode mode, VLFloat x){
            super(mode);
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

    public static class Uniform4ivd extends ArrayDirect<VLArrayInt>{

        public Uniform4ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform4ive extends ArrayElement<VLArrayInt>{

        public Uniform4ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ivd extends ArrayDirect<VLArrayInt>{

        public Uniform3ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform3ive extends ArrayElement<VLArrayInt>{

        public Uniform3ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ivd extends ArrayDirect<VLArrayInt>{

        public Uniform2ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform2ive extends ArrayElement<VLArrayInt>{

        public Uniform2ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glUniform4iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ivd extends ArrayDirect<VLArrayInt>{

        public Uniform1ivd(Mode mode, VLArrayInt array, int offset, int count){
            super(mode, array, offset, count);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glUniform1iv(location, count(), array().provider(), offset());
        }
    }

    public static class Uniform1ive extends ArrayElement<VLArrayInt>{

        public Uniform1ive(Mode mode, int instance, int element, int offset, int count){
            super(mode, element, instance, offset, count);
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

        public Uniform4i(Mode mode, VLInt x, VLInt y, VLInt z, VLInt w){
            super(mode);

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

        public Uniform3i(Mode mode, VLInt x, VLInt y, VLInt z){
            super(mode);

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

        public Uniform2i(Mode mode, VLInt x, VLInt y){
            super(mode);

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

        public Uniform1i(Mode mode, VLInt x){
            super(mode);
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

    public static class Attrib4fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib4fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib4fve extends ArrayElement<VLArrayFloat>{

        public Attrib4fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib4fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib3fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 3);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib3fve extends ArrayElement<VLArrayFloat>{

        public Attrib3fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 3);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib3fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib2fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 2);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib2fve extends ArrayElement<VLArrayFloat>{

        public Attrib2fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 2);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttrib2fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fvd extends ArrayDirect<VLArrayFloat>{

        public Attrib1fvd(Mode mode, VLArrayFloat array, int offset){
            super(mode, array, offset, 1);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttrib1fv(location, array().provider(), offset());
        }
    }

    public static class Attrib1fve extends ArrayElement<VLArrayFloat>{

        public Attrib1fve(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 1);
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

        public AttribI4i(Mode mode, VLInt x, VLInt y, VLInt z, VLInt w){
            super(mode);

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

    public static class AttribI4ivd extends ArrayDirect<VLArrayInt>{

        public AttribI4ivd(Mode mode, VLArrayInt array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4ive extends ArrayElement<VLArrayInt>{

        public AttribI4ive(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4iv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uivd extends ArrayDirect<VLArrayInt>{

        public AttribI4uivd(Mode mode, VLArrayInt array, int offset){
            super(mode, array, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class AttribI4uive extends ArrayElement<VLArrayInt>{

        public AttribI4uive(Mode mode, int instance, int element, int offset){
            super(mode, element, instance, offset, 4);
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            super.configure(program, mesh, meshindex, passindex);
            GLES32.glVertexAttribI4uiv(location, array().provider(), offset());
        }
    }

    public static class UniformBlockElement extends FSConfigLocated{

        public int element;
        public int bufferindex;

        protected String name;

        public UniformBlockElement(Mode mode, int element, String name, int bufferindex){
            super(mode);

            this.element = element;
            this.bufferindex = bufferindex;
            this.name = name;
        }

        @Override
        protected void notifyProgramBuilt(FSP program){
            location = program.getUniformBlockIndex(name);
            name = null;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSVertexBuffer<?> buffer = mesh.first().bufferBindings().get(element, bufferindex).vbuffer;

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
            VLDebug.append(FSHub.ELEMENT_NAMES[element]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(element, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
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
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            program.uniformBlockBinding(location, vbuffer.bindPoint());
            vbuffer.bindBufferBase();
        }

        @Override
        public int getGLSLSize(){
            return 1;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[NONE] ");

            vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class TextureBind extends FSConfig{

        public FSTexture texture;

        public TextureBind(Mode mode, FSTexture texture){
            super(mode);
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

        public TextureColorBind(Mode mode){
            super(mode);
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

        public TextureColorUnit(Mode mode){
            super(mode);
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

        public DrawArrays(Mode mode){
            super(mode);
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

        public DrawArraysInstanced(Mode mode){
            super(mode);
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

        public int bufferindex;

        public DrawElements(Mode mode, int bufferindex){
            super(mode);
            this.bufferindex = bufferindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex);
            VLBufferTrackerDetailed tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawElements(mesh.drawmode, tracker.count(), FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset() * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX]);
        }

        @Override
        public int getGLSLSize(){
            return 0;
        }

        @Override
        public void debugInfo(FSP program, FSMesh mesh, int debug){
            super.debugInfo(program, mesh, debug);

            VLDebug.append("element[");
            VLDebug.append(FSHub.ELEMENT_NAMES[FSHub.ELEMENT_INDEX]);
            VLDebug.append("] bufferIndex[");
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class DrawElementsInstanced extends FSConfig{

        public int bufferindex;

        public DrawElementsInstanced(Mode mode, int bufferindex){
            super(mode);
            this.bufferindex = bufferindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex);
            VLBufferTrackerDetailed tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawElementsInstanced(mesh.drawmode, tracker.count(), FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset() * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX], mesh.size());
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
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

            VLDebug.append("] ");
        }
    }

    public static class DrawRangeElements extends FSConfig{

        public int start;
        public int end;
        public int count;
        public int bufferindex;

        public DrawRangeElements(Mode mode, int start, int end, int count, int bufferindex){
            super(mode);

            this.start = start;
            this.end = end;
            this.count = count;
            this.bufferindex = bufferindex;
        }

        @Override
        public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
            FSBufferBindings.Binding<?> binding = mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex);
            VLBufferTrackerDetailed tracker = binding.tracker;

            binding.vbuffer.bind();
            GLES32.glDrawRangeElements(mesh.drawmode, start, end, count, FSHub.ELEMENT_GLDATA_TYPES[FSHub.ELEMENT_INDEX], tracker.offset() * FSHub.ELEMENT_BYTES[FSHub.ELEMENT_INDEX]);
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
            VLDebug.append(bufferindex);
            VLDebug.append("] bufferAddress[");

            mesh.first().bufferBindings().get(FSHub.ELEMENT_INDEX, bufferindex).vbuffer.stringify(VLDebug.get(), BUFFER_PRINT_LIMIT);

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
