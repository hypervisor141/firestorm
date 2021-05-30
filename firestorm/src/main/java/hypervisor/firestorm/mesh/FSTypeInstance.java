package hypervisor.firestorm.mesh;

import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.array.VLArrayShort;

public interface FSTypeInstance extends FSTypeRender{

    void activateElement(int element, int index);
    void storage(FSElementStore store);
    void modelMatrix(FSMatrixModel set);
    FSTexture colorTexture();
    FSLightMaterial material();
    FSLightMap lightMap();
    FSMatrixModel modelMatrix();
    FSSchematics schematics();
    FSElementStore storage();
    int elementUnitsCount(int element);
    Object elementData(int element);
    FSElement<?, ?> element(int element);
    int vertexSize();
    FSArrayModel model();
    VLArrayFloat positions();
    VLArrayFloat colors();
    VLArrayFloat texCoords();
    VLArrayFloat normals();
    VLArrayShort indices();
    FSElement.FloatArray modelElement();
    FSElement.FloatArray positionsElement();
    FSElement.FloatArray colorsElement();
    FSElement.FloatArray texCoordsElement();
    FSElement.FloatArray normalsElement();
    FSElement.Short indicesElement();
    void updateBuffer(int element, int bindingindex);
    void updateVertexBuffer(int element, int bindingindex);
    void updateVertexBufferStrict(int element, int bindingindex);
}
