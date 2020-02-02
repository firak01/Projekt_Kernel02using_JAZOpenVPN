package use.openvpn.clientui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;


import use.openvpn.ConfigStarterZZZ;
import use.openvpn.client.ClientConfigFileZZZ;
import use.openvpn.client.ClientMainZZZ;

import basic.zKernel.KernelZZZ;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;

public class ClientTrayUIZZZ extends KernelUseObjectZZZ implements ActionListener {
	public static final int iSTATUS_DISCONNECTED = 0;
	public static final int iSTATUS_CONNECTING = 1;
	public static final int iSTATUS_CONNECTED = 2;
	public static final int iSTATUS_ERROR = 3;
	public static final int iSTATUS_FAILED = 4;
	public static final int iSTATUS_INTERRUPTED = 5;
	private String sStatusString = null;

	private SystemTray objTray = null;
	private TrayIcon objTrayIcon = null;
	private ClientMainZZZ objClientMain = null;
	
	
	public ClientTrayUIZZZ(IKernelZZZ objKernel, ClientMainZZZ objClientMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ClientTrayUINew_(objClientMain, saFlagControl);
	}
	
	private void ClientTrayUINew_(ClientMainZZZ objClientMain, String[] saFlagControl) throws ExceptionZZZ{
		main:{
		
			//try{		
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ( stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}	
	
				if(objClientMain==null){
						ExceptionZZZ ez = new ExceptionZZZ("ClientMain-Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName()); 					 
					   throw ez;		 
				}else{
					this.objClientMain = objClientMain;
				}
			}//End check
			
			//Dieses muss beim Beenden angesprochen werden, um das TrayIcon wieder zu entfernen
			this.objTray = SystemTray.getDefaultSystemTray();
			
			JPopupMenu menu = new JPopupMenu();
			
			JMenuItem menueeintrag2 = new JMenuItem("Verbinden");
			menu.add(menueeintrag2);
			menueeintrag2.addActionListener(this);
			
			JMenuItem menueeintrag3 = new JMenuItem("Client Log ansehen");
            menu.add(menueeintrag3);
			menueeintrag3.addActionListener(this);
			
			//??? Warum geht das auf meinem Desktop-Rechner nicht, auf dem Notebook aber ???			
			JMenuItem menueeintrag = new JMenuItem("Beenden");	
			menu.add(menueeintrag);		
			menueeintrag.addActionListener(this);
			
			/* das scheint dann doch nicht notwendig zu sein !!!
			menueeintrag.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent me){
					System.out.println("mausi released");
				}
				public void mousePressed(MouseEvent me){
					System.out.println("mausi pressed");
				}
				public void mouseClicked(MouseEvent me){
					System.out.println("mausi clicked");
				}
				
				//Das wird erkannt
				public void mouseEntered(MouseEvent me){
					System.out.println("mausi entered");
				}
				
				//
				public void mouseExited(MouseEvent me){
					System.out.println("mausi exited");
				}
			});
			*/

		
			
			ImageIcon objIcon = this.readImageIconByStatus(iSTATUS_DISCONNECTED);			
			this.objTrayIcon = new TrayIcon(objIcon, "OVPNConnector", menu);
			this.objTrayIcon.addActionListener(this);
			
			this.objTray.addTrayIcon(this.objTrayIcon);
			
		}//END main
	}
	
	public ImageIcon readImageIconByStatus(int iStatus){
		ImageIcon objReturn = null;
		main:{
			URL url = null;
			switch(iStatus){
			case iSTATUS_CONNECTING:
				 url= ClassLoader.getSystemResource("button-yellow_benji_park_01.png");
				 break;
			case iSTATUS_CONNECTED:
				 url= ClassLoader.getSystemResource("button-green_benji_park_01.png");
				 break;
			case iSTATUS_DISCONNECTED:
				 url= ClassLoader.getSystemResource("button-blue_benji_park_01.png");
				 break;
			case iSTATUS_ERROR:
				url= ClassLoader.getSystemResource("button-red_benji_park_01.png");
				 break;
			case iSTATUS_FAILED:
				url= ClassLoader.getSystemResource("button-purple_benji_park_01.png");
				 break;
			case iSTATUS_INTERRUPTED:
				url= ClassLoader.getSystemResource("button-purple_benji_park_01.png");
				 break;
			default:
				break main;
			}
			objReturn = new ImageIcon(url);
		}//END main:
		return objReturn;
	}
	
	public String readStatusStringByStatus(int iStatus){
		String sReturn=null;
		main:{
			switch(iStatus){
			case iSTATUS_CONNECTING:
				sReturn = "Connecting ...";
				break;
			case iSTATUS_CONNECTED:
				sReturn = "Connected.";
				break;
			case iSTATUS_DISCONNECTED:
				sReturn = "Disconnected.";
				break;
			case iSTATUS_ERROR:
				sReturn = "ERROR.";
				break;
			case  iSTATUS_FAILED:
				sReturn = "Connection failed.";
				break;
			case  iSTATUS_INTERRUPTED:
				sReturn = "Connection interrupted.";
				break;
			default: 
				break main;
			}
		}
		return sReturn;
	}
	
	public boolean switchStatus(int iStatus){
		boolean bReturn = false;
		main:{
			//StatusString �ndern
			this.sStatusString = this.readStatusStringByStatus(iStatus);
			
			
			//ImageIcon �ndern
			ImageIcon objIcon = this.readImageIconByStatus(iStatus);
			if(objIcon==null)break main;
			
			this.getTrayIconObject().setIcon(objIcon);
			bReturn = true;
		}//END main:
		return bReturn;
	}
	

	

	/**Adds an icon ito the systemtray. 
	 * Right click on the item to show available menue entries.
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 13:03:47
	 */
	public boolean load(){
		boolean bReturn = false;
		main:{		
			this.objTray.addTrayIcon(this.objTrayIcon);
			bReturn = true;
		}//END main:
		return bReturn;
	}

	

	/**Removes the icon from the systemtray
	 * AND removes any running "openvpn.exe" processes. (or how the exe-file is named)
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 13:05:25
	 * @throws ExceptionZZZ 
	 */
	public boolean unload() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			//TODO Nat�rlich m�ssen hier ggf. noch weitere Sachen gemacht werden, z.B. Threads beenden
			
			//###### Processe beenden
			//+++ Vorbereitend den processnamen auslesen
			File objFileExe = ClientConfigFileZZZ.findFileExe();
			if(objFileExe!=null){
				String sExeCaption = objFileExe.getName();
				//+++ Wenigstens beende ich nun alle openvpn.exe - Processe (DASZU WIRD JACOB (Java COM Bridge) verwendet.	
				KernelWMIZZZ objWMI = new KernelWMIZZZ(objKernel, null);
				objWMI.killProcessAll(sExeCaption);				
			}//END if file!= null
			
			//+++ Merke: Windows merkt das Entfernen nicht sofort, sondern manchmal erst, wenn man den Mauszeiger auf den System-Tray-Bereich bewegt.
			this.objTray.removeTrayIcon(this.objTrayIcon);
			bReturn = true;
			System.exit(0);
		}
		return bReturn;
	}

	
	public boolean connect(){
		boolean bReturn = false;
		main:{
			try{
				check:{
				}
				this.switchStatus(iSTATUS_CONNECTING);
											
				//DIES �ber einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				Thread objThreadConfig = new Thread(this.objClientMain);
				objThreadConfig.start();
				
				ClientMonitorRunnerZZZ objMonitor = new ClientMonitorRunnerZZZ(this.getKernelObject(), this, this.objClientMain, null);
				Thread objThreadMonitor = new Thread(objMonitor);
				objThreadMonitor.start();
							   
			}catch(ExceptionZZZ ez){
				//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zur�ckgesetzt werden kann.
				this.switchStatus(iSTATUS_ERROR);
			}		
		}
		return bReturn;
	}
	
	/** Ausgabe des Strings für den Dialog, beim Clicken auf das TrayIcon. 
	 *  Merke: Die Länge des Strings ist dafür begrenzt.
	 * @return
	 * @throws ExceptionZZZ
	 */
	public String readStatusDetailString() throws ExceptionZZZ{
		String sReturn = "";
		main:{
			String stemp = null;
			check:{
				if (this.objClientMain == null){
					sReturn = "Not yet tried to connect";
						break main;
				}
			}//END check
			
		if(this.sStatusString==null){
			sReturn = sReturn + "assssffffggg\n\n"; //DAS SOLL NICHT VORKOMMEN
		}else{
			//20200114: Erweiterung - Angabe des Rechnernamens
			try {
				String sServerOrClient = this.objClientMain.getConfigChooserObject().getOvpnContextUsed();
				sReturn = sReturn + sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
			} catch (UnknownHostException e) {				
				e.printStackTrace();
				ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
			}
			
			sReturn = sReturn + "STATUS: " + this.sStatusString + "\n";
		}
		
		if(this.objClientMain.getFlag("useProxy")==true){
			sReturn = sReturn + "Proxy: " + this.objClientMain.getApplicationObject().getProxyHost() + ":" + this.objClientMain.getApplicationObject().getProxyPort() + "\n"; 					
		}else{
			sReturn = sReturn + "No proxy.\n";
		}
		
		stemp = this.objClientMain.getApplicationObject().getURL2Parse();
		if(stemp==null){
			sReturn = sReturn + "Parsed URL: NOT RECEIVED\n";
		}else{
			sReturn = sReturn + "Parsed URL: '" + stemp + "'\n";
		}
		
		//REMOTE
		stemp = this.objClientMain.getApplicationObject().getIpRemote();
		if(stemp==null){
			sReturn = sReturn + "Remote IP: Not found on URL.\n";
		}else{
			sReturn = sReturn + "Remote IP: '" + stemp + "'\n";
		}
		 
		if(this.objClientMain.isPortScanEnabled()==true){
			stemp = this.objClientMain.getApplicationObject().getRemotePortScanned();
			if(stemp == null){
				sReturn = sReturn + "Remote Port(s): Not yet scanned.\n";
			}else{
				stemp = this.objClientMain.getApplicationObject().getRemotePortScanned();
				sReturn = sReturn + "Remote Port(s):" + stemp+"\n";
			}
		}
		
		
		
		
		//VPNIP
		stemp = this.objClientMain.getApplicationObject().getVpnIpEstablished();
		if(stemp == null){
			sReturn = sReturn + "Remote VPN-IP: Not yet connected.\n";
		}else{
			sReturn = sReturn + "Remote VPN-IP: " + stemp + "\n";
			/* Logischer Fehler: Wenn die VPN-Verbindung erstellt worden ist, dann ist ggf. auch ein anderer Port "anpingbar" per meinem JavaPing.
			stemp = this.objConfig.getVpnPortEstablished();
			sReturn = sReturn + ":" + stemp;
			*/
		}
		
		if(this.objClientMain.isPortScanEnabled()==true){
			stemp = this.objClientMain.getApplicationObject().getVpnPortScanned();
			if(stemp == null){
				sReturn = sReturn + "Remote VPN-IP Port(s): Not yet scanned.\n";
			}else{
				stemp = this.objClientMain.getApplicationObject().getVpnPortScanned();
				sReturn = sReturn + "Remote VPN-IP Port(s):" + stemp+"\n";
			}
		}
		
		String sTap = this.objClientMain.getApplicationObject().getTapAdapterUsed();
		if(sTap==null){
			sTap = "-> TAP Adapter: Not defined in Kernel Ini-File.";
		}else{
			sTap = "-> TAP Adapter: '" + sTap + "'";
		}
		
		stemp = this.objClientMain.getApplicationObject().getVpnIpLocal();
		if(stemp==null){
			sReturn = sReturn + "Local VPN-IP: Not defined in Kernel Ini-File.\n\t\t" + sTap + "\n";
		}else{
			sReturn = sReturn + "Local VPN-IP: '" + stemp + "'\n\t\t" + sTap + "\n";
		}
		
		stemp = this.objClientMain.getApplicationObject().getIpLocal();
		if(stemp==null){
			sReturn = sReturn + "Local IP: Not availabel.\n";
		}else{
			sReturn = sReturn + "Local IP: '" + stemp + "'\n";
		}
		}//END main
		return sReturn;
	}
	
	public String readLogString(){
		String sReturn = "";
		main:{
			check:{
				if (this.objClientMain == null){
					sReturn = "Not yet tried to connect";
						break main;
				}
			}//END check:
		
		ArrayList listaLogString = this.objClientMain.getStatusStringAll();
		if(listaLogString.isEmpty()){
			if (this.objClientMain == null){
					sReturn = "No log string available.";
					break main;
			}
		}
		
		for(int icount = 0; icount < listaLogString.size(); icount++){
			String sLog = (String)listaLogString.get(icount);
			sReturn = sReturn + sLog + "\n";
		}
		
		}//END main
		return sReturn;
	}
	
	
	//#######################
	//### GETTER / SETTER
	public TrayIcon getTrayIconObject(){
		return this.objTrayIcon;
	}
	
	public String getStatusString(){
		return this.sStatusString;
	}
	
	
	
	

//FGL Es scheint so als geht das nicht mit extra Klassen.
		public void actionPerformed(ActionEvent arg0) {
			try{
				String sCommand = arg0.getActionCommand();
				//System.out.println("Action to perform: " + sCommand);
				if(sCommand.equals("Beenden")){
					this.unload();	
				}else if(sCommand.equals("Verbinden")){
					this.connect();
				}else if(sCommand.equals("Client Log ansehen")){
					//JOptionPane pane = new JOptionPane();
					String stemp = this.readLogString();
					//this.getTrayIconObject() ist keine Component ????
					JOptionPane.showMessageDialog(null, stemp, "Log der Verbindung", JOptionPane.INFORMATION_MESSAGE );
				}else if(sCommand.equals("PressAction")){			//DAS SCHEINT EIN FEST VORGEGEBENER NAME VON JDIC zu sein.		
					String stemp = this.readStatusDetailString();
					if(stemp!= null){
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Status der Verbindung.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
					}else{
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Status der Verbindung.", "unable to receive any status.", TrayIcon.INFO_MESSAGE_TYPE);
					}
				
					/* DAS PASST NICHT IN DIESE SPRECHBLASE REIN
					String stemp = this.readLogString();
					if(stemp!= null){
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Log der Verbindung.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
					}else{
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Log der Verbindung.", "unable to receive any log.", TrayIcon.INFO_MESSAGE_TYPE);
					}
					*/
				}
			}catch(ExceptionZZZ ez){
				//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zur�ckgesetzt werden kann.
				this.getKernelObject().getLogObject().WriteLineDate(ez.getDetailAllLast());
				this.switchStatus(iSTATUS_ERROR);
			}
		}

	

}//END Class
