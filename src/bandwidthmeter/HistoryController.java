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

import static bwm.mainController.totalRecive;
import static bwm.mainController.totalSend;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author ma
 * mohamadardestani@gmail.com
 */
public class HistoryController implements Initializable {

    @FXML
    private Label totalSendLabel;
    @FXML
    private Label totalReciveLabel;
    @FXML
    private Label totalSendInt;
    @FXML
    private Label totalReciveInt;
     @FXML
    private TableView<historyItem> hTable;
    @FXML
    private TableColumn<historyItem, String> date;
    @FXML
    private TableColumn<historyItem, String> send;
    @FXML
    private TableColumn<historyItem, String> recive;
    @FXML
    private Button moreHistory;
    @FXML
    private Button allHistory;
    @FXML
    private Button delHistory;
    private String sendMark;
    private String reciveMark;
    private int q=0;
    ObservableList obm=FXCollections.observableArrayList();
    Path dm=Paths.get("src", "bwm","history.txt");
    long count;

    public HistoryController() throws IOException {
        this.count = Files.lines(dm).count();
    }
    void showHistory() throws IOException{
     ObservableList ob=FXCollections.observableArrayList();
     Path dir=Paths.get("src", "bwm","history.txt");
     Stream<String> lines=Files.lines(dir).skip(0).limit(10);
     lines.forEach((line)->{
     String[] split = line.split("-");
         historyItem hi=new historyItem(split[0],split[1],split[2]);
         ob.add(hi); 
     });     
    hTable.setItems(ob);
    if(totalSend!=0){
    if(totalSend<1024){
        sendMark=" BYTE";
        totalSendInt.setText((String) String.valueOf(totalSend).subSequence(0,5)+sendMark);
    }
    else if(totalSend<1048576){
        sendMark=" KB";
        totalSendInt.setText((String) String.valueOf(totalSend/1024).subSequence(0,6)+sendMark);
    }
    else if(totalSend<1073741824){
        sendMark=" MB";
        totalSendInt.setText((String) String.valueOf(totalSend/1048576).subSequence(0,6)+sendMark);
    }else{
        sendMark=" GB";
        int dot=String.valueOf(totalSend).indexOf(".");
        totalSendInt.setText((String) String.valueOf(totalSend/1073741824).subSequence(0,dot+2)+sendMark);
    }
    }
    if(totalRecive!=0){
    if(totalRecive<1024){
        reciveMark=" BYTE";
        totalReciveInt.setText(String.valueOf(totalRecive).subSequence(0, 5)+reciveMark);
    }
    else if(totalRecive<1048576){
        reciveMark=" KB";
        totalReciveInt.setText(String.valueOf(totalRecive/1024).subSequence(0, 6)+reciveMark);
    }
    else if(totalRecive<1073741824){
        reciveMark=" MB";
        totalReciveInt.setText(String.valueOf(totalRecive/1048576).subSequence(0, 6)+reciveMark);
    }
    else{
        reciveMark=" GB"; 
        int dot=String.valueOf(totalRecive).indexOf(".");
        totalReciveInt.setText(String.valueOf(totalRecive/1073741824).subSequence(0, dot+2)+reciveMark);
    }
    }   
    }
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @FXML
    private void moreHistory() throws IOException{
    q=q+10; 
    if(q<count){
     Stream<String> lines=Files.lines(dm).skip(q).limit(q);
     lines.forEach((line)->{
     String[] split = line.split("-");
         historyItem hi=new historyItem(split[0],split[1],split[2]);
         obm.add(hi);   
    });
     hTable.getItems().addAll(obm);
     obm.clear();
    }else{
     moreHistory.disableProperty().setValue(Boolean.TRUE);
     allHistory.disableProperty().setValue(Boolean.TRUE);
    }
    }
    @FXML
    private void allHistory() throws IOException{
      moreHistory.disableProperty().setValue(Boolean.TRUE);
      ObservableList ob=FXCollections.observableArrayList();
     Path dir=Paths.get("src", "bwm","history.txt");
     Stream<String> lines=Files.lines(dir);
     lines.forEach((line)->{
     String[] split = line.split("-");
         historyItem hi=new historyItem(split[0],split[1],split[2]);
         ob.add(hi);   
    });
     hTable.setItems(ob);
      allHistory.disableProperty().setValue(Boolean.TRUE);
             }
    @FXML
    private void delHistory() throws IOException{
      moreHistory.disableProperty().setValue(Boolean.TRUE);
      allHistory.disableProperty().setValue(Boolean.TRUE);
      Path dir=Paths.get("src", "bwm","history.txt");
      Files.newBufferedWriter(dir , StandardOpenOption.TRUNCATE_EXISTING); 
      hTable.getItems().clear();
      totalSendInt.setText("0");
      totalReciveInt.setText("0");
      Properties prop=new Properties();
      FileOutputStream out = new FileOutputStream("src/bwm/total.properties"); 
            prop.setProperty("totalSend","0.0");
            prop.setProperty("totalRecive","0.0");
            prop.setProperty("autoSave","false");
            prop.store(out,null);
            out.close();
      delHistory.disableProperty().setValue(Boolean.TRUE);      
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        date.setEditable(false);
        send.setEditable(false);
        recive.setEditable(false);
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        send.setCellValueFactory(new PropertyValueFactory<>("send"));
        recive.setCellValueFactory(new PropertyValueFactory<>("recive"));
        try {
            showHistory();
        } catch (IOException ex) {
            Logger.getLogger(HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
