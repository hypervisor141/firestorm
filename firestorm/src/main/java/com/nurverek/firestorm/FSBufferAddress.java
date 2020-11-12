package com.nurverek.firestorm;

import com.nurverek.vanguard.VLStringify;

public final class FSBufferAddress implements VLStringify {

    protected FSBufferManager manager;

    protected int bufferindex;
    protected int offset;
    protected int unitoffset;
    protected int unitsize;
    protected int stride;
    protected int count;
    protected int gldatatype;
    protected int databytesize;

    public FSBufferAddress(FSBufferManager manager, int bufferindex, int offset, int unitoffset, int unitsize, int stride, int count, int gldatatype, int databytesize){
        this.manager = manager;
        this.bufferindex = bufferindex;
        this.offset = offset;
        this.unitoffset = unitoffset;
        this.unitsize = unitsize;
        this.stride = stride;
        this.count = count;
        this.gldatatype = gldatatype;
        this.databytesize = databytesize;
    }


    public FSBufferManager manager(){
        return manager;
    }

    public FSVertexBuffer target(){
        return manager.get(bufferindex);
    }

    public int bufferIndex(){
        return bufferindex;
    }

    public int glDataType(){
        return gldatatype;
    }

    public int unitSize(){
        return unitsize;
    }

    public int unitOffset(){
        return unitoffset;
    }

    public int count(){
        return count;
    }

    public int offset(){
        return offset;
    }

    public int stride(){return stride;}

    public int unitSizeBytes(){
        return unitsize * databytesize;
    }

    public int unitOffsetBytes(){
        return unitoffset * databytesize;
    }

    public int countBytes(){
        return count * databytesize;
    }

    public int offsetBytes(){
        return offset * databytesize;
    }

    public int strideBytes(){return stride * databytesize;}

    public int dataByteSize(){return databytesize;}

    @Override
    public void stringify(StringBuilder src, Object hint){
        src.append("[BufferAddress] offset[");
        src.append(offset);
        src.append("] unitsize[");
        src.append(unitsize);
        src.append("] unitoffset[");
        src.append(unitoffset);
        src.append("] stride[");
        src.append(stride);
        src.append("] count[");
        src.append(count);
        src.append("] gldatatype[");
        src.append(gldatatype);
        src.append("] databytesize[");
        src.append(databytesize);
        src.append("] content[ ");

        target().stringify(src, hint);

        src.append(" ]");
    }
}
