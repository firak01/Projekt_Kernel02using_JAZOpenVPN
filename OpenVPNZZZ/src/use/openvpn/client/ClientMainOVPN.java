package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectWithStatusZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.net.client.KernelPortScanHostZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalZZZ;
import basic.zKernel.status.StatusLocalAvailableHelperZZZ;
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
import use.openvpn.client.status.EventObject4ClientMainStatusLocalMessageOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventObject4ClientMainStatusLocalMessageSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalOVPN;
import use.openvpn.clientui.component.tray.IClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ;
import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalSetOVPN;

/**This class is used as a backend worker.
 * For frontend features, use ConfigMainUIZZZ.
 * There are some methods to read the current status.
 * @author 0823
 *
 */
public class ClientMainOVPN extends AbstractMainOVPN implements IClientMainOVPN,IEventBrokerStatusLocalSetUserOVPN,IListenerObjectStatusLocalOVPN{		
	private volatile ISenderObjectStatusLocalOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
		
	private volatile ClientConfigFileZZZ objFileConfigReached = null;
	private volatile ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter = null; //Liste der Batches, die OVPN starten mit den Konfigurationen.
	private volatile ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = null; //Liste der Batches, die OVPN starten mit den Konfigurationen, die noch laufen, also gestartet worden sind.
	
	//Die Objekte, an die sich der Tray registriert und auf deren LocalStauts - Events er hoert.
	private volatile ClientThreadProcessWatchMonitorOVPN  objMonitorProcess = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
	private volatile ClientThreadVpnIpPingerOVPN  objVpnIpPinger = null;         //Der Thread, welcher auf hereinkommende Verbindungen (an bestimmten Port) lauscht. Er startet dazu eigene ServerConnectionListener-Threads und stellt deren Ergebnisse zur Verf�gung, bzw. �ndert das TrayIcon selbst.
		
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
		
		//Idee: Den Monitor nach der erfolgreichen Verbindung einfach beenden.
		//String[] saFlagMonitorProcess = {IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};

		//Mit diesen Flags auf Pruefung der Statusaenderung, wird u.a. das Neustarten des Watcher-Processes indirekt verhindert.
		//           siehe: ClientThreadProcessWatchMonitorOVPN.run - Line: 93: Starting monitor thread canceled.
		String[] saFlagMonitorProcess = {IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};		
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
	public boolean startAsThread() throws ExceptionZZZ{
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
			
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalAbbreviation(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
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
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalAbbreviation(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
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
		
			ExceptionZZZ ez = new ExceptionZZZ(this.getStatusLocalAbbreviation(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
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
	
	public boolean resetVpnIpPingerObject() throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(this.objVpnIpPinger!=null) {
				if(this.objVpnIpPinger.getFlagLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING.getName())) {
					this.objVpnIpPinger.setFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP, true);
	
					String sLog  = ReflectCodeZZZ.getPositionCurrent() + ": Set previous thread object to NULL";
					System.out.println(sLog);
					this.logProtocolString(sLog);
					this.objVpnIpPinger = null;
					bReturn = true;
					break main;
				}else {
					String sLog  = ReflectCodeZZZ.getPositionCurrent() + ": Previous thread object was not started";
					System.out.println(sLog);
					this.logProtocolString(sLog);
				}
			}else {
				String sLog  = ReflectCodeZZZ.getPositionCurrent() + ": Previous thread object still NULL";
				System.out.println(sLog);
				this.logProtocolString(sLog);
			}
		}//end main:
		return bReturn;
	}
	
	
	public ClientConfigFileZZZ getFileConfigReached(){
		return this.objFileConfigReached;
	}
	public void setFileConfigReached(ClientConfigFileZZZ objFileConfig){
		this.objFileConfigReached = objFileConfig;
	}
	
	public ClientConfigStarterOVPN getClientConfigStarter(int iPosition) {
		ClientConfigStarterOVPN objReturn=null;
		main:{
			if(iPosition<= 0) break main;
				 
			ArrayList<ClientConfigStarterOVPN> listaConfigStarter = this.getClientConfigStarterList();
			if(iPosition > listaConfigStarter.size()) break main;
			
		 	objReturn = (ClientConfigStarterOVPN) listaConfigStarter.get(iPosition);
		 	
		 }//END main
		 return objReturn;
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
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(enumStatus, bStatusValue, null);
		}//end main:
		return bReturn;
	}
	
	@Override 
	public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(enumStatus, bStatusValue, null);
		}//end main:
		return bReturn;
	}
	
	@Override 
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	@Override 
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			

			bReturn = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	//################################################
		//+++ aus IStatusLocalUserMessageZZZ			
		@Override 
		public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sMessage) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal(enumStatus, bStatusValue, sMessage);
			}//end main:
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sMessage, bStatusValue);
			}//end main:
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue, String sMessage) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, bStatusValue, sMessage);
			}//end main:
			return bReturn;
		}				
		
		@Override 
		public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		
		
	@Override 
	public boolean offerStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			

			bReturn = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	@Override
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
	
			bReturn = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocal(java.lang.Enum, java.lang.String, boolean)
	 */
	private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) break main;
			
			
			//Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum		
		    IClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			String sStatusName = enumStatus.name();
			bReturn = this.proofStatusLocalExists(sStatusName);
			if(!bReturn) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMainOVPN would like to fire event, but this status is not available: '" + sStatusName + "'";
				this.logProtocolString(sLog);
				break main;
			}
		
			bReturn = this.proofStatusLocalValue(sStatusName, bStatusValue);
			if(!bReturn) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMainOVPN would like to fire event, but this status has a value to be ignored: '" + sStatusName + "'";
				this.logProtocolString(sLog);
				break main;
			}

		
			//++++++++++++++++++++	
			//Setze den Status nun in die HashMap
			HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
			hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
			
		
			//Den enumStatus als currentStatus im Objekt speichern...
			//                   dito mit dem "vorherigen Status"
			//Setze nun das Enum, und damit auch die Default-StatusMessage		
			String sStatusMessageToSet = null;
			if(StringZZZ.isEmpty(sStatusMessage)){
				if(bStatusValue) {
					sStatusMessageToSet = enumStatus.getStatusMessage();
				}else {
					sStatusMessageToSet = "NICHT " + enumStatus.getStatusMessage();
				}			
			}else {
				sStatusMessageToSet = sStatusMessage;
			}		
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
			this.logProtocolString(sLog);

		//Falls eine Message extra uebergeben worden ist, ueberschreibe...
		if(sStatusMessageToSet!=null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain setze sStatusMessageToSet='" + sStatusMessageToSet + "'";
			this.logProtocolString(sLog);			
		}
		
		//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
		this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);
	
		//++++++++++++++++++++
		//Besonderheit im Client-Main-Objekt
		//Konfiguration ggfs. einer Liste hinzufuegen
		ClientConfigStarterOVPN objClientConfigStarter = null;
		if(iIndexOfProcess>=0) {
			objClientConfigStarter = this.getClientConfigStarterList().get(iIndexOfProcess);
			if(objClientConfigStarter!=null) {
				if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTED.getName())){
					sLog = ReflectCodeZZZ.getPositionCurrent() + ":  Nimm Konfiguration in die Liste der gestarteten auf: '" + sStatusName + "'";
					this.logProtocolString(sLog);				
					
					//fuege die Verbundene Konfiguration der entsprechenden Liste hinzu
					this.getClientConfigStarterRunningList().add(objClientConfigStarter);
				}
			}			
		}
		//++++++++++++++++++++
				
		//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
		//Dann erzeuge den Event und feuer ihn ab.	
		if(this.getSenderStatusLocalUsed()==null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
			this.logProtocolString(sLog);	
			break main;
		}
		
		//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
		sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";
		this.logProtocolString(sLog);
		IEventObject4ClientMainStatusLocalMessageOVPN event = new EventObject4ClientMainStatusLocalMessageOVPN(this,1,enumStatus, bStatusValue);
		event.setApplicationObjectUsed(objApplication);
		event.setStatusMessage(sStatusMessageToSet);
		
		//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
		if(objClientConfigStarter!=null) {			
			event.setClientConfigStarterObjectUsed(objClientConfigStarter);
		}
			
		sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain feuert event '" + enumStatus.getAbbreviation() + "'";
		this.logProtocolString(sLog);
		this.getSenderStatusLocalUsed().fireEvent(event);
		
		bReturn = true;										
		}	// end main:
	return bReturn;
	}
	
	@Override
	public boolean setStatusLocal(String sStatusName, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{	
			if(StringZZZ.isEmpty(sStatusName))break main;
			boolean bProof = this.proofStatusLocalExists(sStatusName);
			if(!bProof)break main;

			//Setze den Status nun in die HashMap
			HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
			hmStatus.put(sStatusName.toUpperCase(), bStatusValue);

			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.				
			EventObject4ClientMainStatusLocalMessageOVPN event = null;
			if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTING.getName())){
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage=NICHT VORHANDEN";
				this.logProtocolString(sLog);					
				event = new EventObject4ClientMainStatusLocalMessageOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTING, true);
				
			}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISSTARTED.getName())) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage=NICHT VORHANDEN";
				this.logProtocolString(sLog);					
				event = new EventObject4ClientMainStatusLocalMessageOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISSTARTED, true);
				
			}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTING.getName())){
					String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage=NICHT VORHANDEN";
					this.logProtocolString(sLog);				
					event = new EventObject4ClientMainStatusLocalMessageOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
					
			}else if(sStatusName.equalsIgnoreCase(IClientMainOVPN.STATUSLOCAL.ISCONNECTED.getName())){
					String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage=NICHT VORHANDEN";
					this.logProtocolString(sLog);				
					event = new EventObject4ClientMainStatusLocalMessageOVPN(this,1,IClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
							
			}else {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": KEIN Event erzeugt fuer '" + sStatusName + "'";
				this.logProtocolString(sLog);
			}
			
			if(event!=null) {
				event.setApplicationObjectUsed(objApplication);			
				this.getSenderStatusLocalUsed().fireEvent(event);			
				bReturn = true;
			}else {
				bReturn = false;
			}																				
		}	// end main:		
		return bReturn;
	}
	
	//### aus IEventBrokerStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
		if(this.objEventStatusLocalBroker==null) {
			//++++++++++++++++++++++++++++++
			//Nun geht es darum den Sender fuer Aenderungen an den Flags zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
			ISenderObjectStatusLocalOVPN objSenderStatusLocal = new SenderObjectStatusLocalOVPN();
			this.objEventStatusLocalBroker = objSenderStatusLocal;
		}
		return this.objEventStatusLocalBroker;
	}

	@Override
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
		this.objEventStatusLocalBroker = objEventStatusLocalBroker;
	}

	@Override
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().addListenerObjectStatusLocal(objEventListener);
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().removeListenerObjectStatusLocal(objEventListener);
	}


    //### aus IListenerObjectStatusLocalSetOVPN,
	@Override
	public boolean changedStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Das Main Objekt ist woanders registriert.
		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent() + "Event gefangen.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
				
			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
			
//			if(eventStatusLocalSet.getStatusEnum() instanceof IClientMainOVPN.STATUSLOCAL){
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 01");
//				bReturn = this.statusLocalChangedMainEvent_(eventStatusLocalSet);
//				break main;
//				
//			}else 

			if (eventStatusLocalSet.getStatusEnum() instanceof IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +"TYP FGLTEST 12");
				bReturn = this.changeStatusLocalMonitorEvent_(eventStatusLocalSet);
				break main;
				
			}else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL) {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +"TYP FGLTEST 13");
				bReturn = this.changeStatusLocalPingerEvent_(eventStatusLocalSet);
				break main;
			
			}else{	
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +"TYP FGLTEST 00 ELSE");
				
			}											
			
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
	private boolean changeStatusLocalMonitorEvent_(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			if(eventStatusLocalSet==null)break main;
						
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++			
			boolean bRelevant = this.isEventRelevant2ChangeStatusLocal(eventStatusLocalSet); 
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
			
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

			
			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();
						
			//+++++++++++++++++++++
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmEnum = this.getHashMapEnumSetForCascadingStatusLocal();
			IClientMainOVPN.STATUSLOCAL objEnum = (IClientMainOVPN.STATUSLOCAL) hmEnum.get(enumStatus);			
			if(objEnum==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			
			boolean bValue = eventStatusLocalSet.getStatusValue();
			
//			boolean bHasError = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR)&& bValue;
//			boolean bEnded = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED) && bValue;
//			boolean bHasConnection = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION) && bValue;
//			boolean bHasConnectionLost = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST) && bValue;
//		
			boolean bEventHasError = objEnum.equals(IClientMainOVPN.STATUSLOCAL.HASERROR);
			boolean bEventEnded = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISSTOPPED);
			boolean bEventHasConnection = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISCONNECTED);
			boolean bEventHasConnectionLost = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPTED);
						
			int iIndex = -1;			
			objStarter = eventStatusLocalSet.getClientConfigStarterObjectUsed();
			if(objStarter==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				//break main; //Kein Abbruchkriterium
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);	
				
				iIndex = objStarter.getIndex();
			}
				
			String sStatusMessage = eventStatusLocalSet.getStatusMessage();		
			boolean bStatusLocalSet = this.offerStatusLocal(iIndex, objEnum, sStatusMessage, bValue);//Es wird ein Event gefeuert, an dem das Tray-Objekt und andere registriert sind und dann sich passend einstellen kann.
			if(!bStatusLocalSet) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			//++++++++++++++
			
			//Die Stati vom Monitor-Objekt mit dem Backend-Objekt mappen
			//if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPRO==objStatusEnum) {
			//	this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISCONNECTING, eventStatusLocalSet.getStatusValue());				
			//}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
			
			if(bEventHasError && bEventEnded){
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasError && bEventEnded";
				System.out.println(sLog);
				this.logProtocolString(sLog);					
			}else if((!bEventHasError) && bEventEnded){
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bEventHasError && bEventEnded";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				
			}else if(bEventHasConnection) {
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
				
			}else if(bEventHasConnectionLost ) {				
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
	private boolean changeStatusLocalPingerEvent_(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			if(eventStatusLocalSet==null)break main;
						
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer PingerEvent.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++			
//			boolean bRelevant = this.isEventRelevant(eventStatusLocalSet); 
//			if(!bRelevant) {
//				sLog = 	ReflectCodeZZZ.getPositionCurrent() + ": Event / Status nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.logProtocolString(sLog);
//				break main;
//			}
			
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
			
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			
			//+++++++++++++++++++++
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmEnum = this.getHashMapEnumSetForCascadingStatusLocal();
			IClientMainOVPN.STATUSLOCAL objEnum = (IClientMainOVPN.STATUSLOCAL) hmEnum.get(enumStatus);			
			if(objEnum==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			
			boolean bValue = eventStatusLocalSet.getStatusValue();
			
//			boolean bHasError = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR)&& bValue;
//			boolean bEnded = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED) && bValue;
//			boolean bHasConnection = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION) && bValue;
//			boolean bHasConnectionLost = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST) && bValue;
//		
			boolean bEventHasError = objEnum.equals(IClientMainOVPN.STATUSLOCAL.HASERROR);
			boolean bEventEnded = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISSTOPPED);
	//		boolean bEventHasConnection = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISCONNECTED);
	//		boolean bEventHasConnectionLost = objEnum.equals(IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPTED);
			
			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
			//ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
			
			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
			
			
			int iIndex = -1;			
			objStarter = eventStatusLocalSet.getClientConfigStarterObjectUsed();
			if(objStarter==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				//break main; //Kein Abbruchkriterium
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
				System.out.println(sLog);
				this.logProtocolString(sLog);	
				
				iIndex = objStarter.getIndex();
			}
				
			
			String sStatusMessage = eventStatusLocalSet.getStatusMessage();		
			boolean bStatusLocalSet = this.offerStatusLocal(iIndex, objEnum, sStatusMessage, bValue);//Es wird ein Event gefeuert, an dem das Tray-Objekt und andere registriert sind und dann sich passend einstellen kann.
			if(!bStatusLocalSet) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			//++++++++++++++
			
			//Die Stati vom Monitor-Objekt mit dem Backend-Objekt mappen
			//if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPRO==objStatusEnum) {
			//	this.setStatusLocal(IClientMainOVPN.STATUSLOCAL.ISCONNECTING, eventStatusLocalSet.getStatusValue());				
			//}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
			
			if(bEventHasError && bEventEnded){
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasError && bEventEnded";
				System.out.println(sLog);
				this.logProtocolString(sLog);					
			}else if((!bEventHasError) && bEventEnded){
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bEventHasError && bEventEnded";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				
			}

			bReturn = true;
		}//end main:
		return bReturn;
	}
			
	

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatusFromEvent = eventStatusLocalSet.getStatusEnum();				
			if(enumStatusFromEvent==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
				System.out.println(sLog);
				this.logProtocolString(sLog);							
				break main;
			}
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen. Wert: " + bStatusValue;
			System.out.println(sLog);
			this.logProtocolString(sLog);
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus hat class='"+enumStatusFromEvent.getClass()+"'";
			System.out.println(sLog);
			this.logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus='" + enumStatusFromEvent.getAbbreviation()+"'";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			
			
			//#### Problemansatz: Mappen des Lokalen Status auf einen Status aus dem Event, verschiedener Klassen.
			String sStatusAbbreviationLocal = null;
			IEnumSetMappedZZZ objEnumStatusLocal = null;

			//HashMap<IEnumSetMappedZZZ,IEnumSetMappedZZZ>hm=this.createHashMapEnumSetForCascadingStatusLocalCustom();
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hm = this.getHashMapEnumSetForCascadingStatusLocal();
			objEnumStatusLocal = hm.get(enumStatusFromEvent);					
			//###############################
			
			if(objEnumStatusLocal==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping nicht mit Wert vorhanden. Damit nicht relevant.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
				//sStatusAbbreviationLocal = enumStatusFromEvent.getAbbreviation();
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping mit Wert vorhanden. Damit relevant.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				
				sStatusAbbreviationLocal = objEnumStatusLocal.getAbbreviation();
			}
			
			//+++ Pruefungen
			bReturn = this.isEventRelevantByClass2ChangeStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isStatusLocalDifferent(sStatusAbbreviationLocal, bStatusValue);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status geaendert. Mache weiter.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
			}
						
			bReturn = this.isEventRelevantByStatusLocalValue2ChangeStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByStatusLocal2ChangeStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
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
	public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
	public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
	public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			IEnumSetMappedStatusZZZ enumStatus = eventStatusLocalSet.getStatusEnum();							
			bReturn = this.isStatusLocalRelevant(enumStatus);
			if(!bReturn) break main;
		}//end main:
		return bReturn;
	}
		
	
	//#######################################
	
	//### aus IListenerObjectStatusLocalMapForEventUserZZZ
	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> createHashMapEnumSetForCascadingStatusLocalCustom() {
		HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmReturn = new HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>();
		main:{
			
			//Reine Lokale Statuswerte kommen nicht aus einem Event und werden daher nicht gemapped. 
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTNEW, IClientMainOVPN.STATUSLOCAL.ISCONNECTNEW);
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, IClientMainOVPN.STATUSLOCAL.ISCONNECTING);
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTED, IClientMainOVPN.STATUSLOCAL.ISCONNECTING);
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTION, IClientMainOVPN.STATUSLOCAL.ISCONNECTED);
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTIONLOST, IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPTED);
			hmReturn.put(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASERROR, IClientMainOVPN.STATUSLOCAL.HASERROR);
			
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTNEW, IClientMainOVPN.STATUSLOCAL.ISPINGNEW);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING, IClientMainOVPN.STATUSLOCAL.ISPINGSTARTING);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTED, IClientMainOVPN.STATUSLOCAL.ISPINGSTARTED);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTNEW, IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTNEW);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTING, IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTING);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTED, IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASERROR, IClientMainOVPN.STATUSLOCAL.HASPINGERROR);
			
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTING, IClientMainOVPN.STATUSLOCAL.ISSTARTNEW);
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTED, IClientMainOVPN.STATUSLOCAL.ISSTARTNEW);			
			hmReturn.put(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASERROR, IClientMainOVPN.STATUSLOCAL.HASERROR);
		}//end main:
		return hmReturn;
	}

	@Override
	public String getProgramName() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgramAlias() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetProgramUsed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean reset() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}



	
}//END class

