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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.net.util.SubnetUtils;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

/**
 *
 * @author ma
 * mohamadardestani@gmail.com
 */
public class mainController implements Initializable {    
    @FXML
    private Label label;
    @FXML
    private Button buttonH;
    @FXML
    private Button buttonE;
    @FXML
    private Label sendLabel;
    @FXML
    private Label reciveLabel;
    @FXML
   public  Label reciveInt;
    @FXML
   public  Label sendInt;
    static double totalSend;
    static double totalRecive;
    static Properties prop=new Properties();
    FileWriter outSave ;
    static AtomicLong send=new AtomicLong();
    static AtomicLong recive=new AtomicLong();;
    private Ip4 ip = new Ip4();
    byte[] sIP = new byte[4];
    byte[] dIP = new byte[4];
    int snaplen = 64 * 1024; // Capture all packets, no trucation
    int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
    int timeout = 10 * 1000; // 10 seconds in millis
    ObservableMap<String,String> ipAndMask=FXCollections.observableHashMap();
    List<PcapIf> alldevs = new ArrayList<>(); // Will be filled with NICs
    StringBuilder errbuf = new StringBuilder(); // For any error msgs
   HashSet<String> lanIps = new HashSet<String>();
   List<String> sourceIps=new ArrayList<>();
   static boolean closeStatus;
   Timer timer=new Timer();
   HashSet<Thread> dt;
    private  boolean tStatus=false;
    private  BufferedWriter writer;
    public mainController() {
        this.dt = new HashSet();
    }
  
    @FXML
    private void handleButtonE(ActionEvent event) throws IOException {
       close(); 
       System.exit(0);
    }
    @FXML
    private void handleButtonH(ActionEvent event) throws InterruptedException {
       Stage stage=new Stage();
       stage.resizableProperty().setValue(Boolean.FALSE);
       Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("history.fxml"));
            } catch (IOException ex) {
                Logger.getLogger(BWM.class.getName()).log(Level.SEVERE, null, ex);
            }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("History");
        stage.getIcons().add(new Image(getClass().getResource("icon.png").toString()));
        stage.show();
    }
    void total() throws FileNotFoundException, IOException{    
        FileInputStream in=new FileInputStream("src/bwm/total.properties");
        prop.load(in);
        in.close();
        totalSend=Double.parseDouble(prop.getProperty("totalSend"));
        totalRecive=Double.parseDouble(prop.getProperty("totalRecive"));  
    }
    private void updateHistory() throws FileNotFoundException, IOException{
        FileInputStream in=new FileInputStream("src/bwm/total.properties");
        prop.load(in);
        in.close(); 
        double beforeSend=Double.parseDouble(prop.getProperty("totalSend"));
        double beforeRecive=Double.parseDouble(prop.getProperty("totalRecive"));
        String autoSaveStatus=prop.getProperty("autoSave");
        if("true".equals(autoSaveStatus)){
        String[] split = null;
      try (BufferedReader br = new BufferedReader(new FileReader("src/bwm/autoSave.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {
        split = line.split("-");
    }
    }
      
      int autoSaveSend=Integer.parseInt(split[0]);
      int autoSaveRecive=Integer.parseInt(split[1]);
      double session_Send=0;
      double session_Recive=0;
      if(autoSaveSend!=0){
         session_Send=(double)autoSaveSend/1024;
      }
      if(autoSaveRecive!=0){
         session_Recive=(double)autoSaveRecive/1024;
      }
     try ( //update total.properties
            FileOutputStream out = new FileOutputStream("src/bwm/total.properties")) {
            beforeRecive+=autoSaveRecive;
            beforeSend+=autoSaveSend;
            prop.setProperty("totalSend",String.valueOf(beforeSend));
            prop.setProperty("totalRecive",String.valueOf(beforeRecive));
            prop.setProperty("autoSave","true");
            prop.store(out,null);
    }
      String row=split[2]+"-"+String.valueOf(session_Send)+"-"+String.valueOf(session_Recive);
        try (FileWriter fw = new FileWriter(new File("src/bwm/history.txt"),true)) {
            fw.append(row);
            fw.append("\n");
        }
    }else{ 
            Properties b=new Properties();
            try (FileOutputStream out = new FileOutputStream("src/bwm/total.properties")) {
                b.setProperty("autoSave","true");
                b.setProperty("totalSend",prop.getProperty("totalSend"));
                b.setProperty("totalRecive",prop.getProperty("totalRecive"));
                b.store(out,null);
            }   
        }
    }
    static void saveHistory() throws IOException{
         Date date=new Date();
         SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd HH:mm");
         String format1 = format.format(date);
         double session_Send=0;
         double session_Recive=0;
         if(!send.equals(0)){
         session_Send=send.doubleValue()/1024;
         }
         if(!recive.equals(0)){
          session_Recive=recive.doubleValue()/1024;
         }
         String row=format1+"-"+String.valueOf(session_Send)+"-"+String.valueOf(session_Recive);
         //update history.txt
         BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
         new FileOutputStream("src/bwm/history.txt", true),StandardCharsets.UTF_8));
         fw.append(row);
         fw.append("\n");
         fw.close();
        //update total.properties
        try (FileOutputStream out = new FileOutputStream("src/bwm/total.properties")) {
            //convert traffic to megabyte
            totalSend+=send.doubleValue();
            totalRecive+=recive.doubleValue();
            prop.setProperty("totalSend",String.valueOf(totalSend));
            prop.setProperty("totalRecive",String.valueOf(totalRecive));
            prop.setProperty("autoSave","false");
            prop.store(out,null);
        }
     }
    private void autoSave() throws IOException{
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Path path=Paths.get("src","bwm","autoSave.txt");         
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Date date=new Date();                    
                    String format1 = format.format(date);
                    String row=String.valueOf(send)+"-"+String.valueOf(recive)+"-"+format1; 
                    writer=Files.newBufferedWriter( path, StandardOpenOption.TRUNCATE_EXISTING);
                    writer.write(row);
                    writer.flush();
                    System.out.println("autosave");
                } catch (Exception ex) {
                    Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 60000,5000);
    }
    private void measure(){
        int r = Pcap.findAllDevs(alldevs, errbuf);
		if (r != Pcap.OK || alldevs.isEmpty()) {
		System.err.printf("Can't read list of devices, error is %s",
		errbuf.toString());
		return;
                }
                //find lan ips and source ip
                for (PcapIf device : alldevs) {
                    String ip=device.getAddresses().get(0).getAddr().toString()
                            .replaceAll("([a-zA-Z]\\w+)([0-9:])","")
                            .replaceAll("[\\[\\]]","");
                   if(ip.equals("0.0.0.0")){
                       continue;
                   }
                    sourceIps.add(ip);
                    
                    String mask=device.getAddresses().get(0).getNetmask().toString()
                            .replaceAll("([a-zA-Z]\\w+)([0-9:])","")
                            .replaceAll("[\\[\\]]","");
                   String [] allip=getAllIps(ip,mask);
                   for(String item:allip){
                       lanIps.add(item);
                   }
                }
                getDevices();
}
    private void reload(){        
            Timer re=new Timer();
            re.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                if(dt!=null){    
                    for (Thread t : dt) {
                        if(t.isAlive()){
                            tStatus=true;                           
                        }else{
                            t.interrupt();                           
                        }  
                    }
                    if(tStatus==false){
                        getDevices();
                    }
                    tStatus=false;
                }
                }
            }, 7500, 5000); 
        }
    private void getDevices(){
        if(dt!=null){
            dt.clear();
        }
        for (PcapIf devices : alldevs) {
            Thread thread=new Thread(){
                public void run() {   
                 PcapIf device =devices; 
                 System.out.printf("\nChoosing '%s' on your behalf:\n",
				(device.getDescription() != null) ? device.getDescription()
						: device.getAddresses());
                Pcap pcap=Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
		if (pcap == null) {
			System.err.printf("Error while opening device for capture: "
					+ errbuf.toString());
			return;
		}    
		// capture all packages
                try{  
		pcap.loop(Pcap.LOOP_INFINITE, jpacketHandler, "jNetPcap");
                
              }catch(Exception e){
                          System.out.println(e.getMessage());
                           
            } finally{
                    pcap.close();
                    
                } 
                }
            };
            thread.start(); 
            dt.add(thread);
        } 
    }
    PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {   
			public void nextPacket(PcapPacket packet, String user) {
                             byte[] data = packet.getByteArray(0, packet.size()); // the package data
				if (packet.hasHeader(ip) == false) {
					return; // Not IP packet
				}
				dIP = packet.getHeader(ip).destination();
				sIP = packet.getHeader(ip).source();
                                int size=packet.getCaptureHeader().caplen();
				/* Use jNetPcap format utilities */
				String sourceIP = org.jnetpcap.packet.format.FormatUtils.ip(sIP);
				String destinationIP = org.jnetpcap.packet.format.FormatUtils.ip(dIP);                               
				if(sourceIps.contains(sourceIP)){
                                    if(!lanIps.contains(destinationIP)){
                                    send.addAndGet(size); 
                                        Platform.runLater(()->{
                                         sendInt.setText(String.valueOf(send));  
                                        });
                                    
                                    }
                                    
                                }else{
                                    if(!lanIps.contains(sourceIP)){
                                    recive.addAndGet(size); 
                                        Platform.runLater(()->{
                                          reciveInt.setText(String.valueOf(recive));  
                                        });
                                    
                                    }
                                    }
                                }
			
		};
    String[] getAllIps(String ip,String mask){
        SubnetUtils utils = new SubnetUtils(ip,mask);
       utils.setInclusiveHostCount(true);
       String[] allIps = utils.getInfo().getAllAddresses();
       return allIps;
    }
    static void close() throws IOException{ 
        saveHistory();
    } 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
         this.closeStatus = false;
       lanIps.add("255.255.255.255");
       String [] allip=getAllIps("224.0.0.0","255.255.255.0");
                   for(String item:allip){
                       lanIps.add(item);
                   }                  
        try {
            updateHistory();
        } catch (IOException ex) {
            Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            total();
        } catch (IOException ex) {
            Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            autoSave();
        } catch (IOException ex) {
            Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
        }
      measure();
      reload();
    }    
    
}
