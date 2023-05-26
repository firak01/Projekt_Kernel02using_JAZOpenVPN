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

import use.openvpn.client.ClientConfigFileZZZ;
import use.openvpn.server.ServerMainZZZ;
import use.openvpn.serverui.component.FTPCredentials.DlgFTPCredentialsOVPN;
import use.openvpn.serverui.component.FTPCredentials.IConstantProgramFTPCredentialsOVPN;
import use.openvpn.serverui.component.IPExternalUpload.DlgIPExternalOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.JarEasyZZZ;
import basic.zBasic.util.file.ResourceEasyZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.util.JTextFieldHelperZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;

public class ServerTrayUIZZZ extends KernelUseObjectZZZ implements ActionListener {
	public static final int iSTATUS_NEW = 0;                       //Wenn das SystemTry-icon neu ist 
	public static final int iSTATUS_STARTING = 1;               //Die OVPN-Konfiguration wird gesucht und die Processe werden mit diesen Konfigurationen gestartet.
	public static final int iSTATUS_LISTENING = 2;               //Die OVPN-Processe laufen.
	public static final int iSTATUS_CONNECTED = 3;            //Falls sich ein Client per vpn mit dem Server verbunden hat und erreichbar ist
	public static final int iSTATUS_INTERRUPTED = 4;          //Falls der Client wieder nicht erreichbar ist. Das soll aber keine Fehlermeldung in dem Sinne sein, sondern nur anzeigen, dass mal ein Client verbunden war.
	                                                                                      //Dies wird auch angezeigt, wenn z.B. die Netzwerkverbindung unterbrochen worden ist.
	public static final int iSTATUS_STOPPED = 5; 				 //Wenn kein OVPN-Prozess mehr l�uft.
	public static final int iSTATUS_ERROR = 6;
	//private String sStatusString = null;  soll nun aus dem objMonitor ausgelsen werden

	private SystemTray objTray = null;                                    //Das gesamte SystemTray von Windows
	private TrayIcon objTrayIcon = null;                                 //Das TrayIcon dieser Application
	private ServerMonitorRunnerZZZ  objMonitor = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
	private ServerMainZZZ objServerBackend = null;                            //Ein Thread, der die OpenVPN.exe mit der gew�nschten Konfiguration startet.
	
	//TODOGOON 20210210: Realisiere die Idee
	//Idee: In ClientMainUI eine/verschiedene HashMaps anbieten, in die dann diese Container-Objekte kommen.
	//      Dadurch muss man sie nicht als Variable deklarieren und kann dynamischer neue Dialogboxen, etc. hinzufügen.
	//Ziel diese hier als Variable zu deklarieren ist: Die Dialogbox muss nicht immer wieder neu erstellt werden.
	private KernelJDialogExtendedZZZ dlgIPExternal=null;
	private KernelJDialogExtendedZZZ dlgFTPCredentials=null;
	
	public ServerTrayUIZZZ(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ServerTrayUINew_(saFlagControl);
	}
	
	private void ServerTrayUINew_(String[] saFlagControl) throws ExceptionZZZ{
		main:{
			
			//try{		
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ( stemp, IFlagZUserZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}	
			}//End check
			
			//Dieses muss beim Beenden angesprochen werden, um das TrayIcon wieder zu entfernen
			this.objTray = SystemTray.getDefaultSystemTray();
			
			JPopupMenu menu = new JPopupMenu();
			
			JMenuItem menueeintrag2 = new JMenuItem(IConstantServerOVPN.sLABEL_START);
			menu.add(menueeintrag2);
			menueeintrag2.addActionListener(this);
			
			JMenuItem menueeintrag3 = new JMenuItem(IConstantServerOVPN.sLABEL_LOG);
            menu.add(menueeintrag3);
			menueeintrag3.addActionListener(this);
			
			JMenuItem menueeintragFTPCredentials = new JMenuItem(IConstantServerOVPN.sLABEL_FTP_CREDENTIALS);
            menu.add(menueeintragFTPCredentials);
			menueeintragFTPCredentials.addActionListener(this);
			
			JMenuItem menueeintragIPPage = new JMenuItem(IConstantServerOVPN.sLABEL_PAGE_IP_UPLOAD);
            menu.add(menueeintragIPPage);
			menueeintragIPPage.addActionListener(this);
			
			//??? Warum geht das auf meinem Desktop-Rechner nicht, auf dem Notebook aber ???			
			JMenuItem menueeintrag = new JMenuItem(IConstantServerOVPN.sLABEL_END);	
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

		
			ImageIcon objIcon = this.getImageIconByStatus(iSTATUS_NEW);			
			this.objTrayIcon = new TrayIcon(objIcon, "OVPNListener", menu);
			this.objTrayIcon.addActionListener(this);
			
			this.objTray.addTrayIcon(this.objTrayIcon);
			
		}//END main
	}
	
	public static ImageIcon getImageIconByStatus(int iStatus)throws ExceptionZZZ{
		ImageIcon objReturn = null;
		main:{
			URL url = null;
			ClassLoader objClassLoader = ServerTrayUIZZZ.class.getClassLoader(); 
			if(objClassLoader==null) {
				ExceptionZZZ ez = new ExceptionZZZ("unable to receiver classloader object", iERROR_RUNTIME, ServerTrayUIZZZ.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			String sPath= ResourceEasyZZZ.searchDirectoryAsStringRelative("resourceZZZ/image/tray"); //Merke: Innerhalb einer JAR-Datei soll hier ein src/ vorangestellt werden.					
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for directory '"+sPath+"'");
			
			String sPathTotal = "";
			switch(iStatus){
			case iSTATUS_NEW:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": NEW");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-yellow_benji_01.png");
				break;
			case iSTATUS_STARTING:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": STARTING");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-blue_benji_p_01.png");
				break;
			case iSTATUS_LISTENING:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": LISTENING");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-green_benji__01.png");
				break;
			case iSTATUS_CONNECTED:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": CONNECTED");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-seagreen_ben_01.png");
				break;
			case iSTATUS_INTERRUPTED:	
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": INTERRRUPTED");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-purple_benji_01.png");				
				break;
			case iSTATUS_STOPPED:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": STOPPED");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "pill-button-yellow_benji_01.png");				
				break;		
			case iSTATUS_ERROR:
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": ERROR");
				sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, "button-red_benji_park_01.png");
				break;		
			default:
				break main;
			}
			
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for imageicon '"+sPathTotal+"'");			
			url= ClassLoader.getSystemResource(sPathTotal);
			if(url==null) {
				String sLog = "unable to receive url object. Path '" + sPathTotal + "' not found?";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
				ExceptionZZZ ez = new ExceptionZZZ(sLog, iERROR_RUNTIME, ServerTrayUIZZZ.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": URL = '"+url.toExternalForm() + "'");
			}
			objReturn = new ImageIcon(url);
		}//END main:
		return objReturn;
	}
	
	public static String getStatusStringByStatus(int iStatus){
		String sReturn=null;
		main:{
			switch(iStatus){
			case iSTATUS_NEW:
				sReturn = "Not yet started.";
				break;
			case iSTATUS_STARTING:
				sReturn = "Starting ...";
				break;
			case iSTATUS_LISTENING:
				sReturn = "Listening.";
				break;
			case iSTATUS_CONNECTED:
				sReturn = "Connected.";
				break;
			case iSTATUS_INTERRUPTED:
				sReturn = "Connection ended or interrupted.";
				break;
			case iSTATUS_STOPPED:
				sReturn = "Stopped listening.";
				break;
			case iSTATUS_ERROR:
				sReturn = "ERROR.";
				break;			
			default: 
				break main;
			}
		}
		return sReturn;
	}
	
	public boolean switchStatus(int iStatus) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			//ImageIcon �ndern
			ImageIcon objIcon = this.getImageIconByStatus(iStatus);
			if(objIcon==null)break main;
			
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

	
	/**
	 * @return
	 * @author Fritz Lindhauer, 24.05.2023, 08:07:21
	 */
	public boolean listen(){
		boolean bReturn = false;
		main:{
			try{ 
				check:{
				}
				this.switchStatus(ServerTrayUIZZZ.iSTATUS_STARTING);
				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.
				//this.objServerBackend = (ServerMainZZZ) this.getServerBackendObject().getApplicationObject(); //new ServerMainZZZ(this.getKernelObject(), null);
				
				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				Thread objThreadConfig = new Thread(this.getServerBackendObject());
				objThreadConfig.start();
					
				//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.
				//Merke: Dieser Monitor Thread muss mit dem Starten der einzelnen Unterthreads solange warten, bis das ServerMainZZZ-Object in seinem Flag anzeigt, dass es fertig mit dem Start ist.				
				this.objMonitor = new ServerMonitorRunnerZZZ(this.getKernelObject(), this, this.objServerBackend, null);
				Thread objThreadMonitor = new Thread(objMonitor);
				objThreadMonitor.start();
							   
			}catch(ExceptionZZZ ez){
				//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
				ez.printStackTrace();
				String stemp = ez.getDetailAllLast();
				this.getKernelObject().getLogObject().WriteLineDate(stemp);
				try {
					this.switchStatus(ServerTrayUIZZZ.iSTATUS_ERROR);
				} catch (ExceptionZZZ ez2) {					
					ez2.printStackTrace();
					this.getLogObject().WriteLineDate(ez2.getDetailAllLast());
				}
			}		
		}
		return bReturn;
	}
	
	/**TODO Describe what the method does
	 * @return, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 21.07.2006 - 11:02:02
	 */
	public String computeStatusDetailString(){
		String sReturn = "";
		main:{
			check:{
				if (this.objServerBackend == null){
					sReturn = "Not yet tried to connect";
						break main;
				}
			}//END check
			
		String sStatusString = this.readStatusString();
		if(StringZZZ.isEmpty(sStatusString)){
			sReturn = sReturn + "No status available. Not yet started as server? \n\n";
		}else{
			//20200114: Erweiterung - Angabe des Rechnernamens
			try {
				sStatusString = sStatusString + "Rechner als Server: " + InetAddress.getLocalHost().getHostName() + "\n\n";
			} catch (UnknownHostException e) {				
				e.printStackTrace();
				ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernamens", iERROR_RUNTIME, (Object)this, (Exception)e);
			}						
			sReturn = sReturn + "STATUS: " + sStatusString + "\n\n";
		}
		
		//Die Details der Connection werden in einer HashMap bereitgestellt. Diese HashMap wird durch den ServerMonitorRunner gef�llt.
		HashMap hmStatusDetail = this.readStatusDetailHashMap();
		if(hmStatusDetail != null){
			if(hmStatusDetail.size()>=1){			
				Set objSet = hmStatusDetail.entrySet();
				if(objSet!=null){
					Iterator objIterator = objSet.iterator();
					if(objIterator!=null){
						while(objIterator.hasNext()){						
							sReturn = sReturn + objIterator.next() + "\n";
						}//END while
					}else{
						sReturn = sReturn + "No further connection details available (Iterator Null case)\n";
					}//END if objIterator != null
				}else{
					sReturn = sReturn + "No further connection details available (Set Null case)\n";
				}//END if objSet != null
			}else{
				sReturn = sReturn + "No further connection details available (HashMap.size()=0 case)\n";
			}
		}else{
			sReturn = sReturn + "No further connection details available (HashMap null case)\n";
		}
				
		}//END main
		return sReturn;
	}
	
	/** Reads the HashMap with detailed information from the ServerMonitor-object-Thread
	* @return HashMap
	* 
	* lindhaueradmin; 10.08.2006 11:06:52
	 */
	public HashMap readStatusDetailHashMap(){
		HashMap hmReturn = null;
		ServerMonitorRunnerZZZ objMonitor = this.getMonitorObject();
		if(objMonitor!=null){
			hmReturn = objMonitor.getStatusDetailHashMap();
		}
		return hmReturn;
	}
	
	public String readLogString(){
		String sReturn = "";
		main:{
			check:{
				if (this.objServerBackend == null){
					sReturn = "Not yet tried to connect";
					break main;
				}
			}//END check:
		 
		ArrayList listaLogString = this.objServerBackend.getStatusStringAll();
		if(listaLogString.isEmpty()){
			if (this.objServerBackend == null){
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
	
	
	/** Reads a status string from the ServerMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readStatusString(){
		String sReturn = "";
		if(this.objMonitor!=null){
			sReturn = this.objMonitor.getStatusString();
		}
		return sReturn;
	}
	
	
	//#######################
	//### GETTER / SETTER
	public TrayIcon getTrayIconObject(){
		return this.objTrayIcon;
	}
	
	public ServerMonitorRunnerZZZ  getMonitorObject(){
		return this.objMonitor;
	}
	public void setMonitorObject(ServerMonitorRunnerZZZ objMonitor){
		this.objMonitor = objMonitor;
	}
	
	public void setServerBackendObject(ServerMainZZZ objServerBackend){
		this.objServerBackend = objServerBackend;
	}
	public ServerMainZZZ getServerBackendObject(){
		return this.objServerBackend;
	}
	
	
	
	

//FGL Es scheint so als geht das nicht mit extra Klassen.
		public void actionPerformed(ActionEvent arg0) {
			try{
				String sCommand = arg0.getActionCommand();
				//System.out.println("Action to perform: " + sCommand);
				if(sCommand.equals(IConstantServerOVPN.sLABEL_END)){
					this.unload();	
				}else if(sCommand.equals(IConstantServerOVPN.sLABEL_START)){
					this.listen();
				}else if(sCommand.equals(IConstantServerOVPN.sLABEL_LOG)){
					//JOptionPane pane = new JOptionPane();
					String stemp = this.readLogString();
					//this.getTrayIconObject() ist keine Component ????
					JOptionPane.showMessageDialog(null, stemp, "Log des OVPN Connection Listeners", JOptionPane.INFORMATION_MESSAGE );
				}else if(sCommand.equals(IConstantServerOVPN.sLABEL_PAGE_IP_UPLOAD)) {
					
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
				}else if(sCommand.equals(IConstantServerOVPN.sLABEL_FTP_CREDENTIALS)) {					
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
					
				}else if(sCommand.equals(IConstantServerOVPN.sLABEL_DETAIL)){		
					String stemp = this.computeStatusDetailString();
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
				//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zur�ckgesetzt werden kann.				
				ez.printStackTrace();
				String stemp = ez.getDetailAllLast();
				this.getKernelObject().getLogObject().WriteLineDate(stemp);
				try {
					this.switchStatus(iSTATUS_ERROR);
				} catch (ExceptionZZZ ez2) {					
					ez2.printStackTrace();
					this.getLogObject().WriteLineDate(ez2.getDetailAllLast());
				}
			}
		}

	

}//END Class

