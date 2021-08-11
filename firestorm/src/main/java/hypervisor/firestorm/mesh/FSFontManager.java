//package hypervisor.firestorm.mesh;
//
//import android.opengl.GLES32;
//
//import java.io.InputStream;
//import java.nio.ByteOrder;
//
//import hypervisor.firestorm.automation.FSAutomator;
//import hypervisor.firestorm.automation.FSHScanner;
//import hypervisor.firestorm.program.FSP;
//import hypervisor.vanguard.list.arraybacked.VLListType;
//import vanguard.VLListType;
//
//public class FSFontManager extends FSP{
//
//    public static final String[] CHARS = new String[]{
//            "`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "[", "]", "\\",
//            "a", "s", "d", "f", "g", "h", "j", "k", "l", ";", "'", "z", "x", "c", "v", "b", "n", "m", ",", ".", "/", "~", "!", "@", "#", "$",
//            "%", "^", "&", "*", "(", ")", "_", "+", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "{", "}", "|", "A", "S", "D", "F", "G",
//            "H", "J", "K", "L", ":", "\"", "Z", "X", "C", "V", "B", "N", "M", "<", ">", "?"
//    };
//
//    private InputStream datastream;
//
//    public FSFontManager(InputStream datastream, int debug){
//        super(2, 100, debug);
//        this.datastream = datastream;
//    }
//
//    @Override
//    protected CoreConfig generateConfigurations(VLListType<FSTypeMesh<?>> targets, int debug){
//        return null;
//    }
//
//    @Override
//    public void destroy(){
//        super.destroy();
//        datastream = null;
//    }
//}
