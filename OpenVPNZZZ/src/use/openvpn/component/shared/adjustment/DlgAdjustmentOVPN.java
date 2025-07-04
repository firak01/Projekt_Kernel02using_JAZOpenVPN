package use.openvpn.component.shared.adjustment;

import java.awt.Frame;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ.FLAGZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import custom.zKernelUI.component.PanelDialogContentEmptyZZZ;
import custom.zKernelUI.module.config.DLG.DLGBOX4INIZZZ;

/**Dialogbox zum Verändern der Einstellungswerte eines Moduls (also z.B. einer anderen Dialogbox)
 *  Merke: Das ist angelehnt an der Dialogbox aus dem Debug - Package des KernelUI - Projekts 
 *  DLGBOX4INIZZZ frameDLG = new DLGBOX4INIZZZ(objKernel, null);
 * 	
 * 
 * @author Fritz Lindhauer, 16.03.2021, 07:06:55
 * 
 */
public class DlgAdjustmentOVPN extends KernelJDialogExtendedZZZ {

	/**
	 * @param owner
	 * @param bModal
	 * @param bSnappedToScreen
	 * @param panelCenter
	 */
	public DlgAdjustmentOVPN(IKernelZZZ objKernel, KernelJFrameCascadedZZZ frameOwner, HashMap<String, Boolean> hmFlag) throws ExceptionZZZ {		
		super(objKernel, frameOwner, false, hmFlag);  //true, d.h. modal, geht leider nur im Konstruktor zu uebergeben, weil JDialog diesen Parameter im Konstruktor braucht und Super(...) kann keinen Code beinhalten, der auf eigene Properties etc. zugreift.
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
	public boolean isButtonCloseAvailable() {
		return true;
	}	
	public boolean isButtonOKAvailable(){
		return true;
	}
	public String getText4ButtonOk(){
		return  "USE VALUE";
	}
	
	@Override
	public KernelJPanelCascadedZZZ createPanelButton() throws ExceptionZZZ{
		PanelDialogContentEmptyZZZ panelButton = new PanelDialogContentEmptyZZZ(this.getKernelObject(), this);
		return panelButton;
	}
	
	@Override
	public KernelJPanelCascadedZZZ createPanelContent() throws ExceptionZZZ{
		PanelDlgAdjustmentContentOVPN panelContent = new PanelDlgAdjustmentContentOVPN(this.getKernelObject(), this);
		return panelContent;
	}
	
	@Override
	public KernelJPanelCascadedZZZ createPanelNavigator() throws ExceptionZZZ {
		//TODOGOON; //20210731 hier muss das Modell für das Panel erstellt werden und dann im Konstruktor übergeben werden.
		
		
		PanelDlgAdjustmentNavigatorOVPN panelNavigator = new PanelDlgAdjustmentNavigatorOVPN(this.getKernelObject(), this);
		return panelNavigator;
	}
}

