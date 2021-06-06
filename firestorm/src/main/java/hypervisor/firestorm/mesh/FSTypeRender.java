package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.vanguard.utils.VLCopyable;

public interface FSTypeRender extends VLCopyable<FSTypeRender>{

    void scanComplete();
    void bufferComplete();
    void buildComplete();
    void allocateElement(int element, int capacity, int resizer);
    void storeElement(int element, FSElement<?, ?> data);
    void activateFirstElement(int element);
    void activateLastElement(int element);
    void name(String name);
    void parent(FSTypeRenderGroup<?> parent);
    void material(FSLightMaterial material);
    void lightMap(FSLightMap map);
    void colorTexture(FSTexture texture);
    void dispatch(Dispatch<FSTypeRender> dispatch);
    void checkInputs(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    void updateBuffer(int element);
    void schematicsRebuild();
    void schematicsSyncBaseBounds();
    void schematicsUpdateBaseBoundsOrder();
    void schematicsMarkFullUpdate();
    void schematicsMarkModelBoundsForUpdate();
    void schematicsMarkCentroidForUpdate();
    void schematicsMarkCollisionBoundsForUpdate();
    void schematicsMarkInputBoundsForUpdate();
    void applyModelMatrix();
    void destroy();

    FSTypeRenderGroup<?> parent();
    FSTypeRenderGroup<?> parentRoot();
    String name();
    long id();

    interface Dispatch<TYPE extends FSTypeRender>{
        void process(TYPE target);
    }
}
