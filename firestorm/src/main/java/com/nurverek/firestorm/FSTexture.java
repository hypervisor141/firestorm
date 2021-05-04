package com.nurverek.firestorm;

import android.graphics.Bitmap;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import java.nio.Buffer;

import vanguard.VLInt;

public class FSTexture{

    private VLInt target;
    private VLInt texunit;

    private int id;
    
    public FSTexture(VLInt target, VLInt texunit){
        initialize(target, texunit);
    }

    public FSTexture(int id, VLInt target, VLInt texunit){
        this.id = id;
        initialize(target, texunit);
    }

    public FSTexture(){
        id = -1;
    }

    public void initialize(VLInt target, VLInt texunit){
        this.target = target;
        this.texunit = texunit;

        GLES32.glGenTextures(1, FSCache.INT1, 0);
        id = FSCache.INT1[0];
    }

    public void activateUnit(){
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + texunit.get());
    }

    public void bind(){
        GLES32.glBindTexture(target.get(), id);
    }

    public void unbind(){
        GLES32.glBindTexture(target.get(), 0);
    }

    public void storage2D(int levels, int internalformat, int width, int height){
        GLES32.glTexStorage2D(target.get(), levels, internalformat, width, height);
    }

    public void image2D(int level, Bitmap bitmap){
        GLUtils.texImage2D(target.get(), level, bitmap, 0);
        bitmap.recycle();
    }

    public void image2D(int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){
        GLES32.glTexImage2D(target.get(), level, internalformat, width, height, border, format, type, pixels);
    }

    public void subImage2D(int level, int xoffset, int yoffset, Bitmap bitmap){
        GLUtils.texSubImage2D(target.get(), level, xoffset, yoffset, bitmap);
    }

    public void subImage2D(int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){
        GLES32.glTexSubImage2D(target.get(), level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void storage3D(int levels, int internalFormat, int width, int height, int depth){
        GLES32.glTexStorage3D(target.get(), levels, internalFormat, width, height, depth);
    }

    public void image3D(int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels){
        GLES32.glTexImage3D(target.get(), level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void image3D(int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset){
        GLES32.glTexImage3D(target.get(), level, internalformat, width, height, depth, border, format, type, offset);
    }

    public void subImage3D(int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels){
        GLES32.glTexSubImage3D(target.get(), level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void subImage3D(int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset){
        GLES32.glTexSubImage3D(target.get(), level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
    }

    public void cubemap(int level, Bitmap right, Bitmap left, Bitmap top, Bitmap bottom, Bitmap front, Bitmap back){
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_X, level, right, 0);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, level, left, 0);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, level, top, 0);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, level, bottom, 0);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, level, front, 0);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, level, back, 0);

        right.recycle();
        left.recycle();
        top.recycle();
        bottom.recycle();
        front.recycle();
        back.recycle();
    }

    public void cubemap(int level, int internalformat, int width, int height,
                                   int border, int format, int type, Buffer face1, Buffer face2,
                                   Buffer face3, Buffer face4, Buffer face5, Buffer face6){

        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_X, level, internalformat, width, height, border, format, type, face1);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, level, internalformat, width, height, border, format, type, face2);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, level, internalformat, width, height, border, format, type, face3);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, level, internalformat, width, height, border, format, type, face4);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, level, internalformat, width, height, border, format, type, face5);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, level, internalformat, width, height, border, format, type, face6);
    }

    public void generateMipMap(){
        GLES32.glGenerateMipmap(target.get());
    }
    
    public void wrapS(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_WRAP_S, mode);
    }

    public void wrapT(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_WRAP_T, mode);
    }

    public void wrapR(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_WRAP_R, mode);
    }

    public void minFilter(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_MIN_FILTER, mode);
    }

    public void magFilter(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_MAG_FILTER, mode);
    }

    public void compareMode(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_COMPARE_MODE, mode);
    }

    public void compareFunc(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_COMPARE_FUNC, mode);
    }

    public void baseLevel(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_BASE_LEVEL, mode);
    }

    public void maxLevel(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_MAX_LEVEL, mode);
    }

    public void swizzleR(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_SWIZZLE_R, mode);
    }

    public void swizzleG(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_SWIZZLE_G, mode);
    }

    public void swizzleB(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_SWIZZLE_B, mode);
    }

    public void swizzleA(int mode){
        GLES32.glTexParameteri(target.get(), GLES32.GL_TEXTURE_SWIZZLE_A, mode);
    }

    public int id(){
        return id;
    }

    public VLInt target(){
        return target;
    }

    public VLInt unit(){
        return texunit;
    }

    public void destroy(){
        target = null;
        texunit = null;

        FSCache.INT1[0] = id;
        GLES32.glDeleteTextures(1, FSCache.INT1, 0);
    }
}
