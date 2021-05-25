package com.firestorm.mesh;

import vanguard.list.VLListType;
import vanguard.variable.VLV;
import vanguard.variable.VLVMatrix;
import vanguard.variable.VLVTypeVariable;

public class FSMatrixModel extends VLVMatrix{

    public FSMatrixModel(int initialcapacity, int resizercount){
        super(initialcapacity, resizercount);
    }

    public FSMatrixModel(FSMatrixModel src, long flags){
        super(src, flags);
    }

    protected FSMatrixModel(){

    }

    public void addRowTranslation(VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSArrayModel.FLAG_TRANSLATE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(row);
    }

    public void addRowTranslation(int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSArrayModel.FLAG_TRANSLATE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(rowindex, row);
    }

    public void addRowScale(VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSArrayModel.FLAG_SCALE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(row);
    }

    public void addRowScale(int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSArrayModel.FLAG_SCALE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(rowindex, row);
    }

    public void addRowRotate(VLV a, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(5, 0);

        row.add(FSArrayModel.FLAG_ROTATE);
        row.add(x);
        row.add(y);
        row.add(z);
        row.add(a);

        matrix.add(row);
    }

    public void addRowRotate(int rowindex, VLV a, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(5, 0);

        row.add(FSArrayModel.FLAG_ROTATE);
        row.add(x);
        row.add(y);
        row.add(z);
        row.add(a);

        matrix.add(rowindex, row);
    }

    public void setTranslateType(int rowindex){
        matrix.get(rowindex).set(0, FSArrayModel.FLAG_TRANSLATE);
    }

    public void setScaleType(int rowindex){
        matrix.get(rowindex).set(0, FSArrayModel.FLAG_SCALE);
    }

    public void setRotateType(int rowindex){
        matrix.get(rowindex).set(0, FSArrayModel.FLAG_ROTATE);
    }

    public void setX(int rowindex, VLV x){
        matrix.get(rowindex).set(1, x);
    }

    public void setY(int rowindex, VLV y){
        matrix.get(rowindex).set(2, y);
    }

    public void setZ(int rowindex, VLV z){
        matrix.get(rowindex).set(3, z);
    }

    public void setAngle(int rowindex, VLV a){
        matrix.get(rowindex).set(4, a);
    }

    public VLVTypeVariable getX(int rowindex){
        return matrix.get(rowindex).get(1);
    }

    public VLVTypeVariable getY(int rowindex){
        return matrix.get(rowindex).get(2);
    }

    public VLVTypeVariable getZ(int rowindex){
        return matrix.get(rowindex).get(3);
    }

    public VLVTypeVariable getAngle(int rowindex){
        return matrix.get(rowindex).get(4);
    }

    public int getTransformType(int rowindex){
        return (int) matrix.get(rowindex).get(0).get();
    }

    @Override
    public FSMatrixModel duplicate(long flags){
        return new FSMatrixModel(this, flags);
    }
}
