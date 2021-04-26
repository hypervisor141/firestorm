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

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, REPORTER, debug ? FSControl.LOGTAG : null);
    }

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, PostReporter reporter, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, reporter, debug ? FSControl.LOGTAG : null);
    }
}
