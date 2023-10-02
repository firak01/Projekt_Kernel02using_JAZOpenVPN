package use.openvpn.clientui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigFileZZZ;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.process.ClientThreadConnectionVpnIpMonitorOVPN;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.ProcessWatchRunnerOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.clientui.IClientStatusMappedValueZZZ.ClientTrayStatusTypeZZZ;
import use.openvpn.clientui.IClientTrayMenuZZZ.ClientTrayMenuTypeZZZ;
import use.openvpn.clientui.IConstantClientOVPN;
import use.openvpn.clientui.component.IPExternalRead.DlgIPExternalOVPN;
import use.openvpn.component.shared.adjustment.DlgAdjustmentOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.serverui.ServerMonitorRunnerOVPN;
import use.openvpn.serverui.ServerTrayMenuZZZ;
import use.openvpn.serverui.ServerTrayStatusMappedValueZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.IListenerObjectFlagZsetZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.enums.EnumSetUtilZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.ResourceEasyZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;

public class ClientTrayUIZZZ extends KernelUseObjectZZZ implements ActionListener ,IListenerObjectFlagZsetZZZ, IListenerObjectStatusLocalSetOVPN {
//	public static final int iSTATUS_NEW = 0;	
//	public static final int iSTATUS_CONNECTING = 1;
//	public static final int iSTATUS_FAILED = 2;	
//	public static final int iSTATUS_CONNECTED = 3;		
//	public static final int iSTATUS_INTERRUPTED = 4;	
//	public static final int iSTATUS_DISCONNECTED = 5;
//	public static final int iSTATUS_ERROR = 6;
//	private String sStatusString = null;

	private SystemTray objTray = null;
	private TrayIcon objTrayIcon = null;
	private ClientThreadProcessWatchMonitorOVPN  objMonitorProcess = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
	private ClientThreadConnectionVpnIpMonitorOVPN  objMonitorConnection = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
	private ClientMainOVPN objClientBackend = null;
	
	//TODOGOON 20210210: Realisiere die Idee
	//Idee: In ClientMainUI eine/verschiedene HashMaps anbieten, in die dann diese Container-Objekte kommen.
	//      Dadurch muss man sie nicht als Variable deklarieren und kann dynamischer neue Dialogboxen, etc. hinzufügen.
	//Ziel diese hier als Varible zu deklarieren ist: Die Dialogbox muss nicht immer wieder neu erstellt werden.
	private KernelJDialogExtendedZZZ dlgIPExternal=null;
	private KernelJDialogExtendedZZZ dlgAdjustment=null;
	
	public ClientTrayUIZZZ(IKernelZZZ objKernel, ClientMainOVPN objClientMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);//20210402: Die direkten Flags werden nun in der Elternklasse verarbeitet
		ClientTrayUINew_(objClientMain);
	}
	
	private void ClientTrayUINew_(ClientMainOVPN objClientMain) throws ExceptionZZZ{
		main:{		
			//try{		
			check:{
				if(this.getFlag("init")) break main;
				if(objClientMain==null){
						ExceptionZZZ ez = new ExceptionZZZ("ClientMain-Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName()); 					 
					   throw ez;		 
				}else{
					this.objClientBackend = objClientMain;
				}
			}//End check
			
			//Dieses muss beim Beenden angesprochen werden, um das TrayIcon wieder zu entfernen
			//Merke 20220718: Wohl unter Win10 nicht lauffähig
			this.objTray = SystemTray.getDefaultSystemTray();
			
			//https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html
			//this.objTray = SystemTray.getSystemTray();
			
			JPopupMenu menu = new JPopupMenu();
			
			JMenuItem menueeintrag2 = new JMenuItem(ClientTrayMenuTypeZZZ.START.getMenu());
			menu.add(menueeintrag2);
			menueeintrag2.addActionListener(this);
			
			
			JMenuItem menueeintrag2b = new JMenuItem(ClientTrayMenuTypeZZZ.CONNECT.getMenu());
			menu.add(menueeintrag2b);
			menueeintrag2b.addActionListener(this);
			
			JMenuItem menueeintrag2c = new JMenuItem(ClientTrayMenuTypeZZZ.WATCH.getMenu());
			menu.add(menueeintrag2c);
			menueeintrag2c.addActionListener(this);
			
			JMenuItem menueeintrag3 = new JMenuItem(ClientTrayMenuTypeZZZ.LOG.getMenu());
            menu.add(menueeintrag3);
			menueeintrag3.addActionListener(this);
			
			JMenuItem menueeintrag4 = new JMenuItem(ClientTrayMenuTypeZZZ.ADJUSTMENT.getMenu());
			menu.add(menueeintrag4);
			menueeintrag4.addActionListener(this);
			
			JMenuItem menueeintragFTP = new JMenuItem(ClientTrayMenuTypeZZZ.PAGE_IP_READ.getMenu());
            menu.add(menueeintragFTP);
			menueeintragFTP.addActionListener(this);
						
			JMenuItem menueeintrag = new JMenuItem(ClientTrayMenuTypeZZZ.END.getMenu());	
			menu.add(menueeintrag);		
			menueeintrag.addActionListener(this);
			
			//DUMMY Einträge, a: Server / Client
			//DUMMY Einträge, damit der unterste Eintrag ggfs. nicht durch die Windows Taskleiste verdeckt wird
			JMenuItem menueeintragLine = new JMenuItem("------------------");
			menu.add(menueeintragLine);
			//Kein actionListener für Dummy Eintrag
			
			JMenuItem menueeintragContext = new JMenuItem("RUNNING AS CLIENT");
			menu.add(menueeintragContext);
			//Kein actionListener für Dummy Eintrag
			
			JMenuItem menueeintragDummy = new JMenuItem(" ");
			menu.add(menueeintragDummy);
			//Kein actionListener für Dummy Eintrag
			
			
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

			ImageIcon objIcon = this.getImageIconByStatus(ClientTrayStatusTypeZZZ.NEW);			
			this.objTrayIcon = new TrayIcon(objIcon, "OVPNConnector", menu);
			this.objTrayIcon.addActionListener(this);			
			this.objTray.addTrayIcon(this.objTrayIcon);
			
			
			//Den Process-Monitor auch schon vorbereiten, auch wenn ggfs. nicht schon am Anfang auf die Verbindung "gelistend" wird.
			//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ClientThreadProcessWatchMonitorOVPN-Object";
			System.out.println(sLog);
			this.getLogObject().WriteLineDate(sLog);
			this.objMonitorProcess = new ClientThreadProcessWatchMonitorOVPN(this.getKernelObject(), this.getClientBackendObject(), null);
			this.getClientBackendObject().registerForStatusLocalEvent(this.objMonitorProcess);
			
			//Monitor noch nicht starten!!!
			//Thread objThreadProcessMonitor = new Thread(this.objMonitorProcess);
			//objThreadProcessMonitor.start();
			
			//Den VPNIP-Monitor auch schon vorbereiten.
			//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ClientThreadConnectionVpnIpMonitorOVPN-Object";
			System.out.println(sLog);
			this.getLogObject().WriteLineDate(sLog);
			this.objMonitorConnection = new ClientThreadConnectionVpnIpMonitorOVPN(this.getKernelObject(), this.getClientBackendObject(), null);
			this.getClientBackendObject().registerForStatusLocalEvent(this.objMonitorConnection);
			
			//Monitor noch nicht starten!!!
			//Thread objThreadConnectionMonitor = new Thread(this.objMonitorConnection);
			//objThreadConnectionMonitor.start();
			
		}//END main
	}
		
	public static ImageIcon getImageIconByStatus(ClientTrayStatusTypeZZZ enumSTATUS)throws ExceptionZZZ{	
		ImageIcon objReturn = null;
		main:{
			URL url = null;
			ClassLoader objClassLoader = ClientTrayUIZZZ.class.getClassLoader(); 
			if(objClassLoader==null) {
				ExceptionZZZ ez = new ExceptionZZZ("unable to receiver classloader object", iERROR_RUNTIME, ClientTrayUIZZZ.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			String sPath= ResourceEasyZZZ.searchDirectoryAsStringRelative("resourceZZZ/image/tray"); //Merke: Innerhalb einer JAR-Datei soll hier ein src/ vorangestellt werden.					
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for directory '"+sPath+"'");
						
			String sImageIcon = enumSTATUS.getIconFileName();
			String sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, sImageIcon);
			
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for imageicon '"+sPathTotal+"'");			
			url= ClassLoader.getSystemResource(sPathTotal);
			if(url==null) {
				String sLog = "unable to receive url object. Path '" + sPathTotal + "' not found?";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
				ExceptionZZZ ez = new ExceptionZZZ(sLog, iERROR_RUNTIME, ClientTrayUIZZZ.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": URL = '"+url.toExternalForm() + "'");
			}
			objReturn = new ImageIcon(url);
		}//END main:
		return objReturn;
	}
	
//	public static String getStatusStringByStatus(Enum enumSTATUS) throws ExceptionZZZ{
//		String sReturn=null;
//		main:{
//			
//			//TODO: Diese Strings müssen aus dem enum kommen
//			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Status="+enumSTATUS.name();			
//			System.out.println(sLog);
//			String a = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "NEW");
//			String c = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "CONNECTING");
//			String b = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "STARTING");
//			String d = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "CONNECTED");
//			String e = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "INTERRUPTED");
//			String f = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "DISCONNCTED");
//			String g = EnumSetUtilZZZ.readEnumConstant_NameValue(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.class, "ERROR");
//			if(enumSTATUS.name().equalsIgnoreCase(a)){ 			
//				sReturn = "Not yet started.";
//			}else if(enumSTATUS.name().equalsIgnoreCase(b)) {
//				sReturn = "Starting ...";
//			}else if(enumSTATUS.name().equalsIgnoreCase(c)) {
//				sReturn = "Listening for Connection.";
//			}else if(enumSTATUS.name().equalsIgnoreCase(d)) {
//				sReturn = "Connected.";
//			}else if(enumSTATUS.name().equalsIgnoreCase(e)) {
//				sReturn = "Connection ended or interrupted.";
//			}else if(enumSTATUS.name().equalsIgnoreCase(f)) {
//				sReturn = "Stopped listening.";
//			}else if(enumSTATUS.name().equalsIgnoreCase(g)) {
//				sReturn = "ERROR.";			
//			}else{ 
//				sReturn = "... Status not handled ...";
//				break main;
//			}
//		}//end main:
//		return sReturn;
//	}
		
	public boolean switchStatus(ClientTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ{	
		boolean bReturn = false;
		main:{
			//ImageIcon aendern
			ImageIcon objIcon = this.getImageIconByStatus(enumSTATUS);
			if(objIcon==null)break main;
			
			this.getTrayIconObject().setIcon(objIcon);

//          Der Monitor aendert den Status String selbst, aufgrund des vom Backend geworfenen Ereignisses			
//			String sStatus = this.getStatusStringByStatus(enumSTATUS);
//			if(this.getMonitorObject()!=null) {
//				this.getMonitorObject().setStatusString(sStatus);
//			}
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
			//TODO Natuerlich muessen hier ggf. noch weitere Sachen gemacht werden, z.B. Threads beenden
			
			//###### Prozesse beenden
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

	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
			if(this.getClientBackendObject()==null)break main;
			}

			//Wenn das so ohne Thread gestartet wird, dann reagiert der Tray auf keine weiteren Clicks.
			//Z.B. den Status anzuzeigen.
			//this.getClientBackendObject().start(this);
			
			//Also dies über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
			//Problem dabei: Der SystemTray wird nicht aktualisiert.
			Thread objThreadMain = new Thread(this.getClientBackendObject());
			objThreadMain.start();
				
			bReturn = true;
		}//end main:
		return bReturn;
	}
			
				
	
	
	public boolean connect(){
		boolean bReturn = false;
		main:{
			try{ 
				check:{
				if(this.getClientBackendObject()==null)break main;
				}
				
				ClientThreadProcessWatchMonitorOVPN objMonitor = this.getProcessMonitorObject();
				if(objMonitor==null) break main;
				
				boolean bStarted = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED);
				if(!bStarted) {
					
					boolean bStarting = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bStarting) {	
						objMonitor.setStatusString("Client not starting.");
						break main;
					}
					
					objMonitor.setStatusString("Client not finished starting. Waiting for process?");
					break main;
				}
				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.
				//this.objServerBackend = (ServerMainZZZ) this.getServerBackendObject().getApplicationObject(); //new ServerMainZZZ(this.getKernelObject(), null);
				
				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Thread objThreadConfig = new Thread(this.getServerBackendObject());
				//objThreadConfig.start();

				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.
				//Merke: Dieser Monitor Thread muss mit dem Starten der einzelnen Unterthreads solange warten, bis das ServerMainZZZ-Object in seinem Flag anzeigt, dass es fertig mit dem Start ist.
				
				//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
				this.objClientBackend.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
								
				Thread objThreadMonitorThread = new Thread(objMonitor);
				objThreadMonitorThread.start();
								
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.
				
				bReturn = true;
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ ez2) {
					System.out.println(ez.getDetailAllLast());
					ez2.printStackTrace();					
				}
			}		
		}
		return bReturn;		
	}
	
	/** Hiermit wird der Prozess gestartet, der die erfolgreiche Verbindung permanent mit einem PING ueberwacht.
	 * @return
	 * @author Fritz Lindhauer, 29.09.2023, 17:09:11
	 */
	public boolean watch(){
		boolean bReturn = false;
		main:{
			try{ 
				check:{
				if(this.getClientBackendObject()==null)break main;
				}
				
				ClientThreadConnectionVpnIpMonitorOVPN objMonitor = this.getConnectionMonitorObject();
				if(objMonitor==null) break main;
				
				boolean bStarted = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED);
				if(!bStarted) {
					
					boolean bStarting = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bStarting) {	
						objMonitor.setStatusString("Client not starting.");
						break main;
					}
					
					objMonitor.setStatusString("Client not finished starting. Waiting for process?");
					break main;
				}
				
				
				boolean bConnecting = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING) ;
				if(!bConnecting) {
					objMonitor.setStatusString("Client not connecting.");
					break main;
				}
				
				boolean bConnected = this.getClientBackendObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED) ;
				if(!bConnected) {
					objMonitor.setStatusString("Client not connected.");
					break main;
				}
				
				this.objClientBackend.setStatusLocal(ClientMainOVPN.STATUSLOCAL.WATCHRUNNERSTARTING, true);
				
				Thread objConnectionMonitorThread = new Thread(objMonitor);
				objConnectionMonitorThread.start();
								
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "WATCHRUNNERSTARTED" gesetzt und ein Event geworfen.
				
				bReturn = true;
				
				
				//TODOGOON20230929;
				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.
				//this.objServerBackend = (ServerMainZZZ) this.getServerBackendObject().getApplicationObject(); //new ServerMainZZZ(this.getKernelObject(), null);
				
				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Thread objThreadConfig = new Thread(this.getServerBackendObject());
				//objThreadConfig.start();

				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.
				//Merke: Dieser Monitor Thread muss mit dem Starten der einzelnen Unterthreads solange warten, bis das ServerMainZZZ-Object in seinem Flag anzeigt, dass es fertig mit dem Start ist.
				
				//Merke: Der MonitorThread wurde schon beim Verbinden gestartet. ... Diesen weiternutzen.
				//Thread objThreadMonitor = new Thread(objMonitor);
				//objThreadMonitor.start();
				
				//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
				//this.objClientBackend.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
					
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.								
				bReturn = true;
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ ez2) {
					System.out.println(ez.getDetailAllLast());
					ez2.printStackTrace();					
				}
			}		
		}
		return bReturn;		
	}
	
	
	/** Reads a status string from the ProcessMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readProcessMonitorStatusString(){
		String sReturn = "";
		if(this.objMonitorProcess!=null){
			sReturn = this.objMonitorProcess.getStatusString();
		}
		return sReturn;
	}
	
	/** Reads a status string from the ConnectionMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readConnectionMonitorStatusString(){
		String sReturn = "";
		if(this.objMonitorConnection!=null){
			sReturn = this.objMonitorConnection.getStatusString();
		}
		return sReturn;
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
				if (this.getClientBackendObject() == null || this.getClientBackendObject().getConfigChooserObject()==null || this.getClientBackendObject().getApplicationObject()==null){
					sReturn = ClientMainOVPN.STATUSLOCAL.ISLAUNCHED.getStatusMessage() + "(readStatusDetailString - objects NULL case)";
					break main;
				}
			}//END check
		
		String sStatusProcessMonitorString = this.readProcessMonitorStatusString();
		if(StringZZZ.isEmpty(sStatusProcessMonitorString)){
			sReturn = sReturn + "PROCESS: " + ClientMainOVPN.STATUSLOCAL.ISCONNECTNEW.getStatusMessage() + "\n";
		}else{
			//20200114: Erweiterung - Angabe des Rechnernamens
			try {				
					String sServerOrClient = this.getClientBackendObject().getConfigChooserObject().getOvpnContextUsed();
					sReturn = sReturn + sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
			} catch (UnknownHostException e) {				
				e.printStackTrace();
				ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
				throw ez;
			}
			
			sReturn = sReturn + "PROCESS: " + sStatusProcessMonitorString + "\n";
		}
		
		
		String sStatusConnectionMonitorString = this.readConnectionMonitorStatusString();
		if(StringZZZ.isEmpty(sStatusConnectionMonitorString)){
			sReturn = sReturn + "CONNECTION: " + ClientMainOVPN.STATUSLOCAL.WATCHRUNNERNEW.getStatusMessage() + "\n";
		}else{
			//20200114: Erweiterung - Angabe des Rechnernamens
			try {				
					String sServerOrClient = this.getClientBackendObject().getConfigChooserObject().getOvpnContextUsed();
					sReturn = sReturn + sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
			} catch (UnknownHostException e) {				
				e.printStackTrace();
				ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
				throw ez;
			}
			
			sReturn = sReturn + "CONNECTION: " + sStatusConnectionMonitorString + "\n";
		}
		
		
		
		if(this.getClientBackendObject().getFlag("useProxy")==true){
			sReturn = sReturn + "Proxy: " + this.getClientBackendObject().getApplicationObject().getProxyHost() + ":" + this.objClientBackend.getApplicationObject().getProxyPort() + "\n"; 					
		}else{
			sReturn = sReturn + "No proxy.\n";
		}
		
		stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getURL2Parse();
		if(stemp==null){
			sReturn = sReturn + "Parsed URL: NOT RECEIVED\n";
		}else{
			sReturn = sReturn + "Parsed URL: '" + stemp + "'\n";
		}
		
		//REMOTE
		stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getIpRemote();
		if(stemp==null){
			sReturn = sReturn + "Remote IP: Not found on URL.\n";
		}else{
			sReturn = sReturn + "Remote IP: '" + stemp + "'\n";
		}
		 
		if(this.getClientBackendObject().isPortScanEnabled()){			 
			stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getRemotePortScanned();
			if(stemp == null){
				sReturn = sReturn + "Remote Port(s): Not yet scanned.\n";
			}else{
				stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getRemotePortScanned();
				sReturn = sReturn + "Remote Port(s):" + stemp+"\n";
			}
		}
		
		//VPNIP, wird extra an die VpnIpEstablished-Property uebergeben, wenn eine Verbindung festgestellt wird.
		//this.getClientMainObject().getApplicationObject().getVpnIpRemote() //Das wäre aber noch keine erstellte Verbindung, sondern eher nur das Ziel.
		stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getVpnIpRemoteEstablished();
		if(stemp == null){
			sReturn = sReturn + "Remote VPN-IP: Not yet connected.\n";
		}else{
			sReturn = sReturn + "Remote VPN-IP: " + stemp + "\n";
			/* Logischer Fehler: Wenn die VPN-Verbindung erstellt worden ist, dann ist ggf. auch ein anderer Port "anpingbar" per meinem JavaPing.
			stemp = this.objConfig.getVpnPortEstablished();
			sReturn = sReturn + ":" + stemp;
			*/
		}
		
		if(this.getClientBackendObject().isPortScanEnabled()==true){
			stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getVpnPortScanned();
			if(stemp == null){
				sReturn = sReturn + "Remote VPN-IP Port(s): Not yet scanned.\n";
			}else{
				stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getVpnPortScanned();
				sReturn = sReturn + "Remote VPN-IP Port(s):" + stemp+"\n";
			}
		}
		
		String sTap = this.getClientBackendObject().getApplicationObject().getTapAdapterUsed();
		if(sTap==null){
			sTap = "-> TAP Adapter: Not defined in Kernel Ini-File.";
		}else{
			sTap = "-> TAP Adapter: '" + sTap + "'";
		}
		
		stemp = ((ClientApplicationOVPN)this.getClientBackendObject().getApplicationObject()).getVpnIpLocal();
		if(stemp==null){
			sReturn = sReturn + "Local VPN-IP: Not defined in Kernel Ini-File.\n\t\t" + sTap + "\n";
		}else{
			sReturn = sReturn + "Local VPN-IP: '" + stemp + "'\n\t\t" + sTap + "\n";
		}
		
		stemp = this.getClientBackendObject().getApplicationObject().getIpLocal();
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
				if (this.getClientBackendObject() == null){
					sReturn = ClientMainOVPN.STATUSLOCAL.ISLAUNCHED.getStatusMessage() + "(objClientBackend NULL case)";
					
						break main;
				}
			}//END check:
		
		ArrayList listaLogString = this.getClientBackendObject().getMessageStringAll();
		if(listaLogString.isEmpty()){
			if (this.objClientBackend == null){
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
	
	public ClientThreadProcessWatchMonitorOVPN  getProcessMonitorObject(){
		return this.objMonitorProcess;
	}
	public void setProcessMonitorObject(ClientThreadProcessWatchMonitorOVPN objMonitor){
		this.objMonitorProcess = objMonitor;
	}
	
	public ClientThreadConnectionVpnIpMonitorOVPN  getConnectionMonitorObject(){
		return this.objMonitorConnection;
	}
	public void setConnectionMonitorObject(ClientThreadConnectionVpnIpMonitorOVPN objMonitor){
		this.objMonitorConnection = objMonitor;
	}
	
	public void setClientBackendObject(IClientMainOVPN objClientBackend){
		this.objClientBackend = (ClientMainOVPN) objClientBackend;
	}
	public ClientMainOVPN getClientBackendObject(){
		return this.objClientBackend;
	}
	
//FGL Es scheint so als geht das nicht mit extra Klassen.
		public void actionPerformed(ActionEvent arg0) {
			try{
				String sCommand = arg0.getActionCommand();
				//System.out.println("Action to perform: " + sCommand);
				if(sCommand.equals(ClientTrayMenuTypeZZZ.END.getMenu())){
					this.unload();	
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.START.getMenu())){
					this.start();
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.CONNECT.getMenu())) {
					this.connect();
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.WATCH.getMenu())) {
					this.watch();	
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.LOG.getMenu())){
					//JOptionPane pane = new JOptionPane();
					String stemp = this.readLogString();
					//this.getTrayIconObject() ist keine Component ????
					JOptionPane.showMessageDialog(null, stemp, "Log der Verbindung", JOptionPane.INFORMATION_MESSAGE );
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.ADJUSTMENT.getMenu())) {
					//TODOGOON 20210210: Wenn es eine HashMap gäbe, dann könnte man diese über eine Methode 
					//                   ggfs. holen, wenn sie schon mal erzeugt worden ist.	
					
					 //Also In ClientMainUI
					//HashMap<String, KernelJDialogExtendedZZZ> hmContainerDialog....
					//
					//Also ClientMainUIZZZ implements Interface IClientMainUIZZZ
					//                                 mit der Methode HashMap<String, KernelJDialogExtendedZZZ> .getDialogs();
					//                                 mit der Methode KernelJDialogExtendedZZZ .getDialogByAlias(....)
				   
					
					//Also ClientTrayUIZZZ implements Interface IClientMainUIUserZZZ 
					//                               mit der Methode .getClientMainUI();
					//                                               .setClientMainUI(IClientMainUI objClientMain)
					//objMainUI = this.getClientMainUI
					//objMainUI.getDialogByAlias(....)
					
					//Bei CANCEL: Lösche diese Dialogbox, d.h. sie wird auch wieder komplett neu gemacht.
					//Neuer Button CLOSE: D.h. die Dialogbox wird geschlossen, aber wenn sie wieder neu geöffnet wird, 
					//                    dann sind ggfs. eingegebene Werte wieder da.
					
					if(this.dlgAdjustment==null || this.dlgAdjustment.isDisposed() ) {
					
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
						//       Sie ermöglicht die Konfiguration der anderen Module.
						HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
						hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
						DlgAdjustmentOVPN dlgAdjustment = new DlgAdjustmentOVPN(this.getKernelObject(), null, hmFlag);
						dlgAdjustment.setText4ButtonOk("USE VALUE");
						
						this.dlgAdjustment = dlgAdjustment;					
					}
										
					try {
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						dlgAdjustment.showDialog(null, "Adjustments for available Modules");
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: Dialog 'Adjustment'");
					} catch (ExceptionZZZ ez) {					
						System.out.println(ez.getDetailAllLast()+"\n");
						ez.printStackTrace();
						ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
					}				
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.PAGE_IP_READ.getMenu())) {
					
					//TODOGOON 20210210: Wenn es eine HashMap gäbe, dann könnte man diese über eine Methode 
					//                   ggfs. holen, wenn sie schon mal erzeugt worden ist.	
					
					 //Also In ClientMainUI
					//HashMap<String, KernelJDialogExtendedZZZ> hmContainerDialog....
					//
					//Also ClientMainUIZZZ implements Interface IClientMainUIZZZ
					//                                 mit der Methode HashMap<String, KernelJDialogExtendedZZZ> .getDialogs();
					//                                 mit der Methode KernelJDialogExtendedZZZ .getDialogByAlias(....)
				   
					
					//Also ClientTrayUIZZZ implements Interface IClientMainUIUserZZZ 
					//                               mit der Methode .getClientMainUI();
					//                                               .setClientMainUI(IClientMainUI objClientMain)
					//objMainUI = this.getClientMainUI
					//objMainUI.getDialogByAlias(....)
					
					//Bei CANCEL: Lösche diese Dialogbox, d.h. sie wird auch wieder komplett neu gemacht.
					//Neuer Button CLOSE: D.h. die Dialogbox wird geschlossen, aber wenn sie wieder neu geöffnet wird, 
					//                    dann sind ggfs. eingegebene Werte wieder da.
					
					if(this.dlgIPExternal==null || this.dlgIPExternal.isDisposed() ) {
					
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
						HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
						hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
						DlgIPExternalOVPN dlgIPExternal = new DlgIPExternalOVPN(this.getKernelObject(), null, hmFlag);
						dlgIPExternal.setText4ButtonOk("USE VALUE");
						
						this.dlgIPExternal = dlgIPExternal;					
					}
										
					try {
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						dlgIPExternal.showDialog(null, "Read IP External/Build Page");
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: Dialog 'Read IP External/Build Page'");
					} catch (ExceptionZZZ ez) {					
						System.out.println(ez.getDetailAllLast()+"\n");
						ez.printStackTrace();
						ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
					}
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.DETAIL.getMenu())){			//"PressAction": DAS SCHEINT EIN FEST VORGEGEBENER NAME VON JDIC zu sein für das Clicken AUF das Icon.		
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
				try {
					this.getKernelObject().getLogObject().WriteLineDate(ez.getDetailAllLast());
				} catch (ExceptionZZZ e1) {
					System.out.println(ez.getDetailAllLast());
					e1.printStackTrace();
				}
				try {
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ e2) {
					System.out.println(e2.getDetailAllLast());
					e2.printStackTrace();
				}
			}
		}

		//+++ Aus IListenerObjectFlagZsetZZZ
		@Override
		public boolean flagChanged(IEventObjectFlagZsetZZZ eventFlagZset) throws ExceptionZZZ {
			boolean bReturn=false;
			main:{
				Enum objFlagEnum = eventFlagZset.getFlagEnum();
				if(objFlagEnum==null) break main;
				
				boolean bFlagValue = eventFlagZset.getFlagValue();
				if(bFlagValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.

			}//end main:
			return bReturn;
		}
		
		//+++ Aus IListenerObjectStatusLocalSetOVPN
		@Override
		public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			boolean bReturn=false;
			main:{
				STATUSLOCAL objStatusEnum = (STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
				if(objStatusEnum==null) break main;
				
				//Falls nicht zuständig, mache nix
			    boolean bProof = this.isStatusLocalRelevant(eventStatusLocalSet);
				if(!bProof) break main;
				
				boolean bStatusValue = eventStatusLocalSet.getStatusValue();
				if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
				
				//20230818: Die Meldungen werden nun direkt aus dem ServerMonitor-Objekt ausgelesen, welches sich auch am BackenServer - Registriert hat.
				//Nimm erst einmal die Meldung vom Server entgegen.
				//String sMessage = objStatusEnum.getStatusMessage();
				//System.out.println(ReflectCodeZZZ.getPositionCurrent()+": " + sMessage);			

				//Die Stati vom Backend-Objekt mit dem TrayIcon mappen
				if(ClientMainOVPN.STATUSLOCAL.ISLAUNCHED==objStatusEnum) {
					this.switchStatus(ClientTrayStatusTypeZZZ.NEW);				
				}else if(ClientMainOVPN.STATUSLOCAL.ISSTARTING==objStatusEnum) {
					this.switchStatus(ClientTrayStatusTypeZZZ.STARTING);
				}else if(ClientMainOVPN.STATUSLOCAL.ISSTARTED==objStatusEnum) {
					this.switchStatus(ClientTrayStatusTypeZZZ.STARTED);
				}else if(ClientMainOVPN.STATUSLOCAL.ISCONNECTING==objStatusEnum) {
					this.switchStatus(ClientTrayStatusTypeZZZ.CONNECTING);
				}else if(ClientMainOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
				
					//Ggfs. vorhandene Aenderungen aus dem Backend-Application-Objekt des Events holen, bzw. hier das ganze Backen-Objekt austauschen
					//Dann kann sich z.B. die Datailinfo-box die aktuellsten Werte daraus holen.
					IApplicationOVPN objApplicationUsed = eventStatusLocalSet.getApplicationObjectUsed();
					if(objApplicationUsed!=null) {
						this.getClientBackendObject().setApplicationObject(objApplicationUsed);
					}
					this.switchStatus(ClientTrayStatusTypeZZZ.CONNECTED);
					
					
//			}else if(ClientMainOVPN.STATUSLOCAL.PortScanAllFinished==objStatusEnum) {
//					this.switchStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.CONNECTED);
				}else if(ClientMainOVPN.STATUSLOCAL.HASERROR==objStatusEnum) {
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				}else {
					String sLog = "Der Status wird nicht behandelt - '"+objStatusEnum.getAbbreviation()+"'.";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
					this.logLineDate(sLog);
					break main;
				}
							
				bReturn = true;
			}//end main:
			return bReturn;
		}

		@Override
		public boolean isStatusLocalRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			
			//fuer den Tray ist jeder Status relevant. 
			return true;
		}

}//END Class
