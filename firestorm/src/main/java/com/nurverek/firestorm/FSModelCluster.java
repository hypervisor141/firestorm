package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLV;
import com.nurverek.vanguard.VLVCluster;

public class FSModelCluster extends VLVCluster {

    public FSModelCluster(int initialcapacity, int resizercount){
        super(initialcapacity, resizercount);
    }


    public void addRowTranslation(int setindex, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.TRANSLATE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);

        cluster.get(setindex).add(row);
    }

    public void addRowTranslation(int setindex, int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.TRANSLATE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);

        cluster.get(setindex).add(rowindex, row);
    }

    public void addRowScale(int setindex, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.SCALE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);

        cluster.get(setindex).add(row);
    }

    public void addRowScale(int setindex, int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.SCALE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);

        cluster.get(setindex).add(rowindex, row);
    }

    public void addRowRotate(int setindex, VLV a, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.ROTATE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);
        row.add(a);

        cluster.get(setindex).add(row);
    }

    public void addRowRotate(int setindex, int rowindex, VLV a, VLV x, VLV y, VLV z){
        VLListType<VLV> row = new VLListType<>(4, 10);

        row.add(FSModelArray.ROTATE_FLAG);
        row.add(x);
        row.add(y);
        row.add(z);
        row.add(a);

        cluster.get(setindex).add(rowindex, row);
    }

    public void setTranslateType(int setindex, int rowindex){
        cluster.get(setindex).get(rowindex).set(0, FSModelArray.TRANSLATE_FLAG);
    }

    public void setScaleType(int setindex, int rowindex){
        cluster.get(setindex).get(rowindex).set(0, FSModelArray.SCALE_FLAG);
    }

    public void setRotateType(int setindex, int rowindex){
        cluster.get(setindex).get(rowindex).set(0, FSModelArray.ROTATE_FLAG);
    }

    public void setX(int setindex, int rowindex, VLV x){
        cluster.get(setindex).get(rowindex).set(1, x);
    }

    public void setY(int setindex, int rowindex, VLV y){
        cluster.get(setindex).get(rowindex).set(2, y);
    }

    public void setZ(int setindex, int rowindex, VLV z){
        cluster.get(setindex).get(rowindex).set(3, z);
    }

    public void setAngle(int setindex, int rowindex, VLV a){
        cluster.get(setindex).get(rowindex).set(4, a);
    }

    public VLV getX(int setindex, int rowindex){
        return cluster.get(setindex).get(rowindex).get(1);
    }

    public VLV getY(int setindex, int rowindex){
        return cluster.get(setindex).get(rowindex).get(2);
    }

    public VLV getZ(int setindex, int rowindex){
        return cluster.get(setindex).get(rowindex).get(3);
    }

    public VLV getAngle(int setindex, int rowindex){
        return cluster.get(setindex).get(rowindex).get(4);
    }

    public int getTransformType(int setindex, int rowindex){
        return (int) cluster.get(setindex).get(rowindex).get(0).get();
    }
}
