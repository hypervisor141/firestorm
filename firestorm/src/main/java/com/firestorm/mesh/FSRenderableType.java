package com.firestorm.mesh;

import com.firestorm.program.FSConfig;
import com.firestorm.program.FSConfigType;
import com.firestorm.program.FSLightMap;
import com.firestorm.program.FSLightMaterial;
import com.firestorm.program.FSTexture;

import vanguard.utils.VLCopyable;

public interface FSRenderableType extends VLCopyable<FSRenderableType>, FSConfigType{

    void scanComplete();
    void buildComplete();
    void allocateElement(int element, int capacity, int resizer);
    void storeElement(int element, FSElement<?, ?> data);
    void activateFirstElement(int element);
    void activateLastElement(int element);
    void parent(FSRenderableType parent);
    void material(FSLightMaterial material);
    void lightMap(FSLightMap map);
    void colorTexture(FSTexture tex);
    void updateSchematicBoundaries();
    void markSchematicsForUpdate();
    void applyModelMatrix();
    void updateBuffer(int element);
    void destroy();

    FSRenderableType parent();
    FSRenderableType parentRoot();
    FSConfig configs();
    String name();
    long id();
}
