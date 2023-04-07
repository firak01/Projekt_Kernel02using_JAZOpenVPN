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
import basic.zBasic.util.crypt.code.ICryptZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
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
public class ProgramFTPCredentials2iniOVPN extends AbstractProgram2iniOVPN implements IConstantProgramFTPCredentialsOVPN{
	private String sUsernameFromUi=null;
	private String sPasswordFromUi=null;

	//Keine Flags gesetzt
	//private boolean bFlagUseProxy = false;

	
	public ProgramFTPCredentials2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
		main:{			
			this.setPanelParent(panel);			
		}//END main
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
			KernelJPanelCascadedZZZ panel = this.getPanelParent();
			JTextField textField = (JTextField) panel.getComponent(sCOMPONENT_TEXTFIELD_USERNAME);					
			sReturn = textField.getText();
		}
		return sReturn;
	}
	
	public String readPasswordFromUi() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			KernelJPanelCascadedZZZ panel = this.getPanelParent();
			JTextField textField = (JTextField) panel.getComponent(sCOMPONENT_TEXTFIELD_PASSWORD);					
			sReturn = textField.getText();
		}
		return sReturn;
	}
	
	public boolean writeCredentialsToIni(String sUsername, String sPasswordDecrypted) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{			
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			
			IKernelZZZ objKernel = this.getKernelObject();
			objKernel.setParameterByProgramAlias(sModule, sProgram, "FTPUsername", sUsername);
			
			CryptAlgorithmFactoryZZZ objCryptFactory = CryptAlgorithmFactoryZZZ.getInstance();
			ICryptZZZ objCrypt = objCryptFactory.createAlgorithmType(CryptAlgorithmMappedValueZZZ.CipherTypeZZZ.ROT13);
			
			TODOGOON20230407;//Wenn es fuer dieses Program einen Aliasnamen gibt, 
			                 //die Section [Aliasname] aber noch nicht in der ini Datei vorhanden ist,
			                 //eine entsprechende Section anlegen
			                 //und die Werte dort hineinschreiben.
			objKernel.setParameterByProgramAlias(sModule, sProgram, "FTPPassword", sPasswordDecrypted, objCrypt);
						
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
			 objKernel.setParameterByProgramAlias(sModule, sProgram, "CredentialDate", sNowDate);		
			 objKernel.setParameterByProgramAlias(sModule, sProgram, "CredentialTime", sNowTime);
			
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
		super.reset();
		this.sPasswordFromUi = ""; //Damit der Wert neu geholt wird.			
		this.sUsernameFromUi = ""; //Damit der Wert neu geholt wird.
	}
	
	@Override
	public void updateLabel(String stext) {
		updateLabel(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD, stext);
	}
}

