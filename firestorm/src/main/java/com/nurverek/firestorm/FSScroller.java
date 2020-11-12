package com.nurverek.firestorm;//package firestone;
//
//import android.opengl.GLES30;
//
//import vanguard.VLEfficientArray;
//import vanguard.VLEfficientArrayI;
//import vanguard.VLV;
//import vanguard.VLVConstant;
//import vanguard.VLVProcessor;
//import vanguard.VLVRanged;
//
//@SuppressWarnings("unused")
//public abstract class FSScroller extends FSConstructor {
//
//    public static final int AXIS_X = 326325;
//    public static final int AXIS_Y = 326326;
//    public static final int AXIS_Z = 326327;
//    public static final int AXIS_XY = 326328;
//    public static final int AXIS_XZ = 326329;
//    public static final int AXIS_YZ = 326330;
//    public static final int AXIS_XYZ = 326331;
//
//    private FSConstructor target;
//    private VLEfficientArray<VLEfficientArrayI> setindices;
//    private VLVProcessor animator;
//    private Listener listener;
//    private VLV.Interpolator interpolator;
//
//    private int stencilvalue;
//    private int scrollaxis;
//
//    public FSScroller(int scrollaxis, int stencilvalue, VLV.Interpolator interpolator){
//        setindices = new VLEfficientArray<>(1, 10);
//
//        this.stencilvalue = stencilvalue;
//        this.scrollaxis = scrollaxis;
//        this.interpolator = interpolator;
//    }
//
//
//
//    @Override
//    public void initialize(FSActivity act){
//        animator = new VLVProcessor(1, 10);
//        animator.setListener(new VLVProcessor.Listener(){
//
//            @Override
//            public void finishedAnimation(VLVProcessor a){
//                if(listener != null){
//                    listener.scrollFinished(FSScroller.this);
//                }
//            }
//        });
//
//        super.initialize(act);
//
//        if(PROCESSORS.size() == 0){
//            PROCESSORS.add(animator);
//
//        }else{
//            PROCESSORS.set(0, animator);
//        }
//    }
//
//    protected abstract void drawScroller();
//
//    @Override
//    protected void customizeDraw(int passindex, FSBufferManager buffermanager, VLEfficientArray<VLVProcessor> processors, VLEfficientArray<FSMesh> meshes, VLEfficientArray<FSTexture> textures, VLEfficientArray<FSProgram> programs){
//        FSRenderer.depthMask(false);
//        FSRenderer.enable(FSGLES.GL_STENCIL_TEST);
//        FSRenderer.stencilOp(FSGLES.GL_KEEP, FSGLES.GL_KEEP, FSGLES.GL_REPLACE);
//        FSRenderer.stencilFunc(FSGLES.GL_ALWAYS, stencilvalue, 0xFF);
//        FSRenderer.stencilMask(0xFF);
//
//        drawScroller();
//
//        FSRenderer.depthMask(true);
//        FSRenderer.stencilOp(FSGLES.GL_EQUAL, stencilvalue, 0xFF);
//        FSRenderer.stencilMask(0xFF);
//
//        int changes = 0;
//
//        target.update(FSRenderer.CURRENT_RENDER_PASS_INDEX);
//        target.draw(FSRenderer.CURRENT_RENDER_PASS_INDEX);
//        changes += target.process();
//
//        FSRenderer.addExternalChangesForFrame(changes);
//        FSRenderer.disable(FSGLES.GL_STENCIL_TEST);
//    }
//
//    private void initializeAnimator(){
//        VLEfficientArray<FSMesh> meshes = target.MESHES;
//
//        for(int i = 0; i < meshes.size(); i++){
//            VLEfficientArrayI subset = setindices.get(i);
//
//            if(subset == null){
//                continue;
//            }
//            FSMesh mesh = meshes.get(i);
//
//            for(int i2 = 0; i2 < mesh.instances.size(); i2++){
//                int setindex = subset.get(i2);
//
//                if(setindex < 0){
//                    continue;
//                }
//                FSInstance instance = mesh.instances.get(i2);
//                FSSchematics b = instance.schematics;
//                FSModelSetArray model = instance.modelSet();
//
//                switch(scrollaxis){
//                    case AXIS_X:
//                        model.addTranslation(setindex, getNewX(b, i, i2, interpolator), VLVConstant.ZERO, VLVConstant.ZERO);
//                        break;
//
//                    case AXIS_Y:
//                        model.addTranslation(setindex, VLVConstant.ZERO, getNewY(b, i, i2, interpolator), VLVConstant.ZERO);
//                        break;
//
//                    case AXIS_Z:
//                        model.addTranslation(setindex, VLVConstant.ZERO, VLVConstant.ZERO, getNewZ(b, i, i2, interpolator));
//                        break;
//
//                    case AXIS_XY:
//                        model.addTranslation(setindex, getNewX(b, i, i2, interpolator), getNewY(b, i, i2, interpolator), VLVConstant.ZERO);
//                        break;
//
//                    case AXIS_XZ:
//                        model.addTranslation(setindex, getNewX(b, i, i2, interpolator), VLVConstant.ZERO, getNewZ(b, i, i2, interpolator));
//                        break;
//
//                    case AXIS_XYZ:
//                        model.addTranslation(setindex, getNewX(b, i, i2, interpolator), getNewY(b, i, i2, interpolator), getNewZ(b, i, i2, interpolator));
//                        break;
//
//                    default:
//                        throw new RuntimeException("Invalud scroll axis : " + scrollaxis);
//                }
//
//                animator.addSetArray(model, setindex, 0);
//            }
//        }
//    }
//
//    public void updateRanges(){
//        if(animator != null){
//            animator.clear();
//
//            VLEfficientArray<FSMesh> meshes = target.MESHES;
//
//            for(int i = 0; i < meshes.size(); i++){
//                VLEfficientArrayI subset = setindices.get(i);
//
//                if(subset == null){
//                    continue;
//                }
//                FSMesh mesh = meshes.get(i);
//
//                for(int i2 = 0; i2 < mesh.instances.size(); i2++){
//                    int setindex = subset.get(i2);
//
//                    if(setindex < 0){
//                        continue;
//                    }
//                    FSInstance instance = mesh.instances.get(i2);
//                    FSSchematics b = instance.schematics;
//                    FSModelSetArray model = instance.modelSet();
//
//                    switch(scrollaxis){
//                        case AXIS_X:
//                            model.setColumn(setindex, 0, 2, getNewY(b, i, i2, interpolator));
//                            break;
//
//                        case AXIS_Y:
//                            model.setColumn(setindex, 0, 2, getNewY(b, i, i2, interpolator));
//                            break;
//
//                        case AXIS_Z:
//                            model.setColumn(setindex, 0, 3, getNewZ(b, i, i2, interpolator));
//                            break;
//
//                        case AXIS_XY:
//                            model.setColumn(setindex, 0, 1, getNewX(b, i, i2, interpolator));
//                            model.setColumn(setindex, 0, 2, getNewY(b, i, i2, interpolator));
//                            break;
//
//                        case AXIS_XZ:
//                            model.setColumn(setindex, 0, 1, getNewX(b, i, i2, interpolator));
//                            model.setColumn(setindex, 0, 3, getNewZ(b, i, i2, interpolator));
//                            break;
//
//                        case AXIS_XYZ:
//                            model.setColumn(setindex, 0, 1, getNewX(b, i, i2, interpolator));
//                            model.setColumn(setindex, 0, 2, getNewY(b, i, i2, interpolator));
//                            model.setColumn(setindex, 0, 3, getNewZ(b, i, i2, interpolator));
//                            break;
//
//                        default:
//                            throw new RuntimeException("Invalud scroll axis : " + scrollaxis);
//                    }
//
//                    animator.addSetArray(model, setindex, 0);
//                }
//            }
//        }
//    }
//
//    private VLVRanged getNewX(FSSchematics sb, int meshindex, int instanceindex, VLV.Interpolator interpolator){
//        FSSchematics b = target.mesh(meshindex).instances.get(instanceindex).schematics();
//        VLVRanged v = new VLVRanged(0, sb.modelLeft() - b.modelRight(), sb.modelRight() - b.modelLeft(), interpolator,
//                true, getNewRangedListener(meshindex, instanceindex));
//
//        return v;
//    }
//
//    private VLVRanged getNewY(FSSchematics sb, int meshindex, int instanceindex, VLV.Interpolator interpolator){
//        FSSchematics b = target.mesh(meshindex).instances.get(instanceindex).schematics();
//        VLVRanged v = new VLVRanged(0, sb.modelBottom() - b.modelTop(), sb.modelTop() - b.modelBottom(), interpolator,
//                true, getNewRangedListener(meshindex, instanceindex));
//
//        return v;
//    }
//
//    private VLVRanged getNewZ(FSSchematics sb, int meshindex, int instanceindex, VLV.Interpolator interpolator){
//        FSSchematics b = target.mesh(meshindex).instances.get(instanceindex).schematics();
//        VLVRanged v = new VLVRanged(0, sb.modelBack() - b.modelFront(), sb.modelFront() - b.modelBack(), interpolator,
//                true, getNewRangedListener(meshindex, instanceindex));
//
//        return v;
//    }
//
//    private VLVRanged.Listener getNewRangedListener(final int meshindex, final int instanceindex){
//        return new VLVRanged.Listener(target){
//
//            @Override
//            public void crossed(VLVRanged v){
//                if(listener != null){
//                    listener.constructorCrossed(FSScroller.this, v, target, meshindex, instanceindex);
//                }
//            }
//        };
//    }
//
//    public void scroll(long sleeptime, int directionalcycles, float amountX, float amountY, float amountZ){
//        scroll(directionalcycles, amountX, amountY, amountZ);
//        animator.nextTimedSynced(sleeptime);
//    }
//
//    public void scroll(int directionalcycles, float amountX, float amountY, float amountZ){
//        if(amountX != 0){
//            animator.push(amountX, directionalcycles);
//        }
//        if(amountY != 0){
//            animator.push(amountY, directionalcycles);
//        }
//        if(amountZ != 0){
//            animator.push(amountZ, directionalcycles);
//        }
//
//        animator.start();
//
//        if(listener != null){
//            listener.scrollStarted(this);
//        }
//    }
//
//    public void stopScroll(){
//        animator.pause();
//
//        if(listener != null){
//            listener.scrollFinished(this);
//        }
//    }
//
//    public void setListener(Listener l){
//        listener = l;
//    }
//
//    public void set(FSConstructor target, VLEfficientArray<VLEfficientArrayI> setindex){
//        this.target = target;
//        setindices = setindex;
//
//        initializeAnimator();
//    }
//
//    public void set(FSConstructor target, int setindex){
//        this.target = target;
//        setindices.clear(target.meshesSize());
//
//        for(int i = 0; i < target.meshesSize(); i++){
//            FSMesh m = target.mesh(i);
//            VLEfficientArrayI subset = new VLEfficientArrayI(m.size(), 10);
//
//            for(int i2 = 0; i2 < m.size(); i2++){
//                subset.add(setindex);
//            }
//
//            setindices.add(subset);
//        }
//
//        initializeAnimator();
//    }
//
//    public VLVProcessor getAnimation(){
//        return animator;
//    }
//
//
//    public static class Listener{
//
//        public void scrollStarted(FSScroller s){}
//        public void constructorCrossed(FSScroller s, VLVRanged v, FSConstructor constructor, int mesh, int instance){}
//        public void scrollFinished(FSScroller s){}
//    }
//}