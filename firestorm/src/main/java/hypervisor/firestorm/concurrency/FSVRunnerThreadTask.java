package hypervisor.firestorm.concurrency;

import hypervisor.firestorm.engine.FSCFrames;
import hypervisor.firestorm.engine.FSR;
import hypervisor.vanguard.variable.VLVRunnerThreadTask;
import hypervisor.vanguard.variable.VLVTypeRunner;

public class FSVRunnerThreadTask extends VLVRunnerThreadTask{

    private static final Checkpoint PRERUN = new Checkpoint(){

        @Override
        public void process(int localchanges){
            FSR.requestSyncWindow();
        }
    };
    private static final Checkpoint POSTRUN = new Checkpoint(){

        @Override
        public void process(int localchanges){
            FSR.notifySyncCompleted();
            FSCFrames.addExternalChangesForFrame(localchanges);
        }
    };

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug, Checkpoint prerun, Checkpoint postrun){
        super(root, freqmillis, freqextrananos, enablecomensator, debug, prerun, postrun);
    }

    public FSVRunnerThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecomensator, boolean debug){
        super(root, freqmillis, freqextrananos, enablecomensator, debug, PRERUN, POSTRUN);
    }

    protected FSVRunnerThreadTask(){

    }
}
