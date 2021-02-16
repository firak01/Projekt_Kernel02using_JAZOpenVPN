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
import basic.zKernel.component.AbstractKernelProgramZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.AbstractKernelProgramUIZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;

/**Vereinfacht den Zugriff auf die HTML-Seite, in der die externe IPAdresse des Servers bekannt gemacht wird. 
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwentet.
 * @author 0823
 *
 */
public class ProgramPageWebCreateOVPN  extends AbstractKernelProgramUIZZZ implements IConstantProgramPageWebCreateOVPN{
		
	private KernelJPanelCascadedZZZ panel = null;
	private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Hier als Variable, damit die interne Runner-Klasse darauf zugreifen kann.
	// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.

	private boolean bFlagUseProxy = false;
	
	public ProgramPageWebCreateOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, panel, saFlagControl);
		main:{			
			this.setPanelParent(panel);						
		}//END main
	}
		
	
	//### Getter / Setter
	public KernelJPanelCascadedZZZ getPanelParent(){
		return this.panel;
	}
	public void setPanelParent(KernelJPanelCascadedZZZ panel){
		this.panel = panel;
	}
	
	
	public boolean createPageWeb() throws ExceptionZZZ{
		boolean bReturn = false;
		System.out.println("Start");
		main:{
			boolean btemp; 
			try {

		//1. Erstellen das Z-Kernel Objekt							
		IKernelZZZ objKernel = this.getKernelObject(); //KernelSingletonZZZ.getInstance("FGL", "01", "", "ZKernelConfigFTP_test.ini",(String[]) null);
		
		//2. Protokoll
		IKernelLogZZZ objLog = objKernel.getLogObject();

//		//3. FTPZZZ-Objekt, als Wrapper um jakarta.commons.net.ftpclient
//		objFTP = new SFTPZZZ(objKernel, objLog, (String[]) null);
//		
//		//4. Konfiguration auslesen
//		//Hier werden Informationen ueber die IP-Adressdatei ausgelesen, etc.
		String sModule = this.getModuleName();
		FileIniZZZ objFileIniIPConfig = objKernel.getFileConfigIniByAlias(sModule);
//		
//		//Programname nicht aus dem Panel, sondern das Program selbst
		String sProgram = this.getProgramName();
		
		TODOGOON; //20210216: Hier wird allerdings ein ggfs. gecachter Wert geholt. 
		          //          Dadurch wird sich nie eine Änderung ergeben.
		          //          Merke: Wenn der erste Cache - Zugriff abgestellt wird,
		          //                 dann sollte ggfs. der zweite Cache - Zugriff (halt über die Formel) durchaus erlaubt sein.		
		IKernelConfigSectionEntryZZZ entryServer = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPExternal");
		String sIP = entryServer.getValue();
		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": sIP='"+sIP+"'");
		
		IKernelConfigSectionEntryZZZ entryDate = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPDate");
		String sIPDate = entryDate.getValue();
		
		IKernelConfigSectionEntryZZZ entryTime =objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPTime"); 
		String sIPTime = entryTime.getValue();
		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Page Generator - IP Detail read from file: "+sIP + " ("+sIPDate+" - "+sIPTime+")");

		IKernelConfigSectionEntryZZZ entryServerPrevious = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPExternalPrevious");
		String sIPPrevious = entryServer.getValue();
		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": sIPPrevious='"+sIPPrevious+"'");
		
		if(sIP.equals(sIPPrevious)==false){
			System.out.println("PageGenerator - neuer Wert fuer die IP-Adresse. Erstelle neue HTML-Datei.");
			//this.sIPNrPrevious=sIPNr;
			
			bReturn = true;
		}else {
			System.out.println("PageGenerator - unveraenderter Wert fuer die IP-Adresse. Erstelle keine neue HTML-Datei.");		
			bReturn = false;
		}
			
		
//		IKernelConfigSectionEntryZZZ entryUser = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"User");
//		String sUser = entryUser.getValue();
//		
//		IKernelConfigSectionEntryZZZ entryPassword = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"Password");
//		String sPassword = entryPassword.getValue();
//		
//		IKernelConfigSectionEntryZZZ entryRoot = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"RootPath");
//		String sRootPath = entryRoot.getValue();
//		
//		
//		System.out.println("Page Transfer - Login detail read from file: "+sServer + " ("+sUser+" - "+sPassword+"), RootPath='" + sRootPath + "'" );
//		objFTP.setServer(sServer);
//		objFTP.setUser(sUser);
//		objFTP.setPassword(sPassword);
//		objFTP.setRootPath(sRootPath);
//		
//			//5. Login
//		btemp = objFTP.makeConnection();
//		if (btemp==true) System.out.println("Connction - successfull, now transfering file");
//		
//		//6. Datei ermitteln und �bertragen
//		//TODO Mit eine @Z-Formel in der Konfiguration DIESES Programms auslesen, die auf den wert in der konfiguration eines anderen Programms hinweist.
//		//Hier die Konfiguration direkt auslesen
//		/*
//			 * TargetDirectory=c:\temp
//TargetFile=testpage.html
//			 */
//		
//		IKernelConfigSectionEntryZZZ entryDirSource=objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"SourceDirectory");
//		String sDirSource = entryDirSource.getValue();
//		
//		IKernelConfigSectionEntryZZZ entryFile = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"SourceFile");
//		String sFile = entryFile.getValue();
//		
//		//Also eigentlich objKernel.getParameterByProgramAlias(objFileIniIPConfgi, "ProgFTP","SourceFile");
//		String sFilePath = FileEasyZZZ.joinFilePathName(sDirSource, sFile);
//		File objFile = new File(sFilePath);
//		if(!FileEasyZZZ.exists(objFile)){
//			System.out.println("File not found '"+sFilePath);
//			bReturn = false;
//		}else{
//			IKernelConfigSectionEntryZZZ entryDirTarget=objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"TargetDirectory");
//			String sDirTarget = entryDirTarget.getValue();
//			StringZZZ.replace(sDirTarget, FileEasyZZZ.sDIRECTORY_SEPARATOR_WINDOWS, CharZZZ.toString(objFTP.getDirectorySeparatorRemote()));
//			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": DirTarget='"+sDirTarget+"'");
//			
//			//Dateiname bleibt ggfs. nicht gleich, also extra auslesen.
//			IKernelConfigSectionEntryZZZ entryFileTarget = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"TargetFile");
//			String sFileTarget = entryFileTarget.getValue();
//			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": FileTarget='"+sFileTarget+"'");
//															
//			bReturn = objFTP.uploadFile(objFile, sDirTarget, sFileTarget);
//		}
//		
//		
//		//7. Verbindung schliessen
//		if (bReturn==true){
//			System.out.println("Transfer - successfull, now disconnecting"); 
//		}else{
//			 System.out.println("Transfer - NOT successfull, now disconnecting");					
//		}
//		objFTP.closeConnection();
//
		} catch (ExceptionZZZ ez) {
				System.out.println(ez.getDetailAllLast());
		} 
		System.out.println("Ende");
	}//end main:
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
			/*
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("useproxy")){
				bFunction = bFlagUseProxy;
				break main;						
			}else if(stemp.equals("isconnected")){
				bFunction = bFlagIsConnected;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}else if(stemp.equals("portscanallfinished")){				
				bFunction = bFlagPortScanAllFinished;
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
		/*
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("useproxy")){
			bFlagUseProxy = bFlagValue;
			bFunction = true;			
			break main;
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
		}
		*/
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
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField '" + sCOMPONENT_TEXTFIELD + "'");				
				JTextField textField = (JTextField) panel.getComponent(sCOMPONENT_TEXTFIELD);					
				textField.setText(sText2Update);
				textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
			}
		};
		
		SwingUtilities.invokeLater(runnerUpdateLabel);			
	}
}

