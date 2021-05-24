package com.nurverek.firestorm;

import vanguard.VLCopyable;

public interface FSRenderableType<TYPE> extends VLCopyable<TYPE>, FSAutomator.Registrable{

    void scanComplete();
    void bufferComplete();
    void buildComplete();
    void allocateElement(int element, int capacity, int resizer);
    void storeElement(int element, FSElement<?, ?> data);
    void activateFirstElement(int element);
    void activateLastElement(int element);
    void material(FSLightMaterial material);
    void lightMap(FSLightMap map);
    void colorTexture(FSTexture tex);
    void updateSchematicBoundaries();
    void markSchematicsForUpdate();
    void applyModelMatrix();
    void updateBuffer(int element);
    void destroy();

    String name();
    long id();
}
