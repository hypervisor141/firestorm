package com.nurverek.firestorm;

import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLInt;

public abstract class FSShadow<LIGHT extends FSLight> extends FSConfigSelective{

    public static final String FUNCTION_BIAS =
            "float shadowMapBias(vec3 normal, vec3 lightdir, float minbias, float maxbias){\n" +
                    "\t   return max(maxbias * (1.0 - dot(normal, lightdir)), minbias);\n" +
                    "}";

    public static final float[] PERSPECTIVECACHE = new float[16];
    public static final float[] LOOKCACHE = new float[16];
    
    protected FSFrameBuffer framebuffer;
    protected FSTexture texture;
    protected LIGHT light;

    protected VLInt width;
    protected VLInt height;
    protected VLFloat minbias;
    protected VLFloat maxbias;
    protected VLFloat divident;

    public FSShadow(int size, int resizer, LIGHT light, VLInt width, VLInt height, VLFloat minbias, VLFloat maxbias, VLFloat divident){
        super(size, resizer);

        this.light = light;
        this.width = width;
        this.height = height;
        this.divident = divident;
        this.minbias = minbias;
        this.maxbias = maxbias;
    }

    public void initialize(VLInt texunit){
        texture = initializeTexture(texunit, width, height);
        framebuffer = initializeFrameBuffer(texture);
    }

    protected abstract FSTexture initializeTexture(VLInt texunit, VLInt width, VLInt height);

    protected abstract FSFrameBuffer initializeFrameBuffer(FSTexture texture);

    public void width(VLInt width){
        this.width = width;
    }

    public void height(VLInt height){
        this.height = height;
    }

    public void minBias(VLFloat b){
        minbias = b;
    }

    public void maxBias(VLFloat b){
        maxbias = b;
    }

    public void divident(VLFloat divident){
        this.divident = divident;
    }

    public VLInt width(){
        return width;
    }

    public VLInt height(){
        return height;
    }

    public VLFloat minBias(){
        return minbias;
    }

    public VLFloat maxBias(){
        return maxbias;
    }

    public VLFloat divident(){
        return divident;
    }

    public FSTexture texture(){
        return texture;
    }

    public FSFrameBuffer frameBuffer(){
        return framebuffer;
    }

    public LIGHT light(){
        return light;
    }

    public String getBiasFunction(){
        return FUNCTION_BIAS;
    }

    public abstract String[] getStructMemebers();

    public abstract String getShadowFunction();

    public abstract String getSoftShadowFunction();

    public void destroy(){
        framebuffer.destroy();
        texture.destroy();

        texture = null;
        framebuffer = null;
    }

}
