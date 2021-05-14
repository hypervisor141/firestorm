package com.nurverek.firestorm;

public interface FSMeshType{

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
    void updateVertexBuffer(int element);
    void updateVertexBufferStrict(int element);
    void updateBufferPipeline(int element);
    void updateBufferPipelineStrict(int element);
    void destroy();
}
