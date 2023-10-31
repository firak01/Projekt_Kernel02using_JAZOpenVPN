package use.openvpn.client.process;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL;
import use.openvpn.client.status.EventObject4ClientMainStatusLocalSetOVPN;
import use.openvpn.client.status.EventObject4ProcessMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.EventObject4VpnIpPingerStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.clientui.ClientTrayStatusMappedValueZZZ;
import use.openvpn.clientui.ClientTrayUIZZZ;
import use.openvpn.client.status.ISenderObjectStatusLocalSetUserOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventObject4ProcessWatchMonitorStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObject4VpnIpPingerStatusLocalSetOVPN;
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
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;

public class ClientThreadVpnIpPingerOVPN extends AbstractKernelUseObjectWithStatusZZZ implements IClientThreadVpnIpPingerOVPN, Runnable,IListenerObjectStatusLocalSetOVPN, IEventBrokerStatusLocalSetUserOVPN{
	private IClientMainOVPN objMain = null;
	private ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	//IClientThreadVpnIpPingerOVPN.STATUSLOCAL
	
	
public ClientThreadVpnIpPingerOVPN(IKernelZZZ objKernel, ClientMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
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
				try {
					check:{
						if(this.objMain==null) break main;
					}//END check:
				   	
					//######### 20230826 Verschoben aus ClientMainOVPN.start(), durch das Aufteilen sind mehrere Prozesse parallel moeglich.					
	 				//+++ Noch keine Verbindung/Noch fehlende Verbindungen, dann wird es aber Zeit verschiedene Threads damit zu beauftragen
	 				String sLog = ReflectCodeZZZ.getPositionCurrent()+": Trying to establish a new connection with every OVPN-configuration-file. Starting threads.";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);	
	 				
					//NUN DAS BACKEND-AUFRUFEN. Merke, dass muss in einem eigenen Thread geschehen, damit das Icon anclickbar bleibt.								
					//Merke: Wenn über das enum der setStatusLocal gemacht wird, dann kann über das enum auch weiteres uebergeben werden. Z.B. StatusMeldungen.				
					//besser ueber eine geworfenen Event... und nicht direkt: this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTING, true);
					this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTARTING, true);
					System.out.println("... Starte...");
					do {			 								
						//B) Pingen der gewuenschten Zieladressen hinsichtlich der Erreichbarkeit VORBEREITEN	 													
						//Verwende nicht das File-Objekt, sondern das Konfigurations-Objekt.
						ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = this.getMainObject().getClientConfigStarterRunningList();
						if(listaClientConfigStarterRunning==null) {
							sLog = ReflectCodeZZZ.getPositionCurrent()+": Keine gestarteten Konfigurationen aus OVPN-configuration-file vorhanden. Breche ab.";
							System.out.println(sLog);
							this.getMainObject().logProtocolString(sLog);
							break main;
						}
						
						for(int icount3=0; icount3 < listaClientConfigStarterRunning.size(); icount3++){
							//TODOGOON20231005;//Nun die Verbindungen anpingen.
							Thread.sleep(10000);							
							System.out.println("..."+(icount3+1)+". Mache neue Verbindung...");							
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTNEW, true);
							
							Thread.sleep(10000);														
							System.out.println("..."+(icount3+1)+". Verbinde...");
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTING, true);
							
							Thread.sleep(10000);							
							System.out.println("..."+(icount3+1)+". Verbunden");							
							this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISCONNECTED, true);
						
							Thread.sleep(10000);							
							System.out.println("..."+(icount3+1)+". Beende...");																				
						}//END For

						break;
					}while(true);	
					this.setStatusLocal(IClientThreadVpnIpPingerOVPN.STATUSLOCAL.ISSTOPPED, true);
					
			
			//##################################

	 					//### Aus clientMonitorRunner... vom Ende
	 						//########################################################################
	 						//TODOGOON20230827;//C) Das eigentliche Pingen mit dem OVPNConnectionWatchRunnerZZZ in einen eigenen Menüpunkt unterbringen.
	 		 								//also was steht unter 
	 		 								//bConnected = objMain.getStatusLocal(ClientMainOVPN.STATUSLOCAL.ISCONNECTED);
	 		 								//if(bConnected){											
	 							            //darin das Pruefen auf Erreichbarkeit einbauen...
	 		 								
//	 						//Das Pr�fen auf Erreichbarkeit
//	 						//In this.scanVPNIPFirstEstablished wird schon eine Schleife durchgef�hrt......     for(int icount3=0; icount3 < listaFileNotFinished.size(); icount3++){	
//	 							//1. Diese ArrayList der StarterObjecte nun hinsichtlich der VPN-IP-Erreichbarkeit scannen.
//	 							this.objMain.logMessageString("Checking success. Pinging all not jet finished configurations for the configured vpn-ip."); //DEN PORT ZU PINGEN IST QUATSCH  + ":" + objStarter.getVpnPort();					
//	 							String sIP = this.scanVpnIpFirstEstablished(listaFileNotFinished);
//	 							
//	 							//2.Falls eine der konfigurierten Adressen erreichbar ist: Flag "Connected" setzen. Alle anderen Processe zum Verbindungsaufbau stoppen.
//	 							//TODO: Sollen alle Verbindungen aufgebaut werden, dann lediglich aus der Liste herausnehmen. Nat�rlich daf�r sorgen, dass das Frontend �ber die neue VPN M�glichkeit informiert wird. 
//	 							if(sIP!=null){
//	 								((ClientApplicationOVPN)this.getApplicationObject()).setVpnIpEstablished(sIP);  //Wichtig: Die erreichbare IP - Adresse f�r das Frontend greifbar machen.
//	 								//this.sPortVPN = objStarter.getVpnPort();	
//	 								this.logMessageString( "Connection successfully established with '"+ ((ClientApplicationOVPN)this.getApplicationObject()).getVpnIpEstablished() +"'"); //Der Port ist nicht aussagekr�ftig !!! + ":" + this.getVpnPortEstablished() + "'";)					
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
//	 												//TODO GOON: VERSUCHE DEN STEUERCODE F�R "BEENDEN" ZU SENDEN, DAS GEHT ABER NOCH NICHT
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
	 								
//######## 
//Aus ClientMainOVPN vom Ende				
	 								
//	 								///##########################################################
//	 								//Sollen die Ports der Gegenseite "gescannt" werden ?
//	 						if(this.isPortScanEnabled()==true){
//	 								
//	 								//Nun die zur Verf�gung stehenden Ports erfassen	
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
//	 							//Merke: Das Frontend wird nun ggf. einen Thread starten, der die Verbindung �berwacht
//	 									this.setFlag("PortScanAllFinished", true);	
//	 								}//END if(this.isPortScanEnabled()){
	 					
	 			
	 									 
				 //}//END if "ConnectionRunnerStarted"
				 
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			//}while(true);
			
			
			} catch (InterruptedException e) {
				ExceptionZZZ ez = new ExceptionZZZ(e);
				throw ez;
			}
		}catch(ExceptionZZZ ez){
			System.out.println(ez.getDetailAllLast());
		}
		}//END main:
		
	}//END run
	
	
	@Override
	public void setMainObject(IClientMainOVPN objClientBackend){
		this.objMain = (ClientMainOVPN) objClientBackend;
	}
	
	@Override
	public IClientMainOVPN getMainObject(){
		return this.objMain;
	}
	
	public boolean isStatusChanged(String sStatusString) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(sStatusString == null) {
				bReturn = this.getStatusLocalString()==null;
				break main;
			}
			
			if(!sStatusString.equals(this.getStatusLocalString())) {
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

	//aus IListenerObjectStatusLocalSetZZZ
	@Override
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{		
			//Falls nicht zuständig, mache nix
		    boolean bProof = this.isEventRelevant(eventStatusLocalSet);
			if(!bProof) break main;
		    
			//Lies den Status (geworfen vom Backend aus)
			String sStatus = eventStatusLocalSet.getStatusText();
		
			//übernimm den Status
			this.setStatusLocalString(sStatus);
		
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isEventRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isEventRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
			bReturn = this.isStatusChanged(eventStatusLocalSet.getStatusText());
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status nicht geaendert. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				break main;
			}
						
			bReturn = this.isEventRelevantByStatusLocalValue(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Statuswert nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByStatusLocal(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Status an sich aus dem Event ist fuer diese Klasse nicht relevant. Breche ab.";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);				
				break main;
			}
			
			bReturn = this.isEventRelevantByClass(eventStatusLocalSet);
			if(!bReturn) {
				sLog = ReflectCodeZZZ.getPositionCurrent()+": Event werfenden Klasse ist fuer diese Klasse hinsichtlich eines Status nicht relevant. Breche ab.";
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
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(eventStatusLocalSet==null)break main;
			
			boolean bStatusValue = eventStatusLocalSet.getStatusValue();
			if(bStatusValue==false)break main; //Hier interessieren nur "true" werte, die also etwas neues setzen.
			
			bReturn = true;
		}
		return bReturn;
	}
	
	@Override
	public boolean isEventRelevantByClass(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
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
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet)	throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			IEnumSetMappedZZZ enumStatus = eventStatusLocalSet.getStatusEnum();							
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

	@Override
	public boolean getFlag(use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag,	boolean bFlagValue) throws ExceptionZZZ {
		return this.setFlag(objEnumFlag.name(), bFlagValue);
	}

	@Override
	public boolean[] setFlag(IClientThreadVpnIpPingerOVPN.FLAGZ[] objaEnumFlag,	boolean bFlagValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
				baReturn = new boolean[objaEnumFlag.length];
				int iCounter=-1;
				for(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
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
	public boolean proofFlagExists(IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag)throws ExceptionZZZ {
		return this.proofFlagExists(objEnumFlag.name());
	}

	@Override
	public boolean proofFlagSetBefore(use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.FLAGZ objEnumFlag)throws ExceptionZZZ {
		return this.proofFlagSetBefore(objEnumFlag.name());
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
					IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
					
					bFunction = this.setStatusLocal(enumStatus, null, bStatusValue);
				}//end main:
				return bFunction;
			}
			
			@Override 
			public boolean setStatusLocal(Enum enumStatusIn, int iIndex, boolean bStatusValue) throws ExceptionZZZ {
				boolean bFunction = false;
				main:{
					if(enumStatusIn==null) {
						break main;
					}
					IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
					
					bFunction = this.setStatusLocal(enumStatus, iIndex, null, bStatusValue);
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
					IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
					
					bFunction = this.setStatusLocal(enumStatus, -1, sStatusMessage, bStatusValue);
				}//end main:
				return bFunction;
			}
			
			@Override
			public boolean setStatusLocal(Enum enumStatusIn, int iIndex, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
				boolean bFunction = false;
				main:{
					if(enumStatusIn==null) {
						break main;
					}
				//return this.getStatusLocal(objEnumStatus.name());
				//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum
				
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL) enumStatusIn;
				String sStatusName = enumStatus.name();
				bFunction = this.proofStatusLocalExists(sStatusName);															
				if(!bFunction){
					String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger would like to fire event, but this status is not available: '" + sStatusName + "'";
					System.out.println(sLog);
					this.logLineDate(sLog);				
					break main;
				}
					
				bFunction = this.proofStatusLocalChanged(sStatusName, bStatusValue);
				if(!bFunction) break main;
				
				//Setze das Flag nun in die HashMap
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
				
				//Setze nun das Enum, und damit auch die Default-StatusMessage
				this.setStatusLocalEnum(enumStatus);
				String sStatusMessageToSet = enumStatus.getStatusMessage();
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
				System.out.println(sLog);
				this.getMainObject().logProtocolString(sLog);
				
				//Falls eine Message extra uebergeben worden ist, ueberschreibe...
				if(sStatusMessage!=null) {
					sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientMain uebersteuere sStatusMessageToSet='" + sStatusMessage + "'";
					System.out.println(sLog);
					this.getMainObject().logProtocolString(sLog);					
					this.setStatusLocalMessage(sStatusMessage);
				}
				
				
				//....hier keine Verarbeitung der Startkonfiguration
				
				
				
				
				
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.	
				if(this.getSenderStatusLocalUsed()==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadProcessWatchMonitor for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
					System.out.println(sLog);
					this.logLineDate(sLog);			
					break main;
				}
				
				//Merke: Nun aber ueber das enum, in dem ja noch viel mehr Informationen stecken können.
				IEventObject4VpnIpPingerStatusLocalSetOVPN event = new EventObject4VpnIpPingerStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);
				event.setApplicationObjectUsed(this.getMainObject().getApplicationObject());
				//Kein besonderes Mapping ... if(sStatusName.equalsIgnoreCase(IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL.STATUSLOCAL.ISSTARTING.getName())){
							
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger fires event '" + enumStatus.getAbbreviation() + "'";
				System.out.println(sLog);
				this.logLineDate(sLog);
				
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger for Process iIndex= '" + iIndex + "'";
				System.out.println(sLog);
				this.logLineDate(sLog);
				if(iIndex>=0) {
					event.setClientConfigStarterObjectUsed(this.getMainObject().getClientConfigStarterList().get(iIndex));
				}
				
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ClientThreadVpnIpPinger fires event '" + enumStatus.getAbbreviation() + "'";
				System.out.println(sLog);
				this.logLineDate(sLog);
				this.getSenderStatusLocalUsed().fireEvent(event);
						
				bFunction = true;	
			}	// end main:
			return bFunction;
			}

	//################################
			@Override 
			public String getStatusLocalMessage() {
				String sReturn = null;
				main:{
					if(this.sStatusLocalMessage!=null) {
						sReturn =  this.sStatusLocalMessage;
						break main;				
					}
					
					//Merke: Erst in OVPN-Klassen gibt es enum mit Message
					IClientThreadVpnIpPingerOVPN.STATUSLOCAL objEnum = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL)this.getStatusLocalEnum();
					if(objEnum!=null) {
						sReturn = objEnum.getStatusMessage();
					}			
				}//end main:
				return sReturn;
			}

			@Override
			public String getStatusLocalMessagePrevious(){
				String sReturn = null;
				main:{
					if(this.sStatusLocalMessage!=null) {
						sReturn =  this.sStatusLocalMessage;
						break main;				
					}
					
					//Merke: Erst in OVPN-Klassen gibt es enum mit Message
					IClientThreadVpnIpPingerOVPN.STATUSLOCAL objEnum = (IClientThreadVpnIpPingerOVPN.STATUSLOCAL)this.getStatusLocalEnumPrevious();
					if(objEnum!=null) {
						sReturn = objEnum.getStatusMessage();
					}			
				}//end main:
				return sReturn;
			}
			
			
	@Override
	public boolean isStatusLocalRelevant(IEnumSetMappedZZZ objEnumStatusIn) throws ExceptionZZZ {
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
			ClientThreadVpnIpPingerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
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
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);;
	}
}//END class
