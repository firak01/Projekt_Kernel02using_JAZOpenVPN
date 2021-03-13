package use.openvpn.clientui.component.IPExternalRead;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import use.openvpn.clientui.component.IPExternalRead.ProgramIPContentOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zBasicUI.thread.SwingWorker;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelUserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelLogZZZ;
import basic.zKernel.component.IKernelModuleUserZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.component.IKernelProgramZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.KernelActionCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import custom.zKernel.LogZZZ;

/**Das Panel, was im "BorderLayout.CENTER" des entprechenden Dialogs angezeigt werden soll.
 * Merke: Die Buttons OK / Cancel werden durch die DialogBox-Extended-Klasse in den BorderLayout.SOUTH der Dialogbox gesetzt.
 * 
 * @author 0823
 *
 */
public class PanelDlgIPExternalContentOVPN  extends KernelJPanelCascadedZZZ implements IKernelProgramZZZ{	
	/**
	 * DEFAULT Konstruktor, notwendig, damit man objClass.newInstance(); einfach machen kann.
	 *                                 
	 * lindhaueradmin, 23.07.2013
	 */
	public PanelDlgIPExternalContentOVPN(){
		super();
	}
	public PanelDlgIPExternalContentOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended) {
		super(objKernel, dialogExtended);
		String stemp; boolean btemp;
		try{
		//Diese Panel ist Grundlage für diverse INI-Werte auf die über Buttons auf "Programname" zugegriffen wird.
			stemp = IKernelProgramZZZ.FLAGZ.ISKERNELPROGRAM.name();
			btemp = this.setFlagZ(stemp, true);
			if(btemp==false){
				ExceptionZZZ ez = new ExceptionZZZ( "the flag '" + stemp + "' is not available. Maybe an interface is not implemented.", iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
				throw ez;		 
			}
			
		//#############################################################################################
		//### Auslesen des bisher verwendeten ini-Eintrags. 
		//### Merke: Das wäre ggfs. der zuletzt ins Web gebrachte Wert.
		//20190123: Lies die zuvor eingegebene / ausgelesene IPAdresse aus der ini-Datei aus.
		String sIp = "";
				
		//Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
		String sProgram; String sModule;
		sModule = KernelUIZZZ.getModuleUsedName((IKernelModuleUserZZZ) this);
		if(StringZZZ.isEmpty(sModule)){
			ExceptionZZZ ez = new ExceptionZZZ("No module configured for this component '" + this.getClass().getName() + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}
		
		sProgram = this.getProgramName();
		if(StringZZZ.isEmpty(sProgram)){
			ExceptionZZZ ez = new ExceptionZZZ("No program '" + sProgram + "' configured for the module: '" +  sModule + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}

		//DARIN WIRD NACH DEM ALIASNAMEN 'IP_CONTEXT' GESUCHT, UND DER WERT  FÜR 'IPExternal' geholt.
		IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "IPExternal");
		sIp = objEntry.getValue();
				
		//TODO GOON 20190124: Hier soll unterschieden werden zwischen einem absichtlich eingetragenenem Leersstring und nix.
		if(StringZZZ.isEmpty(sIp)){
			sIp = "Enter or refresh";
		}
				
		//##################################################################
		//### Definition des Masken UIs
		//###
		//Diese einfache Maske besteht nur aus 1 Zeile und 4 Spalten. 
		//Es gibt außen einen Rand von jeweils einer Spalte/Zeile
		//Merke: gibt man pref an, so bewirkt dies, das die Spalte beim ver�ndern der Fenstergröße nicht angepasst wird, auch wenn grow dahinter steht.
		
		//erster Parameter sind die Spalten/Columns (hier: vier 5dlu), als Komma getrennte Eintraege. .
		//zweiter Parameter sind die Zeilen/Rows (hier:  drei), Merke: Wenn eine feste L�nge k�rzer ist als der Inhalt, dann wird der Inhalt als "..." dargestellt
		FormLayout layout = new FormLayout(
				"5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu",  
				"5dlu, center:10dlu, 5dlu"); 				 
		this.setLayout(layout);              //!!! wichtig: Das layout muss dem Panel zugwiesen werden BEVOR mit constraints die Componenten positioniert werden.
		CellConstraints cc = new CellConstraints();
		
		this.createRowIpWeb(this, cc, 1, sIp);
		
		} catch (ExceptionZZZ ez) {					
			System.out.println(ez.getDetailAllLast()+"\n");
			ez.printStackTrace();
			ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
		}
	}//END Konstruktor
	
	private boolean createRowIpWeb(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) {
		boolean bReturn = false;
		main:{
				
			//Beim Schliessen der Dialogbox soll ggfs. der Wert übernommen werden, also kein extra Button.
//			JButton buttonIpWeb2ini = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpWeb2iniOVPN actionIpWeb2iniOVPN = new ActionIpWeb2iniOVPN(objKernel, this);
//			buttonIpWeb2ini.addActionListener(actionIpWeb2iniOVPN);
//			panel.add(buttonIpWeb2ini, cc.xy(6,iRow*2));
			
			//#########	
			JLabel label = new JLabel(IConstantProgramIpWebOVPN.sLABEL_TEXTFIELD);
			panel.add(label, cc.xy(2,iRow*2));
														
			JTextField textfieldIPExternal = new JTextField(sDefaultValue, 20);
			textfieldIPExternal.setHorizontalAlignment(JTextField.LEFT);
			textfieldIPExternal.setCaretPosition(0);
			//Dimension dim = new Dimension(10, 15);
			//textfield.setPreferredSize(dim);
			panel.add(textfieldIPExternal, cc.xy(4,iRow*2));
			
			// Dieses Feld soll einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds soll dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramIpWebOVPN.sCOMPONENT_TEXTFIELD, textfieldIPExternal);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			
			
			JButton buttonReadIPExternal = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON);
			ActionIPWebRefreshOVPN actionIPRefresh = new ActionIPWebRefreshOVPN(objKernel, this);
			buttonReadIPExternal.addActionListener(actionIPRefresh);			
			panel.add(buttonReadIPExternal, cc.xy(6,iRow*2));
			
			
			/* Das funktioniert nicht. Funktionalit�t des JGoodies-Framework. Warum ???
			PanelBuilder builder = new PanelBuilder(layout);
			builder.setDefaultDialogBorder();
			builder.addLabel("Externe IP Adresse des Servers");
			JTextField textfield = new JTextField("noch automatisch zu f�llen");
			builder.add(textfield, cc.xy(3,2));
			*/	
			
		}//end main;
		return bReturn;
	}
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionIPWebRefreshOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionIPWebRefreshOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
				super(objKernel, panelParent);			
			}
			
			public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//				try {
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'IP-Refresh'");
													
				String[] saFlag = {"useProxy"};					
				KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																		
				SwingWorker4ProgramIPContentOVPN worker = new SwingWorker4ProgramIPContentOVPN(objKernel, panelParent, saFlag);
				worker.start();  //Merke: Das Setzen des Label Felds geschieht durch einen extra Thread, der mit SwingUtitlities.invokeLater(runnable) gestartet wird.
				

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
			
			class SwingWorker4ProgramIPContentOVPN extends SwingWorker implements IObjectZZZ, IKernelUserZZZ{
				private IKernelZZZ objKernel;
				private LogZZZ objLog;
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;
											
				protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
				
				public SwingWorker4ProgramIPContentOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super();
					this.objKernel = objKernel;
					this.objLog = objKernel.getLogObject();
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct() {
					try{
						ProgramIPContentOVPN objProg = new ProgramIPContentOVPN(objKernel, this.panel, this.saFlag4Program);
						
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						updateTextField(objProg,"Reading ...");
						
						//2. IP Auslesen von der Webseite										
						String sIp = objProg.getIpExternal();
						
						//3. Diesen Wert wieder ins Label schreiben.
						updateTextField(objProg,sIp);
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
				public void updateTextField(final ProgramIPContentOVPN objProg, final String stext){
					
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld eintragen, das etwas passiert.	
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
			
	}//End class ...KErnelActionCascaded....
}

