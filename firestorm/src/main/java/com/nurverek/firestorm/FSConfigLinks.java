package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public class FSConfigLinks extends FSConfigDynamic{

    private final VLListType<FSLink> links;

    public FSConfigLinks(int size, int resizer){
        links = new VLListType<>(size, resizer);
    }

    public VLListType<FSLink> links(){
        return links;
    }

    public void activate(int index){
        config(links.get(index).config);
    }

    public int size(){
        return links.size();
    }
}
