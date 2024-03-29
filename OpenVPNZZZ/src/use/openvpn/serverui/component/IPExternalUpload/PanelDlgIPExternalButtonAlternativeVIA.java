package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zBasicUI.thread.SwingWorker;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.AbstractKernelActionListenerCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelDialogButtonDefaultZZZ;
import basic.zKernelUI.component.KernelJPanelDialogButtonDefaultZZZ.ActionListenerButtonOkDefaultZZZ;
import basic.zKernelUI.thread.KernelSwingWorkerZZZ;
import custom.zKernel.LogZZZ;
import use.openvpn.serverui.component.IPExternalUpload.PanelDlgIPExternalContentOVPN.ActionIpWeb2iniOVPN.SwingWorker4ProgramIpWeb2iniOVPN;
import basic.zKernel.IKernelUserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelLogZZZ;

/**
 * @author 0823
 *
 */
public class PanelDlgIPExternalButtonAlternativeVIA  extends KernelJPanelDialogButtonDefaultZZZ{
	public PanelDlgIPExternalButtonAlternativeVIA(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable) throws ExceptionZZZ{
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable);
	}
		
	//#### Interfaces ##############################
	//### Zugriff auf den alternativen Button
	public AbstractKernelActionListenerCascadedZZZ getActionListenerButtonOk(KernelJPanelCascadedZZZ panelButton){		
		AbstractKernelActionListenerCascadedZZZ objReturn = null;
		try {
			objReturn = new ActionListenerDlgIPExternalButtonOk(this.getKernelObject(), panelButton);
		}catch(ExceptionZZZ ez) {
			ez.printStackTrace();
		}
		return objReturn;
	}
	
	
	class ActionListenerDlgIPExternalButtonOk extends  ActionListenerButtonOkDefaultZZZ {
		/**  Durch Überschreiben des Standardbuttons für die Dialogbox, können hier noch andere Aktionen durchgef�hrt werden, als nur das Schliessen der Dialogbox. 
		* lindhaueradmin; 17.01.2007 10:10:21
		 * @param objKernel
		 * @param panelParent
		 * @throws ExceptionZZZ 
		 */
		public ActionListenerDlgIPExternalButtonOk(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ {
			super(objKernel, panelParent);
		}
		
//		/**Durch überschreiben dieser Methoden können erbende Klassen noch anderen Code ausf�hren
//		* @param ActionEvent
//		* @return true ==> es wird der weitere Code ausgef�hrt
//		* 
//		* lindhaueradmin; 09.01.2007 09:03:32
//		 */		
//		public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult){
//			boolean bReturn = false;
//			try{
//				main:{
//				//Hier erst einmal den Inhalt einer per Alias zugänglich gemachten Komponente (siehe KernelPanelCascadedZZZ) auslesen
//				KernelJPanelCascadedZZZ panelButton = (KernelJPanelCascadedZZZ) this.getPanelParent();
//				KernelJPanelCascadedZZZ panelCenter = (KernelJPanelCascadedZZZ) panelButton.getPanelNeighbour("CENTER");
//				JTextField texttemp = (JTextField) panelCenter.getComponent("text1");
//				String sIP= texttemp.getText();	
//				
//				ReportLogZZZ.write(ReportLogZZZ.INFO, "IP/URL found for 'Export Data via Http': " + sIP);
//				
////				Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
//				IKernelZZZ objKernel = this.getKernelObject();
//				KernelJDialogExtendedZZZ dialog = panelCenter.getDialogParent();	
//				KernelJFrameCascadedZZZ frameParent = null;
//				String sProgram; String sModule;
//				sModule = this.getModuleUsed();
//				if(StringZZZ.isEmpty(sModule)){
//					ExceptionZZZ ez = new ExceptionZZZ("No module configured.", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//					throw ez;
//				}
//				
//				sProgram = this.getProgramUsed();
//				if(StringZZZ.isEmpty(sProgram)){
//					ExceptionZZZ ez = new ExceptionZZZ("No program configured for the module: '" +  sModule + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//					throw ez;
//				}	
//				
//				objKernel.setParameterByProgramAlias(sModule, sProgram, "IPExternal", sIP);
//				bReturn = true; //erst dann wird das PostCustom-ausgeführt
//				
//				}//END main:
//			}catch(ExceptionZZZ ez){
//				System.out.println(ez.getDetailAllLast());
//				ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());
//			}			
//			return bReturn;
//		}
		
		public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//			try {
			ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'IpWeb2ini'");
												
			String[] saFlag = null; //{"useProxy"};					
			KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																	
			SwingWorker4ProgramIpWeb2iniOVPN worker = new SwingWorker4ProgramIpWeb2iniOVPN(objKernel, panelParent, saFlag);
			worker.start();  
			
		/*} catch (ExceptionZZZ ez) {				
			this.getLogObject().WriteLineDate(ez.getDetailAllLast());
			ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());
		}	*/
			
			return true;
		}

		public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
			return true;
		}

		public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
		}			 							
		
		class SwingWorker4ProgramIpWeb2iniOVPN extends KernelSwingWorkerZZZ{			
			private KernelJPanelCascadedZZZ panel;
			private String[] saFlag4Program;
			
			public SwingWorker4ProgramIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
				super(objKernel);
				this.panel = panel;
				this.saFlag4Program = saFlag4Program;					
			}
			
			//#### abstracte - Method aus SwingWorker
			public Object construct() {
				try{
					//1. IP Auslesen von der Webseite
					ProgramIpWeb2iniOVPN objProg = new ProgramIpWeb2iniOVPN(objKernel, this.panel, this.saFlag4Program);
					objProg.reset();
					String sIp = objProg.getIpFromUi();
					logLineDate("Ip from UI for Web2ini '" + sIp + "'");
					
					
					//2. Schreiben des Werts in die Ini Datei
					updateTextField(objProg, "writing...");
					boolean bErg = objProg.writeIpToIni(sIp);
										
					//3. Diesen Wert wieder ins Label schreiben.
					updateTextField(objProg, sIp);
										
				}catch(ExceptionZZZ ez){
					System.out.println(ez.getDetailAllLast());
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());					
				}
				return "all done";
			}
			
			/**Aus dem Worker-Thread heraus wird ein Thread gestartet (der sich in die EventQueue von Swing einreiht.)
			 *  Entspricht auch ProgramIPContext.updateLabel(..)
			* @param stext
			* 
			* lindhaueradmin; 17.01.2007 12:09:17
			 */
			public void updateTextField(final ProgramIpWeb2iniOVPN objProg, final String stext){
								
//				Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
				Runnable runnerUpdateLabel= new Runnable(){

					public void run(){
//						In das Textfeld eintragen, das etwas passiert.
						logLineDate("TextField updated with '" + stext + "'");						
						try {
							objProg.updateLabel(stext);
						} catch (ExceptionZZZ e) {
							e.printStackTrace();
						}
					}
				};
				
				SwingUtilities.invokeLater(runnerUpdateLabel);					
			}			
			
			/**Overwritten and using an object of jakarta.commons.lang
			 * to create this string using reflection. 
			 * Remark: this is not yet formated. A style class is available in jakarta.commons.lang. 
			 */
			public String toString(){
				String sReturn = "";
				sReturn = ReflectionToStringBuilder.toString(this);
				return sReturn;
			}

		} //End Class MySwingWorker

		public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) {
			// TODO Auto-generated method stub
			
		}
	}//END class actionListenerDlgIPExternalButtonOk
}

