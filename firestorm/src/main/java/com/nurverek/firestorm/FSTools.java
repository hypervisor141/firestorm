package com.nurverek.firestorm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.EGL14;
import android.opengl.GLES32;
import android.opengl.GLU;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import vanguard.VLMath;

public final class FSTools{

    public static final int LOCATION_MID_LEFT = 2351;
    public static final int LOCATION_MID_RIGHT = 2352;
    public static final int LOCATION_MID_CENTER = 2353;
    public static final int LOCATION_TOP_LEFT = 2354;
    public static final int LOCATION_TOP_RIGHT = 2355;
    public static final int LOCATION_TOP_CENTER = 2356;
    public static final int LOCATION_BOTTOM_LEFT = 2357;
    public static final int LOCATION_BOTTOM_CENTER = 2358;
    public static final int LOCATION_BOTTOM_RIGHT = 2359;

    private static final TextPaint PAINTCACHE = new TextPaint();
    private static final Typeface TYPEFACE_NORMAL = Typeface.create("sans-serif", Typeface.NORMAL);
    private static final Typeface TYPEFACE_BOLD = Typeface.create("sans-serif", Typeface.BOLD);

    public static Bitmap generateTextedBitmap(Context cxt, String text, int dpisize, int color, int textcolor,
                                              boolean bold, int width, int height, int location, Bitmap.Config config){
        DisplayMetrics metrics = cxt.getResources().getDisplayMetrics();

        Bitmap b = Bitmap.createBitmap(metrics, width, height, config);
        Canvas c = new Canvas(b);

        PAINTCACHE.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpisize, metrics));
        PAINTCACHE.setColor(textcolor);
        PAINTCACHE.setAntiAlias(true);
        PAINTCACHE.setDither(false);
        PAINTCACHE.setTypeface(bold ? TYPEFACE_BOLD : TYPEFACE_NORMAL);

        float textw = PAINTCACHE.measureText(text, 0, text.length());
        float texth = PAINTCACHE.descent() + PAINTCACHE.ascent();

        c.drawColor(color);

        if(location == LOCATION_TOP_LEFT){
            c.drawText(text, 0, 0, PAINTCACHE);

        }else if(location == LOCATION_TOP_CENTER){
            c.drawText(text, width / 2f - textw / 2f, 0, PAINTCACHE);

        }else if(location == LOCATION_TOP_RIGHT){
            c.drawText(text, width - textw, 0, PAINTCACHE);

        }else if(location == LOCATION_MID_LEFT){
            c.drawText(text, 0, height / 2f - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_MID_CENTER){
            c.drawText(text, width / 2f - textw / 2f, height / 2 - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_MID_RIGHT){
            c.drawText(text, width - textw, height / 2f - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_LEFT){
            c.drawText(text, 0, height - texth, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_CENTER){
            c.drawText(text, width / 2f - textw / 2f, height - texth, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_RIGHT){
            c.drawText(text, width - textw, height - texth, PAINTCACHE);

        }else{
            throw new RuntimeException("Invalid location[" + location + "]");
        }

        return b;
    }

    public static void addTextOverBitmap(Context cxt, Bitmap b, String text, int dip, int textcolor, boolean bold, int location){
        DisplayMetrics metrics = cxt.getResources().getDisplayMetrics();
        Canvas c = new Canvas(b);

        int width = b.getWidth();
        int height = b.getHeight();

        PAINTCACHE.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics));
        PAINTCACHE.setColor(textcolor);
        PAINTCACHE.setAntiAlias(true);
        PAINTCACHE.setDither(false);
        PAINTCACHE.setTypeface(bold ? TYPEFACE_BOLD : TYPEFACE_NORMAL);

        float textw = PAINTCACHE.measureText(text, 0, text.length());
        float texth = PAINTCACHE.descent() + PAINTCACHE.ascent();

        if(location == LOCATION_TOP_LEFT){
            c.drawText(text, 0, 0, PAINTCACHE);

        }else if(location == LOCATION_TOP_CENTER){
            c.drawText(text, width / 2f - textw / 2f, 0, PAINTCACHE);

        }else if(location == LOCATION_TOP_RIGHT){
            c.drawText(text, width - textw, 0, PAINTCACHE);

        }else if(location == LOCATION_MID_LEFT){
            c.drawText(text, 0, height / 2f - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_MID_CENTER){
            c.drawText(text, width / 2f - textw / 2f, height / 2 - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_MID_RIGHT){
            c.drawText(text, width - textw, height / 2f - texth / 2f, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_LEFT){
            c.drawText(text, 0, height - texth, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_CENTER){
            c.drawText(text, width / 2f - textw / 2f, height - texth, PAINTCACHE);

        }else if(location == LOCATION_BOTTOM_RIGHT){
            c.drawText(text, width - textw, height - texth, PAINTCACHE);
        }
    }

    public static Bitmap getCircleCroppedBitmap(Bitmap bitmap, Bitmap.Config config){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getCenterCroppedBitmap(Bitmap bitmap, int width, int height, Bitmap.Config config){
        Bitmap output = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(output);

        int diffw = (bitmap.getWidth() - width) / 2;
        int diffh = (bitmap.getHeight() - height) / 2;

        final Paint paint = new Paint();
        final Rect src = new Rect(diffw, diffh, diffw + width, diffh + height);
        final Rect dst = new Rect(0, 0, output.getWidth(), output.getHeight());
        canvas.drawBitmap(bitmap, src, dst, paint);

        return output;
    }

    public static void addBackgroundToBitmap(Bitmap bitmap, int color){
        Canvas c = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        c.drawColor(color);
        c.drawBitmap(bitmap, 0, 0, paint);
    }

    public static void addTriangleFanBorderToBitmap(Bitmap bitmap, float xradius, float yradius, float radianoffset, int sidecount, float strokewidth, int color){
        Canvas c = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokewidth);
        paint.setColor(color);

        float xoffset = bitmap.getWidth() / 2;
        float yoffset = bitmap.getHeight() / 2;

        double cycle = 2 * Math.PI / sidecount;
        double x, y;

        Path path = new Path();

        for(int i = 0; i < sidecount + 1; i++){
            x = xoffset + xradius * Math.cos(radianoffset + (cycle * i));
            y = yoffset + yradius * Math.sin(radianoffset + (cycle * i));

            if(Math.abs(x) < 0.00001f){
                x = 0;
            }
            if(Math.abs(y) < 0.00001f){
                y = 0;
            }

            if(i == 0){
                path.moveTo((float)x, (float)y);
            }

            path.lineTo((float)x, (float)y);
        }

        c.drawPath(path, paint);
    }

    public static void addBitmapOverBitmap(Bitmap bitmap, Bitmap bitmap2){
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        c.drawBitmap(bitmap2, 0, 0, paint);
    }

    public static Bitmap replaceBitmapColor(Context cxt, Bitmap bitmap, int[] targets, int[] replacement){
        Bitmap result = convertBitmapToMutable(cxt, bitmap);

        int[] pixels = new int[result.getWidth() * result.getHeight()];
        result.getPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());

        int replace = Color.argb(replacement[0], replacement[1], replacement[2], replacement[3]);

        for(int index = 0; index < pixels.length; index++){
            int color = pixels[index];

            for(int i = 0; i < targets.length; i++){
                if(color == targets[i]){
                    pixels[index] = replace;
                    break;
                }
            }
        }

        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    public static Bitmap convertBitmapToMutable(final Context context, final Bitmap bitmap) {
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        final Bitmap.Config type = bitmap.getConfig();
        File outputFile = null;
        final File outputDir = context.getCacheDir();

        try{
            outputFile = File.createTempFile(Long.toString(System.currentTimeMillis()), null, outputDir);
            outputFile.deleteOnExit();
            RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile, "rw");

            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, bitmap.getRowBytes() * height);

            bitmap.copyPixelsToBuffer(map);
            bitmap.recycle();
            map.position(0);

            Bitmap result = Bitmap.createBitmap(width, height, type);
            result.copyPixelsFromBuffer(map);

            channel.close();
            randomAccessFile.close();
            outputFile.delete();

            return result;
        }catch(final Exception e){

        }finally{
            if(outputFile != null){
                outputFile.delete();
            }
        }

        return null;
    }

    public static byte[] convertBitmapToBytes(Bitmap b){
        ByteBuffer buffer = ByteBuffer.allocateDirect(12 + b.getByteCount());
        buffer.order(ByteOrder.nativeOrder());
        buffer.position(0);
        buffer.putInt(b.getWidth());
        buffer.putInt(b.getHeight());
        b.copyPixelsToBuffer(buffer);

        return buffer.array();
    }

    public static Bitmap convertBytesToBitmap(byte[] data, Bitmap.Config config){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(0);
        int width = buffer.getInt();
        int height = buffer.getInt();

        Bitmap b = Bitmap.createBitmap(width, height, config);
        b.copyPixelsFromBuffer(buffer);

        return b;
    }

    public static void hideKeyboard(Activity act){
        InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
    }

    public static void hideSystemUI(Activity act){
        act.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public static void showSystemUI(Activity act){
        act.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void checkGLError(){
        if(FSControl.DEBUG_GLOBALLY){
            int error;

            while((error = GLES32.glGetError()) != GLES32.GL_NO_ERROR){
                throw new RuntimeException(GLU.gluErrorString(error) + "[" + error + "]");
            }
        }
    }

    public static void checkEGLError(String name){
        if(FSControl.DEBUG_GLOBALLY){
            int error;

            while((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS){
                throw new RuntimeException(name + " : EGLError " + error);
            }
        }
    }

    public static float[] createVerticesForTriangleFan(int sidecount, float radius, float radianoffset, float zvalue, boolean Wpadding){
        double cycle = 2 * Math.PI / sidecount;
        float[] coords;

        if(Wpadding){
            coords = new float[(sidecount + 1) * 4];

            coords[0] = 0;
            coords[1] = 0;
            coords[2] = zvalue;
            coords[3] = 1;

            for(int i = 1, i2 = 0; i < sidecount + 1; i++, i2++){
                double x, y;

                x = radius * Math.cos(radianoffset + (cycle * i2));
                y = radius * Math.sin(radianoffset + (cycle * i2));

                if(Math.abs(x) < 0.00001f){
                    x = 0;
                }
                if(Math.abs(y) < 0.00001f){
                    y = 0;
                }

                int index = (i * 4);

                coords[index] = (float)x;
                coords[index + 1] = (float)y;
                coords[index + 2] = zvalue;
                coords[index + 3] = 1;
            }

        }else{
            coords = new float[(sidecount + 1) * 3];

            coords[0] = 0;
            coords[1] = 0;
            coords[2] = zvalue;

            for(int i = 1, i2 = 0; i < sidecount + 1; i++, i2++){
                double x, y;

                x = radius * Math.cos(radianoffset + (cycle * i2));
                y = radius * Math.sin(radianoffset + (cycle * i2));

                if(Math.abs(x) < 0.00001f){
                    x = 0;
                }
                if(Math.abs(y) < 0.00001f){
                    y = 0;
                }

                int index = (i * 3);

                coords[index] = (float)x;
                coords[index + 1] = (float)y;
                coords[index + 2] = zvalue;
            }
        }

        return coords;
    }

    public static short[] createIndicesForTriangleFan(int sidecount){
        short[] indices = new short[sidecount + 2];

        for(short i = 0; i < indices.length - 1; i++){
            indices[i] = i;
        }

        indices[indices.length - 1] = 1;

        return indices;
    }

    public static float[] createTextureCoordsForTriangleFan(int sidecount){
        double cycle = Math.PI * 2f / sidecount;
        float[] texturecoords = new float[(sidecount + 1) * 2];
        texturecoords[0] = 0.5f;
        texturecoords[1] = 0.5f;

        for(int i = 1, i2 = sidecount; i < sidecount + 1; i++, i2--){
            double x, y;

            x = 0.5f + 0.5f * Math.cos(cycle * i2);
            y = 0.5f + 0.5f * Math.sin(cycle * i2);

            texturecoords[(i * 2)] = (float)x;
            texturecoords[(i * 2) + 1] = (float)y;
        }

        return texturecoords;
    }

    public static float[] createTextureCoordsForTriangleFanVertices(float[] vertices, boolean wpadded){
        float[] coords;
        int jumps;

        if(wpadded){
            coords = new float[vertices.length / 2];
            jumps = 4;

        }else{
            coords = new float[(vertices.length / 3) * 2];
            jumps = 3;
        }

        float x, y;
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = -Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;

        for(int i = 0; i < vertices.length; i += jumps){
            x = vertices[i];
            y = vertices[i + 1];

            if(maxx < x){
                maxx = x;
            }
            if(minx > x){
                minx = x;
            }

            if(maxy < y){
                maxy = y;
            }
            if(miny > y){
                miny = y;
            }
        }

        for(int i = 0, i2 = 0; i < vertices.length; i += jumps, i2 += 2){
            coords[i2] = VLMath.range(vertices[i], minx, maxx, 0, 1);
            coords[i2 + 1] = VLMath.range(vertices[i + 1], miny, maxy, 0, 1);
        }

        return coords;
    }
}
