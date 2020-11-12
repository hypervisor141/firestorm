package com.nurverek.firestorm;

import android.opengl.GLES32;
import android.opengl.Matrix;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLInt;
import com.nurverek.vanguard.VLListType;

public final class FSShadowPoint extends FSShadow {

    private static int[] DRAWBUFFERMODECACHE = new int[]{ GLES32.GL_NONE };

    public static final String[] STRUCT_MEMBERS = new String[]{
            "float minbias",
            "float maxbias",
            "float divident",
            "float zfar"
    };

    public static final String FIELD_CONST_SHADOW_PCF_SAMPLING =
            "const vec3 sampleOffsetDirections[20] = vec3[]\n" +
                    "(\n" +
                    "   vec3( 1,  1,  1), vec3( 1, -1,  1), vec3(-1, -1,  1), vec3(-1,  1,  1), \n" +
                    "   vec3( 1,  1, -1), vec3( 1, -1, -1), vec3(-1, -1, -1), vec3(-1,  1, -1),\n" +
                    "   vec3( 1,  1,  0), vec3( 1, -1,  0), vec3(-1, -1,  0), vec3(-1,  1,  0),\n" +
                    "   vec3( 1,  0,  1), vec3(-1,  0,  1), vec3( 1,  0, -1), vec3(-1,  0, -1),\n" +
                    "   vec3( 0,  1,  1), vec3( 0, -1,  1), vec3( 0, -1, -1), vec3( 0,  1, -1)\n" +
                    ");";

    public static final String FUNCTION_NORMAL =
            "float shadowMap(vec3 fragPos, vec3 lightPos, samplerCube shadowmap, float zfar, float bias, float divident){\n" +
                    "\t    vec3 fragToLight = fragPos - lightPos;\n" +
                    "\t    return 1.0 - ((length(fragToLight) - bias) > (texture(shadowmap, fragToLight).r * zfar) ? 1.0 : 0.0) / divident;\n" +
                    "}\n";

    public static final String FUNCTION_SOFT =
            "float shadowMap(vec3 fragPos, vec3 lightPos, vec3 cameraPos, samplerCube shadowmap, float zfar, float bias, int samples, float divident){\n" +
                    "\t    vec3 fragToLight = fragPos - lightPos;\n" +
                    "\t    float currentDepth = length(fragToLight) - bias;\n" +
                    "\t    float shadow = 0.0;\n" +
                    "\t    float viewDistance = length(cameraPos - fragPos);\n" +
                    "\t    float diskRadius = (1.0 + (viewDistance / zfar)) / 25.0;\n" +
                    "\t    for(int i = 0; i < samples; i++){\n" +
                    "\t        if(currentDepth > (texture(shadowmap, fragToLight + sampleOffsetDirections[i] * diskRadius).r * zfar)){\n" +
                    "\t            shadow += 1.0;\n" +
                    "\t        }\n" +
                    "\t    }\n" +
                    "\t    return 1.0 - (shadow / float(samples)) / divident;\n" +
                    "}\n";

    public static final int SELECT_STRUCT_DATA = 0;
    public static final int SELECT_LIGHT_TRANSFORMS = 1;

    private VLArrayFloat[] lightvp;
    protected VLFloat znear;
    protected VLFloat zfar;

    public FSShadowPoint(FSLight light, VLInt width, VLInt height, VLFloat minbias, VLFloat maxbias, VLFloat divident, VLFloat znear, VLFloat zfar){
        super(2, 0, light, width, height, minbias, maxbias, divident);

        this.zfar = zfar;
        this.znear = znear;

        lightvp = new VLArrayFloat[6];

        for(int i = 0; i < 6; i++){
            lightvp[i] = new VLArrayFloat(new float[16]);
        }

        updateLightVP();

        configs().add(new FSConfigSequence(new VLListType<>(new FSConfig[]{
                new FSP.Uniform1f(minbias),
                new FSP.Uniform1f(maxbias),
                new FSP.Uniform1f(divident),
                new FSP.Uniform1f(zfar)
        }, 0)));
        
        configs().add(new FSConfigSequence(new VLListType<FSConfig>(new FSConfig[]{
                new FSP.UniformMatrix4fvd(lightViewProjection(0), 0, 1),
                new FSP.UniformMatrix4fvd(lightViewProjection(1), 0, 1),
                new FSP.UniformMatrix4fvd(lightViewProjection(2), 0, 1),
                new FSP.UniformMatrix4fvd(lightViewProjection(3), 0, 1),
                new FSP.UniformMatrix4fvd(lightViewProjection(4), 0, 1),
                new FSP.UniformMatrix4fvd(lightViewProjection(5), 0, 1)

        }, 0)));
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
        FSFrameBuffer framebuffer = new FSFrameBuffer();
        framebuffer.initialize();
        framebuffer.bind();
        framebuffer.attachTexture(GLES32.GL_DEPTH_ATTACHMENT, texture.id(), 0);
        framebuffer.checkStatus();

        FSRenderer.readBuffer(GLES32.GL_NONE);
        FSRenderer.drawBuffers(0, DRAWBUFFERMODECACHE, 0);

        framebuffer.unbind();

        return framebuffer;
    }

    public void updateLightVP(){
        Matrix.perspectiveM(PERSPECTIVECACHE, 0, 90, width.get() / height.get(), znear.get(), zfar.get());
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

    @Override
    public String[] getStructMemebers(){
        return STRUCT_MEMBERS;
    }

    @Override
    public String getShadowFunction(){
        return FUNCTION_NORMAL;
    }

    @Override
    public String getSoftShadowFunction(){
        return FUNCTION_SOFT;
    }

    public String getShadowPCFSamplingFields(){
        return FIELD_CONST_SHADOW_PCF_SAMPLING;
    }
}
