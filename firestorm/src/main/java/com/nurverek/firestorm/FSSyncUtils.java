package com.nurverek.firestorm;

import vanguard.VLSyncMap;
import vanguard.VLSyncType;

public class FSSyncUtils{

    public static final class Config extends VLSyncMap<Object, FSConfig>{

        public Config(FSConfig target){
            super(target);
        }

        public Config(Config src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            if(target.mode() != FSConfig.MODE_FULLTIME){
                target.mode(FSConfig.MODE_ONETIME);
            }
        }

        @Override
        public void copy(VLSyncType<Object> src, long flags){}

        @Override
        public Config duplicate(long flags){
            return new Config(this, depth);
        }
    }

    public static final class SchematicsUpdateBounds extends VLSyncMap<Object, FSSchematics>{

        public SchematicsUpdateBounds(FSSchematics schematics){
            super(schematics);
        }

        public SchematicsUpdateBounds(SchematicsUpdateBounds src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.updateBoundaries();
        }

        @Override
        public void copy(VLSyncType<Object> src, long flags){}

        @Override
        public SchematicsUpdateBounds duplicate(long flags){
            return new SchematicsUpdateBounds(this, depth);
        }
    }

    public static final class SchematicsMarkUpdate extends VLSyncMap<Object, FSSchematics>{

        public SchematicsMarkUpdate(FSSchematics schematics){
            super(schematics);
        }

        public SchematicsMarkUpdate(SchematicsMarkUpdate src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.markForNewUpdates();
        }

        @Override
        public void copy(VLSyncType<Object> src, long flags){}

        @Override
        public SchematicsMarkUpdate duplicate(long flags){
            return new SchematicsMarkUpdate(this, depth);
        }
    }

    public static class MatrixModel_ArrayModel extends VLSyncMap<FSMatrixModel, FSArrayModel>{

        public boolean replace;

        public MatrixModel_ArrayModel(FSArrayModel target, boolean replace){
            super(target);
            this.replace = replace;
        }

        public MatrixModel_ArrayModel(MatrixModel_ArrayModel src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(FSMatrixModel source){
            target.transform(0, source, replace);
        }

        @Override
        public void copy(VLSyncType<FSMatrixModel> src, long flags){
            replace = ((MatrixModel_ArrayModel)src).replace;
        }

        @Override
        public MatrixModel_ArrayModel duplicate(long flags){
            return new MatrixModel_ArrayModel(this, depth);
        }
    }

    public static final class Bounds extends VLSyncMap<Object, FSBounds>{

        public Bounds(FSBounds target){
            super(target);
        }

        public Bounds(Bounds src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.markForUpdate();
        }

        @Override
        public void copy(VLSyncType<Object> src, long flags){}

        @Override
        public Bounds duplicate(long flags){
            return new Bounds(this, depth);
        }
    }

    public static final class VertexBuffer extends VLSyncMap<Object, FSVertexBuffer<?>>{

        public VertexBuffer(FSVertexBuffer<?> target){
            super(target);
        }

        public VertexBuffer(VertexBuffer src, long flags){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.needsUpdate();
        }

        @Override
        public void copy(VLSyncType<Object> src, long flags){}

        @Override
        public VertexBuffer duplicate(long flags){
            return new VertexBuffer(this, depth);
        }
    }
}
