package use.openvpn.clientui.component.tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.abstractList.ArrayListUtilZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.ResourceEasyZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.flag.event.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.event.IListenerObjectFlagZsetZZZ;
import basic.zKernel.status.IStatusBooleanZZZ;
import basic.zKernel.status.IStatusLocalMapForStatusLocalUserZZZ;
import basic.zKernelUI.component.IActionCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.tray.AbstractKernelTrayUIZZZ;
import basic.zKernelUI.component.tray.IActionTrayZZZ;
import basic.zWin32.com.wmi.KernelWMIZZZ;
import use.openvpn.IMainOVPN;
import use.openvpn.ITrayOVPN;
import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigFileZZZ;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalOVPN;
import use.openvpn.clientui.component.IPExternalRead.DlgIPExternalOVPN;
import use.openvpn.clientui.component.tray.IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ;
import use.openvpn.clientui.component.tray.IClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ;
import use.openvpn.component.shared.adjustment.DlgAdjustmentOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.serverui.component.FTPCredentials.DlgFTPCredentialsOVPN;
import use.openvpn.serverui.component.tray.ActionServerTrayUIOVPN;
import use.openvpn.serverui.component.tray.ServerTrayUIOVPN;
import use.openvpn.serverui.component.tray.IServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ;

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
public class ClientTrayUIOVPN extends AbstractKernelTrayUIZZZ implements  ITrayOVPN, IListenerObjectStatusLocalSetOVPN {
	private static final long serialVersionUID = -6110753128564853105L;

	private volatile ClientMainOVPN objMain = null;
	
		
	//TODOGOON 20210210: Realisiere die Idee
	//Idee: In ClientMainUI eine/verschiedene HashMaps anbieten, in die dann diese Container-Objekte kommen.
	//      Dadurch muss man sie nicht als Variable deklarieren und kann dynamischer neue Dialogboxen, etc. hinzufügen.
	//Ziel diese hier als Variable zu deklarieren ist: Die Dialogbox muss nicht immer wieder neu erstellt werden.
	private KernelJDialogExtendedZZZ dlgIPExternal=null;
	private KernelJDialogExtendedZZZ dlgAdjustment=null;
	
	public ClientTrayUIOVPN(IKernelZZZ objKernel, ClientMainOVPN objClientMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);//20210402: Die direkten Flags werden nun in der Elternklasse verarbeitet
		ClientTrayUINew_(objClientMain);
	}
	
	public ClientTrayUIOVPN(IKernelZZZ objKernel, IClientMainOVPN objClientMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);//20210402: Die direkten Flags werden nun in der Elternklasse verarbeitet
		ClientTrayUINew_(objClientMain);
	}
	
	private void ClientTrayUINew_(IClientMainOVPN objClientMain) throws ExceptionZZZ{
		main:{		
			if(this.getFlag("init")) break main;
			if(objClientMain==null){
				ExceptionZZZ ez = new ExceptionZZZ("ClientMain-Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName()); 					 
				 throw ez;		 
			}
			this.setMainObject(objClientMain);
			
			
			//Dieses muss beim Beenden angesprochen werden, um das TrayIcon wieder zu entfernen
			//Merke 20220718: Wohl unter Win10 nicht lauffähig
			TrayIcon objTrayIcon = this.getTrayIcon();
			
			//https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html
			//this.objTray = SystemTray.getSystemTray();
			
			IActionTrayZZZ objActionListener = this.getActionListenerTrayIcon();
			objTrayIcon.addActionListener((ActionListener) objActionListener);
			
			SystemTray objTray = this.getSystemTray();
			objTray.addTrayIcon(objTrayIcon);
			//Merke: Ueber unload() wird das TrayIcon wieder entfernt.
			
			//Den Process-Monitor auch schon vorbereiten, auch wenn ggfs. nicht schon am Anfang auf die Verbindung "gelistend" wird.
			//Er wird auch am Backend-Objekt registriert, um dortige Aenderungen mitzubekommen.
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating ClientThreadProcessWatchMonitorOVPN-Object";
			System.out.println(sLog);
			this.getLogObject().WriteLineDate(sLog);
						
			//### Registriere das Tray-Objekt selbst ##############
			//a) Fuer Aenderungen an den Main-Objekt-Flags. Das garantiert, das der Tray auch auf Änderungen der Flags reagiert, wenn ServerMain in einem anderen Thread ausgeführt wird.
			this.getMainObject().registerForFlagEvent(this);
						
			//b) Fuer Aenderung am Main-Objekt-Status. Das garantiert, das der Tray auch auf Änderungen des Status reagiert, wenn ServerMain in einem anderen Thread ausgeführt wird.
			this.getMainObject().registerForStatusLocalEvent(this);
		}//END main
	}
		
	public static ImageIcon getImageIconByStatus(ClientTrayStatusTypeZZZ enumSTATUS)throws ExceptionZZZ{	
		ImageIcon objReturn = null;
		main:{
			URL url = null;
			ClassLoader objClassLoader = ClientTrayUIOVPN.class.getClassLoader(); 
			if(objClassLoader==null) {
				ExceptionZZZ ez = new ExceptionZZZ("unable to receiver classloader object", iERROR_RUNTIME, ClientTrayUIOVPN.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			String sPath= ResourceEasyZZZ.searchDirectoryAsStringRelative("resourceZZZ/image/tray"); //Merke: Innerhalb einer JAR-Datei soll hier ein src/ vorangestellt werden.					
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for directory '"+sPath+"'");
						
			String sImageIcon = enumSTATUS.getIconFileName();
			String sPathTotal = FileEasyZZZ.joinFilePathNameForUrl(sPath, sImageIcon);
			
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Using path for imageicon '"+sPathTotal+"'");			
			url= ClassLoader.getSystemResource(sPathTotal);
			if(url==null) {
				String sLog = "unable to receive url object. Path '" + sPathTotal + "' not found?";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
				ExceptionZZZ ez = new ExceptionZZZ(sLog, iERROR_RUNTIME, ClientTrayUIOVPN.class.getName(), ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else {
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": URL = '"+url.toExternalForm() + "'");
			}
			objReturn = new ImageIcon(url);
		}//END main:
		return objReturn;
	}
			
	/** Aendere das ImageIcon je nach dem uebergebenen enum des StatusTyps. 
	 * @param enumSTATUS
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 11.10.2023, 07:56:09
	 */
	@Override
	public boolean switchStatus(IEnumSetMappedZZZ objEnumMappedIn) throws ExceptionZZZ{	
		boolean bReturn = false;
		main:{			
			ClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ enumSTATUS = (ClientTrayStatusTypeZZZ) objEnumMappedIn;
			
			ImageIcon objIcon = this.getImageIconByStatus(enumSTATUS);
			if(objIcon==null)break main;
			
			//+++++ Test: Logge den Menüpunkt			
			IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ objEnumMenu = (IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ) enumSTATUS.getAccordingTrayMenuType();
			if(objEnumMenu!=null){
				String sLog = ReflectCodeZZZ.getPositionCurrent() +": Menuepunkt=" + objEnumMenu.getMenu();
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
			}else {
				String sLog = ReflectCodeZZZ.getPositionCurrent() +": Kein Menuepunkt vorhanden.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
			}
			//++++++++++++++++++++++++++++++++
						
			this.getTrayIcon().setIcon(objIcon);
			
			bReturn = true;
		}//END main:
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
			//###### Prozesse beenden
			//+++ Vorbereitend den processnamen auslesen
			File objFileExe = ClientConfigFileZZZ.findFileExe();
			if(objFileExe!=null){
				String sExeCaption = objFileExe.getName();
				//+++ Wenigstens beende ich nun alle openvpn.exe - Processe (DASZU WIRD JACOB (Java COM Bridge) verwendet.	
				KernelWMIZZZ objWMI = new KernelWMIZZZ(objKernel, null);
				objWMI.killProcessAll(sExeCaption);				
			}//END if file!= null
			
			//+++ Merke: Windows merkt das Entfernen nicht sofort, sondern manchmal erst, wenn man den Mauszeiger auf den System-Tray-Bereich bewegt.
			this.objTray.removeTrayIcon(this.objTrayIcon);
			bReturn = true;
			System.exit(0);
		}
		return bReturn;
	}

	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
			if(this.getMainObject()==null)break main;
			}

			//Wenn das so ohne Thread gestartet wird, dann reagiert der Tray auf keine weiteren Clicks.
			//Z.B. den Status anzuzeigen.			
			//Den Staart ueber einen extra Thread durchfuehren, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!			
			Thread objThreadMain = new Thread(this.getMainObject());
			objThreadMain.start();
			
			bReturn = true;
		}//end main:
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
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
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
					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
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
			if (this.objMain == null){
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
			sReturn = sReturn + "Proxy: " + this.getMainObject().getApplicationObject().getProxyHost() + ":" + this.objMain.getApplicationObject().getProxyPort() + "\n"; 					
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
			if (this.objMain == null){
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
	
//	@Override 
//	public SystemTray getSystemTray() throws ExceptionZZZ{
//		if(this.objTray == null) {
//			this.objTray = SystemTray.getDefaultSystemTray();
//		}
//		return this.objTray;
//	}
//	
//	@Override
//	public void setSystemTray(SystemTray objTray) {
//		this.objTray = objTray;
//	}
//	
//	@Override
//	public TrayIcon getTrayIcon() throws ExceptionZZZ{
//		if(this.objTrayIcon==null) {
//			JPopupMenu menu = this.getMenu();
//			ImageIcon objIcon = ClientTrayUIOVPN.getImageIconByStatus(ClientTrayStatusTypeZZZ.NEW);
//			this.objTrayIcon = new TrayIcon(objIcon, "OVPNListener", menu);
//		}
//		return this.objTrayIcon;
//	}
//	
//	@Override
//	public void setTrayIcon(TrayIcon objTrayIcon) {
//		this.objTrayIcon = objTrayIcon;
//	}
	
	@Override
	public ClientMainOVPN getMainObject(){
		return this.objMain;
	}
	
	@Override
	public void setMainObject(IMainOVPN objMain){
		this.objMain = (ClientMainOVPN) objMain;
	}
	
	
	
	public KernelJDialogExtendedZZZ getDialogIpExternal() throws ExceptionZZZ {
		if(this.dlgIPExternal==null || this.dlgIPExternal.isDisposed() ) {
			
			//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
			//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
			HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
			hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
			DlgIPExternalOVPN dlgIPExternal = new DlgIPExternalOVPN(this.getKernelObject(), null, hmFlag);
			dlgIPExternal.setText4ButtonOk("USE VALUE");
			
			this.dlgIPExternal = dlgIPExternal;					
		}
		return this.dlgIPExternal;
	}
	public void setDialogIpExternal(KernelJDialogExtendedZZZ dlgIpExternal) {
		this.dlgIPExternal = dlgIpExternal;
	}
	
	public KernelJDialogExtendedZZZ getDialogAdjustment() throws ExceptionZZZ {
					
		if(this.dlgAdjustment==null || this.dlgAdjustment.isDisposed() ) {
			
			//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
			//Merke: Diese Dialogbox soll als Modul in der Kernel-Ini-Datei konfiguriert sein.
			//       Sie ermöglicht die Konfiguration der anderen Module.
			HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
			hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
			DlgAdjustmentOVPN dlgAdjustment = new DlgAdjustmentOVPN(this.getKernelObject(), null, hmFlag);
			dlgAdjustment.setText4ButtonOk("USE VALUE");
			
			this.dlgAdjustment = dlgAdjustment;					
		}
		return this.dlgAdjustment;
	}
	public void setDialogAdjustment(KernelJDialogExtendedZZZ dlgAdjustment) {
		this.dlgAdjustment = dlgAdjustment;
	}
	
	//+++ Aus IListenerObjectFlagZsetZZZ
	@Override
	public boolean flagChanged(IEventObjectFlagZsetZZZ eventFlagZset) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{
			Enum objFlagEnum = eventFlagZset.getFlagEnum();
			if(objFlagEnum==null) break main;
			
			boolean bFlagValue = eventFlagZset.getFlagValue();
			if(bFlagValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.

		}//end main:
		return bReturn;
	}
	
	//+++ Aus IListenerObjectStatusLocalSetOVPN
	@Override
	public boolean changedStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Der Tray ist am MainObjekt registriert.
		//Wenn ein Event geworfen wird, dann reagiert er darauf, hiermit....
		boolean bReturn=false;
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
			
			//+++ Mappe nun die eingehenden Status-Enums auf die eigenen
			if(eventStatusLocalSet.getStatusEnum() instanceof IClientMainOVPN.STATUSLOCAL){					
				bReturn = this.statusLocalChangedMainEvent_(eventStatusLocalSet);
				break main;
				
			}
//				else if(eventStatusLocalSet.getStatusEnum() instanceof IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) {
//					System.out.println(ReflectCodeZZZ.getPositionCurrent() +" :FGLTEST 02");
//					bReturn = this.statusLocalChangedMonitorEvent_(eventStatusLocalSet);
//					break main;
//				}
			else {	
				sLog = ReflectCodeZZZ.getPositionCurrent() +" : Status-Enum wird von der Klasse her nicht betrachtet.";
				System.out.println(sLog);	
				this.getMainObject().logProtocolString(sLog);
			}											
			bReturn = true;
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
	private boolean statusLocalChangedMainEvent_(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn=false;
		main:{	
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer MainEvent.";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusLocal();				
			STATUSLOCAL objStatusEnum = (STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
			if(objStatusEnum==null) break main;
				
			//Falls nicht zuständig, mache nix
			boolean bProof = this.isEventRelevant2ChangeStatusLocal(eventStatusLocalSet);
			if(!bProof) break main;
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
				
			//+++ Weiterverarbeitung des relevantenStatus. Merke: Das ist keine CascadingStatus-Enum. Sondern hier ist nur der Bilddateiname drin.
			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hmEnum	= this.getHashMapEnumSetForStatusLocal();		
			ClientTrayStatusTypeZZZ objEnumForTray = (ClientTrayStatusTypeZZZ) hmEnum.get(objStatusEnum);			
			if(objEnumForTray==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
			}			
		
			
			
				
			//########################################	
			//### Erneute Verarbeitung mit einem frueheren Status:
			//### Setze den Status auf einen anderen Status zurueck
			//########################################	
			if(objEnumForTray == ClientTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE) {
				//++++++++ TESTEN - Ermittle u.a. die StatusGroupIds über alle vorherigen Events...
				this.getMainObject().debugCircularBufferStatusLocalMessage();				
				//+++ TESTENDE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					

				//++++++++ TESTEN - Ermittle die Indexwerte der aktuellen GroupId im CircularBuffer
				int iGroupIdCurrent = this.getMainObject().getStatusLocalGroupIdFromCurrent();
				int iIndexLower = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index der GroupId " + iGroupIdCurrent +" ist="+iIndexLower;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				int iIndexLowerInterrupted = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index (interrupted) der GroupId " + iGroupIdCurrent +" ist="+iIndexLowerInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				int iIndexUpper = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index der GroupId " + iGroupIdCurrent +" ist="+iIndexUpper;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	

				int iIndexUpperInterrupted = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index(interrupted) der GroupId " + iGroupIdCurrent +" ist="+iIndexUpperInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				

				//++++++++ TESTEN - Ermittle die vorherige GroupId
				int iGroupIdPreviousDifferentFromCurrent = this.getMainObject().searchStatusLocalGroupIdPreviousDifferentFromCurrent();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Die vorherige, andere GroupId ist = " + iGroupIdPreviousDifferentFromCurrent +".";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	
				//+++ TESTENDE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				
				//++++++++ TESTEN - Ermittle die Indexwerte der vorherigen GroupId im CircularBuffer
				iIndexLower = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdPreviousDifferentFromCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexLower;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				iIndexLowerInterrupted = this.getMainObject().searchStatusLocalGroupIndexLowerInBuffer(iGroupIdPreviousDifferentFromCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der lower Index (interrupted) der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexLowerInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
				
				iIndexUpper = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdPreviousDifferentFromCurrent);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexUpper;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	

				iIndexUpperInterrupted = this.getMainObject().searchStatusLocalGroupIndexUpperInBuffer(iGroupIdPreviousDifferentFromCurrent, true);
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der upper Index(interrupted) der GroupId " + iGroupIdPreviousDifferentFromCurrent +" ist="+iIndexUpperInterrupted;
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);	
				
				
				
				//Nun kann man aus der ermittelten Liste den ersten Eintrag nehmen
				ArrayList<IStatusBooleanZZZ>listaObjStatusLocalPrevious = this.getMainObject().searchStatusLocalGroupById(iGroupIdPreviousDifferentFromCurrent, true);
				IStatusBooleanZZZ objStatusLocalPrevious = (IStatusBooleanZZZ) ArrayListUtilZZZ.getFirst(listaObjStatusLocalPrevious);					
				if(objStatusLocalPrevious!=null) {
					objEnumForTray = (ClientTrayStatusTypeZZZ) hmEnum.get(objStatusLocalPrevious.getEnumObject());			
					if(objEnumForTray==null) {
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen gemappten Status aus dem Event-Objekt erhalten. Breche ab";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);
						break main;
					}else {
						//Erst einmal den gefundenen Status neu hinzufügen. Damit er auch bei einem weiteren "rueckwaerts Suchen" in der Liste auftaucht.
						sLog = ReflectCodeZZZ.getPositionCurrent()+": Nimm den gefundenen Status in die Liste als neuen Status auf: '" + objEnumForTray.getAbbreviation() + "'";
						System.out.println(sLog);
						this.getMainObject().logProtocolString(sLog);							
						this.getMainObject().offerStatusLocal((Enum) objStatusLocalPrevious.getEnumObject(), true, "");											
					}	
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent()+": Keinen Status aus dem Event-Objekt erhalten. Breche ab";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);
					break main;
				}
						
				//ggfs. noch weiter schrittweise zurück, wenn der vorherige Status ein Steuerevent war...,d.h. ohne Icon
//					if(objStatusLocalPrevious!=null) {
//						objEnum = (ClientTrayStatusTypeZZZ) hmEnum.get(objStatusLocalPrevious);			
//						if(objEnum!=null) {					
//							if(StringZZZ.isEmpty(objEnum.getIconFileName())){
//								iStepsToSearchBackwards=1;
//								
//								sLog = ReflectCodeZZZ.getPositionCurrent()+": Steuerevent als gemappten Status aus dem Event-Objekt erhalten. Gehe noch einen weitere " + iStepsToSearchBackwards + " Schritt(e) zurueck.";
//								System.out.println(sLog);
//								this.getMainObject().logProtocolString(sLog);							
//							}else {
//								bGoon=true;
//							}
//						}
//					}

				
				
				//#############################					
				//Frage nach dem Status im Backend nach...
				IEnumSetMappedStatusZZZ objStatusLocalCurrent = this.getMainObject().getStatusLocalEnumCurrent();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Der aktuelle Status im Main ist '" + objStatusLocalCurrent.getAbbreviation()+"'.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
			
				int iGroupIdPrevious = this.getMainObject().getStatusLocalGroupIdFromPrevious();
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Die vorherige GroupId ist= " + iGroupIdPrevious +".";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
				this.logLineDate(sLog);
					
			}//if(objEnum == ClientTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE) {					
			this.switchStatus(objEnumForTray); //Merke: Der Wert true wird angenommen.
			
			bReturn = true;
		}//end main:
		return bReturn;
	}

//		/** Merke: Diese private Methode wird nach ausführlicher Prüfung aufgerufen, daher hier mehr noetig z.B.:
//		 * - Keine Pruefung auf NULLL
//		 * - kein instanceof 
//		 * @param eventStatusLocalSet
//		 * @return
//		 * @throws ExceptionZZZ
//		 * @author Fritz Lindhauer, 19.10.2023, 09:43:19
//		 */
//		private boolean statusLocalChangedMonitorEvent_(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
//			boolean bReturn=false;
//			main:{	
//				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Fuer MonitorEvent.";
//				System.out.println(sLog);
//				this.getClientBackendObject().logMessageString(sLog);
//				
//				IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();				
//				IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL objStatusEnum = (IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL) eventStatusLocalSet.getStatusEnum();
//				if(objStatusEnum==null) break main;
//					
//				//Falls nicht zuständig, mache nix
//				boolean bProof = this.isEventStatusLocalRelevant(eventStatusLocalSet);
//				if(!bProof) break main;
//					
//				boolean bStatusValue = eventStatusLocalSet.getStatusValue();
//				if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
//					
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus hat class='"+enumStatus.getClass()+"'";
//				System.out.println(sLog);
//				this.getClientBackendObject().logMessageString(sLog);	
//					
//				sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"'";
//				System.out.println(sLog);
//				this.getClientBackendObject().logMessageString(sLog);
//					
//				boolean bRelevant = this.isStatusLocalRelevant(enumStatus);
//				if(!bRelevant) {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus='" + enumStatus.getAbbreviation()+"' ist nicht relevant. Breche ab.";
//					System.out.println(sLog);
//					this.getClientBackendObject().logMessageString(sLog);
//				}														
//	
//				//Die Stati vom Backend-Objekt mit dem TrayIcon mappen
//				if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTING==objStatusEnum) {
//					this.switchStatus(ClientTrayStatusTypeZZZ.CONNECTING);				
//				}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.ISCONNECTED==objStatusEnum) {
//					//this.switchStatus(ClientTrayStatusTypeZZZ.CONNECTED);
//					
//					//Ggfs. vorhandene Aenderungen aus dem Backend-Application-Objekt des Events holen, bzw. hier das ganze Backen-Objekt austauschen
//					//Dann kann sich z.B. die Datailinfo-box die aktuellsten Werte daraus holen.
//					IApplicationOVPN objApplicationUsed = eventStatusLocalSet.getApplicationObjectUsed();
//					if(objApplicationUsed==null) {						
//						sLog = ReflectCodeZZZ.getPositionCurrent()+": Kein Application-Objekt aus dem Event erhalten.";
//						System.out.println(sLog);
//						this.getClientBackendObject().logMessageString(sLog);
//					}else {
//						this.getClientBackendObject().setApplicationObject(objApplicationUsed);
//
//						//################################
//						//Merke: Dieser Wert kommt beim Setzen im ClientThreadProcessWatchMonitor in diesem Backenobjekt nicht an.
//						//       Darum explizit holen und setzen.
//						String sVpnIp = this.getClientBackendObject().getApplicationObject().getVpnIpRemote();
//												
//						sLog = ReflectCodeZZZ.getPositionCurrent()+": Verbunden mit remote VPNIP='"+sVpnIp+"'";
//						System.out.println(sLog);
//						this.getClientBackendObject().logMessageString(sLog);
//						
//						//Nun die als "verbunden" gekennzeichnete IP an das ApplicationObjekt uebergben.
//						this.getClientBackendObject().getApplicationObject().setVpnIpRemoteEstablished(sVpnIp);
//						//################################
//					}
//					this.switchStatus(ClientTrayStatusTypeZZZ.CONNECTED);
//					
//				}else if(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASERROR==objStatusEnum) {
//					this.switchStatus(ClientTrayStatusTypeZZZ.ERROR);
//				}else {
//					sLog = "Der Status wird nicht behandelt - '"+objStatusEnum.getAbbreviation()+"'.";
//					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);					
//					this.logLineDate(sLog);
//					break main;
//				}
//				
//				bReturn = true;
//			}//end main:
//			return bReturn;
//		}
		
	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isStatusLocalRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Pruefe Relevanz des Events.";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			IEnumSetMappedZZZ enumStatusFromEvent = eventStatusLocalSet.getStatusEnum();				
			if(enumStatusFromEvent==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": KEINEN enumStatus empfangen. Beende.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);							
				break main;
			}
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen. Wert: " + bStatusValue;
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus hat class='"+enumStatusFromEvent.getClass()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);	
				
			sLog = ReflectCodeZZZ.getPositionCurrent()+": enumFromEventStatus='" + enumStatusFromEvent.getAbbreviation()+"'";
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
			
			
			//#### Problemansatz: Mappen des Lokalen Status auf einen Status aus dem Event, verschiedener Klassen.
			String sStatusAbbreviationLocal = null;
			IEnumSetMappedZZZ objEnumStatusLocal = null;

			HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hm=this.createHashMapEnumSetForStatusLocalCustom();
			objEnumStatusLocal = hm.get(enumStatusFromEvent);					
			//###############################
			
			if(objEnumStatusLocal==null) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping nicht mit Wert vorhanden. Damit nicht relevant.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
				//sStatusAbbreviationLocal = enumStatusFromEvent.getAbbreviation();
			}else {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Klasse '" + enumStatusFromEvent.getClass() + "' ist im Mapping mit Wert vorhanden. Damit relevant.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				sStatusAbbreviationLocal = objEnumStatusLocal.getAbbreviation();
			}
			
			//+++ Pruefungen
			bReturn = this.isEventRelevant2ChangeStatusLocalByClass(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
// für den Tray nicht relevant
//				bReturn = this.isStatusLocalChanged(sStatusAbbreviationLocal, bStatusValue);
//				if(!bReturn) {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
//					System.out.println(sLog);
//					this.getMainObject().logProtocolString(sLog);
//					break main;
//				}else {
//					sLog = ReflectCodeZZZ.getPositionCurrent()+": Status geaendert. Mache weiter.";
//					System.out.println(sLog);
//					this.getMainObject().logProtocolString(sLog);
//				}
			
			//dito gibt es auch die Methode isStatusLocalRelevant(...) nicht, da der Tray kein AbstractObjectWithStatus ist, er verwaltet halt selbst keinen Status.
			
						
			bReturn = this.isEventRelevantByStatusLocalValue2ChangeStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByReactionHashMap2ChangeStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
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

						
			bReturn = true;
		}//end main:
		return bReturn;
	}

	@Override
	public boolean isEventRelevant2ChangeStatusLocalByClass(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		return true;
	}

	@Override
	public boolean isEventRelevant2ChangeStatusLocalByStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		return true;
	}

	@Override
	public boolean isEventRelevant2ChangeStatusLocalByStatusLocalValue(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			String sLog = ReflectCodeZZZ.getPositionCurrent()+": Einen enumStatus empfangen. Wert: " + bStatusValue;
			System.out.println(sLog);
			this.getMainObject().logProtocolString(sLog);
		
			if(!bStatusValue)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
			
			bReturn = true;
		}
		return bReturn;
	}

	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> getHashMapEnumSetForStatusLocal() {
		if(this.hmEnumSet==null) {
			this.hmEnumSet = this.createHashMapEnumSetForStatusLocalCustom();
		}
		return this.hmEnumSet;
	}

	@Override
	public void setHashMapEnumSetForStatusLocal(HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> hmEnumSet) {
		this.hmEnumSet = hmEnumSet;
	}

	@Override
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedZZZ> createHashMapEnumSetForStatusLocalCustom() {
		HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>hmReturn = new HashMap<IEnumSetMappedStatusZZZ,IEnumSetMappedZZZ>();
		main:{
			
			//Reine Lokale Statuswerte kommen nicht aus einem Event und werden daher nicht gemapped. 
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISSTARTNEW, ClientTrayStatusTypeZZZ.NEW);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISSTARTING, ClientTrayStatusTypeZZZ.STARTING);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISSTARTED, ClientTrayStatusTypeZZZ.STARTED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISCONNECTING, ClientTrayStatusTypeZZZ.CONNECTING);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISCONNECTED, ClientTrayStatusTypeZZZ.CONNECTED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISCONNECTINTERUPTED, ClientTrayStatusTypeZZZ.INTERRUPTED);
			
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTING, ClientTrayStatusTypeZZZ.PINGING);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGSTARTED, ClientTrayStatusTypeZZZ.PINGED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTING, ClientTrayStatusTypeZZZ.PINGCONNECTING);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTED, ClientTrayStatusTypeZZZ.PINGCONNECTED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGCONNECTNO, ClientTrayStatusTypeZZZ.PINGCONNECTNO);
			
			//++++++++++++++++++++++++
			//Berechne den wirklichen Typen anschliessend, dynamisch. Es wird auf auf einen vorherigen Event zugegriffen durch eine zweite Abfrage
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.ISPINGSTOPPED, ClientTrayStatusTypeZZZ.PREVIOUSEVENTRTYPE);
			
			//+++++++++++++++++++++++
			
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.HASPINGERROR, ClientTrayStatusTypeZZZ.FAILED);
			hmReturn.put(IClientMainOVPN.STATUSLOCAL.HASERROR, ClientTrayStatusTypeZZZ.ERROR);
		}//end main:
		return hmReturn;
	}
	
//	@Override
//	public JPopupMenu getMenu() throws ExceptionZZZ {
//		if(this.objMenu==null) {
//			this.objMenu = this.createMenuCustom();
//		}
//		return this.objMenu;	}
//
//	@Override
//	public void setMenu(JPopupMenu menu) throws ExceptionZZZ {
//		this.objMenu = menu;
//		this.getTrayIcon().setPopupMenu(menu);
//	}

	@Override
	public IActionTrayZZZ getActionListenerTrayIcon() throws ExceptionZZZ {
		if(this.objActionListener==null) {
			IActionTrayZZZ objActionListener = new ActionClientTrayUIOVPN(this.getKernelObject(), this);
			this.setActionListenerTrayIcon(objActionListener);
		}
		return this.objActionListener;		
	}

	@Override
	public void setActionListenerTrayIcon(IActionTrayZZZ objActionListener) {
		this.objActionListener = objActionListener;
	}

	@Override
	public JPopupMenu createMenuCustom() throws ExceptionZZZ {
		JPopupMenu objReturn = new JPopupMenu();
		main:{
			IActionTrayZZZ objActionListener = this.getActionListenerTrayIcon();
			
			JMenuItem menueeintrag2 = new JMenuItem(ClientTrayMenuTypeZZZ.START.getMenu());
			objReturn.add(menueeintrag2);
			menueeintrag2.addActionListener((ActionListener) objActionListener);
			
			
			JMenuItem menueeintrag2b = new JMenuItem(ClientTrayMenuTypeZZZ.CONNECT.getMenu());
			objReturn.add(menueeintrag2b);
			menueeintrag2b.addActionListener((ActionListener) objActionListener);
			
			JMenuItem menueeintrag2c = new JMenuItem(ClientTrayMenuTypeZZZ.PING.getMenu());
			objReturn.add(menueeintrag2c);
			menueeintrag2c.addActionListener((ActionListener) objActionListener);
			
			JMenuItem menueeintrag3 = new JMenuItem(ClientTrayMenuTypeZZZ.PROTOCOL.getMenu());
			objReturn.add(menueeintrag3);
			menueeintrag3.addActionListener((ActionListener) objActionListener);
			
			JMenuItem menueeintrag4 = new JMenuItem(ClientTrayMenuTypeZZZ.ADJUSTMENT.getMenu());
			objReturn.add(menueeintrag4);
			menueeintrag4.addActionListener((ActionListener) objActionListener);
			
			JMenuItem menueeintragFTP = new JMenuItem(ClientTrayMenuTypeZZZ.PAGE_IP_READ.getMenu());
			objReturn.add(menueeintragFTP);
			menueeintragFTP.addActionListener((ActionListener) objActionListener);
						
			JMenuItem menueeintrag = new JMenuItem(ClientTrayMenuTypeZZZ.END.getMenu());	
			objReturn.add(menueeintrag);		
			menueeintrag.addActionListener((ActionListener) objActionListener);
			
			//DUMMY Einträge, a: Server / Client
			//DUMMY Einträge, damit der unterste Eintrag ggfs. nicht durch die Windows Taskleiste verdeckt wird
			JMenuItem menueeintragLine = new JMenuItem("------------------");
			objReturn.add(menueeintragLine);
			//Kein actionListener für Dummy Eintrag
			
			JMenuItem menueeintragContext = new JMenuItem("RUNNING AS CLIENT");
			objReturn.add(menueeintragContext);
			//Kein actionListener für Dummy Eintrag
			
			JMenuItem menueeintragDummy = new JMenuItem(" ");
			objReturn.add(menueeintragDummy);
			//Kein actionListener für Dummy Eintrag
			
			
			/* Spezielle Mouse-Listener sind nicht notwendig, aber moeglich !!!
			menueeintrag.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent me){
					System.out.println("mausi released");
				}
				public void mousePressed(MouseEvent me){
					System.out.println("mausi pressed");
				}
				public void mouseClicked(MouseEvent me){
					System.out.println("mausi clicked");
				}
				public void mouseEntered(MouseEvent me){
					System.out.println("mausi entered");
				}				
				public void mouseExited(MouseEvent me){
					System.out.println("mausi exited");
				}
			});
			*/
			
		}//end main:
		return objReturn;
	}
}//END Class
