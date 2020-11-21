package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public class FSConfigLinks<LINK extends FSLink> extends FSConfigDynamic{

    private final VLListType<LINK> links;

    public FSConfigLinks(int size, int resizer){
        links = new VLListType<>(size, resizer);
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
