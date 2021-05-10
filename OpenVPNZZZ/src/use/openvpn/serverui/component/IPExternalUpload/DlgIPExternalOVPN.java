package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import custom.zKernelUI.component.PanelDialogContentEmptyZZZ;

/**
 * @author 0823
 *
 */
public class DlgIPExternalOVPN extends KernelJDialogExtendedZZZ {
	PanelDlgIPExternalContentOVPN panelContent = null;
	PanelDlgIPExternalButtonAlternativeVIA panelButton = null;
	
	/**
	 * @param owner
	 * @param bModal
	 * @param bSnappedToScreen
	 * @param panelCenter
	 */
	public DlgIPExternalOVPN(IKernelZZZ objKernel, KernelJFrameCascadedZZZ frameOwner, HashMap<String, Boolean> hmFlag) throws ExceptionZZZ {		
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
	
	@Override
	public KernelJPanelCascadedZZZ getPanelButton(){
		return null; //Damit gibt es im Dialog nur die Standardbuttons.
	}
	
	@Override
	public KernelJPanelCascadedZZZ getPanelContent() throws ExceptionZZZ{
		if(this.panelContent==null) {
			PanelDlgIPExternalContentOVPN panelContent = new PanelDlgIPExternalContentOVPN(this.getKernelObject(), this);
			this.panelContent=panelContent;
		}
		return this.panelContent;
	}
	
	@Override
	public KernelJPanelCascadedZZZ getPanelNavigator() throws ExceptionZZZ{
		PanelDialogContentEmptyZZZ panelNavigator = new PanelDialogContentEmptyZZZ(this.getKernelObject(), this);
		return panelNavigator;
	}
	
	
}

