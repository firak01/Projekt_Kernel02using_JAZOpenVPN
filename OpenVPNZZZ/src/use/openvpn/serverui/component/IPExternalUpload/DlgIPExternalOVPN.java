package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.util.HashMap;

import basic.zKernel.IKernelZZZ;

import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;

/**
 * @author 0823
 *
 */
public class DlgIPExternalOVPN extends KernelJDialogExtendedZZZ {

	/**
	 * @param owner
	 * @param bModal
	 * @param bSnappedToScreen
	 * @param panelCenter
	 */
	public DlgIPExternalOVPN(IKernelZZZ objKernel, KernelJFrameCascadedZZZ frameOwner, HashMap<String, Boolean> hmFlag) {		
		super(objKernel, frameOwner, false, hmFlag);  //true, d.h. modal, geht leider nur im Konstruktor zu �bergeben, weil JDialog diesen Parameter im Konstruktor braucht und Super(...) kann keinen Code beinhalten, der auf eigene Properties etc. zugreift.
	}
	public boolean isCentered(){
		return false;
	}	

	public boolean isJComponentSnappedToScreen(){
		return true;
	}
	public boolean isButtonCancelAvailable(){
		return true;
	}
	public boolean isButtonOKAvailable(){
		return true;
	}
	public String getText4ButtonOk(){
		return  "USE VALUE";
	}
	public KernelJPanelCascadedZZZ getPanelButton(){
		PanelDlgIPExternalButtonAlternativeVIA panelButton = new PanelDlgIPExternalButtonAlternativeVIA(this.getKernelObject(), this, this.isButtonOKAvailable(), this.isButtonCancelAvailable());
		return panelButton;
	}
	public KernelJPanelCascadedZZZ getPanelContent(){
		PanelDlgIPExternalContentVIA panelContent = new PanelDlgIPExternalContentVIA(this.getKernelObject(), this);
		return panelContent;
	}

}

