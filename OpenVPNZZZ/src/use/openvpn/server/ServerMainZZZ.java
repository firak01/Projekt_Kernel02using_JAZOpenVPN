package use.openvpn.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import use.openvpn.ConfigStarterZZZ;
import use.openvpn.ConfigChooserZZZ;
import use.openvpn.ConfigFileZZZ;
import use.openvpn.ProcessWatchRunnerZZZ;
import basic.zKernel.KernelZZZ;
import custom.zUtil.io.FileZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;

public class ServerMainZZZ extends KernelUseObjectZZZ implements Runnable{
	private String sStatusCurrent = null; //Hier�ber kann das Frontend abfragen, was gerade in der Methode "start()" so passiert.
	private ArrayList listaStatus = new ArrayList(); //Hier�ber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
    																	//Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen k�nnen.
	private ArrayList listaConfigStarter = new ArrayList(); //Hier�ber werden alle Procese, die mit einem bestimmten Konfigurations-File gestartet wurden festgehalten.
	private boolean bFlagIsStarted=false;
	private boolean bFlagWatchRunnerStarted=false;
	private boolean bFlagHasError= false;
	                                                                      //Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen k�nnen.

	public ServerMainZZZ(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel); 
		ServerMainNew_(saFlagControl);
	}
	
	private void ServerMainNew_(String[] saFlagControl) throws ExceptionZZZ{
		main:{
			
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
										
			}//End check
	
		}//END mai
	}

	
	/** Adds a line to the status arraylist. This status is used to enable the frontend-client to show a log dialogbox.
	 * Remark: This method does not write anything to the kernel-log-file. 
	* @param sStatus 
	* 
	* lindhaueradmin; 13.07.2006 08:34:56
	 */
	public void addStatusString(String sStatus){
		if(sStatus!=null){
			this.sStatusCurrent = sStatus;
			this.listaStatus.add(sStatus);
		}
	}
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sStatus 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 */
	public void logStatusString(String sStatus){
		if(sStatus!=null){
			this.addStatusString(sStatus);
			
			IKernelZZZ objKernel = this.getKernelObject();
			if(objKernel!= null){
				objKernel.getLogObject().WriteLineDate(sStatus);
			}
		}
	}
	
	/**This status is a type of "Log".
	 * This is the last entry.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public String getStatusStringCurrent(){
		return this.sStatusCurrent;
	}

	/**This status is a type of "Log".
	 * This are all entries.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public ArrayList getStatusStringAll(){
		return this.listaStatus;
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
			//TODO Dies in eine Methode "find fileConfigAvailableAndConfigured" 
			//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 			
			this.logStatusString("Searching for configuration template files '*.ovpn'"); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			IKernelZZZ objKernel = this.getKernelObject();			
			ConfigChooserZZZ objChooser = new ConfigChooserZZZ(objKernel);
			
			//TEST
			//System.out.println(objChooser.toString());
			
			//Die Konfigurations Dateien finden		
			File[] objaFileConfig = objChooser.findFileConfigUsed(null);
			if(objaFileConfig==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(objaFileConfig.length == 0){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				this.logStatusString(objaFileConfig.length + " configuration file(s) were found in the directory: '" + objChooser.readDirectoryConfigPath() + "'");  //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
			//ABER: Es werden nur die Konfigurationsdateien verwendet, die auch konfiguriert sind. Falls nix konfiguriert ist, werden alle gefundenen verwendet.
			String sFileConfigConfigured = objKernel.getParameterByProgramAlias("OVPN", "ProgConfigHandler", "ConfigFile").getValue();
			ArrayList listaFileConfigUsed  = new ArrayList();
			
			if(StringZZZ.isEmpty(sFileConfigConfigured)){
				//Die Arraylist besteht nun aus allen konfigurierten Dateien
				for(int icount=0; icount < objaFileConfig.length; icount++){
					listaFileConfigUsed.add(objaFileConfig[icount]);
				}
			}else{
				//Aus der Liste aller zur Verf�gung stehenden Dateien nur diejenigen raussortieren, die konfiguriert sind.
				StringTokenizer objToken = new StringTokenizer(sFileConfigConfigured, File.separator);
				while(objToken.hasMoreTokens()){
					String stemp = objToken.nextToken();
					for(int icount=0; icount < objaFileConfig.length; icount++){
						File objFileTemp = objaFileConfig[icount];
						if(objFileTemp.getName().equalsIgnoreCase(stemp)){
							listaFileConfigUsed.add(objFileTemp);
							break;
						}
					}//END for
				}//END while
			}//END if
			if(listaFileConfigUsed.size()==0){
				this.logStatusString("No configuration available which is configured.");
				bReturn = false;
				break main;
			}else{
				String stemp = "";
				for(int icount=0; icount < listaFileConfigUsed.size(); icount++){
					File objFileTemp = (File) listaFileConfigUsed.get(icount);
					if(stemp.equals("")){
						stemp = objFileTemp.getName();
					}else{
						stemp = stemp + "; " + objFileTemp.getName();	
					}					 
				}//END for
				this.logStatusString("Finally used configuration  file(s): " + stemp);
			}
			
			
			//######################################################
			//2. Diverse Dinge mit WMI testen.
			KernelWMIZZZ objWMI = new KernelWMIZZZ(objKernel, null);
			
			//++++++++++++++++++++++++++++++
			//Starterlaubnis: L�uft schon ovpn ???
		   //TODO KernelWMIZZZ eines win32 - packages.
			File objFileExe = ServerConfigFileZZZ.findFileExe();   //Anders als im Client muss nix weiter gemacht werden
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
			this.logStatusString("Open VPN not yet running. Continue starting process.");
			
            //+++++++++++++++++++++++++++++++
			//Starterlaubnis: L�uft der domino Server schon ???
			//Falls nicht: Warte eine konfigurierte Zeit. (Merke: Nicht abbrechen, weil ja ggf. Probleme existieren oder der Server gar nicht starten soll).			
			String sDominoCaption = objKernel.getParameterByProgramAlias("OVPN","ProgProcessCheck","Process2Check").getValue();
			if(StringZZZ.isEmpty(sDominoCaption)==false){
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
							Thread.sleep(1000); //DIESEN THREAD f�r 1 Sekunde anhalten
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
				this.logStatusString("Depending process '" + sDominoCaption + "' running. Continue stating process.");
			}//END if sDominoCaption isempty
			
			
			//##########################################################################################
			//+++ Diese OVPN-Konfigurationsfiles zum Starten der VPN-Verbindung verwenden !!!
			Thread[] threadaOVPN = new Thread[listaFileConfigUsed.size()];
			ProcessWatchRunnerZZZ[] runneraOVPN = new ProcessWatchRunnerZZZ[listaFileConfigUsed.size()];	
			int iNumberOfProcessStarted = 0;
			for(int icount=0; icount < listaFileConfigUsed.size(); icount++){
				File objFile = (File) listaFileConfigUsed.get(icount);
				String sAlias = Integer.toString(icount);
				String[] saTemp = {"ByBatch"}; //Weil auf dem Server der endg�ltige auszuf�hrende Befehl �ber eine Batch gegeben werden muss. Herausgefunden durch Try and Error.
				ConfigStarterZZZ objStarter = new ConfigStarterZZZ(objKernel, objFile, sAlias, saTemp);
				this.logStatusString("Requesting start of process #"+ icount + " (File: " + objFile.getName() + ")");				
				Process objProcessTemp = objStarter.requestStart();			
				if(objProcessTemp==null){
					//Hier nicht abbrechen, sondern die Verarbeitung bei der n�chsten Datei fortf�hren
					this.logStatusString( "Unable to create process, using file: '"+ objStarter.getFileConfig().getPath()+"'");
				}else{			
					this.addProcessStarter(objStarter);
					
					//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
					 runneraOVPN[icount] =new ProcessWatchRunnerZZZ(objKernel, objProcessTemp,icount, null);
					 threadaOVPN[icount] = new Thread(runneraOVPN[icount]);					
					 threadaOVPN[icount].start();
					 iNumberOfProcessStarted++;	
					//Das bl�ht das Log unn�tig auf .... zum Test aber i.o.
					 this.logStatusString("Finished starting thread # " + icount + " for listening to connection.");
					 this.setFlag("WatchRunnerStarted", true);
				}				
			}//END for
			
			//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.			
			
			
		   bReturn = true;
	}//END main
		this.setFlag("isstarted", bReturn);
		return bReturn;
	}//END start()
	

	public void run() {
		try {
			this.start();
		} catch (ExceptionZZZ ez) {
			this.setFlag("haserror", true);
			this.getKernelObject().getLogObject().WriteLineDate(ez.getDetailAllLast());
		}
	}
	
	
	
	 public  boolean addProcessStarter(ConfigStarterZZZ objStarter){
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
	public ConfigStarterZZZ getProcessStarter(int iPosition){
		 ConfigStarterZZZ objReturn=null;
		 main:{
			 check:{				
				 if(iPosition<= 0 || iPosition > listaConfigStarter.size()) break main;
			 }//END check
		 	objReturn = (ConfigStarterZZZ) listaConfigStarter.get(iPosition);
		 	
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
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("isstarted")){
				bFunction = bFlagIsStarted;
				break main;
			}else if(stemp.equals("watchrunnerstarted")){
				bFunction = bFlagWatchRunnerStarted;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}
		}//end main:
		return bFunction;
	}
	
	


	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 * - isconnected
	 * - haserror
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
	
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("isstarted")){
			bFlagIsStarted = bFlagValue;
			bFunction = true;
			break main;	
		}else if(stemp.equals("watchrunnerstarted")){
				bFlagWatchRunnerStarted = bFlagValue;
				bFunction = true;
				break main;				
		}else if(stemp.equals("haserror")){
			bFlagHasError = bFlagValue;
			bFunction = true;
			break main;	
		}
		}//end main:
		return bFunction;
	}

}//END class
