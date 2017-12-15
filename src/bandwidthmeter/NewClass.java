/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bandwidthmeter;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import static bandwidthmeter.brief.jLabel7;
import static bandwidthmeter.brief.jLabel8;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javax.swing.JFrame;
import org.apache.commons.net.util.SubnetUtils;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

/**
 *
 * @author ma
 */
public final class NewClass {
    private static volatile NewClass instance;
    static double totalSend;
    static double totalRecive;
    static Properties prop=new Properties();
    FileWriter outSave ;
    static AtomicLong send=new AtomicLong();
    static AtomicLong recive=new AtomicLong();
    private Ip4 ip = new Ip4();
    byte[] sIP = new byte[4];
    byte[] dIP = new byte[4];
    int snaplen = 64 * 1024; // Capture all packets, no trucation
    int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
    int timeout = 10 * 1000; // 10 seconds in millis
    ObservableMap<String,String> ipAndMask=FXCollections.observableHashMap();
    List<PcapIf> alldevs = new ArrayList<>(); // Will be filled with NICs
    StringBuilder errbuf = new StringBuilder(); // For any error msgs
   Set<String> lanIps = new HashSet();
   List<String> sourceIps=new ArrayList<>();
   static boolean closeStatus;
   Timer timer=new Timer();
   MenuItem historyItem;
   MenuItem trafficItem;
   private NewClass() throws AWTException, IOException{ 
       this.closeStatus = false;
       lanIps.add("255.255.255.255");
       String [] allip=getAllIps("224.0.0.0","255.255.255.0");
                   for(String item:allip){
                       lanIps.add(item);
                   }
        updateHistory();
        tryIcon();
        total();
        measure();
        autoSave();
   }
   public static NewClass getInstance() throws AWTException, IOException{ 
       if(instance==null){
           synchronized(NewClass.class){
           instance=new NewClass();
       }
       }     
       return instance;
   }
    void total() throws FileNotFoundException, IOException{    
        FileInputStream in=new FileInputStream("src/bandwidthmeter/total.properties");
        prop.load(in);
        in.close();
        totalSend=Double.parseDouble(prop.getProperty("totalSend"));
        totalRecive=Double.parseDouble(prop.getProperty("totalRecive"));  
    }

    WindowListener l=new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {
        trafficItem.setEnabled(false);
        }

        @Override
        public void windowClosing(WindowEvent e) {
              trafficItem.setEnabled(true);
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    };
    WindowListener h=new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {
        historyItem.setEnabled(false);
        }

        @Override
        public void windowClosing(WindowEvent e) {
              historyItem.setEnabled(true);
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    };
    void tryIcon() throws AWTException{
            if (!SystemTray.isSupported()) {
                JFrame frame=new JFrame("BandWidthMeter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new noSupport());
                frame.pack();
                frame.setVisible(true);
                return;
            }
            SystemTray tray = SystemTray.getSystemTray();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.getImage("src/bandwidthmeter/icon.png");
            
            PopupMenu menu = new PopupMenu();
             trafficItem = new MenuItem("traffic");
             trafficItem.addActionListener((ActionEvent e)->{
             
                   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                   int width=screenSize.width;
                   int height=screenSize.height;
                JFrame frame=new JFrame("BandWidthMeter");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setIconImage(image);
                frame.setLocation(width-300,height-300);
                frame.getContentPane().add(new brief());
                frame.pack();
                frame.setVisible(true);
                frame.addWindowListener(l);
                   
               });
              
            
            menu.add(trafficItem);
            historyItem = new MenuItem("Show History");
            
            historyItem.addActionListener((ActionEvent e) -> {
                
                JFrame frame=new JFrame("BandWidthMeter");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setIconImage(image);
                frame.getContentPane().add(new history());
                frame.pack();
                frame.setVisible(true);
                frame.addWindowListener(h);
            }); 
            menu.add(historyItem); 
            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        closeStatus=true;
                        close();
                    } catch (IOException ex) {
                        Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.exit(0);
                }    
            });
            menu.add(closeItem);
            TrayIcon icon = new TrayIcon(image, "BandWidthMeter", menu);
            icon.setImageAutoSize(true);
            String toolTip="BandWidthMeter";
            icon.setToolTip(toolTip);
            tray.add(icon);     
  }
    
    private void updateHistory() throws FileNotFoundException, IOException{
        FileInputStream in=new FileInputStream("src/bandwidthmeter/total.properties");
        prop.load(in);
        in.close(); 
        double beforeSend=Double.parseDouble(prop.getProperty("totalSend"));
        double beforeRecive=Double.parseDouble(prop.getProperty("totalRecive"));
        String autoSaveStatus=prop.getProperty("autoSave");
        if("true".equals(autoSaveStatus)){
        String[] split = null;
      try (BufferedReader br = new BufferedReader(new FileReader("src/bandwidthmeter/autoSave.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
        split = line.split("-");
    }
    }
      int autoSaveSend=Integer.parseInt(split[0]);
      int autoSaveRecive=Integer.parseInt(split[1]);
      double sessionSend = 0;
      double sessionRecive=0;
      double session_Send=0;
      double session_Recive=0;
      if(autoSaveSend!=0){
         sessionSend=(double)autoSaveSend/1048576;
         session_Send=(double)autoSaveSend/1024;
      }
      if(autoSaveRecive!=0){
         sessionRecive=(double)autoSaveRecive/1048576;
         session_Recive=(double)autoSaveRecive/1024;
      }
     try ( //update total.properties
            FileOutputStream out = new FileOutputStream("src/bandwidthmeter/total.properties")) {
            beforeRecive+=sessionRecive;
            beforeSend+=sessionSend;
            prop.setProperty("totalSend",String.valueOf(beforeSend));
            prop.setProperty("totalRecive",String.valueOf(beforeRecive));
            prop.setProperty("autoSave","true");
            prop.store(out,null);
    }
      String row=split[2]+"-"+String.valueOf(session_Send)+"-"+String.valueOf(session_Recive);
        try (FileWriter fw = new FileWriter(new File("src/bandwidthmeter/history.txt"),true)) {
            fw.append(row);
            fw.append("\n");
        }
    }else{ 
            Properties b=new Properties();
            try (FileOutputStream out = new FileOutputStream("src/bandwidthmeter/total.properties")) {
                b.setProperty("autoSave","true");
                b.setProperty("totalSend",prop.getProperty("totalSend"));
                b.setProperty("totalRecive",prop.getProperty("totalRecive"));
                b.store(out,null);
            }   
        }
    }
    static void saveHistory() throws IOException{
         Date date=new Date();
         SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd hh:mm");
         String format1 = format.format(date);
         double sessionSend = 0;
         double sessionRecive=0;
         double session_Send=0;
         double session_Recive=0;
         if(send!=0){
         session_Send=(double)send/1024;
         sessionSend=(double)send/1048576;
         }
         if(recive!=0){
          session_Recive=(double)recive/1024;
           sessionRecive=(double)recive/1048576;
         }
         String row=format1+"-"+String.valueOf(session_Send)+"-"+String.valueOf(session_Recive);
        try (FileWriter fw = new FileWriter(new File("src/bandwidthmeter/history.txt"),true)) {
            fw.append(row);
            fw.append("\n");
        }
        //update total.properties
        try (FileOutputStream out = new FileOutputStream("src/bandwidthmeter/total.properties")) {
            //convert traffic to megabyte
            totalSend+=sessionSend;
            totalRecive+=sessionRecive;
            prop.setProperty("totalSend",String.valueOf(totalSend));
            prop.setProperty("totalRecive",String.valueOf(totalRecive));
            prop.setProperty("autoSave","false");
            prop.store(out,null);
        }
     }
    private void autoSave(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Date date=new Date();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd hh:mm");
                    String format1 = format.format(date);
                    String row=String.valueOf(send)+"-"+String.valueOf(recive)+"-"+format1;
                    Path path=Paths.get("src","bandwidthmeter","autoSave.txt");
                    BufferedWriter writer=Files.newBufferedWriter( path, StandardOpenOption.TRUNCATE_EXISTING);
                    writer.write(row);
                    writer.close();
                    System.out.println("save");
                } catch (Exception ex) {
                    Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
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
    private void getDevices(){
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
                                    send+=size;  
                                    if(jLabel7!=null){
                                    jLabel7.setText(String.valueOf(NewClass.send));
                                    }
                                    }
                                }else{
                                    if(!lanIps.contains(sourceIP)){
                                    recive+=size; 
                                    if(jLabel8!=null){
                                    jLabel8.setText(String.valueOf(NewClass.recive));
                                    }
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
}
