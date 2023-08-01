package use.openvpn.server;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import use.openvpn.AbstractMainOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.serverui.ServerTrayStatusZZZ.ServerTrayStatusTypeZZZ;
import use.openvpn.serverui.ServerTrayStatusZZZ;
import use.openvpn.serverui.ServerTrayUIZZZ;
import use.openvpn.ProcessWatchRunnerZZZ;
import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigMapper4TemplateOVPN;
import use.openvpn.client.ClientConfigTemplateUpdaterZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.EventObjectFlagZsetZZZ;
import basic.zKernel.flag.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.flag.ISenderObjectFlagZsetZZZ;
import basic.zKernel.flag.KernelSenderObjectFlagZsetZZZ;
import basic.zKernel.flag.json.FlagZHelperZZZ;
import basic.zKernel.status.EventObjectStatusLocalSetZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalSetZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalSetZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;
import basic.zKernel.status.KernelSenderObjectStatusLocalSetZZZ;
import basic.zKernel.status.StatusLocalHelperZZZ;
import basic.zUtil.io.KernelFileExpansionZZZ;
import custom.zUtil.io.FileExpansionZZZ;
import custom.zUtil.io.FileZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractList.ArrayListZZZ;
import basic.zBasic.util.datatype.calling.ReferenceArrayZZZ;
import basic.zBasic.util.datatype.calling.ReferenceZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.FileTextWriterZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;

public class ServerMainZZZ extends AbstractMainOVPN implements IServerMainOVPN{
//	private ServerApplicationOVPN objApplication = null;//Objekt, dass Werte, z.B. aus der Kernelkonfiguration holt/speichert
//	private ConfigChooserOVPN objConfigChooser = null;   //Objekt, dass Templates "verwaltet"
//	private ServerConfigMapperOVPN objConfigMapper = null; //Objekt, dass ein Mapping zu passenden Templatezeilen verwaltet.
	private ArrayList<File> listaConfigFile = null;

	private String sMessageCurrent = null; //Hierueber kann das Frontend abfragen, was gerade in der Methode "start()" so passiert.
	private ArrayList listaMessage = new ArrayList(); //Hierueber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
    																	//Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen koennen.
	private ArrayList listaConfigStarter = new ArrayList(); //Hier�ber werden alle Procese, die mit einem bestimmten Konfigurations-File gestartet wurden festgehalten.	
	private HashMap<String, Boolean>hmStatusLocal = new HashMap<String, Boolean>(); //Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen koennen.

	protected ISenderObjectStatusLocalSetZZZ objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	public ServerMainZZZ(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
	}

	/**Entrypoint for managing the configuration files for "OpenVPN" client.
	 * @param args, 
	 *
	 * @return void
	 *
	 * javadoc created by: 0823, 30.06.2006 - 10:24:31
	 */
	public boolean start()throws ExceptionZZZ{
		return this.start(null);
	}
	
	public boolean start(ServerTrayUIZZZ objServerTray) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			//TODOGOON20230727; //Aufteilen in einen OVPN-Teil und einen "Warte auf windows-Task"-Teil
			//TodoGOON: im ServerSystemTray muss das dann Heissen... Starte OVPN.
			//TODOGOON: Erst sollte das Warten auf den Windows - Task passieren... und zwar in einer Schleife... dann erst OVPN Dinge
			//this.setFlag("isstarting", true);
			
			//Merke: Wenn über das enum der setFlag gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.
			//this.setFlag(ServerTrayStatusZZZ.ServerTrayStatusTypeZZZ.STARTING, true);
			this.setStatusLocal(IServerMainOVPN.STATUSLOCAL.ISSTARTING, true);
			if(objServerTray!=null) objServerTray.switchStatus(ServerTrayStatusTypeZZZ.STARTING);
									
			//1. Diverse Dinge mit WMI testen.
			KernelWMIZZZ objWMI = new KernelWMIZZZ(objKernel, null);
			
			//++++++++++++++++++++++++++++++
			//Starterlaubnis: Läuft schon ovpn ???
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
			this.logMessageString("Open VPN installed and not yet running. Continue starting process.");
			
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
				this.logMessageString("Depending process '" + sDominoCaption + "' running. Continue starting process.");
			}//END if sDominoCaption isempty
			
			
			//+++++++++++++++++++++++++++++++++++++++++
			//3. OVPN Dateien fertig machen
			//TODO Dies in eine Methode "find fileConfigAvailableAndConfigured"
			this.logMessageString("Searching for configuration template files '*.ovpn'"); //Darueber kann dann ggf. ein Frontend den laufenden Process beobachten.			
			
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
				this.logMessageString(objaFileConfigTemplate.length + " configuration TEMPLATE file(s) was (were) found in the directory: '" + sDirectoryConfigPath + "'");  //Darueber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
			//####################################################################
			//### DAS SCHREIBEN DER NEUEN KONFIGURATION
					
			//+++ A) Vorbereitung			
			//+++ 1. Die früher mal verwendeten Dateien entfernen
			this.logMessageString("Removing former configuration file(s)."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			ReferenceArrayZZZ<String> strUpdate=new ReferenceArrayZZZ<String>(null);
			int itemp = objChooser.removeFileConfigUsed(null,strUpdate);			
			this.logMessageString(strUpdate);
			
			//+++ B) Die gefundenen Werte überall eintragen: IN neue Dateien
			this.logMessageString("Creating new configuration-file(s) from template-file(s), using new line(s)");					
			//ArrayList listaFileConfig = new ArrayList(objaFileConfigTemplate.length);
			for(int icount = 0; icount <= objaFileConfigTemplate.length-1; icount++){	
				File fileConfigTemplateOvpnUsed = objaFileConfigTemplate[icount];
				ServerConfigMapper4TemplateOVPN objMapper = new ServerConfigMapper4TemplateOVPN(objKernel, this, fileConfigTemplateOvpnUsed);
				this.setConfigMapperObject(objMapper);
				
				//Mit dem Setzen der neuen Datei, basierend auf dem Datei-Template wird intern ein Parser für das Datei-Template aktiviert
				ServerConfigTemplateUpdaterOVPN objUpdater = new ServerConfigTemplateUpdaterOVPN(objKernel, this, objChooser, objMapper, null);
				File objFileNew = objUpdater.refreshFileUsed(fileConfigTemplateOvpnUsed);	
				if(objFileNew==null){
					this.logMessageString("Unable to create 'used file' file base on template template: '" + objaFileConfigTemplate[icount].getPath() + "'");					
				}else{
					
					boolean btemp = objUpdater.update(objFileNew, true); //Bei false werden also auch Zeilen automatisch hinzugefügt, die nicht im Template sind. Z.B. Proxy-Einstellungen.
					if(btemp==true){
						this.logMessageString( "'Used file' successfully created for template: '" + objaFileConfigTemplate[icount].getPath() + "'");
		
						//+++ Nun dieses used-file dem Array hinzufuegen, dass fuer den Start der OVPN-Verbindung verwendet wird.
						//listaFileConfig.add(objUpdater.getFileUsed());
						this.getConfigFileObjectsAll().add(objUpdater.getFileUsed());
					}else{
						this.logMessageString( "'Used file' not processed, based upon: '" + objaFileConfigTemplate[icount].getPath() + "'");					
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
				this.logMessageString("No configuration available which is configured in Kernel Ini File for Program 'ProgConfigHandler' and Property 'ConfigFile'.");
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
				this.logMessageString("Finally used configuration  file(s): " + stemp);
			}
			
			//#############
			//Auslesen der "erlaubten" Clients und Datei dafür in einem bestimmten Verzeichnis anlegen.			
			//Das wird alles in einem Handler gekapselt, den Namen (auch der Methode noch optimieren)
			ServerConfigOnServerAllowedClientFacadeOVPN objClientConfigHandler = new ServerConfigOnServerAllowedClientFacadeOVPN(this.getKernelObject(), this, null);
			boolean bSuccess = objClientConfigHandler.execute();
			
			
			
			//##########################################################################################
			//+++ Diese OVPN-Konfigurationsfiles zum Starten der VPN-Verbindung verwenden !!!
			Thread[] threadaOVPN = new Thread[listaFileConfigUsed.size()];
			ProcessWatchRunnerZZZ[] runneraOVPN = new ProcessWatchRunnerZZZ[listaFileConfigUsed.size()];	
			int iNumberOfProcessStarted = 0;
			for(int icount=0; icount <= listaFileConfigUsed.size()-1; icount++){
				File objFileConfigOvpn = (File) listaFileConfigUsed.get(icount);
				String sAlias = Integer.toString(icount);
				String[] saTemp = {"ByBatch"}; //Weil auf dem Server der endgültige auszuführende Befehl über eine Batch gegeben werden muss. Herausgefunden durch Try and Error.				
				File objFileTemplateBatch = objChooser.findFileConfigBatchTemplateFirst();				
				ServerConfigStarterOVPN objStarter = new ServerConfigStarterOVPN(objKernel, (IMainOVPN) this, objFileTemplateBatch, objFileConfigOvpn, sAlias, saTemp);
				this.logMessageString("Requesting start of process #"+ icount + " (File: " + objFileConfigOvpn.getName() + ")");				
				Process objProcessTemp = objStarter.requestStart();			
				if(objProcessTemp==null){
					//Hier nicht abbrechen, sondern die Verarbeitung bei der naechsten Datei fortfuehren
					this.logMessageString( "Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"'");
				}else{			
					this.addProcessStarter(objStarter);
					
					//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
					 runneraOVPN[icount] =new ProcessWatchRunnerZZZ(objKernel, objProcessTemp,icount, null);
					 threadaOVPN[icount] = new Thread(runneraOVPN[icount]);					
					 threadaOVPN[icount].start();
					 iNumberOfProcessStarted++;	
					//Das blaeht das Log unnoetig auf .... zum Test aber i.o.
					 this.logMessageString("Finished starting thread # " + icount + " for listening to connection.");
					 this.setFlag("WatchRunnerStarted", true);					
				}				
			}//END for
			 //this.setFlag("isstarted", true);
			
			//Merke: Wenn über das enum der setFlag gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.
			//this.setFlag(ServerTrayStatusZZZ.ServerTrayStatusTypeZZZ.STARTED, true);
			this.setFlag(IServerMainOVPN.STATUSLOCAL.ISSTARTED, true);
			 if(objServerTray!=null) objServerTray.switchStatus(ServerTrayStatusZZZ.ServerTrayStatusTypeZZZ.STARTED);
				
			
			//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.									
		   bReturn = true;
	}//END main		
		return bReturn;
	}//END start()
	
	public void run() {
		try {
			boolean bStarted = this.start();
			//this.setFlag("isstarted", bStarted);
		} catch (ExceptionZZZ ez) {
			try {
				this.setFlag("haserror", true);
				this.getKernelObject().getLogObject().WriteLineDate(ez.getDetailAllLast());
				System.out.println(ez.getDetailAllLast());
			} catch (ExceptionZZZ e) {				
				System.out.println(ez.getDetailAllLast());
				e.printStackTrace();
			}
			
		}
	}
	
	/** Adds a line to the status arraylist. This status is used to enable the frontend-client to show a log dialogbox.
	 * Remark: This method does not write anything to the kernel-log-file. 
	* @param sStatus 
	* 
	* lindhaueradmin; 13.07.2006 08:34:56
	 */
	public void addMessageString(String sMessage){
		if(sMessage!=null){
			this.sMessageCurrent = sMessage;
			this.listaMessage.add(sMessage);
		}
	}
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sMessage 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 * @throws ExceptionZZZ 
	 */
	public void logMessageString(String sMessage) throws ExceptionZZZ{
		if(sMessage!=null){
			this.addMessageString(sMessage);
			
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
				this.logMessageString(saMessage[icount]);
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
	
	/**This status is a type of "Log".
	 * This is the last entry.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public String getMessageStringCurrent(){
		return this.sMessageCurrent;
	}

	/**This status is a type of "Log".
	 * This are all entries.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public ArrayList getMessageStringAll(){
		return this.listaMessage;
	}
	
	 public  boolean addProcessStarter(ServerConfigStarterOVPN objStarter){
		 boolean bReturn = false;
		 main:{
			 check:{
				 if(objStarter==null) break main;
			 }//END check
		 
		 	this.listaConfigStarter.add(objStarter);
		 
		 }//END main
		 return bReturn;
	 }
	 
	 /**returns a ConfigStarterZZZ-object.
	 * @param iPosition, Remark: Starting at index postion 1
	 * @return boolean
	 *
	 * javadoc created by: 0823, 24.07.2006 - 09:32:09
	 */
	public ServerConfigStarterOVPN getProcessStarter(int iPosition){
		ServerConfigStarterOVPN objReturn=null;
		 main:{
			 check:{				
				 if(iPosition<= 0 || iPosition > listaConfigStarter.size()) break main;
			 }//END check
		 	objReturn = (ServerConfigStarterOVPN) listaConfigStarter.get(iPosition);
		 	
		 }//END main
		 return objReturn;
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
		
//	######### GetFlags - Handled ##############################################
	/** (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used:<CR>
	-  isConnected	
	- haserror
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
						
			//getting the flags of this object
			//sind nun stati
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("islaunched")){
//				bFunction = bFlagIsLaunched;
//				break main;
//			}else if(stemp.equals("isstarting")){
//				bFunction = bFlagIsStarting;
//				break main;
//			}else if(stemp.equals("isstarted")){
//				bFunction = bFlagIsStarted;
//				break main;
//			}else if(stemp.equals("islistening")){
//				bFunction = bFlagIsListening;
//				break main;
//			}else if(stemp.equals("watchrunnerstarted")){
//				bFunction = bFlagWatchRunnerStarted;
//				break main;
//			}else if(stemp.equals("haserror")){				
//				bFunction = bFlagHasError;
//				break main;
//			}
		}//end main:
		return bFunction;
	}
	
	


	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 * - isconnected
	 * - haserror
	 * @throws ExceptionZZZ 
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
	
		//setting the flags of this object
		//sind nun stati
//		String stemp = sFlagName.toLowerCase();
//		if(stemp.equals("islaunched")){
//			bFlagIsLaunched = bFlagValue;
//			bFunction = true;
//			break main;	
//		}else if(stemp.equals("isstarting")){
//			bFlagIsStarting = bFlagValue;
//			bFunction = true;
//			break main;	
//		}else if(stemp.equals("isstarted")){
//			bFlagIsStarted = bFlagValue;
//			bFunction = true;
//			break main;	
//		}else if(stemp.equals("islistening")) {
//			bFlagIsListening = bFlagValue;
//			bFunction = true;
//			break main;	
//		}else if(stemp.equals("watchrunnerstarted")){
//				bFlagWatchRunnerStarted = bFlagValue;
//				bFunction = true;
//				break main;				
//		}else if(stemp.equals("haserror")){
//			bFlagHasError = bFlagValue;
//			bFunction = true;
//			break main;	
//		}
		}//end main:
		return bFunction;
	}

	//##### GETTER / SETTER
//	public ConfigChooserOVPN getConfigChooserObject() {
//		return this.objConfigChooser;
//	}
//	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
//		this.objConfigChooser = objConfigChooser;
//	}
//	
//	public ServerConfigMapperOVPN getConfigMapperObject() {
//		return this.objConfigMapper;
//	}
//	public void setConfigMapperObject(ServerConfigMapperOVPN objConfigMapper) {
//		this.objConfigMapper = objConfigMapper;
//	}
//	
//	public ServerApplicationOVPN getApplicationObject() {
//		return this.objApplication;
//	}
//	public void setApplicationObject(ServerApplicationOVPN objApplication) {
//		this.objApplication = objApplication;
//	}
	
	
	public ArrayList<File> getConfigFileObjectsAll() {
		if(this.listaConfigFile==null) {
			this.listaConfigFile = new ArrayList<File>();
		}
		return this.listaConfigFile;
	}
	public void setConfigFileObjectsAll(ArrayList<File> listaConfigFile) {
		this.listaConfigFile = listaConfigFile;
	}
	
	//### IStatusLocalUserZZZ
	/** DIESE METHODEN MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHREN STATUS SETZEN WOLLEN
	 * Weitere Voraussetzungen:
	 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
	 * - Innere Klassen müssen auch public deklariert werden.
	 * @param objClassParent
	 * @param sFlagName
	 * @param bFlagValue
	 * @return
	 * lindhaueradmin, 23.07.2013
	 * @throws ExceptionZZZ 
	 */
	@Override
	public boolean getStatusLocal(Enum enumStatus) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatus==null) {
				break main;
			}
			
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
	public boolean setStatusLocal(Enum enumStatus, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatus==null) {
				break main;
			}
		//return this.getStatusLocal(objEnumStatus.name());
		//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum
		
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
				IEventObjectStatusLocalSetZZZ event = new EventObjectStatusLocalSetZZZ(this,1,enumStatus, bStatusValue);
				this.objEventStatusLocalBroker.fireEvent(event);
			}			
			bFunction = true;								
		}										
	}	// end main:
	return bFunction;
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
		return this.proofFlagExists(objEnumStatus.name());
	}
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++++++++++++++++++++++++
	/* @see basic.zBasic.IFlagZZZ#getFlagZ(java.lang.String)
	 * 	 Weteire Voraussetzungen:
	 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
	 * - Innere Klassen m�ssen auch public deklariert werden.(non-Javadoc)
	 */
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
	public boolean setStatusLocal(String sStatusName, boolean bFlagValue) throws ExceptionZZZ {
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
				hmStatus.put(sStatusName.toUpperCase(), bFlagValue);
				
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.
				if(this.objEventStatusLocalBroker!=null) {
					IEventObjectStatusLocalSetZZZ event = new EventObjectStatusLocalSetZZZ(this,1,sStatusName.toUpperCase(), bFlagValue);
					this.objEventStatusLocalBroker.fireEvent(event);
				}
				
				bFunction = true;								
			}										
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
		
	@Override
	public HashMap<String, Boolean>getHashMapStatusLocal(){
		return this.hmStatusLocal;
	}
	
	@Override
	public void setHashMapStatusLocal(HashMap<String, Boolean> hmStatusLocal) {
		this.hmStatusLocal = hmStatusLocal;
	}
	
	/**Gibt alle möglichen FlagZ Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	public String[] getStatusLocal() throws ExceptionZZZ{
		String[] saReturn = null;
		main:{	
			saReturn = StatusLocalHelperZZZ.getStatusLocalDirectAvailable(this.getClass());				
		}//end main:
		return saReturn;
	}
	
	/**Gibt alle "true" gesetzten FlagZ - Werte als Array zurück. 
	 * @return
	 * @throws ExceptionZZZ 
	 */
	public String[] getStatusLocal(boolean bValueToSearchFor) throws ExceptionZZZ{
		return this.getStatusLocal_(bValueToSearchFor, false);
	}
	
	public String[] getStatusLocal(boolean bValueToSearchFor, boolean bLookupExplizitInHashMap) throws ExceptionZZZ{
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
			bReturn = StatusLocalHelperZZZ.proofStatusLocalDirectExists(this.getClass(), sStatusName);				
		}//end main:
		return bReturn;
	}

	//### aus ISenderObjectStatusLocalSetZZZ
	@Override
	public void fireEvent(IEventObjectStatusLocalSetZZZ event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetZZZ objEventListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetZZZ objEventListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<IListenerObjectStatusLocalSetZZZ> getListenerRegisteredAll() {
		// TODO Auto-generated method stub
		return null;
	}

	//### aus IEventBrokerStatusLocalSetUserZZZ
	@Override
	public ISenderObjectStatusLocalSetZZZ getSenderStatusLocalUsed() throws ExceptionZZZ {		
		if(this.objEventStatusLocalBroker==null) {
			//++++++++++++++++++++++++++++++
			//Nun geht es darum den Sender fuer Aenderungen an den Flags zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
			ISenderObjectStatusLocalSetZZZ objSenderStatusLocal = new KernelSenderObjectStatusLocalSetZZZ();
			this.objEventStatusLocalBroker = objSenderStatusLocal;
		}
		return this.objEventStatusLocalBroker;
		
	}

	@Override
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetZZZ objEventStatusLocalBroker) {
		this.objEventStatusLocalBroker = objEventStatusLocalBroker;
	}

	@Override
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetZZZ objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetZZZ objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);
	}
		
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++++++++++++++++++++++++
	
	
}//END class
