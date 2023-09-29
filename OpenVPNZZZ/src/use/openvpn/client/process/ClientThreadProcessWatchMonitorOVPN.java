package use.openvpn.client.process;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.status.EventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.clientui.ClientTrayStatusMappedValueZZZ;
import use.openvpn.clientui.ClientTrayUIZZZ;
import use.openvpn.client.status.ISenderObjectStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.server.ServerMainOVPN;

import use.openvpn.serverui.ServerTrayStatusMappedValueZZZ;
import use.openvpn.serverui.ServerTrayUIOVPN;
import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.process.AbstractProcessWatchRunnerZZZ;
import basic.zKernel.process.IProcessWatchRunnerZZZ;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ClientThreadProcessWatchMonitorOVPN extends KernelUseObjectZZZ implements Runnable,IListenerObjectStatusLocalSetOVPN, ISenderObjectStatusLocalSetUserOVPN{
	private ClientMainOVPN objMain = null;
	private ClientTrayUIZZZ objTray = null;
	
	private String sWatchRunnerStatus = new String("");            //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatusPrevious = new String("");    //den vorherigen Status festhalten, damit z.B. nicht immer wieder das Icon geholt wird.
	
//	private ConnectionWatchRunnerOVPN  objWatchRunner = null;
//	private Thread objWatchThread = null;
	
	protected ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
public ClientThreadProcessWatchMonitorOVPN(IKernelZZZ objKernel, ClientTrayUIZZZ objTray, ClientMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ConfigMonitorRunnerNew_(objTray, objConfig, saFlagControl);
}

private void ConfigMonitorRunnerNew_(ClientTrayUIZZZ objTray, ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
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
			this.objTray = objTray;
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
		   
			
//			boolean bConnected= false;			
								
				//######### 20230826 Verschoben aus ClientMainOVPN.start(), durch das Aufteilen sind mehrere Prozesse parallel moeglich.					
 				//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
 				this.objMain.logMessageString("Trying to establish a new connection with every OVPN-configuration-file. Starting threads.");
 			
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
 						//Hier nicht abbrechen, sondern die Verarbeitung bei der n�chsten Datei fortf�hren
 						this.objMain.logMessageString( "Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"' for thread #" + iNumberOfProcessStarted + " von " + listaStarter.size());
 					}else{	
 						
 						//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
 						 runneraOVPN[icount] =new ProcessWatchRunnerOVPN(objKernel, objProcess,iNumberOfProcessStarted, null);
 						 threadaOVPN[icount] = new Thread(runneraOVPN[icount]);					
 						 threadaOVPN[icount].start();	 						 
 						this.objMain.logMessageString("Finished starting thread #" + iNumberOfProcessStarted + " von " + listaStarter.size() + " for watching connection.");
 					}
 				}//END for	
 					
				
				boolean bStatusLocalExists = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.WATCHRUNNERSTARTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
				
				
				
				//############################################################################### 					
				//TODO: Timeout fuer threads programmieren, die sich einfach nicht beenden wollen.
				//TODO GOON: Die Thread-Objekte und das Monitor-Objekt in die Config-Starter-Klasse integrieren.
				ArrayList listaIntegerFinished = new ArrayList();
				boolean[] baRunnerOVPNEndedMessage = new boolean[listaStarter.size()];  //Hier werden die beendeten Procese vermerkt.					
				for(int icount2=0; icount2 < listaStarter.size(); icount2++){     
					baRunnerOVPNEndedMessage[icount2]=false;
				}

 				//#########################################################
				//+++ Monitoren der Threads, die versuchen per Batch und cmd.exe eine Verbindung aufzubauen.
				//ENDLOSSCHLEIFE: Die Thread, die die Batch starten laufen noch!! Diese beobachten.             
				//Merke: Wird der Thread (cmd.exe) per Task Manager geschlossen, so bekommt das der Monitor-Thread nicht mit.
				//       
				long lThreadSleepTime=5000;
				do{			 							
					//A) Beobachten der Threads, mit denen OVPN-gestartet werden soll						 						    
					for(int icount2 = 0; icount2 < runneraOVPN.length; icount2++){
						ProcessWatchRunnerOVPN runnerOVPN = runneraOVPN[icount2];
						if(runnerOVPN == null){
							if(baRunnerOVPNEndedMessage[icount2] !=false){ //Ziel: Unn�tigen Output vermeiden
								//+++ Die Runner, die beendet worden sind und einen Fehler zurueckgemeldet haben vermerken. Die brauchen dann ja nicht mehr angepingt zu werden.
								this.objMain.logMessageString("Runner # " + (icount2+1) + " was set to  null.");
								baRunnerOVPNEndedMessage[icount2] = true;
							}
						}else{							
							boolean bHasError = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
							boolean bEnded = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED);
							boolean bHasConnection = runnerOVPN.getStatusLocal(ProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
							if(bHasError && bEnded){
					 			
								//+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aud der Liste der anzupingenden ips) herausnehmen, die auf einen Fehler gelaufen sind
								this.objMain.logMessageString("Thread # " + (icount2+1) + " could not create a connection. Ending thread with ERROR reported. For more details look at the log file.");
					
								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
								if(objStarter2.isProcessAlive()==true) objStarter2.requestStop(); //Den Prozess beenden								
														
								threadaOVPN[icount2].interrupt();
								runneraOVPN[icount2]=null;
								
								Integer intTemp = new Integer(icount2);
								listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
							}else if((!bHasError) && bEnded){
					 			//+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aud der Liste der anzupingenden ips) herausnehmen, die einfach nur so beendet worden sind
								//       Merke: Falls ein openvpn.exe die connection geschaft hat, wird dieser auf jeden Fall nicht beendet.
								this.objMain.logMessageString("Thread # " + (icount2+1) + " could not create a connection. Ending thread. For more details look at the log file.");
								
								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
								if(objStarter2.isProcessAlive()==true) objStarter2.requestStop(); //Den Prozess beenden								
														
								threadaOVPN[icount2].interrupt();
								runneraOVPN[icount2]=null;
								
								Integer intTemp = new Integer(icount2);
								listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
								
							}else if(bHasConnection){
								this.objMain.logMessageString("Thread # " + (icount2+1) + " has connection.");
								try {
									Thread.sleep(lThreadSleepTime);
								} catch (InterruptedException e) {
									System.out.println("ClientMonitorRunnerThread: InterruptedExceptionError");
									e.printStackTrace();
								}
								
								//Wie an die sVpnIp rankommen.... Das geht über das ApplicationObjekt des erfolgreichen starters.								
								ClientConfigStarterOVPN objStarter2 = (ClientConfigStarterOVPN) listaStarter.get(icount2);
								String sVpnIp = objStarter2.getMainObject().getApplicationObject().getVpnIpRemote();
								
								String sLog = "Verbunden mit remote VPNIP='"+sVpnIp+"'";
								this.objMain.logMessageString("Thread # " + (icount2+1) + " " + sLog);;
								
								//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt übergben.
								objStarter2.getMainObject().getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);								
								//Dieser Wert wird aber vom Tray nicht erkannt / kommt im Backen objekt dort nicht an, darum das Application-Objekt noch dem Event explizit uebergeben.
								
								//Einen Event werfen, der dann das Icon im Menue-Tray aendert, etc....
								//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
								//Dann erzeuge den Event und feuer ihn ab.
								//Merke: Nun aber ueber das enum								
								if(this.getSenderStatusLocalUsed()!=null) {
									IEventObjectStatusLocalSetOVPN event = new EventObjectStatusLocalSetOVPN(this,1,ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
									event.setApplicationObjectUsed(objStarter2.getMainObject().getApplicationObject());
									this.getSenderStatusLocalUsed().fireEvent(event);
								}		 
								
							}else{	 											
								try {
									//Das blaeht das Log unnoetig auf .... 
									this.objMain.logMessageString("Thread # " + (icount2+1) + " not jet ended or has reported an error.");
									Thread.sleep(lThreadSleepTime);
								} catch (InterruptedException e) {
									System.out.println("ClientMonitorRunnerThread: InterruptedExceptionError");
									e.printStackTrace();
								}
							}
						}//END if (runnnerOVPN==null
					}//END for
						 								
					//B) VORBEREITEN fuer das Pingen der gewuenschten Zieladressen hinsichtlich der Erreichbarkeit.	 								
					//    Erstellen der Arraylist, die zu Pingen ist. D.h.  von den listaStarter die Positionen die in listaIntegerRemoved drinstehen abziehen.
				
					//Verwende nicht das File-Objekt, sondern das Konfigurations-Objekt.
					ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = new ArrayList<ClientConfigStarterOVPN>();
					for(int icount3=0; icount3 < listaStarter.size(); icount3++){	
						//Diejenigen Starter, die schon vorzeitig beendet wurden hier herausfiltern. Den Rest anpingen
						Integer intTemp = new Integer(icount3);
						if(!listaIntegerFinished.contains(intTemp)){											
							listaClientConfigStarterRunning.add(listaStarter.get(icount3));
						}//END if
					}//END For
					
 					//Diese Liste ist für das Scannen der IP wichtig. Es ist die Liste der "noch übrigen"/"erfolgreichen" Verbindungen.
					//Diese Liste in das Main-Objekt wegsichern... Nun kann der Monitor der VpnIp - Verbindung auf die Details zugreifen, z.B. VpnIp-Adresse. 						
					this.objMain.setClientConfigStarterRunningList(listaClientConfigStarterRunning);	 									 								
				}while(true);				 						 					
				//##################################
 					
		}catch(ExceptionZZZ ez){
			System.out.println(ez.getDetailAllLast());
		}
	}//END main:
	
	}//END run
	
	
	public String getStatusString(){
		return this.sWatchRunnerStatus;
	}
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
	
	public String getStatusPreviousString() {
		return this.sWatchRunnerStatusPrevious;
	}
	public void setStatusPrevious(String sStatusPrevious) {
		this.sWatchRunnerStatusPrevious = sStatusPrevious;
	}
	
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

	//###### FLAGS
	/* (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- connectionrunnerstarted	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
		
			//getting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("connectionrunnerstarted")){
//				bFunction = bFlagConnectionRunnerStarted;
//				break main;
//			}		
		}//end main:
		return bFunction;
	}

	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 	- ConnectionRunnerStarted.
	 * @throws ExceptionZZZ 
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
		boolean bFunction = false;
		main:{			
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
			if(bFunction==true) break main;
			
			//setting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("connectionrunnerstarted")){
//				bFlagConnectionRunnerStarted = bFlagValue;
//				bFunction = true;
//				break main;
//	
//			}
		}//end main:
		return bFunction;
	}

	//aus IListenerObjectStatusLocalSetZZZ
	@Override
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Lies den Status (geworfen vom Backend aus)
		String sStatus = eventStatusLocalSet.getStatusText();
		
		//übernimm den Status
		this.setStatusString(sStatus);
		
		return true;
	}

	//### aus ISenderObjectStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
		return this.objMain.getSenderStatusLocalUsed();
	}

	@Override
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
		this.objMain.setSenderStatusLocalUsed(objEventSender);
	}
}//END class
