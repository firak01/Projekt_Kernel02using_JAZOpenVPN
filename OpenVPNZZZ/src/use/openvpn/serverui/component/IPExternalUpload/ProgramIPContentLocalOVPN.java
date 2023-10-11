package use.openvpn.serverui.component.IPExternalUpload;


import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import use.openvpn.component.AbstractProgramIPContentOVPN;
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
//20210222 Nutze abstrakt, Package use.openvpn.common
//         Mache dann ProgramIPContentWebOVPN und ProgramIPConententLocalOVPN extends AbstractProgramIPContenOVPN
public class ProgramIPContentLocalOVPN extends AbstractProgramIPContentOVPN implements IConstantProgramIpLocalOVPN{		
	public ProgramIPContentLocalOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
	}
		
	
	//### Getter / Setter

	//######### GetFlags - Handled ##############################################

	//### METHODEN
		
	// ######### Auftruf abstrakter Methoden ######################################		
	/**Aus dem Worker-Thread heraus wird ein Thread gestartet (der sich in die EventQueue von Swing einreiht.)
	* @param stext
	* 
	* lindhaueradmin; 17.01.2007 12:09:17
	 */
	@Override
	public void updateLabel(String stext) throws ExceptionZZZ {
		
	}	
	
	@Override
	public void updateValue(String stext) throws ExceptionZZZ {
		super.updateComponent(sCOMPONENT_VALUE, stext);
	}
	
	@Override
	public void updateMessage(String stext) throws ExceptionZZZ {
		
	}
}

