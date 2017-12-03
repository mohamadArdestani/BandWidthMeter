
package bandwidthmeter;

import static bandwidthmeter.NewClass.closeStatus;
import java.awt.AWTException;
import java.io.IOException;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author ma
 */
public class BandWidthMeter {
    private static void detectCorrectLibrary() throws Exception{
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        String realArch = arch.endsWith("64")
                  || wow64Arch != null && wow64Arch.endsWith("64")
                      ? "64" : "32";
          switch (Util.getOS()) {
            case WINDOWS:
                if(realArch=="64"){
                String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bandwidthmeter"+File.separator+"win64";
                addLibraryPath(property);
                System.loadLibrary("jnetpcap");
                }else{
                    String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bandwidthmeter"+File.separator+"win32";
                    addLibraryPath(property);
                    System.loadLibrary("jnetpcap");
                }
                break;
            case LINUX:
                if(realArch=="64"){
                  String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bandwidthmeter"+File.separator+"linux64";
                  addLibraryPath(property);
                  System.loadLibrary("libjnetpcap");  
                }else{
                    String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bandwidthmeter"+File.separator+"linux32";
                    addLibraryPath(property);
                    System.loadLibrary("libjnetpcap");
                }
                break;
            case MAC:
                   noSupport();
                break;
            case SOLARIS:
                   noSupport();
                break;
    }
    }
    private static void noSupport(){
        JFrame frame=new JFrame("BandWidthMeter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new noSupport());
                frame.pack();
                frame.setVisible(true);
    }
    //addLibraryPath code from fahdshariff.blogspot.fr/2011/08/changing-java-library-path-at-runtime.html
    public static void addLibraryPath(String pathToAdd) throws Exception{
    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    //get array of paths
    final String[] paths = (String[])usrPathsField.get(null);

    //check if the path to add is already present
    for(String path : paths) {
        if(path.equals(pathToAdd)) {
            return;
        }
    }
    //add the new path
    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length-1] = pathToAdd;
    usrPathsField.set(null, newPaths);
}
    public static void main(String[] args) throws AWTException, IOException, URISyntaxException, Exception {
        
        String appId = "bandwidthmeter_id";
        boolean alreadyRunning;
        try {
           JUnique.acquireLock(appId);
            alreadyRunning = false;
            } catch (AlreadyLockedException e) {
               alreadyRunning = true;
            }
        if (alreadyRunning) {
            System.exit(1);
        }
        detectCorrectLibrary();
            try {  
                NewClass n=NewClass.getInstance();
            } catch (AWTException ex) {
                Logger.getLogger(BandWidthMeter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BandWidthMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
        
       Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
            try {
               
                if(closeStatus==false){
                     System.out.println("hg");
               NewClass.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BandWidthMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }, "Shutdown-thread"));
   }    
}

