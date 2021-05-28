package hypervisor.firestorm.program;

import android.opengl.GLES32;

import hypervisor.firestorm.engine.FSCache;
import hypervisor.firestorm.engine.FSView;
import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.primitive.VLFloat;
import hypervisor.vanguard.primitive.VLInt;

public final class FSShadowDirect extends FSShadow<FSLightDirect>{

    protected FSView config;

    public FSShadowDirect(FSLightDirect light, VLInt width, VLInt height, VLFloat minbias, VLFloat maxbias, VLFloat divident){
        super(light, width, height, minbias, maxbias, divident);

        config = new FSView(false);
    }

    protected FSShadowDirect(){

    }

    @Override
    protected FSTexture initializeTexture(VLInt texunit, VLInt width, VLInt height){
        FSTexture texture = new FSTexture(new VLInt(GLES32.GL_TEXTURE_2D), texunit);
        texture.bind();
        texture.storage2D(1, GLES32.GL_DEPTH_COMPONENT32F, width.get(), height.get());
        texture.minFilter(GLES32.GL_NEAREST);
        texture.magFilter(GLES32.GL_NEAREST);
        texture.wrapS(GLES32.GL_CLAMP_TO_EDGE);
        texture.wrapT(GLES32.GL_CLAMP_TO_EDGE);
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
        buffer.attachTexture2D(GLES32.GL_DEPTH_ATTACHMENT, texture.target().get(), texture.id(), 0);
        buffer.checkStatus();

        FSCache.INT1[0] = GLES32.GL_NONE;

        GLES32.glReadBuffer(GLES32.GL_NONE);
        GLES32.glDrawBuffers(0, FSCache.INT1, 0);

        buffer.unbind();

        return buffer;
    }

    public void updateLightProjection(float upX, float upY, float upZ, float left, float right,
                                      float bottom, float top, float znear, float zfar){
        float[] pos = light.position().provider();
        float[] cent = light.center().provider();

        config.lookAt(pos[0], pos[1], pos[2], cent[0], cent[1], cent[2], upX, upY, upZ);
        config.orthographic(left, right, bottom, top, znear, zfar);
        config.applyViewProjection();
    }

    public VLArrayFloat lightViewProjection(){
        return config.matrixViewProjection();
    }

    public FSView viewConfig(){
        return config;
    }
}
