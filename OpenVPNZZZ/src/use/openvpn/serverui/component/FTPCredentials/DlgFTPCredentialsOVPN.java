package use.openvpn.serverui.component.FTPCredentials;

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
public class DlgFTPCredentialsOVPN extends KernelJDialogExtendedZZZ {	
	
	/**
	 * @param owner
	 * @param bModal
	 * @param bSnappedToScreen
	 * @param panelCenter
	 */
	public DlgFTPCredentialsOVPN(IKernelZZZ objKernel, KernelJFrameCascadedZZZ frameOwner, HashMap<String, Boolean> hmFlag) throws ExceptionZZZ {		
		super(objKernel, frameOwner, false, hmFlag);  //true, d.h. modal, geht leider nur im Konstruktor zu �bergeben, weil JDialog diesen Parameter im Konstruktor braucht und Super(...) kann keinen Code beinhalten, der auf eigene Properties etc. zugreift.
		DlgFTPCredentialsOVPN_();		
	}
	
	private boolean DlgFTPCredentialsOVPN_() throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			//Panels werden über die createPanelContent(), etc. Methoden an die passende Stelle eingebunden.			
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
	public KernelJPanelCascadedZZZ createPanelButton() throws ExceptionZZZ{
		//return null; //Damit gibt es im Dialog nur die Standardbuttons.
		
		PanelDlgFTPCredentialsButtonAlternativeOVPN panelButton = new PanelDlgFTPCredentialsButtonAlternativeOVPN(this.getKernelObject(), this,true,true);//Den CLOSE Button weglassen.
		panelButton.setAlias("BUTTON");
		return panelButton;
	}
	
	/* (non-Javadoc)
	 * @see basic.zKernelUI.component.KernelJDialogExtendedZZZ#getPanelContent()
	 */
	@Override
	public KernelJPanelCascadedZZZ createPanelContent() throws ExceptionZZZ{				
		PanelDlgFTPCredentialsContentOVPN panelContent = new PanelDlgFTPCredentialsContentOVPN(this.getKernelObject(), this);
		panelContent.setAlias("CONTENT");
		return panelContent;
	}
	
	@Override
	public KernelJPanelCascadedZZZ createPanelNavigator() throws ExceptionZZZ{
		PanelDialogContentEmptyZZZ panelNavigator = new PanelDialogContentEmptyZZZ(this.getKernelObject(), this);
		panelNavigator.setAlias("NAVIGATOR");
		return panelNavigator;
	}
	
	
}

