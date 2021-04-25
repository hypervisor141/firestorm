package com.nurverek.firestorm;

import android.os.Looper;

import java.util.ArrayList;

public class FSRThread extends Thread{

    protected static final int CREATE_GL_CONTEXT = 7435;
    protected static final int SURFACE_CREATED = 7436;
    protected static final int SURFACE_CHANGED = 7437;
    protected static final int DRAW_FRAME = 7438;

    private final Object lock;

    private boolean enabled;
    private boolean lockdown;
    private boolean waiting;

    private final ArrayList<Integer> orders;
    private final ArrayList<Object> data;

    public FSRThread(){
        orders = new ArrayList<>();
        data = new ArrayList<>();

        lock = new Object();

        enabled = false;
        lockdown = false;
        waiting = false;
    }

    @Override
    public void run(){
        Looper.prepare();

        synchronized(lock){
            enabled = true;
            lock.notifyAll();
        }

        ArrayList<Object> datacache = new ArrayList<>();
        ArrayList<Integer> orderscache = new ArrayList<>();

        while(true){
            synchronized(lock){
                if(!enabled){
                    orders.clear();
                    data.clear();
                    return;

                }else{
                    while(lockdown || orders.isEmpty()){
                        try{
                            lock.notifyAll();
                            waiting = true;

                            lock.wait();
                            waiting = false;

                        }catch(InterruptedException ex){
                            //
                        }

                        if(!enabled){
                            orders.clear();
                            data.clear();
                            return;
                        }
                    }

                    orderscache.addAll(orders);
                    datacache.addAll(data);

                    orders.clear();
                    data.clear();
                }
            }

            int size = orderscache.size();
            int order;
            Object obj;

            for(int i = 0; i < size; i++){
                order = orderscache.get(i);
                obj = datacache.get(i);

                if(order == CREATE_GL_CONTEXT){
                    Object[] msg = (Object[])obj;
                    FSCEGL.initialize(FSControl.getSurface().getHolder(), (int[])msg[0], (boolean)msg[1]);

                }else if(order == SURFACE_CREATED){
                    FSR.onSurfaceCreated((boolean)obj);

                }else if(order == SURFACE_CHANGED){
                    int[] a = (int[])obj;
                    FSR.onSurfaceChanged(a[0], a[1]);

                }else if(order == DRAW_FRAME){
                    FSR.onDrawFrame();
                }
            }

            orderscache.clear();
            datacache.clear();
        }
    }

    protected void initiate(){
        start();

        synchronized(lock){
            while(!enabled){
                try{
                    lock.wait();
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean enabled(){
        synchronized(lock){
            return enabled;
        }
    }

    public Object lock(){
        return lock;
    }

    public void post(int code, Object d){
        synchronized(lock){
            orders.add(code);
            data.add(d);

            lock.notifyAll();
        }
    }

    public void lockdown(){
        synchronized(lock){
            lockdown = true;

            orders.clear();
            data.clear();

            while(!waiting){
                try{
                    lock.wait();

                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public void unlock(){
        synchronized(lock){
            lockdown = false;

            orders.clear();
            data.clear();

            lock.notifyAll();
        }
    }

    public void shutdown(){
        try{
            synchronized(lock){
                lockdown = false;
                enabled = false;

                lock.notifyAll();
            }

            join();

        }catch(InterruptedException ex){
            ex.printStackTrace();
        }
    }
}
