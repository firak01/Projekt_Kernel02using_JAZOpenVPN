package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import use.openvpn.AbstractMainOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.OVPNFileFilterConfigZZZ;
import use.openvpn.ProcessWatchRunnerZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelPortScanHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import basic.zKernel.KernelZZZ;

/**This class is used as a backend worker.
 * For frontend features, use ConfigMainUIZZZ.
 * There are some methods to read the current status.
 * @author 0823
 *
 */
public class ClientMainZZZ extends AbstractMainOVPN{	
//private ClientApplicationOVPN objApplication = null;//Objekt, dass Werte, z.B. aus der Kernelkonfiguration holt/speichert
//private ConfigChooserOVPN objConfigChooser = null;   //Objekt, dass Templates "verwaltet"
//private ClientConfigMapperOVPN objConfigMapper = null; //Objekt, dass ein Mapping zu passenden Templatezeilen verwaltet.

private ClientConfigFileZZZ objFileConfigReached = null;

/*STEHEN LASSEN: DIE PROBLEMATIK IST, DAS NICHT NACHVOLLZIEHBAR IST, �BER WELCHEN PORT DIE VPN-VERBINDUNG HERGESTELLT WURDE 
 * Zumindest nicht PER PING-BEFEHL !!!
private String sPortVPN = null;
*/

private boolean bFlagUseProxy = false;
private boolean bFlagIsConnected = false;
private boolean bFlagHasError = false;
private boolean bFlagPortScanAllFinished = false;
	

	
	public ClientMainZZZ(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,saFlagControl);		
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
			connmain:{
			//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 			
			this.logStatusString("Searching for configuration template files 'Template*.ovpn'"); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			IKernelZZZ objKernel = this.getKernelObject();	
			
			ClientApplicationOVPN objApplication = new ClientApplicationOVPN(objKernel, this);
			this.setApplicationObject(objApplication);
			
			ConfigChooserOVPN objChooser = new ConfigChooserOVPN(objKernel,"client");
			this.setConfigChooserObject(objChooser);
						
			//Die Konfigurations-Template Dateien finden		
			File[] objaFileConfigTemplate = objChooser.findFileConfigOvpnTemplates();
			if(objaFileConfigTemplate==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(objaFileConfigTemplate.length == 0){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				this.logStatusString(objaFileConfigTemplate.length + " configuration TEMPLATE file(s) was (were) found in the directory: '" + objChooser.readDirectoryConfigPath() + "'");  //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			}
			
//			### 2. Voraussetzung: Web-Seite konfiguriert, auf der die dynamische IP vorhanden ist.
			//Zur Web-Seite verbinden, dazu den KernelReaderURL verwenden und zun�chst initialisieren.
			this.logStatusString("Reading configured url to parse for ip-adress."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.			
			if(((ClientApplicationOVPN)this.getApplicationObject()).getURL2Parse()==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_CONFIGURATION_MISSING+"URL String", iERROR_CONFIGURATION_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;		
			}else{
				this.logStatusString("URL to read IP from is configured as: '" + ((ClientApplicationOVPN)this.getApplicationObject()).getURL2Parse() + "'");
			}
			
						
//			###3. Voraussetzung: Auf der konfigurierten Web-Seite muss eine IP-Adresse auszulesen sein
			this.logStatusString("Parsing IP-adress from URL."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.			
			if(((ClientApplicationOVPN)this.getApplicationObject()).getIpRemote()==null){//Dabei wird auch der Proxy eingestellt.
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to receive new IP-adress.", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				this.logStatusString("New IP-adress received: " + ((ClientApplicationOVPN)this.getApplicationObject()).getIpRemote());			
			}
			
			
			//####################################################################
			//### DAS SCHREIBEN DER NEUEN KONFIGURATION
					
			//+++ A) Vorbereitung
			///*TEST: Path Expanded Berechnung
			//+++ 1. Die früher mal verwendeten Dateien entfernen
			this.logStatusString("Removing former configuration file(s)."); //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
			File[] objaFileConfigUsed = objChooser.findFileConfigUsed();
			if(objaFileConfigUsed==null){
				this.logStatusString("No previously used file was found (null case). Nothing removed.");
			}else if(objaFileConfigUsed.length==0){
				this.logStatusString("No previously used file was found (0 case). Nothing removed.");			
			}else{
				this.logStatusString("Trying to remove previously used file(s): " + objaFileConfigUsed.length);
				for(int icount = 0; icount < objaFileConfigUsed.length; icount++){
					boolean btemp = objaFileConfigUsed[icount].delete();
					if(btemp==true){
						this.logStatusString( "File successfully removed: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}else{
						this.logStatusString("Unable to remove file: '" + objaFileConfigUsed[icount].getPath()+"'");						
					}					
				}//END for
			}
			//*/
			
			
						
			//+++ B) Die gefundenen Werte überall eintragen: IN neue Dateien
			this.logStatusString("Creating new configuration-file(s) from template-file(s), using new line(s)");									
			ArrayList listaFileUsed = new ArrayList(objaFileConfigTemplate.length);
			for(int icount = 0; icount < objaFileConfigTemplate.length; icount++){
				File fileTemplateOvpnUsed = objaFileConfigTemplate[icount];
				
				ClientConfigMapper4TemplateOVPN objMapper = new ClientConfigMapper4TemplateOVPN(objKernel, this, fileTemplateOvpnUsed);
				this.setConfigMapperObject(objMapper);
				
				//Mit dem Setzen der neuen Datei, basierend auf dem Datei-Template wird intern ein Parser f�r das Datei-Template aktiviert
				ClientConfigTemplateUpdaterZZZ objUpdater = new ClientConfigTemplateUpdaterZZZ(objKernel, this, objChooser, objMapper, null);				
				File objFileNew = objUpdater.refreshFileUsed(fileTemplateOvpnUsed);					
				if(objFileNew==null){
					this.logStatusString("Unable to create 'used file' file base on template template: '" + objaFileConfigTemplate[icount].getPath() + "'");					
				}else{
					boolean btemp = objUpdater.update(objFileNew, true); //Bei false werden also auch Zeilen automatisch hinzugef�gt, die nicht im Template sind. Z.B. Proxy-Einstellungen.
					if(btemp==true){
						this.logStatusString( "'Used file' successfully created for template: '" + objaFileConfigTemplate[icount].getPath() + "'");
		
						//+++ Nun dieses used-file dem Array hinzuf�gen, dass f�r den Start der OVPN-Verbindung verwendet wird.
						listaFileUsed.add(objUpdater.getFileUsed());
					}else{
						this.logStatusString( "'Used file' not processed, based upon: '" + objaFileConfigTemplate[icount].getPath() + "'");					
					}																						
				}
			}//end for
			
			
			//##########################################################################################
			//+++ Die neuen OVPN-Verbindungsfiles zum Starten der VPN-Verbindung verwenden !!!
			//Diese werden in unabh�ngigen Threads "gemonitored"
			
			/*TODO WARUM GEHT DAS NICHT. Kl�ren, ob Firewall oder Proxy-Einstellungen bei mir oder bei der itelligence das verhindern !!!
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
					
					//DIESE KONFIGURATION DEMN�CHST ENTFERNEN
					listaPos.add(new Integer(icount));					
				}
			}//END for
			
			//TODO DAS ENTFERNEN AUS DER ARRAYLIST ALS KERNEL-STATIC-METHODE ANBIETEN !!!
			//NUN DAS TATS�CHLICHE ENTFERNEN, Von hinten nach vorne und dann immer um 1 abnehmend.
			if(listaPos.isEmpty()==false){
				for(int icount=listaPos.size()-1;icount >=0;icount--){
				Integer inttemp = (Integer)listaPos.get(icount);
				int itemp = inttemp.intValue();
				listaFileUsed.remove(itemp);
				}//END for
			}
			*/
			
			//Gibt es �berhaupt eine "mögliche" Konfiguration  ???
			if(listaFileUsed.isEmpty()){
				this.logStatusString("No valid remote connection available. Quitting.");
			
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusStringCurrent(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}
			
			
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++ VPN schon verbunden ? Dies checken .....
			//TODO DAS CHECKEN DER VERBINDUNGEN IN THREAD AUSLAGERN UND SO PARALLESISIEREN
			//         L�sungsansatz: Neue Klasse PingCollectionZZZ(...), in der dann PingZZZ in verschiedenen Threads ausgef�hrt wird		
			//                                Neue Methode in ConfigMainZZZ: isAnyTargetReachable(...) 
			this.logStatusString("Checking if any OVPN connection is still established.");
			String sVpnIp = this.scanVpnIpFirstEstablished(listaFileUsed);
			if(sVpnIp!=null){
				((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpEstablished(sVpnIp);
				
				//DAS IST NICHT AUSSAGEKR�FTIG. DIE VPN-VERBINDUNG KANN �BER EINEN GANZ ANDEREN PORT HERGESTELLT WORDEN SEIN !!! this.sPortVPN = objStarter.getVpnPort();	
				this.logStatusString("A connection with an VPN target ip is still established: " + ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished() + ". Quitting."); //+ ":" + this.getVpnPortEstablished() + ". Quitting.")
				
				//NEU: HERAUSFINDEN, �BER WELCHEN PORT DIE VERBINDUNG ERSTELLT WORDEN IST.
				//TODO Das ist technisch, hinter einer Firewall, nicht so einfach zu realisieren.
				
				//Momentan wird das noch als Fehler ausgegeben.
				//TODO Ggf. sollte diese IP dann lediglich aus der Lister der aufzubauenden VPN-Verbindungen herausgenommen werden. 
				//            Diese IP sollte dann dem Frontend als eine vorhandene Verbindung mitgeteilt werden, obwohl z.B. der Status noch auf "Verbiinden" steht.
				//            Die noch unverbundenen VPN-Verbindungen sollten dann versucht werden zu verbinden (was aber nur mit einem Timeout sinnvoll erscheint, sonst bekommt man ggf. nie das Frontend auf den "gr�nen"-Status.
				ExceptionZZZ ez = new ExceptionZZZ(this.getStatusStringCurrent(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;	
			}else{
				this.logStatusString("No connection with OVPN is established, till now.");//DER PORT IST NICHT AUSSAGEKR�FTIG + ":" + objStarter.getVpnPort();							
			}
			
		
			
						
			//+++ AUFBAUEN DER LISTE DER ZU STARTENDEN KONFIGURATIONEN, dabei wieder von den Files ausgehen.
			//TODO: Ggf. diejenigen Verbindungen herausnehmen, die schon konfiguriert sind. 
			ArrayList listaStarter = new ArrayList(listaFileUsed.size());			//Hier sollen diejenigen rein, die dann versucht werden sollen herzustellen.
			for(int icount = 0; icount < listaFileUsed.size(); icount++){
				File objFileConfig2start = (File) listaFileUsed.get(icount);
				ClientConfigStarterOVPN objStarter = new ClientConfigStarterOVPN(objKernel, this, objFileConfig2start, null);
				listaStarter.add(icount, objStarter);					
			}//END For
					
			//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
			this.logStatusString("Trying to establish a new connection with every OVPN-configuration-file. Starting threads.");
		
			//Starten der Threads, mit denen die OVPN-Processe gestartet werden sollen
			Thread[] threadaOVPN = new Thread[listaStarter.size()];
			ProcessWatchRunnerZZZ[] runneraOVPN = new ProcessWatchRunnerZZZ[listaStarter.size()];	
			int iNumberOfProcessStarted = 0;
			for(int icount = 0; icount < listaStarter.size(); icount++){	
				ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN)listaStarter.get(icount);
				Process objProcess = objStarter.requestStart();
				if(objProcess==null){
					//Hier nicht abbrechen, sondern die Verarbeitung bei der n�chsten Datei fortf�hren
					this.logStatusString( "Unable to create process, using file: '"+ objStarter.getFileConfigOvpn().getPath()+"'");
				}else{	
					
					//NEU: Einen anderen Thread zum "Monitoren" des Inputstreams des Processes verwenden. Dadurch werden die anderen Prozesse nicht angehalten.
					 runneraOVPN[icount] =new ProcessWatchRunnerZZZ(objKernel, objProcess,icount, null);
					 threadaOVPN[icount] = new Thread(runneraOVPN[icount]);					
					 threadaOVPN[icount].start();
					 iNumberOfProcessStarted++;	
					//Das bl�ht das Log unn�tig auf .... this.logStatusString("Finished starting thread # " + icount + " for establishing connection.");
				}				
			}//END for
			this.logStatusString("Finished starting " + iNumberOfProcessStarted + " threads for establishing connection.");
			
			//###############################################################################
			//+++ Monitoren der Threads, die versuchen eine Verbindung aufzubauen.
			//ENDLOSSCHLEIFE: DIE ANDEREN THREAD LAUFEN JA NOCH !!!
			//TODO: Timeout f�r threads programmieren, die sich einfach nicht beenden wollen.
			//TODO GOON: Die Thread-Objekte und das Monitor-Objekt in die Config-Starter-Klasse integrieren.
			ArrayList listaIntegerFinished = new ArrayList();
			boolean[] baRunnerOVPNEndedMessage = new boolean[listaStarter.size()];  //Hier werden die beendeten Procese vermerkt.					
			for(int icount=0; icount < listaStarter.size(); icount++){     
				baRunnerOVPNEndedMessage[icount]=false;
			}
			do{		
					//#########################################################
				    //A) Beobachten der Threads, mit denen OVPN-gestartet werden soll					
						for(int icount = 0; icount < runneraOVPN.length; icount++){
							ProcessWatchRunnerZZZ runnerOVPN = runneraOVPN[icount];
							if(runnerOVPN == null){
								if(baRunnerOVPNEndedMessage[icount] !=false){ //Ziel: Unn�tigen Output vermeiden
									//+++ Die Runner, die beendet worden sind und einen Fehler zur�ckgemeldet haben vermerken. Die brauchen dann ja nicht mehr angepingt zu werden.
									this.logStatusString("Runner # " + icount + " was set to  null.");
									baRunnerOVPNEndedMessage[icount] = true;
								}
							}else{								
								if(runnerOVPN.getFlag("hasError")==true && runnerOVPN.bEnded == true){
//									+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aud der Liste der anzupingenden ips) herausnehmen, die auf einen Fehler gelaufen sind
									this.logStatusString("Thread # " + icount + " could not create a connection. Ending thread with ERROR reported. For more details look at the log file.");
						
									ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN) listaStarter.get(icount);
									if(objStarter.isProcessAlive()==true) objStarter.requestStop(); //Den Prozess beenden								
															
									threadaOVPN[icount].interrupt();
									runneraOVPN[icount]=null;
									
									Integer intTemp = new Integer(icount);
									listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
								}else if(runnerOVPN.getFlag("hasError")==false && runnerOVPN.bEnded == true){
//									//+++ Diejenigen Processe aus den zu verarbeitenden (und wichtig: aud der Liste der anzupingenden ips) herausnehmen, die einfach nur so beendet worden sind
									//       Merke: Falls ein openvpn.exe die connection geschaft hat, wird dieser auf jeden Fall nicht beendet.
									this.logStatusString("Thread # " + icount + " could not create a connection. Ending thread. For more details look at the log file.");
									
									ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN) listaStarter.get(icount);
									if(objStarter.isProcessAlive()==true) objStarter.requestStop(); //Den Prozess beenden								
															
									threadaOVPN[icount].interrupt();
									runneraOVPN[icount]=null;
									
									Integer intTemp = new Integer(icount);
									listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
								}else{
									//Das bl�ht das Log unn�tig auf .... this.logStatusString("Thread # " + icount + " not jet ended or has reported an error.");									
								}
							}//END if (runnnerOVPN==null
						}//END for
						
						//########################################################################
						//B) Pingen der gew�nschten Zieladressen hinsichtlich der Erreichbarkeit
						
						//Erstellen der Arraylist, die zu Pingen ist. D.h.  von den listaStarter die Positionen die in listaIntegerRemoved drinstehen abziehen.
						ArrayList listaFileNotFinished = new ArrayList();
						for(int icount3=0; icount3 < listaStarter.size(); icount3++){	
							//Diejenigen Starter, die schon vorzeitig beendet wurden hier herausfiltern. Den Rest anpingen
							Integer intTemp = new Integer(icount3);
							if(!listaIntegerFinished.contains(intTemp)){			
								File objFileConfig = (File) listaFileUsed.get(icount3);							
								listaFileNotFinished.add(objFileConfig);
							}//END if
						}//END For
						
						//Das Pr�fen auf Erreichbarkeit
						//In this.scanVPNIPFirstEstablished wird schon eine Schleife durchgef�hrt......     for(int icount3=0; icount3 < listaFileNotFinished.size(); icount3++){	
							//1. Diese ArrayList der StarterObjecte nun hinsichtlich der VPN-IP-Erreichbarkeit scannen.
							this.logStatusString("Checking success. Pinging all not jet finished configurations for the configured vpn-ip."); //DEN PORT ZU PINGEN IST QUATSCH  + ":" + objStarter.getVpnPort();					
							String sIP = this.scanVpnIpFirstEstablished(listaFileNotFinished);
							
							//2.Falls eine der konfigurierten Adressen erreichbar ist: Flag "Connected" setzen. Alle anderen Processe zum Verbindungsaufbau stoppen.
							//TODO: Sollen alle Verbindungen aufgebaut werden, dann lediglich aus der Liste herausnehmen. Nat�rlich daf�r sorgen, dass das Frontend �ber die neue VPN M�glichkeit informiert wird. 
							if(sIP!=null){
								((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpEstablished(sIP);  //Wichtig: Die erreichbare IP - Adresse f�r das Frontend greifbar machen.
								//this.sPortVPN = objStarter.getVpnPort();	
								this.logStatusString( "Connection successfully established with '"+ ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished() +"'"); //Der Port ist nicht aussagekr�ftig !!! + ":" + this.getVpnPortEstablished() + "'";)					
								bReturn = true;					
								this.setFlag("isconnected", bReturn);  //DAS SOLL DANN z.B: dem Frontend sagen, dass die Verbindung steht.
								
								
								//Nun diejenigen Threads beenden, die ungleich der Gefundenen Konfigurationsdatei sind
								ClientConfigFileZZZ objFileConfig = this.getFileConfigReached();
								if(objFileConfig!=null){
									File objFile = objFileConfig.getFileConfig();
									String sPath = objFile.getPath();
									
									//Alle noch nicht beendeten Prozesse beenden, AUSSER dort ist die gefundene Konfiguration verwendet.
									for(int icount2=0;icount2<runneraOVPN.length; icount2++){
										Integer intTemp = new Integer(icount2);
										if(!listaIntegerFinished.contains(intTemp)){
											
											ClientConfigStarterOVPN objStarter = (ClientConfigStarterOVPN) listaStarter.get(icount2);
											File objFileStarter = objStarter.getFileConfigOvpn();
											String sPathStarter = objFileStarter.getPath();
											if(sPath.equalsIgnoreCase(sPathStarter) == false){											
												this.logStatusString("Requesting to end thread # " + icount2);							
													
												//TODO GOON: VERSUCHE DEN STEUERCODE F�R "BEENDEN" ZU SENDEN, DAS GEHT ABER NOCH NICHT
												runneraOVPN[icount2].sendStringToProcess("hard");   //???????
												runneraOVPN[icount2].setFlag("StopRequested", true); // DAMIT WIRD DAS RUNNER OBJEKT ANGEHALTEN SICH SELBST ZU BEENDEN
																																		
												objStarter.requestStop(); //!!! TODO GOON: Ich schaffe es nicht den Process zu beenden !!! Darum werden z.B. beim Beenden des Frontend-Clients alle prozesse desn Namens "openvpn.exe" beendet. 
												threadaOVPN[icount2].interrupt();
												//runneraOVPN[icount2]=null;
													
												listaIntegerFinished.add(intTemp);						//Festhalten, welche der Positionen entfernt werden soll
											}//END 	if(sPath.equalsIgnoreCase(sPathStarter) == false){
										}//END if
									}//End for
								}		
								//5. Endlosschleife erfolgreich verlassen. Nicht main: verlassen, da ist noch viel zu tun !!!							
								break connmain;
							}else{
								String stemp = "";
								for(int icount2=0; icount2 < listaFileNotFinished.size(); icount2++){
									File objFileConfig = (File) listaFileNotFinished.get(icount2);
									if(stemp.equals("")){
										stemp = objFileConfig.getPath();
									}else{
										stemp = stemp + "; " + objFileConfig.getPath();
									}
								}								
								this.logStatusString( "No connection estblished till now based upon the file(s) '"+ stemp +"'");																				
							}//END if sIP != null																
					//}//END for
			}while(true);			
		}//END connmainmain;
		
		
		
		///##########################################################
		//Sollen die Ports der Gegenseite "gescannt" werden ?
if(this.isPortScanEnabled()==true){
		
		//Nun die zur Verf�gung stehenden Ports erfassen	
		//1. VPN-Ports
		IKernelConfigSectionEntryZZZ entryPortLow=objKernel.getParameterByProgramAlias("OVPN","ProgPortScan","VPNPortLow");
		String sPortLow=entryPortLow.getValue();
		IKernelConfigSectionEntryZZZ entryPortHigh=objKernel.getParameterByProgramAlias("OVPN", "ProgPortScan", "VPNPortHigh");
		String sPortHigh = entryPortHigh.getValue();
		if(sPortLow!=null && sPortHigh != null){ 
			//1. VPN-Ports
			this.logStatusString( "Scanning ports on VPN-IP-Adress: " +  ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished());	
			this.scanVpnPortAll(((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished(), sPortLow, sPortHigh);
			this.logStatusString( "VPN-IP-Port scan finished.");	
		}else{
			this.logStatusString( "VPN-IP-Port scan not properly configured: Ports missing.");	
		}
		
		
	//		2. Remote-Ports
			if(this.getFlag("useproxy")==true){
				((ClientApplicationOVPN)this.getApplicationObject()).setRemotePortScanned("Proxy/Firewall make port scan obsolete.");
			}else{
				this.logStatusString( "Scanning ports on Remote-IP-Adress: " +  ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished());	
				sPortLow=objKernel.getParameterByProgramAlias("OVPN","ProgPortScan","RemotePortLow").getValue();
				sPortHigh=objKernel.getParameterByProgramAlias("OVPN", "ProgPortScan", "RemoteProtHigh").getValue();
				if(sPortLow!=null && sPortHigh != null){  
					this.scanRemotePortAll(((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished(), sPortLow, sPortHigh);				
					this.logStatusString( "Remote-IP-Port scan finished.");	
				}else{
					this.logStatusString( "Remote-IP-Port scan not properly configured: Ports missing.");	
				}//END if(sRemotePortLow!=null && sRemotePortHigh != null){  			
			}//END if getFlag("useProxy")
		
	//########################################
	//Merke: Das Frontend wird nun ggf. einen Thread starten, der die Verbindung �berwacht
			this.setFlag("PortScanAllFinished", true);	
		}//END if(this.isPortScanEnabled()){
	}//END main
		if(bReturn == true){
			this.logStatusString( "Finished everything: 'Start successfull case'.");	
		}else{
			this.logStatusString( "Finished everything: 'Start not really successfull case'.");
		}
		
		return bReturn;
	}//END start()
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			this.start();
		} catch (ExceptionZZZ e) {
			this.setFlag("haserror", true);
			this.getKernelObject().getLogObject().WriteLineDate(e.getDetailAllLast());
		}
	}
	
	/**Scans the ports of the VPN-Ip in the given range.
	 * Updates the status.
	 * 
	 * @param sIP
	 * @param sPortLow
	 * @param sPortHigh
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 17.07.2006 - 16:27:00
	 */
	public boolean scanVpnPortAll(String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		return this.scanPortAll_("VPN", sIP, sPortLow, sPortHigh);
	}
	
	/**Scans the ports of the VPN-Ip in the given range.
	 * Updates the status.
	 * 
	 * @param sIP
	 * @param sPortLow
	 * @param sPortHigh
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 17.07.2006 - 16:27:00
	 */
	public boolean scanRemotePortAll(String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		return this.scanPortAll_("Remote", sIP, sPortLow, sPortHigh);
	}
	
	private boolean scanPortAll_(String sAlias, String sIP, String sPortLow, String sPortHigh) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{				
			}//END check:
				
		this.writePortStatusByAlias(sAlias, "Scan started ...");
		KernelPortScanHostZZZ objPortScan = new KernelPortScanHostZZZ(objKernel, sIP, null);
		objPortScan.setThreadStorageSize(200);
		
		int iLow = Integer.parseInt(sPortLow);
		int iHigh = Integer.parseInt(sPortHigh);
		
		int iHighTemp = 0;
		int iLowTemp = iLow;	
		String sPortScanTemp = "";
		int iPortScanCount=0;
		do{
//			Ziel: Statusansicht schneller aktualisieren. Daher nur 1000 Ports pro Durchlauf scannen
			iHighTemp = iLowTemp + 1000;
			if(iHighTemp>= iHigh){
				iHighTemp = iHigh;
			}
			this.getKernelObject().getLogObject().WriteLineDate("Scanning Ports on Host: " + sIP + " from :" + iLowTemp + " to: " + iHighTemp);				
			boolean btemp = objPortScan.scan(iLowTemp,iHighTemp); 
			if(btemp==true){
				ArrayList listaPortConnected = objPortScan.getPortConnected();
				if(listaPortConnected.isEmpty()==true){
					//System.out.println("No port found open.");
					//this.sPortVpnScanned = "No port found open.";
				}else{
	//				System.out.println( listaPortConnected.size() + " Ports found open on host: " + sHost);
					iPortScanCount = iPortScanCount + listaPortConnected.size();				
					Integer intPort = (Integer) listaPortConnected.get(0);
					if(sPortScanTemp.length()>= 1){
						sPortScanTemp = sPortScanTemp + "; " + intPort.toString(); //Das ist den den folgenden 1000 Ports, jeweils der 1. Wert.
					}else{
						sPortScanTemp = intPort.toString(); //Das ist dann der erste Wert
					}						
					for(int icount=1; icount < listaPortConnected.size(); icount++){
						intPort = (Integer) listaPortConnected.get(icount);
						//System.out.println(intPort.toString());
						sPortScanTemp = sPortScanTemp + "; " + intPort.toString() ;
					}
					this.getKernelObject().getLogObject().WriteLineDate("Open ports found on Host: " + sIP + " : " + sPortScanTemp);
					this.writePortStatusByAlias(sAlias, iPortScanCount + " Ports found open (" + sPortScanTemp + " ....");								
				}
			}else{
				this.writePortStatusByAlias(sAlias, "Unable to scan ports.");
			}
			iLowTemp = iLowTemp + 1001;				
		}while(iHighTemp <= iHigh);
		if(iPortScanCount==0){
			this.writePortStatusByAlias(sAlias, "No port found open.");
		}else{
			this.writePortStatusByAlias(sAlias, iPortScanCount + " Ports found open (" + sPortScanTemp + ")");
		}
		bReturn = true;
		}//End main:
		return bReturn;
	}
	
	private void writePortStatusByAlias(String sAlias, String sStatus){
		if(sAlias.equalsIgnoreCase("Remote")){
			((ClientApplicationOVPN)this.getApplicationObject()).setRemotePortScanned(sStatus);
		}else if(sAlias.equalsIgnoreCase("VPN")){
			((ClientApplicationOVPN)this.getApplicationObject()).setVpnPortScanned(sStatus);
		}
	}
	
	
	/**Scans the IP of the provided File-Objects
	 * @param listaFileUsed, an ArrayList, File-Objects. These Files must be open-vpn configuration files.
	 * @throws ExceptionZZZ, 
	 *
	 * @return String, the first IP found, which can be reached.
	 *
	 * javadoc created by: 0823, 13.07.2006 - 14:56:30
	 */
	public String scanVpnIpFirstEstablished(ArrayList listaFileUsed) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			check:{
				if(listaFileUsed==null) break main;
				if(listaFileUsed.isEmpty()) break main;
				 
				//Prüfen, ob es sich um File-Objekte handelt
				for (int icount = 0; icount < listaFileUsed.size(); icount++){
					File objFileConfig2start = null;				
					try{ //Falls eine Arraylist ohne File-Objekte �bergeben wird, dann hier den Fehler abfangen
						objFileConfig2start = (File) listaFileUsed.get(icount);
					}catch(Exception e){
						ExceptionZZZ ez = new ExceptionZZZ("Error on Element " + icount + " of the ArrayList. This should be a file object. Error reported: " + e.getMessage(), iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					//Prüfen, ob es sich auch immer um ein Configuration-File handelt.
					OVPNFileFilterConfigZZZ objFileFilter = new OVPNFileFilterConfigZZZ();
					boolean btemp = objFileFilter.accept(objFileConfig2start);
					if(btemp==false){
						ExceptionZZZ ez = new ExceptionZZZ("Error on Element " + icount + " of the ArrayList. This should be a ovpn configuration file. It is a file but OVPNFileFilterConfigZZZ.accept() reports 'false'.", iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}					
				}				 				
			}//END check
		
		//+++ Performanceverbesserung: Eine Art "Unique" �ber die ArrayList, daraus kommt dann die zu �berpr�fende Liste.
		//TODO: Dies als Kernel-Erweiturung f�r ArrayList zur Verf�gung stellen.
		ArrayList listaUnique = new ArrayList(listaFileUsed.size());   			
		for(int icount = 0; icount < listaFileUsed.size(); icount++){		
			File objFileConfig2start = (File) listaFileUsed.get(icount);		
			//ClientConfigStarterZZZ objStarter = new ClientConfigStarterZZZ(objKernel, objFileConfig2start, null);			
			//String sIP2Check = objStarter.getVpnIp();
			ClientConfigFileZZZ objClientFile = new ClientConfigFileZZZ(objKernel, objFileConfig2start, null);
			String sIP2Check = objClientFile.getVpnIpRemote();
			if(sIP2Check!=null){					
				if(!listaUnique.isEmpty()){
		
					//Liste der Configurationen durchsuchen und nur dann die neue Konfiguration hinzuf�gen, wenn die VPN-IP-Adresse unterschiedlich ist.
					for(int icount2 = 0; icount2 < listaUnique.size(); icount2++){
						//ClientConfigStarterZZZ objStarter2Check = (ClientConfigStarterZZZ) listaUnique.get(icount2);
						//String stemp = objStarter2Check.getVpnIp();
						File objFileConfig2Check = (File) listaUnique.get(icount2); 
						ClientConfigFileZZZ objClientFile2Check = new ClientConfigFileZZZ(objKernel, objFileConfig2Check, null);
						String stemp = objClientFile2Check.getVpnIpRemote();
						if(stemp!=null){
							if(!sIP2Check.equals(stemp)){
								//IP in die Lister der zu pr�fenden VPN-Verbindungen aufnehmen
								listaUnique.add(objFileConfig2Check);
								break; //Die innere For-Schleife verlassen. Wurde ja schon hinzugef�gt. Ergo zum n�chsten File gehen.
							}
						}
					}//END for "2"
				}else{
					//IP in die Lister der zu pr�fenden VPN-Verbindungen aufnehmen
					listaUnique.add(objFileConfig2start);
				}//END if isempty()
			}//END if sIP2Check!=null
		}//END for "1"
		
		//	Gibt es �berhaupt eine "m�gliche" Konfiguration  ???
		if(listaUnique.isEmpty()){
			this.logStatusString("No valid 'unique' remote connection availabe. Quitting.");
		
			ExceptionZZZ ez = new ExceptionZZZ(this.getStatusStringCurrent(), iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
			throw ez;	
		}
		
		
		//#############################################################################
		//+++ Pr�fen der Erreichbarkeit der VPN-Verbindung (NEU: auf fixen Port 80, bzw. was so konfiguriert wurde)
		String sVPNPort4Check = ((ClientApplicationOVPN)this.getApplicationObject()).readVpnPort2Check();		
		for(int icount=0;icount < listaUnique.size(); icount++){
			//ClientConfigStarterZZZ objStarter = (ClientConfigStarterZZZ) listaUnique.get(icount);
			File objFileConfig = (File) listaUnique.get(icount);
			ClientConfigFileZZZ objConfigFile = new ClientConfigFileZZZ(objKernel, objFileConfig, null);
			boolean bReachable = objConfigFile.isVpnReachable(sVPNPort4Check);
			if(bReachable==true){
				sReturn = objConfigFile.getVpnIpRemote();
				this.setFileConfigReached(objConfigFile);
				break main;
				
				//NEU: HERAUSFINDEN, �BER WELCHEN PORT DIE VERBINDUNG ERSTELLT WORDEN IST.
				//TODO Das ist technisch, hinter einer Firewall, nicht so einfach zu realisieren.
		
			}
		}
		}//End main:
		return sReturn;
	}
	
	
	
	
	
	
	
	
	

	public boolean isConnectOnStart() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
		//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enth�lt, die wie der Application - Key aussieht. 
		String stemp = this.objKernel.getParameter("ConnectOnStart").getValue();
		if(stemp==null) break main;
		if(stemp.equals("1")){
			bReturn = true;
		}
		}//END main
		return bReturn;
	}
	
	public boolean isPortScanEnabled() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			check:{
				if(this.objKernel==null) break main;				
			}//END check:
		
			//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enth�lt, die wie der Application - Key aussieht. 
			String sPortScanEnabled = objKernel.getParameter("PortScanEnabled").getValue();
			if(!StringZZZ.isEmpty(sPortScanEnabled)){
				if(sPortScanEnabled=="1"){
					bReturn = true;
				}//END if sPortScanEnabled=="1"
			}//END if(!StringZZZ.isEmpty(sPortScanEnabled)){			
		}//END main
		return bReturn;
	}	
	
	
	
	
//	######### GetFlags - Handled ##############################################
	/** (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used:<CR>
	-  isConnected
	- useProxy
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
			if(stemp.equals("isconnected")){
				bFunction = bFlagIsConnected;
				break main;
			}else if(stemp.equals("useproxy")){
				bFunction = bFlagUseProxy;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}else if(stemp.equals("portscanallfinished")){				
				bFunction = bFlagPortScanAllFinished;
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
	 * - useproxy
	 * - haserror
	 * - PortScanAllFinished //das ist zusammen mit "isconnected" das Zeichen f�r den ConnectionMonitor des Frontends, das er starten darf. Grund: Die PortScans f�hren ggf. zu timeouts.
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
	
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("isconnected")){
			bFlagIsConnected = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("useproxy")){
			bFlagUseProxy = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("haserror")){
			bFlagHasError = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("portscanallfinished")){
			bFlagPortScanAllFinished = bFlagValue;
			bFunction = true;
			break main;
		}
		}//end main:
		return bFunction;
	}


	
	//######################################################
	//### Getter / Setter
	public ClientConfigFileZZZ getFileConfigReached(){
		return this.objFileConfigReached;
	}
	public void setFileConfigReached(ClientConfigFileZZZ objFileConfig){
		this.objFileConfigReached = objFileConfig;
	}
	
//	public ConfigChooserOVPN getConfigChooserObject() {
//		return this.objConfigChooser;
//	}
//	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
//		this.objConfigChooser = objConfigChooser;
//	}
//	
//	public ClientConfigMapperOVPN getConfigMapperObject() {
//		return this.objConfigMapper;
//	}
//	public void setConfigMapperObject(ClientConfigMapperOVPN objConfigMapper) {
//		this.objConfigMapper = objConfigMapper;
//	}
//	
//	public ClientApplicationOVPN getApplicationObject() {
//		return this.objApplication;
//	}
//	public void setApplicationObject(ClientApplicationOVPN objApplication) {
//		this.objApplication = objApplication;
//	}
}//END class

