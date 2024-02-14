package use.openvpn.clientui.component.tray;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.jdesktop.jdic.tray.TrayIcon;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernelUI.component.tray.AbstractKernelActionTrayZZZ;
import use.openvpn.ITrayOVPN;
import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.clientui.component.IPExternalRead.DlgIPExternalOVPN;
import use.openvpn.clientui.component.tray.IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ;
import use.openvpn.clientui.component.tray.IClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ;
import use.openvpn.component.shared.adjustment.DlgAdjustmentOVPN;

/** Der Icon unter Windows in der TaskLeiste.
 *  Aus ihm heraus werden:
 *  - ueber ein wechselndes Icon der aktuelle Status angezeigt.
 *  - die einzelnen Schritte gestartet
 *  - der Status im Detail angezeigt
 *  
 *  Merke: 
 *  Dieser Tray ist an den verschiedenen Monitor-Objekten, die den LocalStatus nutzen registriert.
 *  Er reagiert auf Events, die er empfaengt.
 *  Selbst hat der ClientTray keinen LocalStatus und feuert daher auch keine Events ab.
 *  
 * @author Fritz Lindhauer, 11.10.2023, 07:46:15
 * 
 */
public class ActionClientTrayUIOVPN extends AbstractKernelActionTrayZZZ {
	private static final long serialVersionUID = 1004331678604454588L;

	public ActionClientTrayUIOVPN(IKernelZZZ objKernel, ITrayOVPN objTrayParent) throws ExceptionZZZ{
		super(objKernel, objTrayParent);
		ActionClientTrayUINew_();
	}
		
	private boolean ActionClientTrayUINew_() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			
			bReturn = true;
		}//END main
		return bReturn;
	}
	
	
	
	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(this.getMainObject()==null)break main;

			//Wenn das so ohne Thread gestartet wird, dann reagiert der Tray auf keine weiteren Clicks.
			//Z.B. den Status anzuzeigen.			
			//Den Staart ueber einen extra Thread durchfuehren, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!			
			Thread objThreadMain = new Thread(this.getMainObject());
			objThreadMain.start();
			
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	/**Removes the icon from the systemtray
	 * AND removes any running "openvpn.exe" processes. (or how the exe-file is named)
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 13:05:25
	 * @throws ExceptionZZZ 
	 */
	public boolean unload() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{		
			bReturn = this.getTrayParent().unload();
		}
		return bReturn;
	}
					
	public boolean connect(){
		boolean bReturn = false;
		main:{
			try{ 
				if(this.getMainObject()==null)break main;
								
				ClientThreadProcessWatchMonitorOVPN objMonitor = this.getMainObject().getProcessMonitorObject();
				if(objMonitor==null) break main;
				
				boolean bStarted = this.getMainObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED);
				if(!bStarted) {
					
					boolean bStarting = this.getMainObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bStarting) {	
						objMonitor.offerStatusLocalEnum(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASCLIENTNOTSTARTING);						
						break main;
					}
					
					objMonitor.offerStatusLocalEnum(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASCLIENTNOTSTARTED);					
					break main;
				}
									
				Thread objThreadMonitorThread = new Thread(objMonitor);
				objThreadMonitorThread.start();							
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.
				
				bReturn = true;
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.getTrayParent().switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ ez2) {
					System.out.println(ez.getDetailAllLast());
					ez2.printStackTrace();					
				}
			}		
		}
		return bReturn;		
	}
	
	/** Hiermit wird der Prozess gestartet, der die erfolgreiche Verbindung permanent mit einem PING ueberwacht.
	 * @return
	 * @author Fritz Lindhauer, 29.09.2023, 17:09:11
	 */
	public boolean ping(){
		boolean bReturn = false;
		main:{
			try{ 
				check:{
					if(this.getMainObject()==null)break main;
				}
				
				//Fuer einen zweiten moelichen Neustart das VpnIpPingerObjekt erst einmal beseitigen.
				boolean bResetted = this.getMainObject().resetVpnIpPingerObject();
			
				ClientThreadVpnIpPingerOVPN objPinger = this.getMainObject().getVpnIpPingerObject();
				if(objPinger==null) break main;
				
				boolean bWaitForStart = objPinger.isWaitingForClientStart(); //objPinger.getFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTSTART);
				if(bWaitForStart) {				
					boolean bMainStarting = this.getMainObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bMainStarting) {					
						objPinger.offerStatusLocalEnum(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTING);
						break main;
					}
					
					boolean bMainStarted = this.getMainObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISSTARTED);
					if(!bMainStarted) {
						objPinger.offerStatusLocalEnum(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTSTARTED);
						break main;
					}
					
						boolean bWaitForConnect = objPinger.isWaitingForClientConnect(); //objPinger.getFlag(IClientThreadVpnIpPingerOVPN.FLAGZ.WAIT_FOR_CLIENTCONNECT);
						if(bWaitForConnect) {
						boolean bMainConnected = this.getMainObject().getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED) ;
						if(!bMainConnected) {
							objPinger.offerStatusLocalEnum(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.HASCLIENTNOTCONNECTED);
							break main;
						}
					} //end if bWaitForConnect								
				}//end if bWaitForStart
				
				Thread objThreadPingerThread = new Thread(objPinger);
				objThreadPingerThread.start();							
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISPINGED" gesetzt und ein Event geworfen.			
				bReturn = true;	
		
								
				
				
				
				//TODOGOON20230929;
				
				//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.
				//this.objServerBackend = (ServerMainZZZ) this.getServerBackendObject().getApplicationObject(); //new ServerMainZZZ(this.getKernelObject(), null);
				
				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Thread objThreadConfig = new Thread(this.getServerBackendObject());
				//objThreadConfig.start();

				//DIES über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
				//Merke: Es ist nun Aufgabe des Frontends einen Thread zu starten, der den Verbindungsaufbau und das "aktiv sein" der Processe monitored.
				//Merke: Dieser Monitor Thread muss mit dem Starten der einzelnen Unterthreads solange warten, bis das ServerMainZZZ-Object in seinem Flag anzeigt, dass es fertig mit dem Start ist.
				
				//Merke: Der MonitorThread wurde schon beim Verbinden gestartet. ... Diesen weiternutzen.
				//Thread objThreadMonitor = new Thread(objMonitor);
				//objThreadMonitor.start();
				
				//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
				//this.objClientBackend.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
					
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.
				
				//#############
				
				
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
				
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.getTrayParent().switchStatus(ClientTrayStatusTypeZZZ.ERROR);
				} catch (ExceptionZZZ ez2) {
					System.out.println(ez.getDetailAllLast());
					ez2.printStackTrace();					
				}
			}		
		}
		return bReturn;		
	}
	
	/** Reads a status string from the Backend-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readBackendStatusString(){
		String sReturn = "";
		if(this.getMainObject()!=null){
			sReturn = this.getMainObject().getStatusLocalAbbreviation();
		}else {	
			sReturn = ClientMainOVPN.STATUSLOCAL.ISSTARTNEW.getAbbreviation();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
		}
		return sReturn;
	}
	
	/** Reads a status string from the Backend-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readBackendStatusMessage(){
		String sReturn = "";
		if(this.getMainObject()!=null){
			sReturn = this.getMainObject().getStatusLocalMessage();
		}else {	
			sReturn = ClientMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
		}
		return sReturn;
	}
	
	
	
	
	/** Reads a status string from the ProcessMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readProcessMonitorStatusString(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getProcessMonitorObject()==null) break main;
			
			sReturn = this.getMainObject().getProcessMonitorObject().getStatusLocalAbbreviation();
		}//end main:
		return sReturn;
	}
	
	/** Reads a status string from the ProcessMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readProcessMonitorStatusMessage(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getProcessMonitorObject()==null) break main;
			
			sReturn = this.getMainObject().getProcessMonitorObject().getStatusLocalMessage();
		}//end main:
		return sReturn;
	}
	
	/** Reads a status string from the ConnectionMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readVpnIpPingerStatusString(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getVpnIpPingerObject()==null) break main;
			
			sReturn = this.getMainObject().getVpnIpPingerObject().getStatusLocalAbbreviation();
		}//end main:
		return sReturn;
	}
	
	/** Reads a status string from the ConnectionMonitor-Object-Thread
	* @return String
	* 
	* lindhaueradmin; 10.08.2006 11:32:16
	 */
	public String readVpnIpPingerStatusMessage(){
		String sReturn = "";
		main:{
			if(this.getMainObject()==null)break main;
			if(this.getMainObject().getVpnIpPingerObject()==null) break main;
			
			sReturn = this.getMainObject().getVpnIpPingerObject().getStatusLocalMessage();
			
			String sError = this.getMainObject().getVpnIpPingerObject().getStatusLocalError();
			if(!StringZZZ.isEmpty(sError)) {
				sReturn = sReturn + " -" + sError;
			}
		}//end main:
		return sReturn;
	}
	
	/** Ausgabe des Strings für den Dialog, beim Clicken auf das TrayIcon. 
	 *  Merke: Die Länge des Strings ist dafür begrenzt.
	 * @return
	 * @throws ExceptionZZZ
	 */
	public String readStatusDetailString() throws ExceptionZZZ{
		String sReturn = "";
		main:{
			if (this.getMainObject() == null){
				sReturn = "No Client Backend available.";
				break main;
			}
			
			String stemp = null;
			
			//Merke: der BackendStausString wird von den Events, die er empfaengt gefuettert.
			//Hole immer die letzte StatusMeldung...
			String sStatusClientString = this.readBackendStatusMessage();
			if(StringZZZ.isEmpty(sStatusClientString)){				
				sReturn = sReturn + ClientMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "\n";
				break main;
			}else{			
				sReturn = sReturn + sStatusClientString + "\n";
			}

			//Hole die Meldung vom OVPN-Monitor
			String sStatusProcessMonitorString = this.readProcessMonitorStatusMessage();
			if(StringZZZ.isEmpty(sStatusProcessMonitorString)){
				sReturn = sReturn + ClientMainOVPN.STATUSLOCAL.ISCONNECTNEW.getStatusMessage() + "\n";
				break main;
			}else{
				//Falls der Client gestartet wurde, hole die Statusmeldung aus dem Backend-Monotor-Objekt. Also neu setzen.
				sStatusProcessMonitorString = this.getMainObject().getProcessMonitorObject().getStatusLocalMessage() + "\n";
								
				//20200114: Erweiterung - Angabe des Rechnernamens
				try {																
					//Falls der Client gestartet wurde, gib den Rechnernamen aus, anstatt einer "ist gestartet" Meldung. Spart Platz.
					String sServerOrClient = this.getMainObject().getConfigChooserObject().getOvpnContextUsed();					
					sReturn = sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
				} catch (UnknownHostException e) {				
					e.printStackTrace();
					ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
					throw ez;
				}
				
				sReturn = sStatusProcessMonitorString + sReturn;
			}
					
		if(this.getMainObject().getFlag("useProxy")==true){
			sReturn = sReturn + "Proxy: " + this.getMainObject().getApplicationObject().getProxyHost() + ":" + this.getMainObject().getApplicationObject().getProxyPort() + "\n"; 					
		}else{
			//sReturn = sReturn + "No proxy.\n";
		}
		
		stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getURL2Parse();
		if(stemp==null){
			sReturn = sReturn + "URL: NOT RECEIVED\n";
		}else{
			stemp = StringZZZ.right("http://" + stemp, "http://", false);
			stemp = StringZZZ.abbreviateDynamic(stemp,40);
			sReturn = sReturn + "URL: " + stemp + "\n";
		}
		
		//REMOTE
		stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getIpRemote();
		if(stemp==null){
			sReturn = sReturn + "Remote IP: Not found on URL.|";
		}else{			
			sReturn = sReturn + "Remote IP: " + stemp + "|";
		}
		 
		if(this.getMainObject().isPortScanEnabled()){			 
			stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getRemotePortScanned();
			if(stemp == null){
				sReturn = sReturn + "->Port(s): Not yet scanned.\n";
			}else{
				stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getRemotePortScanned();
				sReturn = sReturn + "->Port(s):" + stemp+"\n";
			}
		}
		
		//VPNIP, wird extra an die VpnIpEstablished-Property uebergeben, wenn eine Verbindung festgestellt wird.
		//this.getClientMainObject().getApplicationObject().getVpnIpRemote() //Das wäre aber noch keine erstellte Verbindung, sondern eher nur das Ziel.
		stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpRemoteEstablished();
		if(stemp == null){
			sReturn = sReturn + "->VPN-IP: Not connected.\n";
		}else{
			sReturn = sReturn + "->VPN-IP: " + stemp + "\n";
			/* Logischer Fehler: Wenn die VPN-Verbindung erstellt worden ist, dann ist ggf. auch ein anderer Port "anpingbar" per meinem JavaPing.
			stemp = this.objConfig.getVpnPortEstablished();
			sReturn = sReturn + ":" + stemp;
			*/
		}
		
		if(this.getMainObject().isPortScanEnabled()==true){
			stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnPortScanned();
			if(stemp == null){
				sReturn = sReturn + "->VPN-IP Port(s): Not scanned.\n";
			}else{
				stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnPortScanned();
				sReturn = sReturn + "->VPN-IP Port(s):" + stemp+"\n";
			}
		}
		
		stemp = this.getMainObject().getApplicationObject().getIpLocal();
		if(stemp==null){
			sReturn = sReturn + "Local IP: Not availabel.|";
		}else{
			sReturn = sReturn + "Local IP: " + stemp + "|";
		}
		
		String sTap = this.getMainObject().getApplicationObject().getTapAdapterUsed();
		if(sTap==null){
			sTap = "->TAP: Not defined in Kernel Ini-File.";
		}else{
			sTap = "->TAP: " + sTap + "";
		}
		
		stemp = ((ClientApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpLocal();
		if(stemp==null){
			sReturn = sReturn + "->VPN-IP: Not defined in Kernel Ini-File.|" + sTap + "\n";
		}else{
			sReturn = sReturn + "->VPN-IP: " + stemp + "\n\t" + sTap + "\n";
		}
		
			
		//Hole die Status Message vom Pinger
		String sStatusPingerString = this.readVpnIpPingerStatusMessage();
		if(StringZZZ.isEmpty(sStatusPingerString)){
			sReturn = sReturn + ClientMainOVPN.STATUSLOCAL.ISPINGNEW.getStatusMessage() + "\n";
			break main;
		}else{
			//Falls der Pinger gestartet wurde, hole die Statusmeldung aus dem Backend-Monotor-Objekt. Also neu setzen.
			//Das unterschlaegt ggfs. vorhanden Fehlermeldunge: sStatusPingerString = this.getMainObject().getVpnIpPingerObject().getStatusLocalMessage() + "\n";
			sReturn = sReturn + sStatusPingerString + "\n";
		}
				
		}//END main
		return sReturn;
	}
	
	public String readProtocolString(){
		String sReturn = "";
		main:{
			check:{
				if (this.getMainObject() == null){
					sReturn = ClientMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "(objClientBackend NULL case)";					
					break main;
				}
			}//END check:
		
		ArrayList listaProtocolString = this.getMainObject().getProtocolStringAll();
		if(listaProtocolString.isEmpty()){
			if (this.getMainObject() == null){
					sReturn = "No log string available.";
					break main;
			}
		}
		
		for(int icount = 0; icount < listaProtocolString.size(); icount++){
			String sProtocol = (String)listaProtocolString.get(icount);
			sReturn = sReturn + sProtocol + "\n";
		}
		
		}//END main
		return sReturn;
	}
	
	
	//#######################
	//### GETTER / SETTER
	public void setMainObject(IClientMainOVPN objClientBackend){
		ITrayOVPN objTray = (ITrayOVPN) this.getTrayParent();
		objTray.setMainObject((ClientMainOVPN)objClientBackend);
	}
	public ClientMainOVPN getMainObject(){
		ClientMainOVPN objReturn = null;
		main:{
			ITrayOVPN objTray = (ITrayOVPN) this.getTrayParent();		
			objReturn = (ClientMainOVPN) objTray.getMainObject();
		}//end main:
		return objReturn;
	}
	
	@Override
	public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
		try{
				String sCommand = ae.getActionCommand();
				//System.out.println("Action to perform: " + sCommand);
				if(sCommand.equals(ClientTrayMenuTypeZZZ.END.getMenu())){
					this.unload();	
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.START.getMenu())){
					this.start();
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.CONNECT.getMenu())) {
					this.connect();
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.PING.getMenu())) {
					this.ping();	
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.PROTOCOL.getMenu())){
					//JOptionPane pane = new JOptionPane();
					String stemp = this.readProtocolString();
					//this.getTrayIconObject() ist keine Component ????
					JOptionPane.showMessageDialog(null, stemp, "Log der Verbindung", JOptionPane.INFORMATION_MESSAGE );
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.ADJUSTMENT.getMenu())) {
					//TODOGOON 20210210: Wenn es eine HashMap gäbe, dann könnte man diese über eine Methode 
					//                   ggfs. holen, wenn sie schon mal erzeugt worden ist.	
					
					 //Also In ClientMainUI
					//HashMap<String, KernelJDialogExtendedZZZ> hmContainerDialog....
					//
					//Also ClientMainUIZZZ implements Interface IClientMainUIZZZ
					//                                 mit der Methode HashMap<String, KernelJDialogExtendedZZZ> .getDialogs();
					//                                 mit der Methode KernelJDialogExtendedZZZ .getDialogByAlias(....)
				   
					
					//Also ClientTrayUIZZZ implements Interface IClientMainUIUserZZZ 
					//                               mit der Methode .getClientMainUI();
					//                                               .setClientMainUI(IClientMainUI objClientMain)
					//objMainUI = this.getClientMainUI
					//objMainUI.getDialogByAlias(....)
					
					//Bei CANCEL: Lösche diese Dialogbox, d.h. sie wird auch wieder komplett neu gemacht.
					//Neuer Button CLOSE: D.h. die Dialogbox wird geschlossen, aber wenn sie wieder neu geöffnet wird, 
					//                    dann sind ggfs. eingegebene Werte wieder da.
					ClientTrayUIOVPN objTray = (ClientTrayUIOVPN) this.getTrayParent();
					if(objTray.getDialogAdjustment()==null || objTray.getDialogAdjustment().isDisposed() ) {
					
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
						//       Sie ermöglicht die Konfiguration der anderen Module.
						HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
						hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
						DlgAdjustmentOVPN dlgAdjustment = new DlgAdjustmentOVPN(this.getKernelObject(), null, hmFlag);
						dlgAdjustment.setText4ButtonOk("USE VALUE");
						
						objTray.setDialogAdjustment(dlgAdjustment);				
					}
										
					try {
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						objTray.getDialogAdjustment().showDialog(null, "Adjustments for available Modules");
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: Dialog 'Adjustment'");
					} catch (ExceptionZZZ ez) {					
						System.out.println(ez.getDetailAllLast()+"\n");
						ez.printStackTrace();
						ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
					}				
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.PAGE_IP_READ.getMenu())) {					
					ClientTrayUIOVPN objTray = (ClientTrayUIOVPN) this.getTrayParent();
					
					//TODOGOON 20210210: Wenn es eine HashMap gäbe, dann könnte man diese über eine Methode 
					//                   ggfs. holen, wenn sie schon mal erzeugt worden ist.	
					
					 //Also In ClientMainUI
					//HashMap<String, KernelJDialogExtendedZZZ> hmContainerDialog....
					//
					//Also ClientMainUIZZZ implements Interface IClientMainUIZZZ
					//                                 mit der Methode HashMap<String, KernelJDialogExtendedZZZ> .getDialogs();
					//                                 mit der Methode KernelJDialogExtendedZZZ .getDialogByAlias(....)
				   
					
					//Also ClientTrayUIZZZ implements Interface IClientMainUIUserZZZ 
					//                               mit der Methode .getClientMainUI();
					//                                               .setClientMainUI(IClientMainUI objClientMain)
					//objMainUI = this.getClientMainUI
					//objMainUI.getDialogByAlias(....)
					
					//Bei CANCEL: Lösche diese Dialogbox, d.h. sie wird auch wieder komplett neu gemacht.
					//Neuer Button CLOSE: D.h. die Dialogbox wird geschlossen, aber wenn sie wieder neu geöffnet wird, 
					//                    dann sind ggfs. eingegebene Werte wieder da.
					
					if(objTray.getDialogIpExternal()==null || objTray.getDialogIpExternal().isDisposed() ) {
					
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
						HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
						hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
						DlgIPExternalOVPN dlgIPExternal = new DlgIPExternalOVPN(this.getKernelObject(), null, hmFlag);
						dlgIPExternal.setText4ButtonOk("USE VALUE");
						
						objTray.setDialogIpExternal(dlgIPExternal);					
					}
										
					try {
						//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
						objTray.getDialogIpExternal().showDialog(null, "Read IP External/Build Page");
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: Dialog 'Read IP External/Build Page'");
					} catch (ExceptionZZZ ez) {					
						System.out.println(ez.getDetailAllLast()+"\n");
						ez.printStackTrace();
						ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
					}
				}else if(sCommand.equals(ClientTrayMenuTypeZZZ.DETAIL.getMenu())){			//"PressAction": DAS SCHEINT EIN FEST VORGEGEBENER NAME VON JDIC zu sein für das Clicken AUF das Icon.
					TrayIcon objTrayIcon = this.getTrayParent().getTrayIcon();
					
					String stemp = this.readStatusDetailString();
					if(stemp!= null){
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Status der Verbindung.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
					}else{
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Status der Verbindung.", "unable to receive any status.", TrayIcon.INFO_MESSAGE_TYPE);
					}
				
					/* DAS PASST NICHT IN DIESE SPRECHBLASE REIN
					String stemp = this.readLogString();
					if(stemp!= null){
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Log der Verbindung.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
					}else{
						if(objTrayIcon!=null) objTrayIcon.displayMessage("Log der Verbindung.", "unable to receive any log.", TrayIcon.INFO_MESSAGE_TYPE);
					}
					*/
				}
				
				bReturn = true;
		}catch(ExceptionZZZ ez){
			//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zur�ckgesetzt werden kann.				
			ez.printStackTrace();
			String stemp = ez.getDetailAllLast();
			this.getKernelObject().getLogObject().WriteLineDate(stemp);
			System.out.println(stemp);
			this.getTrayParent().switchStatus(ClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ.ERROR);			
		}
		
		}//end main:
		return bReturn;
	}
		
		
		@Override
		public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) throws ExceptionZZZ {
			// TODO Auto-generated method stub
			
		}
		
}//END Class
