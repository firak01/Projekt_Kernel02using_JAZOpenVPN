package use.openvpn.client.process;

import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.AbstractObjectWithStatusOnStatusListeningZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.component.IModuleUserZZZ;
import basic.zBasic.component.IModuleZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.component.IProgramZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusOnStatusListeningZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import basic.zKernel.status.ISenderObjectStatusBasicZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalZZZ;
import basic.zKernel.status.SenderObjectStatusLocalZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.status.EventObject4VpnIpPingerStatusLocalOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalUserOVPN;
import use.openvpn.client.status.IEventObject4VpnIpPingerStatusLocalOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalOVPN;

public class ClientThreadVpnIpPingerOVPN_BACKUP extends AbstractKernelUseObjectWithStatusOnStatusListeningZZZ implements IClientThreadVpnIpPingerOVPN{ //AbstractKernelUseObjectWithStatusZZZ implements IClientThreadVpnIpPingerOVPN, IListenerObjectStatusLocalOVPN, IEventBrokerStatusLocalUserOVPN{
	private static final long serialVersionUID = 4598201201165618817L;
	protected volatile IModuleZZZ objModule = null;
	protected volatile String sModuleName=null;
	protected volatile String sProgramName = null;
	
	
	private IClientMainOVPN objMain = null;
	private ISenderObjectStatusLocalOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	public ClientThreadVpnIpPingerOVPN_BACKUP(IKernelZZZ objKernel, ClientMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		PingerNew_(objConfig, saFlagControl);
	}
	
	private void PingerNew_(ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
		main:{
			
			check:{
		 		
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
									
				this.objMain = objMain;
				
				//Da dies ein KernelProgram ist automatisch das FLAG IKERNELMODULE Setzen!!!				
				this.setFlag(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
			}//End check
	
		}//END main
	}
	
	public boolean isWaitingForClientStart() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		boolean bProof = this.proofFlagSetBefore(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTSTART.name());
		if(bProof) {
			bReturn = this.getFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTSTART.name());
		}else {
			//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht.
			String sModuleAlias = this.getModuleName();
			String stemp = this.objKernel.getParameterByModuleAlias(sModuleAlias, "WaitForClientStart").getValue();
			if(stemp==null) break main;
			if(stemp.equals("1")){
				bReturn = true;
			}
			this.setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTSTART, bReturn);
		}//end if
		}//END main
		return bReturn;
	}	
	
	public boolean isWaitingForClientConnect() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		boolean bProof = this.proofFlagSetBefore(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTCONNECT.name());
		if(bProof) {
			bReturn = this.getFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTCONNECT.name());
		}else {
			//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enthaelt, die wie der Application - Key aussieht.
			String sModuleAlias = this.getModuleName();
			String stemp = this.objKernel.getParameterByModuleAlias(sModuleAlias,"WaitForClientConnect").getValue();
			if(stemp==null) break main;
			if(stemp.equals("1")){
				bReturn = true;
			}
			this.setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTCONNECT, bReturn);
		}//end if
		}//END main
		return bReturn;
	}	
	
	//###
		public void run() {
			try {
				this.startAsThread();
			} catch (ExceptionZZZ ez) {
				try {
					this.logLineDate(ez.getDetailAllLast());
				} catch (ExceptionZZZ e1) {
					System.out.println(e1.getDetailAllLast());
					e1.printStackTrace();
				}		
			}
		}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public boolean start() throws ExceptionZZZ {
		boolean bReturn = false;
		try {
			main:{							
					if(this.objMain==null) break main;
									   	
					//######### 20230826 Verschoben aus ClientMainOVPN.start(), durch das Aufteilen sind mehrere Prozesse parallel moeglich.					
	 				//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
	 				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Trying to establish a new connection with every OVPN-configuration-file. Starting threads.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);	
	 				
					//Setze ggfs. vorher gesetzte Werte zurück.
					this.reset();
					
					//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.
					//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.
					//this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISPINGSTARTING, true);								
					//besser ueber eine geworfenen Event... und nicht direkt: this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
					//boolean bStatusLocalSet = this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING, true);
					boolean bStatusLocalSet = this.switchStatusLocalAllGroupTo(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING, true); //Damit der ISSTOPPED Wert auf jeden Fall auch beseitigt wird
					if(!bStatusLocalSet) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Lokaler Status nicht gesetzt, aus Gruenden. Breche ab";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);
						break main;
					}
					
					do {			 								
						//B) Pingen der gewuenschten Zieladressen hinsichtlich der Erreichbarkeit VORBEREITEN	 													
						//Verwende nicht das File-Objekt, sondern das Konfigurations-Objekt.
						ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = this.getMainObject().getClientConfigStarterList();
						if(listaClientConfigStarterRunning==null) {
							sLog = ReflectCodeZZZ.getPositionCurrent()+": PING: Keine Konfigurationen aus OVPN-configuration-file vorhanden (NULL). Breche ab.";
							System.out.println(sLog);
							this.getMainObject().logProtocolString(sLog);
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTED, true);
							break main;
						}else if(listaClientConfigStarterRunning.size()==0) {
							sLog = ReflectCodeZZZ.getPositionCurrent()+": PING: Konfigurationen aus OVPN-configuration-file vorhanden (0). Breche ab.";
							System.out.println(sLog);
							this.getMainObject().logProtocolString(sLog);
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTED, true);
							break main;							
						}
						
						Thread.sleep(5000);							
						System.out.println("... PING: Starte fuer alle Konfigurationen...");					
						this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTED, true);
						
						int iNumberOfPingsStarted = 0;
						for(int icount3=0; icount3 < listaClientConfigStarterRunning.size(); icount3++){
							
							Thread.sleep(5000);							
							System.out.println("..."+(icount3+1)+". PING: Mache neue Verbindung...");							
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTING, true);
																					
			 				IApplicationOVPN objApplication = this.getMainObject().getApplicationObject();
	
//			 				ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN)listaStarter.get(icount);
			 				//Process objProcess = objStarter.requestStart();
			 				
			 				//Idee20231119, mache entsprechend: KernelPingHostZZZ objPing = objStarter.requestPing();
			 					
			 					
			 				Thread.sleep(10000);
							String sVpnIp = objApplication.getVpnIpRemote();
							String sVpnPort = objApplication.getVpnPortRemote(); //Es gibt im OVPN-Server einen Listener, der auf einen Port ("5000") hört. Wenn die VPN Verbindung steht, wird dazu die Verbindung klappen;
							KernelPingHostZZZ objPing = new KernelPingHostZZZ(this.getKernelObject(),null);
							iNumberOfPingsStarted++;
							boolean bConnected = objPing.ping(sVpnIp, sVpnPort);
							if(bConnected) {																				
								System.out.println("..."+(icount3+1)+". PING: Verbunden");							
								this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTED, true);
																							
							}else {
								System.out.println("..."+(icount3+1)+". PING: Nicht verbunden");							
								this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTNO, true);															
							}
							
							Thread.sleep(5000);							
							System.out.println("..."+(icount3+1)+". PING: Beende...");	
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, true);
						}//END For

						Thread.sleep(1000);
						break;
					}while(true);
					
			
			//##################################

	 					//### Aus clientMonitorRunner... vom Ende
	 						//########################################################################
	 						//TODOGOON20230827;//C) Das eigentliche Pingen mit dem OVPNConnectionWatchRunnerZZZ in einen eigenen Menüpunkt unterbringen.
	 		 								//also was steht unter 
	 		 								//bConnected = objMain.getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED);
	 		 								//if(bConnected){											
	 							            //darin das Pruefen auf Erreichbarkeit einbauen...
	 		 								
//	 						//Das Pr fen auf Erreichbarkeit
//	 						//In this.scanVPNIPFirstEstablished wird schon eine Schleife durchgef hrt......     for(int icount3=0; icount3 < listaFileNotFinished.size(); icount3++){	
//	 							//1. Diese ArrayList der StarterObjecte nun hinsichtlich der VPN-IP-Erreichbarkeit scannen.
//	 							this.objMain.logMessageString("Checking success. Pinging all not jet finished configurations for the configured vpn-ip."); //DEN PORT ZU PINGEN IST QUATSCH  + ":" + objStarter.getVpnPort();					
//	 							String sIP = this.scanVpnIpFirstEstablished(listaFileNotFinished);
//	 							
//	 							//2.Falls eine der konfigurierten Adressen erreichbar ist: Flag "Connected" setzen. Alle anderen Processe zum Verbindungsaufbau stoppen.
//	 							//TODO: Sollen alle Verbindungen aufgebaut werden, dann lediglich aus der Liste herausnehmen. Nat rlich daf r sorgen, dass das Frontend  ber die neue VPN M glichkeit informiert wird. 
//	 							if(sIP!=null){
//	 								((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpEstablished(sIP);  //Wichtig: Die erreichbare IP - Adresse f r das Frontend greifbar machen.
//	 								//this.sPortVPN = objStarter.getVpnPort();	
//	 								this.logMessageString( "Connection successfully established with '"+ ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished() +"'"); //Der Port ist nicht aussagekr ftig !!! + ":" + this.getVpnPortEstablished() + "'";)					
//	 								bReturn = true;					
//	 								this.setFlag("isconnected", bReturn);  //DAS SOLL DANN z.B: dem Frontend sagen, dass die Verbindung steht.
//	 								
//	 								
//	 								//Nun diejenigen Threads beenden, die ungleich der Gefundenen Konfigurationsdatei sind
//	 								ClientConfigFileZZZ objFileConfig = this.getFileConfigReached();
//	 								if(objFileConfig!=null){
//	 									File objFile = objFileConfig.getFileConfig();
//	 									String sPath = objFile.getPath();
//	 									
//	 									//Alle noch nicht beendeten Prozesse beenden, AUSSER dort ist die gefundene Konfiguration verwendet.
//	 									for(int icount2=0;icount2<runneraOVPN.length; icount2++){
//	 										Integer intTemp = new Integer(icount2);
//	 										if(!listaIntegerFinished.contains(intTemp)){
//	 											
//	 											ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN) listaStarter.get(icount2);
//	 											File objFileStarter = objStarter.getFileConfigOvpn();
//	 											String sPathStarter = objFileStarter.getPath();
//	 											if(sPath.equalsIgnoreCase(sPathStarter) == false){											
//	 												this.logMessageString("Requesting to end thread # " + icount2);							
//	 													
//	 												//TODO GOON: VERSUCHE DEN STEUERCODE F R "BEENDEN" ZU SENDEN, DAS GEHT ABER NOCH NICHT
//	 												runneraOVPN[icount2].sendStringToProcess("hard");   //???????
//	 												runneraOVPN[icount2].setFlag("StopRequested", true); // DAMIT WIRD DAS RUNNER OBJEKT ANGEHALTEN SICH SELBST ZU BEENDEN
//	 																																		
//	 												objStarter.requestStop(); //!!! TODO GOON: Ich schaffe es nicht den Process zu beenden !!! Darum werden z.B. beim Beenden des Frontend-Clients alle prozesse desn Namens "openvpn.exe" beendet. 
//	 												threadaOVPN[icount2].interrupt();
//	 												//runneraOVPN[icount2]=null;
//	 													
//	 												listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
//	 											}//END 	if(sPath.equalsIgnoreCase(sPathStarter) == false){
//	 										}//END if
//	 									}//End for
//	 								}		
//	 								//5. Endlosschleife erfolgreich verlassen. Nicht main: verlassen, da ist noch viel zu tun !!!							
//	 								break connmain;
//	 							}else{
//	 								String stemp = "";
//	 								for(int icount2=0; icount2 < listaFileNotFinished.size(); icount2++){
//	 									File objFileConfig = (File) listaFileNotFinished.get(icount2);
//	 									if(stemp.equals("")){
//	 										stemp = objFileConfig.getPath();
//	 									}else{
//	 										stemp = stemp + "; " + objFileConfig.getPath();
//	 									}
//	 								}								
//	 								this.logMessageString( "No connection estblished till now based upon the file(s) '"+ stemp +"'");																				
//	 							}//END if sIP != null																
	 					//}//END for
	 					
	 								/*TODO WARUM GEHT DAS NICHT. Kl ren, ob Firewall oder Proxy-Einstellungen bei mir oder bei der itelligence das verhindern !!!
	 								//+++ Vorab: Checken, ob die Remote-Verbindungen erreichbar sind !!!
	 								//       Die Konfigurationen, die nicht erreichbar sind, hier schon entfernen !!!
	 								sStatus = "Checking if the remote connection connection is available.";
	 								objKernel.getLogObject().WriteLineDate(sStatus);				
	 								this.addStatusString(sStatus);
	 								
	 								ArrayList listaPos = new ArrayList(); //Hier werden die zu entfernenden ArrayList-Konfigurations-Positionen eingetragen
	 								for(int icount = 0; icount < listaFileUsed.size(); icount++){
	 									File objFileConfig2start = (File) listaFileUsed.get(icount);
	 									ConfigStarterZZZ objStarter = new ConfigStarterZZZ(objKernel, objFileConfig2start, null);
	 									
	 									//Falls die Remote-Verbindung nicht aufzubauen ist, hieraus entfernen.
	 									boolean bReachable = objStarter.isRemoteReachable();
	 									if(bReachable==true){
	 										sStatus = "Remote connection availabe. '"+ objStarter.getRemoteIp() + ":" + objStarter.getRemotePort() + "'";
	 										objKernel.getLogObject().WriteLineDate(sStatus);				
	 										this.addStatusString(sStatus);
	 										//Keine weitere Konsequenz
	 									}else{
	 										sStatus = "Remote connection NOT availabe. Not starting this configuration. '"+ objStarter.getRemoteIp() + ":" + objStarter.getRemotePort() + "'";
	 										objKernel.getLogObject().WriteLineDate(sStatus);				
	 										this.addStatusString(sStatus);
	 										
	 										//DIESE KONFIGURATION DEMN CHST ENTFERNEN
	 										listaPos.add(new Integer(icount));					
	 									}
	 								}//END for
	 								
	 								//TODO DAS ENTFERNEN AUS DER ARRAYLIST ALS KERNEL-STATIC-METHODE ANBIETEN !!!
	 								//NUN DAS TATS CHLICHE ENTFERNEN, Von hinten nach vorne und dann immer um 1 abnehmend.
	 								if(listaPos.isEmpty()==false){
	 									for(int icount=listaPos.size()-1;icount >=0;icount--){
	 									Integer inttemp = (Integer)listaPos.get(icount);
	 									int itemp = inttemp.intValue();
	 									listaFileUsed.remove(itemp);
	 									}//END for
	 								}
	 								*/
	 								
//######## 
//Aus ClientMainOVPN vom Ende				
	 								
//	 								///##########################################################
//	 								//Sollen die Ports der Gegenseite "gescannt" werden ?
//	 						if(this.isPortScanEnabled()==true){
//	 								
//	 								//Nun die zur Verf gung stehenden Ports erfassen	
//	 								//1. VPN-Ports
//	 								IKernelConfigSectionEntryZZZ entryPortLow=objKernel.getParameterByProgramAlias("OVPN","ProgPortScan","VPNPortLow");
//	 								String sPortLow=entryPortLow.getValue();
//	 								IKernelConfigSectionEntryZZZ entryPortHigh=objKernel.getParameterByProgramAlias("OVPN", "ProgPortScan", "VPNPortHigh");
//	 								String sPortHigh = entryPortHigh.getValue();
//	 								if(sPortLow!=null && sPortHigh != null){ 
//	 									//1. VPN-Ports
//	 									this.logMessageString( "Scanning ports on VPN-IP-Adress: " +  ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished());	
//	 									this.scanVpnPortAll(((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished(), sPortLow, sPortHigh);
//	 									this.logMessageString( "VPN-IP-Port scan finished.");	
//	 								}else{
//	 									this.logMessageString( "VPN-IP-Port scan not properly configured: Ports missing.");	
//	 								}
//	 								
//	 								
//	 							//		2. Remote-Ports
//	 									if(this.getFlag("useproxy")==true){
//	 										((ClientApplicationOVPN)this.getApplicationObject()).setRemotePortScanned("Proxy/Firewall make port scan obsolete.");
//	 									}else{
//	 										this.logMessageString( "Scanning ports on Remote-IP-Adress: " +  ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished());	
//	 										sPortLow=objKernel.getParameterByProgramAlias("OVPN","ProgPortScan","RemotePortLow").getValue();
//	 										sPortHigh=objKernel.getParameterByProgramAlias("OVPN", "ProgPortScan", "RemoteProtHigh").getValue();
//	 										if(sPortLow!=null && sPortHigh != null){  
//	 											this.scanRemotePortAll(((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished(), sPortLow, sPortHigh);				
//	 											this.logMessageString( "Remote-IP-Port scan finished.");	
//	 										}else{
//	 											this.logMessageString( "Remote-IP-Port scan not properly configured: Ports missing.");	
//	 										}//END if(sRemotePortLow!=null && sRemotePortHigh != null){  			
//	 									}//END if getFlag("useProxy")
//	 								
//	 							//########################################
//	 							//Merke: Das Frontend wird nun ggf. einen Thread starten, der die Verbindung  berwacht
//	 									this.setFlag("PortScanAllFinished", true);	
//	 								}//END if(this.isPortScanEnabled()){
	 					
	 			
	 									 
				 //}//END if "ConnectionRunnerStarted"
				 
//			
			
					
					//Damit werden die anderen Statuswerte nicht veraendert. 
					//Das hat Probleme beim Neustart  this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, true);
					Thread.sleep(5000);							
					System.out.println(".... PING: Stoppe");						
					this.switchStatusLocalAllGroupTo(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, true);
					
					bReturn = true;
											
			}//END main:
		} catch (InterruptedException e) {
			ExceptionZZZ ez2 = new ExceptionZZZ(e);
			throw ez2;
		}catch(ExceptionZZZ ez){
			String sLog;
			try {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": "+ez.getDetailAllLast();
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
					
				try {
					Thread.sleep(5000);
					System.out.println("... PING: Fehler...");	
					String sMessage = ez.getDetailAllLast();
					this.setStatusLocalError(sMessage);
					this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASERROR, true, sMessage);
					
					
					Thread.sleep(5000);							
					System.out.println(".... PING: Stoppe");						
					this.switchStatusLocalAllGroupTo(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, true);
				} catch (InterruptedException e) {
					ExceptionZZZ ez2 = new ExceptionZZZ(e);
					throw ez2;
				}		
				
			} catch (ExceptionZZZ e) {
				e.printStackTrace();
			}
		}
		return bReturn;
	}//END run
	
	
	@Override
	public void setMainObject(IClientMainOVPN objClientBackend){
		this.objMain = (ClientMainOVPN) objClientBackend;
	}
	
	@Override
	public IClientMainOVPN getMainObject(){
		return this.objMain;
	}
	
	@Override
	public boolean reset() {
		this.resetModuleUsed();
		this.resetStatusLocalError();
		return true;
	}
	
	
	
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
	 * @see AbstractKernelUseObjectZZZ.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
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

	//### aus IListenerObjectStatusLocalSetZZZ
	@Override
	public boolean changedStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{		
			//Falls nicht zuständig, mache nix
		    boolean bProof = this.isEventRelevant2ChangeStatusLocal(eventStatusLocalSet);
			if(!bProof) break main;
		    
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
			this.getMainObject().logProtocol(sLog);
			
			IEnumSetMappedZZZ enumStatus = (IEnumSetMappedZZZ) eventStatusLocalSet.getStatusEnum();				
			if(enumStatus==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
				System.out.println(sLog);
				this.getMainObject().logProtocol(sLog);							
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
	public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			//Merke: Beim Monitor interessieren auch "false" Werte, um den Status ggfs. wieder zuruecksetzen zu koennen
			//if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
			
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
			//Merke: enumStatus hat class='class use.openvpn.client.process.IClientThreadVpnIpPingerOVPN$STATUSLOCAL'				
			if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL){
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
	public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			IEnumSetMappedStatusZZZ enumStatus = (IEnumSetMappedStatusZZZ) eventStatusLocalSet.getStatusEnum();							
			bReturn = this.isStatusLocalRelevant(enumStatus);
			if(!bReturn) break main;
		
			String sAbr = eventStatusLocalSet.getStatusAbbreviation();
			if(!StringZZZ.startsWith(sAbr, "hasconnection")) break main;
			
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	
	//#######################################
	//### aus ISenderObjectStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
	//public ISenderObjectStatusLocalZZZ getSenderStatusLocalUsed() throws ExceptionZZZ {
		if(this.objEventStatusLocalBroker==null) {
			//++++++++++++++++++++++++++++++
			//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
			ISenderObjectStatusLocalOVPN objSenderStatusLocal = new SenderObjectStatusLocalOVPN();
			this.objEventStatusLocalBroker = objSenderStatusLocal;
		}
		return this.objEventStatusLocalBroker;
	}
	
	
//	@Override
//	public ISenderObjectStatusLocalZZZ getSenderStatusLocalUsed() throws ExceptionZZZ {
//		if(this.objEventStatusLocalBroker==null) {
//			//++++++++++++++++++++++++++++++
//			//Nun geht es darum den Sender/Broker fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung des Status zu informiert
//			ISenderObjectStatusLocalZZZ objSenderStatusLocal = new SenderObjectStatusLocalZZZ();			
//			this.objEventStatusLocalBroker = objSenderStatusLocal;
//		}		
//		return this.objEventStatusLocalBroker;
//	}

//	@Override
//	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender) {
//		this.objEventStatusLocalBroker = objEventSender;
//	}

	@Override
	public boolean getFlag(use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag) {
		return this.getFlag(objEnumFlag.name());
	}

	@Override
	public boolean setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag,	boolean bFlagValue) throws ExceptionZZZ {
		return this.setFlag(objEnumFlag.name(), bFlagValue);
	}

	@Override
	public boolean[] setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ[] objaEnumFlag,	boolean bFlagValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
				baReturn = new boolean[objaEnumFlag.length];
				int iCounter=-1;
				for(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
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
	public boolean proofFlagExists(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag)throws ExceptionZZZ {
		return this.proofFlagExists(objEnumFlag.name());
	}

	@Override
	public boolean proofFlagSetBefore(use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag)throws ExceptionZZZ {
		return this.proofFlagSetBefore(objEnumFlag.name());
	}

	//#############
	//Aus IStatusLocalUserBasicZZZ
	@Override 
	public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
			
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
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bFunction;
	}
	
	@Override 
	public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(enumStatusIn==null) {
				break main;
			}
			ClientThreadVpnIpPingerOVPN2.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
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
			ClientThreadVpnIpPingerOVPN2.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
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
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
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
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
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
			ClientThreadVpnIpPingerOVPN2.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
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
			ClientThreadVpnIpPingerOVPN2.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
		}//end main:
		return bReturn;
	}
	
	
	//+++++++++++++++++++++++++++++++++++
	@Override 
	public boolean offerStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) break main;
			
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bFunction;
	}
		
	@Override
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) break main;
			
			IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			
			bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);
		}//end main:
		return bFunction;				
	}
	
	
	//###############			
	private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(enumStatusIn==null) break main;
							
		    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
		    IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) enumStatusIn;
			String sStatusName = enumStatus.name();
			bFunction = this.proofStatusLocalExists(sStatusName);															
			if(!bFunction){
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger would like to fire event, but this status is not available: '" + sStatusName + "'";
				this.getMainObject().logProtocolString(sLog);			
				break main;
			}
			
			bFunction = this.proofStatusLocalValueChanged(sStatusName, bStatusValue);
			if(!bFunction) {
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger would like to fire event, but this status has not changed: '" + sStatusName + "'";
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
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger uebersteuere sStatusMessageToSet='" + sStatusMessage + "'";
				this.getMainObject().logProtocolString(sLog);
				
				sStatusMessageToSet = sStatusMessage;
			}
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
			this.getMainObject().logProtocolString(sLog);
			
			//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
			boolean bSuccess = this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);

			
			//....hier keine Verarbeitung der Startkonfiguration

			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.	
			if(this.getSenderStatusLocalUsed()==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
				this.getMainObject().logProtocolString(sLog);			
				break main;
			}
			
			//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
			sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";
			this.getMainObject().logProtocolString(sLog);				
			IEventObject4VpnIpPingerStatusLocalOVPN event = new EventObject4VpnIpPingerStatusLocalOVPN(this,1,enumStatus, bStatusValue);
			event.setApplicationObjectUsed(this.getMainObject().getApplicationObject());
			
			
			//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger for Process iIndex= '" + iIndexOfProcess + "'";
			this.getMainObject().logProtocolString(sLog);
			if(iIndexOfProcess>=0) {
				event.setClientConfigStarterObjectUsed(this.getMainObject().getClientConfigStarterList().get(iIndexOfProcess));
			}
			
			sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger fires event '" + enumStatus.getAbbreviation() + "'";
			this.getMainObject().logProtocolString(sLog);
			this.getSenderStatusLocalUsed().fireEvent(event);
				
			bFunction = true;	
		}	// end main:
		return bFunction;
	}

	//################################
	@Override
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
			if(!(objEnumStatusIn instanceof IClientThreadVpnIpPingerOVPN.STATUSLOCAL) ){
				String sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus wird wg. unpassender Klasse ignoriert.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
			}		
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
			ClientThreadVpnIpPingerOVPN2.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
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

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IEventBrokerStatusLocalUserOVPN#registerForStatusLocalEvent(use.openvpn.client.status.IListenerObjectStatusLocalOVPN)
	 */
	@Override
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed()).addListenerObjectStatusLocalSet(objEventListener);
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		((use.openvpn.server.status.ISenderObjectStatusLocalOVPN) this.getSenderStatusLocalUsed()).removeListenerObjectStatusLocalSet((use.openvpn.server.status.IListenerObjectStatusLocalOVPN) objEventListener);;
	}
	
	//### Aus IModuleZZZ
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
	public boolean getFlag(IKernelModuleZZZ.FLAGZ objEnumFlag) {
		return this.getFlag(objEnumFlag.name());
	}
	@Override
	public boolean setFlag(IKernelModuleZZZ.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
		return this.setFlag(objEnumFlag.name(), bFlagValue);
	}
	
	@Override
	public boolean[] setFlag(IKernelModuleZZZ.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
				baReturn = new boolean[objaEnumFlag.length];
				int iCounter=-1;
				for(IKernelModuleZZZ.FLAGZ objEnumFlag:objaEnumFlag) {
					iCounter++;
					boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
					baReturn[iCounter]=bReturn;
				}
			}
		}//end main:
		return baReturn;
	}
	
	@Override
	public boolean proofFlagExists(IKernelModuleZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagExists(objEnumFlag.name());
	}
	
	@Override
	public boolean proofFlagSetBefore(IKernelModuleZZZ.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagSetBefore(objEnumFlag.name());
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
				if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
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
				if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
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
				if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
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

		@Override
		public boolean startAsThread() throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean startCustom() throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		
		@Override
		public boolean queryOfferStatusLocalCustom() throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, String> createHashMapStatusLocal4ReactionCustom_String() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, String> getHashMapStatusLocal4Reaction_String() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setHashMapStatusLocal4Reaction_String(
				HashMap<IEnumSetMappedStatusZZZ, String> hmEnumSetForReaction) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> createHashMapStatusLocal4ReactionCustom_Enum() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> getHashMapStatusLocal4Reaction_Enum() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setHashMapStatusLocal4Reaction_Enum(
				HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> hmEnumSetForReaction) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> createHashMapStatusLocal4ReactionCustom_EnumStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> getHashMapStatusLocal4Reaction_EnumStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setHashMapStatusLocal4Reaction_EnumStatus(
				HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> hmEnumSetForReaction) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getActionAliasString(IEnumSetMappedStatusZZZ enumStatus) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean queryReactOnStatusLocalEvent(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean queryReactOnStatusLocalEventCustom(IEventObjectStatusLocalZZZ eventStatusLocal)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean reactOnStatusLocalEvent(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean queryReactOnStatusLocalEvent4Action(IEventObjectStatusLocalZZZ eventStatusLocal)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean reactOnStatusLocalEvent4Action(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean queryReactOnStatusLocal4Action(String sActionAlias, IEnumSetMappedStatusZZZ enumStatus,
				boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
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
		public boolean reactOnStatusLocal4Action(String sActionAlias, IEnumSetMappedStatusZZZ enumStatus,
				boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean reactOnStatusLocal4ActionCustom(String sActionAlias, IEnumSetMappedStatusZZZ enumStatus,
				boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevantAny(IEventObjectStatusLocalZZZ eventStatusLocal) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevant4ReactionOnStatusLocal(IEventObjectStatusLocalZZZ eventStatusLocal)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalZZZ eventStatusLocal)
				throws ExceptionZZZ {
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

		@Override
		public boolean getFlag(basic.zKernel.status.IListenerObjectStatusLocalZZZ.FLAGZ objEnumFlag) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean setFlag(basic.zKernel.status.IListenerObjectStatusLocalZZZ.FLAGZ objEnumFlag, boolean bFlagValue)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean[] setFlag(basic.zKernel.status.IListenerObjectStatusLocalZZZ.FLAGZ[] objaEnumFlag,
				boolean bFlagValue) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean proofFlagExists(basic.zKernel.status.IListenerObjectStatusLocalZZZ.FLAGZ objEnumFlag)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean proofFlagSetBefore(basic.zKernel.status.IListenerObjectStatusLocalZZZ.FLAGZ objEnumFlag)
				throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}
	
	
}//END class
