package use.openvpn.serverui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.abstractList.ArrayListZZZ;
import basic.zBasic.util.datatype.enums.EnumSetUtilZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.ResourceEasyZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.flag.IListenerObjectFlagZsetZZZ;
import basic.zKernel.status.IStatusBooleanZZZ;
import basic.zKernel.status.IStatusLocalMapForStatusLocalUserZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.util.JTextFieldHelperZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;
import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigFileZZZ;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;
import use.openvpn.clientui.IClientTrayMenuZZZ;
import use.openvpn.clientui.IClientStatusMappedValueZZZ.ClientTrayStatusTypeZZZ;
import use.openvpn.server.IServerMainOVPN;
import use.openvpn.server.IServerMainOVPN.STATUSLOCAL;
import use.openvpn.server.ServerApplicationOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.serverui.IServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ;
import use.openvpn.serverui.component.FTPCredentials.DlgFTPCredentialsOVPN;
import use.openvpn.serverui.component.FTPCredentials.IConstantProgramFTPCredentialsOVPN;
import use.openvpn.serverui.component.IPExternalUpload.DlgIPExternalOVPN;

public class ServerTrayUIOVPN extends AbstractKernelUseObjectZZZ implements ActionListener, IListenerObjectFlagZsetZZZ, IListenerObjectStatusLocalSetOVPN, IStatusLocalMapForStatusLocalUserZZZ {		
	private static final long serialVersionUID = 4170579821557468353L;
		
	private SystemTray objTray = null;                                    //Das gesamte SystemTray von Windows
	private TrayIcon objTrayIcon = null;                                 //Das TrayIcon dieser Application	
	private ServerMainOVPN objMain = null;                            //Ein Thread, der die OpenVPN.exe mit der gew�nschten Konfiguration startet.
	
	//Merke: Der Tray selbst hat keinen Status. Er nimmt aber Statusaenderungen vom Main-Objekt entgegen und mapped diese auf sein "Aussehen"
	//       Wie in AbstractObjectWithStatusListeningZZZ wird für das Mappen des reinkommenden Status auf ein Enum eine Hashmap benötigt.
	private HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ> hmEnumSet =null; //Hier wird ggfs. der Eigene Status mit dem Status einer anderen Klasse (definiert durch das Interface) gemappt.
	
	//TODOGOON 20210210: Realisiere die Idee
	//Idee: In ClientMainUI eine/verschiedene HashMaps anbieten, in die dann diese Container-Objekte kommen.
	//      Dadurch muss man sie nicht als Variable deklarieren und kann dynamischer neue Dialogboxen, etc. hinzufügen.
	//Ziel diese hier als Variable zu deklarieren ist: Die Dialogbox muss nicht immer wieder neu erstellt werden.
	private KernelJDialogExtendedZZZ dlgIPExternal=null;
	private KernelJDialogExtendedZZZ dlgFTPCredentials=null;
	
	public ServerTrayUIOVPN(IKernelZZZ objKernel, ServerMainOVPN objServerMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
		ServerTrayUINew_(objServerMain);
	}
	
	public ServerTrayUIOVPN(IKernelZZZ objKernel, IServerMainOVPN objServerMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
		ServerTrayUINew_(objServerMain);
	}
	
	private void ServerTrayUINew_(IServerMainOVPN objServerMain) throws ExceptionZZZ{
		main:{
			
			//try{		
			check:{
				if(this.getFlag("init")) break main;
				if(objServerMain==null){
					ExceptionZZZ ez = new ExceptionZZZ("ServerMain-Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName()); 					 
					throw ez;		 
				}else{
					this.objMain = (ServerMainOVPN) objServerMain;
				}
			}//End check
			this.objMain = (ServerMainOVPN) objServerMain;
	
			//Dieses muss beim Beenden angesprochen werden, um das TrayIcon wieder zu entfernen
			this.objTray = SystemTray.getDefaultSystemTray();
			
			JPopupMenu menu = new JPopupMenu();
			
			JMenuItem menueeintrag2 = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START.getMenu());
			menu.add(menueeintrag2);
			menueeintrag2.addActionListener(this);
			
			JMenuItem menueeintrag2b = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN.getMenu());
			menu.add(menueeintrag2b);
			menueeintrag2b.addActionListener(this);
			
			JMenuItem menueeintrag3 = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PROTOCOL.getMenu());
            menu.add(menueeintrag3);
			menueeintrag3.addActionListener(this);
			
			JMenuItem menueeintragFTPCredentials = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.FTP_CREDENTIALS.getMenu());
            menu.add(menueeintragFTPCredentials);
			menueeintragFTPCredentials.addActionListener(this);
			
			JMenuItem menueeintragIPPage = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PAGE_IP_UPLOAD.getMenu());
            menu.add(menueeintragIPPage);
			menueeintragIPPage.addActionListener(this);
					
			JMenuItem menueeintrag = new JMenuItem(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.END.getMenu());	
			menu.add(menueeintrag);		
			menueeintrag.addActionListener(this);
			
			//DUMMY Einträge, sofort erkennen ob Server / Client
			//DUMMY Einträge, damit der unterste Eintrag ggfs. nicht durch die Windows Taskleiste verdeckt wird
			JMenuItem menueeintragLine = new JMenuItem("------------------");
			menu.add(menueeintragLine);
			//Kein actionListener für Dummy Eintrag
						
			JMenuItem menueeintragContext = new JMenuItem("RUNNING AS SERVER");
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

		
			ImageIcon objIcon = this.getImageIconByStatus(ServerTrayStatusTypeZZZ.NEW);			
			this.objTrayIcon = new TrayIcon(objIcon, "OVPNListener", menu);
			this.objTrayIcon.addActionListener(this);			
			this.objTray.addTrayIcon(this.objTrayIcon);
			
			//Den Process-Monitor auch schon vorbereiten, auch wenn ggfs. nicht schon am Anfang auf die Verbindung "gelistend" wird.
			//Er wird später auch am Backend-Objekt registriert, um dort Änderungen mitzubekommen.
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ServerMonitorRunner-Object";
			System.out.println(sLog);
			this.getLogObject().WriteLineDate(sLog);
			
			//### Registriere das Tray-Objekt selbst ##############
			//a) Fuer Aenderungen an den Main-Objekt-Flags. Das garantiert, das der Tray auch auf Änderungen der Flags reagiert, wenn ServerMain in einem anderen Thread ausgeführt wird.			
			this.getMainObject().registerForFlagEvent(this);
			
			//b) Fuer Aenderung am Main-Objekt-Status. Das garantiert, das der Tray auch auf Änderungen des Status reagiert, wenn ServerMain in einem anderen Thread ausgeführt wird.
			this.getMainObject().registerForStatusLocalEvent(this);
		}//END main
	}
	
	//public static ImageIcon getImageIconByStatus(Enum enumSTATUS)throws ExceptionZZZ{
	public static ImageIcon getImageIconByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ enumSTATUS)throws ExceptionZZZ{	
		ImageIcon objReturn = null;
		main:{
			URL url = null;
			ClassLoader objClassLoader = ServerTrayUIOVPN.class.getClassLoader(); 
			if(objClassLoader==null) {
				ExceptionZZZ ez = new ExceptionZZZ("unable to receiver classloader object", iERROR_RUNTIME, ServerTrayUIOVPN.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
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
				ExceptionZZZ ez = new ExceptionZZZ(sLog, iERROR_RUNTIME, ServerTrayUIOVPN.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": URL = '"+url.toExternalForm() + "'");
			}
			objReturn = new ImageIcon(url);
		}//END main:
		return objReturn;
	}
		
	public boolean switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ{	
		boolean bReturn = false;
		main:{
			//ImageIcon aendern
			ImageIcon objIcon = this.getImageIconByStatus(enumSTATUS);
			if(objIcon==null)break main;
			
			//+++++ Test: Logge den Menüpunkt			
			IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ objEnum = (IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ) enumSTATUS.getAccordingTrayMenuType();
			if(objEnum!=null){
				String sLog = ReflectCodeZZZ.getPositionCurrent() +": Menuepunkt=" + objEnum.getMenu();
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
			}else {
				String sLog = ReflectCodeZZZ.getPositionCurrent() +": Kein Menuepunkt vorhanden.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
			}
			//++++++++++++++++++++++++++++++++
				
			this.getTrayIconObject().setIcon(objIcon);

			bReturn = true;
		}//END main:
		return bReturn;
	}
	

	
	

	/**Loads an icon in the systemtray. 
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
			//+++ Vorbereitend den prozessnamen auslesen
			File objFileExe = ClientConfigFileZZZ.findFileExe();
			if(objFileExe!=null){
				String sExeCaption = objFileExe.getName();
				//+++ Wenigstens beende ich nun alle openvpn.exe - Processe (DAZU WIRD JACOB (Java COM Bridge) verwendet.	
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
	
	public boolean start() throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			check:{
			if(this.getMainObject()==null)break main;
			}
			
			//Wenn das so ohne Thread gestartet wird, dann reagiert der Tray auf keine weiteren Clicks.
			//Z.B. den Status anzuzeigen.
			//this.getServerBackendObject().start(this);
			
			//Also dies über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
			//Problem dabei: Der SystemTray wird nicht aktualisiert.
			Thread objThreadMain = new Thread(this.getMainObject());
			objThreadMain.start();
			
			bReturn = true;
		}//end main:
		return bReturn;
	}

	
	/**
	 * @return
	 * @author Fritz Lindhauer, 24.05.2023, 08:07:21
	 */
	public boolean listen() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			try{ 
				if(this.getMainObject()==null)break main;
								
				ServerThreadProcessWatchMonitorOVPN objMonitor = this.getMainObject().getProcessMonitorObject();
				if(objMonitor==null) break main;
				
				boolean bStarted = this.getMainObject().getStatusLocal(ServerMainOVPN.STATUSLOCAL.ISSTARTED);
				if(!bStarted) {
					
					boolean bStarting = this.getMainObject().getStatusLocal(ServerMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bStarting) {	
						objMonitor.offerStatusLocalEnum(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASSERVERNOTSTARTING);
						break main;
					}
					objMonitor.offerStatusLocalEnum(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASSERVERNOTSTARTED);						
					break main;
				}
				
				Thread objThreadMonitor = new Thread(objMonitor);
				objThreadMonitor.start();
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.
				
				bReturn = true;
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ ez2) {
					System.out.println(ez.getDetailAllLast());
					ez2.printStackTrace();					
				}
			}		
		}
		return bReturn;
	}
	
	/** Reads a status string from the Backend-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readBackendStatusString(){
		String sReturn = "";
		if(this.getMainObject()!=null){
			sReturn = this.getMainObject().getStatusLocalAbbreviation();
		}else {	
			sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getAbbreviation();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
		}
		return sReturn;
	}
	
	/** Reads a status string from the Backend-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readBackendStatusMessage(){
		String sReturn = "";
		if(this.getMainObject()!=null){
			sReturn = this.getMainObject().getStatusLocalMessage();
		}else {	
			sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
		}
		return sReturn;
	}
	
	/** Reads a status string from the ProcessMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readProcessMonitorStatusString(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getProcessMonitorObject()==null) break main;
			
			sReturn = this.getMainObject().getProcessMonitorObject().getStatusLocalAbbreviation();
		}//end main:
		return sReturn;
	}
	
	/** Reads a status string from the ProcessMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readProcessMonitorStatusMessage(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getProcessMonitorObject()==null) break main;
			
			sReturn = this.getMainObject().getProcessMonitorObject().getStatusLocalMessage();
		}//end main:
		return sReturn;
	}
	
	
	/**TODO Describe what the method does
	 * @return, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 21.07.2006 - 11:02:02
	 */
	public String readStatusDetailString() throws ExceptionZZZ{
		String sReturn = "";
		main:{
			if (this.objMain == null){
				sReturn = "No Server Backend available.";
				break main;
			}

			String stemp = null;
			
			//Merke: der BackendStausString wird von den Events, die er empfaengt gefuettert.
			//Hole immer die letzte StatusMeldung...
			String sStatusServerString = this.readBackendStatusMessage();
			if(StringZZZ.isEmpty(sStatusServerString)){				
				sReturn = sReturn + ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "\n";
				break main;
			}else{			
				sReturn = sReturn + sStatusServerString + "\n";
			}
			
			//Hole die Meldung vom OVPN-Monitor
			String sStatusProcessMonitorString = this.readProcessMonitorStatusMessage();
			if(StringZZZ.isEmpty(sStatusProcessMonitorString)){
				sReturn = sReturn + ClientMainOVPN.STATUSLOCAL.ISCONNECTNEW.getStatusMessage() + "\n";
				break main;
			}else{
				//Falls der Client gestartet wurde, hole die Statusmeldung aus dem Backend-Monotor-Objekt. Also neu setzen.
				sStatusProcessMonitorString = this.getMainObject().getProcessMonitorObject().getStatusLocalMessage() + "\n";
								
				//20200114: Erweiterung - Angabe des Rechnernamens
				try {																
					//Falls der Server gestartet wurde, gib den Rechnernamen aus, anstatt einer "ist gestartet" Meldung. Spart Platz.
					String sServerOrClient = this.getMainObject().getConfigChooserObject().getOvpnContextUsed();					
					sReturn = sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
				} catch (UnknownHostException e) {				
					e.printStackTrace();
					ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
					throw ez;
				}
				
				sReturn = sStatusProcessMonitorString + sReturn;
			}
					
			ServerApplicationOVPN objApplication = (ServerApplicationOVPN) this.getMainObject().getApplicationObject();
			stemp = objApplication.getURL2Parse();
			if(stemp==null){
				sReturn = sReturn + "URL: NOT RECEIVED\n";
			}else{
				stemp = StringZZZ.right("http://" + stemp, "http://", false);
				stemp = StringZZZ.abbreviateDynamic(stemp,40);
				sReturn = sReturn + "URL: " + stemp + "\n";
			}
			
			//REMOTE
			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getIpRemote();
			if(stemp==null){
				sReturn = sReturn + "My ext. IP: Not found on URL.|";
			}else{			
				sReturn = sReturn + "My ext. IP: " + stemp + "|";
			}
			 
			stemp = this.getMainObject().getApplicationObject().getIpLocal();
			if(stemp==null){
				sReturn = sReturn + "-> Local IP: Not availabel.\n";
			}else{
				sReturn = sReturn + "-> Local IP: " + stemp + "\n";
			}
			
			String sTap = this.getMainObject().getApplicationObject().getTapAdapterUsed();
			if(sTap==null){
				sTap = "->TAP: Not defined in Kernel Ini-File.";
			}else{
				sTap = "->TAP: " + sTap ;
			}

			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpLocal();
			if(stemp==null){
				sReturn = sReturn + "VPN-IP: Not defined in Kernel Ini-File.|" + sTap + "\n";
			}else{
				sReturn = sReturn + "VPN-IP: " + stemp + "|" + sTap + "\n";
			}
			
			
			//VPNIP, wird extra an die VpnIpEstablished-Property uebergeben, wenn eine Verbindung festgestellt wird.
			//this.getClientMainObject().getApplicationObject().getVpnIpRemote() //Das wäre aber noch keine erstellte Verbindung, sondern eher nur das Ziel.
			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpRemoteEstablished();
			if(stemp == null){
				sReturn = sReturn + "\t\t|->VPN-IP: Not connected.\n";
			}else{
				sReturn = sReturn + "\t\t|->VPN-IP: " + stemp + "\n";
				/* Logischer Fehler: Wenn die VPN-Verbindung erstellt worden ist, dann ist ggf. auch ein anderer Port "anpingbar" per meinem JavaPing.
				stemp = this.objConfig.getVpnPortEstablished();
				sReturn = sReturn + ":" + stemp;
				*/
			}
		}//END main
		return sReturn;
	}
	
//	/** Reads the HashMap with detailed information from the ServerMonitor-object-Thread
//	* @return HashMap
//	* 
//	* lindhaueradmin; 10.08.2006 11:06:52
//	 */
//	public HashMap readStatusDetailHashMap(){
//		HashMap hmReturn = null;
//		ServerThreadProcessWatchMonitorOVPN objMonitor = this.getM.getMonitorObject();
//		if(objMonitor!=null){
//			hmReturn = objMonitor.getStatusDetailHashMap();
//		}
//		return hmReturn;
//	}
	
	public String readProtocolString(){
		String sReturn = "";
		main:{
			check:{
				if (this.objMain == null){
					sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "(objServerBackend NULL case)";
					break main;
				}
			}//END check:
		 
		ArrayList listaLogString = this.objMain.getProtocolStringAll();
		if(listaLogString.isEmpty()){
			if (this.objMain == null){
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
	
	
//	/** Reads a status string from the ServerMonitor-Object-Thread
//	* @return String
//	* 
//	* lindhaueradmin; 10.08.2006 11:32:16
//	 */
//	public String readStatusString(){
//		String sReturn = "";
//		if(this.objMonitor!=null){
//			sReturn = this.objMonitor.getStatusString();
//		}
//		return sReturn;
//	}
	
	
	//#######################
	//### GETTER / SETTER
	public TrayIcon getTrayIconObject(){
		return this.objTrayIcon;
	}
	
	public void setMainObject(IServerMainOVPN objServerBackend){
		this.objMain = (ServerMainOVPN) objServerBackend;
	}
	public ServerMainOVPN getMainObject(){
		return this.objMain;
	}

	public void actionPerformed(ActionEvent arg0) {
		try{
			String sCommand = arg0.getActionCommand();
			//System.out.println("Action to perform: " + sCommand);
			if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.END.getMenu())){
				this.unload();	
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START.getMenu())){
				boolean bFlagValue = this.start();					
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN.getMenu())) {
				boolean bFlagValue = this.listen();
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PROTOCOL.getMenu())){
				//JOptionPane pane = new JOptionPane();
				String stemp = this.readProtocolString();
				//this.getTrayIconObject() ist keine Component ????
				JOptionPane.showMessageDialog(null, stemp, "Log des OVPN Connection Listeners", JOptionPane.INFORMATION_MESSAGE );
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PAGE_IP_UPLOAD.getMenu())) {
				
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
					HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
					hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
					DlgIPExternalOVPN dlgIPExternal = new DlgIPExternalOVPN(this.getKernelObject(), null, hmFlag);
					//dlgIPExternal.setText4ButtonOk("USE VALUE");	
					this.dlgIPExternal = dlgIPExternal;
				}
				try {
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
					this.dlgIPExternal.showDialog(null, "Build and Upload IP Page");
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: 'Build and Upload IP Page'");
				} catch (ExceptionZZZ ez) {					
					System.out.println(ez.getDetailAllLast()+"\n");
					ez.printStackTrace();
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
				}
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.FTP_CREDENTIALS.getMenu())) {					
				if(this.dlgFTPCredentials==null || this.dlgFTPCredentials.isDisposed() ) {									
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;					
					HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
					hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
					
					HashMap<String,Boolean>hmFlagLocal=new HashMap<String,Boolean>();
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_CANCEL.name(), false);
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_CLOSE.name(), true);
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_OK.name(), false);
					DlgFTPCredentialsOVPN dlgFTPCredentials = new DlgFTPCredentialsOVPN(this.getKernelObject(), null, hmFlagLocal, hmFlag);
					dlgFTPCredentials.setText4ButtonOk("USE VALUES");	
					this.dlgFTPCredentials = dlgFTPCredentials;
				}
				try {
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
					this.dlgFTPCredentials.showDialog(null, "FTP Credentials");
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: 'FTP Credentials'");
					
					//Versuch den Focus und ein markiertes Feld zu setzen
					//DAS GEHT NUR AN DIESER STELLE, NACHDEM DER DIALOG SCHON GESTARTET IST
					//UND DER STARTWERT SCHON GESETZT WURDE
					//UND DAS GEHT AUCH NUR FUER 1 TEXTFIELD
//						String sTextfield4Update1 =IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_USERNAME;
//						JTextField textField1 = (JTextField) this.dlgFTPCredentials.getPanelContent().searchComponent(sTextfield4Update1);
//						if(textField1!=null) {
//							//textField.setText(sText2Update);					
//							JTextFieldHelperZZZ.markAndFocus(this.dlgFTPCredentials.getPanelContent(),textField1);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.
//						}else {
//							ReportLogZZZ.write(ReportLogZZZ.DEBUG, "JTextField '" + sTextfield4Update1 + "' NOT FOUND in panel '" + this.dlgFTPCredentials.getPanelContent().getClass() + "' !!!");										
//						}
					
					String sTextfield4Update2 =IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED;
					JTextField textField2 = (JTextField) this.dlgFTPCredentials.getPanelContent().searchComponent(sTextfield4Update2);
					if(textField2!=null) {											
						JTextFieldHelperZZZ.markAndFocus(this.dlgFTPCredentials.getPanelContent(),textField2);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.
					}else {
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "JTextField '" + sTextfield4Update2 + "' NOT FOUND in panel '" + this.dlgFTPCredentials.getPanelContent().getClass() + "' !!!");										
					}
				} catch (ExceptionZZZ ez) {					
					System.out.println(ez.getDetailAllLast()+"\n");
					ez.printStackTrace();
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
				}
				
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.DETAIL.getMenu())){		
				String stemp = this.readStatusDetailString();
				if(stemp!= null){
					if(objTrayIcon!=null) objTrayIcon.displayMessage("Status des OVPN Connection Listeners.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
				}else{
					if(objTrayIcon!=null) objTrayIcon.displayMessage("Status des OVPN Connection Listeners.", "unable to receive any status.", TrayIcon.INFO_MESSAGE_TYPE);
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
			try {
				//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zur�ckgesetzt werden kann.				
				ez.printStackTrace();
				String stemp = ez.getDetailAllLast();
				this.getKernelObject().getLogObject().WriteLineDate(stemp);
				System.out.println(stemp);
				this.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);
			} catch (ExceptionZZZ ez2) {					
				System.out.println(ez.getDetailAllLast());
				ez2.printStackTrace();
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
		//Der Tray ist am MainObjekt registriert.
		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
		boolean bReturn=false;
		main:{	
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			boolean bRelevant = this.isEventRelevant(eventStatusLocalSet); 
			if(!bRelevant) {
				sLog = 	ReflectCodeZZZ.getPositionCurrent() + ": Event / Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
			}
			
			
			/* Loesung: DOWNCASTING mit instanceof , s.: https://www.positioniseverything.net/typeof-java/
		 	class Animal { }
			class Dog2 extends Animal {
				static void method(Animal j) {
				if(j instanceof Dog2){
				Dog2 d=(Dog2)j;//downcasting
				System.out.println(“downcasting done”);
				}
				}
				public static void main (String [] args) {
				Animal j=new Dog2();
				Dog2.method(j);
				}
			}
		 */
			
			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen
			if(eventStatusLocalSet.getStatusEnum() instanceof IServerMainOVPN.STATUSLOCAL){					
				bReturn = this.statusLocalChangedMainEvent_(eventStatusLocalSet);
				break main;
				
			}
//			else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 02");
//				bReturn = this.statusLocalChangedMonitorEvent_(eventStatusLocalSet);
//				break main;
//			}
			else {	
				sLog = ReflectCodeZZZ.getPositionCurrent() +" : Status-Enum wird von der Klasse her nicht betrachtet.";
				System.out.println(sLog);	
				this.getMainObject().logProtocolString(sLog);
			}											
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	/** Merke: Diese private Methode wird nach ausführlicher Prüfung aufgerufen, daher hier mehr noetig z.B.:
	 * - Keine Pruefung auf NULLL
	 * - kein instanceof 
	 * @param eventStatusLocalSet
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 19.10.2023, 09:43:19
	 */
	private boolean statusLocalChangedMainEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer MainEvent.";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
			STATUSLOCAL objStatusEnum = (STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
			if(objStatusEnum==null) break main;
				
			//Falls nicht zuständig, mache nix
			boolean bProof = this.isEventRelevant(eventStatusLocalSet);
			if(!bProof) break main;
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
				
			//+++ Weiterverarbeitung des relevantenStatus. Merke: Das ist keine CascadingStatus-Enum. Sondern hier ist nur der Bilddateiname drin.
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hmEnum	= this.getHashMapEnumSetForStatusLocal();		
			ServerTrayStatusTypeZZZ objEnumForTray = (ServerTrayStatusTypeZZZ) hmEnum.get(objStatusEnum);			
			if(objEnumForTray==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
			}			
		
			
			
				
			//########################################	
			//### Erneute Verarbeitung mit einem frueheren Status:
			//### Setze den Status auf einen anderen Status zurueck
			//########################################	
			if(objEnumForTray == ServerTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE) {
				//++++++++ TESTEN - Ermittle u.a. die StatusGroupIds über alle vorherigen Events...
				this.getMainObject().debugCircularBufferStatusLocalMessage();				
				//+++ TESTENDE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					

				//++++++++ TESTEN - Ermittle die Indexwerte der aktuellen GroupId im CircularBuffer
				int iGroupIdCurrent = this.getMainObject().getStatusLocalGroupIdFromCurrent();
				int iIndexLower = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index der GroupId " + iGroupIdCurrent +" ist="+iIndexLower;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				int iIndexLowerInterrupted = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index (interrupted) der GroupId " + iGroupIdCurrent +" ist="+iIndexLowerInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				int iIndexUpper = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index der GroupId " + iGroupIdCurrent +" ist="+iIndexUpper;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	

				int iIndexUpperInterrupted = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index(interrupted) der GroupId " + iGroupIdCurrent +" ist="+iIndexUpperInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				

				//++++++++ TESTEN - Ermittle die vorherige GroupId
				int iGroupIdPreviousDifferentFromCurrent = this.getMainObject().searchStatusLocalGroupIdPreviousDifferentFromCurrent();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Die vorherige, andere GroupId ist = " + iGroupIdPreviousDifferentFromCurrent +".";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	
				//+++ TESTENDE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				
				//++++++++ TESTEN - Ermittle die Indexwerte der vorherigen GroupId im CircularBuffer
				iIndexLower = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdPreviousDifferentFromCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexLower;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				iIndexLowerInterrupted = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdPreviousDifferentFromCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index (interrupted) der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexLowerInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				iIndexUpper = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdPreviousDifferentFromCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexUpper;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	

				iIndexUpperInterrupted = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdPreviousDifferentFromCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index(interrupted) der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexUpperInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	
				
				
				
				//Nun kann man aus der ermittelten Liste den ersten Eintrag nehmen
				ArrayList<IStatusBooleanZZZ>listaObjStatusLocalPrevious = this.getMainObject().searchStatusLocalGroupById(iGroupIdPreviousDifferentFromCurrent, true);
				IStatusBooleanZZZ objStatusLocalPrevious = (IStatusBooleanZZZ) ArrayListZZZ.getFirst(listaObjStatusLocalPrevious);					
				if(objStatusLocalPrevious!=null) {
					objEnumForTray = (ServerTrayStatusTypeZZZ) hmEnum.get(objStatusLocalPrevious.getEnumObject());			
					if(objEnumForTray==null) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);
						break main;
					}else {
						//Erst einmal den gefundenen Status neu hinzufügen. Damit er auch bei einem weiteren "rueckwaerts Suchen" in der Liste auftaucht.
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Nimm den gefundenen Status in die Liste als neuen Status auf: '" + objEnumForTray.getAbbreviation() + "'";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);							
						this.getMainObject().offerStatusLocal((Enum) objStatusLocalPrevious.getEnumObject(), "", true);											
					}	
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen Status aus dem Event-Objekt erhalten. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
						
				//ggfs. noch weiter schrittweise zurück, wenn der vorherige Status ein Steuerevent war...,d.h. ohne Icon
//				if(objStatusLocalPrevious!=null) {
//					objEnum = (ClientTrayStatusTypeZZZ) hmEnum.get(objStatusLocalPrevious);			
//					if(objEnum!=null) {					
//						if(StringZZZ.isEmpty(objEnum.getIconFileName())){
//							iStepsToSearchBackwards=1;
//							
//							sLog = ReflectCodeZZZ.getPositionCurrent()+": Steuerevent als gemappten Status aus dem Event-Objekt erhalten. Gehe noch einen weitere " + iStepsToSearchBackwards + " Schritt(e) zurueck.";
//							System.out.println(sLog);
//							this.getMainObject().logProtocolString(sLog);							
//						}else {
//							bGoon=true;
//						}
//					}
//				}

				
				
				//#############################					
				//Frage nach dem Status im Backend nach...
				IEnumSetMappedStatusZZZ objStatusLocalCurrent = this.getMainObject().getStatusLocalEnumCurrent();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der aktuelle Status im Main ist '" + objStatusLocalCurrent.getAbbreviation()+"'.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
			
				int iGroupIdPrevious = this.getMainObject().getStatusLocalGroupIdFromPrevious();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Die vorherige GroupId ist= " + iGroupIdPrevious +".";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
					
			}//if(objEnum == ClientTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE) {					
			this.switchStatus(objEnumForTray); //Merke: Der Wert true wird angenommen.
			
			bReturn = true;
		}//end main:
		return bReturn;
	}
	

	@Override
	public boolean isEventRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatusFromEvent = eventStatusLocalSet.getStatusEnum();				
			if(enumStatusFromEvent==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);							
				break main;
			}
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen. Wert: " + bStatusValue;
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus hat class='"+enumStatusFromEvent.getClass()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus='" + enumStatusFromEvent.getAbbreviation()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			
			//#### Problemansatz: Mappen des Lokalen Status auf einen Status aus dem Event, verschiedener Klassen.
			String sStatusAbbreviationLocal = null;
			IEnumSetMappedZZZ objEnumStatusLocal = null;

			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hm=this.createHashMapEnumSetForStatusLocalCustom();
			objEnumStatusLocal = hm.get(enumStatusFromEvent);					
			//###############################
			
			if(objEnumStatusLocal==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping nicht mit Wert vorhanden. Damit nicht relevant.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
				//sStatusAbbreviationLocal = enumStatusFromEvent.getAbbreviation();
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping mit Wert vorhanden. Damit relevant.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				sStatusAbbreviationLocal = objEnumStatusLocal.getAbbreviation();
			}
			
			//+++ Pruefungen
			bReturn = this.isEventRelevantByClass(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
//für den Tray nicht relevant
//			bReturn = this.isStatusLocalChanged(sStatusAbbreviationLocal, bStatusValue);
//			if(!bReturn) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}else {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status geaendert. Mache weiter.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//			}
			
			//dito gibt es auch die Methode isStatusLocalRelevant(...) nicht, da der Tray kein AbstractObjectWithStatus ist, er verwaltet halt selbst keinen Status.
			
						
			bReturn = this.isEventRelevantByStatusLocalValue(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByStatusLocalValue(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}

						
			bReturn = true;
		}//end main:
		return bReturn;

	}

	@Override
	public boolean isEventRelevantByClass(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		return true;
	}

	@Override
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		return true;
	}

	@Override
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen. Wert: " + bStatusValue;
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
		
			if(!bStatusValue)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
			
			bReturn = true;
		}
		return bReturn;
	}
	
	
	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> getHashMapEnumSetForStatusLocal() {
		if(this.hmEnumSet==null) {
			this.hmEnumSet = this.createHashMapEnumSetForStatusLocalCustom();
		}
		return this.hmEnumSet;
	}

	@Override
	public void setHashMapEnumSetForStatusLocal(HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> hmEnumSet) {
		this.hmEnumSet = hmEnumSet;
	}
	
	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> createHashMapEnumSetForStatusLocalCustom() {
		HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hmReturn = new HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>();
		main:{
			
			//Reine Lokale Statuswerte kommen nicht aus einem Event und werden daher nicht gemapped. 
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISSTARTNEW, ServerTrayStatusTypeZZZ.NEW);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISSTARTING, ServerTrayStatusTypeZZZ.STARTING);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISSTARTED, ServerTrayStatusTypeZZZ.STARTED);
			
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTNEW, ServerTrayStatusTypeZZZ.STARTED);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTING, ServerTrayStatusTypeZZZ.LISTENERSTARTING);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTED, ServerTrayStatusTypeZZZ.LISTENERSTARTED);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTNO, ServerTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE);//Wieder einen Status im Menue zurueckgehen
			
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERCONNECTED, ServerTrayStatusTypeZZZ.CONNECTED);
			hmReturn.put(IServerMainOVPN.STATUSLOCAL.ISLISTENERINTERRUPTED, ServerTrayStatusTypeZZZ.INTERRUPTED);
			
			
			//++++++++++++++++++++++++
			//Berechne den wirklichen Typen anschliessend, dynamisch. Es wird auf auf einen vorherigen Event zugegriffen durch eine zweite Abfrage
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED, ClientTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE);
			
			//+++++++++++++++++++++++
			
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.HASPINGERROR, ClientTrayStatusTypeZZZ.FAILED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.HASERROR, ClientTrayStatusTypeZZZ.ERROR);
		}//end main:
		return hmReturn;
	}
}//END Class

