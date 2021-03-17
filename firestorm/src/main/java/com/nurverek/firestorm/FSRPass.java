package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.util.ArrayList;

import vanguard.VLListType;

public class FSRPass{

    private ArrayList<FSP> entries;
    private float[] color;

    private boolean clearcolor;
    private boolean cleardepth;
    private boolean clearstencil;
    private boolean draw;
    
    private long id;

    protected final ArrayList<Order> orders = new ArrayList<>(15);
    protected int debug;
    
    public FSRPass(int debug){
        entries = new ArrayList<>();

        clearcolor = true;
        cleardepth = true;
        clearstencil = true;
        draw = true;
        
        id = FSRFrames.getNextID();

        this.debug = debug;
    }

    public void add(FSG<?> source){
        VLListType<FSP> programs = source.programs();
        int size = programs.size();

        for(int i = 0; i < size; i++){
            entries.add(programs.get(i));
        }
    }

    public void add(FSP entry){
        entries.add(entry);
    }

    public void add(int index, FSP entry){
        entries.add(index, entry);
    }

    public void remove(FSP target){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            if(entries.get(i).id() == target.id()){
                entries.remove(i);
                i--;
            }
        }
    }

    public FSP remove(int index){
        return entries.remove(index);
    }

    public FSRPass setColor(float[] color){
        this.color = color;
        return this;
    }

    public FSRPass setClearColor(boolean enabled){
        clearcolor = enabled;
        return this;
    }

    public FSRPass setClearDepth(boolean enabled){
        cleardepth = enabled;
        return this;
    }

    public FSRPass setClearStencil(boolean enabled){
        clearstencil = enabled;
        return this;
    }

    public FSRPass setDrawMeshes(boolean enabled){
        draw = enabled;
        return this;
    }

    public float[] getColor(){
        return color;
    }

    public boolean getClearColor(){
        return clearcolor;
    }

    public boolean getClearDepth(){
        return cleardepth;
    }

    public boolean getClearStencil(){
        return clearstencil;
    }

    public boolean getDrawMeshes(){
        return draw;
    }

    public long id(){
        return id;
    }

    public FSP get(int index){
        return entries.get(index);
    }

    public FSP findEntryByID(int id){
        FSP entry;

        for(int i = 0; i < entries.size(); i++){
            entry = entries.get(i);

            if(entry.id() == id){
                return entry;
            }
        }

        return null;
    }

    public int size(){
        return entries.size();
    }

    public FSRPass build(){
        orders.add(new Order(){

            @Override
            public void execute(int orderindex, int passindex){
                FSRFrames.timeFrameStarted();
            }
        });
        
        if(clearcolor || cleardepth || clearstencil){
            int clearbit = 0;

            if(clearcolor){
                clearbit |= GLES32.GL_COLOR_BUFFER_BIT;
            }
            if(cleardepth){
                clearbit |= GLES32.GL_DEPTH_BUFFER_BIT;
            }
            if(clearstencil){
                clearbit |= GLES32.GL_STENCIL_BUFFER_BIT;
            }

            final int clearbitf = clearbit;

            orders.add(new Order(){

                @Override
                public void execute(int orderindex, int passindex){
                    GLES32.glClear(clearbitf);

                    if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                        try{
                            FSTools.checkGLError();

                        }catch(Exception ex){
                            throw new RuntimeException("Error running glClear() for bits[" + clearbitf + "] renderPass[" + passindex + "]", ex);
                        }
                    }
                }
            });

            if(clearcolor){
                orders.add(new Order(){

                    @Override
                    public void execute(int orderindex, int passindex){
                        GLES32.glClearColor(color[0], color[1], color[2], color[3]);

                        if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                            try{
                                FSTools.checkGLError();

                            }catch(Exception ex){
                                throw new RuntimeException("Error clearing color with glClear() on renderPass[" + passindex + "]", ex);
                            }
                        }
                    }
                });
            }
        }
        if(draw){
            orders.add(new Order(){

                @Override
                public void execute(int orderindex, int passindex){
                    draw();
                }
            });
        }
        
        return this;
    }

    protected void execute(){
        int size = orders.size();

        for(int i = 0; i < size; i++){
            orders.get(i).execute(i, FSR.CURRENT_PASS_INDEX);
        }
    }

    protected void noitifyPostFrameSwap(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).postFrame(FSR.CURRENT_PASS_INDEX);

            if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                try{
                    FSTools.checkGLError();

                }catch(Exception ex){
                    throw new RuntimeException("Error running postFrameSwap() for Entry[" + i + "] PassIndex[" + FSR.CURRENT_PASS_INDEX + "]", ex);
                }
            }
        }
    }

    private void draw(){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreDraw();

        int size = entries.size();
        FSP entry;

        for(int index = 0; index < size; index++){
            entry = entries.get(index);

            FSR.CURRENT_ENTRY_INDEX = index;

            entry.draw(FSR.CURRENT_PASS_INDEX);

            if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                try{
                    FSTools.checkGLError();

                }catch(Exception ex){
                    throw new RuntimeException("Error running draw() for Entry[" + index + "] PassIndex[" + FSR.CURRENT_PASS_INDEX + "]", ex);
                }
            }
        }

        events.GLPostDraw();
    }

    public FSRPass copySettings(FSRPass src){
        clearcolor = src.clearcolor;
        cleardepth = src.cleardepth;
        clearstencil = src.clearstencil;
        draw = src.draw;
        
        id = FSRFrames.getNextID();

        return this;
    }

    public void destroy(){
        for(int i = 0; i < entries.size(); i++){
            entries.get(i).destroy();

            if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                try{
                    FSTools.checkGLError();

                }catch(Exception ex){
                    throw new RuntimeException("Error running destroy() for Entry[" + i + "] PassIndex[" + FSR.CURRENT_PASS_INDEX + "]", ex);
                }
            }
        }

        entries = null;
    }
    
    protected static interface Order{
        
        void execute(int orderindex, int passindex);
    }
}
