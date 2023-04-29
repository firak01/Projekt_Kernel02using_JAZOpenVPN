package use.openvpn.clientui.component.IPExternalRead;


import java.util.ArrayList;
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
public class ProgramIpWeb2iniOVPN extends AbstractProgram2iniOVPN implements IConstantProgramIpWebOVPN{
	private String sIpFromUi=null;

	//Keine Flags gesetzt
	//private boolean bFlagUseProxy = false;

	public ProgramIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
		main:{			
			this.setPanelParent(panel);			
		}//END main
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
			KernelJPanelCascadedZZZ panelCenter = (KernelJPanelCascadedZZZ) panel.getPanelNeighbour("CENTER");
			JTextField textField = (JTextField) panelCenter.getComponent(sCOMPONENT_TEXTFIELD);
			if(textField!=null) {
				sReturn = textField.getText();
			}
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
	
	
	//### Aus Interfaces und abstrakten Klassen
	@Override
	public void updateLabel(String stext) throws ExceptionZZZ {

	}
	
	@Override
	public void updateValue(String stext) throws ExceptionZZZ {
		super.updateComponent(IConstantProgramIpWebOVPN.sCOMPONENT_TEXTFIELD, stext);//Merke: ggfs. gibt es das Feld sogar gar nicht
	}

	@Override
	public void updateMessage(String stext) throws ExceptionZZZ {
		
	}

	public void reset() {
		super.reset();
		this.sIpFromUi = ""; //Damit der Wert neu geholt wird.			
	}
}

