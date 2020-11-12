package com.nurverek.firestorm;

public final class FSVertexArray {

    private int id = 0;

    public FSVertexArray(int id){
        this.id = id;
    }

    public FSVertexArray(){

    }


    public void create(){
        id = FSRenderer.createVertexArrays(1)[0];
    }

    public void bind(){
        FSRenderer.vertexArrayBind(id);
    }

    public void unbind(){
        FSRenderer.vertexArrayBind(0);
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
