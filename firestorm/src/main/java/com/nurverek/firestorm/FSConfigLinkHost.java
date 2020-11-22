package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public class FSConfigLinkHost<LINK extends FSConfigLink> extends FSConfigDynamic{

    private final VLListType<LINK> links;

    public FSConfigLinkHost(int glslsize, int capacity, int resizer){
        super(glslsize);

        links = new VLListType<>(capacity, resizer);
    }

    public void activate(int index){
        config(links.get(index));
    }

    public void add(LINK link){
        link.host = this;
        link.indexonhost = size();

        links.add(link);
    }

    public LINK get(int index){
        return links.get(index);
    }

    public LINK remove(int index){
        return links.remove(index);
    }

    public int size(){
        return links.size();
    }
}
