package use.openvpn.serverui.component.IPExternalUpload;


import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
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
public class ProgramIpWeb2iniOVPN extends KernelUseObjectZZZ{
	private String sModuleName=null;
	private String sProgramName=null;
	private String sIpFromUi=null;
	
	private KernelJPanelCascadedZZZ panel = null;
	private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Hier als Variable, damit die interne Runner-Klasse darauf zugreifen kann.
	// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.

	//Keine Flags gesetzt
	//private boolean bFlagUseProxy = false;

	
	public ProgramIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
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
			
			KernelJPanelCascadedZZZ panelParent = this.getPanelParent();
			String sProgramName = ""; 
			if(panelParent!=null){
				sProgramName = KernelUIZZZ.getProgramUsedName(panelParent);
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
	/**Merke: Man kann den konkreten Program Alias nicht ermitteln, wenn man nicht weiss, in welchen Wert er gesetzt werden soll.
	 *        Darum kann hier nur eine ArrayListe zurückgegeben werden.
	 * @return
	 * @throws ExceptionZZZ
	 */
	public ArrayList<String> getProgramAlias() throws ExceptionZZZ{
		ArrayList<String> listasReturn = new ArrayList<String>();
		main:{			
		IKernelZZZ objKernel = this.getKernelObject();
		
		FileIniZZZ objFileIniConfig = objKernel.getFileConfigIni();
		String sMainSection = this.getModuleName();
		String sProgramName = this.getProgramName();
		String sSystemNumber = objKernel.getSystemNumber();
		listasReturn = objKernel.getProgramAliasUsed(objFileIniConfig, sMainSection, sProgramName, sSystemNumber);

		}//end main:
		return listasReturn;
	}
	/**Gehe die ProgramAlias-Namen durch und prüfe, wo der Wert gesetzt ist . 
	 * Das ist dann der verwendete Alias.
	 * @param sPropertyName
	 * @return
	 * @throws ExceptionZZZ 
	 */
	public String getProgramAlias(String sProperty) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			ArrayList<String> listasProgramAlias = this.getProgramAlias();
			
			String sModule = this.getModuleUsed();
			FileIniZZZ objFileIniConfig = this.getKernelObject().getFileConfigIniByAlias(sModule);
			
			//+++ Als Program mit Alias:
			Iterator<String> itAlias = listasProgramAlias.iterator();
			while(itAlias.hasNext()){				
				String sProgramAliasUsed = itAlias.next();
				String sSection = sProgramAliasUsed;
				System.out.println(ReflectCodeZZZ.getMethodCurrentNameLined(0)+ ": (x) Verwende als sSection '"+ sSection + "' für die Suche nach der Property '" + sProperty + "'");
				if(!StringZZZ.isEmpty(sSection)){
					boolean bSectionExists = objFileIniConfig.proofSectionExists(sSection);
					if(bSectionExists==true){
						String sValue = objFileIniConfig.getPropertyValue(sSection, sProperty).getValue(); 
						if(sValue != null){
							System.out.println(ReflectCodeZZZ.getMethodCurrentNameLined(0)+ ": (x)Value gefunden für Property '" + sProperty + "'='" + sReturn + "'");
							sReturn = sSection;
							break main;
						}else{
							System.out.println(ReflectCodeZZZ.getMethodCurrentNameLined(0)+ ": (x) Kein Value gefunden in Section '" + sSection + "' für die Property: '" + sProperty + "'.");
						}
					}
				}
			}
			System.out.println(ReflectCodeZZZ.getMethodCurrentNameLined(0)+ ": (x) Keinen Value gefunden in einem möglichen Programalias. Suche direkter nach der Property.'" + sProperty +"'.");			
		}
		return sReturn;
	}

	public String getProgramName(){
		return this.sProgramName;
	}
	public void setProgramName(String sProgramName){
		this.sProgramName = sProgramName;
	}
	
	public String getModuleName(){
		return this.sModuleName;
	}
	public void setModuleName(String sModuleName){
		this.sModuleName=sModuleName;
	}
	
	public String getIpFromUi() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sIpFromUi)){
			String stemp = this.readIpFromUi();
			this.sIpFromUi = stemp;
		}
		return this.sIpFromUi;
	}
	
	public String readIpFromUi() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			KernelJPanelCascadedZZZ panel = this.getPanelParent();
			JTextField textField = (JTextField) panel.getComponent("textIpContent");					
			sReturn = textField.getText();
		}
		return sReturn;
	}
	
	public boolean writeIpToIni(String sIp) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{			
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			
			IKernelZZZ objKernel = this.getKernelObject();
			objKernel.setParameterByProgramAlias(sModule, sProgram, "IPExternal", sIp);
						
			bReturn = true;
		}
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
			/*
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
		String stemp = sFlagName.toLowerCase();
		/*
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
	
	public void reset() {
		this.sIpFromUi = ""; //Damit der Wert neu geholt wird.	
		this.sText2Update = ""; 
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
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField 'textIpContent");				
				JTextField textField = (JTextField) panel.getComponent("textIpContent");					
				textField.setText(sText2Update);
				textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
			}
		};
		
		SwingUtilities.invokeLater(runnerUpdateLabel);	
		
//		In das Textfeld eintragen, das etwas passiert.								
		//JTextField textField = (JTextField) panelParent.getComponent("text1");					
		//textField.setText("Lese aktuellen Wert .....");
		
	}
}

