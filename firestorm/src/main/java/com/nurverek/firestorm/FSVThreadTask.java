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

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean debug, boolean enablecomensator){
        super(root, freqmillis, freqextrananos, debug, enablecomensator, REPORTER);
    }

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean debug, boolean enablecomensator, PostReporter reporter){
        super(root, freqmillis, freqextrananos, debug, enablecomensator, reporter);
    }
}
