package use.openvpn.serverui.component.tray;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.jdesktop.jdic.tray.TrayIcon;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernelUI.component.AbstractKernelActionListenerCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.tray.AbstractKernelActionTrayZZZ;
import basic.zKernelUI.util.JTextFieldHelperZZZ;
import use.openvpn.ITrayOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.server.IServerMainOVPN;
import use.openvpn.server.ServerApplicationOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.process.ServerThreadProcessWatchMonitorOVPN;
import use.openvpn.serverui.component.FTPCredentials.DlgFTPCredentialsOVPN;
import use.openvpn.serverui.component.FTPCredentials.IConstantProgramFTPCredentialsOVPN;
import use.openvpn.serverui.component.IPExternalUpload.DlgIPExternalOVPN;

public class ActionServerTrayUIOVPN extends AbstractKernelActionTrayZZZ{		
	private static final long serialVersionUID = 4170579821557468353L;		
	
	public ActionServerTrayUIOVPN(IKernelZZZ objKernel, ITrayOVPN objTrayParent) throws ExceptionZZZ{
		super(objKernel, objTrayParent);
		ActionServerTrayUINew_();
	}
		
	private boolean ActionServerTrayUINew_() throws ExceptionZZZ{
		boolean bReturn = false;	
		main:{
		
			bReturn = true;
		}//end main:	
		return bReturn;
	}
	
	
	public boolean start() throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(this.getMainObject()==null)break main;
			
			//Wenn das so ohne Thread gestartet wird, dann reagiert der Tray auf keine weiteren Clicks.
			//Z.B. den Status anzuzeigen.
			//this.getServerBackendObject().start(this);
			
			//Also dies über einen extra thread tun, damit z.B. das Anclicken des SystemTrays mit der linken Maustaste weiterhin funktioniert !!!
			//Problem dabei: Der SystemTray wird nicht aktualisiert.
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
	
	

	
	/**
	 * @return
	 * @author Fritz Lindhauer, 24.05.2023, 08:07:21
	 */
	public boolean listen() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			try{ 
				if(this.getMainObject()==null)break main;
								
				ServerThreadProcessWatchMonitorOVPN objMonitor = this.getMainObject().getProcessMonitorObject();
				if(objMonitor==null) break main;
				
				boolean bStarted = this.getMainObject().getStatusLocal(ServerMainOVPN.STATUSLOCAL.ISSTARTED);
				if(!bStarted) {
					
					boolean bStarting = this.getMainObject().getStatusLocal(ServerMainOVPN.STATUSLOCAL.ISSTARTING);
					if(!bStarting) {	
						objMonitor.offerStatusLocalEnum(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASSERVERNOTSTARTING);
						break main;
					}
					objMonitor.offerStatusLocalEnum(IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL.HASSERVERNOTSTARTED);						
					break main;
				}
				
				Thread objThreadMonitor = new Thread(objMonitor);
				objThreadMonitor.start();
				//Merke: Wenn der erfolgreich verbunden wurde, wird der den Status auf "ISCONNECTED" gesetzt und ein Event geworfen.
				
				bReturn = true;
			}catch(ExceptionZZZ ez){
				try {
					//Merke: diese Exception hier abhandeln. Damit das ImageIcon wieder zurueckgesetzt werden kann.
					ez.printStackTrace();
					String stemp = ez.getDetailAllLast();
					this.getKernelObject().getLogObject().WriteLineDate(stemp);
					System.out.println(ez.getDetailAllLast());
					this.getTrayParent().switchStatus(ServerTrayStatusMappedValueOVPN.ServerTrayStatusTypeZZZ.ERROR);
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
			sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getAbbreviation();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
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
			sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage();//ohne das TrayIcon kann man ja auf nix clicken. Darum gibt es keine frueheren Status.
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
	
	
	/**TODO Describe what the method does
	 * @return, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 21.07.2006 - 11:02:02
	 */
	public String readStatusDetailString() throws ExceptionZZZ{
		String sReturn = "";
		main:{
			if (this.getMainObject() == null){
				sReturn = "No Server Backend available.";
				break main;
			}

			String stemp = null;
			
			//Merke: der BackendStausString wird von den Events, die er empfaengt gefuettert.
			//Hole immer die letzte StatusMeldung...
			String sStatusServerString = this.readBackendStatusMessage();
			if(StringZZZ.isEmpty(sStatusServerString)){				
				sReturn = sReturn + ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "\n";
				break main;
			}else{			
				sReturn = sReturn + sStatusServerString + "\n";
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
					//Falls der Server gestartet wurde, gib den Rechnernamen aus, anstatt einer "ist gestartet" Meldung. Spart Platz.
					String sServerOrClient = this.getMainObject().getConfigChooserObject().getOvpnContextUsed();					
					sReturn = sServerOrClient.toUpperCase() + ": " + InetAddress.getLocalHost().getHostName() + "\n";
				} catch (UnknownHostException e) {				
					e.printStackTrace();
					ExceptionZZZ ez = new ExceptionZZZ("Fehler bei Ermittlung des Rechnernames", iERROR_RUNTIME, (Object)this, (Exception)e);
					throw ez;
				}
				
				sReturn = sStatusProcessMonitorString + sReturn;
			}
					
			ServerApplicationOVPN objApplication = (ServerApplicationOVPN) this.getMainObject().getApplicationObject();
			stemp = objApplication.getURL2Parse();
			if(stemp==null){
				sReturn = sReturn + "URL: NOT RECEIVED\n";
			}else{
				stemp = StringZZZ.right("http://" + stemp, "http://", false);
				stemp = StringZZZ.abbreviateDynamic(stemp,40);
				sReturn = sReturn + "URL: " + stemp + "\n";
			}
			
			//REMOTE
			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getIpRemote();
			if(stemp==null){
				sReturn = sReturn + "My ext. IP: Not found on URL.|";
			}else{			
				sReturn = sReturn + "My ext. IP: " + stemp + "|";
			}
			 
			stemp = this.getMainObject().getApplicationObject().getIpLocal();
			if(stemp==null){
				sReturn = sReturn + "-> Local IP: Not availabel.\n";
			}else{
				sReturn = sReturn + "-> Local IP: " + stemp + "\n";
			}
			
			String sTap = this.getMainObject().getApplicationObject().getTapAdapterUsed();
			if(sTap==null){
				sTap = "->TAP: Not defined in Kernel Ini-File.";
			}else{
				sTap = "->TAP: " + sTap ;
			}

			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpLocal();
			if(stemp==null){
				sReturn = sReturn + "VPN-IP: Not defined in Kernel Ini-File.|" + sTap + "\n";
			}else{
				sReturn = sReturn + "VPN-IP: " + stemp + "|" + sTap + "\n";
			}
			
			
			//VPNIP, wird extra an die VpnIpEstablished-Property uebergeben, wenn eine Verbindung festgestellt wird.
			//this.getClientMainObject().getApplicationObject().getVpnIpRemote() //Das wäre aber noch keine erstellte Verbindung, sondern eher nur das Ziel.
			stemp = ((ServerApplicationOVPN)this.getMainObject().getApplicationObject()).getVpnIpRemoteEstablished();
			if(stemp == null){
				sReturn = sReturn + "\t\t|->VPN-IP: Not connected.\n";
			}else{
				sReturn = sReturn + "\t\t|->VPN-IP: " + stemp + "\n";
				/* Logischer Fehler: Wenn die VPN-Verbindung erstellt worden ist, dann ist ggf. auch ein anderer Port "anpingbar" per meinem JavaPing.
				stemp = this.objConfig.getVpnPortEstablished();
				sReturn = sReturn + ":" + stemp;
				*/
			}
		}//END main
		return sReturn;
	}
	
//	/** Reads the HashMap with detailed information from the ServerMonitor-object-Thread
//	* @return HashMap
//	* 
//	* lindhaueradmin; 10.08.2006 11:06:52
//	 */
//	public HashMap readStatusDetailHashMap(){
//		HashMap hmReturn = null;
//		ServerThreadProcessWatchMonitorOVPN objMonitor = this.getM.getMonitorObject();
//		if(objMonitor!=null){
//			hmReturn = objMonitor.getStatusDetailHashMap();
//		}
//		return hmReturn;
//	}
	
	public String readProtocolString(){
		String sReturn = "";
		main:{
			check:{
				if (this.getMainObject() == null){
					sReturn = ServerMainOVPN.STATUSLOCAL.ISSTARTNEW.getStatusMessage() + "(objServerBackend NULL case)";
					break main;
				}
			}//END check:
		 
		ArrayList listaLogString = this.getMainObject().getProtocolStringAll();
		if(listaLogString.isEmpty()){
			if (this.getMainObject() == null){
					sReturn = "No log string available.";
					break main;
			}
		}
		
		for(int icount = 0; icount < listaLogString.size(); icount++){
			String sLog = (String)listaLogString.get(icount);
			sReturn = sReturn + sLog + "\n";
		}
		
		}//END main
		return sReturn;
	}
	
	
//	/** Reads a status string from the ServerMonitor-Object-Thread
//	* @return String
//	* 
//	* lindhaueradmin; 10.08.2006 11:32:16
//	 */
//	public String readStatusString(){
//		String sReturn = "";
//		if(this.objMonitor!=null){
//			sReturn = this.objMonitor.getStatusString();
//		}
//		return sReturn;
//	}
	
	
	
	
	//#######################
	//### GETTER / SETTER
	public void setMainObject(IServerMainOVPN objServerBackend){
		ITrayOVPN objTray = (ITrayOVPN) this.getTrayParent();
		objTray.setMainObject((ServerMainOVPN)objServerBackend);
	}
	public ServerMainOVPN getMainObject(){
		ServerMainOVPN objReturn = null;
		main:{
			ITrayOVPN objTray = (ITrayOVPN) this.getTrayParent();		
			objReturn = (ServerMainOVPN) objTray.getMainObject();
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
			if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.END.getMenu())){
				this.unload();	
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START.getMenu())){
				boolean bFlagValue = this.start();					
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN.getMenu())) {
				boolean bFlagValue = this.listen();
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PROTOCOL.getMenu())){
				//JOptionPane pane = new JOptionPane();
				String stemp = this.readProtocolString();
				//this.getTrayIconObject() ist keine Component ????
				JOptionPane.showMessageDialog(null, stemp, "Log des OVPN Connection Listeners", JOptionPane.INFORMATION_MESSAGE );
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.PAGE_IP_UPLOAD.getMenu())) {
				
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
				ServerTrayUIOVPN objTray = (ServerTrayUIOVPN) this.getTrayParent();
				if(objTray.getDialogIpExternal()==null || objTray.getDialogIpExternal().isDisposed() ) {									
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;					
					HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
					hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
					DlgIPExternalOVPN dlgIPExternal = new DlgIPExternalOVPN(this.getKernelObject(), null, hmFlag);
					//dlgIPExternal.setText4ButtonOk("USE VALUE");	
					objTray.setDialogIpExternal(dlgIPExternal);
				}
				try {
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
					objTray.getDialogIpExternal().showDialog(null, "Build and Upload IP Page");
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: 'Build and Upload IP Page'");
				} catch (ExceptionZZZ ez) {					
					System.out.println(ez.getDetailAllLast()+"\n");
					ez.printStackTrace();
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
				}
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.FTP_CREDENTIALS.getMenu())) {
				ServerTrayUIOVPN objTray = (ServerTrayUIOVPN) this.getTrayParent();		
				if(objTray.getDialogFtpCredentials()==null || objTray.getDialogFtpCredentials().isDisposed() ) {									
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;					
					HashMap<String,Boolean>hmFlag=new HashMap<String,Boolean>();
					hmFlag.put(IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name(), true);
					
					HashMap<String,Boolean>hmFlagLocal=new HashMap<String,Boolean>();
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_CANCEL.name(), false);
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_CLOSE.name(), true);
					hmFlagLocal.put(KernelJDialogExtendedZZZ.FLAGZLOCAL.HIDE_ON_OK.name(), false);
					DlgFTPCredentialsOVPN dlgFTPCredentials = new DlgFTPCredentialsOVPN(this.getKernelObject(), null, hmFlagLocal, hmFlag);
					dlgFTPCredentials.setText4ButtonOk("USE VALUES");	
					objTray.setDialogFtpCredentials(dlgFTPCredentials);
				}
				try {
					//Merke: Hier gibt es keinen ParentFrame, darum ist this.getFrameParent() = null;
					objTray.getDialogFtpCredentials().showDialog(null, "FTP Credentials");
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Ended Action: 'FTP Credentials'");
					
					//Versuch den Focus und ein markiertes Feld zu setzen
					//DAS GEHT NUR AN DIESER STELLE, NACHDEM DER DIALOG SCHON GESTARTET IST
					//UND DER STARTWERT SCHON GESETZT WURDE
					//UND DAS GEHT AUCH NUR FUER 1 TEXTFIELD
//						String sTextfield4Update1 =IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_USERNAME;
//						JTextField textField1 = (JTextField) this.dlgFTPCredentials.getPanelContent().searchComponent(sTextfield4Update1);
//						if(textField1!=null) {
//							//textField.setText(sText2Update);					
//							JTextFieldHelperZZZ.markAndFocus(this.dlgFTPCredentials.getPanelContent(),textField1);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.
//						}else {
//							ReportLogZZZ.write(ReportLogZZZ.DEBUG, "JTextField '" + sTextfield4Update1 + "' NOT FOUND in panel '" + this.dlgFTPCredentials.getPanelContent().getClass() + "' !!!");										
//						}
					
					String sTextfield4Update2 =IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED;
					JTextField textField2 = (JTextField) objTray.getDialogFtpCredentials().getPanelContent().searchComponent(sTextfield4Update2);
					if(textField2!=null) {											
						JTextFieldHelperZZZ.markAndFocus(objTray.getDialogFtpCredentials().getPanelContent(),textField2);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.
					}else {
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "JTextField '" + sTextfield4Update2 + "' NOT FOUND in panel '" + objTray.getDialogFtpCredentials().getPanelContent().getClass() + "' !!!");										
					}
				} catch (ExceptionZZZ ez) {					
					System.out.println(ez.getDetailAllLast()+"\n");
					ez.printStackTrace();
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
				}
				
			}else if(sCommand.equalsIgnoreCase(ServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.DETAIL.getMenu())){
				TrayIcon objTrayIcon = this.getTrayParent().getTrayIcon();
			
				String stemp = this.readStatusDetailString();
				if(stemp!= null){
					if(objTrayIcon!=null) objTrayIcon.displayMessage("Status des OVPN Connection Listeners.", stemp, TrayIcon.INFO_MESSAGE_TYPE);
				}else{
					if(objTrayIcon!=null) objTrayIcon.displayMessage("Status des OVPN Connection Listeners.", "unable to receive any status.", TrayIcon.INFO_MESSAGE_TYPE);
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
			this.getTrayParent().switchStatus(ServerTrayStatusMappedValueOVPN.ServerTrayStatusTypeZZZ.ERROR);			
		}
				
		}//end main:
		return bReturn;
	}

	@Override
	public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
		return true;
	}

	@Override
	public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
	}

	@Override
	public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) throws ExceptionZZZ {	
	}
}//END Class

