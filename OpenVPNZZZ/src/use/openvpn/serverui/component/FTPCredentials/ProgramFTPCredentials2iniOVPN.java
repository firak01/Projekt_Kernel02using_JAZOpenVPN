package use.openvpn.serverui.component.FTPCredentials;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import use.openvpn.component.AbstractProgram2iniOVPN;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.crypt.code.CryptAlgorithmFactoryZZZ;
import basic.zBasic.util.crypt.code.CryptAlgorithmMappedValueZZZ;
import basic.zBasic.util.crypt.code.ICharacterPoolEnabledConstantZZZ;
import basic.zBasic.util.crypt.code.ICryptZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.component.AbstractKernelProgramZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.AbstractKernelProgramUIZZZ;
import basic.zKernelUI.component.IPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;


/**Vereinfacht den Zugriff auf die HTML-Seite, in der die externe IPAdresse des Servers bekannt gemacht wird. 
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwentet.
 * @author 0823
 *
 */
public class ProgramFTPCredentials2iniOVPN extends AbstractProgram2iniOVPN implements IConstantProgramFTPCredentialsOVPN{
	private String sUsernameFromUi=null;
	private String sPasswordFromUi=null;
	
	private String sPasswordEncodedWritten=null;
	private String sUsernameWritten=null;

	//Keine Flags gesetzt
	//private boolean bFlagUseProxy = false;

	
	public ProgramFTPCredentials2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
		main:{			
			this.setPanelParent(panel);			
		}//END main
	}
		
	
	//### Aus IResettableValues
	@Override
	public boolean reset() throws ExceptionZZZ{
		return super.reset();
	}
	
	@Override
	public boolean resetValues() throws ExceptionZZZ{
		super.resetValues();
		this.sPasswordFromUi = ""; //Damit der Wert neu geholt wird.			
		this.sUsernameFromUi = ""; //Damit der Wert neu geholt wird.
		return true;
	}
	
	//### Getter / Setter
	public String getUsernameFromUi() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sUsernameFromUi)){
			String stemp = this.readUsernameFromUi();
			this.sUsernameFromUi = stemp;
		}
		return this.sUsernameFromUi;
	}
	
	public String getPasswordFromUi() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sPasswordFromUi)){
			String stemp = this.readPasswordFromUi();
			this.sPasswordFromUi = stemp;
		}
		return this.sPasswordFromUi;
	}
	
	public String readUsernameFromUi() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IPanelCascadedZZZ panelParent = this.getPanelParent();
			IPanelCascadedZZZ panelContent = panelParent.searchPanel("CONTENT"); 
			JTextField textField = (JTextField) panelContent.getComponent(sCOMPONENT_TEXTFIELD_USERNAME);					
			sReturn = textField.getText();
		}
		return sReturn;
	}
	
	public String readPasswordFromUi() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			KernelJPanelCascadedZZZ panelParent = this.getPanelParent();
			IPanelCascadedZZZ panelContent = panelParent.searchPanel("CONTENT"); 
			JTextField textField = (JTextField) panelContent.getComponent(sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED);					
			sReturn = textField.getText();
		}
		return sReturn;
	}
	
	public String getUsernameWritten() {
		return this.sUsernameWritten;
	}
	private void setUsernameWritten(String sUsername) {
		this.sUsernameWritten=sUsername;
	}
	
	public String getPasswordEncodedWritten() {
		return this.sPasswordEncodedWritten;
	}
	private void setPasswordEncodedWritten(String sPassword) {
		this.sPasswordEncodedWritten=sPassword;
	}
	
	
	public boolean writeCredentialsToIni(String sUsername, String sPasswordDecrypted) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{			
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			
			IKernelZZZ objKernel = this.getKernelObject();
			objKernel.setParameterByProgramAlias(sModule, sProgram, this.sINI_PROPERTY_USERNAME, sUsername);
			this.setUsernameWritten(sUsername);
			
			CryptAlgorithmFactoryZZZ objCryptFactory = CryptAlgorithmFactoryZZZ.getInstance();
			//ICryptZZZ objCrypt = objCryptFactory.createAlgorithmType(CryptAlgorithmMappedValueZZZ.CipherTypeZZZ.ROT13);
			//Gewuenscht ist folgendes:
			//<Z><Z:Encrypted><Z:Cipher>VigenereNn</Z:Cipher><z:KeyString>Hundi</z:KeyString><z:CharacterPool> abcdefghijklmnopqrstuvwxyz</z:CharacterPool><z:CharacterPoolAdditional>!</z:CharacterPoolAdditional><z:FlagControl>USEUPPERCASE,USENUMERIC,USELOWERCASE,USEADDITIONALCHARACTER</Z:FlagControl><Z:Code>8kBiFyIsAhNOD</Z:Code></Z:Encrypted></Z>
			ICryptZZZ objCrypt = objCryptFactory.createAlgorithmType(CryptAlgorithmMappedValueZZZ.CipherTypeZZZ.VIGENEREnn);
			objCrypt.setCryptKey("Hundi");
			
			objCrypt.setFlag(ICharacterPoolEnabledConstantZZZ.FLAGZ.USESTRATEGY_CHARACTERPOOL.name(), true);
			objCrypt.setCharacterPoolBase(" abcdefghijklmnopqrstuvwxyz");
			objCrypt.setFlag(ICharacterPoolEnabledConstantZZZ.FLAGZ.USEUPPERCASE.name(), true);
			objCrypt.setFlag(ICharacterPoolEnabledConstantZZZ.FLAGZ.USELOWERCASE.name(), true);
			objCrypt.setFlag(ICharacterPoolEnabledConstantZZZ.FLAGZ.USENUMERIC.name(), true);
			objCrypt.setFlag(ICharacterPoolEnabledConstantZZZ.FLAGZ.USEADDITIONALCHARACTER.name(), true);
			objCrypt.setCharacterPoolAdditional("!");
			
			String sPasswordEncrypted = objCrypt.encrypt(sPasswordDecrypted);
			
			//Merke: Wenn es fuer dieses Program einen Aliasnamen gibt, 
			//die Section [Aliasname] aber noch nicht in der ini Datei vorhanden ist,
			//dann wird eine entsprechende Section angelegt und die Werte dort hineingeschrieben.
			objKernel.setParameterByProgramAliasEncrypted(sModule, sProgram, this.sINI_PROPERTY_PASSWORD, sPasswordEncrypted, objCrypt);
			this.setPasswordEncodedWritten(sPasswordEncrypted);		
			
			long lTime = System.currentTimeMillis();
			Date objDate = new Date(lTime);
			
			 GregorianCalendar d = new GregorianCalendar();
			 Integer iDateYear = new Integer(d.get(Calendar.YEAR));
			 Integer iDateMonth = new Integer(d.get(Calendar.MONTH) + 1);
			 Integer iDateDay = new Integer(d.get(Calendar.DAY_OF_MONTH));
			 Integer iTimeHour = new Integer(d.get(Calendar.HOUR_OF_DAY));
			 Integer iTimeMinute = new Integer(d.get(Calendar.MINUTE)); 			
				
			 String sNowDate = iDateYear.toString() + "-" + iDateMonth.toString() + "-" + iDateDay.toString();
			 String sNowTime = iTimeHour.toString() + ":" + iTimeMinute.toString(); 		     			
			 objKernel.setParameterByProgramAlias(sModule, sProgram, this.sINI_PROPERTY_CREDENTIALDATE, sNowDate);		
			 objKernel.setParameterByProgramAlias(sModule, sProgram, this.sINI_PROPERTY_CREDENTIALTIME, sNowTime);
			
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
			//String stemp = sFlagName.toLowerCase();
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
	 * @see AbstractKernelUseObjectZZZ.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
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
		//String stemp = sFlagName.toLowerCase();
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
	
	
	
	@Override
	public void updateLabel(String stext) throws ExceptionZZZ {
		
	}
	
	@Override
	public void updateValue(String stext) throws ExceptionZZZ {
		super.updateComponent(sCOMPONENT_LABEL_PASSWORD_ENCRYPTED, stext);
	}
	
	@Override
	public void updateMessage(String stext) throws ExceptionZZZ {
		super.updateComponent(sCOMPONENT_LABEL_MESSAGE, stext);
	}
}

