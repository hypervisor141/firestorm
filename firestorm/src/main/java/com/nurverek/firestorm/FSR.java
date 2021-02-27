package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLThreadHost;
import com.nurverek.vanguard.VLVManager;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class FSR{

    public final static Object RENDERLOCK = new Object();

    private static ArrayList<FSRPass> passes;
    private static ArrayList<VLThreadHost> threadhosts;
    private static ArrayList<Runnable> tasks;

    private static FSRThread renderthread;

    protected static boolean isInitialized;

    protected static int CURRENT_RENDER_PASS_INDEX;
    protected static int CURRENT_FSG_INDEX;
    protected static int CURRENT_PROGRAM_SET_INDEX;
    private static int EXTERNAL_CHANGES;
    private static int INTERNAL_CHANGES;

    protected static VLVManager CONTROLMANAGER = new VLVManager(1, 10);

    protected static void initialize(FSRThread thread){
        renderthread = thread;

        passes = new ArrayList<>();
        threadhosts = new ArrayList<>();
        tasks = new ArrayList<>();

        isInitialized = true;

        EXTERNAL_CHANGES = 0;
        INTERNAL_CHANGES = 0;
        CURRENT_RENDER_PASS_INDEX = 0;
        CURRENT_FSG_INDEX = 0;
        CURRENT_PROGRAM_SET_INDEX = 0;
    }

    protected static void initialize(){
        initialize(new FSRThread());
    }

    protected static void startRenderThread(){
        renderthread.setPriority(Thread.MAX_PRIORITY);
        renderthread.initialize();
    }

    protected static void onSurfaceCreated(boolean continuing){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreCreated(continuing);
        events.GLPostCreated(continuing);
    }

    protected static void onSurfaceChanged(int width, int height){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreChange(width, height);
        events.GLPostChange(width, height);
    }

    protected static void onDrawFrame(){
        synchronized(RENDERLOCK){
            int size = passes.size();

            for(int i = 0; i < size; i++){
                CURRENT_RENDER_PASS_INDEX = i;
                CURRENT_PROGRAM_SET_INDEX = -1;
                CURRENT_FSG_INDEX = -1;

                passes.get(i).execute();
            }

            finishFrame();
        }
    }

    protected static void runTasks(){
        try{
            synchronized(tasks){
                int size = tasks.size();

                for(int i = 0; i < size; i++){
                    tasks.get(i).run();
                }

                tasks.clear();
            }

        }catch(NoSuchElementException ex){
            ex.printStackTrace();
        }
    }

    protected static void advanceRunners(){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreAdvancement();

        int changes = CONTROLMANAGER.next() + EXTERNAL_CHANGES + INTERNAL_CHANGES;
        int size = passes.size();

        for(int i = 0; i < size; i++){
            changes += passes.get(i).advanceRunners();
        }

        EXTERNAL_CHANGES = 0;
        INTERNAL_CHANGES = 0;

        FSRControl.processFrameAndSignalNextFrame(changes);

        events.GLPostAdvancement(changes);
    }

    public static void addExternalChangesForFrame(int changes){
        EXTERNAL_CHANGES += changes;
    }

    protected static void addInternalChangesForFrame(int changes){
        INTERNAL_CHANGES += changes;
    }

    public static void addRenderPass(FSRPass pass){
        passes.add(pass);
    }

    public static void addThreadHost(VLThreadHost p){
        threadhosts.add(p);
    }

    public static void addTask(Runnable task){
        synchronized(tasks){
            tasks.add(task);
            FSRControl.signalFrameRender(true);
        }
    }

    public static void addTaskPriority(Runnable task){
        synchronized (tasks){
            tasks.add(0, task);
        }
    }

    protected static FSRThread getRenderThread(){
        return renderthread;
    }

    public static int getCurrentRenderPassIndex(){
        return CURRENT_RENDER_PASS_INDEX;
    }

    public static FSRPass getRenderPass(int index){
        return passes.get(index);
    }

    public static VLThreadHost getThreadHost(int index){
        return threadhosts.get(index);
    }

    public static void removeRenderPass(FSRPass pass){
        int size = passes.size();

        for(int i = 0; i < size; i++){
            if(passes.get(i).id() == pass.id()){
                passes.remove(i);
            }
        }
    }

    public static FSRPass removeRenderPass(int index){
        return passes.remove(index);
    }

    public static void removeThreadHost(int index, boolean destroy){
        VLThreadHost p = threadhosts.remove(index);

        if(destroy){
            p.destroy();
        }
    }

    public static void removeThreadHost(VLThreadHost p, boolean destroy){
        threadhosts.remove(p);

        if(destroy){
            p.destroy();
        }
    }

    public static int getRenderPassesSize(){
        return passes.size();
    }

    public static int getThreadHostSize(){
        return threadhosts.size();
    }

    public static VLVManager getControlManager(){
        return CONTROLMANAGER;
    }

    protected static void finishFrame(){
        FSRControl.timeFrameEnded();
        FSEGL.swapBuffers();

        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }

        FSRControl.timeBufferSwapped();
    }

    protected static boolean needsSwap(){
        if(CURRENT_RENDER_PASS_INDEX >= passes.size() - 1){
            return true;
        }

        return false;
    }

    public static void clear(int flag){
        GLES32.glClear(flag);
    }

    public static void clearColor(){
        GLES32.glClearColor(FSControl.clearcolor[0], FSControl.clearcolor[1], FSControl.clearcolor[2], FSControl.clearcolor[3]);
    }

    public static void polygonOffset(float factor, float units){
        GLES32.glPolygonOffset(factor, units);
    }

    public static void drawBuffers(int count, int[] ids, int offset){
        GLES32.glDrawBuffers(count, ids, offset);
    }

    public static void drawBuffers(int count, IntBuffer buffer){
        GLES32.glDrawBuffers(count, buffer);
    }

    public static void enable(int target){
        GLES32.glEnable(target);
    }

    public static void disable(int target){
        GLES32.glDisable(target);
    }

    public static void cullFace(int mode){
        GLES32.glCullFace(mode);
    }

    public static void frontFace(int mode){
        GLES32.glFrontFace(mode);
    }

    public static void blendFunc(int sfactor, int dfactor){
        GLES32.glBlendFunc(sfactor, dfactor);
    }

    public static void depthFunc(int func){
        GLES32.glDepthFunc(func);
    }

    public static void depthMask(boolean mask){
        GLES32.glDepthMask(mask);
    }

    public static void stencilOp(int fail, int zfail, int zpass){
        GLES32.glStencilOp(fail, zfail, zpass);
    }

    public static void stencilFunc(int func, int ref, int mask){
        GLES32.glStencilFunc(func, ref, mask);
    }

    public static void stencilMask(int mask){
        GLES32.glStencilMask(mask);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha){
        GLES32.glColorMask(red, green, blue, alpha);
    }

    public static void readBuffer(int mode){
        GLES32.glReadBuffer(mode);
    }

    public static int[] createFrameBuffer(int count){
        int[] fb = new int[count];
        GLES32.glGenFramebuffers(count, fb, 0);

        return fb;
    }

    public static int createShader(int type, String code){
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, code);
        GLES32.glCompileShader(shader);

        return shader;
    }

    public static int[] createBuffers(int count){
        int[] buffers = new int[count];
        GLES32.glGenBuffers(count, buffers, 0);

        return buffers;
    }

    public static int[] createVertexArrays(int count){
        int[] buffers = new int[count];
        GLES32.glGenVertexArrays(count, buffers, 0);

        return buffers;
    }

    public static int[] createTexture(int count){
        int[] t = new int[count];
        GLES32.glGenTextures(1, t, 0);

        return t;
    }

    public static void deleteFrameBuffer(int[] id){
        GLES32.glDeleteFramebuffers(id.length, id, 0);
    }

    public static void deleteVertexBuffers(int[] id){
        GLES32.glDeleteBuffers(id.length, id, 0);
    }

    public static void deleteVertexArrays(int[] id){
        GLES32.glDeleteVertexArrays(id.length, id, 0);
    }

    public static void deleteTextures(int[] id){
        GLES32.glDeleteTextures(id.length, id, 0);
    }

    public static int checkFramebufferStatus(int id){
        return GLES32.glCheckFramebufferStatus(id);
    }

    public static void frameBufferBind(int target, int id){
        GLES32.glBindFramebuffer(target, id);
    }

    public static void frameBufferTexture2D(int target, int attachment, int textarget, int texture, int level){
        GLES32.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void frameBufferTexture(int target, int attachment, int texture, int level){
        GLES32.glFramebufferTexture(target, attachment, texture, level);
    }

    public static void frameBufferTextureLayer(int target, int attachment, int texture, int level, int layer){
        GLES32.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    public static void framebufferRenderBuffer(int target, int attachment, int renderbuffertarget, int renderbuffer){
        GLES32.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public static void vertexBufferBind(int target, int id){
        GLES32.glBindBuffer(target, id);
    }

    public static void vertexBufferBindBase(int target, int uboindex, int bufferid){
        GLES32.glBindBufferBase(target, uboindex, bufferid);
    }

    public static void vertexBufferBindRange(int target, int uboindex, int bufferid, int offset, int size){
        GLES32.glBindBufferRange(target, uboindex, bufferid, offset, size);
    }

    public static void vertexBufferData(int target, int size, Buffer buffer, int usage){
        GLES32.glBufferData(target, size, buffer, usage);
    }

    public static void vertexBufferSubData(int target, int offset, int size, Buffer buffer){
        GLES32.glBufferSubData(target, offset, size, buffer);
    }

    public static void vertexBufferUniformBlockBinding(int program, int uboid, int bindpoint){
        GLES32.glUniformBlockBinding(program, uboid, bindpoint);
    }

    public static ByteBuffer mapBufferRange(int target, int offset, int length, int access){
        return ((ByteBuffer) GLES32.glMapBufferRange(target, offset, length, access)).order(ByteOrder.nativeOrder());
    }

    public static void flushMapBuffer(int target, int offset, int length){
        GLES32.glFlushMappedBufferRange(target, offset, length);
    }

    public static boolean unMapBuffer(int target){
        return GLES32.glUnmapBuffer(target);
    }

    public static void vertexArrayBind(int id){
        GLES32.glBindVertexArray(id);
    }

    public static void texStorage2D(int target, int levels, int internalformat, int width, int height){
        GLES32.glTexStorage2D(target, levels, internalformat, width, height);
    }

    public static void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){
        GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void texSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){
        GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void texStorage3D(int target, int levels, int internalFormat, int width, int height, int depth){
        GLES32.glTexStorage3D(target, levels, internalFormat, width, height, depth);
    }

    public static void texImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels){
        GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public static void texImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset){
        GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
    }

    public static void texSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels){
        GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public static void texSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset){
        GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
    }

    public static void generateMipMap(int target){
        GLES32.glGenerateMipmap(target);
    }

    public static void textureActive(int unit){
        GLES32.glActiveTexture(unit);
    }

    public static void textureBind(int target, int id){
        GLES32.glBindTexture(target, id);
    }

    public static void textureParameteri(int target, int option, int mode){
        GLES32.glTexParameteri(target, option, mode);
    }

    public static int[] genTransformFeedback(int count){
        int[] results = new int[count];
        GLES32.glGenTransformFeedbacks(count, results, 0);

        return results;
    }

    public static void transformFeedbackVaryings(FSP program, String[] varyings, int buffermode){
        GLES32.glTransformFeedbackVaryings(program.id(), varyings, buffermode);
    }

    public static void beginTransformFeedback(int primitivemode){
        GLES32.glBeginTransformFeedback(primitivemode);
    }

    public static void pauseTransformFeedback(){
        GLES32.glPauseTransformFeedback();
    }

    public static void resumeTransformFeedback(){
        GLES32.glResumeTransformFeedback();
    }

    public static void endTransformFeedback(int primitivemode){
        GLES32.glBeginTransformFeedback(primitivemode);
    }

    protected static void destroy(){
        renderthread.shutdown();

        if(!FSControl.getKeepAlive()){
            int size = passes.size();

            for(int i = 0; i < size; i++){
                passes.get(i).destroy();
            }

            isInitialized = false;

            CURRENT_RENDER_PASS_INDEX = 0;
            EXTERNAL_CHANGES = 0;
            INTERNAL_CHANGES = 0;

            passes = null;
            CONTROLMANAGER = null;
            tasks = null;
            threadhosts = null;
        }
    }
}

