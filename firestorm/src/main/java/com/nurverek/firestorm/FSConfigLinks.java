package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public class FSConfigLinks<LINK extends FSLink> extends FSConfigDynamic{

    private final VLListType<LINK> links;

    public FSConfigLinks(int glslsize, int capacity, int resizer){
        super(glslsize);

        links = new VLListType<>(capacity, resizer);
    }

    public VLListType<LINK> links(){
        return links;
    }

    public void activate(int index){
        config(links.get(index).config);
    }

    public int size(){
        return links.size();
    }
}
