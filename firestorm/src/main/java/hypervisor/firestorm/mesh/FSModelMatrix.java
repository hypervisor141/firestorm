package hypervisor.firestorm.mesh;

import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.variable.VLV;
import hypervisor.vanguard.variable.VLVMatrix;
import hypervisor.vanguard.variable.VLVTypeVariable;

public class FSModelMatrix extends VLVMatrix{

    public FSModelMatrix(int capacity, int resizeoverhead){
        super(capacity, resizeoverhead);
    }

    public FSModelMatrix(FSModelMatrix src, long flags){
        super(src, flags);
    }

    protected FSModelMatrix(){

    }

    public void addRowTranslation(VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSModelArray.FLAG_TRANSLATE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(row);
    }

    public void addRowTranslation(int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSModelArray.FLAG_TRANSLATE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(rowindex, row);
    }

    public void addRowScale(VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSModelArray.FLAG_SCALE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(row);
    }

    public void addRowScale(int rowindex, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(4, 0);

        row.add(FSModelArray.FLAG_SCALE);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(rowindex, row);
    }

    public void addRowRotate(VLV a, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(5, 0);

        row.add(FSModelArray.FLAG_ROTATE);
        row.add(a);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(row);
    }

    public void addRowRotate(int rowindex, VLV a, VLV x, VLV y, VLV z){
        VLListType<VLVTypeVariable> row = new VLListType<>(5, 0);

        row.add(FSModelArray.FLAG_ROTATE);
        row.add(a);
        row.add(x);
        row.add(y);
        row.add(z);

        matrix.add(rowindex, row);
    }

    public VLVTypeVariable getTransformType(int rowindex){
        return matrix.get(rowindex).get(0);
    }

    @Override
    public FSModelMatrix duplicate(long flags){
        return new FSModelMatrix(this, flags);
    }
}
