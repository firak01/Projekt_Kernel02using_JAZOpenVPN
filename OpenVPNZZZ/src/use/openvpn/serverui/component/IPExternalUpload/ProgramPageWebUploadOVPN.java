package use.openvpn.serverui.component.IPExternalUpload;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import custom.zKernel.net.ftp.FTPSZZZ;
import custom.zKernel.net.ftp.SFTPZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelLogZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelSingletonZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.character.CharZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.module.AbstractKernelProgramZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;

/**Vereinfacht den Zugriff auf die HTML-Seite, in der die externe IPAdresse des Servers bekannt gemacht wird. 
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwentet.
 * @author 0823
 *
 */
public class ProgramPageWebUploadOVPN extends AbstractKernelProgramZZZ{
	private String sModuleName=null;
	
	private String sTargetUrl=null;

	private String sIPProxy = null;
	private String sPortProxy = null;
	
	private KernelJPanelCascadedZZZ panel = null;
	private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Hier als Variable, damit die interne Runner-Klasse darauf zugreifen kann.
	// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.

	
	private boolean bFlagUseProxy = false;
	
	//public static final String PROGRAM_ALIAS = "IP_ServerContext";
	
	public ProgramPageWebUploadOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		main:{
			check:{	 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ(stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
					
					
				}							
			}//End check
			
			this.setPanelParent(panel);
			
			KernelJDialogExtendedZZZ dialog = this.getPanelParent().getDialogParent();  //this.getDialogParent();
			String sModuleName = dialog.getClass().getName();  //der Frame, über den diese Dialogbox liegt	
			if(StringZZZ.isEmpty(sModuleName)){
				ExceptionZZZ ez = new ExceptionZZZ("ModuleName", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else{
				this.setModuleName(sModuleName);
			}
			
			//TODOGOON; //20210128 Programname nicht aus dem Panel, sondern das Program selbst
			          //Ggf. IKernelModuleUserZZZ implementieren
			String sProgramName = ""; 
			sProgramName = KernelUIZZZ.getProgramName(this);
			
			KernelJPanelCascadedZZZ panelParent = this.getPanelParent();
			
			if(panelParent!=null){
				sProgramName = KernelUIZZZ.getProgramName(panelParent);
			}else{
				sProgramName = this.getClass().getName();
			}
			if(StringZZZ.isEmpty(sProgramName)){
				ExceptionZZZ ez = new ExceptionZZZ("ProgramName", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}else{
				this.setProgramName(sProgramName);
			}
			
			
			//### Prüfen, ob das Modul konfiguriert ist
//			boolean bIsConfigured = objKernel.proofModuleFileIsConfigured(sModuleAlias);
//			if(bIsConfigured==false){
//				ExceptionZZZ ez = new ExceptionZZZ("ModuleAlias='" + sModuleAlias + "' seems not to be configured for the Application '" + objKernel.getApplicationKey(), iERROR_CONFIGURATION_MISSING, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}		
//			boolean bExists = objKernel.proofModuleFileExists(sModuleAlias);
//			if(bExists==false){
//				ExceptionZZZ ez = new ExceptionZZZ("ModuleAlias='" + sModuleAlias + "' is configured, but the file does not exist for the Application '" + objKernel.getApplicationKey(), iERROR_CONFIGURATION_MISSING, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}	
			
			
		}//END main
	}
		
	
	//### Getter / Setter
	public KernelJPanelCascadedZZZ getPanelParent(){
		return this.panel;
	}
	public void setPanelParent(KernelJPanelCascadedZZZ panel){
		this.panel = panel;
	}
	
	
	public String getModuleName(){
		return this.sModuleName;
	}
	public void setModuleName(String sModuleName){
		this.sModuleName=sModuleName;
	}
	
	public String getTargetUrl() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sTargetUrl)){
			String stemp = this.readTargetUrl();
			this.sTargetUrl = stemp;
		}
		return this.sTargetUrl;
	}
	
	public String readTargetUrl() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "TargetURL");
			sReturn = objEntry.getValue();
		}
		return sReturn;
	}
	
	public String getIpProxy() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sIPProxy)){
			String sIP = this.readIpProxy();
			this.sIPProxy = sIP;
		}
		return this.sIPProxy;
	}
	public String readIpProxy() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "ProxyHost");
			sReturn = objEntry.getValue();
		}
		return sReturn;		
	}
	
	public String getPortProxy() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sPortProxy)){
			String sPort = this.readPortProxy();
			this.sPortProxy = sPort;
		}
		return this.sPortProxy;
	}
	public String readPortProxy() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "ProxyPort");
			sReturn = objEntry.getValue();
		}
		return sReturn;		
	}

	public boolean uploadPageWeb() throws ExceptionZZZ{
		boolean bReturn = false;
		System.out.println("Start");
		main:{
			boolean btemp; SFTPZZZ objFTP=null;
			try {

		//1. Erstellen das Z-Kernel Objekt							
		IKernelZZZ objKernel = this.getKernelObject(); //KernelSingletonZZZ.getInstance("FGL", "01", "", "ZKernelConfigFTP_test.ini",(String[]) null);
		
		//2. Protokoll
		IKernelLogZZZ objLog = objKernel.getLogObject();

		//3. FTPZZZ-Objekt, als Wrapper um jakarta.commons.net.ftpclient
		objFTP = new SFTPZZZ(objKernel, objLog, (String[]) null);
		
		//4. Konfiguration auslesen
		//Hier werden Informationen ueber die IP-Adressdatei ausgelesen, etc.
		FileIniZZZ objFileIniIPConfig = objKernel.getFileConfigIniByAlias("FTP");
								
		IKernelConfigSectionEntryZZZ entryServer = objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTP","Server");
		String sServer = entryServer.getValue();
		
		IKernelConfigSectionEntryZZZ entryUser = objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTP","User");
		String sUser = entryUser.getValue();
		
		IKernelConfigSectionEntryZZZ entryPassword = objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTPModul","Password");
		String sPassword = entryPassword.getValue();
		
		System.out.println("Page Transfer - Login detail read from file: "+sServer + " ("+sUser+" - "+sPassword+")");

			//5. Login
		btemp = objFTP.makeConnection(sServer, sUser, sPassword);
		if (btemp==true) System.out.println("Connction - successfull, now transfering file");
		
		//6. Datei ermitteln und �bertragen
		//TODO Mit eine @Z-Formel in der Konfiguration DIESES Programms auslesen, die auf den wert in der konfiguration eines anderen Programms hinweist.
		//Hier die Konfiguration direkt auslesen
		/*
			 * TargetDirectory=c:\temp
TargetFile=testpage.html
			 */
		IKernelConfigSectionEntryZZZ entryDirSource=objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTPModul","SourceDirectory");
		String sDirSource = entryDirSource.getValue();
		
		//Also eigentlich objKernel.getParameterByProgramAlias(objFileIniIPConfgi, "ProgFTP","SourceDirectory");
		IKernelConfigSectionEntryZZZ entryFile = objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTPModul","SourceFile");
		String sFile = entryFile.getValue();
		
		//Also eigentlich objKernel.getParameterByProgramAlias(objFileIniIPConfgi, "ProgFTP","SourceFile");
		String sFilePath = FileEasyZZZ.joinFilePathName(sDirSource, sFile);
		File objFile = new File(sFilePath);
		if(!FileEasyZZZ.exists(objFile)){
			System.out.println("File not found '"+sFilePath);
		}else{
			IKernelConfigSectionEntryZZZ entryDirTarget=objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTPModul","TargetDirectory");
			String sDirTarget = entryDirTarget.getValue();
			StringZZZ.replace(sDirTarget, FileEasyZZZ.sDIRECTORY_SEPARATOR_WINDOWS, CharZZZ.toString(objFTP.getDirectorySeparatorRemote()));
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": DirTarget='"+sDirTarget+"'");
			
			//Dateiname bleibt gleich, also nicht extra auslesen.
			//IKernelConfigSectionEntryZZZ entryFileTarget = objKernel.getParameterByProgramAlias(objFileIniIPConfig, "FTPModul","TargetFile");
			//String sFileTarget = entryFileTarget.getValue();				
			String sFileTargetTotal = FileEasyZZZ.joinFilePathName(sDirTarget, sFile, objFTP.getDirectorySeparatorRemote(), true); //Merke: Bei dem Remote-Pfad soll sichergestellt sein, dass kein src-Root Ordner voranagestellt ist.
			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": FileTargetName: "+sFileTargetTotal);
															
			btemp = objFTP.uploadFile(objFile, sDirTarget);
		}
		
		
		//7. Verbindung schliessen
		if (btemp==true){
			System.out.println("Transfer - successfull, now disconnecting"); 
		}else{
			 System.out.println("Transfer - NOT successfull, now disconnecting");					
		}
		objFTP.closeConnection();

		} catch (ExceptionZZZ ez) {
				System.out.println(ez.getDetailAllLast());
		} 
		System.out.println("Ende");
	}//end main:
		return bReturn;
	}
	
//DAS IST EIN ANDERES PROGRAM
//	public String getIpExternal() throws ExceptionZZZ{
//		if(StringZZZ.isEmpty(this.sIPExternal)){
//			String sIP = this.readIpExternal();
//			this.sIPExternal = sIP;
//		}
//		return this.sIPExternal;
//	}
//	public String readIpExternal() throws ExceptionZZZ{
//		String sReturn = null;
//		main:{
//			String sURL = this.getUrl2Read();
//			if(StringZZZ.isEmpty(sURL)){
//				ExceptionZZZ ez = new ExceptionZZZ("URL to read Ip from", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}
//			
//			String[] satemp = {"UseStream"};
//			KernelReaderURLZZZ objReaderURL = new KernelReaderURLZZZ(objKernel, sURL,satemp, "");			
//			if(this.getFlag("useProxy")==true){
//				this.updateLabel("Proxy: Connecting ...");
//				boolean btemp = this.readProxyEnabled();
//				if(btemp){
//					String sProxyHost = this.getIpProxy();
//					String sProxyPort = this.getPortProxy();
//					objReaderURL.setProxyEnabled(sProxyHost, sProxyPort);
//					this.updateLabel("Proxy: Continue ...");
//				}else{
//					this.updateLabel("No proxy: Continue ...");
//				}
//			}
//						
//			//+++ Nachdem nun ggf. der Proxy aktiviert wurde, die Web-Seite versuchen auszulesen				
//			//+++ Den IP-Wert holen aus dem HTML-Code der konfigurierten URL
//			KernelReaderPageZZZ objReaderPage = objReaderURL.getReaderPage();
//			KernelReaderHtmlZZZ objReaderHTML = objReaderPage.getReaderHTML();
//			 
//			//Nun alle input-Elemente holen und nach dem Namen "IPNr" suchen.
//			TagTypeInputZZZ objTagTypeInput = new TagTypeInputZZZ(objKernel);			
//			TagInputZZZ objTag = (TagInputZZZ) objReaderHTML.readTagFirstZZZ(objTagTypeInput, "IPNr");
//			sReturn = objTag.readValue();  //Merke: Das Eintragen des Wertes wird der �bergeordneten Methode �berlassen. 
//						
//		}//end main:
//		this.sIPExternal = sReturn;
//		return sIPExternal;
//	}	
	
	
	/**Read from the configuration file a proxy which might be necessary to use AND enables the proxy for this application.
	 * Remember: This proxy is used to read the url (containing the ip adress)
	 *                  
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 14:20:24
	 */
	public boolean readProxyEnabled() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			
		    //+++ Ggf. notwendige Proxy-Einstellung pr�fen.
			//Z.B. bei der itelligence bin ich hinter einem Proxy. Die auszulesende Seite ist aber im Web.
			String sProxyHost = this.getIpProxy();			
			if(!StringZZZ.isEmpty(sProxyHost)){		//Eine Proxy-Konfiguration ist nicht Pflicht		
				String sProxyPort = this.getPortProxy();
				
				//+++ Nun versuchen herauszufinden, ob der Proxy auch erreichbar ist und existiert. Nur nutzen, falls er existiert
				KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
				try{ //Hier soll nicht abgebrochen werden, wenn es nicht klappt. Lediglich ins Log soll etwas geschrieben werden.
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Trying to reach the proxy configured. '" + sProxyHost + " : " + sProxyPort +"'");									
					bReturn = objPing.ping(sProxyHost, sProxyPort);								
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Configured proxy reached. " + sProxyHost + " : " + sProxyPort +"'");
					bReturn = true;
					break main;
					
				}catch(ExceptionZZZ ez){
					objKernel.getLogObject().WriteLineDate("Will not use the proxy configured, because: " + ez.getDetailAllLast());
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Configured proxy unreachable. " + sProxyHost + " : " + sProxyPort +"'. No proxy will be enabled.");
				}	
			}else{
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "No proxy configured.");								
			}//END 	if(sProxyHost!=null && sProxyHost.equals("")==false){		//Eine Proxy-Konfiguration ist nicht Pflicht		
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
			if(stemp.equals("useproxy")){
				bFunction = bFlagUseProxy;
				break main;			
				/*
			}else if(stemp.equals("isconnected")){
				bFunction = bFlagIsConnected;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}else if(stemp.equals("portscanallfinished")){				
				bFunction = bFlagPortScanAllFinished;
				break main; 
				*/
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
		if(stemp.equals("useproxy")){
			bFlagUseProxy = bFlagValue;
			bFunction = true;			
			break main;
			/*
		}else if(stemp.equals("isconnected")){
			bFlagIsConnected = bFlagValue;
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
			*/
		}
		}//end main:
		return bFunction;
	}
	
	
	
	/**Aus dem Worker-Thread heraus wird ein Thread gestartet (der sich in die EventQueue von Swing einreiht.)
	* @param stext
	* 
	* lindhaueradmin; 17.01.2007 12:09:17
	 */
	public void updateLabel(String stext){
		this.sText2Update = stext;
		
//		Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
		Runnable runnerUpdateLabel= new Runnable(){

			public void run(){
//				In das Textfeld den gefundenen Wert eintragen, der Wert ist ganz oben als private Variable deklariert			
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField 'text1");				
				JTextField textField = (JTextField) panel.getComponent("text1");					
				textField.setText(sText2Update);
				textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
			}
		};
		
		SwingUtilities.invokeLater(runnerUpdateLabel);	
		
//		In das Textfeld eintragen, das etwas passiert.								
		//JTextField textField = (JTextField) panelParent.getComponent("text1");					
		//textField.setText("Lese aktuellen Wert .....");
		
	}


	@Override
	public String getProgramAlias() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return null;
	}
}

