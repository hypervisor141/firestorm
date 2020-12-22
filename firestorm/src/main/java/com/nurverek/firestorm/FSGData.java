package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLListType;

public final class FSGData{

    protected VLListType<Data> packs;

    public FSGData(VLListType<Data> packs){
        this.packs = packs;
    }

    protected Data get(int index){
        return packs.get(index);
    }

    public static final class Data{

        protected VLArrayFloat replacementcolor;
        protected FSTexture colortexture;
        protected FSLightMaterial material;
        protected FSLightMap map;

        public Data(VLArrayFloat replacementcolor, FSTexture colortexture, FSLightMaterial material, FSLightMap map){
            this.replacementcolor = replacementcolor;
            this.colortexture = colortexture;
            this.material = material;
            this.map = map;
        }
    }
}
