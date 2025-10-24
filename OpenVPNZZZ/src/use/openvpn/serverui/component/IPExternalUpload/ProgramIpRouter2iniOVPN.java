package use.openvpn.serverui.component.IPExternalUpload;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import use.openvpn.clientui.component.IPExternalRead.IConstantProgramIpWebOVPN;
import use.openvpn.component.AbstractProgram2iniOVPN;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
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
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;


/**Vereinfacht den Zugriff auf die HTML-Seite, in der die externe IPAdresse des Servers bekannt gemacht wird. 
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwentet.
 * @author 0823
 *
 */
public class ProgramIpRouter2iniOVPN  extends AbstractProgram2iniOVPN implements IConstantProgramIpRouterOVPN{
	private String sIpFromUi=null;
	
	//Keine Flags gesetzt
	//private boolean bFlagUseProxy = false;

	
	public ProgramIpRouter2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
		main:{			
			this.setPanelParent(panel);			
		}//END main
	}
		
	//Aus IResettableValuesZZZ
	@Override
	public boolean reset() throws ExceptionZZZ{
		return super.reset();
	}
	
	@Override 
	public boolean resetValues() throws ExceptionZZZ{		
		boolean bReturn = false;
		main:{
			bReturn = super.resetValues();
			if(!bReturn & this.sIpFromUi!=null) bReturn = true;
			this.sIpFromUi = null; //Damit der Wert neu geholt wird.
		}//end main:
		return bReturn;
	}

	@Override
	public boolean resetValues(Object objDefault) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			bReturn = super.resetValues();
			if(!bReturn & this.sIpFromUi!=null) bReturn = true;
			this.sIpFromUi = objDefault.toString(); //Damit der Wert neu geholt wird.
		}//end main:
		return bReturn;		
	}
	

	//### Getter / Setter	
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
			JTextField textField = (JTextField) panel.getComponent(sCOMPONENT_TEXTFIELD);					
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
			 objKernel.setParameterByProgramAlias(sModule, sProgram, "IPDate", sNowDate);		
			 objKernel.setParameterByProgramAlias(sModule, sProgram, "IPTime", sNowTime);
			
			bReturn = true;
		}
		return bReturn;
		
	}
	
	
	
////	######### GetFlags - Handled ##############################################
//	/** (non-Javadoc)
//	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
//	Flags used:<CR>
//	-  isConnected
//	- useProxy
//	- haserror
//	 */
//	public boolean getFlag(String sFlagName){
//		boolean bFunction = false;
//		main:{
//			if(StringZZZ.isEmpty(sFlagName)) break main;
//			bFunction = super.getFlag(sFlagName);
//			if(bFunction==true) break main;
//							
//			//getting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			/*
//			if(stemp.equals("useproxy")){
//				bFunction = bFlagUseProxy;
//				break main;						
//			}else if(stemp.equals("isconnected")){
//				bFunction = bFlagIsConnected;
//				break main;
//			}else if(stemp.equals("haserror")){				
//				bFunction = bFlagHasError;
//				break main;
//			}else if(stemp.equals("portscanallfinished")){				
//				bFunction = bFlagPortScanAllFinished;
//				break main; 				
//			}
//			*/
//		}//end main:
//		return bFunction;
//	}
	
	


//	/**
//	 * @see AbstractKernelUseObjectZZZ.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
//	 * @param sFlagName
//	 * Flags used:<CR>
//	 * - isconnected
//	 * - useproxy
//	 * - haserror
//	 * - PortScanAllFinished //das ist zusammen mit "isconnected" das Zeichen f�r den ConnectionMonitor des Frontends, das er starten darf. Grund: Die PortScans f�hren ggf. zu timeouts.
//	 * @throws ExceptionZZZ 
//	 */
//	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
//		boolean bFunction = false;
//		main:{
//			if(StringZZZ.isEmpty(sFlagName)) break main;
//			bFunction = super.setFlag(sFlagName, bFlagValue);
//			if(bFunction==true) break main;
//	
//		//setting the flags of this object
//		String stemp = sFlagName.toLowerCase();
//		/*
//		if(stemp.equals("useproxy")){
//			bFlagUseProxy = bFlagValue;
//			bFunction = true;			
//			break main;			
//		}else if(stemp.equals("isconnected")){
//			bFlagIsConnected = bFlagValue;
//			bFunction = true;
//			break main;
//		}else if(stemp.equals("haserror")){
//			bFlagHasError = bFlagValue;
//			bFunction = true;
//			break main;
//		}else if(stemp.equals("portscanallfinished")){
//			bFlagPortScanAllFinished = bFlagValue;
//			bFunction = true;
//			break main;			
//		}
//		*/
//		}//end main:
//		return bFunction;
//	}
	
	
	@Override
	public void updateLabel(String stext) throws ExceptionZZZ {
		
	}
	
	@Override
	public void updateValue(String stext) throws ExceptionZZZ {
		updateComponent(IConstantProgramIpRouterOVPN.sCOMPONENT_TEXTFIELD, stext);
	}

	@Override
	public void updateMessage(String stext) throws ExceptionZZZ {
		updateComponent(IConstantProgramIpRouterOVPN.sCOMPONENT_TEXTFIELD, stext);
	}

	
}

