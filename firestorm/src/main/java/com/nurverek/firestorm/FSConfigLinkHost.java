package com.nurverek.firestorm;

import com.nurverek.vanguard.VLListType;

public class FSConfigLinkHost<LINK extends FSConfigLink> extends FSConfigDynamic{

    private final VLListType<LINK> links;

    public FSConfigLinkHost(int glslsize, int capacity, int resizer){
        super(glslsize);

        links = new VLListType<>(capacity, resizer);
    }

    public VLListType<LINK> links(){
        return links;
    }

    public void activate(int index){
        config(links.get(index));
    }

    public int size(){
        return links.size();
    }
}
