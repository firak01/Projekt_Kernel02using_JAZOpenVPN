package use.openvpn.serverui.component.FTPCredentials;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

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
import basic.zKernelUI.thread.KernelSwingWorker4UIZZZ;
import basic.zKernelUI.thread.KernelSwingWorkerZZZ;
import custom.zKernel.LogZZZ;
import use.openvpn.clientui.component.IPExternalRead.ProgramIpWeb2iniOVPN;
import basic.zKernel.IKernelUserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelLogZZZ;

/**
 * @author 0823
 *
 */
public class PanelDlgFTPCredentialsButtonAlternativeOVPN  extends KernelJPanelDialogButtonDefaultZZZ{
	public PanelDlgFTPCredentialsButtonAlternativeOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable) throws ExceptionZZZ{
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable);
	}
	public PanelDlgFTPCredentialsButtonAlternativeOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended, boolean bIsButtonOkAvailable, boolean bIsButtonCancelAvailable, boolean bIsButtonCloseAvailable) throws ExceptionZZZ{
		super(objKernel, dialogExtended, bIsButtonOkAvailable, bIsButtonCancelAvailable, bIsButtonCloseAvailable);
	}
	
	//#######################################################
	//### Zugriff auf den alternativen Button
	public KernelActionCascadedZZZ getActionListenerButtonOk(KernelJPanelCascadedZZZ panelButton) {
		KernelActionCascadedZZZ objReturn = null;
		try {
			objReturn = new ActionListenerDlgFTPCredentialsButtonOk(this.getKernelObject(), panelButton);
		}catch(ExceptionZZZ ez) {
			ez.printStackTrace();
		}
		return objReturn;
	}	
	
	class ActionListenerDlgFTPCredentialsButtonOk extends  ActionListenerButtonOkDefaultZZZ {
		/**  Durch Überschreiben des Standardbuttons für die Dialogbox, können hier noch andere Aktionen durchgefuehrt werden, als nur das Schliessen der Dialogbox. 
		* lindhaueradmin; 17.01.2007 10:10:21
		 * @param objKernel
		 * @param panelParent
		 * @throws ExceptionZZZ 
		 */
		public ActionListenerDlgFTPCredentialsButtonOk(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ {
			super(objKernel, panelParent);
		}
		
		
		/* Hierdurch findet beim Cliecken auf den "APPLY" Button das Setzen der Werte in die INI-Datei statt
		 * 
		 * (non-Javadoc)
		 * @see basic.zKernelUI.component.KernelJPanelDialogButtonDefaultZZZ.ActionListenerButtonOkDefaultZZZ#actionPerformCustom(java.awt.event.ActionEvent, boolean)
		 */
		public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//			try {
			ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'FTPCredentials2ini'");
												
			String[] saFlag = null; //{"useProxy"};					
			KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																	
			SwingWorker4ProgramFTPCredentials2iniOVPN worker = new SwingWorker4ProgramFTPCredentials2iniOVPN(objKernel, panelParent, saFlag);
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
			super.actionPerformPostCustom(ae,bQueryResult);
		}			 							
		
		class SwingWorker4ProgramFTPCredentials2iniOVPN extends KernelSwingWorker4UIZZZ{
			private KernelJPanelCascadedZZZ panel;
			private String[] saFlag4Program;
			
			public SwingWorker4ProgramFTPCredentials2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
				super(objKernel);
				this.panel = panel;
				this.saFlag4Program = saFlag4Program;					
			}
						
			//#### abstracte - Method aus SwingWorker
			public Object construct() {
				try{
					//1. Auslesen
					ProgramFTPCredentials2iniOVPN objProg = new ProgramFTPCredentials2iniOVPN(objKernel, this.panel, this.saFlag4Program);
					//objProg.reset();//Das setzt die Felder leer
					String sUsername = objProg.getUsernameFromUi();
					logLineDate("Username from Local2ini'" + sUsername + "'");
					
					String sPasswordDecrypted = objProg.getPasswordFromUi();
					//Unverschluesseltes Kennwort nicht loggen!!! logLineDate("Password from Local2ini'" + sPassword + "'");
					
					updateMessage(objProg, "writing..."); //Schreibe einen anderen Text in das Feld...
					
					//2. Schreibe in die ini-Datei
					boolean bErg = objProg.writeCredentialsToIni(sUsername, sPasswordDecrypted);
									
					//3. Diesen Wert wieder ins Label zurückschreiben.
					updateMessage(objProg, "closing dialog...");
					String sPasswordEncodedWritten = objProg.getPasswordEncodedWritten();
					updateValue(objProg, sPasswordEncodedWritten);

					//20230416: Solange wie es noch keine Loesung gibt, das gesetzte INI Werte in den Cache kommen... cache loeschen.
					//Sonst bekommt man immer wieder den alten Wert aus dem Cache beim Neuoeffnen des Dialogs.
					this.getKernelObject().getCacheObject().clear();
					
					//Den Dialog schliessen					
					this.panel.getDialogParent().onOk();
					
				}catch(ExceptionZZZ ez){
					System.out.println(ez.getDetailAllLast());
					ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());					
				}
				return "all done";
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
			
		}				
	}//END class actionListenerButtonCancelDefault
}

