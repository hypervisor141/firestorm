package com.nurverek.firestorm;

import android.opengl.GLES32;
import android.opengl.GLU;
import android.opengl.Matrix;

import vanguard.VLArrayFloat;
import vanguard.VLArrayInt;
import vanguard.VLCopyable;

public class FSView implements VLCopyable<FSView>{

    protected VLArrayFloat matview;
    protected VLArrayFloat matperspective;
    protected VLArrayFloat matorthographic;
    protected VLArrayFloat matprojection;
    protected VLArrayFloat matviewprojection;

    protected VLArrayInt settingsviewport;
    protected VLArrayFloat settingsview;
    protected VLArrayFloat settingsperspective;
    protected VLArrayFloat settingsorthographic;

    public FSView(){
        matprojection = null;

        matview = new VLArrayFloat(new float[16]);
        matperspective = new VLArrayFloat(new float[16]);
        matorthographic = new VLArrayFloat(new float[16]);
        matviewprojection = new VLArrayFloat(new float[16]);

        settingsviewport = new VLArrayInt(new int[4]);
        settingsview = new VLArrayFloat(new float[9]);
        settingsperspective = new VLArrayFloat(new float[4]);
        settingsorthographic = new VLArrayFloat(new float[6]);

        setPerspectiveMode();
    }

    public FSView(FSView src, long flags){
        copy(src, flags);
    }

    public void setPerspectiveMode(){
        matprojection = matperspective;
    }

    public void setOrthographicMode(){
        matprojection = matorthographic;
    }

    public VLArrayFloat matrixPerspective(){
        return matperspective;
    }

    public VLArrayFloat matrixOrthographic(){
        return matorthographic;
    }

    public VLArrayFloat matrixView(){
        return matview;
    }

    public VLArrayFloat matrixViewProjection(){
        return matviewprojection;
    }

    public VLArrayInt settingsViewport(){
        return settingsviewport;
    }

    public VLArrayFloat settingsView(){
        return settingsview;
    }

    public VLArrayFloat settingsPerspective(){
        return settingsperspective;
    }

    public VLArrayFloat settingsOrthographic(){
        return settingsorthographic;
    }

    public void viewPort(int x, int y, int width, int height){
        settingsViewPort(x, y, width, height);
        applyViewPort();
    }

    public void lookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ){
        settingsLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        applyLookAt();
    }

    public void perspective(float fovy, float aspect, float znear, float zfar){
        settingsPerspective(fovy, aspect, znear, zfar);
        applyPerspective();
    }

    public void orthographic(float left, float right, float bottom, float top, float znear, float zfar){
        settingsOrthographic(left, right, bottom, top, znear, zfar);
        applyOrthographic();
    }

    public void settingsViewPort(int x, int y, int width, int height){
        int[] viewport = this.settingsviewport.provider();

        viewport[0] = x;
        viewport[1] = y;
        viewport[2] = width;
        viewport[3] = height;
    }

    public void settingsLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ){
        float[] settings = settingsview.provider();

        settings[0] = eyeX;
        settings[1] = eyeY;
        settings[2] = eyeZ;
        settings[3] = centerX;
        settings[4] = centerY;
        settings[5] = centerZ;
        settings[6] = upX;
        settings[7] = upY;
        settings[8] = upZ;
    }

    public void settingsPerspective(float fovy, float aspect, float znear, float zfar){
        float[] settings = settingsperspective.provider();

        settings[0] = fovy;
        settings[1] = aspect;
        settings[2] = znear;
        settings[3] = zfar;
    }

    public void settingsOrthographic(float left, float right, float bottom, float top, float znear, float zfar){
        float[] settings = settingsorthographic.provider();

        settings[0] = left;
        settings[1] = right;
        settings[2] = bottom;
        settings[3] = top;
        settings[4] = znear;
        settings[5] = zfar;
    }

    public void applyLookAt(){
        float[] settings = settingsview.provider();
        Matrix.setLookAtM(matview.provider(), 0, settings[0], settings[1], settings[2], settings[3], settings[4], settings[5], settings[6], settings[7], settings[8]);
    }

    public void applyOrthographic(){
        float[] settings = settingsorthographic.provider();
        Matrix.orthoM(matorthographic.provider(), 0, settings[0], settings[1], settings[2], settings[3], settings[4], settings[5]);
    }

    public void applyPerspective(){
        float[] settings = settingsperspective.provider();
        Matrix.perspectiveM(matperspective.provider(), 0, settings[0], settings[1], settings[2], settings[3]);
    }

    public void applyViewPort(){
        int[] viewport = this.settingsviewport.provider();
        GLES32.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    public void applyViewProjection(){
        Matrix.multiplyMM(matviewprojection.provider(), 0, matprojection.provider(), 0, matview.provider(), 0);
    }

    public void multiplyViewPerspective(float[] results, int offset, float[] point, int offset2){
        Matrix.multiplyMV(results, offset, matviewprojection.provider(), 0, point, offset2);

        float w = results[offset + 3];
        results[offset] /= w;
        results[offset + 1] /= w;
        results[offset + 2] /= w;
    }

    public void convertToMVP(float[] results, int offset, float[] model){
        Matrix.multiplyMM(results, offset, matviewprojection.provider(), 0, model, 0);
    }

    public void unProject2DPoint(float x, float y, float[] resultsnear, int offset1, float[] resultsfar, int offset2){
        y = settingsviewport.get(3) - y;

        GLU.gluUnProject(x, y, 0F, matview.provider(), 0, matprojection.provider(), 0, settingsviewport.provider(), 0, resultsnear, offset1);
        GLU.gluUnProject(x, y, 1F, matview.provider(), 0, matprojection.provider(), 0, settingsviewport.provider(), 0, resultsfar, offset2);

        y = resultsnear[offset1 + 3];

        resultsnear[offset1] /= y;
        resultsnear[offset1 + 1] /= y;
        resultsnear[offset1 + 2] /= y;

        y = resultsfar[offset2 + 3];

        resultsfar[offset2] /= y;
        resultsfar[offset2 + 1] /= y;
        resultsfar[offset2 + 2] /= y;
    }

    @Override
    public void copy(FSView src, long flags){
        if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
            matview = src.matview;
            matperspective = src.matperspective;
            matorthographic = src.matorthographic;
            matviewprojection = src.matviewprojection;
            settingsviewport = src.settingsviewport;
            settingsview = src.settingsview;
            settingsperspective = src.settingsperspective;
            settingsorthographic = src.settingsorthographic;

        }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
            matview = src.matview.duplicate(FLAG_MAX_DEPTH);
            matperspective = src.matperspective.duplicate(FLAG_MAX_DEPTH);
            matorthographic = src.matorthographic.duplicate(FLAG_MAX_DEPTH);
            matviewprojection = src.matviewprojection.duplicate(FLAG_MAX_DEPTH);
            settingsviewport = src.settingsviewport.duplicate(FLAG_MAX_DEPTH);
            settingsview = src.settingsview.duplicate(FLAG_MAX_DEPTH);
            settingsperspective = src.settingsperspective.duplicate(FLAG_MAX_DEPTH);
            settingsorthographic = src.settingsorthographic.duplicate(FLAG_MAX_DEPTH);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }

        matprojection = src.matprojection;
    }

    @Override
    public FSView duplicate(long flags){
        return new FSView(this, flags);
    }
}