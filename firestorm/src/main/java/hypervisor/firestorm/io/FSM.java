package hypervisor.firestorm.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import hypervisor.vanguard.list.VLListFloat;
import hypervisor.vanguard.list.VLListShort;
import hypervisor.vanguard.list.VLListType;

public class FSM{

    private FSM(){

    }

    public static void loadFromFile(InputStream is, ByteOrder order, boolean fullsizedvertex, DataOperator operator) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(8);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] rawbuffer = new byte[1000];

        buffer.order(order);
        buffer.position(0);

        int size = readInt(bis, buffer);

        for(int i = 0; i < size; i++){
            int namesize = readInt(bis, buffer);
            int positionsize = readInt(bis, buffer);
            int colorsize = readInt(bis, buffer);
            int texcoordssize = readInt(bis, buffer);
            int normalsize = readInt(bis, buffer);
            int indexsize = readInt(bis, buffer);

            bis.read(rawbuffer, 0, namesize);

            Data data = new Data(positionsize, colorsize, texcoordssize, normalsize, indexsize);
            data.name = new String(rawbuffer, 0, namesize, StandardCharsets.UTF_8);
            data.positions.resize(positionsize + (fullsizedvertex ? (positionsize / 3) : 0));

            for(int i2 = 0, counter = 0; i2 < positionsize; i2++){
                data.positions.add(readFloat(bis, buffer));
                counter++;

                if(fullsizedvertex && counter == 3){
                    data.positions.add(1.0F);
                    counter = 0;
                }
            }

            data.colors.resize(colorsize);
            for(int i2 = 0; i2 < colorsize; i2++){
                data.colors.add(readFloat(bis, buffer));
            }

            data.texcoords.resize(texcoordssize);
            for(int i2 = 0; i2 < texcoordssize; i2++){
                data.texcoords.add(readFloat(bis, buffer));
            }

            data.normals.resize(normalsize);
            for(int i2 = 0; i2 < normalsize; i2++){
                data.normals.add(readFloat(bis, buffer));
            }

            data.indices.resize(indexsize);
            for(int i2 = 0; i2 < indexsize; i2++){
                data.indices.add(readShort(bis, buffer));
            }

            data.clean();
            operator.operate(data);
        }

        bis.close();
    }

    private static int readInt(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 4);
        buffer.position(0);

        return buffer.getInt();
    }

    private static float readFloat(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 4);
        buffer.position(0);

        return buffer.getFloat();
    }

    private static short readShort(BufferedInputStream is, ByteBuffer buffer) throws IOException{
        is.read(buffer.array(), 0, 2);
        buffer.position(0);

        return buffer.getShort();
    }

    public static final class Data{

        public String name;
        public boolean locked = false;

        public VLListFloat positions;
        public VLListFloat colors;
        public VLListFloat texcoords;
        public VLListFloat normals;
        public VLListShort indices;

        public Data(int positionsize, int colorsize, int texcoordsize, int normalsize, int indexsize){
            name = "";
            locked = false;

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

    public interface DataOperator{

        void operate(Data data);
    }
}
