package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLThreadManager;
import vanguard.VLThreadWorker;

public class FSVThreadManager{

    private static VLThreadManager manager;

    protected static void initialize(){
        manager = new VLThreadManager(8);
        EXTERNAL_CHANGES
    }

    public static VLThreadManager manager(){
        return manager;
    }

    protected static void next(){
        int size = passes.size();
        int changes = 0;

        for(int i = 0; i < size; i++){
            changes += passes.get(i).next();
        }

        EXTERNAL_CHANGES = 0;
        INTERNAL_CHANGES = 0;

        FSRFrames.processFrameAndSignalNextFrame(changes);
    }

    public static void destroy(){
        manager.destroy();
    }
}
