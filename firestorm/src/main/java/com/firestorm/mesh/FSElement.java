package com.firestorm.mesh;

import com.firestorm.engine.FSElements;
import com.firestorm.program.FSVertexBuffer;

import java.util.Arrays;

import vanguard.array.VLArrayByte;
import vanguard.array.VLArrayDouble;
import vanguard.array.VLArrayFloat;
import vanguard.array.VLArrayInt;
import vanguard.array.VLArrayLong;
import vanguard.array.VLArrayShort;
import vanguard.buffer.VLBuffer;
import vanguard.buffer.VLBufferByte;
import vanguard.buffer.VLBufferDouble;
import vanguard.buffer.VLBufferFloat;
import vanguard.buffer.VLBufferInt;
import vanguard.buffer.VLBufferLong;
import vanguard.buffer.VLBufferShort;
import vanguard.buffer.VLBufferTracker;
import vanguard.list.VLListType;
import vanguard.primitive.VLByte;
import vanguard.primitive.VLDouble;
import vanguard.primitive.VLFloat;
import vanguard.primitive.VLInt;
import vanguard.primitive.VLLong;
import vanguard.primitive.VLShort;
import vanguard.utils.VLCopyable;
import vanguard.utils.VLLog;
import vanguard.utils.VLLoggable;

public abstract class FSElement<DATA extends VLCopyable<?>, BUFFER extends VLBuffer<?, ?>> implements VLCopyable<FSElement<DATA, BUFFER>>, VLLoggable{

    public int element;
    public DATA data;
    public VLListType<FSBufferBinding<BUFFER>> bindings;

    public FSElement(int element, DATA data){
        this.element = element;
        this.data = data;

        bindings = new VLListType<>(1, 5);
    }

    protected FSElement(int element){
        this.element = element;
    }

    protected FSElement(){

    }

    public FSElement(FSElement<DATA, BUFFER> src, long flags){
        copy(src, flags);
    }

    @Override
    public void copy(FSElement<DATA, BUFFER> src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            data = src.data;
            bindings = src.bindings;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            data = (DATA)src.data.duplicate(FLAG_DUPLICATE);
            bindings = src.bindings.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }

        element = src.element;
    }

    @Override
    public abstract FSElement<DATA, BUFFER> duplicate(long flags);
    public abstract int size();
    public abstract void updateBuffer(int bindingindex);

    public VLBufferTracker buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer){
        VLBufferTracker tracker = new VLBufferTracker();
        bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));

        tracker.unitoffset = 0;
        tracker.unitsize = FSElements.UNIT_SIZES[element];
        tracker.unitsubcount = tracker.unitsize;
        tracker.stride = tracker.unitsize;

        return tracker;
    }

    public VLBufferTracker buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
        VLBufferTracker tracker = new VLBufferTracker();
        bindings.add(new FSBufferBinding<>(vbuffer, buffer, tracker));

        return tracker;
    }

    public final VLBufferTracker buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int unitoffset, int unitsize, int unitsubcount, int stride){
        return buffer(vbuffer, buffer, 0, size(), unitoffset, unitsize, unitsubcount, stride);
    }

    public void updateBuffer(){
        int size = bindings.size();

        for(int i = 0; i < size; i++){
            updateBuffer(i);
        }
    }

    private static abstract class PrimitiveType<DATA extends VLCopyable<?>, BUFFER extends VLBuffer<?, ?>> extends FSElement<DATA, BUFFER>{

        public PrimitiveType(int element, DATA data){
            super(element, data);
        }

        public PrimitiveType(int element){
            super(element);
        }

        protected PrimitiveType(){}

        @Override
        public int size(){
            return 1;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            return buffer(vbuffer, buffer);
        }
    }

    public static class Byte extends PrimitiveType<VLByte, VLBufferByte>{

        public Byte(int element, VLByte data){
            super(element, data);
        }

        public Byte(int element){
            super(element);
        }

        public Byte(Byte src, long flags){
            copy(src, flags);
        }

        protected Byte(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferByte> vbuffer, VLBufferByte buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferByte> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Byte duplicate(long flags){
            return new Byte(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferByte> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class Short extends PrimitiveType<VLShort, VLBufferShort>{

        public Short(int element, VLShort data){
            super(element, data);
        }

        public Short(int element){
            super(element);
        }

        public Short(Short src, long flags){
            copy(src, flags);
        }

        protected Short(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferShort> vbuffer, VLBufferShort buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferShort> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Short duplicate(long flags){
            return new Short(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferShort> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class Int extends PrimitiveType<VLInt, VLBufferInt>{

        public Int(int element, VLInt data){
            super(element, data);
        }

        public Int(int element){
            super(element);
        }

        public Int(Int src, long flags){
            copy(src, flags);
        }

        protected Int(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferInt> vbuffer, VLBufferInt buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferInt> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Int duplicate(long flags){
            return new Int(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferInt> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class Long extends PrimitiveType<VLLong, VLBufferLong>{

        public Long(int element, VLLong data){
            super(element, data);
        }

        public Long(int element){
            super(element);
        }

        public Long(Long src, long flags){
            copy(src, flags);
        }

        protected Long(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferLong> vbuffer, VLBufferLong buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferLong> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Long duplicate(long flags){
            return new Long(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferLong> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class Float extends PrimitiveType<VLFloat, VLBufferFloat>{

        public Float(int element, VLFloat data){
            super(element, data);
        }

        public Float(int element){
            super(element);
        }

        public Float(Float src, long flags){
            copy(src, flags);
        }

        protected Float(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferFloat> vbuffer, VLBufferFloat buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferFloat> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Float duplicate(long flags){
            return new Float(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferFloat> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class Double extends PrimitiveType<VLDouble, VLBufferDouble>{

        public Double(int element, VLDouble data){
            super(element, data);
        }

        public Double(int element){
            super(element);
        }

        public Double(Double src, long flags){
            copy(src, flags);
        }

        protected Double(){}

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferDouble> vbuffer, VLBufferDouble buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.get());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferDouble> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.get());
        }

        @Override
        public Double duplicate(long flags){
            return new Double(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferDouble> binding = bindings.get(index);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data[");
            log.append(this.data.get());
            log.append("] bufferedData[");
            log.append(binding.buffer.read(binding.tracker.offset));
            log.append("]");
        }
    }

    public static class ByteArray extends FSElement<VLArrayByte, VLBufferByte>{

        public ByteArray(int element, VLArrayByte data){
            super(element, data);
        }

        public ByteArray(int element){
            super(element);
        }

        public ByteArray(ByteArray src, long flags){
            copy(src, flags);
        }

        protected ByteArray(){}

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferByte> vbuffer, VLBufferByte buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferByte> vbuffer, VLBufferByte buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferByte> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public ByteArray duplicate(long flags){
            return new ByteArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferByte> binding = bindings.get(index);

            byte[] results = new byte[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }

    public static class ShortArray extends FSElement<VLArrayShort, VLBufferShort>{

        public ShortArray(int element, VLArrayShort data){
            super(element, data);
        }

        public ShortArray(int element){
            super(element);
        }

        public ShortArray(ShortArray src, long flags){
            copy(src, flags);
        }

        protected ShortArray(){}

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferShort> vbuffer, VLBufferShort buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferShort> vbuffer, VLBufferShort buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferShort> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public ShortArray duplicate(long flags){
            return new ShortArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferShort> binding = bindings.get(index);

            short[] results = new short[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }

    public static class IntArray extends FSElement<VLArrayInt, VLBufferInt>{

        public IntArray(int element, VLArrayInt data){
            super(element, data);
        }

        public IntArray(int element){
            super(element);
        }

        public IntArray(IntArray src, long flags){
            copy(src, flags);
        }

        protected IntArray(){}

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferInt> vbuffer, VLBufferInt buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferInt> vbuffer, VLBufferInt buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferInt> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public IntArray duplicate(long flags){
            return new IntArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferInt> binding = bindings.get(index);

            int[] results = new int[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }

    public static class LongArray extends FSElement<VLArrayLong, VLBufferLong>{

        public LongArray(int element, VLArrayLong data){
            super(element, data);
        }

        public LongArray(int element){
            super(element);
        }

        public LongArray(LongArray src, long flags){
            copy(src, flags);
        }

        protected LongArray(){

        }

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferLong> vbuffer, VLBufferLong buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferLong> vbuffer, VLBufferLong buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferLong> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public LongArray duplicate(long flags){
            return new LongArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferLong> binding = bindings.get(index);

            long[] results = new long[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }

    public static class FloatArray extends FSElement<VLArrayFloat, VLBufferFloat>{

        public FloatArray(int element, VLArrayFloat data){
            super(element, data);
        }

        public FloatArray(int element){
            super(element);
        }

        public FloatArray(FloatArray src, long flags){
            super();
            copy(src, flags);
        }

        protected FloatArray(){
            
        }

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferFloat> vbuffer, VLBufferFloat buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferFloat> vbuffer, VLBufferFloat buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferFloat> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public FloatArray duplicate(long flags){
            return new FloatArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferFloat> binding = bindings.get(index);

            float[] results = new float[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }

    public static class DoubleArray extends FSElement<VLArrayDouble, VLBufferDouble>{

        public DoubleArray(int element, VLArrayDouble data){
            super(element, data);
        }

        public DoubleArray(int element){
            super(element);
        }

        public DoubleArray(DoubleArray src, long flags){
            copy(src, flags);
        }

        protected DoubleArray(){

        }

        @Override
        public int size(){
            return data.size();
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferDouble> vbuffer, VLBufferDouble buffer){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer);
            buffer.put(tracker, data.provider());

            return tracker;
        }

        @Override
        public VLBufferTracker buffer(FSVertexBuffer<VLBufferDouble> vbuffer, VLBufferDouble buffer, int arrayoffset, int arraysize, int unitoffset, int unitsize, int unitsubcount, int stride){
            VLBufferTracker tracker = super.buffer(vbuffer, buffer, arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);
            buffer.put(tracker, data.provider(), arrayoffset, arraysize, unitoffset, unitsize, unitsubcount, stride);

            return tracker;
        }

        @Override
        public void updateBuffer(int bindingindex){
            FSBufferBinding<VLBufferDouble> binding = bindings.get(bindingindex);
            binding.buffer.update(binding.tracker, data.provider());
        }

        @Override
        public DoubleArray duplicate(long flags){
            return new DoubleArray(this, flags);
        }

        @Override
        public void log(VLLog log, Object data){
            int index = data == null ? 0 : (int)data;

            FSBufferBinding<VLBufferDouble> binding = bindings.get(index);

            double[] results = new double[binding.tracker.count];
            binding.buffer.read(binding.tracker, results, 0);

            log.append("vBuffer[");
            binding.vbuffer.log(log, null);
            log.append("] tracker[");
            binding.tracker.log(log, null);
            log.append("] data");
            log.append(Arrays.toString(this.data.provider()));
            log.append(" bufferedData");
            log.append(Arrays.toString(results));
        }
    }
}
