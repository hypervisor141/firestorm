package hypervisor.firestorm.concurrency;

import hypervisor.firestorm.engine.FSCFrames;
import hypervisor.vanguard.variable.VLVRunnerThreadTask;
import hypervisor.vanguard.variable.VLVTypeRunner;

public class FSVRunnerThreadTask extends VLVRunnerThreadTask{

    private static final PostReporter REPORTER = new PostReporter(){

        @Override
        public void iterated(int changes){
            FSCFrames.addExternalChangesForFrame(changes);
        }
    };

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, debug, REPORTER);
    }

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug, PostReporter reporter){
        super(root, freqmillis, freqextrananos, enablecomensator, debug, reporter);
    }

    protected FSVRunnerThreadTask(){

    }
}
