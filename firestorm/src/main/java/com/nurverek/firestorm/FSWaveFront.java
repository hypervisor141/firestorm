package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListFloat;
import com.nurverek.vanguard.VLListShort;
import com.nurverek.vanguard.VLListType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class FSWaveFront{

    public VLListType<Data> data;

    private int dataresizer;
    private int pdivider;

    private int positioncount;
    private int uvcount;
    private int normalcount;

    public FSWaveFront(int objcapacity, int subdataresizer){
        data = new VLListType<>(objcapacity, objcapacity);
        this.dataresizer = subdataresizer;
    }


    public void load(InputStream input, boolean fullsizedposition) throws IOException{
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(isr, 64000);
        String line;
        String type;
        Data current = null;
        int[] v;

        pdivider = fullsizedposition ? 4 : 3;

        while(((line = reader.readLine()) != null)){
            line = line.trim();
            type = line.substring(0, line.indexOf(" "));
            line = line.substring(line.indexOf(" ") + 1);

            if(type.equals("o")){
                if(current != null){
                    current.resolve(fullsizedposition);
                }

                current = new Data(dataresizer);
                data.add(current);

                readNameLine(current, line);

            }else if(type.equals("v")){
                readPositionLine(current, line, fullsizedposition);

            }else if(type.equals("vt")){
                readUVLine(current, line);

            }else if(type.equals("vn")){
                readNormalLine(current, line);

            }else if(type.equals("f")){
                readFaceLine(current, line);
            }
        }

        current.resolve(fullsizedposition);

        reader.close();
        isr.close();
        input.close();
    }

    private void readNameLine(Data o, String line){
        o.name = line.trim().toLowerCase();
    }

    private void readPositionLine(Data o, String line, boolean fullsizedposition){
        int index = line.indexOf(" ");
        int secondindex = line.lastIndexOf(" ");

        o.positions.add(Float.valueOf(line.substring(0, index)));
        o.positions.add(Float.valueOf(line.substring(index + 1, secondindex)));
        o.positions.add(Float.valueOf(line.substring(secondindex + 1)));

        if(fullsizedposition){
            o.positions.add(1F);
        }

        positioncount++;
    }

    private void readUVLine(Data o, String line){
        int index = line.indexOf(" ");
        int secondindex = line.lastIndexOf(" ");

        o.texcoords.add(Float.valueOf(line.substring(0, index)));
        o.texcoords.add(1 - Float.valueOf(line.substring(index + 1)));

        uvcount++;
    }

    private void readNormalLine(Data o, String line){
        int index = line.indexOf(" ");
        int secondindex = line.lastIndexOf(" ");

        o.normals.add(Float.valueOf(line.substring(0, index)));
        o.normals.add(Float.valueOf(line.substring(index + 1, secondindex)));
        o.normals.add(Float.valueOf(line.substring(secondindex + 1)));

        normalcount++;
    }

    private void readFaceLine(Data o, String line){
        int separator1 = line.indexOf("/");
        int separator2  = line.indexOf("/", separator1 + 1);
        int start = 0;
        int space = line.indexOf(" ", separator2);
        VertexData data;

        for(int i = 0; i < 3; i++){
            data = new VertexData(
                    Integer.valueOf(line.substring(start, separator1)),
                    o.texcoords.size() == 0 ? -1 : Integer.valueOf(line.substring(separator1 + 1, separator2)),
                    o.normals.size() == 0 ? -1 : Integer.valueOf(line.substring(separator2 + 1, space)));
            
            separator1 = line.indexOf("/", space);
            separator2  = line.indexOf("/", separator1 + 1);
            start = space + 1;
            space = line.indexOf(" ", separator2);

            if(space == -1){
                space = line.length();
            }

            o.faces.add(data);
        }
    }

    public void release(){
        data.clear();
        data.resize(10);
    }

    public final class Data{

        public String name;

        public VLListFloat positions;
        public VLListFloat texcoords;
        public VLListFloat normals;
        public VLListShort indices;
        public VLListType<VertexData> faces;

        protected Data(int resizer){
            positions = new VLListFloat(10, resizer);
            texcoords = new VLListFloat(10, resizer);
            normals = new VLListFloat(10, resizer);
            indices = new VLListShort(10, resizer);
            faces = new VLListType<>(10, resizer);
        }

        protected void resolve(boolean fullsizedposition){
            VLListFloat p = new VLListFloat(positions.size(), 1);
            VLListFloat t = new VLListFloat(texcoords.size(), 1);
            VLListFloat n = new VLListFloat(normals.size(), 1);
            VLListShort i = new VLListShort(positions.size() / pdivider, 1);

            VLListType<VertexData> tracker = new VLListType<>(faces.size(), faces.size() / 4);

            int poffset = (positioncount * pdivider - positions.size()) / pdivider + 1;
            int uoffset = (uvcount * 2 - texcoords.size()) / 2 + 1;
            int noffset = (normalcount * 3 - normals.size()) / 3 + 1;

            for(int index = 0; index < faces.size(); index++){
                VertexData f1 = faces.get(index);
                int found = -1;

                for(int index2 = 0; index2 < tracker.size(); index2++){
                    VertexData f2 = tracker.get(index2);

                    if(f1.positionindex == f2.positionindex && f1.uvindex == f2.uvindex && f1.normalindex == f2.normalindex){
                        found = index2;
                        break;
                    }
                }

                if(found >= 0){
                    i.add((short)(found));

                }else{
                    int positionindex = (f1.positionindex - poffset) * pdivider;
                    int uvindex = (f1.uvindex - uoffset) * 2;
                    int normalindex = (f1.normalindex - noffset) * 3;

                    p.add(positions.get(positionindex));
                    p.add(positions.get(positionindex + 1));
                    p.add(positions.get(positionindex + 2));

                    if(fullsizedposition){
                        p.add(positions.get(positionindex + 3));
                    }

                    t.add(texcoords.get(uvindex));
                    t.add(texcoords.get(uvindex + 1));

                    n.add(normals.get(normalindex));
                    n.add(normals.get(normalindex + 1));
                    n.add(normals.get(normalindex + 2));

                    i.add((short)(p.size() / pdivider - 1));
                    tracker.add(f1);
                }
            }

            positions = p;
            texcoords = t;
            normals = n;
            indices = i;

            positions.restrictSize();
            texcoords.restrictSize();
            normals.restrictSize();
            indices.restrictSize();
        }
    }

    public static final class VertexData{

        public int positionindex;
        public int uvindex;
        public int normalindex;

        protected VertexData(int positionindex, int uvindex, int normalindex){
            this.positionindex = positionindex;
            this.uvindex = uvindex;
            this.normalindex = normalindex;
        }
    }
}
