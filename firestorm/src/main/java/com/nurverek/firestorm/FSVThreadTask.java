package com.nurverek.firestorm;

import vanguard.VLVThreadTask;
import vanguard.VLVTypeRunner;

public class FSVThreadTask extends VLVThreadTask{

    private static final PostReporter REPORTER = new PostReporter(){

        @Override
        public void completed(int changes){
            FSRFrames.addExternalChangesForFrame(changes);
        }
    };

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, long freqextrananos, boolean debug){
        super(root, FSR.RENDERLOCK, freqmillis, freqextrananos, debug, REPORTER);
    }

    public FSVThreadTask(VLVTypeRunner root, Object lock, long freqmillis, long freqextrananos, boolean debug){
        super(root, lock, freqmillis, freqextrananos, debug, REPORTER);
    }

    public FSVThreadTask(VLVTypeRunner root, Object lock, long freqmillis, long freqextrananos, boolean debug, PostReporter reporter){
        super(root, lock, freqmillis, freqextrananos, debug, reporter);
    }
}
