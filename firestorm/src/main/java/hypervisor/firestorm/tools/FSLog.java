package hypervisor.firestorm.tools;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.vanguard.utils.VLLog;

public class FSLog extends VLLog{

    public FSLog(String[] tags, int resizeoverhead){
        super(tags, resizeoverhead);
        addTag(FSControl.LOGTAG);
    }

    public FSLog(String[] tags, int resizeoverhead, int debugtagsoffset){
        super(tags, resizeoverhead, debugtagsoffset);
        addTag(FSControl.LOGTAG);
    }

    public FSLog(int tagcapacity){
        super(tagcapacity);
        addTag(FSControl.LOGTAG);
    }

    public FSLog(int tagcapacity, int debugtagsoffset){
        super(tagcapacity, debugtagsoffset);
        addTag(FSControl.LOGTAG);
    }

    protected FSLog(){

    }
}
