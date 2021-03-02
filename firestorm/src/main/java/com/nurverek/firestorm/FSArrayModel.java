package com.nurverek.firestorm;

import android.opengl.Matrix;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLV;
import com.nurverek.vanguard.VLVMatrix;
import com.nurverek.vanguard.VLVTypeVariable;

public class FSArrayModel extends VLArrayFloat{

    private static final int TRANSLATE = 8941;
    private static final int ROTATE = 8942;
    private static final int SCALE = 8943;

    public static final VLV TRANSLATE_FLAG = new VLV(TRANSLATE);
    public static final VLV ROTATE_FLAG = new VLV(ROTATE);
    public static final VLV SCALE_FLAG = new VLV(SCALE);

    public FSArrayModel(float[] s){
        super(s);
    }

    public FSArrayModel(){
        super(new float[FSG.UNIT_SIZE_MODEL]);
        identity();
    }


    @Override
    public void transform(int index, VLVMatrix matrix, boolean replace){
        if(replace){
            identity();
        }

        VLListType<VLVTypeVariable> row;

        for(int i = matrix.sizeRows() - 1; i >= 0; i--){
            row = matrix.getRow(i);

            if(row != null){
                int flag = (int)row.get(0).get();

                switch(flag){
                    case TRANSLATE:
                        translate(row.get(1).get(), row.get(2).get(), row.get(3).get());
                        break;

                    case ROTATE:
                        rotate(row.get(1).get(), row.get(2).get(), row.get(3).get(), row.get(4).get());
                        break;

                    case SCALE:
                        scale(row.get(1).get(), row.get(2).get(), row.get(3).get());
                        break;

                    default:
                        throw new RuntimeException("Invalid model transform flag[" + flag + "]");
                }
            }
        }
    }

    public void identity(){
        Matrix.setIdentityM(array, 0);
    }

    public void scale(float x, float y, float z){
        Matrix.scaleM(array, 0, x, y ,z);
    }

    public void translate(float x, float y, float z){
        Matrix.translateM(array, 0, x, y ,z);
    }

    public void rotate(float x, float y, float z, float a){
        Matrix.rotateM(array, 0, a, x, y, z);
    }

    public void multiply(float[] mat){
        Matrix.multiplyMM(array, 0, array,0, mat, 0);
    }

    public void transformPoint(float[] results, int offset, float[] point, int offset2){
        point[offset2 + 3] = 1;
        Matrix.multiplyMV(results, offset, array, 0, point, offset2);

        float w = results[offset + 3];
        results[offset] /= w;
        results[offset + 1] /= w;
        results[offset + 2] /= w;
    }
}
