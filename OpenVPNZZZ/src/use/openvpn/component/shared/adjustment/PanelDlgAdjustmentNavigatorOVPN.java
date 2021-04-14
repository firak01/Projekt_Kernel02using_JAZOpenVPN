package use.openvpn.component.shared.adjustment;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

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
import basic.zKernelUI.component.IComponentCascadedUserZZZ;
import basic.zKernelUI.component.KernelActionCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelFormLayoutedZZZ;
import basic.zKernelUI.thread.KernelSwingWorkerZZZ;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import custom.zKernel.LogZZZ;

/**Das Panel, was im "BorderLayout.CENTER" des entprechenden Dialogs angezeigt werden soll.
 * Merke: Die Buttons OK / Cancel werden durch die DialogBox-Extended-Klasse in den BorderLayout.SOUTH der Dialogbox gesetzt.
 * 
 * @author 0823
 *
 */
public class PanelDlgAdjustmentNavigatorOVPN  extends KernelJPanelFormLayoutedZZZ implements IKernelProgramZZZ, IKernelModuleZZZ{	
	/**
	 * DEFAULT Konstruktor, notwendig, damit man objClass.newInstance(); einfach machen kann.
	 *                                 
	 * lindhaueradmin, 23.07.2013
	 */
	public PanelDlgAdjustmentNavigatorOVPN(){
		super();
	}
	public PanelDlgAdjustmentNavigatorOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended) throws ExceptionZZZ {
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
			
			stemp = IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name();
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
			
			this.initFormLayoutContent();
			
		} catch (ExceptionZZZ ez) {					
			System.out.println(ez.getDetailAllLast()+"\n");
			ez.printStackTrace();
			ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
		}
	}//END Konstruktor
	
			
	//###################################################	
	//Interface IFormLayoutUserZZZ		
	@Override
	public boolean fillRowContent(CellConstraints cc, int iRow) {
		boolean bReturn = false;
		main:{			
			//TODOGOON; //20210412: Der Wert soll ein Modulname sein, aus einem Array.
			JLabel label = new JLabel("TESTDEFAULTVALUE");
			label.setHorizontalAlignment(JTextField.LEFT);
			
			iRow=iRow*2;//wg. der Gap-Zeile
			int iRowUsed = this.computeContentRowNumberUsed(iRow);			
			this.add(label, cc.xy(2,iRowUsed));
			
		}//end main;
		return bReturn;
	}

	@Override
	public ArrayList<RowSpec> buildRowSpecs() {
		ArrayList<RowSpec>listReturn=new ArrayList<RowSpec>();
		main:{
			//geht nicht RowSpec rs = RowSpec.decode("5dlu, center:10dlu, 5dlu");
			RowSpec rsGap = this.buildRowSpecGap();
			listReturn.add(rsGap);
			
			RowSpec rs1 = new RowSpec(RowSpec.CENTER,Sizes.dluX(5),0.5);
			listReturn.add(rs1);
			listReturn.add(rsGap);				
		}//end main:
		return listReturn;
	}
	
	@Override
	public ArrayList<ColumnSpec> buildColumnSpecs() {
		ArrayList<ColumnSpec>listReturn=new ArrayList<ColumnSpec>();
		main:{
			//ColumnSpec cs = ColumnSpec.decode("5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu");
			ColumnSpec csGap = this.buildColumnSpecGap();
			listReturn.add(csGap);
			
//			ColumnSpec cs1 = new ColumnSpec(ColumnSpec.RIGHT, Sizes.dluX(5), 0.5 );
//			listReturn.add(cs1);
//							
//			ColumnSpec cs2 = new ColumnSpec(ColumnSpec.DEFAULT, Sizes.dluX(5), 0.5);
//			listReturn.add(cs2);
			
			ColumnSpec cs3 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(50), 0.5 );
			listReturn.add(cs3);
			listReturn.add(csGap);
			
			ColumnSpec cs4 = new ColumnSpec(ColumnSpec.CENTER, Sizes.dluX(5), 0.5 );
			listReturn.add(cs3);
			listReturn.add(csGap);
			
		}//end main
		return listReturn;			
	}
	
	public RowSpec buildRowSpecGap() {
		RowSpec rs = new RowSpec(Sizes.dluX(5));
		return rs;
	}
	
	public ColumnSpec buildColumnSpecGap() {
		ColumnSpec cs = new ColumnSpec(Sizes.dluX(5));
		return cs;
	}
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionModuleChangeOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionModuleChangeOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
				super(objKernel, panelParent);			
			}
			
			public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//				try {
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'IP-Refresh'");
													
				String[] saFlag = {"useProxy"};					
				KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																		
				SwingWorker4ProgramModuleChangeOVPN worker = new SwingWorker4ProgramModuleChangeOVPN(objKernel, panelParent, saFlag);
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
			
			class SwingWorker4ProgramModuleChangeOVPN extends KernelSwingWorkerZZZ{				
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;
				
				public SwingWorker4ProgramModuleChangeOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super(objKernel);
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct() {
					try{
						ProgramAdjustementModuleChangeOVPN objProg = new ProgramAdjustementModuleChangeOVPN(objKernel, this.panel, this.saFlag4Program);
						
						//1. Ins Label schreiben, dass dies hier ausgewählt worden ist
						logLineDate("Clicked ..... TODOGOON");
						
						updateLabel(objProg,"*");
						
						
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
				public void updateLabel(final ProgramAdjustementModuleChangeOVPN objProg, final String stext){
					
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld eintragen, das etwas passiert.	
							logLineDate("label updatede ..... TODOGOON '" + stext + "'");
							
							objProg.updateLabel(stext);  
							
							//TODOGOON; //20210319 Nun das Modul in dem Nachbarpanel neu laden.
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
			
	}//End class ...KErnelActionCascaded....		
}

