package com.firestorm.concurrency;

import com.firestorm.engine.FSCFrames;

import vanguard.variable.VLVRunnerThreadTask;
import vanguard.variable.VLVTypeRunner;

public class FSVRunnerThreadTask extends VLVRunnerThreadTask{

    private static final PostReporter REPORTER = new PostReporter(){

        @Override
        public void completed(int changes){
            FSCFrames.addExternalChangesForFrame(changes);
        }
    };

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, REPORTER, debug);
    }

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, PostReporter reporter, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, reporter, debug);
    }

    protected FSVRunnerThreadTask(){

    }
}
