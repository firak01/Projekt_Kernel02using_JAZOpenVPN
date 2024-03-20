package use.openvpn.server;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectWithStatusZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.abstractList.ArrayListZZZ;
import basic.zBasic.util.datatype.calling.ReferenceArrayZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import basic.zKernel.status.StatusLocalAvailableHelperZZZ;
import basic.zUtil.io.KernelFileExpansionZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;
import use.openvpn.AbstractMainOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.IApplicationOVPN;
import use.openvpn.IConfigStarterOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.STATUSLOCAL;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.status.EventObject4ServerMainStatusLocalSetOVPN;
import use.openvpn.server.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.server.status.IEventObject4ServerMainStatusLocalSetOVPN;
import use.openvpn.server.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.server.status.SenderObjectStatusLocalSetOVPN;

public class ServerMainOVPN extends AbstractMainOVPN implements IServerMainOVPN,IEventBrokerStatusLocalSetUserOVPN,IListenerObjectStatusLocalSetOVPN{
	protected ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	volatile ArrayList<File> listaConfigFile = null;
	volatile ArrayList<ServerConfigStarterOVPN> listaConfigStarter = new ArrayList<ServerConfigStarterOVPN>(); //Hierueber werden alle Prozesse, die mit einem bestimmten Konfigurations-File gestartet wurden festgehalten.	
	
	//Die Objekte, an die sich der Tray registriert und auf deren LocalStatus - Events er hoert.
	private ServerThreadProcessWatchMonitorOVPN  objMonitorProcess = null;
		
	public ServerMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
		ServerMainNew_();
	}
	
	private boolean ServerMainNew_() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			//try{		
			if(this.getFlag("init")) break main;
					
		//#################
        //Definiere einen Monitor, der die OVPN-Watch Processe beobachtet (die ihren jeweiligen, eigentlichen OVPN.exe Process beobachten)
		//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
		String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ServerMonitorRunnerOVPN-Object";
		System.out.println(sLog);
		this.getLogObject().WriteLineDate(sLog);
		
		//Idee: Den Monitor nach der erfolgreichen Verbindung einfach beenden.
		//String[] saFlagMonitorProcess = {IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};

		//Mit diesen Flags auf Pruefung der Statusaenderung, wird u.a. das Neustarten des Watcher-Processes indirekt verhindert.
		//           siehe: ClientThreadProcessWatchMonitorOVPN.run - Line: 93: Starting monitor thread canceled.
		String[] saFlagMonitorProcess = {IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
		this.objMonitorProcess = new ServerThreadProcessWatchMonitorOVPN(this.getKernelObject(), this, saFlagMonitorProcess);				
		this.registerForStatusLocalEvent(this.objMonitorProcess);//Den Thread am Main-Backend-Objekt registrieren			
		this.objMonitorProcess.registerForStatusLocalEvent(this); //Das Main-Backend-Objekt am MonitorProcess registrieren
		
		//Monitor noch nicht starten!!!
		//Thread objThreadProcessMonitor = new Thread(this.objMonitorProcess);
		//objThreadProcessMonitor.start();

		bReturn = true;
	}//end main:
	return bReturn;
	}
	
	
	/**Starten des ServerBackends.
	 * - Aufgeteil in einen "Warte auf windows-Task"-Teil und in einen OVPN-Teil
	 * - Erst sollte das Warten auf den Windows - Task passieren... und zwar in einer Schleife... dann erst OVPN Dinge
	 * 
	 * Merke: Weil bei Änderung des Status ein Event erzeugt und geworfen wird, braucht man z.B. kein ServerTrayUI-Objekt.
	 *        Das ServerTrayUI-Objekt muss an dem Event registriert sein.
	 * 
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 03.08.2023, 09:31:22
	 */
	public boolean startAsThread() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			String sLog=null;
			
			//TODOGOON20231125; //TESTEN
			
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.						
			boolean bStatusLocalSet = this.switchStatusLocalForGroupTo(ServerMainOVPN.STATUSLOCAL.ISSTARTING, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
			if(!bStatusLocalSet) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			
			
			//1. Diverse Dinge mit WMI testen.
			KernelWMIZZZ objWMI = new KernelWMIZZZ(objKernel, null);
			
			//++++++++++++++++++++++++++++++
			//Starterlaubnis: Läuft schon ovpn ???
			this.logProtocolString("Searching for configuration template files 'Template*.ovpn'"); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			IKernelZZZ objKernel = this.getKernelObject();			
			
			ServerApplicationOVPN objApplication = (ServerApplicationOVPN) this.getApplicationObject();//Im UI erzeugt und übergeben.
			ConfigChooserOVPN objChooser = new ConfigChooserOVPN(objKernel,"server", objApplication);
			this.setConfigChooserObject(objChooser);
			
		   //TODO KernelWMIZZZ eines win32 - packages.
			File objFileExe = ServerConfigFileOVPN.findFileExe();   //Anders als im Client muss nix weiter gemacht werden
			if(objFileExe!=null){
				String sExeCaption = objFileExe.getName();
								
				boolean bproof = objWMI.isProcessRunning(sExeCaption);
				if(bproof==true){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "There were found processes '"+ sExeCaption + "' still running. Will not start new ones. Quitting.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				
			}else{
				//Wahrscheinlich ist .ovpn garnicht als Dateiendung installiert. Abbrechen.
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "OVPN seems not to be installed. '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			this.logProtocolString("Open VPN installed and not yet running. Continue starting process.");
			
			//2.  Läuft schon ein  benötigter anderer Prozesss
			 //+++++++++++++++++++++++++++++++			
			//Starterlaubnis: Läuft der benoetigte Task, z.B. der Domino Server schon ???
			//Falls nicht: Warte eine konfigurierte Zeit. (Merke: Nicht abbrechen, weil ja ggf. Probleme existieren oder der Server gar nicht starten soll).
			String sDominoCaption = objKernel.getParameterByProgramAlias("OVPN","ProgProcessCheck","Process2Check").getValue();
			if(!StringZZZ.isEmpty(sDominoCaption)){
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Warte auf Start von Prozess '" + sDominoCaption + "'");
				
				String sTimeoutSecond = objKernel.getParameterByProgramAlias("OVPN","ProgProcessCheck","CheckTimeout").getValue();
				if(StringZZZ.isEmpty(sTimeoutSecond)){
					sTimeoutSecond = "1";
				}				
				int iTimeoutSecond = Integer.parseInt(sTimeoutSecond);
				boolean bProof = false;
				do{					
					bProof = objWMI.isProcessRunning(sDominoCaption);
					if(bProof == false){
						try{
							Thread.sleep(1000); //DIESEN THREAD fuer 1 Sekunde anhalten
							iTimeoutSecond--;
							//System.out.println(iTimeoutSecond);
							if(iTimeoutSecond<=0){
								ExceptionZZZ ez = new ExceptionZZZ(sERROR_RUNTIME_TIMEOUT + "Waiting for domino-servertask to start - '" + sDominoCaption + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
								throw ez;
							}
						}catch(InterruptedException e){
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_RUNTIME + "Thread.sleep(...). " + e.getMessage(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
					}//bProof == false
				}while(bProof==false);
				this.logProtocolString("Depending process '" + sDominoCaption + "' running. Continue starting process.");
			}//END if sDominoCaption isempty
			
			
			//+++++++++++++++++++++++++++++++++++++++++
			//3. OVPN Dateien fertig machen
			//TODO Dies in eine Methode "find fileConfigAvailableAndConfigured"
			this.logProtocolString("Searching for configuration template files '*.ovpn'"); //Darueber kann dann ggf. ein Frontend den laufenden Process beobachten.			
			
			//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 						
			//Die Konfigurations-Template Dateien finden
			String sDirectoryConfigPath = objChooser.readDirectoryConfigPath();
			
			File[] objaFileConfigTemplate = objChooser.findFileConfigOvpnTemplates(null);
			if(objaFileConfigTemplate==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + sDirectoryConfigPath + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(objaFileConfigTemplate.length == 0){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + sDirectoryConfigPath + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				this.logProtocolString(objaFileConfigTemplate.length + " configuration TEMPLATE file(s) was (were) found in the directory: '" + sDirectoryConfigPath + "'");  //Darueber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
			//####################################################################
			//### DAS SCHREIBEN DER NEUEN KONFIGURATION
					
			//+++ A) Vorbereitung			
			//+++ 1. Die früher mal verwendeten Dateien entfernen
			this.logProtocolString("Removing former configuration file(s)."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			ReferenceArrayZZZ<String> strUpdate=new ReferenceArrayZZZ<String>(null);
			int itemp = objChooser.removeFileConfigUsed(null,strUpdate);			
			this.logMessageString(strUpdate);
			
			//+++ B) Die gefundenen Werte überall eintragen: IN neue Dateien
			this.logProtocolString("Creating new configuration-file(s) from template-file(s), using new line(s)");					
			//ArrayList listaFileConfig = new ArrayList(objaFileConfigTemplate.length);
			for(int icount = 0; icount <= objaFileConfigTemplate.length-1; icount++){	
				File fileConfigTemplateOvpnUsed = objaFileConfigTemplate[icount];
				ServerConfigMapper4TemplateOVPN objMapper = new ServerConfigMapper4TemplateOVPN(objKernel, this, fileConfigTemplateOvpnUsed);
				this.setConfigMapperObject(objMapper);
				
				//Mit dem Setzen der neuen Datei, basierend auf dem Datei-Template wird intern ein Parser für das Datei-Template aktiviert
				ServerConfigTemplateUpdaterOVPN objUpdater = new ServerConfigTemplateUpdaterOVPN(objKernel, this, objChooser, objMapper, null);
				File objFileNew = objUpdater.refreshFileUsed(fileConfigTemplateOvpnUsed);	
				if(objFileNew==null){
					this.logProtocolString("Unable to create 'used file' file base on template template: '" + objaFileConfigTemplate[icount].getPath() + "'");					
				}else{
					
					boolean btemp = objUpdater.update(objFileNew, true); //Bei false werden also auch Zeilen automatisch hinzugefügt, die nicht im Template sind. Z.B. Proxy-Einstellungen.
					if(btemp==true){
						this.logProtocolString( "'Used file' successfully created for template: '" + objaFileConfigTemplate[icount].getPath() + "'");
		
						//+++ Nun dieses used-file dem Array hinzufuegen, dass fuer den Start der OVPN-Verbindung verwendet wird.
						//listaFileConfig.add(objUpdater.getFileUsed());
						this.getConfigFileObjectsAll().add(objUpdater.getFileUsed());
					}else{
						this.logProtocolString( "'Used file' not processed, based upon: '" + objaFileConfigTemplate[icount].getPath() + "'");					
					}	
				}
			}//end for
			
			//ABER: Es werden nur die Konfigurationsdateien verwendet, die auch konfiguriert sind. Falls nix konfiguriert ist, werden alle gefundenen verwendet.
			ArrayList<File> listaFileConfigUsed = this.getConfigFileObjectsAll();		
			String[] saFileConfigConfigured = objKernel.getParameterArrayWithStringByProgramAlias("OVPN", "ProgConfigHandler", "ConfigFile");
			File[] objaFileConfigUsed = objChooser.findFileConfigUsed(null);//.findFileConfigTemplate(null);
			
			listaFileConfigUsed.clear();//Baue die Liste der letztendlich genutzten Konfigurationen neu auf.			
			if(StringArrayZZZ.isEmpty(saFileConfigConfigured)){
				//Wenn true: Die Arraylist besteht nun aus allen konfigurierten Dateien
				for(int icount=0; icount <= objaFileConfigUsed.length-1; icount++){
					listaFileConfigUsed.add(objaFileConfigUsed[icount]);
				}
			}else{
				//Aus der Liste aller zur Verfügung stehenden Dateien nur diejenigen raussortieren, die konfiguriert sind.
				for(int iCounter=0;iCounter<=saFileConfigConfigured.length-1;iCounter++) {
					String sFileConfigConfigured = saFileConfigConfigured[iCounter];				
					StringTokenizer objToken = new StringTokenizer(sFileConfigConfigured, File.separator);
					while(objToken.hasMoreTokens()){
						String stemp = objToken.nextToken();
						for(int icount=0; icount <= objaFileConfigUsed.length-1; icount++){
							File objFileTemp = objaFileConfigUsed[icount];						
							boolean bIsExpandedOrSameFilename = KernelFileExpansionZZZ.isExpansionOrSameFilename(objFileTemp, stemp, 3);
							if(bIsExpandedOrSameFilename) {
								listaFileConfigUsed.add(objFileTemp);
							}
						}//END for
					}//END while
				}//END for
			}//END if
			if(listaFileConfigUsed.size()==0){
				this.logProtocolString("No configuration available which is configured in Kernel Ini File for Program 'ProgConfigHandler' and Property 'ConfigFile'.");
				bReturn = false;
				break main;
			}else{
				String stemp = "";
				for(int icount=0; icount <= listaFileConfigUsed.size()-1; icount++){										
					File objFileTemp = (File) listaFileConfigUsed.get(icount);
					if(stemp.equals("")){
						stemp = objFileTemp.getName();
					}else{
						stemp = stemp + "; " + objFileTemp.getName();	
					}					 
				}//END for
				this.logProtocolString("Finally used configuration  file(s): " + stemp);
			}
						
			//#############
			//Auslesen der "erlaubten" Clients und Datei dafür in einem bestimmten Verzeichnis anlegen.			
			//Das wird alles in einem Handler gekapselt, den Namen (auch der Methode noch optimieren)
			ServerConfigOnServerAllowedClientFacadeOVPN objClientConfigHandler = new ServerConfigOnServerAllowedClientFacadeOVPN(this.getKernelObject(), this, null);
			boolean bSuccess = objClientConfigHandler.execute();
			
			//############
			//Erstelle die Konfigurationstprocesse
			int iNumberOfProcessStarted = 0;
			for(int icount=0; icount <= listaFileConfigUsed.size()-1; icount++){
				File objFileConfigOvpn = (File) listaFileConfigUsed.get(icount);
				String sAlias = Integer.toString(icount);								
				File objFileTemplateBatch = objChooser.findFileConfigBatchTemplateFirst();				
				
				boolean bByGui = objKernel.getParameterBooleanByProgramAlias("OVPN","ProgProcessCheck","startByOvpnGUI");
				boolean bByBatch = objKernel.getParameterBooleanByProgramAlias("OVPN","ProgProcessCheck","startByBatch");
				if(bByGui && bByBatch) {
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_CONFIGURATION_VALUE + "OVPN kann entweder als Batch oder als GUI gestartet werden. '" + objChooser.readDirectoryConfigPath() + "'", iERROR_CONFIGURATION_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				
				ArrayList<String> listasFlagStarter = new ArrayList<String>();
				if(bByGui) {
					//saFlagStarter = {IConfigStarterOVPN.FLAGZ.BY_OVPNGUI.name()}; 
					listasFlagStarter.add(IConfigStarterOVPN.FLAGZ.BY_OVPNGUI.name());
				}
				if(bByBatch) {
					//saFlagStarter = {IConfigStarterOVPN.FLAGZ.BY_BATCH.name()}; 
					listasFlagStarter.add(IConfigStarterOVPN.FLAGZ.BY_BATCH.name()); //Problem: Wenn man eine Batch startet, kann man nicht auf den Output eines in der Batch gestarteten Programs zugreifen.
				}
								
				String[] saTemp = ArrayListZZZ.toStringArray(listasFlagStarter);
				ServerConfigStarterOVPN objStarter = new ServerConfigStarterOVPN(objKernel, (IMainOVPN) this, icount, objFileTemplateBatch, objFileConfigOvpn, sAlias, saTemp);
				this.addProcessStarter(objStarter);
			}//end for
			
			//############
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.						
			bStatusLocalSet = this.switchStatusLocalForGroupTo(ServerMainOVPN.STATUSLOCAL.ISSTARTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
			if(!bStatusLocalSet) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				break main;
			}
			
			
			
			//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.									
		   bReturn = true;
		}//END main		
		return bReturn;
	}//END start()
	
	public void run() {
		try {
			this.startAsThread();
		} catch (ExceptionZZZ ez) {
			try {
				String sLog = ez.getDetailAllLast();
				this.logProtocolString("An error happend: '" + sLog + "'");
				this.setStatusLocal(ServerMainOVPN.STATUSLOCAL.HASERROR, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
				
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
	
	
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sMessage 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 * @throws ExceptionZZZ 
	 */
	public void logProtocolString(String sMessage) throws ExceptionZZZ{
		if(sMessage!=null){
			this.addProtocolString(sMessage);
			
			IKernelZZZ objKernel = this.getKernelObject();
			if(objKernel!= null){
				objKernel.getLogObject().WriteLineDate(sMessage);
			}
		}
	}
	
	public void logMessageString(String[] saMessage) throws ExceptionZZZ {
		if(saMessage!=null) {
			int iMax = Array.getLength(saMessage)-1;
			for(int icount=0;icount<=iMax;icount++) {
				this.logProtocolString(saMessage[icount]);
			}
		}		
	}
	
	public void logMessageString(ArrayList<String>lista) throws ExceptionZZZ {
		String[]sa = ArrayListZZZ.toStringArray(lista);
		this.logMessageString(sa);
	}
	
	public void logMessageString(ReferenceArrayZZZ<String>strStatus) throws ExceptionZZZ {
		ArrayList<String>listas=strStatus.getArrayList();
		this.logMessageString(listas);
	}
		
	 public  boolean addProcessStarter(ServerConfigStarterOVPN objStarter){
		 boolean bReturn = false;
		 main:{
			 check:{
				 if(objStarter==null) break main;
			 }//END check
		 
		 	this.getServerConfigStarterList().add(objStarter);
		 
		 }//END main
		 return bReturn;
	 }
	 
	 /**returns a ConfigStarterZZZ-object.
	 * @param iPosition, Remark: Starting at index postion 1
	 * @return boolean
	 *
	 * javadoc created by: 0823, 24.07.2006 - 09:32:09
	 */
	 @Override
	public ServerConfigStarterOVPN getServerConfigStarter(int iPosition){
		ServerConfigStarterOVPN objReturn=null;
		main:{
			if(iPosition<= 0) break main;
				 
			ArrayList<ServerConfigStarterOVPN> listaConfigStarter = this.getServerConfigStarterList();
			if(iPosition > listaConfigStarter.size()) break main;
			
		 	objReturn = (ServerConfigStarterOVPN) listaConfigStarter.get(iPosition);
		 	
		 }//END main
		 return objReturn;
	 }
	 
	 @Override
	 public ArrayList<ServerConfigStarterOVPN> getServerConfigStarterList() {
		if(this.listaConfigStarter==null) {
			this.listaConfigStarter=new ArrayList<ServerConfigStarterOVPN>();
		}
		return this.listaConfigStarter;
	}
	
	/**Returns an ArrayList,which contains all ConfigStarterZZZ-objects
	 * @return ArrayList
	 *
	 * javadoc created by: 0823, 28.07.2006 - 13:51:51
	 */
	public ArrayList getProcessStarterAll(){
		return listaConfigStarter;
	}
	
	public boolean isStartingOnLaunch() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht. 
		String stemp = this.objKernel.getParameter("StartingOnLaunch").getValue();
		if(stemp==null) break main;
		if(stemp.equals("1")){
			bReturn = true;
		}
		}//END main
		return bReturn;
	}
	
	public boolean isListenOnStart() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht. 
		String stemp = this.objKernel.getParameter("ListenOnStart").getValue();
		if(stemp==null) break main;
		if(stemp.equals("1")){
			bReturn = true;
		}
		}//END main
		return bReturn;
	}
		
	public ArrayList<File> getConfigFileObjectsAll() {
		if(this.listaConfigFile==null) {
			this.listaConfigFile = new ArrayList<File>();
		}
		return this.listaConfigFile;
	}
	public void setConfigFileObjectsAll(ArrayList<File> listaConfigFile) {
		this.listaConfigFile = listaConfigFile;
	}
	
	//######################################################
	//### Getter / Setter
	public ServerThreadProcessWatchMonitorOVPN  getProcessMonitorObject(){
		return this.objMonitorProcess;
	}
	public void setProcessMonitorObject(ServerThreadProcessWatchMonitorOVPN objMonitor){
		this.objMonitorProcess = objMonitor;
	}
	
	
	//##########################################
	//### IStatusLocalUserZZZ
	/** DIESE METHODEN MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHREN STATUS SETZEN WOLLEN*/
	
	/* (non-Javadoc)
	 * @see basic.zKernel.status.IStatusLocalUserZZZ#getStatusLocal(java.lang.Enum)
	 */
	@Override
	public boolean getStatusLocal(Enum enumStatusIn) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
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
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal(enumStatus, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
								
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal(enumStatus, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ServerMainOVPN.STATUSLOCAL enumStatus = (ServerMainOVPN.STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	//################################################
		//+++ aus IStatusLocalUserMessageZZZ			
		@Override 
		public boolean setStatusLocal(Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal(enumStatus, sMessage, bStatusValue);
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
				ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sMessage, bStatusValue);
			}//end main:
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, sMessage, bStatusValue);
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
				ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	@Override 
	public boolean offerStatusLocal(Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
								
			bFunction = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	
	
	
	@Override
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
								
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) break main;
			
		//Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
	    IServerMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
		String sStatusName = enumStatus.name();
		bReturn = this.proofStatusLocalExists(sStatusName);
		if(!bReturn) {
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMainOVPN would like to fire event, but this status is not available: '" + sStatusName + "'";
			this.logProtocolString(sLog);
			break main;
		}
		
		bReturn = this.proofStatusLocalValue(sStatusName, bStatusValue);
		if(!bReturn) {
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMainOVPN would like to fire event, but this status has a value to be ignored: '" + sStatusName + "'";
			this.logProtocolString(sLog);
			break main;
		}
			
		
		//++++++++++++++++++++
		//Setze das Flag nun in die HashMap
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
		String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
		this.logProtocolString(sLog);
		
		//Falls eine Message extra uebergeben worden ist, ueberschreibe...
		if(sStatusMessageToSet!=null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain setze sStatusMessageToSet='" + sStatusMessageToSet + "'";
			this.logProtocolString(sLog);			
		}
				
		//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
		this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);

		//++++++++++++++++++++++++++
		//Besonderheiten im Server-Main-Objekt
		ServerConfigStarterOVPN objServerConfigStarter = null;
		if(iIndexOfProcess>=0) {
			objServerConfigStarter = this.getServerConfigStarterList().get(iIndexOfProcess);
		}
		
		//Merke: Anders als beim Client wird das hier nicht einer Liste hinzugefuegt.
		//++++++++++++++++++++++++++
		
		
		//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
		//Dann erzeuge den Event und feuer ihn ab.
		//Merke: Nun aber ueber das enum			
		if(this.getSenderStatusLocalUsed()==null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
			this.logProtocolString(sLog);	
			break main;
		}
		
		//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
		sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";
		this.logProtocolString(sLog);
		IEventObject4ServerMainStatusLocalSetOVPN event = new EventObject4ServerMainStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);
		event.setApplicationObjectUsed(objApplication);
		
		//das ServerStarterObjekt nun auch noch dem Event hinzufuegen
		if(objServerConfigStarter!=null) {			
				event.setServerConfigStarterObjectUsed(objServerConfigStarter);
		}
		
		sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain feuert event '" + enumStatus.getAbbreviation() + "'";
		this.logProtocolString(sLog);
		this.getSenderStatusLocalUsed().fireEvent(event);
				
		bReturn = true;																			
		}	// end main:
	return bReturn;
	}	
	
	@Override
	public boolean[] setStatusLocal(Enum[] objaEnumStatus, boolean bStatusValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isEmpty(objaEnumStatus)) {
				baReturn = new boolean[objaEnumStatus.length];
				int iCounter=-1;
				for(Enum objEnumStatus:objaEnumStatus) {
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
	public boolean getStatusLocal(String sStatusName) {
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
			if(StringZZZ.isEmpty(sStatusName)) break main;						
			boolean bProof = this.proofStatusLocalExists(sStatusName);															
			if(!bProof) break main;
				
			//Setze den Status nun in die HashMap
			HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
			hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
				
			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.
			IEventObject4ServerMainStatusLocalSetOVPN event = null;
			if(sStatusName.equalsIgnoreCase(IServerMainOVPN.STATUSLOCAL.ISSTARTING.getName())){
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";				
				this.logProtocolString(sLog);					
				event = new EventObject4ServerMainStatusLocalSetOVPN(this,1,STATUSLOCAL.ISSTARTING, true);
			}else {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": KEIN Event erzeugt fuer '" + sStatusName + "'";
				this.logProtocolString(sLog);
			}
			
			bFunction = true;																
			}	// end main:
		return bFunction;	
	}
	
	@Override
	public boolean[] setStatusLocal(String[] saStatus, boolean bValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!StringArrayZZZ.isEmptyTrimmed(saStatus)) {
				baReturn = new boolean[saStatus.length];
				int iCounter=-1;
				for(String sStatusName:saStatus) {
					iCounter++;
					boolean bReturn = this.setStatusLocal(sStatusName, bValue);
					baReturn[iCounter]=bReturn;
				}
			}
		}//end main:
		return baReturn;
	}
		
	/**Gibt alle möglichen StatusLocal Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	public String[] getStatusLocalAll() throws ExceptionZZZ{
		String[] saReturn = null;
		main:{	
			saReturn = StatusLocalAvailableHelperZZZ.getStatusLocalDirect(this.getClass());				
		}//end main:
		return saReturn;
	}
	
	/**Gibt alle "true" gesetzten StatusLocal - Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	public String[] getStatusLocal(boolean bStatusValueToSearchFor) throws ExceptionZZZ{
		return this.getStatusLocal_(bStatusValueToSearchFor, false);
	}
	
	public String[] getStatusLocal(boolean bStatusValueToSearchFor, boolean bLookupExplizitInHashMap) throws ExceptionZZZ{
		return this.getStatusLocal_(bStatusValueToSearchFor, bLookupExplizitInHashMap);
	}
	
	private String[]getStatusLocal_(boolean bStatusValueToSearchFor, boolean bLookupExplizitInHashMap) throws ExceptionZZZ{
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
					if(btemp==bStatusValueToSearchFor){
						listasTemp.add(sKey);
					}
				}
			}else {
				//So bekommt man alle Flags zurück, also auch die, die nicht explizit true oder false gesetzt wurden.						
				String[]saStatus = this.getStatusLocalAll();
				
				//20211201:
				//Problem: Bei der Suche nach true ist das egal... aber bei der Suche nach false bekommt man jedes der Flags zurück,
				//         auch wenn sie garnicht gesetzt wurden.
				//Lösung:  Statt dessen explitzit über die HashMap der gesetzten Werte gehen....						
				for(String sStatus : saStatus){
					boolean btemp = this.getStatusLocal(sStatus);
					if(btemp==bStatusValueToSearchFor ){ //also 'true'
						listasTemp.add(sStatus);
					}
				}
			}
			saReturn = listasTemp.toArray(new String[listasTemp.size()]);
		}//end main:
		return saReturn;
	}
	
	/** DIESE METHODE MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung ODER Interface Implementierung -, DIE IHRE FLAGS SETZEN WOLLEN
	 *  SIE WIRD PER METHOD.INVOKE(....) AUFGERUFEN.
	 * @param name 
	 * @param sFlagName
	 * @return
	 * lindhaueradmin, 23.07.2013
	 * @throws ExceptionZZZ 
	 */
	public boolean proofStatusLocalExists(String sStatusName) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(StringZZZ.isEmpty(sStatusName))break main;
			bReturn = StatusLocalAvailableHelperZZZ.proofStatusLocalDirectExists(this.getClass(), sStatusName);				
		}//end main:
		return bReturn;
	}
	
	@Override
	public boolean proofStatusLocalValueChanged(Enum objEnumStatus, boolean bValue) throws ExceptionZZZ {
		return this.proofStatusLocalValueChanged(objEnumStatus.name(), bValue);
	}

	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#proofStatusLocalValueChanged(java.lang.String, boolean)
	 */
	@Override
	public boolean proofStatusLocalValueChanged(String sStatusName, boolean bValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(StringZZZ.isEmpty(sStatusName))break main;
			
			HashMap<String,Boolean>hmStatusLocal = this.getHashMapStatusLocal();
			bReturn = StatusLocalAvailableHelperZZZ.proofStatusLocalChanged(hmStatusLocal, sStatusName, bValue);
			
		}//end main:
		return bReturn;
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
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker) {
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
	
	//#############################
	//#######################################	
	@Override
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(objEnumStatusIn==null) break main;
				
			//Fuer das Main-Objekt ist erst einmal jeder Status relevant
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	@Override
	public boolean changeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Das Main Objekt ist woanders registriert.
		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
			System.out.println(sLog);
			this.logProtocolString(sLog);
				
						//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
			
//			if(eventStatusLocalSet.getStatusEnum() instanceof IClientMainOVPN.STATUSLOCAL){
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 01");
//				bReturn = this.statusLocalChangedMainEvent_(eventStatusLocalSet);
//				break main;
//				
//			}else 

			if (eventStatusLocalSet.getStatusEnum() instanceof IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST server12");
				bReturn = this.changeStatusLocalMonitorEvent_(eventStatusLocalSet);
				break main;
				
//			}else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL) {
//				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 13");
//				bReturn = this.statusLocalChangedPingerEvent_(eventStatusLocalSet);
//				break main;		
				
			}else{	
				System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST server00 ELSE");
				
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
private boolean changeStatusLocalMonitorEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
	boolean bReturn=false;
	main:{	
		if(eventStatusLocalSet==null)break main;
					
		String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
		System.out.println(sLog);
		this.logProtocolString(sLog);
		
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++			
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
		
		ServerConfigStarterOVPN objStarter = eventStatusLocalSet.getServerConfigStarterObjectUsed();
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
		IServerMainOVPN.STATUSLOCAL objEnum = (IServerMainOVPN.STATUSLOCAL) hmEnum.get(enumStatus);			
		if(objEnum==null) {
			sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
			System.out.println(sLog);
			this.logProtocolString(sLog);
			break main;
		}
		
		boolean bValue = eventStatusLocalSet.getStatusValue();
		
//		boolean bHasError = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR)&& bValue;
//		boolean bEnded = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED) && bValue;
//		boolean bHasConnection = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION) && bValue;
//		boolean bHasConnectionLost = objEnum.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST) && bValue;
//	
		boolean bEventHasError = objEnum.equals(IServerMainOVPN.STATUSLOCAL.HASERROR);
		boolean bEventEnded = objEnum.equals(IServerMainOVPN.STATUSLOCAL.ISSTOPPED);
		
		boolean bEventHasConnection = objEnum.equals(IServerMainOVPN.STATUSLOCAL.ISLISTENERCONNECTED);		
		boolean bEventHasConnectionLost = objEnum.equals(IServerMainOVPN.STATUSLOCAL.ISLISTENERINTERRUPTED);
					
		int iIndex = -1;			
		objStarter = eventStatusLocalSet.getServerConfigStarterObjectUsed();
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
		
		
		///##################################################
		///##################################################
		///##################################################
		///##################################################
		///##################################################
		
		
		String sStatusMessage = eventStatusLocalSet.getStatusMessage();	
		boolean bStatusLocalSet = this.switchStatusLocalForGroupTo(iIndex, (IEnumSetMappedStatusZZZ)objEnum, sStatusMessage, bValue);//Es wird ein Event gefeuert, an dem das Tray-Objekt und andere registriert sind und dann sich passend einstellen kann.
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
			bReturn = this.isEventRelevantByClass(eventStatusLocalSet);
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
						
			bReturn = true;
		}//end main:
		return bReturn;
	}
	

	/* (non-Javadoc)
	 * @see use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN#isEventRelevantByStatusLocal(use.openvpn.server.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			IEnumSetMappedStatusZZZ enumStatus = eventStatusLocalSet.getStatusEnum();							
			bReturn = this.isStatusLocalRelevant(enumStatus);
			if(!bReturn) break main;
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
			if(eventStatusLocalSet.getStatusEnum() instanceof IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse ist instanceof IClientThreadProcessWatchMonitorOVPN. Damit relevant.";
				System.out.println(sLog);
				this.logProtocolString(sLog);
				bReturn = true;
				break main;
			}
		}//end main:
		return bReturn;
	}
		
	//### aus IListenerObjectStatusLocalMapForEventUserZZZ
		@Override
		public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> createHashMapEnumSetForCascadingStatusLocalCustom() {
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmReturn = new HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>();
			main:{
				
				//Reine Lokale Statuswerte kommen nicht aus einem Event und werden daher nicht gemapped. 
				//Alos: Externer Status -> Lokaler Status
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTNEW, IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTNEW);
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTING);
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTED, IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTED);	
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTNO, IServerMainOVPN.STATUSLOCAL.ISLISTENERSTARTNEW);//!!! Also wieder auf einen Server ist gestartet Status zurücksetzen !!!
				
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTNEW, IServerMainOVPN.STATUSLOCAL.ISLISTENERCONNECTING);
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTING, IServerMainOVPN.STATUSLOCAL.ISLISTENERCONNECTING);				
				hmReturn.put(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTED, IServerMainOVPN.STATUSLOCAL.ISLISTENERCONNECTING);
				
		
			}//end main:
			return hmReturn;
		}
		
}//END class
