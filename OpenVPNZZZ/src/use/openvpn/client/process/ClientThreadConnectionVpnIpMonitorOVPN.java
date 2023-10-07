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

public class ClientThreadConnectionVpnIpMonitorOVPN extends KernelUseObjectZZZ implements Runnable,IListenerObjectStatusLocalSetOVPN, ISenderObjectStatusLocalSetUserOVPN{
	protected ClientMainOVPN objMain = null;
	
	protected String sWatchRunnerStatus = new String("");            //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	protected String sWatchRunnerStatusPrevious = new String("");    //den vorherigen Status festhalten, damit z.B. nicht immer wieder das Icon geholt wird.
	
	protected ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
public ClientThreadConnectionVpnIpMonitorOVPN(IKernelZZZ objKernel, ClientMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ConfigMonitorRunnerNew_(objConfig, saFlagControl);
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
				try {
					check:{
						if(this.objMain==null) break main;
					}//END check:
				   								
					do {			 								
						//B) Pingen der gewuenschten Zieladressen hinsichtlich der Erreichbarkeit VORBEREITEN	 													
						//Verwende nicht das File-Objekt, sondern das Konfigurations-Objekt.
						ArrayList<ClientConfigStarterOVPN> listaClientConfigStarterRunning = this.objMain.getClientConfigStarterRunningList();
						for(int icount3=0; icount3 < listaClientConfigStarterRunning.size(); icount3++){	
							
							System.out.println("..."+(icount3+1)+". Verbindung...");
							Thread.sleep(50000);
							
							//TODOGOON20231005;//Nun die Verbindungen anpingen.
							
						
						}//END For
						boolean bMonitorThreadStarted = this.objMain.setStatusLocal(ClientMainOVPN.STATUSLOCAL.WATCHRUNNERSTARTED, true);//Es wird ein Event gefeuert, an dem das ServerTrayUI-Objekt registriert wird und dann sich passend einstellen kann.
						
					}while(true);	
				
			
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
		boolean bReturn = false;
		main:{		
			//Falls nicht zuständig, mache nix
		    boolean bProof = this.isStatusLocalRelevant(eventStatusLocalSet);
			if(!bProof) break main;
		    
			//Lies den Status (geworfen vom Backend aus)
			String sStatus = eventStatusLocalSet.getStatusText();
		
			//übernimm den Status
			this.setStatusString(sStatus);
		
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN#isStatusLocalRelevant(use.openvpn.client.status.IEventObjectStatusLocalSetOVPN)
	 */
	@Override
	public boolean isStatusLocalRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		boolean bReturn = false;
		
		main:{
			String sAbr = eventStatusLocalSet.getStatusAbbreviation();
			if(!StringZZZ.startsWith(sAbr, "watchrunner")) break main;
			
			bReturn = true;			
		}//end main:
		
		return bReturn;
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
