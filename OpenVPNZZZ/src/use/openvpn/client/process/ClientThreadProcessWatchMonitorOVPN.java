package use.openvpn.client.process;

import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectWithStatusZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.component.IModuleUserZZZ;
import basic.zBasic.component.IModuleZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.component.IProgramZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.moduleExternal.process.watch.IProcessWatchRunnerZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusListeningCascadedZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.EventObjectFlagZsetZZZ;
import basic.zKernel.flag.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.status.EventObject4ProcessMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventObject4ProcessWatchMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalSetOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;

public class ClientThreadProcessWatchMonitorOVPN extends AbstractKernelUseObjectWithStatusListeningCascadedZZZ implements IClientThreadProcessWatchMonitorOVPN,IListenerObjectStatusLocalSetOVPN, IEventBrokerStatusLocalSetUserOVPN{
	protected volatile IModuleZZZ objModule = null;
	protected volatile String sModuleName=null;
	protected volatile String sProgramName = null;
	
	
	private IClientMainOVPN objMain = null;
	private ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
public ClientThreadProcessWatchMonitorOVPN(IKernelZZZ objKernel, ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	MonitorNew_(objMain, saFlagControl);
}

private void MonitorNew_(IClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
	main:{
		
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
		
						
			this.objMain = objMain;
		}//End check

	}//END main
}
	@Override
	public boolean reset() throws ExceptionZZZ {
		this.resetProgramUsed();
		this.resetModuleUsed();
		this.resetFlags();
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public boolean start() throws ExceptionZZZ, InterruptedException {
		boolean bReturn = false;
		main:{
			if(this.getMainObject()==null) break main;
					   	
								
				//######### 20230826 Verschoben aus ClientMainOVPN.start(), durch das Aufteilen sind mehrere Prozesse parallel moeglich.					
 				//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
 				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Trying to establish a new connection with every OVPN-configuration-file. Starting threads.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);	
 				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.								
				//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
				//besser ueber eine geworfenen Event... und nicht direkt: this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
				boolean bStartNewGoon = this.setStatusLocal(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, true);
				if(!bStartNewGoon) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Starting monitor thread canceled.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
								
 				ArrayList<ClientConfigStarterOVPN> listaStarter = this.getMainObject().getClientConfigStarterList();
 				
 				//Starten der Threads, mit denen die OVPN-Processe gestartet werden sollen
 				Thread[] threadaOVPN = new Thread[listaStarter.size()];
 				ProcessWatchRunnerOVPN[] runneraOVPN = new ProcessWatchRunnerOVPN[listaStarter.size()];	
 				int iNumberOfProcessStarted = 0;
 				for(int icount = 0; icount < listaStarter.size(); icount++){
 					iNumberOfProcessStarted++;	
 					ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN)listaStarter.get(icount);
 					Process objProcess = objStarter.requestStart();
 					if(objProcess==null){
 						//Hier nicht abbrechen, sondern die Verarbeitung bei der naechsten Datei fortfuehren
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaStarter.size();
 						System.out.println(sLog);
 						this.getMainObject().logProtocolString(sLog); 						
 					}else{	
 						
 						//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Successfull process created, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaStarter.size() +". Starting Thread as Monitor for this process.";
 						System.out.println(sLog);
 						this.getMainObject().logProtocolString(sLog);
 				
 						//TEST, Flagübergabe: Ohne, z.B. die Pruefung auf vorherige Werte wird immer ein Event geworfen fuer "HASOUTPUT"
 						//runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name());
 						//runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted);
 						//String[]saFlagControl = {IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
 						//String[]saFlagControl = {IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
 						String[]saFlagControl = {IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUE.name(), IObjectWithStatusZZZ.FLAGZ.STATUSLOCAL_PROOF_VALUECHANGED.name()};
 						runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, saFlagControl);
 						
 						runneraOVPN[icount].setClientBackendObject(this.getMainObject());
 						runneraOVPN[icount].setClientConfigStarterObject(objStarter);
 						
 						//Wichtig, den ProcessWatchMonitorOVPN an den ProcessWatchRunnerOVPN Listener registrieren.
 						//Dafuer gibt es dann auch ein Mapping, in dem steht wie mit den empfangenen Events umgegangen wird, bzw. welche eigenen Events geworfen werden sollen. 						
 						runneraOVPN[icount].registerForStatusLocalEvent((IListenerObjectStatusLocalSetOVPN)this);//Registriere den Monitor nun am ProcessWatchRunner
 						 						
 						threadaOVPN[icount] = new Thread(runneraOVPN[icount]);//Starte den ProcessWatchRunner					
 						threadaOVPN[icount].start();	 						 
 						this.getMainObject().logProtocolString("");
 						
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Finished starting thread #" + iNumberOfProcessStarted + " von " + listaStarter.size() + " for watching connection.";
 						System.out.println(sLog);
 						this.getMainObject().logProtocolString(sLog);
 					}
 				}//END for	
 					
				//MERKE: Der ClientThreadMonitor feuert nun seinerseits einen Status ab, der z.B. vom Tray als registriertes Objekt empfangen wird.
 				//also nicht mehr so...
				//boolean bStatusLocalIsConnectingExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
				//sLog = ReflectCodeZZZ.getPositionCurrent()+": Status gesetzt auf '" + ClientMainOVPN.STATUSLOCAL.ISCONNECTING.getAbbreviation() + "'.";
				//System.out.println(sLog);
				//this.objMain.logMessageString(sLog);
				
 				this.setStatusLocal(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTED,true);
 				bReturn = true;		
	}//END main:
	return bReturn;
	}//END start()
	
	
	public void run() {
		try {
			this.start();
		} catch (ExceptionZZZ ez) {
			try {
				this.logLineDate(ez.getDetailAllLast());
			} catch (ExceptionZZZ e1) {
				System.out.println(e1.getDetailAllLast());
				e1.printStackTrace();
			}
			
			try {
				String sLog = ez.getDetailAllLast();
				this.logLineDate("An error happend: '" + sLog + "'");
			} catch (ExceptionZZZ e1) {				
				System.out.println(ez.getDetailAllLast());
				e1.printStackTrace();
			}			
		} catch (InterruptedException e) {					
			try {
				String sLog = e.getMessage();
				this.logLineDate("An error happend: '" + sLog + "'");
			} catch (ExceptionZZZ e1) {
				System.out.println(e1.getDetailAllLast());
				e1.printStackTrace();
			}
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void setMainObject(IClientMainOVPN objClientBackend){
		this.objMain = (ClientMainOVPN) objClientBackend;
	}
	
	@Override
	public IClientMainOVPN getMainObject(){
		return this.objMain;
	}
	
	
	
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
		    this.getLogObject().WriteLineDate(sLog);			
		}
		return bReturn;
	}
	
	//###### FLAGS
	/* @see basic.zBasic.IFlagZZZ#getFlagZ(java.lang.String)
	 * 	 Weitere Voraussetzungen:
	 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
	 * - Innere Klassen muessen auch public deklariert werden.(non-Javadoc)
	 */
	public boolean getFlag(String sFlagName) {
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
		public boolean getFlag(IClientThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) {
			return this.getFlag(objEnumFlag.name());
		}
		@Override
		public boolean setFlag(IClientThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			return this.setFlag(objEnumFlag.name(), bFlagValue);
		}
		
		@Override
		public boolean[] setFlag(IClientThreadProcessWatchMonitorOVPN.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			boolean[] baReturn=null;
			main:{
				if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
					baReturn = new boolean[objaEnumFlag.length];
					int iCounter=-1;
					for(IClientThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
						iCounter++;
						boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
						baReturn[iCounter]=bReturn;
					}
					
					//!!! Ein mögliches init-Flag ist beim direkten setzen der Flags unlogisch.
					//    Es wird entfernt.
					this.setFlag(IFlagZUserZZZ.FLAGZ.INIT, false);
				}
			}//end main:
			return baReturn;
		}
		
		@Override
		public boolean proofFlagExists(IClientThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagExists(objEnumFlag.name());
		}	
		
		@Override
		public boolean proofFlagSetBefore(IClientThreadProcessWatchMonitorOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagSetBefore(objEnumFlag.name());
		}

		
		//### Aus IKernelModuleZZZ
		public String readModuleName() throws ExceptionZZZ {
			String sReturn = null;
			main:{
				sReturn = this.getClass().getName();
			}//end main:
			return sReturn;
		}
		
		@Override
		public String getModuleName() throws ExceptionZZZ{
			if(StringZZZ.isEmpty(this.sModuleName)) {
				this.sModuleName = this.readModuleName();
			}
			return this.sModuleName;
		}
		
		public void setModuleName(String sModuleName){
			this.sModuleName=sModuleName;
		}
		
		
		@Override
		public void resetModuleUsed() {
			this.setModuleName(null);
		}
		
		//##########################################
			//### FLAG HANDLING
			@Override
			public boolean getFlag(IProgramRunnableZZZ.FLAGZ objEnumFlag) {
				return this.getFlag(objEnumFlag.name());
			}
			@Override
			public boolean setFlag(IProgramRunnableZZZ.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				return this.setFlag(objEnumFlag.name(), bFlagValue);
			}
			
			@Override
			public boolean[] setFlag(IProgramRunnableZZZ.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				boolean[] baReturn=null;
				main:{
					if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
						baReturn = new boolean[objaEnumFlag.length];
						int iCounter=-1;
						for(IProgramRunnableZZZ.FLAGZ objEnumFlag:objaEnumFlag) {
							iCounter++;
							boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
							baReturn[iCounter]=bReturn;
						}
					}
				}//end main:
				return baReturn;
			}
			
			@Override
			public boolean proofFlagExists(IProgramRunnableZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
					return this.proofFlagExists(objEnumFlag.name());
				}
			
			@Override
			public boolean proofFlagSetBefore(IProgramRunnableZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
				return this.proofFlagSetBefore(objEnumFlag.name());
			}	
			
			
			//### Aus IProgramZZZ
			@Override
			public String getProgramName(){
				if(StringZZZ.isEmpty(this.sProgramName)) {
					if(this.getFlag(IProgramZZZ.FLAGZ.ISPROGRAM.name())) {
						this.sProgramName = this.getClass().getName();
					}
				}
				return this.sProgramName;
			}
			
			@Override
			public String getProgramAlias() throws ExceptionZZZ {		
				return null;
			}
				
			@Override
			public void resetProgramUsed() {
				this.sProgramName = null;
			}
			
			@Override
			public boolean getFlag(IProgramZZZ.FLAGZ objEnumFlag) {
				return this.getFlag(objEnumFlag.name());
			}
			@Override
			public boolean setFlag(IProgramZZZ.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				return this.setFlag(objEnumFlag.name(), bFlagValue);
			}
			
			@Override
			public boolean[] setFlag(IProgramZZZ.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				boolean[] baReturn=null;
				main:{
					if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
						baReturn = new boolean[objaEnumFlag.length];
						int iCounter=-1;
						for(IProgramZZZ.FLAGZ objEnumFlag:objaEnumFlag) {
							iCounter++;
							boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
							baReturn[iCounter]=bReturn;
						}
					}
				}//end main:
				return baReturn;
			}
			
			@Override
			public boolean proofFlagExists(IProgramZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
					return this.proofFlagExists(objEnumFlag.name());
				}
			
			@Override
			public boolean proofFlagSetBefore(IProgramZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
				return this.proofFlagSetBefore(objEnumFlag.name());
			}	


			
			
			//### Aus IKernelModuleUserZZZ	
			@Override
			public IModuleZZZ getModule() {
				return this.objModule;
			}
			
			@Override
			public void setModule(IModuleZZZ objModule) {
				this.objModule = objModule;
			}
			
			
			@Override
			public boolean getFlag(IModuleUserZZZ.FLAGZ objEnumFlag) {
				return this.getFlag(objEnumFlag.name());
			}
			@Override
			public boolean setFlag(IModuleUserZZZ.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				return this.setFlag(objEnumFlag.name(), bFlagValue);
			}
			
			@Override
			public boolean[] setFlag(IModuleUserZZZ.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
				boolean[] baReturn=null;
				main:{
					if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
						baReturn = new boolean[objaEnumFlag.length];
						int iCounter=-1;
						for(IModuleUserZZZ.FLAGZ objEnumFlag:objaEnumFlag) {
							iCounter++;
							boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
							baReturn[iCounter]=bReturn;
						}
					}
				}//end main:
				return baReturn;
			}
			
			@Override
			public boolean proofFlagExists(IModuleUserZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
					return this.proofFlagExists(objEnumFlag.name());
			}
			
			@Override
			public boolean proofFlagSetBefore(IModuleUserZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
				return this.proofFlagExists(objEnumFlag.name());
			}		
			//##########################

		
		//####### aus ISenderObjectStatusLocalSetUserOVPN
		@Override
		public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
			if(this.objEventStatusLocalBroker==null) {
				//++++++++++++++++++++++++++++++
				//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
				ISenderObjectStatusLocalSetOVPN objSenderStatusLocal = new SenderObjectStatusLocalSetOVPN();
				this.objEventStatusLocalBroker = objSenderStatusLocal;
			}
			return this.objEventStatusLocalBroker;
		}

		@Override
		public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
			this.objEventStatusLocalBroker = objEventSender;
		}

		//### aus IEventBrokerStatusLocalSetUserOVPN
		@Override
		public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener)throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);		
		}

		@Override
		public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);;
		}

		//#############
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
				IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
								
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
				
				IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);				
			}//end main;
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, null, bStatusValue);
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
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
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
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
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
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
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
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
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
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		
		
		//++++++++++++++++++++++++
		/* (non-Javadoc)
		 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocal(java.lang.Enum, java.lang.String, boolean)
		 */
		@Override
		public boolean offerStatusLocal(Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				
				IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);				
			}//end main;
			return bFunction;
		}
		
		@Override
		public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				
				IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);				
			}//end main;
			return bFunction;
		}
		
		private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) break main;
				
			
		    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
		    IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			String sStatusName = enumStatus.name();
			bFunction = this.proofStatusLocalExists(sStatusName);															
			if(!bFunction) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event, but this status is not available: '" + sStatusName + "'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);			
				break main;
			}
				
			bFunction = this.proofStatusLocalValueChanged(sStatusName, bStatusValue);
			if(!bFunction) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor would like to fire event, but this status has not changed: '" + sStatusName + "'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
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
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);

			//Falls eine Message extra uebergeben worden ist, ueberschreibe...
			if(sStatusMessageToSet!=null) {
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain setze sStatusMessageToSet='" + sStatusMessageToSet + "'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
			}
			//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
			this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);
			
			
			
			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.	
			if(this.getSenderStatusLocalUsed()==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);		
				break main;
			}
			
			//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			IEventObject4ProcessWatchMonitorStatusLocalSetOVPN event = new EventObject4ProcessMonitorStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);			
			event.setApplicationObjectUsed(this.getMainObject().getApplicationObject());
						
			//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process iIndex= '" + iIndexOfProcess + "'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			if(iIndexOfProcess>=0) {
				event.setClientConfigStarterObjectUsed(this.getMainObject().getClientConfigStarterList().get(iIndexOfProcess));
			}		
			
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process fires event '" + enumStatus.getAbbreviation() + "'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			this.getSenderStatusLocalUsed().fireEvent(event);
					
			bFunction = true;				
		}	// end main:
		return bFunction;
		}
		
		@Override
		public boolean changedStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			//Der Monitor ist am ProcessWatchRunner registriert.
			//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
			boolean bReturn = false;
			main:{
				if(eventStatusLocalSet==null)break main;
				
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				boolean bRelevant = this.isEventRelevant2ChangeStatusLocal(eventStatusLocalSet); 
				if(!bRelevant) {
					sLog = 	ReflectCodeZZZ.getPositionCurrent() + ": Event / Status nicht relevant. Breche ab.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
				
				//+++ Mappe nun die eingehenden Status-Enums auf die eigenen.
				IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();
				
				//+++++++++++++++++++++
				HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedStatusZZZ>hmEnum = this.getHashMapEnumSetForCascadingStatusLocal();
				ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL objEnum = (ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) hmEnum.get(enumStatus);							
				if(objEnum==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
				
				boolean bValue = eventStatusLocalSet.getStatusValue();
								
//				boolean bHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR)&& bValue;
//				boolean bEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED) && bValue;
//				boolean bHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION) && bValue;
//				boolean bHasConnectionLost = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST) && bValue;
//			
				boolean bEventHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
				boolean bEventEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
				boolean bEventHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
				boolean bEventHasConnectionLost = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST);
				
			
				//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
				//ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
				
				//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
				IApplicationOVPN  objApplication = null;				
				ClientConfigStarterOVPN objStarter=null;
				int iIndex = -1;
			
				//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
				objApplication = eventStatusLocalSet.getApplicationObjectUsed();
				if(objApplication==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Event-Objekt erhalten.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Event-Objekt erhalten.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					
				}
					
				objStarter = eventStatusLocalSet.getClientConfigStarterObjectUsed();
				if(objStarter==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": ConfigStarter-Objekt aus dem Event-Objekt erhalten.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					
					iIndex = objStarter.getIndex();
				}
				
					
				boolean bStatusLocalSet = this.setStatusLocal(iIndex, objEnum, bValue);//Es wird ein Event gefeuert, an dem das Backend-Objekt registriert wird und dann sich passend einstellen kann.
				if(!bStatusLocalSet) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
				//++++++++++++++
				
				if(bEventHasError && bEventEnded){
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasError && bEventEnded";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);					
				}else if((!bEventHasError) && bEventEnded){
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bEventHasError && bEventEnded";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					
				}else if(bEventHasConnection){
					//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasConnection";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					
					String sVpnIp = objApplication.getVpnIpRemote();
					int iId = eventStatusLocalSet.getProcessID();
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbindung mit remote VPNIP='"+sVpnIp+"'";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);										
					
					boolean bEndOnConnection = this.getFlag(IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION);
					if(bEndOnConnection) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Beende den Monitor.";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);
					}
					
				}else if(bEventHasConnectionLost) {
					//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bEventHasConnectionLost";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					
					String sVpnIp = objApplication.getVpnIpRemote();
					int iId = eventStatusLocalSet.getProcessID();
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbindung verloren mit remote VPNIP='"+sVpnIp+"'";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);										
		
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status '"+enumStatus.getAbbreviation()+"' nicht weiter behandelt";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);	
				}
				
				bReturn = true;
			}//end main:			
			return bReturn;
		}
		
		/* (non-Javadoc)
		 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
		 */
		@Override
		public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(eventStatusLocalSet==null)break main;
				
				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
				if(enumStatus==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);							
					break main;
				}
								
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
					
				sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);	
					
				sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				//+++ Pruefungen
				bReturn = this.isEventRelevantByClass2ChangeStatusLocal(eventStatusLocalSet);
				if(!bReturn) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);				
					break main;
				}
				
				bReturn = this.isStatusChanged(eventStatusLocalSet.getStatusText());
				if(!bReturn) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
							
				bReturn = this.isEventRelevantByStatusLocalValue2ChangeStatusLocal(eventStatusLocalSet);
				if(!bReturn) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);				
					break main;
				}
				
				bReturn = this.isEventRelevantByStatusLocal2ChangeStatusLocal(eventStatusLocalSet);
				if(!bReturn) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);				
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
		public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(eventStatusLocalSet==null)break main;
				
				//boolean bStatusValue = eventStatusLocalSet.getStatusValue();
				//Merke: Beim Monitor interessieren auch "false" Werte, um den Status ggfs. wieder zuruecksetzen zu koennen
				//if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
				
				bReturn = true;
			}
			return bReturn;
		}
		
		@Override
		public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
				//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
				if(eventStatusLocalSet.getStatusEnum() instanceof IProcessWatchRunnerOVPN.STATUSLOCAL){
					String sLog = ReflectCodeZZZ.getPositionCurrent()+": Enum Klasse ist instanceof IProcessWatchRunnerOVPN. Damit relevant.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
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
		public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet)	throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				IEnumSetMappedStatusZZZ enumStatus = eventStatusLocalSet.getStatusEnum();							
				bReturn = this.isStatusLocalRelevant(enumStatus);
				if(!bReturn) break main;
				
				
				String sAbr = eventStatusLocalSet.getStatusAbbreviation();
				if(!StringZZZ.startsWith(sAbr, "hasconnection")) break main;
				
				bReturn = true;
			}//end main:
			return bReturn;
		}
		
		@Override
		public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalSetZZZ eventStatusLocalSet) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalSetZZZ eventStatusLocalSet) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalSetZZZ eventStatusLocalSet) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalSetZZZ eventStatusLocalSet)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}
		
		
		//#######################################
		//#######################################		
		/* (non-Javadoc)
		 * @see basic.zBasic.AbstractObjectWithStatusZZZ#isStatusLocalRelevant(basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ)
		 */
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
		
		
	public boolean getStatusLocal(Enum objEnumStatusIn) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(objEnumStatusIn==null) {
				break main;
			}
			
			//Merke: Bei einer anderen Klasse, die dieses DesingPattern nutzt, befindet sich der STATUSLOCAL in einer anderen Klasse
			ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
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
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTNEW, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTNEW);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTING, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTING);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTARTED, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTARTED);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASOUTPUT, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSOUTPUT);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTION);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSCONNECTIONLOST);
			
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSSTOPPED);
			hmReturn.put(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR, IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASPROCESSERROR);
						
		}//end main:
		return hmReturn;	
	}
}//END class
