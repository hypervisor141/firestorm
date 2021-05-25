package com.firestorm.program;

import android.opengl.GLES32;
import android.opengl.Matrix;

import com.firestorm.engine.FSCache;

import vanguard.array.VLArrayFloat;
import vanguard.primitive.VLFloat;
import vanguard.primitive.VLInt;

public final class FSShadowPoint extends FSShadow<FSLightPoint>{

    private VLArrayFloat[] lightvp;
    protected VLFloat znear;
    protected VLFloat zfar;

    public FSShadowPoint(FSLightPoint light, VLInt width, VLInt height, VLFloat minbias, VLFloat maxbias, VLFloat divident, VLFloat znear, VLFloat zfar){
        super(light, width, height, minbias, maxbias, divident);

        this.zfar = zfar;
        this.znear = znear;

        lightvp = new VLArrayFloat[6];

        for(int i = 0; i < 6; i++){
            lightvp[i] = new VLArrayFloat(new float[16]);
        }

        updateLightVP();
    }

    protected FSShadowPoint(){

    }

    @Override
    protected FSTexture initializeTexture(VLInt texunit, VLInt width, VLInt height){
        FSTexture texture = new FSTexture(new VLInt(GLES32.GL_TEXTURE_CUBE_MAP), texunit);
        texture.bind();
        texture.storage2D(1, GLES32.GL_DEPTH_COMPONENT32F, width.get(), height.get());
        texture.minFilter(GLES32.GL_NEAREST);
        texture.magFilter(GLES32.GL_NEAREST);
        texture.wrapS(GLES32.GL_CLAMP_TO_EDGE);
        texture.wrapT(GLES32.GL_CLAMP_TO_EDGE);
        texture.wrapR(GLES32.GL_CLAMP_TO_EDGE);
        texture.compareMode(GLES32.GL_COMPARE_REF_TO_TEXTURE);
        texture.compareFunc(GLES32.GL_LEQUAL);
        texture.unbind();

        return texture;
    }

    @Override
    protected FSFrameBuffer initializeFrameBuffer(FSTexture texture){
        FSFrameBuffer buffer = new FSFrameBuffer();
        buffer.initialize();
        buffer.bind();
        buffer.attachTexture(GLES32.GL_DEPTH_ATTACHMENT, texture.id(), 0);
        buffer.checkStatus();

        FSCache.INT1[0] = GLES32.GL_NONE;

        GLES32.glReadBuffer(GLES32.GL_NONE);
        GLES32.glDrawBuffers(0, FSCache.INT1, 0);

        buffer.unbind();

        return buffer;
    }

    public void updateLightVP(){
        Matrix.perspectiveM(PERSPECTIVECACHE, 0, 90, (float)width.get() / height.get(), znear.get(), zfar.get());
        float[] pos = light.position().provider();

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0], pos[1], pos[2], pos[0] + 1F, pos[1], pos[2], 0, -1F, 0);
        Matrix.multiplyMM(lightvp[0].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0], pos[1], pos[2],pos[0] - 1F, pos[1], pos[2], 0, -1F, 0);
        Matrix.multiplyMM(lightvp[1].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0], pos[1], pos[2], pos[0], pos[1] + 1F, pos[2], 0, 0, 1F);
        Matrix.multiplyMM(lightvp[2].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0], pos[1], pos[2], pos[0], pos[1] - 1F, pos[2], 0, 0, -1F);
        Matrix.multiplyMM(lightvp[3].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0] , pos[1], pos[2], pos[0], pos[1], pos[2] + 1F, 0, -1F, 0);
        Matrix.multiplyMM(lightvp[4].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);

        Matrix.setLookAtM(LOOKCACHE, 0, pos[0], pos[1], pos[2], pos[0], pos[1], pos[2] - 1F, 0, -1F, 0);
        Matrix.multiplyMM(lightvp[5].provider(), 0, PERSPECTIVECACHE, 0, LOOKCACHE, 0);
    }

    public void zFar(VLFloat z){
        this.zfar = z;
    }

    public void zNear(VLFloat z){
        this.znear = z;
    }

    public VLFloat zFar(){
        return zfar;
    }

    public VLFloat zNear(){
        return znear;
    }

    public VLArrayFloat lightViewProjection(int face){
        return lightvp[face];
    }

    public VLArrayFloat[] lightViewProjection(){
        return lightvp;
    }
}
