package com.nurverek.firestorm;

import vanguard.VLVThreadTask;
import vanguard.VLVTypeRunner;

public class FSVThreadTask extends VLVThreadTask{

    private static final PostReporter REPORTER = new PostReporter(){

        @Override
        public void completed(int changes){
            FSCFrames.addExternalChangesForFrame(changes);
        }
    };

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, long freqextrananos, boolean debug){
        super(root, freqmillis, freqextrananos, debug, REPORTER);
    }

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, long freqextrananos, boolean debug, PostReporter reporter){
        super(root, freqmillis, freqextrananos, debug, reporter);
    }
}
