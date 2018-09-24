/*
 * Copyright 2018 ma.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bwm;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author ma
 * mohamadardestani@gmail.com
 */
public class BWM extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {       
          String appId = "bwm_id";
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
       
      Parent root = null;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("main.fxml"));
        root=loader.load();
        Scene scene = new Scene(root); 
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.setScene(scene);
        stage.setTitle("BandWidthMeter");
        stage.getIcons().add(new Image(getClass().getResource("icon.png").toString()));
        stage.setOnCloseRequest(e->{
              try {
                  mainController.close();
              } catch (IOException ex) {
                  Logger.getLogger(BWM.class.getName()).log(Level.SEVERE, null, ex);
              }
              System.exit(0);
        });
        stage.show();
        javafx.geometry.Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth())-2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight())-2);
    }

public static void detectCorrectLibrary() throws Exception{   
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        String realArch = arch.endsWith("64")
                  || wow64Arch != null && wow64Arch.endsWith("64")
                      ? "64" : "32";
          switch (Util.getOS()) {
            case WINDOWS:
                if(realArch=="64"){                    
                String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bwm"+File.separator+"win64";
               addLibraryPath(property);
               System.loadLibrary("jnetpcap");
                }else{
                    String property = System.getProperty("user.dir")+File.separator+"src"+File.separator+"bwm"+File.separator+"win32";
                    addLibraryPath(property);                    
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
        Alert alert=new Alert(Alert.AlertType.WARNING);
              alert.setHeaderText("Warning");
              alert.setContentText("This Application Can Not Run In Your System!");
              alert.show();
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
