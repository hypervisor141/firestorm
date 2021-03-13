package com.nurverek.firestorm;

import vanguard.VLThreadTask;
import vanguard.VLThreadWorker;
import vanguard.VLVTypeRunner;

public class FSVThreadTask implements VLThreadTask<VLThreadWorker>{

    public VLVTypeRunner root;
    private final long frequency;
    private final int debug;

    public FSVThreadTask(VLVTypeRunner root, long frequencynanos, int debug){
        this.root = root;
        this.frequency = frequencynanos;
        this.debug = debug;
    }

    @Override
    public void run(VLThreadWorker worker){
        long offsettime = 0;
        long elapsed = 0;

        while(worker.isEnabled()){
            if(elapsed < frequency){
                try{
                    Thread.sleep(frequency / 1000000, (int)(frequency - elapsed));

                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }

            }else if(FSControl.DEBUG_GLOBALLY && debug >= FSControl.DEBUG_NORMAL){
                System.out.println("[WARNING] [");
                System.out.println(worker.getName());
                System.out.println("] [VLV processor thread falling behind pre-set frequency of ");
                System.out.println(frequency);
                System.out.println("ns by ");
                System.out.println(elapsed - frequency);
                System.out.println("ns]");
            }

            offsettime = System.nanoTime();

            int changes = root.next();

            synchronized(FSR.RENDERLOCK){
                FSRFrames.addExternalChangesForFrame(changes);
            }

            elapsed = System.nanoTime() - offsettime;
        }
    }
}
