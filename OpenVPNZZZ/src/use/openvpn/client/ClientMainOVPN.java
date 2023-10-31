package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.net.client.KernelPortScanHostZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalSetZZZ;
import basic.zKernel.status.StatusLocalHelperZZZ;
import use.openvpn.AbstractMainOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.FileFilterConfigOVPN;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IProcessWatchRunnerOVPN;
import use.openvpn.client.process.ProcessWatchRunnerOVPN;
import use.openvpn.client.status.EventObject4ClientMainStatusLocalSetOVPN;
import use.openvpn.client.status.EventObject4ProcessMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventObject4ClientMainStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObject4ProcessWatchMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalSetOVPN;
import use.openvpn.clientui.IClientStatusMappedValueZZZ.ClientTrayStatusTypeZZZ;
import use.openvpn.server.ServerMainOVPN;

/**This class is used as a backend worker.
 * For frontend features, use ConfigMainUIZZZ.
 * There are some methods to read the current status.
 * @author 0823
 *
 */
public class ClientMainOVPN extends AbstractMainOVPN implements IClientMainOVPN,IEventBrokerStatusLocalSetUserOVPN, IListenerObjectStatusLocalSetOVPN{		
	private ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
		
	private ClientConfigFileZZZ objFileConfigReached = null;
	private ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter = null; //Liste der Batches, die OVPN starten mit den Konfigurationen.
	private ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = null; //Liste der Batches, die OVPN starten mit den Konfigurationen, die noch laufen, also gestartet worden sind.
	
	//Die Objekte an die sich der Tray registriert und auf deren LocalStauts - Events er hoert.
	private ClientThreadProcessWatchMonitorOVPN  objMonitorProcess = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
	private ClientThreadVpnIpPingerOVPN  objVpnIpPinger = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
		
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Merke: Das komplexere enum STATUSLOCAL in das Interface IClientMain verschoben, s. auch enum FLAGZ
	//       Dort ist es flexibler einbindbar, als hier in Form einer internen Klasse.
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public ClientMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);
		ClientMainNew_();
	}
	
	private boolean ClientMainNew_() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			//try{		
			check:{
				if(this.getFlag("init")) break main;
			}//End check
		
		
		
		//Merke: Diese Unterprozesse gehoeren ja wohl in das Haupt-Objekt!!! Darum...
		
		//#################
        //Definiere einen Monitor, der die OVPN-Watch Processe beobachtet (die ihren jeweiligen, eigentlichen OVPN.exe Process beobachten)
		//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
		String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ClientThreadProcessWatchMonitorOVPN-Object";
		System.out.println(sLog);
		this.getLogObject().WriteLineDate(sLog);
			
		String[] saFlagMonitorProcess = {IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION.name()};		
		this.objMonitorProcess = new ClientThreadProcessWatchMonitorOVPN(this.getKernelObject(), this, saFlagMonitorProcess);
		this.registerForStatusLocalEvent(this.objMonitorProcess);//Den Thread am Main-Backend-Objekt registrieren			
		this.objMonitorProcess.registerForStatusLocalEvent(this); //Das Main-Backend-Objekt am MonitorProcess registrieren
		
		//Monitor noch nicht starten!!!
		//Thread objThreadProcessMonitor = new Thread(this.objMonitorProcess);
		//objThreadProcessMonitor.start();

		//#################
        //Definiere den VpnIpPinger, der permanent id IP-Adresse der VPN Verbindung ueberwacht
		//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
		sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ClientThreadConnectionVpnIpMonitorOVPN-Object";
		System.out.println(sLog);
		this.getLogObject().WriteLineDate(sLog);			
		
		String[] saFlagVpnIpPinger = null;
		this.objVpnIpPinger = new ClientThreadVpnIpPingerOVPN(this.getKernelObject(), this, saFlagVpnIpPinger);
		this.registerForStatusLocalEvent(this.objVpnIpPinger);
		this.objVpnIpPinger.registerForStatusLocalEvent(this); //Das Main-Backend-Objekt am MonitorProcess registrieren
					
		//Monitor noch nicht starten!!!
		//Thread objThreadVpnIpPinger = new Thread(this.objVpnIpPinger);
		//objThreadVpnIpPinger.start();

		
		bReturn = true;
	}//end main:
	return bReturn;
	}
	
	
	/**Entrypoint for managing the configuration files for "OpenVPN" client.
	 * @param args, 
	 *
	 * @return void
	 *
	 * javadoc created by: 0823, 30.06.2006 - 10:24:31
	 */
	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.
			this.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTING, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert worden ist und dann sich passend einstellen kann.
			
			connmain:{
			this.logProtocolString("Searching for configuration template files 'Template*.ovpn'"); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			IKernelZZZ objKernel = this.getKernelObject();	
						
			ClientApplicationOVPN objApplication = (ClientApplicationOVPN) this.getApplicationObject();//Im UI erzeugt und übergeben.
			ConfigChooserOVPN objChooser = new ConfigChooserOVPN(objKernel,"client",objApplication);
			this.setConfigChooserObject(objChooser);
			
			//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 								 						
			//Die Konfigurations-Template Dateien finden					
			File[] objaFileConfigTemplate = objChooser.findFileConfigOvpnTemplates();
			if(objaFileConfigTemplate==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(objaFileConfigTemplate.length == 0){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				this.logProtocolString(objaFileConfigTemplate.length + " configuration TEMPLATE file(s) was (were) found in the directory: '" + objChooser.readDirectoryConfigPath() + "'");  //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
			//### 2a. Auslesen der IP aus der Ini Datei
			String sIpIni = ((ClientApplicationOVPN)this.getApplicationObject()).getIpIni();
			if(StringZZZ.isEmpty(sIpIni)) {
				this.logProtocolString("No Ip is configured in the INI-File");
			}else {
				this.logProtocolString("IP read from INI-File: '" + sIpIni + "'");
			}

//Merke: Die in der Ini-Datei eingetragene IP hat Vorrang.			
//	     Die IP aus der Webseite wird in einem anderen Menüpunkt ausgelesen.
//			### 2b. Voraussetzung: Web-Seite konfiguriert, auf der die dynamische IP vorhanden ist.
//			//Zur Web-Seite verbinden, dazu den KernelReaderURL verwenden und zunaechst initialisieren.
//			this.logMessageString("Reading configured url to parse for ip-adress."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
//			String sIpUrl=null;
//			if(((ClientApplicationOVPN)this.getApplicationObject()).getURL2Parse()==null){
//				//ExceptionZZZ ez = new ExceptionZZZ(sERROR_CONFIGURATION_MISSING+"URL String", iERROR_CONFIGURATION_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
//				//throw ez;
//				this.logMessageString("No URL to read IP from is configured.");
//			}else{
//				this.logMessageString("URL to read IP from is configured as: '" + ((ClientApplicationOVPN)this.getApplicationObject()).getURL2Parse() + "'");
//				
////				###2ba. Voraussetzung: Auf der konfigurierten Web-Seite muss eine IP-Adresse auszulesen sein
//				this.logMessageString("Parsing IP-adress from URL."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
//				sIpUrl = ((ClientApplicationOVPN)this.getApplicationObject()).getIpURL();//Dabei wird auch der Proxy eingestellt. 
//				if(StringZZZ.isEmpty(sIpUrl)){
//					//ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to receive new IP-adress.", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
//					//throw ez;
//					this.logMessageString("Unable to receive new IP-adress from URL.");
//				}else{
//					this.logMessageString("New IP-adress from URL received: '" + sIpUrl + "'");
//					
//				}
//			}
									
			String sIpUsed = ((ClientApplicationOVPN)this.getApplicationObject()).getIpRemote();//Dahinter liegt dann die Regel welche ausgewählt werden soll.			
			if(StringZZZ.isEmpty(sIpUsed)) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Unable to receive any IP-adress from Ini-file. First read the IP from the URL Website.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + sLog, iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Using IP as remote: '" + sIpUsed + "'";
				System.out.println(sLog);
				this.logProtocolString(sLog);
			}
			
			
			//####################################################################
			//### DAS SCHREIBEN DER NEUEN KONFIGURATION
					
			//+++ A) Vorbereitung
			//+++ 1. Die früher mal verwendeten Dateien entfernen
			this.logProtocolString("Removing former configuration file(s)."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			File[] objaFileConfigUsed = objChooser.findFileConfigUsed();
			if(objaFileConfigUsed==null){
				this.logProtocolString("No previously used file was found (null case). Nothing removed.");
			}else if(objaFileConfigUsed.length==0){
				this.logProtocolString("No previously used file was found (0 case). Nothing removed.");			
			}else{
				this.logProtocolString("Trying to remove previously used file(s): " + objaFileConfigUsed.length);
				for(int icount = 0; icount < objaFileConfigUsed.length; icount++){
					boolean btemp = objaFileConfigUsed[icount].delete();
					if(btemp==true){
						this.logProtocolString( "File successfully removed: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}else{
						this.logProtocolString("Unable to remove file: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}					
				}//END for
			}
					
			//+++ B) Die gefundenen Werte überall eintragen: IN neue Dateien
			this.logProtocolString("Creating new configuration-file(s) from template-file(s), using new line(s)");									
			ArrayList listaFileUsed = new ArrayList(objaFileConfigTemplate.length);
			for(int icount = 0; icount < objaFileConfigTemplate.length; icount++){
				File fileTemplateOvpnUsed = objaFileConfigTemplate[icount];
				
				ClientConfigMapper4TemplateOVPN objMapper = new ClientConfigMapper4TemplateOVPN(objKernel, this, fileTemplateOvpnUsed);
				this.setConfigMapperObject(objMapper);
				
				//Mit dem Setzen der neuen Datei, basierend auf dem Datei-Template wird intern ein Parser f�r das Datei-Template aktiviert
				ClientConfigTemplateUpdaterZZZ objUpdater = new ClientConfigTemplateUpdaterZZZ(objKernel, this, objChooser, objMapper, null);				
				File objFileNew = objUpdater.refreshFileUsed(fileTemplateOvpnUsed);					
				if(objFileNew==null){
					this.logProtocolString("Unable to create 'used file' file base on template template: '" + objaFileConfigTemplate[icount].getPath() + "'");					
				}else{
					boolean btemp = objUpdater.update(objFileNew, true); //Bei false werden also auch Zeilen automatisch hinzugef�gt, die nicht im Template sind. Z.B. Proxy-Einstellungen.
					if(btemp==true){
						this.logProtocolString( "'Used file' successfully created for template: '" + objaFileConfigTemplate[icount].getPath() + "'");
		
						//+++ Nun dieses used-file dem Array hinzuf�gen, dass f�r den Start der OVPN-Verbindung verwendet wird.
						listaFileUsed.add(objUpdater.getFileUsed());
					}else{
						this.logProtocolString( "'Used file' not processed, based upon: '" + objaFileConfigTemplate[icount].getPath() + "'");					
					}																						
				}
			}//end for
			
			
			//##########################################################################################
			//+++ Die neuen OVPN-Verbindungsfiles zum Starten der VPN-Verbindung verwenden !!!
			//    Merke: Diese werden in unabhaengigen Threads "gemonitored"

			//Gibt es ueberhaupt eine "mögliche" Konfiguration  ???
			if(listaFileUsed.isEmpty()){
				this.logProtocolString("No valid remote connection available. Quitting.");
			
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}
			
			
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++ VPN schon verbunden ? Dies checken .....						 
			this.logProtocolString("Checking if any OVPN connection is still established.");
			String sVpnIp = this.scanVpnIpFirstEstablished(listaFileUsed);
			if(sVpnIp!=null){
				((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpRemoteEstablished(sVpnIp);
				
				//DAS IST NICHT AUSSAGEKRAEFTIG. DIE VPN-VERBINDUNG KANN UEBER EINEN GANZ ANDEREN PORT HERGESTELLT WORDEN SEIN !!! this.sPortVPN = objStarter.getVpnPort();	
				this.logProtocolString("A connection with an VPN target ip is still established: " + ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpRemoteEstablished() + ". Quitting."); //+ ":" + this.getVpnPortEstablished() + ". Quitting.")
				
				//NEU: HERAUSFINDEN, UEBER WELCHEN PORT DIE VERBINDUNG ERSTELLT WORDEN IST.
				//TODO Das ist technisch, hinter einer Firewall, nicht so einfach zu realisieren.
				
				//Momentan wird das noch als Fehler ausgegeben.
				//TODO Ggf. sollte diese IP dann lediglich aus der Lister der aufzubauenden VPN-Verbindungen herausgenommen werden. 
				//            Diese IP sollte dann dem Frontend als eine vorhandene Verbindung mitgeteilt werden, obwohl z.B. der Status noch auf "Verbiinden" steht.
				//            Die noch unverbundenen VPN-Verbindungen sollten dann versucht werden zu verbinden (was aber nur mit einem Timeout sinnvoll erscheint, sonst bekommt man ggf. nie das Frontend auf den "gruenen"-Status.
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}else{
				this.logProtocolString("No connection with OVPN is established, till now.");//DER PORT IST NICHT AUSSAGEKR�FTIG + ":" + objStarter.getVpnPort();							
			}
		
			//+++ AUFBAUEN DER LISTE DER ZU STARTENDEN KONFIGURATIONEN, dabei wieder von den Files ausgehen.
			//TODO: Ggf. diejenigen Verbindungen herausnehmen, die schon konfiguriert sind. 
			ArrayList<ClientConfigStarterOVPN> listaStarter = new ArrayList(listaFileUsed.size());			//Hier sollen diejenigen rein, die dann versucht werden sollen herzustellen.
			for(int icount = 0; icount < listaFileUsed.size(); icount++){
				File objFileConfig2start = (File) listaFileUsed.get(icount);
				ClientConfigStarterOVPN objStarter = new ClientConfigStarterOVPN(objKernel, this, icount, objFileConfig2start, null);
				listaStarter.add(icount, objStarter);					
			}//END For
			this.setClientConfigStarterList(listaStarter);
			
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.			
			this.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
															
			bReturn = true;
			
		   //#### Das Starten des Monitoren ueber eine Thread wurde verschoben nach clientMain.connect()
		   
		}//END connmainmain;
	
	}//END main
		if(bReturn == true){
			this.logProtocolString( "Finished everything: 'Start successfull case'.");	
		}else{
			this.logProtocolString( "Finished everything: 'Start not really successfull case'.");
		}
		
		return bReturn;
	}//END start()
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			boolean bStarted = this.start();
		} catch (ExceptionZZZ ez) {
			try {
				String sLog = ez.getDetailAllLast();
				this.logProtocolString("An error happend: '" + sLog + "'");
				this.setStatusLocal(ClientMainOVPN.STATUSLOCAL.HASERROR, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
				
			} catch (ExceptionZZZ e1) {				
				System.out.println(ez.getDetailAllLast());
				e1.printStackTrace();
			}
			try {
				this.getKernelObject().getLogObject().WriteLineDate(ez.getDetailAllLast());
			} catch (ExceptionZZZ e1) {
				System.out.println(e1.getDetailAllLast());
				e1.printStackTrace();
			}
		}
	}
	
	/**Scans the ports of the VPN-Ip in the given range.
	 * Updates the status.
	 * 
	 * @param sIP
	 * @param sPortLow
	 * @param sPortHigh
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 17.07.2006 - 16:27:00
	 */
	public boolean scanVpnPortAll(String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		return this.scanPortAll_("VPN", sIP, sPortLow, sPortHigh);
	}
	
	/**Scans the ports of the VPN-Ip in the given range.
	 * Updates the status.
	 * 
	 * @param sIP
	 * @param sPortLow
	 * @param sPortHigh
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 17.07.2006 - 16:27:00
	 */
	public boolean scanRemotePortAll(String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		return this.scanPortAll_("Remote", sIP, sPortLow, sPortHigh);
	}
	
	private boolean scanPortAll_(String sAlias, String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{				
			}//END check:
				
		this.writePortStatusByAlias(sAlias, "Scan started ...");
		KernelPortScanHostZZZ objPortScan = new KernelPortScanHostZZZ(objKernel, sIP, null);
		objPortScan.setThreadStorageSize(200);
		
		int iLow = Integer.parseInt(sPortLow);
		int iHigh = Integer.parseInt(sPortHigh);
		
		int iHighTemp = 0;
		int iLowTemp = iLow;	
		String sPortScanTemp = "";
		int iPortScanCount=0;
		do{
//			Ziel: Statusansicht schneller aktualisieren. Daher nur 1000 Ports pro Durchlauf scannen
			iHighTemp = iLowTemp + 1000;
			if(iHighTemp>= iHigh){
				iHighTemp = iHigh;
			}
			this.getKernelObject().getLogObject().WriteLineDate("Scanning Ports on Host: " + sIP + " from :" + iLowTemp + " to: " + iHighTemp);				
			boolean btemp = objPortScan.scan(iLowTemp,iHighTemp); 
			if(btemp==true){
				ArrayList listaPortConnected = objPortScan.getPortConnected();
				if(listaPortConnected.isEmpty()==true){
					//System.out.println("No port found open.");
					//this.sPortVpnScanned = "No port found open.";
				}else{
	//				System.out.println( listaPortConnected.size() + " Ports found open on host: " + sHost);
					iPortScanCount = iPortScanCount + listaPortConnected.size();				
					Integer intPort = (Integer) listaPortConnected.get(0);
					if(sPortScanTemp.length()>= 1){
						sPortScanTemp = sPortScanTemp + "; " + intPort.toString(); //Das ist den den folgenden 1000 Ports, jeweils der 1. Wert.
					}else{
						sPortScanTemp = intPort.toString(); //Das ist dann der erste Wert
					}						
					for(int icount=1; icount < listaPortConnected.size(); icount++){
						intPort = (Integer) listaPortConnected.get(icount);
						//System.out.println(intPort.toString());
						sPortScanTemp = sPortScanTemp + "; " + intPort.toString() ;
					}
					this.getKernelObject().getLogObject().WriteLineDate("Open ports found on Host: " + sIP + " : " + sPortScanTemp);
					this.writePortStatusByAlias(sAlias, iPortScanCount + " Ports found open (" + sPortScanTemp + " ....");								
				}
			}else{
				this.writePortStatusByAlias(sAlias, "Unable to scan ports.");
			}
			iLowTemp = iLowTemp + 1001;				
		}while(iHighTemp <= iHigh);
		if(iPortScanCount==0){
			this.writePortStatusByAlias(sAlias, "No port found open.");
		}else{
			this.writePortStatusByAlias(sAlias, iPortScanCount + " Ports found open (" + sPortScanTemp + ")");
		}
		bReturn = true;
		}//End main:
		return bReturn;
	}
	
	private void writePortStatusByAlias(String sAlias, String sStatus){
		if(sAlias.equalsIgnoreCase("Remote")){
			((ClientApplicationOVPN)this.getApplicationObject()).setRemotePortScanned(sStatus);
		}else if(sAlias.equalsIgnoreCase("VPN")){
			((ClientApplicationOVPN)this.getApplicationObject()).setVpnPortScanned(sStatus);
		}
	}
	
	
	/**Scans the IP of the provided File-Objects
	 * @param listaFileUsed, an ArrayList, File-Objects. These Files must be open-vpn configuration files.
	 * @throws ExceptionZZZ, 
	 *
	 * @return String, the first IP found, which can be reached.
	 *
	 * javadoc created by: 0823, 13.07.2006 - 14:56:30
	 */
	public String scanVpnIpFirstEstablished(ArrayList listaFileUsed) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			check:{
				if(listaFileUsed==null) break main;
				if(listaFileUsed.isEmpty()) break main;
				 
				//Prüfen, ob es sich um File-Objekte handelt
				for (int icount = 0; icount < listaFileUsed.size(); icount++){
					File objFileConfig2start = null;				
					try{ //Falls eine Arraylist ohne File-Objekte �bergeben wird, dann hier den Fehler abfangen
						objFileConfig2start = (File) listaFileUsed.get(icount);
					}catch(Exception e){
						ExceptionZZZ ez = new ExceptionZZZ("Error on Element " + icount + " of the ArrayList. This should be a file object. Error reported: " + e.getMessage(), iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					//Prüfen, ob es sich auch immer um ein Configuration-File handelt.
					FileFilterConfigOVPN objFileFilter = new FileFilterConfigOVPN();
					boolean btemp = objFileFilter.accept(objFileConfig2start);
					if(btemp==false){
						ExceptionZZZ ez = new ExceptionZZZ("Error on Element " + icount + " of the ArrayList. This should be a ovpn configuration file. It is a file but OVPNFileFilterConfigZZZ.accept() reports 'false'.", iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}					
				}				 				
			}//END check
		
		//+++ Performanceverbesserung: Eine Art "Unique" ueber die ArrayList, daraus kommt dann die zu ueberpruefende Liste.
		//TODO: Dies als Kernel-Erweiturung fuer ArrayList zur Verfuegung stellen.
		ArrayList listaUnique = new ArrayList(listaFileUsed.size());   			
		for(int icount = 0; icount < listaFileUsed.size(); icount++){		
			File objFileConfig2start = (File) listaFileUsed.get(icount);		
			//ClientConfigStarterZZZ objStarter = new ClientConfigStarterZZZ(objKernel, objFileConfig2start, null);			
			//String sIP2Check = objStarter.getVpnIp();
			ClientConfigFileZZZ objClientFile = new ClientConfigFileZZZ(objKernel, objFileConfig2start, null);
			String sIP2Check = objClientFile.getVpnIpRemote();
			if(sIP2Check!=null){					
				if(!listaUnique.isEmpty()){
		
					//Liste der Configurationen durchsuchen und nur dann die neue Konfiguration hinzuf�gen, wenn die VPN-IP-Adresse unterschiedlich ist.
					for(int icount2 = 0; icount2 < listaUnique.size(); icount2++){
						//ClientConfigStarterZZZ objStarter2Check = (ClientConfigStarterZZZ) listaUnique.get(icount2);
						//String stemp = objStarter2Check.getVpnIp();
						File objFileConfig2Check = (File) listaUnique.get(icount2); 
						ClientConfigFileZZZ objClientFile2Check = new ClientConfigFileZZZ(objKernel, objFileConfig2Check, null);
						String stemp = objClientFile2Check.getVpnIpRemote();
						if(stemp!=null){
							if(!sIP2Check.equals(stemp)){
								//IP in die Lister der zu pr�fenden VPN-Verbindungen aufnehmen
								listaUnique.add(objFileConfig2Check);
								break; //Die innere For-Schleife verlassen. Wurde ja schon hinzugef�gt. Ergo zum n�chsten File gehen.
							}
						}
					}//END for "2"
				}else{
					//IP in die Lister der zu pr�fenden VPN-Verbindungen aufnehmen
					listaUnique.add(objFileConfig2start);
				}//END if isempty()
			}//END if sIP2Check!=null
		}//END for "1"
		
		//	Gibt es �berhaupt eine "m�gliche" Konfiguration  ???
		if(listaUnique.isEmpty()){
			this.logProtocolString("No valid 'unique' remote connection availabe. Quitting.");
		
			ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
			throw ez;	
		}
		
		
		//#############################################################################
		//+++ Pruefen der Erreichbarkeit der VPN-Verbindung (NEU: auf fixen Port 80, bzw. was so konfiguriert wurde)
		String sVPNPort4Check = ((ClientApplicationOVPN)this.getApplicationObject()).readVpnPort2Check();		
		for(int icount=0;icount < listaUnique.size(); icount++){
			//ClientConfigStarterZZZ objStarter = (ClientConfigStarterZZZ) listaUnique.get(icount);
			File objFileConfig = (File) listaUnique.get(icount);
			ClientConfigFileZZZ objConfigFile = new ClientConfigFileZZZ(objKernel, objFileConfig, null);
			boolean bReachable = objConfigFile.isVpnReachable(sVPNPort4Check);
			if(bReachable==true){
				sReturn = objConfigFile.getVpnIpRemote();
				this.setFileConfigReached(objConfigFile);
				break main;
				
				//NEU: HERAUSFINDEN, �BER WELCHEN PORT DIE VERBINDUNG ERSTELLT WORDEN IST.
				//TODO Das ist technisch, hinter einer Firewall, nicht so einfach zu realisieren.
		
			}
		}
		}//End main:
		return sReturn;
	}
	

	public boolean isStartingOnLaunch() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		boolean bProof = this.proofFlagSetBefore(IClientMainOVPN.FLAGZ.LAUNCHONSTART.name());
		if(bProof) {
			bReturn = this.getFlag(IClientMainOVPN.FLAGZ.LAUNCHONSTART.name());
		}else {
			//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht. 
			String stemp = this.objKernel.getParameter("StartingOnLaunch").getValue();
			if(stemp==null) break main;
			if(stemp.equals("1")){
				bReturn = true;
			}
			this.setFlag(IClientMainOVPN.FLAGZ.LAUNCHONSTART, bReturn);
		}//end if
		}//END main
		return bReturn;
	}	

	public boolean isConnectOnStart() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
			boolean bProof = this.proofFlagSetBefore(IClientMainOVPN.FLAGZ.CONNECTONSTART.name());
			if(bProof) {
				bReturn = this.getFlag(IClientMainOVPN.FLAGZ.CONNECTONSTART.name());				
			}else {
			
				//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht. 
				String stemp = this.objKernel.getParameter("ConnectOnStart").getValue();
				if(stemp==null) break main;
				if(stemp.equals("1")){
					bReturn = true;
				}
				bReturn = this.setFlag(IClientMainOVPN.FLAGZ.CONNECTONSTART,bReturn);
			}//end if
		}//END main
		return bReturn;
	}
	
	public boolean isPortScanEnabled() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
			boolean bProof = this.proofFlagSetBefore(IClientMainOVPN.FLAGZ.ENABLEPORTSCAN.name());
			if(bProof) {
				bReturn = this.getFlag(IClientMainOVPN.FLAGZ.ENABLEPORTSCAN.name());
			}else {					
				//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht. 
				String sPortScanEnabled = objKernel.getParameter("PortScanEnabled").getValue();
				if(!StringZZZ.isEmpty(sPortScanEnabled)){
					if(sPortScanEnabled=="1"){
						bReturn = true;
					}//END if sPortScanEnabled=="1"
				}//END if(!StringZZZ.isEmpty(sPortScanEnabled)){
				this.setFlag(IClientMainOVPN.FLAGZ.ENABLEPORTSCAN, bReturn);
			}//end if
		}//END main
		return bReturn;
	}	

	//######################################################
	//### Getter / Setter
	public ClientThreadProcessWatchMonitorOVPN  getProcessMonitorObject(){
		return this.objMonitorProcess;
	}
	public void setProcessMonitorObject(ClientThreadProcessWatchMonitorOVPN objMonitor){
		this.objMonitorProcess = objMonitor;
	}
	
	public ClientThreadVpnIpPingerOVPN  getVpnIpPingerObject(){
		return this.objVpnIpPinger;
	}
	public void setVpnIpPingerObject(ClientThreadVpnIpPingerOVPN objMonitor){
		this.objVpnIpPinger = objMonitor;
	}
	
	public ClientConfigFileZZZ getFileConfigReached(){
		return this.objFileConfigReached;
	}
	public void setFileConfigReached(ClientConfigFileZZZ objFileConfig){
		this.objFileConfigReached = objFileConfig;
	}
	
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterList() {
		if(this.listaClientConfigStarter==null) {
			this.listaClientConfigStarter=new ArrayList<ClientConfigStarterOVPN>();
		}
		return this.listaClientConfigStarter;
	}
	public void setClientConfigStarterList(ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter) {
		this.listaClientConfigStarter = listaClientConfigStarter;
	}
	
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterRunningList() {
		if(this.listaClientConfigStarterRunning==null) {
			this.listaClientConfigStarterRunning=new ArrayList<ClientConfigStarterOVPN>();
		}
		return this.listaClientConfigStarterRunning;
	}
	public void setClientConfigStarterRunningList(ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter) {
		this.listaClientConfigStarterRunning = listaClientConfigStarter;
	}
	
	
	

	

		
	
	//### Aus IListenerObjectStatusLocalSetOVPN
	//Der Client selbst "hoert" nicht auch Statusaenderungen, wie z.B. aktuell schon der Tray.
	
	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isStatusLocalRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
//	@Override
//	public boolean isStatusLocalRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		boolean bReturn = false;
//		
//		main:{
//			String sAbr = eventStatusLocalSet.getStatusAbbreviation();
//			if(!StringZZZ.startsWith(sAbr, "isconnect")) break main;
//			
//			bReturn = true;			
//		}//end main:
//		
//		return bReturn;
//	}


	//#####################################################
	//### IStatusLocalUserZZZ
	@Override 
	public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bFunction = this.setStatusLocal(enumStatus, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocal(Enum enumStatusIn, int iIndex, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			

			bFunction = this.setStatusLocal(enumStatus, iIndex, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocal(Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			

			bFunction = this.setStatusLocal(enumStatus, -1, sStatusMessage, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocal(java.lang.Enum, java.lang.String, boolean)
	 */
	@Override
	public boolean setStatusLocal(Enum enumStatusIn, int iIndex, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			
		//Wichtig: Man kann nicht einfach auf den String zurück...
		//return this.setStatusLocal(objEnumStatus.name(),bStatusValue);
		//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum		
	    ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
		String sStatusName = enumStatus.name();
		bFunction = this.proofStatusLocalExists(sStatusName);
		if(!bFunction) break main;
		
		//Hat sich der Status geändert?
		bFunction = this.proofStatusLocalChanged(sStatusName, bStatusValue);				
		if(!bFunction) break main;
		
		//++++++++++++++++++++	
		//Setze den Status nun in die HashMap
		HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
		hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
		
		//TODOGOON 20231025: der enumStatus als currentStatus im Objekt speichern...
		//                   dito mit dem "vorherigen Status"
		//                   dann kann man auf die Eigenschaften des Enums zugreifen....
		//Setze nun das Enum, und damit auch die Default-StatusMessage
		this.setStatusLocalEnum(enumStatus);
		String sStatusMessageToSet = enumStatus.getStatusMessage();
		String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
		System.out.println(sLog);
		this.logProtocolString(sLog);

		//Falls eine Message extra uebergeben worden ist, ueberschreibe...
		if(sStatusMessage!=null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain uebersteuere sStatusMessageToSet='" + sStatusMessage + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			this.setStatusLocalMessage(sStatusMessage);
		}
		
		
		//++++++++++++++++++++
		//Konfiguration ggfs. einer Liste hinzufuegen
		ClientConfigStarterOVPN objClientConfigStarter = null;
		if(iIndex>=0) {
			objClientConfigStarter = this.getClientConfigStarterList().get(iIndex);
			if(objClientConfigStarter!=null) {
				if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTED.getName())){
					sLog = ReflectCodeZZZ.getPositionCurrent() + ":  Nimm Konfiguration in die Liste der gestarteten auf: '" + sStatusName + "'";
					System.out.println(sLog);
					this.logProtocolString(sLog);				
					
					//fuege die Verbundene Konfiguration der entsprechenden Liste hinzu
					this.getClientConfigStarterRunningList().add(objClientConfigStarter);
				}
			}			
		}
		
		
		
					
		//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
		//Dann erzeuge den Event und feuer ihn ab.	
		if(this.getSenderStatusLocalUsed()==null) break main;
		
		//Dann erzeuge den Event und feuer ihn ab.
		//Merke: Nun aber ueber das enum, in dem ja noch viel mehr Informationen stecken können.			
		IEventObject4ClientMainStatusLocalSetOVPN event = null;
		if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTING.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTING, true);
				
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTED.getName())) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTED, true);

		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTING.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);

		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTED.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
		
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPPTED.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPPTED, true);
		
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTING.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISPINGSTARTING, true);
				
			if(objClientConfigStarter!=null) {					
				//fuege die Verbundene Konfiguration der entsprechenden Liste hinzu
				this.getClientConfigStarterRunningList().add(objClientConfigStarter);
			}
			
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTED.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISPINGSTARTED, true);
		
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTING.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED, true);
		
			
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED, true);
		
		}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED.getName())){
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);				
			event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED, true);
					
		}else {
			
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": KEIN Event erzeugt fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
		}
			
		if(event!=null) {
			event.setApplicationObjectUsed(objApplication);
				
			if(objClientConfigStarter!=null) {
				//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
				event.setClientConfigStarterObjectUsed(objClientConfigStarter);
			}
				
			this.getSenderStatusLocalUsed().fireEvent(event);			
			bFunction = true;
		}else {
			bFunction = false;
		}										
	}	// end main:
	return bFunction;
	}
	
	@Override
	public boolean setStatusLocal(String sStatusName, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sStatusName)) {
				bFunction = true;
				break main;
			}
						
			bFunction = this.proofStatusLocalExists(sStatusName);															
			if(bFunction == true){
				
				//TODOGOON20231028;//Hier das enum ermitteln und dann damit weitere Funktion aufrufen....
				
				
				//Setze das Flag nun in die HashMap
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
				
				
				
				
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.				
				IEventObject4ClientMainStatusLocalSetOVPN event = null;
				if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTING.getName())){
					String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
					System.out.println(sLog);
					this.logProtocolString(sLog);					
					event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTING, true);
					
				}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTED.getName())) {
					String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
					System.out.println(sLog);
					this.logProtocolString(sLog);					
					event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTED, true);
					
				}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTING.getName())){
						String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
						System.out.println(sLog);
						this.logProtocolString(sLog);				
						event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
						
				}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTED.getName())){
						String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
						System.out.println(sLog);
						this.logProtocolString(sLog);				
						event = new EventObject4ClientMainStatusLocalSetOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
								
				}else {
					String sLog = ReflectCodeZZZ.getPositionCurrent() + ": KEIN Event erzeugt fuer '" + sStatusName + "'";
					System.out.println(sLog);
					this.logProtocolString(sLog);
				}
				
				if(event!=null) {
					event.setApplicationObjectUsed(objApplication);			
					this.getSenderStatusLocalUsed().fireEvent(event);			
					bFunction = true;
				}else {
					bFunction = false;
				}								
			}										
		}	// end main:
		
		return bFunction;
	}
	
	//### aus IEventBrokerStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
		if(this.objEventStatusLocalBroker==null) {
			//++++++++++++++++++++++++++++++
			//Nun geht es darum den Sender fuer Aenderungen an den Flags zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
			ISenderObjectStatusLocalSetOVPN objSenderStatusLocal = new SenderObjectStatusLocalSetOVPN();
			this.objEventStatusLocalBroker = objSenderStatusLocal;
		}
		return this.objEventStatusLocalBroker;
	}

	@Override
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
		this.objEventStatusLocalBroker = objEventStatusLocalBroker;
	}

	@Override
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);
	}


    //### aus IListenerObjectStatusLocalSetOVPN,
	@Override
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Das Main Objekt ist woanders registriert.
		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
				
			boolean bRelevant = this.isEventRelevant(eventStatusLocalSet); 
			if(!bRelevant) {
				sLog = 	ReflectCodeZZZ.getPositionCurrent() + ": Event / Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			
			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
			IApplicationOVPN  objApplication = eventStatusLocalSet.getApplicationObjectUsed();
			if(objApplication==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);	
				this.logProtocolString(sLog);
				break main;
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				
			}
			
			ClientConfigStarterOVPN objStarter = eventStatusLocalSet.getClientConfigStarterObjectUsed();
			if(objStarter==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				//Manchmal kann der Event nicht einer konkreten Konfiguration zugeordnet werden.
				//Dann gibt es auch kein Konfigurationsobjekt (z.B. beom Connecten vom monitor)
				//also auch kein Abbruch... break main;
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
			}
			
			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
			
//			if(eventStatusLocalSet.getStatusEnum() instanceof IClientMainOVPN.STATUSLOCAL){
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 01");
//				bReturn = this.statusLocalChangedMainEvent_(eventStatusLocalSet);
//				break main;
//				
//			}else 

			if (eventStatusLocalSet.getStatusEnum() instanceof IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 12");
				bReturn = this.statusLocalChangedMonitorEvent_(eventStatusLocalSet);
				break main;
				
			}else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL) {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 13");
				bReturn = this.statusLocalChangedPingerEvent_(eventStatusLocalSet);
				break main;
			
			}else{	
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 00 ELSE");
				
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
//	private boolean statusLocalChangedMainEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		boolean bReturn=false;
//		main:{	
//			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer MainEvent.";
//			System.out.println(sLog);
//			this.getClientBackendObject().logMessageString(sLog);
//			
//			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
//			STATUSLOCAL objStatusEnum = (STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
//			if(objStatusEnum==null) break main;
//				
//			//Falls nicht zuständig, mache nix
//			boolean bProof = this.isEventStatusLocalRelevant(eventStatusLocalSet);
//			if(!bProof) break main;
//				
//			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
//			if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
//				
//			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
//			System.out.println(sLog);
//			this.getClientBackendObject().logMessageString(sLog);	
//				
//			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
//			System.out.println(sLog);
//			this.getClientBackendObject().logMessageString(sLog);
//				
//			boolean bRelevant = this.isStatusLocalRelevant(enumStatus);
//			if(!bRelevant) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"' ist nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.getClientBackendObject().logMessageString(sLog);
//			}														
//
//			//Die Stati vom Backend-Objekt mit dem TrayIcon mappen
//			if(ClientMainOVPN.STATUSLOCAL.ISSTARTNEW==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.NEW);				
//			}else if(ClientMainOVPN.STATUSLOCAL.ISSTARTING==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.STARTING);
//			}else if(ClientMainOVPN.STATUSLOCAL.ISSTARTED==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.STARTED);
//
//			}else if(ClientMainOVPN.STATUSLOCAL.WATCHRUNNERSTARTING==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.WATCHING);
//			}else if(ClientMainOVPN.STATUSLOCAL.WATCHRUNNERSTARTED==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.WATCHED);
//					
////			}else if(ClientMainOVPN.STATUSLOCAL.PortScanAllFinished==objStatusEnum) {
////					this.switchStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.CONNECTED);
//			}else if(ClientMainOVPN.STATUSLOCAL.HASERROR==objStatusEnum) {
//				this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
//			}else {
//				sLog = "Der Status wird nicht behandelt - '"+objStatusEnum.getAbbreviation()+"'.";
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
//				this.logLineDate(sLog);
//				break main;
//			}
//			
//			bReturn = true;
//		}//end main:
//		return bReturn;
//	}

	/** Merke: Diese private Methode wird nach ausführlicher Prüfung aufgerufen, daher hier mehr noetig z.B.:
	 * - Keine Pruefung auf NULLL
	 * - kein instanceof 
	 * @param eventStatusLocalSet
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 19.10.2023, 09:43:19
	 */
	private boolean statusLocalChangedMonitorEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			if(eventStatusLocalSet==null)break main;
						
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer MonitorEvent.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
			IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL objStatusEnum = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
			if(objStatusEnum==null) break main;
			
			//Merke: Die Relevanz des Eingangsevents wird vorher in statusLocalChanged() geprüft.
												
			//###################
			//Den Statustext aus dem enum
			String sStatusMessage = objStatusEnum.getStatusMessage(); 
			sLog = ReflectCodeZZZ.getPositionCurrent()+": sStatusMessage='" + sStatusMessage + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			this.setStatusLocalString(sStatusMessage);
			
			//Den Index (fuer die Liste der Starter-objekte)
			//Merke: Manche events haben aber auch kein ClientConfigStarterObjekt, das sie nicht auf eine Statkonfiguation bezogen werden koennen.
			int iIndex=-1;
			if(eventStatusLocalSet.getClientConfigStarterObjectUsed()!=null) {
				iIndex = eventStatusLocalSet.getClientConfigStarterObjectUsed().getIndex();
			}
			
			
			//Die Stati vom Monitor-Objekt mit dem Backend-Objekt mappen
			if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTING==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISCONNECTING, eventStatusLocalSet.getStatusValue());				
			}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
				
				//Ggfs. vorhandene Aenderungen aus dem Backend-Application-Objekt des Events holen, bzw. hier das ganze Backen-Objekt austauschen
				//Dann kann sich z.B. die Datailinfo-box die aktuellsten Werte daraus holen.
				IApplicationOVPN objApplicationUsed = eventStatusLocalSet.getApplicationObjectUsed();
				if(objApplicationUsed==null) {						
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Kein Application-Objekt aus dem Event erhalten.";
					System.out.println(sLog);
					this.logProtocolString(sLog);
				}else {
					this.setApplicationObject(objApplicationUsed);

					//################################
					//Merke: Dieser Wert kommt beim Setzen im ClientThreadProcessWatchMonitor in diesem Backenobjekt nicht an.
					//       Darum explizit holen und setzen.
					String sVpnIp = this.getApplicationObject().getVpnIpRemote();
											
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Verbunden mit remote VPNIP='"+sVpnIp+"'";
					System.out.println(sLog);
					this.logProtocolString(sLog);
					
					//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt uebergben.
					this.getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);
					//################################
				}
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISCONNECTED, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
				
			}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISINTERRUPTED==objStatusEnum) {
				//Ggfs. vorhandene Aenderungen aus dem Backend-Application-Objekt des Events holen, bzw. hier das ganze Backen-Objekt austauschen
				//Dann kann sich z.B. die Datailinfo-box die aktuellsten Werte daraus holen.
				IApplicationOVPN objApplicationUsed = eventStatusLocalSet.getApplicationObjectUsed();
				if(objApplicationUsed==null) {						
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Kein Application-Objekt aus dem Event erhalten.";
					System.out.println(sLog);
					this.logProtocolString(sLog);
				}else {
					this.setApplicationObject(objApplicationUsed);

					//################################
					//Merke: Dieser Wert kommt beim Setzen im ClientThreadProcessWatchMonitor in diesem Backenobjekt nicht an.
					//       Darum explizit holen und setzen.
					String sVpnIp = this.getApplicationObject().getVpnIpRemote();
											
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Verbindungsunterbrechung mit remote VPNIP='"+sVpnIp+"'";
					System.out.println(sLog);
					this.logProtocolString(sLog);
					
					//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt uebergben.
					this.getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);
					//################################
				}
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPPTED, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
				
			}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASERROR==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.HASERROR, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
				
			}else {
				sLog = "Der Status wird nicht behandelt - '"+objStatusEnum.getAbbreviation()+"'.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				break main;
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
	private boolean statusLocalChangedPingerEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			if(eventStatusLocalSet==null)break main;
						
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer PingerEvent.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL objStatusEnum = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
			if(objStatusEnum==null) break main;
			
			//Merke: Die Relevanz des Eingangsevents wird vorher in statusLocalChanged() geprüft.
												
			//###################
			//Den Statustext aus dem enum
			String sStatusMessage = objStatusEnum.getStatusMessage(); 
			sLog = ReflectCodeZZZ.getPositionCurrent()+": sStatusMessage='" + sStatusMessage + "'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			this.setStatusLocalString(sStatusMessage);
			
			//Den Index (fuer die Liste der Starter-objekte)
			//Merke: Manche events haben aber auch kein ClientConfigStarterObjekt, das sie nicht auf eine Statkonfiguation bezogen werden koennen.
			int iIndex=-1;
			if(eventStatusLocalSet.getClientConfigStarterObjectUsed()!=null) {
				iIndex = eventStatusLocalSet.getClientConfigStarterObjectUsed().getIndex();
			}
			
			
			//Die Stati vom Monitor-Objekt mit dem Backend-Objekt mappen
			if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTING, eventStatusLocalSet.getStatusValue());
			}else if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTED, eventStatusLocalSet.getStatusValue());				
			}else if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTING==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTING, eventStatusLocalSet.getStatusValue());				
			}else if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
				
				//Ggfs. vorhandene Aenderungen aus dem Backend-Application-Objekt des Events holen, bzw. hier das ganze Backen-Objekt austauschen
				//Dann kann sich z.B. die Datailinfo-box die aktuellsten Werte daraus holen.
				IApplicationOVPN objApplicationUsed = eventStatusLocalSet.getApplicationObjectUsed();
				if(objApplicationUsed==null) {						
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Kein Application-Objekt aus dem Event erhalten.";
					System.out.println(sLog);
					this.logProtocolString(sLog);
				}else {
					this.setApplicationObject(objApplicationUsed);

					//################################
					//Merke: Dieser Wert kommt beim Setzen im ClientThreadProcessWatchMonitor in diesem Backenobjekt nicht an.
					//       Darum explizit holen und setzen.
//					String sVpnIp = this.getApplicationObject().getVpnIpRemote();
//											
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Verbunden mit remote VPNIP='"+sVpnIp+"'";
//					System.out.println(sLog);
//					this.logMessageString(sLog);
//					
//					//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt uebergben.
//					this.getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);
					//################################
				}
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
				
				
			}else if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASERROR==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.HASERROR, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
			}else if(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED==objStatusEnum) {
				this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED, iIndex, sStatusMessage, eventStatusLocalSet.getStatusValue());
			}else {
				sLog = "Der Status wird nicht behandelt - '"+objStatusEnum.getAbbreviation()+"'.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				break main;
			}
			
			bReturn = true;
		}//end main:
		return bReturn;
	}
			
			
//			
//			
//			
//			boolean bHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
//			boolean bEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
//			boolean bHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
//			
//			
//			if(bHasError && bEnded){
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bHasError && bEnded";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);					
//			}else if((!bHasError) && bEnded){
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bHasError && bEnded";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);
//				
//			}else if(bHasConnection){
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bHasConnection";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);
//				
//				//+++++++++++++++++++++					
//				//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
//				//ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//				
//				//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
//				IApplicationOVPN  objApplication = eventStatusLocalSet.getApplicationObjectUsed();
//				if(objApplication==null) {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Event-Objekt erhalten.";
//					System.out.println(sLog);
//					this.objMain.logMessageString(sLog);
//					break main;
//				}else {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Event-Objekt erhalten.";
//					System.out.println(sLog);
//					this.objMain.logMessageString(sLog);
//					
//				}
//				
//				String sVpnIp = objApplication.getVpnIpRemote();
//				int iId = eventStatusLocalSet.getID();
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbunden mit remote VPNIP='"+sVpnIp+"'";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);
//				
//				//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt übergben.
//				//objStarter2.getMainObject().getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);								
//				
//				//TODOGOON20231007;							
//				//Cooler wäre tatsächlich alles über den Status des Main - objekts zu erledigen
//				//Momentan wird der nur abgefragt um die Schleife zu verlassen...., oder?
//				
//				//Einen Event werfen, der dann das Icon im Menue-Tray aendert, etc....
//				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
//				//Dann erzeuge den Event und feuer ihn ab.
//				//Merke: Nun aber ueber das enum								
//				if(this.getSenderStatusLocalUsed()!=null) {								
//					
//					//Im Main den Status setzen. Das ist ggfs. eine Abbruchbedingung fuer diese Schleife.
//					//boolean bStatusLocalIsConnectedExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
//											
//					//Besser als den Wert direkt zu setzen, ist es einen Event zu feuern, auf den das Main hoert.
//					boolean bStatusLocalIsConnectedExists = this.setStatusLocal(ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED, true);//Es wird ein Event gefeuert, an dem das Backend-Objekt registriert wird und dann sich passend einstellen kann.
//					
//					
//				}else {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN StatusSender-Objekt (objectBroker) vorhanden. Ggfs. kein anderes Objekt fuer das Hoeren auf Events hier registriert.";
//					System.out.println(sLog);
//					this.objMain.logMessageString(sLog);	
//				}
//				
//				
//				//+++++++++++++++++++++
//			}else {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status '"+enumStatus.getAbbreviation()+"' nicht weiter behandelt";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);	
//			}
//			
//		}//end main:			
//		return bReturn;
//		
//	}

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
			if(enumStatus==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
				System.out.println(sLog);
				this.logProtocolString(sLog);							
				break main;
			}
			
			
			sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
			System.out.println(sLog);
			this.logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			//+++ Pruefungen
			bReturn = this.isStatusChanged(eventStatusLocalSet.getStatusText());
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
						
			bReturn = this.isEventRelevantByStatusLocalValue(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByClass(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
				break main;
			}
						
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevantByStatusLocalValue(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			//Es interessieren auch "false" Werte, um den Status ggfs. wieder zuruecksetzen zu koennen.
			//(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
			
			bReturn = true;
		}
		return bReturn;
	}
	
	@Override
	public boolean isEventRelevantByClass(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
	
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse ist instanceof IClientThreadProcessWatchMonitorOVPN. Damit relevant.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				bReturn = true;
				break main;
			}else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL) {
				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse ist instanceof IClientThreadProcessWatchMonitorOVPN. Damit relevant.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				bReturn = true;
				break main;
			}
		}//end main:
		return bReturn;
	}

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevantByStatusLocal(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();							
			bReturn = this.isStatusLocalRelevant(enumStatus);
			if(!bReturn) break main;
		}//end main:
		return bReturn;
	}
	
	
	//#######################################
	@Override 
	public String getStatusLocalMessage() {
		String sReturn = null;
		main:{
			if(this.sStatusLocalMessage!=null) {
				sReturn =  this.sStatusLocalMessage;
				break main;				
			}
			
			//Merke: Erst in OVPN-Klassen gibt es enum mit Message
			IClientMainOVPN.STATUSLOCAL objEnum = (IClientMainOVPN.STATUSLOCAL)this.getStatusLocalEnum();
			if(objEnum!=null) {
				sReturn = objEnum.getStatusMessage();
			}			
		}//end main:
		return sReturn;
	}

	@Override
	public String getStatusLocalMessagePrevious(){
		String sReturn = null;
		main:{
			if(this.sStatusLocalMessage!=null) {
				sReturn =  this.sStatusLocalMessage;
				break main;				
			}
			
			//Merke: Erst in OVPN-Klassen gibt es enum mit Message
			IClientMainOVPN.STATUSLOCAL objEnum = (IClientMainOVPN.STATUSLOCAL)this.getStatusLocalEnumPrevious();
			if(objEnum!=null) {
				sReturn = objEnum.getStatusMessage();
			}			
		}//end main:
		return sReturn;
	}
	
	
	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#isStatusLocalRelevant(basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ)
	 */
	@Override
	public boolean isStatusLocalRelevant(IEnumSetMappedZZZ objEnumStatusIn) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(objEnumStatusIn==null) break main;
				
			//Fuer das Main-Objekt ist erst einmal jeder Status relevant
			bReturn = true;
		}//end main:
		return bReturn;
	}

}//END class

