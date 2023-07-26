package use.openvpn.serverui.component.IPExternalUpload;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.LogZZZ;
import custom.zKernel.file.ini.FileIniZZZ;
import custom.zKernel.html.writer.WriterHtmlZZZ;
import custom.zKernel.markup.content.ContentPageIPZZZ;
import custom.zKernel.net.ftp.FTPSZZZ;
import custom.zKernel.net.ftp.SFTPZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.IApplicationUserOVPN;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelConfigZZZ;
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
import basic.zKernel.KernelZZZ;
import basic.zKernel.component.AbstractKernelProgramZZZ;
import basic.zKernel.file.ini.IKernelCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelEncryptionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelExpressionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJavaCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonArrayIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonMapIniSolverZZZ;
import basic.zKernel.file.ini.IKernelZFormulaIniSolverZZZ;
import basic.zKernel.html.ConfigHtmlTableHandlerByJobZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.job.IJobZZZ;
import basic.zKernel.job.JobHtmlProviderZZZ;
import basic.zKernel.net.client.ConfigHtmlTableHandlerZZZ;
import basic.zKernel.net.client.HtmlTableHandlerApplicationZZZ;
import basic.zKernel.net.client.IApplicationZZZ;
import basic.zKernel.net.client.IMainZZZ;
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
public class ProgramPageWebCreateUiOVPN  extends AbstractKernelProgramUIZZZ implements IConstantProgramPageWebCreateUiOVPN, IApplicationUserOVPN{
	private IApplicationOVPN objApplication = null;
	private KernelJPanelCascadedZZZ panel = null;
	private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Hier als Variable, damit die interne Runner-Klasse darauf zugreifen kann.
	// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.

	private boolean bFlagUseProxy = false;
	
	public ProgramPageWebCreateUiOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
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
			//### Neuere Version: Mit Job und Xslt
			try {
				//Parameter aus args auslesen
				String[]saFlag = {IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION.name(),IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA.name(), IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA_MATH.name(),IKernelJsonIniSolverZZZ.FLAGZ.USEJSON.name(),IKernelJsonArrayIniSolverZZZ.FLAGZ.USEJSON_ARRAY.name(),IKernelJsonMapIniSolverZZZ.FLAGZ.USEJSON_MAP.name(), IKernelEncryptionIniSolverZZZ.FLAGZ.USEENCRYPTION.name(),IKernelCallIniSolverZZZ.FLAGZ.USECALL.name(), IKernelJavaCallIniSolverZZZ.FLAGZ.USECALL_JAVA.name()};
				//IKernelConfigZZZ objConfig = new ConfigHtmlTableHandlerByJobZZZ(saArg, saFlag);
				IKernelConfigZZZ objConfig = new ConfigHtmlTableHandlerByJobZZZ();
				IKernelZZZ objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man über die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.
				this.setKernelObject(objKernel);
								
				//############
				IApplicationZZZ objApplication = new HtmlTableHandlerApplicationZZZ(objKernel, null);
				objApplication.setMainObject((IMainZZZ) this);
				this.setApplicationObject((IApplicationOVPN) objApplication);
				
				//TEST:
				String sUrl = objApplication.getUrlToParse();
				System.out.println("URL to parse ='" + sUrl + "'");
				
				//############## DAS UI handlebar machen
				//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
				//this.objClientTray = new ClientTrayUIZZZ(objKernel, this.objClientMain, (String[]) null);
				
	
				//############## DAS BACKEND handlebar machen, nutze anderes Modul, also eine andere ini-Datei...
				//Wie die JobSteps, so muss auch der Job das Modul in einer anderen ini-Datei haben, also das andere Kernel-Objekt!!!
												
				//### Arbeite mit einem JOB, der in einem anderen Projekt liegt. Also auch eine andere ini - Datei hat				
				IKernelConfigZZZ objConfigTableHandlerModule = new ConfigHtmlTableHandlerZZZ();
				String sProjectPathCalling=objConfig.getProjectPath();//Die Verknüpfung zur "Startapplikation".
				objConfigTableHandlerModule.setCallingProjectPath(sProjectPathCalling);//Der aufrufende Pfad ist dann zur Errechnung der Projektpfade wichtig
				
				IKernelZZZ objKernelTableHandlerModule = new KernelZZZ(objConfigTableHandlerModule, (String) null); //Damit kann man über die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.				
				IJobZZZ objJobHtml = new JobHtmlProviderZZZ(objKernelTableHandlerModule, objApplication);
				boolean bSuccess = objJobHtml.process();
				if(bSuccess) {
					System.out.println("Erfolgreich verarbeitet.");
				}else {
					System.out.println(("NICHT erfolgreich verarbeitet"));
				}
			} catch (ExceptionZZZ ez) {
				if(objKernel!=null){
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog!=null){
						objLog.WriteLineDate(ez.getDetailAllLast());
					}else{
						ez.printStackTrace();
						System.out.println(ez.getDetailAllLast());
					}				
				}else{
					ez.printStackTrace();
					System.out.println(ez.getDetailAllLast());
				}
			}//END Catch
		}//END main:
		System.out.println("finished starting application.");
		return bReturn;	
		
//########## Alte Version ohne Job und Xslt
//			boolean btemp; 
//			try {
//
//		//1. Erstellen das Z-Kernel Objekt							
//		IKernelZZZ objKernel = this.getKernelObject(); //KernelSingletonZZZ.getInstance("FGL", "01", "", "ZKernelConfigFTP_test.ini",(String[]) null);
//		
//		//2. Protokoll
//		IKernelLogZZZ objLog = objKernel.getLogObject();
//		String sLog=null;
//		
//		sLog="ReflectCodeZZZ.getPositionCurrent() + :";
//		objLog.WriteLineDate(sLog);
//		
//		sLog = "KernelIniFile= '" + objKernel.getFileConfigKernel().getAbsolutePath() + "'";
//		objLog.WriteLineDate(sLog);		
//				
////		
////		//4. Konfiguration auslesen
////		//Hier werden Informationen ueber die IP-Adressdatei ausgelesen, etc.
//		String sModule = this.getModuleName();
//		FileIniZZZ objFileIniIPConfig = objKernel.getFileConfigModuleIni(sModule);
//		sLog = "objFileIniIPConfig absoluter Pfad='"+objFileIniIPConfig.getFileObject().getAbsolutePath()+"'";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
//		objLog.WriteLineDate(sLog);
//		
//		
////		//Programname nicht aus dem Panel, sondern das Program selbst
//		String sProgram = this.getProgramName();
//		sLog = "Program='"+sProgram+"'";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
//		objLog.WriteLineDate(sLog);
//		
//		//20210216: Hier wird normalerweise ein ggfs. gecachter Wert geholt. 
//		//          Dadurch wird sich nie eine Änderung ergeben, die ja durch ein anderes Program erzeugt wurde.
//		//          Diesen Cache Zugriff kann man nun abstellen.
//		IKernelConfigSectionEntryZZZ entryServer = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPExternal", false);
//		
//		sLog = "entryServer.getSection()='"+entryServer.getSection()+"'";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
//		objLog.WriteLineDate(sLog);
//		
//		sLog = "entryServer.getSystemNumber()='"+entryServer.getSystemNumber()+"'";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
//		objLog.WriteLineDate(sLog);
//
//		
//		String sIP = entryServer.getValue();	
//		sLog = "sIP='"+sIP+"'";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
//		objLog.WriteLineDate(sLog);
//		
//		IKernelConfigSectionEntryZZZ entryDate = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPDate");
//		String sIPDate = entryDate.getValue();
//		
//		IKernelConfigSectionEntryZZZ entryTime =objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPTime"); 
//		String sIPTime = entryTime.getValue();
//		sLog = "Page Generator - IP Detail read from file: "+sIP + " ("+sIPDate+" - "+sIPTime+")";
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": "+sLog);
//		objLog.WriteLineDate(sLog);
//		
//		//Den alten IP Wert aus der Ini-Datei holen
//		//IKernelConfigSectionEntryZZZ entryServerPrevious = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram,"IPExternalPrevious", false);
//		//String sIPPrevious = entryServerPrevious.getValue();
//		//System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": sIPPrevious='"+sIPPrevious+"'");
//		
//		//if(!sIP.equals(sIPPrevious)){
//			//System.out.println("PageGenerator - geaenderter Wert fuer die IP-Adresse. Erstelle neue HTML-Datei.");
//		sLog = "PageGenerator - Unabhängig von bisheriger IP-Adresse. Erstelle neue HTML-Datei.";				
//		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": "+sLog);
//		objLog.WriteLineDate(sLog);	
//		
//			//Nur eine neue Datei erzeugen, wenn es auch eine neue IPNr gibt
//			//Create a Content Store object, here: pass the IP Details as Variable 
//			ContentPageIPZZZ objPageStorage = new ContentPageIPZZZ(objKernel, (String[]) null);
//		   
//			sLog="PageGenerator - Unabhängig von bisheriger IP-Adresse. Erstelle neue HTML-Datei.";
//			System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": "+sLog);
//			objLog.WriteLineDate(sLog);	
//			
//			//Diese Variablen werden nun in das Storage-Objekt �bertragen	
//		   objPageStorage.setVar("IPNr",sIP);
//		   
//		   
//		   //FGL Nun den aktuellen Datums- und Zeitwert eintragen
//		   //TODO Komfortklasse entwickeln, die haeuufig verwendete Zeit-Datumsformate anbietet.
//		 GregorianCalendar d = new GregorianCalendar();
//		 Integer iDateYear = new Integer(d.get(Calendar.YEAR));
//		 Integer iDateMonth = new Integer(d.get(Calendar.MONTH) + 1);
//		 Integer iDateDay = new Integer(d.get(Calendar.DAY_OF_MONTH));
//		 Integer iTimeHour = new Integer(d.get(Calendar.HOUR_OF_DAY));
//		 Integer iTimeMinute = new Integer(d.get(Calendar.MINUTE)); 			
//
//		String sDate = iDateYear.toString() + "-" + iDateMonth.toString() + "-" + iDateDay.toString();
//		String sTime = iTimeHour.toString() + ":" + iTimeMinute.toString(); 
//
//		objPageStorage.setVar("IPDate",sDate);
//		objPageStorage.setVar("IPTime", sTime);
//		   
//		//Erstellen einer Hashmap, die auf Jakarta-ECS basierende Elemente enth�lt 
//		//Dabei werden auch die Variablen ausgelesen und in die ECS-Elemente die entsprechenden Werte eingetragen
//		objPageStorage.compute();
//		   					   
//		   WriterHtmlZZZ objPageWriter = new WriterHtmlZZZ(objKernel, (String[]) null);
//		   btemp = objPageWriter.replaceContent(objPageStorage);
//		   if(btemp==true){
//			   objKernel.setParameterByProgramAlias(objFileIniIPConfig, sProgram, "PageDate",sDate, false);
//			   objKernel.setParameterByProgramAlias(objFileIniIPConfig, sProgram, "PageTime",sTime, true); //aus Performancegruenden nun erst speichern, also nur einmal am Schluss speichern
//			   
//			   IKernelConfigSectionEntryZZZ objEntryDirectory = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram, "CreationDirectory");
//			   String sDirectory = objEntryDirectory.getValue();
//			   
//			   IKernelConfigSectionEntryZZZ objEntryFile = objKernel.getParameterByProgramAlias(objFileIniIPConfig, sProgram, "CreationFile");
//			   String sFile = objEntryFile.getValue();
//			   
//			   String sFilePath = FileEasyZZZ.joinFilePathName(sDirectory, sFile);
//			   bReturn = objPageWriter.toFile(sFilePath);			   	
//		   }else {
//			   bReturn = false;
//		   }
////		}else {
////			System.out.println("PageGenerator - unveraenderter Wert fuer die IP-Adresse. Erstelle keine neue HTML-Datei.");		
////			bReturn = false;
////		}//end if( !sIP.equals(sIPPrevious)){
//			
//		} catch (ExceptionZZZ ez) {
//				System.out.println(ez.getDetailAllLast());
//		} 
//		System.out.println("Ende");
//	}//end main:
//		return bReturn;
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
	 * @throws ExceptionZZZ 
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
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
	public void updateLabel(String stext) throws ExceptionZZZ {
				
	}
	
	@Override
	public void updateValue(String stext) throws ExceptionZZZ {
		super.updateComponent(sCOMPONENT_LABEL_MESSAGE, stext);
	}
	
	@Override
	public void updateMessage(String stext) throws ExceptionZZZ {
		super.updateComponent(sCOMPONENT_LABEL_MESSAGE, stext);
	}


	@Override
	public IApplicationOVPN getApplicationObject() {
		return this.objApplication;
	}


	@Override
	public void setApplicationObject(IApplicationOVPN objApplication) {
		this.objApplication = objApplication;
	}
}

