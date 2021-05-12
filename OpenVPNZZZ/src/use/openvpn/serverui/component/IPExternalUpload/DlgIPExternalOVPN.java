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
	
	/**
	 * @param owner
	 * @param bModal
	 * @param bSnappedToScreen
	 * @param panelCenter
	 */
	public DlgIPExternalOVPN(IKernelZZZ objKernel, KernelJFrameCascadedZZZ frameOwner, HashMap<String, Boolean> hmFlag) throws ExceptionZZZ {		
		super(objKernel, frameOwner, false, hmFlag);  //true, d.h. modal, geht leider nur im Konstruktor zu �bergeben, weil JDialog diesen Parameter im Konstruktor braucht und Super(...) kann keinen Code beinhalten, der auf eigene Properties etc. zugreift.
		DlgIPExternalOVPN_();		
	}
	
	private boolean DlgIPExternalOVPN_() throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			PanelDlgIPExternalContentOVPN panelContent = new PanelDlgIPExternalContentOVPN(this.getKernelObject(), this);
			this.setPanelContent(panelContent);
			
			bReturn = true;
		}//end main:
		return bReturn;
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
	
	/* (non-Javadoc)
	 * @see basic.zKernelUI.component.KernelJDialogExtendedZZZ#getPanelContent()
	 */
	@Override
	public KernelJPanelCascadedZZZ getPanelContent() throws ExceptionZZZ{				
		PanelDlgIPExternalContentOVPN panelContent = new PanelDlgIPExternalContentOVPN(this.getKernelObject(), this);
		this.setPanelContent(panelContent);		
		return panelContent;
	}
	
	@Override
	public KernelJPanelCascadedZZZ getPanelNavigator() throws ExceptionZZZ{
		PanelDialogContentEmptyZZZ panelNavigator = new PanelDialogContentEmptyZZZ(this.getKernelObject(), this);
		this.setPanelNavigator(panelNavigator);
		return panelNavigator;
	}
	
	
}

