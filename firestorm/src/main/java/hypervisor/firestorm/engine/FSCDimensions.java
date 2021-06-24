package hypervisor.firestorm.engine;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

public final class FSCDimensions{

    protected static int mainw;
    protected static int mainh;
    protected static float mainasp;

    protected static int realw;
    protected static int realh;
    protected static float realasp;

    protected static float density;

    public static void initialize(Activity act){
        Display display = act.getWindowManager().getDefaultDisplay();
        Point point = new Point();

        display.getSize(point);
        mainw = point.x;
        mainh = point.y;
        mainasp = (float)realw / realh;

        display.getRealSize(point);
        realw = point.x;
        realh = point.y;
        realasp = (float)realw / realh;

        density = act.getResources().getDisplayMetrics().density;
    }

    protected static void setRealDimensions(int width, int height){

    }

    protected static void setMainDimensions(int width, int height){
        mainw = width;
        mainh = height;
        mainasp = (float)width / height;
    }

    public static int getWidth(){
        return mainw;
    }

    public static int getHeight(){
        return mainh;
    }

    public static float getAspectRatio(){
        return mainasp;
    }

    public static int getRealWidth(){
        return realw;
    }

    public static int getRealHeight(){
        return realh;
    }

    public static float getRealAspectRatio(){
        return realasp;
    }

    public static float getDensity(){
        return density;
    }
    
    protected static void destroy(boolean destroyonpause){
        if(destroyonpause){
            mainw = -1;
            mainh = -1;
            mainasp = -1;

            realw = -1;
            realh = -1;
            realasp = -1;

            density = -1;
        }
    }
}
