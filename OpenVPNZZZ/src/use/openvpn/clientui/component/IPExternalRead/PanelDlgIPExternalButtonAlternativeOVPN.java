package use.openvpn.clientui.component.IPExternalRead;

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
import basic.zKernelUI.component.KernelActionCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelDialogButtonDefaultZZZ;
import custom.zKernel.LogZZZ;
import use.openvpn.clientui.component.IPExternalRead.ProgramIpWeb2iniOVPN;
import basic.zKernel.IKernelUserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelLogZZZ;

/**
 * @author 0823
 *
 */
public class PanelDlgIPExternalButtonAlternativeOVPN  extends KernelJPanelDialogButtonDefaultZZZ{
	public PanelDlgIPExternalButtonAlternativeOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable){
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable);
	}
	public PanelDlgIPExternalButtonAlternativeOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable, boolean bIsButtonCloseAvailable){
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable, bIsButtonCloseAvailable);
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
		
//		/**Durch überschreiben dieser Methoden können erbende Klassen noch anderen Code ausfuehren
//		* @param ActionEvent
//		* @return true ==> es wird der weitere Code ausgefuehrt
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
//				JTextField texttemp = (JTextField) panelCenter.getComponent("textIpContent");
//				String sIP= texttemp.getText();	
//				
//				ReportLogZZZ.write(ReportLogZZZ.INFO, "IP/URL found for use closing dialog 'TODOGOON hier den Namen des Buttons... Export Data via Http': " + sIP);
//				
////				Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
//				IKernelZZZ objKernel = this.getKernelObject();
//				KernelJDialogExtendedZZZ dialog = panelCenter.getDialogParent();	
//				KernelJFrameCascadedZZZ frameParent = null;
//				
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
//				//Merke: Beim Setzen wird auch der KernelCache sofort wieder neu gesetzt.
//				objKernel.setParameterByProgramAlias(sModule, sProgram, "IPExternal", sIP);
//				
//				panelButton.getDialogParent().setDisposed();			
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
		
		class SwingWorker4ProgramIpWeb2iniOVPN extends SwingWorker implements IObjectZZZ, IKernelUserZZZ{
			private IKernelZZZ objKernel;
			private LogZZZ objLog;
			private KernelJPanelCascadedZZZ panel;
			private String[] saFlag4Program;
									
			protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
			
			public SwingWorker4ProgramIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
				super();
				this.objKernel = objKernel;
				this.objLog = objKernel.getLogObject();
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
					
					updateTextField(objProg, "writing...");
					boolean bErg = objProg.writeIpToIni(sIp);
										
					//3. Diesen Wert wieder ins Label schreiben.
					updateTextField(objProg, sIp);
					
					//Den Dialog schliessen
					//this.getPanelParent().getDialogParent().setDisposed();
					updateTextField(objProg, "closing dialog...");
					this.panel.getDialogParent().setDisposed();
					
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
						objProg.updateLabel(stext);
					}
				};
				
				SwingUtilities.invokeLater(runnerUpdateLabel);					
			}

			public IKernelZZZ getKernelObject() {
				return this.objKernel;
			}

			public void setKernelObject(IKernelZZZ objKernel) {
				this.objKernel = objKernel;
			}

			public LogZZZ getLogObject() {
				return this.objLog;
			}

			public void setLogObject(LogZZZ objLog) {
				this.objLog = objLog;
			}
			
			
			
			
			/* (non-Javadoc)
			 * @see zzzKernel.basic.KernelAssetObjectZZZ#getExceptionObject()
			 */
			public ExceptionZZZ getExceptionObject() {
				return this.objException;
			}
			/* (non-Javadoc)
			 * @see zzzKernel.basic.KernelAssetObjectZZZ#setExceptionObject(zzzKernel.custom.ExceptionZZZ)
			 */
			public void setExceptionObject(ExceptionZZZ objException) {
				this.objException = objException;
			}
			
			//aus IKernelLogObjectUserZZZ, analog zu KernelKernelZZZ
			@Override
			public void logLineDate(String sLog) {
				LogZZZ objLog = this.getLogObject();
				if(objLog==null) {
					String sTemp = KernelLogZZZ.computeLineDate(sLog);
					System.out.println(sTemp);
				}else {
					objLog.WriteLineDate(sLog);
				}		
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

	}//END class actionListenerButtonCancelDefault
}

