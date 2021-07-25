package hypervisor.firestorm.concurrency;

import hypervisor.firestorm.engine.FSCFrames;
import hypervisor.vanguard.variable.VLVRunnerThreadTask;
import hypervisor.vanguard.variable.VLVTypeRunner;

public class FSVThreadTask extends VLVRunnerThreadTask{

    private static final Checkpoint POSTRUN = new Checkpoint(){

        @Override
        public void process(int localchanges){
            FSCFrames.addExternalChangesForFrame(localchanges);
        }
    };

    public FSVThreadTask(VLVTypeRunner root, long freqmillis, int freqextrananos, boolean enablecompensator, boolean debug){
        super(root, freqmillis, freqextrananos, enablecompensator, debug, null, POSTRUN);
    }

    protected FSVThreadTask(){

    }
}
