package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.KernelActionCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelDialogButtonDefaultZZZ;
import basic.zKernel.IKernelZZZ;

/**
 * @author 0823
 *
 */
public class PanelDlgIPExternalButtonAlternativeVIA  extends KernelJPanelDialogButtonDefaultZZZ{
	public PanelDlgIPExternalButtonAlternativeVIA(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable){
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable);
	}
	
	//#######################################################
	//### Zugriff auf den alternativen Button
	public KernelActionCascadedZZZ getActionListenerButtonOk(KernelJPanelCascadedZZZ panelButton){
		return new ActionListenerDlgIPExternalButtonOk(this.getKernelObject(), panelButton);
	}	
	
	class ActionListenerDlgIPExternalButtonOk extends  ActionListenerButtonOkDefaultZZZ {
		/**  Durch Überschreiben des Standardbuttons für die Dialogbox, können hier noch andere Aktionen durchgef�hrt werden, als nur das Schliessen der Dialogbox. 
		* lindhaueradmin; 17.01.2007 10:10:21
		 * @param objKernel
		 * @param panelParent
		 */
		public ActionListenerDlgIPExternalButtonOk(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) {
			super(objKernel, panelParent);
		}
		
		/**Durch überschreiben dieser Methoden können erbende Klassen noch anderen Code ausf�hren
		* @param ActionEvent
		* @return true ==> es wird der weitere Code ausgef�hrt
		* 
		* lindhaueradmin; 09.01.2007 09:03:32
		 */		
		public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult){
			boolean bReturn = false;
			try{
				main:{
				//Hier erst einmal den Inhalt einer per Alias zugänglich gemachten Komponente (siehe KernelPanelCascadedZZZ) auslesen
				KernelJPanelCascadedZZZ panelButton = (KernelJPanelCascadedZZZ) this.getPanelParent();
				KernelJPanelCascadedZZZ panelCenter = (KernelJPanelCascadedZZZ) panelButton.getPanelNeighbour("CENTER");
				JTextField texttemp = (JTextField) panelCenter.getComponent("text1");
				String sIP= texttemp.getText();	
				
				ReportLogZZZ.write(ReportLogZZZ.INFO, "IP/URL found for 'Export Data via Http': " + sIP);
				
//				Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
				IKernelZZZ objKernel = this.getKernelObject();
				KernelJDialogExtendedZZZ dialog = panelCenter.getDialogParent();	
				KernelJFrameCascadedZZZ frameParent = null;
				String sProgram; String sModule;
				if(dialog==null){
					frameParent = panelCenter.getFrameParent();	
					//String sProgram = frameParent.getClass().getName(); //der Frame, in den dieses Panel eingebettet ist
					sProgram = this.getProgramUsed(); //der Frame, in den dieses Panel eingebettet ist
					//String sModule = KernelUIZZZ.searchModuleFirstConfiguredClassname(frameParent);
					sModule = this.getModuleUsed(); //KernelUIZZZ.searchModuleFirstConfiguredClassname(frameParent);
					if(StringZZZ.isEmpty(sModule)){
						ExceptionZZZ ez = new ExceptionZZZ("No module configured for the parent frame/program: '" +  sProgram + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					objKernel.setParameterByProgramAlias(sModule, "IP_ServerContext", "IPExternal", sIP);
					bReturn = true; //erst dann wird das PostCustom-ausgeführt				
				}else{		
					System.out.println(ReflectCodeZZZ.getMethodCurrentName() + "# This is a dialog.....");
					
					sModule = dialog.getModuleName();												  								 
					if(StringZZZ.isEmpty(sModule)) {
						sModule = this.getModuleUsed();
					}
					if(StringZZZ.isEmpty(sModule)){
						ExceptionZZZ ez = new ExceptionZZZ("No module configured for this component '" + this.getClass().getName() + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					sProgram = dialog.getProgramName();
					if(StringZZZ.isEmpty(sProgram)){
						sProgram = this.getProgramUsed();
					}
					if(StringZZZ.isEmpty(sProgram)){
						ExceptionZZZ ez = new ExceptionZZZ("No program configured for the module: '" +  sModule + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}																					
				}		
				objKernel.setParameterByProgramAlias(sModule, sProgram, "IPExternal", sIP);
				bReturn = true; //erst dann wird das PostCustom-ausgeführt
				
				}//END main:
			}catch(ExceptionZZZ ez){
				System.out.println(ez.getDetailAllLast());
				ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());
			}			
			return bReturn;
		}

	}//END class actionListenerButtonCancelDefault
}

