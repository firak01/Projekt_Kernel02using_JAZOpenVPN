package use.openvpn.server.process;

import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.moduleExternal.IWatchListenerZZZ;
import basic.zBasic.util.moduleExternal.log.watch.ILogFileWatchRunnerZZZ;
import basic.zBasic.util.moduleExternal.monitor.AbstractProcessWatchMonitorZZZ;
import basic.zBasic.util.moduleExternal.process.watch.IProcessWatchRunnerZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusOnStatusListeningZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ;
import basic.zKernel.flag.event.EventObjectFlagZsetZZZ;
import basic.zKernel.flag.event.IEventObjectFlagZsetZZZ;
import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalZZZ;
import basic.zKernel.status.ISenderObjectStatusBasicZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.server.status.EventObject4ProcessMonitorStatusLocalOVPN;
import use.openvpn.server.status.IEventObjectStatusLocalOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.process.IProcessWatchRunnerOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL;
import use.openvpn.server.IServerMainOVPN;
import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.status.IEventBrokerStatusLocalUserOVPN;
import use.openvpn.server.status.IEventObject4ProcessWatchMonitorStatusLocalOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalOVPN;
import use.openvpn.server.status.SenderObjectStatusLocalOVPN;

/**This class watches the ServerMainZZZ-class and the ServerConnectionListenerRuner-objects.
 * This class runs in a seperate thread, so the TrayIcon stays "clickable", that means that clicking on the icon will be processed.
 * 
 * @author 0823
 *
 */
//NEIN: public class ServerThreadProcessWatchMonitorOVPN extends AbstractKernelUseObjectWithStatusOnStatusListeningZZZ implements IServerThreadProcessWatchMonitorOVPN, Runnable, IListenerObjectStatusLocalOVPN, IEventBrokerStatusLocalSetUserOVPN{
public class ServerThreadProcessWatchMonitorOVPN extends AbstractProcessWatchMonitorZZZ implements IServerThreadProcessWatchMonitorOVPN, Runnable, IListenerObjectStatusLocalOVPN {//Das wird nun über die Abstrakte Klasse gemacht., IEventBrokerStatusLocalSetUserOVPN{
	private IKernelZZZ objKernel = null;
	private IServerMainOVPN objServerMain = null;
	private ISenderObjectStatusLocalOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	public ServerThreadProcessWatchMonitorOVPN(IKernelZZZ objKernel, IServerMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
		super();
		ServerMonitorRunnerNew_(objKernel, objConfig, saFlagControl);
	}

private void ServerMonitorRunnerNew_(IKernelZZZ objKernel, IServerMainOVPN objServerMain, String[] saFlagControl) throws ExceptionZZZ{
	main:{	
		if(saFlagControl != null){
			String stemp; boolean btemp;
			for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
				stemp = saFlagControl[iCount];
				btemp = setFlag(stemp, true);
				if(btemp==false){ 								   
					   ExceptionZZZ ez = new ExceptionZZZ( stemp, IFlagZEnabledZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 						
					   throw ez;		 
				}
			}
			if(this.getFlag("init")) break main;
		}
		this.objKernel = objKernel;
		this.objServerMain = objServerMain;
	}//END main
}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public void run() {		
		main:{
		try {
			if(this.getMainObject()==null) break main;
				
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Trying to establish a new connection with every OVPN-configuration-file. Starting threads.";
			System.out.println(sLog);
			this.getMainObject().logProtocol(sLog);
		
			//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.								
			//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
			//besser ueber eine geworfenen Event... und nicht direkt: this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
			//this.setStatusLocal(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTNO, false);
			//boolean bStartNewGoon = this.setStatusLocal(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, true);
			boolean bStatusLocalSet = this.switchStatusLocalForGroupTo(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, true); //Damit der ISSTOPPED Wert auf jeden Fall auch beseitigt wird
			if(!bStatusLocalSet) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
				System.out.println(sLog);
				this.getMainObject().logProtocol(sLog);
				break main;
			}			
			Thread.sleep(5000);
					
			
			//Erst mal sehn, ob ueberhaupt was da ist.			
			ArrayList<ServerConfigStarterOVPN> listaProcessStarter = objServerMain.getServerConfigStarterList();
			
			//Vorbereitend: Process bereitstellen, der die Ausgabe des gestarteten Processes ueberwacht.
			//              Das funktioniert beim Client per direktem Standard.out.
			//              Beim Server aber nur per Beobachten des Log Files
			ProcessWatchRunnerOVPN[] runneraProcessOVPN = null;
			boolean bUseLogFileWatch = this.getFlag(IServerThreadProcessWatchMonitorOVPN.FLAGZ.USE_LOGFILE_WATCHRUNNER); 
			if(bUseLogFileWatch) {
				//TODOGOON20240127;
			}else {
				runneraProcessOVPN = new ProcessWatchRunnerOVPN[listaProcessStarter.size()];
			}
			//Nun fuer alle in ServerMain bereitgestellten Konfigurationen einen OpenVPN.exe - Process bereitstellen.
			//Den passenden WatchRunner starten den Monitor Prozess daran registrieren.				
			Thread[] threadaOVPN = new Thread[listaProcessStarter.size()];			
			int iNumberOfProcessStarted = 0;
			for(int icount=0; icount < listaProcessStarter.size(); icount++){
				iNumberOfProcessStarted++;
				ServerConfigStarterOVPN objStarter = (ServerConfigStarterOVPN) listaProcessStarter.get(icount);				
				if(objStarter==null){
					//Hier nicht abbrechen, sondern die Verarbeitung bei der naechsten Datei fortfuehren
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size();
					System.out.println(sLog);
					this.getMainObject().logProtocol(sLog);
				}else {
					Process objProcess = objStarter.requestStart();	
					if(objProcess==null){
 						//Hier nicht abbrechen, sondern die Verarbeitung bei der naechsten Datei fortfuehren
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size();
 						System.out.println(sLog);
 						this.getMainObject().logProtocol(sLog); 						
 					}else{	
						//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Successfull process created, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size() +". Starting Thread as Monitor for this process.";
						System.out.println(sLog);
						this.getMainObject().logProtocol(sLog);
							
						//TEST, Flagübergabe: Ohne, z.B. die Pruefung auf vorherige Werte wird immer ein Event geworfen fuer "HASOUTPUT"
						//runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name());
						//runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted);
						//String[]saFlagControl = {IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
						//String[]saFlagControl = {IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
						
						if(bUseLogFileWatch) {
							//TODOGOON20240127;//Projekt einbinden
							//String[]saFlagControl = {ILogFileWatchRunnerZZZ.FLAGZ.END_ON_FILTERFOUND.name()};
							//String[]saFlagControl = {IProcessWatchRunnerOVPN.FLAGZ.END_ON_FILTER_FOUND.name()};
							String[]saFlagControl = {IWatchListenerZZZ.FLAGZ.END_ON_FILTER_FOUND.name()};
							runneraProcessOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, saFlagControl);
							
							runneraProcessOVPN[icount].setServerBackendObject(this.getMainObject());
							runneraProcessOVPN[icount].setServerConfigStarterObject(objStarter);
							
							//Wichtig, den ProcessWatchMonitorOVPN an den ProcessWatchRunnerOVPN Listener registrieren.
							//Dafuer gibt es dann auch ein Mapping, in dem steht wie mit den empfangenen Events umgegangen wird, bzw. welche eigenen Events geworfen werden sollen.
							runneraProcessOVPN[icount].registerForStatusLocalEvent((IListenerObjectStatusLocalZZZ)this);//Registriere den Monitor nun am ProcessWatchRunner
							 						
							threadaOVPN[icount] = new Thread(runneraProcessOVPN[icount]);//Starte den ProcessWatchRunner					
							threadaOVPN[icount].start();	 	
							sLog = ReflectCodeZZZ.getPositionCurrent()+": ProcessWatchRunner started for thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size() + ".";
							this.getMainObject().logProtocol(sLog);
						}else {
							String[]saFlagControl = {IProcessWatchRunnerOVPN.FLAGZ.END_ON_CONNECTION.name()};
							runneraProcessOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, saFlagControl);
							
							runneraProcessOVPN[icount].setServerBackendObject(this.getMainObject());
							runneraProcessOVPN[icount].setServerConfigStarterObject(objStarter);
							
							//Wichtig, den ProcessWatchMonitorOVPN an den ProcessWatchRunnerOVPN Listener registrieren.
							//Dafuer gibt es dann auch ein Mapping, in dem steht wie mit den empfangenen Events umgegangen wird, bzw. welche eigenen Events geworfen werden sollen.
							runneraProcessOVPN[icount].registerForStatusLocalEvent((IListenerObjectStatusLocalZZZ)this);//Registriere den Monitor nun am ProcessWatchRunner
							 						
							threadaOVPN[icount] = new Thread(runneraProcessOVPN[icount]);//Starte den ProcessWatchRunner					
							threadaOVPN[icount].start();	 	
							sLog = ReflectCodeZZZ.getPositionCurrent()+": ProcessWatchRunner started for thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size() + ".";
							this.getMainObject().logProtocol(sLog);
						}
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Finished starting thread #" + iNumberOfProcessStarted + " von " + listaProcessStarter.size() + " for watching connection.";
						System.out.println(sLog);
						this.getMainObject().logProtocol(sLog);
 					}
				}
			}//END for
			if(iNumberOfProcessStarted==0) {
				//Hier nicht abbrechen, sondern den Status wieder zurücksetzen.
				sLog = ReflectCodeZZZ.getPositionCurrent()+": No process started.";
				System.out.println(sLog);									
				this.getMainObject().logProtocol(sLog);
				
				bStatusLocalSet = this.switchStatusLocalForGroupTo(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTNO, true); //Damit der ISSTOPPED Wert auf jeden Fall auch beseitigt wird
				if(!bStatusLocalSet) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocol(sLog);
					break main;
				}			
			}else if(iNumberOfProcessStarted>=1) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": " + iNumberOfProcessStarted + " process started.";
				System.out.println(sLog);
				this.getMainObject().logProtocol(sLog);
				
				bStatusLocalSet = this.switchStatusLocalForGroupTo(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTED, true); //Damit der ISSTOPPED Wert auf jeden Fall auch beseitigt wird
				if(!bStatusLocalSet) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocol(sLog);
					break main;
				}	
			}
		} catch (ExceptionZZZ ez) {
			System.out.println(ez.getDetailAllLast());
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
	}//END main:
	
	}//END run
	
	@Override
	public void setMainObject(IServerMainOVPN objClientBackend){
		this.objServerMain = (ServerMainOVPN) objClientBackend;
	}
	
	@Override
	public IServerMainOVPN getMainObject(){
		return this.objServerMain;
	}
	
	//### Getter / Setter
		
	/**
	 * @param sStatusString
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.10.2023, 11:48:47
	 */
	public boolean isStatusChanged(String sStatusString) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(sStatusString == null) {
				bReturn = this.getStatusLocalAbbreviation()==null;
				break main;
			}
			
			if(!sStatusString.equals(this.getStatusLocalAbbreviation())) {
				bReturn = true;
			}
		}//end main:
		if(bReturn) {
			String sLog = ReflectCodeZZZ.getPositionCurrent()+ ": Status changed to '"+sStatusString+"'";
			System.out.println(sLog);
		    this.getMainObject().logProtocol(sLog);			
		}
		return bReturn;
	}
	
	
	
	

	//###### FLAGS
	/* @see basic.zBasic.IFlagZZZ#getFlagZ(java.lang.String)
	 * 	 Weitere Voraussetzungen:
	 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
	 * - Innere Klassen muessen auch public deklariert werden.(non-Javadoc)
	 */
	public boolean getFlag(String sFlagName) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
										
			HashMap<String, Boolean> hmFlag = this.getHashMapFlag();
			Boolean objBoolean = hmFlag.get(sFlagName.toUpperCase());
			if(objBoolean==null){
				bFunction = false;
			}else{
				bFunction = objBoolean.booleanValue();
			}
							
		}	// end main:
		
		return bFunction;	
	}
	
	//ALTE VERSION
	/* (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- connectionrunnerstarted	 */
//	public boolean getFlag(String sFlagName){
//		boolean bFunction = false;
//		main:{
//			if(StringZZZ.isEmpty(sFlagName)) break main;
//			bFunction = super.getFlag(sFlagName);
//			if(bFunction==true) break main;
//		
			//getting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("connectionrunnerstarted")){
//				bFunction = bFlagConnectionRunnerStarted;
//				break main;
//			}		
//		}//end main:
//		return bFunction;
//	}

/** DIESE METHODE MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHRE FLAGS SETZEN WOLLEN
 * Weitere Voraussetzungen:
 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
 * - Innere Klassen müssen auch public deklariert werden.
 * @param objClassParent
 * @param sFlagName
 * @param bFlagValue
 * @return
 * lindhaueradmin, 23.07.2013
 */
@Override
public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ {
	boolean bFunction = false;
	main:{
		if(StringZZZ.isEmpty(sFlagName)) {
			bFunction = true;
			break main;
		}
					
		bFunction = this.proofFlagExists(sFlagName);															
		if(bFunction == true){
			
			//Setze das Flag nun in die HashMap
			HashMap<String, Boolean> hmFlag = this.getHashMapFlag();
			hmFlag.put(sFlagName.toUpperCase(), bFlagValue);								
			
			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.
			if(this.objEventFlagZBroker!=null) {
				IEventObjectFlagZsetZZZ event = new EventObjectFlagZsetZZZ(this,1,sFlagName.toUpperCase(), bFlagValue);
				this.objEventFlagZBroker.fireEvent(event);
			}
			
			bFunction = true;								
		}										
	}	// end main:
	
	return bFunction;	
}

//ALTE VERSION
	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 	- ConnectionRunnerStarted.
	 * @throws ExceptionZZZ 
	 */
//	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
//		boolean bFunction = false;
//		main:{			
//			if(StringZZZ.isEmpty(sFlagName)) break main;
//			bFunction = super.setFlag(sFlagName, bFlagValue);
//			if(bFunction==true) break main;
//			
			//setting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("connectionrunnerstarted")){
//				bFlagConnectionRunnerStarted = bFlagValue;
//				bFunction = true;
//				break main;
//	
//			}
//		}//end main:
//		return bFunction;
//	}
//	
	
		@Override
		public boolean getFlag(IServerThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.getFlag(objEnumFlag.name());
		}
		@Override
		public boolean setFlag(IServerThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			return this.setFlag(objEnumFlag.name(), bFlagValue);
		}
		
		@Override
		public boolean[] setFlag(IServerThreadProcessWatchMonitorOVPN.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			boolean[] baReturn=null;
			main:{
				if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
					baReturn = new boolean[objaEnumFlag.length];
					int iCounter=-1;
					for(IServerThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
						iCounter++;
						boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
						baReturn[iCounter]=bReturn;
					}
					
					//!!! Ein mögliches init-Flag ist beim direkten setzen der Flags unlogisch.
					//    Es wird entfernt.
					this.setFlag(IFlagZEnabledZZZ.FLAGZ.INIT, false);
				}
			}//end main:
			return baReturn;
		}
		
		@Override
		public boolean proofFlagExists(IServerThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagExists(objEnumFlag.name());
		}	
		
		@Override
		public boolean proofFlagSetBefore(IServerThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagSetBefore(objEnumFlag.name());
		}

		
		

		//### aus IEventBrokerStatusLocalSetUserOVPN
		//FGL20251022: Versuche das über die Abstrakte Klasse zu erledigen
//		@Override
//		public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener)throws ExceptionZZZ {
//			this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);		
//		}
//
//		@Override
//		public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
//			this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);;
//		}
	
		//FGL20251022: Versuche das über die Abstrakte Klasse zu erledigen
//	@Override
//	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ {
//		boolean bReturn = false;
//		main:{
//			if(objEnumStatusIn==null) break main;
//			
//			
//			//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
////			if(!(objEnum instanceof IServerMainOVPN.STATUSLOCAL) ){
////				String sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus wird wg. unpassender Klasse ignoriert.";
////				System.out.println(sLog);
////				//this.objMain.logMessageString(sLog);
////				break main;
////		}	
//			
//			//Fuer das Main-Objekt ist erst einmal jeder Status relevant
//			bReturn = true;
//		}//end main:
//		return bReturn;
//	}
	

	//### aus IStatusLocalUserZZZ
	@Override
	public boolean getStatusLocal(Enum objEnumStatusIn) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(objEnumStatusIn==null) {
				break main;
			}
			
			//Merke: Bei einer anderen Klasse, die dieses DesingPattern nutzt, befindet sich der STATUSLOCAL in einer anderen Klasse
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) objEnumStatusIn;
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
	
//	//### aus ISenderObjectStatusLocalSetUserOVPN
//	@Override
//	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
//		return this.objServerMain.getSenderStatusLocalUsed();
//	}
//
//	@Override
//	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
//		this.objServerMain.setSenderStatusLocalUsed(objEventSender);
//	}
	
	
	//####### aus ISenderObjectStatusLocalSetUserOVPN
	//FGL20251022: Versuche das über die Abstrakte Klasse zu erledigen
//	@Override
//	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
//		if(this.objEventStatusLocalBroker==null) {
//			//++++++++++++++++++++++++++++++
//			//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
//			ISenderObjectStatusLocalSetOVPN objSenderStatusLocal = new SenderObjectStatusLocalSetOVPN();
//			this.objEventStatusLocalBroker = objSenderStatusLocal;
//		}
//		return this.objEventStatusLocalBroker;
//	}
//
//	@Override
//	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
//		this.objEventStatusLocalBroker = objEventSender;
//	}
	

	//FGL20251022: Versuche das über die Abstrakte Klasse zu erledigen: AbstractObjectWithStatusZZZ
//	//#########################################################
//		//### aus ISenderObjectStatusLocalUserZZZ
//	@Override
//	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
//		if(this.objEventStatusLocalBroker==null) {
//			//++++++++++++++++++++++++++++++
//			//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
//			ISenderObjectStatusLocalSetOVPN objSenderStatusLocal = new SenderObjectStatusLocalSetOVPN();
//			this.objEventStatusLocalBroker = objSenderStatusLocal;
//		}
//		return this.objEventStatusLocalBroker;
//	}
//
//	@Override
//	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
//		this.objEventStatusLocalBroker = objEventSender;
//	}
	


	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#offerStatusLocal(java.lang.Enum, boolean, java.lang.String)
	 */
	@Override
	public boolean offerStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);				
		}//end main;
		return bFunction;
	}
	
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);				
		}//end main;
		return bFunction;
	}
	
	private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) break main;
			
		
	    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
	    IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
		String sStatusName = enumStatus.name();
		bFunction = this.proofStatusLocalExists(sStatusName);															
		if(!bFunction) {
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerThreadProcessWatchMonitor for Process would like to fire event, but this status is not available: '" + sStatusName + "'";
			this.getMainObject().logProtocol(sLog);			
			break main;
		}
			
		bFunction = this.proofStatusLocalValueChanged(sStatusName, bStatusValue);
		if(!bFunction) {
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerThreadProcessWatchMonitor would like to fire event, but this status has not changed: '" + sStatusName + "'";
			this.getMainObject().logProtocol(sLog);
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
		
		String sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
		this.getMainObject().logProtocol(sLog);

		//Falls eine Message extra uebergeben worden ist, ueberschreibe...
		if(sStatusMessageToSet!=null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerMain setze sStatusMessageToSet='" + sStatusMessageToSet + "'";
			this.getMainObject().logProtocol(sLog);
		}
		//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
		this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);
		
		
		
		//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
		//Dann erzeuge den Event und feuer ihn ab.	
		if(this.getSenderStatusLocalUsed()==null) {
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerThreadProcessWatchMonitor for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
			this.getMainObject().logProtocol(sLog);		
			break main;
		}
		
		//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
		sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";
		this.getMainObject().logProtocol(sLog);
		IEventObject4ProcessWatchMonitorStatusLocalOVPN event = (IEventObject4ProcessWatchMonitorStatusLocalOVPN) new EventObject4ProcessMonitorStatusLocalOVPN(this,1,enumStatus, bStatusValue);			
		event.setApplicationObjectUsed(this.getMainObject().getApplicationObject());
					
		//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
		sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerThreadProcessWatchMonitor for Process iIndex= '" + iIndexOfProcess + "'";		
		this.getMainObject().logProtocol(sLog);
		if(iIndexOfProcess>=0) {
			event.setServerConfigStarterObjectUsed(this.getMainObject().getServerConfigStarterList().get(iIndexOfProcess));
		}		
		
		sLog = ReflectCodeZZZ.getPositionCurrent() + " ServerThreadProcessWatchMonitor for Process fires event '" + enumStatus.getAbbreviation() + "'";
		this.getMainObject().logProtocol(sLog);
		this.getSenderStatusLocalUsed().fireEvent(event);
				
		bFunction = true;				
	}	// end main:
	return bFunction;
	}
	
//	//FGL20251022:Erste einmal raus und sich auf die abstrakte Klasse AbstractObjectWithStatusOnStatusListeningZZZ verlassen.
//	//            und deren Methode public boolean reactOnStatusLocalEvent4Action(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ{
//	/* (non-Javadoc)
//	 * @see use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN#statusLocalChanged(use.openvpn.server.status.IEventObjectStatusLocalSetOVPN)
//	 */
//	@Override
//	public boolean changeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		//Der Monitor ist am ProcessWatchRunner registriert.
//		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
//		boolean bReturn = false;
//		main:{
//			if(eventStatusLocalSet==null)break main;
//			
//			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
//			System.out.println(sLog);
//			this.getMainObject().logProtocolString(sLog);
//			
//			boolean bRelevant = this.isEventRelevant(eventStatusLocalSet); 
//			if(!bRelevant) {
//				sLog = 	ReflectCodeZZZ.getPositionCurrent() + ": Event / Status nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//			
//			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
//			IEnumSetMappedStatusZZZ enumStatus = eventStatusLocalSet.getStatusEnum();
//			if(enumStatus==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen Status aus dem Event-Objekt erhalten. Breche ab";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//			
//			//+++++++++++++++++++++
//			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmEnum = this.getHashMapEnumSetForCascadingStatusLocal();
//			if(hmEnum==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keine Mapping Hashmap fuer das StatusMapping vorhanden. Breche ab";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//			
//			//+++++++++++++++++++++
//			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL objEnum = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) hmEnum.get(enumStatus);							
//			if(objEnum==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status für en Status aus dem Event-Objekt erhalten. Breche ab";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//			
//			boolean bValue = eventStatusLocalSet.getStatusValue();
//							
////			boolean bHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR)&& bValue;
////			boolean bEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED) && bValue;
////			boolean bHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION) && bValue;
////			boolean bHasConnectionLost = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST) && bValue;
////		
//			boolean bEventHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
//			boolean bEventEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
//			boolean bEventHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
//			boolean bEventHasConnectionLost = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST);
//			
//		
//			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
//			//ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//			
//			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
//			IApplicationOVPN  objApplication = null;				
//			ServerConfigStarterOVPN objStarter=null;
//			int iIndex = -1;
//		
//			//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
//			objApplication = eventStatusLocalSet.getApplicationObjectUsed();
//			if(objApplication==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Event-Objekt erhalten.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}else {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Event-Objekt erhalten.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				
//			}
//				
//			objStarter = eventStatusLocalSet.getServerConfigStarterObjectUsed();
//			if(objStarter==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}else {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				
//				iIndex = objStarter.getIndex();
//			}
//						
//			boolean bStatusLocalSet = this.setStatusLocalEnum(iIndex, objEnum, bValue);//Es wird ein Event gefeuert, an dem das Backend-Objekt registriert wird und dann sich passend einstellen kann.
//			if(!bStatusLocalSet) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//			//++++++++++++++
//			
//			if(bEventHasError && bEventEnded){
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasError && bEventEnded";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);					
//			}else if((!bEventHasError) && bEventEnded){
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bEventHasError && bEventEnded";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				
//			}else if(bEventHasConnection){
//				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasConnection";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				
//				String sVpnIp = objApplication.getVpnIpRemote();
//				int iId = eventStatusLocalSet.getProcessID();
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbindung mit remote VPNIP='"+sVpnIp+"'";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);										
//				
//				boolean bEndOnConnection = this.getFlag(IServerThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION);
//				if(bEndOnConnection) {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Beende den Monitor.";
//					System.out.println(sLog);
//					this.getMainObject().logProtocolString(sLog);
//				}
//				
//			}else if(bEventHasConnectionLost) {
//				//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasConnectionLost";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				
//				String sVpnIp = objApplication.getVpnIpRemote();
//				int iId = eventStatusLocalSet.getProcessID();
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbindung verloren mit remote VPNIP='"+sVpnIp+"'";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);										
//	
//			}else {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status '"+enumStatus.getAbbreviation()+"' nicht weiter behandelt";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);	
//			}
//			
//			bReturn = true;
//		}//end main:			
//		return bReturn;
//	}
	
	
//	//FGL20251022:Erste einmal raus und sich auf die abstrakte Klasse AbstractObjectWithStatusOnStatusListeningZZZ verlassen.
//	//            und deren Methode public boolean isEventRelevant4ReactionOnStatusLocal(IEventObjectStatusLocalZZZ eventStatusLocalReact) throws ExceptionZZZ {
//	//#######################################################
//	/* (non-Javadoc)
//	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
//	 */
//	@Override
//	public boolean isEventRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		boolean bReturn = false;
//		main:{
//			if(eventStatusLocalSet==null)break main;
//			
//			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
//			System.out.println(sLog);
//			this.getMainObject().logProtocolString(sLog);
//			
//			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
//			if(enumStatus==null) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);							
//				break main;
//			}
//							
//			sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen.";
//			System.out.println(sLog);
//			this.getMainObject().logProtocolString(sLog);
//				
//			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
//			System.out.println(sLog);
//			this.getMainObject().logProtocolString(sLog);	
//				
//			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
//			System.out.println(sLog);
//			this.getMainObject().logProtocolString(sLog);
//			
//			//+++ Pruefungen
//			bReturn = this.isEventRelevantByClass(eventStatusLocalSet);
//			if(!bReturn) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);				
//				break main;
//			}
//			
//			bReturn = this.isStatusChanged(eventStatusLocalSet.getStatusText());
//			if(!bReturn) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				break main;
//			}
//						
//			bReturn = this.isEventRelevantByStatusLocalValue(eventStatusLocalSet);
//			if(!bReturn) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);				
//				break main;
//			}
//			
//			bReturn = this.isEventRelevantByStatusLocal(eventStatusLocalSet);
//			if(!bReturn) {
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);				
//				break main;
//			}
//													
//			bReturn = true;
//		}//end main:
//		return bReturn;
//	}
	
//	//FGL20251022:Erste einmal raus und sich auf die abstrakte Klasse AbstractObjectWithStatusOnStatusListeningZZZ verlassen.
//	//            und deren Methode public boolean isEventRelevant4ReactionOnStatusLocal(IEventObjectStatusLocalZZZ eventStatusLocalReact) throws ExceptionZZZ {
//	/* (non-Javadoc)
//	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevantByStatusLocalValue(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
//	 */
//	@Override
//	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		boolean bReturn = false;
//		main:{
//			if(eventStatusLocalSet==null)break main;
//			
//			//boolean bStatusValue = eventStatusLocalSet.getStatusValue();
//			//Merke: Beim Monitor interessieren auch "false" Werte, um den Status ggfs. wieder zuruecksetzen zu koennen
//			//if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
//			
//			bReturn = true;
//		}
//		return bReturn;
//	}
	
//	//FGL20251022:Erste einmal raus und sich auf die abstrakte Klasse AbstractObjectWithStatusOnStatusListeningZZZ verlassen.
//	//            und deren Methode ?????
//	@Override
//	public boolean isEventRelevantByClass(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//		/* Loesung: DOWNCASTING mit instanceof , s.: https://www.positioniseverything.net/typeof-java/
//	 	class Animal { }
//		class Dog2 extends Animal {
//			static void method(Animal j) {
//			if(j instanceof Dog2){
//			Dog2 d=(Dog2)j;//downcasting
//			System.out.println(“downcasting done”);
//			}
//			}
//			public static void main (String [] args) {
//			Animal j=new Dog2();
//			Dog2.method(j);
//			}
//		}
//	 */
//	
//		boolean bReturn = false;
//		main:{
//			//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
//			if(eventStatusLocalSet.getStatusEnum() instanceof IProcessWatchRunnerOVPN.STATUSLOCAL){
//				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Enum Klasse ist instanceof IProcessWatchRunnerOVPN. Damit relevant.";
//				System.out.println(sLog);
//				this.getMainObject().logProtocolString(sLog);
//				bReturn = true;
//				break main;
//			}		
//			
//			
//		}//end main:
//		return bReturn;
//	}
	

	/* (non-Javadoc)
	 * @see use.openvpn.server.status.IListenerObjectStatusLocalOVPN#isEventRelevantByStatusLocal(use.openvpn.server.status.IEventObjectStatusLocalOVPN)
	 */
	@Override
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			// enumStatus = (IEnumSetMappedStatusZZZ) eventStatusLocalSet.getStatusEnum();							
			Enum enumStatus = eventStatusLocalSet.getStatusEnum();
			//bReturn = this.isStatusLocalRelevant(enumStatus);
			bReturn = this.isEventRelevantByStatusLocal((IEventObjectStatusLocalOVPN) enumStatus);
			if(!bReturn) break main;
			
			
			String sAbr = eventStatusLocalSet.getStatusAbbreviation();
			if(!StringZZZ.startsWith(sAbr, "hasconnection")) break main;
			
			bReturn = true;
		}//end main:
		return bReturn;
	}

	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocal(java.lang.Enum, boolean)
	 */
	@Override
	public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
							
			bFunction = this.offerStatusLocal(enumStatus, bStatusValue, null);
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
			
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);				
		}//end main;
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	/* (non-Javadoc)
	 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocalEnum(basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ, boolean)
	 */
	@Override 
	public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(enumStatus, bStatusValue, null);
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
				IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
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
				IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sMessage, bStatusValue);
			}//end main:
			return bFunction;
		}
		
		/* (non-Javadoc)
		 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocalEnum(basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ, boolean, java.lang.String)
		 */
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue, String sMessage) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
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
				IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		//++++++++++++++++++++++++++++++++++++++++

		public boolean setStatusLocalEnum(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, bStatusValue);
			}//end main:
			return bReturn;
		}


	
	//Weil auf den Status anderer Thread gehoert wird und diese weitergeleitet werden sollen.
	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> createHashMapEnumSetForCascadingStatusLocalCustom() {
		
		//Es wird auf Events des ProcessWatchRunnerOVPN gehoert.
		//Die dort geworfenen Events werden hier auf LokaleEvents gemappt.
		//Aufbau der Map: Ankommender externer Event = Lokaler Event
		//Lokale Events, die keine externe Entsprechung haben, tauchen hier nicht auf
		HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmReturn = new HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>();
		main:{
			
			//Merke: Reine Lokale Statuswerte kommen nicht aus einem Event und werden daher nicht gemapped. 			
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTNEW, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTNEW);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTING, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTING);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTED, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTED);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASOUTPUT, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSOUTPUT);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTION);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTIONLOST);
			
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTOPPED);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR, IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSERROR);
						
		}//end main:
		return hmReturn;	
	}

	@Override
	public boolean queryReactOnStatusLocalEventCustom(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean queryReactOnStatusLocal4ActionCustom(String sActionAlias, IEnumSetMappedStatusZZZ enumStatus,
			boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEventRelevant2ChangeStatusLocalByClass(IEventObjectStatusLocalZZZ eventStatusLocal)
			throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEventRelevant2ChangeStatusLocalByStatusLocalValue(IEventObjectStatusLocalZZZ eventStatusLocal)
			throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	
	/* (non-Javadoc)
	 * @see use.openvpn.server.status.IListenerObjectStatusLocalOVPN#changeStatusLocal(use.openvpn.server.status.IEventObjectStatusLocalOVPN)
	 */
	@Override
	public boolean changeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEventRelevant(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEventRelevantByClass(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalOVPN eventStatusLocalSet)
			throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	

	@Override
	public HashMap createHashMapStatusLocal4ReactionCustom_String() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reactOnStatusLocal4ActionCustom(String sAction, IEnumSetMappedStatusZZZ enumStatus,
			boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean queryOfferStatusLocalCustom() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startCustom() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HashMap createHashMapStatusLocal4ReactionCustom_Enum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap createHashMapStatusLocal4ReactionCustom_EnumStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}//END class