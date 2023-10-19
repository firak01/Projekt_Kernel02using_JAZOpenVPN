package use.openvpn.client.process;

import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusZZZ;
import basic.zKernel.IKernelZZZ;
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

public class ClientThreadProcessWatchMonitorOVPN extends AbstractKernelUseObjectWithStatusZZZ implements IClientThreadProcessWatchMonitorOVPN, Runnable,IListenerObjectStatusLocalSetOVPN, IEventBrokerStatusLocalSetUserOVPN{
	private ClientMainOVPN objMain = null;
	
	private String sWatchRunnerStatus = new String("");            //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatusPrevious = new String("");    //den vorherigen Status festhalten, damit z.B. nicht immer wieder das Icon geholt wird.
	
	private ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
public ClientThreadProcessWatchMonitorOVPN(IKernelZZZ objKernel, ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ConfigMonitorRunnerNew_(objMain, saFlagControl);
}

private void ConfigMonitorRunnerNew_(ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
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
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public void run() {		
		main:{
		try {
			check:{
				if(this.objMain==null) break main;
			}//END check:
		   	
								
				//######### 20230826 Verschoben aus ClientMainOVPN.start(), durch das Aufteilen sind mehrere Prozesse parallel moeglich.					
 				//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
 				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Trying to establish a new connection with every OVPN-configuration-file. Starting threads.";
				System.out.println(sLog);
				this.objMain.logMessageString(sLog);	
 				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.								
				//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
				//besser ueber eine geworfenen Event... und nicht direkt: this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
				this.setStatusLocal(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISSTARTING, true);
				
				
 				ArrayList<ClientConfigStarterOVPN> listaStarter = this.objMain.getClientConfigStarterList();
 				
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
 						this.objMain.logMessageString(sLog); 						
 					}else{	
 						
 						//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Successfull process created, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaStarter.size() +". Starting Thread as Monitor for this process.";
 						System.out.println(sLog);
 						this.objMain.logMessageString(sLog);
 				
 						//TEST, Flagübergabe   runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION.name()); 						
 						runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, (String[]) null);
 						runneraOVPN[icount].setClientBackendObject(this.getClientBackendObject());
 						runneraOVPN[icount].registerForStatusLocalEvent((IListenerObjectStatusLocalSetOVPN)this);//Registriere den Monitor nun am ProcessWatchRunner
 						 						
 						threadaOVPN[icount] = new Thread(runneraOVPN[icount]);//Starte den ProcessWatchRunner					
 						threadaOVPN[icount].start();	 						 
 						this.objMain.logMessageString("");
 						
 						sLog = ReflectCodeZZZ.getPositionCurrent()+": Finished starting thread #" + iNumberOfProcessStarted + " von " + listaStarter.size() + " for watching connection.";
 						System.out.println(sLog);
 						this.objMain.logMessageString(sLog);
 					}
 				}//END for	
 					
				//Der ClientThreadMonitor feuert nun seinerseits einen Status ab, der z.B. vom Tray als registriertes Objekt empfangen wird.
 				//also nicht mehr so...
//				boolean bStatusLocalIsConnectingExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status gesetzt auf '" + ClientMainOVPN.STATUSLOCAL.ISCONNECTING.getAbbreviation() + "'.";
//				System.out.println(sLog);
//				this.objMain.logMessageString(sLog);
				
 				this.setStatusLocal(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTING,true);
 				
 				//this.isEventStatusLocalRelevant(eventStatusLocalSet)
 					
		}catch(ExceptionZZZ ez){
			System.out.println(ez.getDetailAllLast());
		}
	}//END main:
	
	}//END run
	
	public void setClientBackendObject(IClientMainOVPN objClientBackend){
		this.objMain = (ClientMainOVPN) objClientBackend;
	}
	public ClientMainOVPN getClientBackendObject(){
		return this.objMain;
	}
	
	/**
	 * @return
	 * @author Fritz Lindhauer, 11.10.2023, 08:25:40
	 */
	public String getStatusString(){
		return this.sWatchRunnerStatus;
	}
	
	/**
	 * @param sStatus
	 * @author Fritz Lindhauer, 11.10.2023, 08:25:45
	 */
	public void setStatusString(String sStatus) {		
		main:{
			String sStatusPrevious = this.getStatusString();
			if(sStatus == null) {
				if(sStatusPrevious==null)break main;
			}
			
			if(!sStatus.equals(sStatusPrevious)) {
				String sStatusCurrent = this.getStatusString();
				this.sWatchRunnerStatus = sStatus;
				this.setStatusPrevious(sStatusCurrent);
			}
		}//end main:
	
	}
	
	/**
	 * @return
	 * @author Fritz Lindhauer, 11.10.2023, 08:25:54
	 */
	public String getStatusPreviousString() {
		return this.sWatchRunnerStatusPrevious;
	}
	
	/**
	 * @param sStatusPrevious
	 * @author Fritz Lindhauer, 11.10.2023, 08:26:03
	 */
	public void setStatusPrevious(String sStatusPrevious) {
		this.sWatchRunnerStatusPrevious = sStatusPrevious;
	}
	
	/**
	 * @param sStatusString
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 11.10.2023, 08:26:07
	 */
	public boolean isStatusChanged(String sStatusString) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(sStatusString == null) {
				bReturn = this.getStatusString()==null;
				break main;
			}
			
			if(!sStatusString.equals(this.getStatusString())) {
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
	
	
	//### aus IListenerObjectStatusLocalSetOVPN
	@Override
	public boolean isEventStatusLocalRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		
		main:{
			String sAbr = eventStatusLocalSet.getStatusAbbreviation();
			if(!StringZZZ.startsWith(sAbr, "isconnect")) break main;
			
			bReturn = true;			
		}//end main:
		
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
			this.objEventStatusLocalBroker = objEventStatusLocalBroker;
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
				
				//Setze die Message, die beim Click auf den Tray angezeigt werden könnte
				String sStatusMessage = enumStatus.getStatusMessage();
				this.setStatusString(sStatusMessage);
				
				bFunction = this.setStatusLocal(enumStatus, sStatusMessage, bStatusValue);
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
			//return this.getStatusLocal(objEnumStatus.name());
			//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum
			
		    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
		    IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL enumStatus = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) enumStatusIn;
			String sStatusName = enumStatus.name();
			bFunction = this.proofStatusLocalExists(sStatusName);															
			if(bFunction){
				
				bFunction = this.proofStatusLocalChanged(sStatusName, bStatusValue);
				if(bFunction) {
					//Setze das Flag nun in die HashMap
					HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
					hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
									
					//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
					//Dann erzeuge den Event und feuer ihn ab.
					//Merke: Nun aber ueber das enum			
					if(this.getSenderStatusLocalUsed()!=null) {
		
						IEventObject4ProcessWatchMonitorStatusLocalSetOVPN event = new EventObject4ProcessMonitorStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);
						event.setApplicationObjectUsed(this.getClientBackendObject().getApplicationObject());
						
						String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process fires event '" + enumStatus.getAbbreviation() + "'";
						System.out.println(sLog);
						this.logLineDate(sLog);
						this.getSenderStatusLocalUsed().fireEvent(event);
					}else {
						String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
						System.out.println(sLog);
						this.logLineDate(sLog);
					}
					bFunction = true;
				}else{
					//Mache nix, auch nix protokollieren, da sich nix geaendert hat.
				}//StatusLocalChanged
			}else {
					
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event, but this status is not available: '" + sStatusName + "'";
				System.out.println(sLog);
				this.logLineDate(sLog);
				
				bFunction = false;				
			}//StatusLocalExists
			
		}	// end main:
		return bFunction;
		}
		
		@Override
		public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
			//Der Monitor ist am ProcessWatchRunner registriert.
			//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
			boolean bReturn = false;
			main:{
				String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Event gefangen.";
				System.out.println(sLog);
				this.getClientBackendObject().logMessageString(sLog);
				
				IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
				if(enumStatus==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);							
					break main;
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen.";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);
					
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
					
					
					sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);	
					
					sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);
					
					boolean bRelevant = this.isStatusLocalRelevant(enumStatus);
					if(!bRelevant) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"' ist nicht relevant. Breche ab.";
						System.out.println(sLog);
						this.getClientBackendObject().logMessageString(sLog);
					}														
				}
				
				
				boolean bHasError = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
				boolean bEnded = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
				boolean bHasConnection = enumStatus.equals(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
				
				
				if(bHasError && bEnded){
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bHasError && bEnded";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);					
				}else if((!bHasError) && bEnded){
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status !bHasError && bEnded";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);
					
				}else if(bHasConnection){
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status bHasConnection";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);
					
					//+++++++++++++++++++++					
					//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
					//ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
					
					//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des Events.
					IApplicationOVPN  objApplication = eventStatusLocalSet.getApplicationObjectUsed();
					if(objApplication==null) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Event-Objekt erhalten.";
						System.out.println(sLog);
						this.getClientBackendObject().logMessageString(sLog);
						break main;
					}else {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Event-Objekt erhalten.";
						System.out.println(sLog);
						this.getClientBackendObject().logMessageString(sLog);
						
					}
					
					String sVpnIp = objApplication.getVpnIpRemote();
					int iId = eventStatusLocalSet.getID();
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # fuer Event mit der ID" + (iId) + " - Verbunden mit remote VPNIP='"+sVpnIp+"'";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);
					
					//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt übergben.
					//objStarter2.getMainObject().getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);								
					
					//TODOGOON20231007;							
					//Cooler wäre tatsächlich alles über den Status des Main - objekts zu erledigen
					//Momentan wird der nur abgefragt um die Schleife zu verlassen...., oder?
					
					//Einen Event werfen, der dann das Icon im Menue-Tray aendert, etc....
					//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
					//Dann erzeuge den Event und feuer ihn ab.
					//Merke: Nun aber ueber das enum								
					if(this.getSenderStatusLocalUsed()!=null) {								
						
						//Im Main den Status setzen. Das ist ggfs. eine Abbruchbedingung fuer diese Schleife.
						//boolean bStatusLocalIsConnectedExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
												
						//Besser als den Wert direkt zu setzen, ist es einen Event zu feuern, auf den das Main hoert.
						boolean bStatusLocalIsConnectedExists = this.setStatusLocal(ClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED, true);//Es wird ein Event gefeuert, an dem das Backend-Objekt registriert wird und dann sich passend einstellen kann.
						
						
					}else {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN StatusSender-Objekt (objectBroker) vorhanden. Ggfs. kein anderes Objekt fuer das Hoeren auf Events hier registriert.";
						System.out.println(sLog);
						this.getClientBackendObject().logMessageString(sLog);	
					}
					
					
					//+++++++++++++++++++++
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status '"+enumStatus.getAbbreviation()+"' nicht weiter behandelt";
					System.out.println(sLog);
					this.getClientBackendObject().logMessageString(sLog);	
				}
				
				
				
//TODOGOON20231011;//Folgender Code stammt aus der run() Methode und wird nun vom Hoeren auf einen Event, den der ProcessWatchRunner feuert abgelöst.
				//Es muss nach bHasConnection der Tray per Event informiert werden, quasi so als Staffel...
				
//				//############################################################################### 					
//				//TODO: Timeout fuer threads programmieren, die sich einfach nicht beenden wollen.
//				//TODO GOON: Die Thread-Objekte und das Monitor-Objekt in die Config-Starter-Klasse integrieren.
//				ArrayList listaIntegerFinished = new ArrayList();
//				boolean[] baRunnerOVPNEndedMessage = new boolean[listaStarter.size()];  //Hier werden die beendeten Procese vermerkt.					
//				for(int icount2=0; icount2 < listaStarter.size(); icount2++){     
//					baRunnerOVPNEndedMessage[icount2]=false;
//				}
//
// 				//#########################################################
//				//+++ Monitoren der Threads, die versuchen per Batch und cmd.exe eine Verbindung aufzubauen.
//				//ENDLOSSCHLEIFE: Die Thread, die die Batch starten laufen noch!! Diese beobachten.             
//				//Merke: Wird der Thread (cmd.exe) per Task Manager geschlossen, bekommt das der Monitor-Thread nicht mit.			
//				long lThreadSleepTime=5000;
//				do{		
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Schleife zum Monitoren der ProcessWatchRunner-Threads.";
//					System.out.println(sLog);
//					this.objMain.logMessageString(sLog);
//					
//					//A) Beobachten der Threads, mit denen OVPN-gestartet werden soll						 						    
//					for(int icount2 = 0; icount2 < runneraOVPN.length; icount2++){
//						sLog = ReflectCodeZZZ.getPositionCurrent()+": Monitore Runner als Thread # " + (icount2+1) + ".";
//						System.out.println(sLog);
//						this.objMain.logMessageString(sLog);
//						
//						
//						ProcessWatchRunnerOVPN runnerOVPN = runneraOVPN[icount2];
//						if(runnerOVPN == null){
//							if(baRunnerOVPNEndedMessage[icount2] !=false){ //Ziel: Ungueltigen Output vermeiden
//								//+++ Die Runner, die beendet worden sind und einen Fehler zurueckgemeldet haben vermerken. Die brauchen dann ja nicht mehr angepingt zu werden.								
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Runner # " + (icount2+1) + " was set to  null.";
//								System.out.println(sLog);
//								this.objMain.logMessageString(sLog);								
//								baRunnerOVPNEndedMessage[icount2] = true;
//							}
//						}else{							
//							boolean bHasError = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
//							boolean bEnded = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
//							boolean bHasConnection = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
//							bHasConnection=true;//DEBUGCODE
//							
//							sLog = ReflectCodeZZZ.getPositionCurrent()+": Status abgefragt von Thread # " + (icount2+1) + ".";
//							this.objMain.logMessageString(sLog);
//							System.out.println(sLog);
//							
//							//WICHTIG: Falls das Monitoren über den Statuswert nicht klappt, bleibt nur die Loesung über den Event, der ja vom ProcessWatch-Thread auch geworfen wird!!!
//							//ABER: Auch die Abfrage der Statuswerte klappt in einer Schleife... ist halt immer etwas zeitverzoegert.
//							sLog = ReflectCodeZZZ.getPositionCurrent()+": bHasConnection="+bHasConnection+"|bEnded="+bEnded+"|bHasError="+bHasError;
//							this.objMain.logMessageString(sLog);
//							System.out.println(sLog);
//
//							if(bHasError && bEnded){
//					 			
//								//+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aus der Liste der anzupingenden ips) herausnehmen, die auf einen Fehler gelaufen sind								
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread # " + (icount2+1) + " could not create a connection. Ending thread with ERROR reported. For more details look at the log file.";
//								System.out.println(sLog);
//								this.objMain.logMessageString(sLog);
//								
//								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//								if(objStarter2.isProcessAlive()==true) objStarter2.requestStop(); //Den Prozess beenden								
//														
//								threadaOVPN[icount2].interrupt();
//								runneraOVPN[icount2]=null;
//								
//								Integer intTemp = new Integer(icount2);
//								listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
//							}else if((!bHasError) && bEnded){
//					 			//+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aud der Liste der anzupingenden ips) herausnehmen, die einfach nur so beendet worden sind
//								//       Merke: Falls ein openvpn.exe die connection geschaft hat, wird dieser auf jeden Fall nicht beendet.
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread #" + (icount2+1) + " - could not create a connection. Ending thread. For more details look at the log file.";
//								System.out.println(sLog);
//								this.objMain.logMessageString(sLog);
//															
//								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//								if(objStarter2.isProcessAlive()==true) objStarter2.requestStop(); //Den Prozess beenden								
//														
//								threadaOVPN[icount2].interrupt();
//								runneraOVPN[icount2]=null;
//								
//								Integer intTemp = new Integer(icount2);
//								listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
//								
//							}else if(bHasConnection){
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread #" + (icount2+1) + " - has connection.";
//								System.out.println(sLog);
//								this.objMain.logMessageString(sLog);
//								
//								try {
//									Thread.sleep(lThreadSleepTime);
//								} catch (InterruptedException e) {
//									System.out.println("ClientMonitorRunnerThread: InterruptedExceptionError");
//									e.printStackTrace();
//								}
//								
//								//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
//								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//								String sVpnIp = objStarter2.getMainObject().getApplicationObject().getVpnIpRemote();
//								
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Thread #" + (icount2+1) + " - Verbunden mit remote VPNIP='"+sVpnIp+"'";
//								System.out.println(sLog);
//								this.objMain.logMessageString(sLog);
//								
//								//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt übergben.
//								objStarter2.getMainObject().getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);								
//								
//								//TODOGOON20231007;							
//								//Cooler wäre tatsächlich alles über den Status des Main - objekts zu erledigen
//								//Momentan wird der nur abgefragt um die Schleife zu verlassen...., oder?
//								
//								//Einen Event werfen, der dann das Icon im Menue-Tray aendert, etc....
//								//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
//								//Dann erzeuge den Event und feuer ihn ab.
//								//Merke: Nun aber ueber das enum								
//								if(this.getSenderStatusLocalUsed()!=null) {								
//									IEventObjectStatusLocalSetOVPN event = new EventObjectStatusLocalSetOVPN(this,1,ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
//									
//									//Der oben gesetze Wert für die VpnIpRemoteEstablished wird aber vom Tray nicht erkannt / kommt im Backend objekt dort nicht an, darum das Application-Objekt noch dem Event explizit uebergeben.									
//									IApplicationOVPN objApplication = objStarter2.getMainObject().getApplicationObject();
//									if(objApplication==null) {
//										sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN Application-Objekt aus dem Main-Objekt erhalten.";
//										System.out.println(sLog);
//										this.objMain.logMessageString(sLog);
//									}else {
//										sLog = ReflectCodeZZZ.getPositionCurrent()+": Application-Objekt aus dem Main-Objekt erhalten.";
//										System.out.println(sLog);
//										this.objMain.logMessageString(sLog);
//										
//										event.setApplicationObjectUsed(objApplication);
//									}
//									
//									//TODOGOON20231008;//Irgendwie wird dieser Event nicht gefeuert.
//									                 //Ausserdem braucht in dieser Klassen niemand das Interface: IEventBrokerStatusLocalSetUserOVPN
//									
//									this.getSenderStatusLocalUsed().fireEvent(event);
//									
//									//Im Main den Status setzen. Das ist ggfs. eine Abbruchbedingung fuer diese Schleife.
//									boolean bStatusLocalIsConnectedExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
//								}else {
//									sLog = ReflectCodeZZZ.getPositionCurrent()+": KEIN StatusSender-Objekt (objectBroker) vorhanden. Ggfs. kein anderes Objekt fuer das Hoeren auf Events hier registriert.";
//									System.out.println(sLog);
//									this.objMain.logMessageString(sLog);	
//								}
//								                 
//								
//																																										
//							}else{	 											
//								try {
//									//Das blaeht das Log unnoetig auf .... 
//									this.objMain.logMessageString("Thread # " + (icount2+1) + " not jet ended or has reported an error.");
//									Thread.sleep(lThreadSleepTime);
//								} catch (InterruptedException e) {
//									System.out.println("ClientMonitorRunnerThread: InterruptedExceptionError");
//									e.printStackTrace();
//								}
//							}																												
//						}//END if (runnnerOVPN==null
//					}//END for
//						 		
//					
//					
//					//B) VORBEREITEN fuer das Pingen der gewuenschten Zieladressen hinsichtlich der Erreichbarkeit.	 								
//					//    Erstellen der Arraylist, die zu Pingen ist. D.h.  von den listaStarter die Positionen die in listaIntegerRemoved drinstehen abziehen.
//				
//					//Verwende nicht das File-Objekt, sondern das Konfigurations-Objekt.
//					ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = new ArrayList<ClientConfigStarterOVPN>();
//					for(int icount3=0; icount3 < listaStarter.size(); icount3++){	
//						//Diejenigen Starter, die schon vorzeitig beendet wurden hier herausfiltern. Den Rest anpingen
//						Integer intTemp = new Integer(icount3);
//						if(!listaIntegerFinished.contains(intTemp)){											
//							listaClientConfigStarterRunning.add(listaStarter.get(icount3));
//						}//END if
//					}//END For
//					
// 					//Diese Liste ist für das Scannen der IP wichtig. Es ist die Liste der "noch übrigen"/"erfolgreichen" Verbindungen.
//					//Diese Liste in das Main-Objekt wegsichern... Nun kann der Monitor der VpnIp - Verbindung auf die Details zugreifen, z.B. VpnIp-Adresse. 						
//					this.objMain.setClientConfigStarterRunningList(listaClientConfigStarterRunning);
//					
//					//Damit nach einem Verbindungsaufbau dieser Thread beendet wird und nicht ewig weiterlaeuft.
//					//Im Main Objekt wurde extra der Status vor dem Feuern des Events gesetzt
//					//Per Flag wird gesteuert, ob eine erfolgreiche Verbindung zum Beenden des Monitors fuehrt.																	
//					boolean bConnected = this.objMain.getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED);
//					if(bConnected) {
//						if(this.getFlag(IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION)) {
//							sLog = ReflectCodeZZZ.getPositionCurrent()+": Verbindung hergestellt und Flag gesetzt: '" + IClientThreadProcessWatchMonitorOVPN.FLAGZ.END_ON_CONNECTION.name() + "' beende den Monitor.";
//							System.out.println(sLog);
//							this.objMain.logMessageString(sLog);
//							break main;						
//						}
//					}
//				}while(true);				 						 					
				//##################################
				
			}//end main:			
			return bReturn;
		}

		//############ ZZZ-EVENTS noch nicht verwendet oder definiert		
		@Override
		public boolean isStatusLocalRelevant(IEnumSetMappedZZZ objEnumStatusIn) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
				if(!(objEnumStatusIn instanceof IProcessWatchRunnerOVPN.STATUSLOCAL) ){
					String sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus wird wg. unpassender Klasse ignoriert.";
					System.out.println(sLog);
					this.objMain.logMessageString(sLog);
					break main;
				}		
				bReturn = true;

			}//end main:
			return bReturn;
		}		
}//END class
