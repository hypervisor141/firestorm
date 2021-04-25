package com.nurverek.firestorm;

import vanguard.VLVRunnerThreadTask;
import vanguard.VLVTypeRunner;

public class FSVRunnerThreadTask extends VLVRunnerThreadTask{

    private static final PostReporter REPORTER = new PostReporter(){

        @Override
        public void completed(int changes){
            FSCFrames.addExternalChangesForFrame(changes);
        }
    };

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean debug, boolean enablecomensator){
        super(root, freqmillis, freqextrananos, debug, enablecomensator, REPORTER);
    }

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean debug, boolean enablecomensator, PostReporter reporter){
        super(root, freqmillis, freqextrananos, debug, enablecomensator, reporter);
    }
}
