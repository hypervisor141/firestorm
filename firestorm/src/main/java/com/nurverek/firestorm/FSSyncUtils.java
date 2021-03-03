package com.nurverek.firestorm;

import com.nurverek.vanguard.VLSyncMap;

public class FSSyncUtils{

    public static final class Config extends VLSyncMap<Object, FSConfig>{

        public Config(FSConfig target){
            super(target);
        }

        @Override
        public void sync(Object source){
            if(target.policy() != FSConfig.POLICY_ALWAYS){
                target.policy(FSConfig.POLICY_ONCE);
            }
        }

        @Override
        public void sync(int i, Object source){
            sync(source);
        }
    }

    public static final class SchematicsUpdateBounds extends VLSyncMap<Object, FSSchematics>{

        public SchematicsUpdateBounds(FSSchematics schematics){
            super(schematics);
        }

        @Override
        public void sync(Object source){
            target.updateBoundaries();
        }

        @Override
        public void sync(int i, Object source){
            sync(source);
        }
    }

    public static final class SchematicsMarkUpdate extends VLSyncMap<Object, FSSchematics>{

        public SchematicsMarkUpdate(FSSchematics schematics){
            super(schematics);
        }

        @Override
        public void sync(Object source){
            target.markForNewUpdates();
        }

        @Override
        public void sync(int i, Object source){
            sync(source);
        }
    }

    public static class MatrixModel_ArrayModel extends VLSyncMap<FSMatrixModel, FSArrayModel>{

        public boolean replace;

        public MatrixModel_ArrayModel(FSArrayModel target, boolean replace){
            super(target);
            this.replace = replace;
        }

        @Override
        public void sync(FSMatrixModel source){
            target.transform(0, source, replace);
        }

        @Override
        public void sync(int i, FSMatrixModel source){
            sync(source);
        }
    }

    public static final class Bounds extends VLSyncMap<Object, FSBounds>{

        public Bounds(FSBounds target){
            super(target);
        }

        @Override
        public void sync(Object source){
            target.markForUpdate();
        }

        @Override
        public void sync(int i, Object source){
            sync(source);
        }
    }

    public static final class VertexBuffer extends VLSyncMap<Object, FSVertexBuffer>{

        public VertexBuffer(FSVertexBuffer target){
            super(target);
        }

        @Override
        public void sync(Object source){
            target.needsUpdate();
        }

        @Override
        public void sync(int i, Object source){
            sync(source);
        }
    }
}
