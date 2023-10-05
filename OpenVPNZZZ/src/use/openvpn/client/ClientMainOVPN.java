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
import basic.zKernel.status.StatusLocalHelperZZZ;
import use.openvpn.AbstractMainOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.FileFilterConfigOVPN;
import use.openvpn.client.process.ProcessWatchRunnerOVPN;
import use.openvpn.client.status.EventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalSetOVPN;
import use.openvpn.server.ServerMainOVPN;

/**This class is used as a backend worker.
 * For frontend features, use ConfigMainUIZZZ.
 * There are some methods to read the current status.
 * @author 0823
 *
 */
public class ClientMainOVPN extends AbstractMainOVPN implements IClientMainOVPN{	
	private HashMap<String, Boolean>hmStatusLocal = new HashMap<String, Boolean>(); //Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen koennen.
	protected ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
		
	private ClientConfigFileZZZ objFileConfigReached = null;
	private ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter = null; //Liste der Batches, die OVPN starten mit den Konfigurationen.
	private ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = null; //Liste der Batches, die OVPN starten mit den Konfigurationen, die noch laufen, also gestartet worden sind.
	//Nein, jetzt aus der ClientConfigStarterRunning - Liste die Konfiguartionsdateien holen.... private ArrayList<File>listaFileConnectionToMonitor = null;                 //Liste der Konfigurationsdateien, die übrig bleiben, also gestartet worden sind.
	
/*STEHEN LASSEN: DIE PROBLEMATIK IST, DAS NICHT NACHVOLLZIEHBAR IST, �BER WELCHEN PORT DIE VPN-VERBINDUNG HERGESTELLT WURDE 
 * Zumindest nicht PER PING-BEFEHL !!!
private String sPortVPN = null;
*/
	
	public ClientMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);
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
			this.logMessageString("Searching for configuration template files 'Template*.ovpn'"); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
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
				this.logMessageString(objaFileConfigTemplate.length + " configuration TEMPLATE file(s) was (were) found in the directory: '" + objChooser.readDirectoryConfigPath() + "'");  //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
			//### 2a. Auslesen der IP aus der Ini Datei
			String sIpIni = ((ClientApplicationOVPN)this.getApplicationObject()).getIpIni();
			if(StringZZZ.isEmpty(sIpIni)) {
				this.logMessageString("No Ip is configured in the INI-File");
			}else {
				this.logMessageString("IP read from INI-File: '" + sIpIni + "'");
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
				String sLog = "Unable to receive any IP-adress from Ini-file. First read the IP from the URL Website.";
				this.logMessageString(sLog);
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + sLog, iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else {
				String sLog = "Using IP as remote: '" + sIpUsed + "'";
				this.logMessageString(sLog);
			}
			
			
			//####################################################################
			//### DAS SCHREIBEN DER NEUEN KONFIGURATION
					
			//+++ A) Vorbereitung
			//+++ 1. Die früher mal verwendeten Dateien entfernen
			this.logMessageString("Removing former configuration file(s)."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			File[] objaFileConfigUsed = objChooser.findFileConfigUsed();
			if(objaFileConfigUsed==null){
				this.logMessageString("No previously used file was found (null case). Nothing removed.");
			}else if(objaFileConfigUsed.length==0){
				this.logMessageString("No previously used file was found (0 case). Nothing removed.");			
			}else{
				this.logMessageString("Trying to remove previously used file(s): " + objaFileConfigUsed.length);
				for(int icount = 0; icount < objaFileConfigUsed.length; icount++){
					boolean btemp = objaFileConfigUsed[icount].delete();
					if(btemp==true){
						this.logMessageString( "File successfully removed: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}else{
						this.logMessageString("Unable to remove file: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}					
				}//END for
			}
					
			//+++ B) Die gefundenen Werte überall eintragen: IN neue Dateien
			this.logMessageString("Creating new configuration-file(s) from template-file(s), using new line(s)");									
			ArrayList listaFileUsed = new ArrayList(objaFileConfigTemplate.length);
			for(int icount = 0; icount < objaFileConfigTemplate.length; icount++){
				File fileTemplateOvpnUsed = objaFileConfigTemplate[icount];
				
				ClientConfigMapper4TemplateOVPN objMapper = new ClientConfigMapper4TemplateOVPN(objKernel, this, fileTemplateOvpnUsed);
				this.setConfigMapperObject(objMapper);
				
				//Mit dem Setzen der neuen Datei, basierend auf dem Datei-Template wird intern ein Parser f�r das Datei-Template aktiviert
				ClientConfigTemplateUpdaterZZZ objUpdater = new ClientConfigTemplateUpdaterZZZ(objKernel, this, objChooser, objMapper, null);				
				File objFileNew = objUpdater.refreshFileUsed(fileTemplateOvpnUsed);					
				if(objFileNew==null){
					this.logMessageString("Unable to create 'used file' file base on template template: '" + objaFileConfigTemplate[icount].getPath() + "'");					
				}else{
					boolean btemp = objUpdater.update(objFileNew, true); //Bei false werden also auch Zeilen automatisch hinzugef�gt, die nicht im Template sind. Z.B. Proxy-Einstellungen.
					if(btemp==true){
						this.logMessageString( "'Used file' successfully created for template: '" + objaFileConfigTemplate[icount].getPath() + "'");
		
						//+++ Nun dieses used-file dem Array hinzuf�gen, dass f�r den Start der OVPN-Verbindung verwendet wird.
						listaFileUsed.add(objUpdater.getFileUsed());
					}else{
						this.logMessageString( "'Used file' not processed, based upon: '" + objaFileConfigTemplate[icount].getPath() + "'");					
					}																						
				}
			}//end for
			
			
			//##########################################################################################
			//+++ Die neuen OVPN-Verbindungsfiles zum Starten der VPN-Verbindung verwenden !!!
			//    Merke: Diese werden in unabhaengigen Threads "gemonitored"

			//Gibt es ueberhaupt eine "mögliche" Konfiguration  ???
			if(listaFileUsed.isEmpty()){
				this.logMessageString("No valid remote connection available. Quitting.");
			
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}
			
			
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++ VPN schon verbunden ? Dies checken .....						 
			this.logMessageString("Checking if any OVPN connection is still established.");
			String sVpnIp = this.scanVpnIpFirstEstablished(listaFileUsed);
			if(sVpnIp!=null){
				((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpRemoteEstablished(sVpnIp);
				
				//DAS IST NICHT AUSSAGEKRAEFTIG. DIE VPN-VERBINDUNG KANN UEBER EINEN GANZ ANDEREN PORT HERGESTELLT WORDEN SEIN !!! this.sPortVPN = objStarter.getVpnPort();	
				this.logMessageString("A connection with an VPN target ip is still established: " + ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpRemoteEstablished() + ". Quitting."); //+ ":" + this.getVpnPortEstablished() + ". Quitting.")
				
				//NEU: HERAUSFINDEN, UEBER WELCHEN PORT DIE VERBINDUNG ERSTELLT WORDEN IST.
				//TODO Das ist technisch, hinter einer Firewall, nicht so einfach zu realisieren.
				
				//Momentan wird das noch als Fehler ausgegeben.
				//TODO Ggf. sollte diese IP dann lediglich aus der Lister der aufzubauenden VPN-Verbindungen herausgenommen werden. 
				//            Diese IP sollte dann dem Frontend als eine vorhandene Verbindung mitgeteilt werden, obwohl z.B. der Status noch auf "Verbiinden" steht.
				//            Die noch unverbundenen VPN-Verbindungen sollten dann versucht werden zu verbinden (was aber nur mit einem Timeout sinnvoll erscheint, sonst bekommt man ggf. nie das Frontend auf den "gruenen"-Status.
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}else{
				this.logMessageString("No connection with OVPN is established, till now.");//DER PORT IST NICHT AUSSAGEKR�FTIG + ":" + objStarter.getVpnPort();							
			}
		
			//+++ AUFBAUEN DER LISTE DER ZU STARTENDEN KONFIGURATIONEN, dabei wieder von den Files ausgehen.
			//TODO: Ggf. diejenigen Verbindungen herausnehmen, die schon konfiguriert sind. 
			ArrayList<ClientConfigStarterOVPN> listaStarter = new ArrayList(listaFileUsed.size());			//Hier sollen diejenigen rein, die dann versucht werden sollen herzustellen.
			for(int icount = 0; icount < listaFileUsed.size(); icount++){
				File objFileConfig2start = (File) listaFileUsed.get(icount);
				ClientConfigStarterOVPN objStarter = new ClientConfigStarterOVPN(objKernel, this, objFileConfig2start, null);
				listaStarter.add(icount, objStarter);					
			}//END For
			this.setClientConfigStarterList(listaStarter);
			
			//Da der Client nicht auf sich selbst "hoert", hier den Status selbst direkt setzen.
			this.setStatusString(ClientMainOVPN.STATUSLOCAL.ISSTARTED.getStatusMessage());
			
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.			
			this.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
			
			//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.									
		   bReturn = true;
			
		   //#### Das Starten des Monitoren ueber eine Thread wurde verschoben nach clientMain.connect()
		   
		}//END connmainmain;
	
	}//END main
		if(bReturn == true){
			this.logMessageString( "Finished everything: 'Start successfull case'.");	
		}else{
			this.logMessageString( "Finished everything: 'Start not really successfull case'.");
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
				this.logMessageString("An error happend: '" + sLog + "'");
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
			this.logMessageString("No valid 'unique' remote connection availabe. Quitting.");
		
			ExceptionZZZ ez = new ExceptionZZZ(this.getStatusString(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
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
	public ClientConfigFileZZZ getFileConfigReached(){
		return this.objFileConfigReached;
	}
	public void setFileConfigReached(ClientConfigFileZZZ objFileConfig){
		this.objFileConfigReached = objFileConfig;
	}
	
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterList() {
		return this.listaClientConfigStarter;
	}
	public void setClientConfigStarterList(ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter) {
		this.listaClientConfigStarter = listaClientConfigStarter;
	}
	
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterRunningList() {
		return this.listaClientConfigStarterRunning;
	}
	public void setClientConfigStarterRunningList(ArrayList<ClientConfigStarterOVPN> listaClientConfigStarter) {
		this.listaClientConfigStarterRunning = listaClientConfigStarter;
	}
	
	
	

	

	//#####################################################
	//### IStatusLocalUserZZZ
	/** DIESE METHODEN MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHREN STATUS SETZEN WOLLEN*/

	/* (non-Javadoc)
	 * @see basic.zKernel.status.IStatusLocalUserZZZ#getStatusLocal(java.lang.Enum)
	 */
	@Override
	public boolean getStatusLocal(Enum objEnumStatusIn) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(objEnumStatusIn==null) {
				break main;
			}
			
			ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
			String sStatusName = enumStatus.name();
			if(StringZZZ.isEmpty(sStatusName)) break main;
										
			HashMap<String, Boolean> hmFlag = this.getHashMapStatusLocal();
			Boolean objBoolean = hmFlag.get(sStatusName.toUpperCase());
			if(objBoolean==null){
				bFunction = false;
			}else{
				bFunction = objBoolean.booleanValue();
			}
							
		}	// end main:
		
		return bFunction;	
	}

	

	@Override
	public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
		//return this.getStatusLocal(objEnumStatus.name());
		//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum
		
	    ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
		String sStatusName = enumStatus.name();
		bFunction = this.proofStatusLocalExists(sStatusName);															
		if(bFunction == true){
			
			//Setze das Flag nun in die HashMap
			HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
			hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
		
			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.
			//Merke: Nun aber ueber das enum			
			if(this.objEventStatusLocalBroker!=null) {
				IEventObjectStatusLocalSetOVPN event = new EventObjectStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);
				this.objEventStatusLocalBroker.fireEvent(event);
			}			
			bFunction = true;								
		}										
	}	// end main:
	return bFunction;
	}


	@Override
	public boolean[] setStatusLocal(Enum[] objaEnumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isEmpty(objaEnumStatusIn)) {
				baReturn = new boolean[objaEnumStatusIn.length];
				int iCounter=-1;
				for(Enum objEnumStatus:objaEnumStatusIn) {
					iCounter++;
					boolean bReturn = this.setStatusLocal(objEnumStatus, bStatusValue);
					baReturn[iCounter]=bReturn;
				}
			}
		}//end main:
		return baReturn;
	}

	

	@Override
	public boolean proofStatusLocalExists(Enum objEnumStatus) throws ExceptionZZZ {
		return this.proofStatusLocalExists(objEnumStatus.name());
	}
	
	@Override
	public boolean getStatusLocal(String sStatusName) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sStatusName)) break main;
										
			HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
			Boolean objBoolean = hmStatus.get(sStatusName.toUpperCase());
			if(objBoolean==null){
				bFunction = false;
			}else{
				bFunction = objBoolean.booleanValue();
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
				
				//Setze das Flag nun in die HashMap
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
				
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.
				if(this.objEventStatusLocalBroker!=null) {
					IEventObjectStatusLocalSetOVPN event = new EventObjectStatusLocalSetOVPN(this,1,sStatusName.toUpperCase(), bStatusValue);
					this.objEventStatusLocalBroker.fireEvent(event);
				}
				
				bFunction = true;								
			}										
		}	// end main:
		
		return bFunction;
	}
	
	@Override
	public boolean[] setStatusLocal(String[] saStatusName, boolean bStatusValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!StringArrayZZZ.isEmptyTrimmed(saStatusName)) {
				baReturn = new boolean[saStatusName.length];
				int iCounter=-1;
				for(String sStatusName:saStatusName) {
					iCounter++;
					boolean bReturn = this.setStatusLocal(sStatusName, bStatusValue);
					baReturn[iCounter]=bReturn;
				}
			}
		}//end main:
		return baReturn;
	}
	
	@Override
	public HashMap<String, Boolean> getHashMapStatusLocal() {
		return this.hmStatusLocal;
	}

	@Override
	public void setHashMapStatusLocal(HashMap<String, Boolean> hmStatusLocal) {
		this.hmStatusLocal = hmStatusLocal;
	}

	/**Gibt alle möglichen StatusLocal Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	@Override
	public String[] getStatusLocal() throws ExceptionZZZ {
		String[] saReturn = null;
		main:{	
			saReturn = StatusLocalHelperZZZ.getStatusLocalDirectAvailable(this.getClass());				
		}//end main:
		return saReturn;
	}
	
	/**Gibt alle "true" gesetzten StatusLocal - Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	@Override
	public String[] getStatusLocal(boolean bValueToSearchFor) throws ExceptionZZZ {
		return this.getStatusLocal_(bValueToSearchFor, false);
	}
	
	@Override
	public String[] getStatusLocal(boolean bValueToSearchFor, boolean bLookupExplizitInHashMap)throws ExceptionZZZ {
		return this.getStatusLocal_(bValueToSearchFor, bLookupExplizitInHashMap);
	}
	
	private String[]getStatusLocal_(boolean bValueToSearchFor, boolean bLookupExplizitInHashMap) throws ExceptionZZZ{
		String[] saReturn = null;
		main:{
			ArrayList<String>listasTemp=new ArrayList<String>();
			
			//FALLUNTERSCHEIDUNG: Alle gesetzten Status werden in der HashMap gespeichert. Aber die noch nicht gesetzten FlagZ stehen dort nicht drin.
			//                                  Diese kann man nur durch Einzelprüfung ermitteln.
			if(bLookupExplizitInHashMap) {
				HashMap<String,Boolean>hmStatus=this.getHashMapStatusLocal();
				if(hmStatus==null) break main;
				
				Set<String> setKey = hmStatus.keySet();
				for(String sKey : setKey){
					boolean btemp = hmStatus.get(sKey);
					if(btemp==bValueToSearchFor){
						listasTemp.add(sKey);
					}
				}
			}else {
				//So bekommt man alle Flags zurück, also auch die, die nicht explizit true oder false gesetzt wurden.						
				String[]saStatus = this.getStatusLocal();
				
				//20211201:
				//Problem: Bei der Suche nach true ist das egal... aber bei der Suche nach false bekommt man jedes der Flags zurück,
				//         auch wenn sie garnicht gesetzt wurden.
				//Lösung:  Statt dessen explitzit über die HashMap der gesetzten Werte gehen....						
				for(String sStatus : saStatus){
					boolean btemp = this.getStatusLocal(sStatus);
					if(btemp==bValueToSearchFor ){ //also 'true'
						listasTemp.add(sStatus);
					}
				}
			}
			saReturn = listasTemp.toArray(new String[listasTemp.size()]);
		}//end main:
		return saReturn;
	}
	
	@Override
	public boolean proofStatusLocalExists(String sStatusName) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(StringZZZ.isEmpty(sStatusName))break main;
			bReturn = StatusLocalHelperZZZ.proofStatusLocalDirectExists(this.getClass(), sStatusName);				
		}//end main:
		return bReturn;
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


	//### aus IEventBrokerStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
		//TODO: Entweder diese LocalStatus-Klassen allgemeingueltig machen
		//      ODER das gleiche Package für den Client machen.
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
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Merke: Das komplexere enum STATUSLOCAL in das Interface IClientMain verschoben, s. auch enum FLAGZ
	//       Dort ist es flexibler einbindbar, als hier in Form einer internen Klasse.
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}//END class

