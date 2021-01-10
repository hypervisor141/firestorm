package com.nurverek.firestorm;

import android.opengl.GLES32;
import android.opengl.GLU;
import android.opengl.Matrix;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayInt;

public final class FSViewConfig{

    protected VLArrayFloat projectionmat;
    protected VLArrayFloat perspectivemat;
    protected VLArrayFloat orthographicmat;
    protected VLArrayFloat viewmat;
    protected VLArrayFloat viewprojectionmat;
    protected VLArrayFloat eyepos;
    protected VLArrayInt viewport;

    protected VLArrayFloat viewmatsettings;
    protected VLArrayFloat perspectivesettings;
    protected VLArrayFloat orthographicsettings;

    public FSViewConfig(){
        projectionmat = null;

        perspectivemat = new VLArrayFloat(new float[16]);
        orthographicmat = new VLArrayFloat(new float[16]);
        viewmat = new VLArrayFloat(new float[16]);
        viewprojectionmat = new VLArrayFloat(new float[16]);

        viewmatsettings = new VLArrayFloat(new float[9]);
        perspectivesettings = new VLArrayFloat(new float[4]);
        orthographicsettings = new VLArrayFloat(new float[6]);

        viewport = new VLArrayInt(new int[4]);
        eyepos = new VLArrayFloat(new float[4]);

        setPerspectiveMode();
    }

    public void viewPort(int x, int y, int width, int height){
        int[] viewport = this.viewport.provider();

        viewport[0] = x;
        viewport[1] = y;
        viewport[2] = width;
        viewport[3] = height;
    }

    public void eyePosition(float eyeX, float eyeY, float eyeZ){
        float[] eyepos = this.eyepos.provider();

        eyepos[0] = eyeX;
        eyepos[1] = eyeY;
        eyepos[2] = eyeZ;
        eyepos[3] = 1;

        float[] settings = viewmatsettings.provider();
        settings[0] = eyepos[0];
        settings[1] = eyepos[1];
        settings[2] = eyepos[2];
    }

    public void eyePositionResetW(){
        eyepos.set(3, 1.0f);
    }

    public void eyePositionDivideByW(){
        float[] eyepos = this.eyepos.provider();
        float w = eyepos[3];

        eyepos[0] /= w;
        eyepos[1] /= w;
        eyepos[2] /= w;

        float[] settings = viewmatsettings.provider();
        settings[0] = eyepos[0];
        settings[1] = eyepos[1];
        settings[2] = eyepos[2];
    }

    public void lookAt(float centerX, float centerY, float centerZ, float upX, float upY, float upZ){
        float[] eyepos = this.eyepos.provider();
        Matrix.setLookAtM(viewmat.provider(), 0, eyepos[0], eyepos[1], eyepos[2], centerX, centerY, centerZ, upX, upY, upZ);

        float[] settings = viewmatsettings.provider();

        settings[0] = eyepos[0];
        settings[1] = eyepos[1];
        settings[2] = eyepos[2];
        settings[3] = centerX;
        settings[4] = centerY;
        settings[5] = centerZ;
        settings[6] = upX;
        settings[7] = upY;
        settings[8] = upZ;
    }

    public void lookAtUpdate(){
        float[] settings = viewmatsettings.provider();
        Matrix.setLookAtM(viewmat.provider(), 0, settings[0], settings[1], settings[2], settings[3], settings[4], settings[5], settings[6], settings[7], settings[8]);
    }

    public void perspective(float fovy, float aspect, float znear, float zfar){
        Matrix.perspectiveM(perspectivemat.provider(), 0, fovy, aspect, znear, zfar);

        float[] settings = perspectivesettings.provider();

        settings[0] = fovy;
        settings[1] = aspect;
        settings[2] = znear;
        settings[3] = zfar;
    }

    public void orthographic(float left, float right, float bottom, float top, float znear, float zfar){
        Matrix.orthoM(orthographicmat.provider(), 0, left, right, bottom, top, znear, zfar);

        float[] settings = perspectivesettings.provider();

        settings[0] = left;
        settings[1] = right;
        settings[2] = bottom;
        settings[3] = top;
        settings[4] = znear;
        settings[5] = zfar;
    }

    public void updateViewPort(){
        int[] viewport = this.viewport.provider();
        GLES32.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    public void updateViewProjection(){
        Matrix.multiplyMM(viewprojectionmat.provider(), 0, projectionmat.provider(), 0, viewmat.provider(), 0);
    }

    public void multiplyViewPerspective(float[] results, int offset, float[] point, int offset2){
        Matrix.multiplyMV(results, offset, viewprojectionmat.provider(), 0, point, offset2);

        float w = results[offset + 3];
        results[offset] /= w;
        results[offset + 1] /= w;
        results[offset + 2] /= w;
    }

    public void convertToMVP(float[] results, int offset, float[] model){
        Matrix.multiplyMM(results, offset, viewprojectionmat.provider(), 0, model, 0);
    }

    public void unProject2DPoint(float x, float y, float[] resultsnear, int offset1, float[] resultsfar, int offset2){
        y = viewPortHeight() - y;

        GLU.gluUnProject(x, y, 0F, viewmat.provider(), 0, projectionmat.provider(), 0, viewport.provider(), 0, resultsnear, offset1);
        GLU.gluUnProject(x, y, 1F, viewmat.provider(), 0, projectionmat.provider(), 0, viewport.provider(), 0, resultsfar, offset2);

        y = resultsnear[offset1 + 3];

        resultsnear[offset1] /= y;
        resultsnear[offset1 + 1] /= y;
        resultsnear[offset1 + 2] /= y;

        y = resultsfar[offset2 + 3];

        resultsfar[offset2] /= y;
        resultsfar[offset2 + 1] /= y;
        resultsfar[offset2 + 2] /= y;
    }

    public void setPerspectiveMode(){
        projectionmat = perspectivemat;
    }

    public void setOrthographicMode(){
        projectionmat = orthographicmat;
    }

    public VLArrayFloat perspectiveMatrix(){
        return perspectivemat;
    }

    public VLArrayFloat orthographicMatrix(){
        return orthographicmat;
    }

    public VLArrayFloat viewMatrix(){
        return viewmat;
    }

    public VLArrayFloat viewProjectionMatrix(){
        return viewprojectionmat;
    }

    public VLArrayInt viewPort(){
        return viewport;
    }

    public VLArrayFloat viewMatrixSettings(){
        return viewmatsettings;
    }

    public VLArrayFloat perspectiveSettings(){
        return perspectivesettings;
    }

    public VLArrayFloat orhtographicsSettings(){
        return orthographicsettings;
    }

    public int viewPortX(){
        return viewport.get(0);
    }

    public int viewPortY(){
        return viewport.get(1);
    }

    public int viewPortWidth(){
        return viewport.get(2);
    }

    public int viewPortHeight(){
        return viewport.get(3);
    }

    public VLArrayFloat eyePosition(){
        return eyepos;
    }
}