package com.nurverek.firestorm;

import vanguard.VLSyncMap;
import vanguard.VLSyncType;

public class FSSyncUtils{

    public static final class Config extends VLSyncMap<Object, FSConfig>{

        public Config(FSConfig target){
            super(target);
        }

        public Config(Config src, int depth){
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
        public void copy(VLSyncType<Object> src, int depth){}

        @Override
        public Config duplicate(int depth){
            return new Config(this, depth);
        }
    }

    public static final class SchematicsUpdateBounds extends VLSyncMap<Object, FSSchematics>{

        public SchematicsUpdateBounds(FSSchematics schematics){
            super(schematics);
        }

        public SchematicsUpdateBounds(SchematicsUpdateBounds src, int depth){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.updateBoundaries();
        }

        @Override
        public void copy(VLSyncType<Object> src, int depth){}

        @Override
        public SchematicsUpdateBounds duplicate(int depth){
            return new SchematicsUpdateBounds(this, depth);
        }
    }

    public static final class SchematicsMarkUpdate extends VLSyncMap<Object, FSSchematics>{

        public SchematicsMarkUpdate(FSSchematics schematics){
            super(schematics);
        }

        public SchematicsMarkUpdate(SchematicsMarkUpdate src, int depth){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.markForNewUpdates();
        }

        @Override
        public void copy(VLSyncType<Object> src, int depth){}

        @Override
        public SchematicsMarkUpdate duplicate(int depth){
            return new SchematicsMarkUpdate(this, depth);
        }
    }

    public static class MatrixModel_ArrayModel extends VLSyncMap<FSMatrixModel, FSArrayModel>{

        public boolean replace;

        public MatrixModel_ArrayModel(FSArrayModel target, boolean replace){
            super(target);
            this.replace = replace;
        }

        public MatrixModel_ArrayModel(MatrixModel_ArrayModel src, int depth){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(FSMatrixModel source){
            target.transform(0, source, replace);
        }

        @Override
        public void copy(VLSyncType<FSMatrixModel> src, int depth){
            replace = ((MatrixModel_ArrayModel)src).replace;
        }

        @Override
        public MatrixModel_ArrayModel duplicate(int depth){
            return new MatrixModel_ArrayModel(this, depth);
        }
    }

    public static final class Bounds extends VLSyncMap<Object, FSBounds>{

        public Bounds(FSBounds target){
            super(target);
        }

        public Bounds(Bounds src, int depth){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.markForUpdate();
        }

        @Override
        public void copy(VLSyncType<Object> src, int depth){}

        @Override
        public Bounds duplicate(int depth){
            return new Bounds(this, depth);
        }
    }

    public static final class VertexBuffer extends VLSyncMap<Object, FSVertexBuffer<?>>{

        public VertexBuffer(FSVertexBuffer<?> target){
            super(target);
        }

        public VertexBuffer(VertexBuffer src, int depth){
            super(null);
            copy(src, depth);
        }

        @Override
        public void sync(Object source){
            target.needsUpdate();
        }

        @Override
        public void copy(VLSyncType<Object> src, int depth){}

        @Override
        public VertexBuffer duplicate(int depth){
            return new VertexBuffer(this, depth);
        }
    }
}
