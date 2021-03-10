package com.nurverek.firestorm;

public final class FSVertexArray{

    private int id = 0;

    public FSVertexArray(int id){
        this.id = id;
    }

    public FSVertexArray(){

    }

    public void create(){
        id = FSR.createVertexArrays(1)[0];
    }

    public void bind(){
        FSR.vertexArrayBind(id);
    }

    public void unbind(){
        FSR.vertexArrayBind(0);
    }

    public void setID(int s){
        id = s;
    }

    public int getArrayID(){
        return id;
    }

    public void destroy(){
        id = -1;
    }
}
