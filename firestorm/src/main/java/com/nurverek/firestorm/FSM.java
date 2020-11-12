package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListFloat;
import com.nurverek.vanguard.VLListShort;
import com.nurverek.vanguard.VLListType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FSM{

    public VLListType<Data> data;


    public FSM(){

    }

    public void loadFromFile(InputStream is, ByteOrder order, boolean fullsizedvertex, int sizeestimate) throws IOException{
        data = new VLListType<>(sizeestimate, (int)Math.ceil(sizeestimate * 80f / 100f));

        ByteBuffer buffer = ByteBuffer.allocate(8);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] rawbuffer = new byte[1000];

        buffer.order(order);
        buffer.position(0);

        int size = readInt(bis, buffer);
        data.resize(size);

        for(int i = 0; i < size; i++){
            int namesize = readInt(bis, buffer);
            int positionsize = readInt(bis, buffer);
            int colorsize = readInt(bis, buffer);
            int texcoordssize = readInt(bis, buffer);
            int normalsize = readInt(bis, buffer);
            int indexsize = readInt(bis, buffer);

            Data d = new Data(positionsize, colorsize, texcoordssize, normalsize, indexsize);

            bis.read(rawbuffer, 0, namesize);
            d.name = new String(rawbuffer, 0, namesize, "UTF-8");

            d.positions.resize(positionsize + (fullsizedvertex ? (positionsize / 3) : 0));
            for(int i2 = 0, counter = 0; i2 < positionsize; i2++){
                d.positions.add(readFloat(bis, buffer));
                counter++;

                if(fullsizedvertex && counter == 3){
                    d.positions.add(1.0F);
                    counter = 0;
                }
            }

            d.colors.resize(colorsize);
            for(int i2 = 0; i2 < colorsize; i2++){
                d.colors.add(readFloat(bis, buffer));
            }

            d.texcoords.resize(texcoordssize);
            for(int i2 = 0; i2 < texcoordssize; i2++){
                d.texcoords.add(readFloat(bis, buffer));
            }

            d.normals.resize(normalsize);
            for(int i2 = 0; i2 < normalsize; i2++){
                d.normals.add(readFloat(bis, buffer));
            }

            d.indices.resize(indexsize);
            for(int i2 = 0; i2 < indexsize; i2++){
                d.indices.add(readShort(bis, buffer));
            }

            d.clean();
            data.add(d);
        }

        bis.close();
        data.restrictSize();
    }

    private int readInt(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 4);
        buffer.position(0);

        return buffer.getInt();
    }

    private float readFloat(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 4);
        buffer.position(0);

        return buffer.getFloat();
    }

    private short readShort(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 2);
        buffer.position(0);

        return buffer.getShort();
    }

    public void release(){
        data = null;
    }

    public static final class Data{

        public String name;

        public VLListFloat positions;
        public VLListFloat colors;
        public VLListFloat texcoords;
        public VLListFloat normals;
        public VLListShort indices;

        public Data(int positionsize, int colorsize, int texcoordsize, int normalsize, int indexsize){
            positions = new VLListFloat(positionsize, 100);
            colors = new VLListFloat(colorsize, 100);
            texcoords = new VLListFloat(texcoordsize, 100);
            normals = new VLListFloat(normalsize, 100);
            indices = new VLListShort(indexsize, 100);
        }

        private void clean(){
            positions.restrictSize();
            colors.restrictSize();
            texcoords.restrictSize();
            normals.restrictSize();
            indices.restrictSize();
        }
    }
}
