package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.engine.FSGlobal;
import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.vanguard.utils.VLCopyable;

public interface FSTypeRender extends VLCopyable<FSTypeRender>{

    void build(FSGlobal global);
    void scanComplete();
    void bufferComplete();
    void buildComplete();
    void allocateElement(int element, int capacity, int resizeoverhead);
    void storeElement(int element, FSElement<?, ?> data);
    void activateFirstElement(int element);
    void activateLastElement(int element);
    void name(String name);
    void parent(FSTypeRenderGroup<?> parent);
    void material(FSLightMaterial material);
    void lightMap(FSLightMap map);
    void colorTexture(FSTexture texture);
    void dispatch(Dispatch<FSTypeRender> dispatch);
    boolean checkInputs(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    void updateBuffer(int element);
    void applyModelMatrix();
    void schematicsRebuild();
    void schematicsCheckFixLocalSpaceFlatness();
    void schematicsRefillLocalSpaceBounds();
    void schematicsRebuildLocalSpaceCentroid();
    void schematicsCheckSortLocalSpaceBounds();
    void schematicsCheckSortModelSpaceBounds();
    void schematicsRequestFullUpdate();
    void schematicsRequestUpdateModelSpaceBounds();
    void schematicsRequestUpdateCentroid();
    void schematicsRequestUpdateCollisionBounds();
    void schematicsRequestUpdateInputBounds();
    void schematicsDirectReloadLocalSpaceBounds();
    void schematicsDirectUpdateModelSpaceBounds();
    void schematicsDirectUpdateCentroid();
    void schematicsDirectUpdateCollisionBounds();
    void schematicsDirectUpdateInputBounds();
    void paused();
    void resumed();
    void destroy();

    FSTypeRenderGroup<?> parent();
    FSTypeRenderGroup<?> parentRoot();
    String name();
    long id();

    interface Dispatch<TYPE extends FSTypeRender>{
        void process(TYPE target);
    }
}
