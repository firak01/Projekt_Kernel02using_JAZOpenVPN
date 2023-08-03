package use.openvpn.serverui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import use.openvpn.client.ClientMainZZZ;
import use.openvpn.clientui.ClientTrayUIZZZ;
import use.openvpn.clientui.OVPNConnectionWatchRunnerZZZ;
import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.ServerMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;

/**This class watches the ServerMainZZZ-class and the ServerConnectionListenerRuner-objects.
 * This class runs in a seperate thread, so the TrayIcon stays "clickable", that means that clicking on the icon will be processed.
 * 
 * @author 0823
 *
 */
public class ServerMonitorRunnerZZZ extends KernelUseObjectZZZ implements Runnable{
	private ServerMainZZZ objServerMain = null;
	private ServerTrayUIZZZ objTray = null;
	
	private HashMap hmWatchRunnerStatus = new HashMap(); //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatus = new String("");            //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatusPrevious = new String("");    //den vorherigen Status festhalten, damit z.B. nicht immer wieder das Icon geholt wird.
	
	private ArrayList listaWatchRunner = new ArrayList();
	
	//private int iStatusSet = 0;  //Der Status, der schon im Tray gesetzt ist. Damit er nicht permanent neu gesetzt wird.
	private boolean bFlagWatchRunnerStarted=false;
	private boolean bFlagStatiAllFilled = false;
	
public ServerMonitorRunnerZZZ(IKernelZZZ objKernel, ServerTrayUIZZZ objTray, ServerMainZZZ objConfig, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ServerMonitorRunnerNew_(objTray, objConfig, saFlagControl);
}

private void ServerMonitorRunnerNew_(ServerTrayUIZZZ objTray, ServerMainZZZ objServerMain, String[] saFlagControl) throws ExceptionZZZ{
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
		
						
			this.objServerMain = objServerMain;
			this.objTray = objTray;
		}//End check

	}//END main
}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public void run() {
		try {
		main:{
			check:{
				if(this.objServerMain==null) break main;
			}//END check:
	
			//Zuerst mal pruefen, ob der ServerMain-Prozess erfolgreich abgeschlossen worden ist.
			boolean bCheck=false;
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": In run()-Schleife.";
			do{
				System.out.println(sLog);
				this.getLogObject().WriteLineDate(sLog);
				if(objServerMain.getFlag("HasError")==true){
					//StatusString aendern
					String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);
					}					
					break main;
				}			
				
				boolean bStarted = objServerMain.getFlag("isstarted");
				boolean bWatchRunnerStarted = objServerMain.getFlag("watchrunnerstarted");
				boolean bListening = objServerMain.getFlag("islistening");
				if(bWatchRunnerStarted & !bListening){
					String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.STARTING);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.STARTING);	
					}					
					Thread.sleep(500);	
				}
				
				if(bWatchRunnerStarted && bListening) {
					String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.LISTENING);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.LISTENING);
					}
					Thread.sleep(500);
				}
				
			}while(bCheck==false);
			
			//Erst mal sehn, ob ueberhaupt was da ist.
			ArrayList listaProcessStarter = objServerMain.getProcessStarterAll();
//			bCheck = objServerMain.getFlag("isstarted");
//			if(listaProcessStarter.size() <= 0 && bCheck){
//				String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayUIZZZ.iSTATUS_INTERRUPTED);
//				if(this.isStatusChanged(sWatchRunnerStatus)) {
//					this.setStatusString(sWatchRunnerStatus);
//					this.objTray.switchStatus(ServerTrayUIZZZ.iSTATUS_INTERRUPTED);
//				}				
//				break main;
//			}
					
			//Nun fuer alle in ServerMain gestarteten OpenVPN.exe - Processe einen Thread bereitstellen, der das VPN-IP-Adressen Ziel versuchen kann zu erreichen.
			listaWatchRunner = new ArrayList(listaProcessStarter.size());			
			for(int icount=0; icount < listaProcessStarter.size(); icount++){
				ServerConfigStarterOVPN objProcessStarterTemp = (ServerConfigStarterOVPN) listaProcessStarter.get(icount);
				if(objProcessStarterTemp!=null){
				  
					/* Merke: (A) Dies setzt voraus, dass auf dem Client ein Thread auf den entsprechenden Port "h�rt". Besser gef�llt mir die L�sung, dass auf dem Server ein spezieller Thread auf den Verbindungsaufbau des Clients h�rt und dementsprechend reagiert. 
					ServerConnectionWatchRunnerZZZ objWatchTemp = new ServerConnectionWatchRunnerZZZ(objKernel, objStarterTemp, null);					
					*/									
					
					//Nur die Processe Monitoren, die noch "aktiv sind
					if(objProcessStarterTemp.isProcessAlive()){
					ServerConnectionListenerZZZ objWatchTemp = new ServerConnectionListenerZZZ(objKernel, objProcessStarterTemp, null);
					
					Thread objThreadTemp = new Thread(objWatchTemp);
					objThreadTemp.start();
					listaWatchRunner.add(objWatchTemp);
					}
				}
			}//END for
			this.setFlag("WatchRunnerStarted", true);
			
			
			
			
			//Nun in einer Endlosschleife permanent den Status der ganzen WatchRunner pruefen
			//Daraus ergibt sich dann ggf. ein aendern der Anzeige und der Status - String dieses Objekts wird aktualisiert.
			this.hmWatchRunnerStatus = new HashMap(listaWatchRunner.size());
			HashMap hmConnectionCount = new HashMap(listaWatchRunner.size()); //Hier wird festgehalten wieviele und welche Verbindung (alias) erfolgt ist
			boolean bConnected = false;
			do{
				//Schleife zum aendern des Statustexts + des Status
				for(int icount=0; icount < listaWatchRunner.size(); icount ++){
					//Das w�re ein aktiver Ping   ServerConnectionWatchRunnerZZZ objWatchTemp = (ServerConnectionWatchRunnerZZZ) listaWatchRunner.get(icount);
					//Nun aber den passiven listener verwenden
					ServerConnectionListenerZZZ objWatchTemp = (ServerConnectionListenerZZZ) listaWatchRunner.get(icount);
					ServerConfigStarterOVPN objProcessStarterTemp = objWatchTemp.getStarterObject();
					if(objProcessStarterTemp.isProcessAlive()==false){	
						//+++ FALL: OVPN-Process wurde beendet 
						hmWatchRunnerStatus.put(objProcessStarterTemp.getAlias(), "Stopped. Not listening.");
						listaWatchRunner.remove(icount);
						hmConnectionCount.remove(objProcessStarterTemp.getAlias());
						break; //Zaehlerindizierung hat sich ggf. geaendert. Darum die Schleife verlassen und neu anfangen.
					}else{
						if(bConnected == false && objWatchTemp.getFlag("isconnected")){
							//+++ FALL: Neue, erste Verbindung entdeckt
							bConnected = true;
							hmConnectionCount.put(objProcessStarterTemp.getAlias(), "1");
							hmWatchRunnerStatus.put(objWatchTemp.getAlias(), "Connected to " + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
							
							//Das Symbol in der Statuszeile aendern. Eine Connection reicht dazu aus.
							this.sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.CONNECTED);	
							this.objTray.getServerBackendObject().logMessageString(icount + "# Connection reported by ServerMonitorRunner to " + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
							this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.CONNECTED);
						}else if(bConnected == true && objWatchTemp.getFlag("isconnected")==true){
							//+++ FALL: Neue, weitere Verbindung entdeckt (bzw. die ist noch aktiv)
							hmConnectionCount.put(objProcessStarterTemp.getAlias(), "1");  //!!! Das soll f�r schon existierende Werte nur ersetzen, nicht neue Listeneintr�ge erzeugen
							hmWatchRunnerStatus.put(objWatchTemp.getAlias(), "Connected to " + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
						   //Das Symbol in der Statusleiste �ndert sich nicht
							
						}else if(bConnected == true && objWatchTemp.getFlag("isconnected")==false){							
							if(hmConnectionCount.containsKey(objProcessStarterTemp.getAlias())){
//								+++ FALL: Eine Verbindung entfernen (es gibt schon mindestens eine Verbindung)
								hmConnectionCount.remove(objProcessStarterTemp.getAlias());
								hmWatchRunnerStatus.put(objWatchTemp.getAlias(), "Disconnected from " + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
								
							}else{
								//+++ FALL: Noch gar keine Verbindung. Weiter lauschen  (es gibt aber schon mindestens eine andere Verbindung).
								hmWatchRunnerStatus.put(objWatchTemp.getAlias(), "Listening for connection to "  + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
							}
														
							//Merke: Das Symbol in der Statuszeile wird erst dann ge�ndert, wenn keine Connection mehr vorhanden ist. s. weiter unten
							
						}else if(objWatchTemp.getFlag("hasError")){
							//+++ FALL: Fehler und ENDE
							//Der Fehler soll abgefangen werden, wenn z.B. das TAP-Defice (virtuelle Netzwerkkarte) nicht gestartet werden kann.
//							Das Symbol in der Statuszeile �ndern
							this.sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);		
							this.objTray.getServerBackendObject().logMessageString(icount + "# Error reported by ServerMonitorRunner. Canceling application. See log file for details.");
							
							
							this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.ERROR);
							//Merke: Die hmWathcRunnerStatus - Eintraege bleiben dabei jedoch unber�hrt.
							break main;
						}else{
							//+++ FALL: Noch garkeine Verbindung
							hmWatchRunnerStatus.put(objWatchTemp.getAlias(), "Listening for connection to "  + objWatchTemp.getVpnIpRemote() + ":" +objWatchTemp.getPortString());
						}						
					}
				}//END for
											
//				+++ Spezielles Symbol in der Statusleiste setzen
				if(bConnected == true && listaWatchRunner.size() == 0){
//					Falls schon mal connected war und die Anzahl der "aktiven" OpenVPN Prozesse auf 0 runtergegangen ist, den Status auf "NotListening" �ndern. Die Schleife verlassen.
					String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.STOPPED);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.STOPPED);
					}
					break; //DIE ENDLOSSCHLEIFE VERLASSEN
				}else if(bConnected == true && hmConnectionCount.isEmpty()){
//					Falls schon mal connected war und nun der Zaehler der Verbindungen auf 0 zur�ckgegangen ist, das Icon entsprechend aendern.
					String sWatchRunnerStatus = ServerTrayUIZZZ.getStatusStringByStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.INTERRUPTED);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ.INTERRUPTED);
					}
					bConnected = false;
				}				
				this.setFlag("StatiAllFilled", true);  //Kennzeichnet, das alle hmWatchRunnerStatus Eintr�ge erfolgt sind.
				Thread.sleep(1000);
			}while(true);
	}//END main:
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExceptionZZZ ez) {
			System.out.println(ez.getDetailAllLast());
		}finally{
			
		}
	}//END run
	
	
	/**Adds a ServerConnectionWatchRunnerZZZ-Object to the internal ArrayList.
	 * This Object will used for a Thread which is started too.
	 * @param objStarter, the Object which has started the OVPN.exe-Process.
	 * @return, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 24.07.2006 - 11:14:11
	 */
	public boolean addWatchRunnerThread(ServerConfigStarterOVPN objStarter){
		boolean bReturn = false;
		main:{
			try {
			check:{
				if(objStarter==null) break main;
			}//END check
		ServerConnectionWatchRunnerZZZ objRunner = new ServerConnectionWatchRunnerZZZ(objKernel, objStarter, null);
		this.listaWatchRunner.add(objRunner);
		
		//TODO GOON diese Threads auch public in einer ArrayList machen.
		Thread t = new Thread(objRunner);
		t.start();
				
		this.setFlag("WatchRunnerStarted", true);
			
		} catch (ExceptionZZZ ez) {
			System.out.println(ez.getDetailAllLast());
		}
		}//END main
		return bReturn;
	}
	
	
	//### Getter / Setter
	public HashMap getStatusDetailHashMap(){
		if(this.getFlag("StatiAllFilled")==true){
			return this.hmWatchRunnerStatus;
		}else{
			return null;
		}
	}
	
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
	- hasError
	- hasOutput
	- hasInput
	- stoprequested
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
			
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("watchrunnerstarted")){
				bFunction = bFlagWatchRunnerStarted;
				break main;
			}else if(stemp.equals("statiallfilled")){
				bFunction = bFlagStatiAllFilled;
				break main;
			}
			/*else if(stemp.equals("hasinput")){
				bFunction = bFlagHasInput;
				break main;
			}else if(stemp.equals("stoprequested")){
				bFunction = bFlagStopRequested;
				break main;
			}
			*/
	
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
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("watchrunnerstarted")){
			bFlagWatchRunnerStarted = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("statiallfilled")){
			bFlagStatiAllFilled = bFlagValue;
			bFunction = true;
			break main;
		}
		/*else if(stemp.equals("hasinput")){
			bFlagHasInput = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("stoprequested")){
			bFlagStopRequested = bFlagValue;
			bFunction = true;
			break main;
		}
		*/

		}//end main:
		return bFunction;
	}

}//END class