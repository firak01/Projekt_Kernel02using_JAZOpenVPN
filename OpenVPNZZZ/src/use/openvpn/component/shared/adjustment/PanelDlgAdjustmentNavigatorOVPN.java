package use.openvpn.component.shared.adjustment;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import use.openvpn.clientui.component.IPExternalRead.ProgramIPContentOVPN;
import use.openvpn.serverui.component.IPExternalUpload.IConstantProgramIpWebOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashMapZZZ;
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
import basic.zKernel.flag.IFlagZLocalEnabledZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.IComponentCascadedUserZZZ;
import basic.zKernelUI.component.AbstractKernelActionListenerCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelFormLayoutedZZZ;
import basic.zKernelUI.thread.KernelSwingWorker4UIZZZ;
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
		super(objKernel, dialogExtended, KernelJPanelCascadedZZZ.FLAGZLOCAL.SKIPDEBUGUI.name());
		String stemp; boolean btemp;
		try{
		//Diese Panel ist Grundlage für diverse INI-Werte auf die über Buttons auf "Programname" zugegriffen wird.
			stemp = IKernelProgramZZZ.FLAGZ.ISKERNELPROGRAM.name();
			btemp = this.setFlag(stemp, true);
			if(btemp==false){
				ExceptionZZZ ez = new ExceptionZZZ( "Flag '" + stemp + "' is not available. Maybe an interface is not implemented.", IFlagZEnabledZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
				throw ez;		 
			}else {
				this.resetProgramUsed();
			}
			
//			stemp = IKernelModuleZZZ.FLAGZ.ISKERNELMODULE.name();
//			btemp = this.setFlagZ(stemp, true);
//			if(btemp==false){
//				ExceptionZZZ ez = new ExceptionZZZ( "Flag '" + stemp + "' is not available. Maybe an interface is not implemented.", iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
//				throw ez;		 
//			}else {
//				this.resetModuleUsed();
//			}
			
			//##################################################################
			//### Definition des Masken UIs	
			this.initFormLayoutContent(); 		//Hiermit dann erst die Werte füllen.
						
		} catch (ExceptionZZZ ez) {					
			System.out.println(ez.getDetailAllLast()+"\n");
			ez.printStackTrace();
			ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
		}
	}//END Konstruktor
	
			
	//###################################################	
	//Interface IFormLayoutUserZZZ				
	@Override
	public boolean fillRowContent(CellConstraints cc, int iRow) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			//#############################################################################################
			//### Auslesen des verwendeten ini-Eintrags für die Details des Navigators 
					
			//Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
			String sProgram; String sModule;
			sModule = KernelUIZZZ.getModuleUsedName((IKernelModuleZZZ)this);
			if(StringZZZ.isEmpty(sModule)){
				ExceptionZZZ ez = new ExceptionZZZ("No module configured for this component '" + this.getClass().getName() + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			
			sProgram = this.getProgramName(); //Das sollte der Name des Panels selbst sein!!!
			if(StringZZZ.isEmpty(sProgram)){
				ExceptionZZZ ez = new ExceptionZZZ("No program '" + sProgram + "' configured for the module: '" +  sModule + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}

			//DARIN WIRD NACH DEM ALIASNAMEN '....' GESUCHT, UND DER WERT  FÜR 'NavigatorContentJson' geholt.
			//TODOGOON;//20210630
			String sValue = null;
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "NavigatorContentJson");
			if(!objEntry.hasAnyValue()) break main;
			
			if(objEntry.isJson()) {
				
				//Mehrfachwerte, beginne mit dem Wert an der passenden iRow-Indexposition
				if(objEntry.isJsonArray()) {
					ArrayList<String>als=objEntry.getValueArrayList();
					if(iRow>als.size()) break main;
					
					//Todogoon: Natürlich eigentlich die passende indexposition und nicht das ganze Array....
					sValue = als.toString();
					
				}else if(objEntry.isJsonMap()) {
					HashMap<String,String>hm=objEntry.getValueHashMap();
					if(iRow>hm.size()) break main;
					
					//Das ergibt aber nur einen Eintrag, den Debug-String: sValue = hm.toString();
				
					
					//TODOGOON; //20210727 eine HashMapExtended aus der HashMap bauen.									
					HashMapZZZ<String,String>hmzzz = HashMapZZZ.toHashMapExtended(hm);
					                                // .clone(hm);
					
					sValue = (String) hmzzz.getValueByIndex(iRow-1);
				}else {					
					if(iRow>1) break main;//quasi wird dieser Wert aus der INI-Zeile immer nur als 1 Zeile zurückgegeben.
					
					String sJson = objEntry.getValue();
					System.out.println(sJson);
					
					sValue = sJson;	
				}
			}else {
				if(iRow>1) break main;
				
				sValue = objEntry.getValue();				
			}
						
			bReturn = this.createRowAdjustmentNavigator(this, cc, iRow, sValue);			
			
//Fallso die Reihen unterschiedliche Typen wären....
//			switch(iRow) {
//			case 1:
//				this.createRowAdjustmentNavigator(this, cc, iRow, sValue);			
//				break;
//			case 2:
//				this.createRowGeneratePage(this, cc, 2, "Generate page");			
//				break;
//			case 3:
//				this.createRowIpLocal(this, cc, 3, sIpLocal);			
//				break;
//			case 4:
//				this.createRowUploadPage(this, cc, 4, "Upload generated page");			
//				break;
//			case 5:
//				this.createRowIpWeb(this, cc, 5, sIp);
//				break;	
//			default:
//				//Keinen Fehler werfen, da diese Methode in einer Schleife ausgeführt wird.
//				//Rückgabewert false ist dann der Abbruch der Schleife				
//				bReturn = false;
//				break main;
//			}
			
//			bReturn = true;
		}
		return bReturn;
	}
	
	private boolean createRowAdjustmentNavigator(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
												
			//TODOGOON; //20210412: Der Wert soll ein Modulname sein, aus einem Array.
			JLabel label = new JLabel(sDefaultValue);
			label.setHorizontalAlignment(JTextField.LEFT);
			
			iRow=iRow*2;//wg. der Gap-Zeile
			int iRowUsed = this.computeContentRowNumberUsed(iRow);
			
			ArrayList<ColumnSpec>listCs=this.getColumnSpecs();														
			int iColumns = listCs.size();//die DebugZeile geht über alle Spalten hinweg
			int iStartingColumn = 2; //vorneweg ist noch eine GAP-Spalte
			
			//+++++++++++++++
			int iColumnCurrent = 1;
			
			// *2 wg. der "GAP" Spalte
			int iWidthRemainingCurrent=iColumns-iStartingColumn-((iColumnCurrent-1)*2);
			this.add(label, cc.xyw(iStartingColumn,iRowUsed,iWidthRemainingCurrent));
			
//			JLabel labelRouter = new JLabel(IConstantProgramIpWebOVPN.sLABEL_TEXTFIELD);
//			panel.add(labelRouter, cc.xy(2,iRow*2));
			
//			JTextField textfieldIPExternal = new JTextField(sDefaultValue, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
//			textfieldIPExternal.setHorizontalAlignment(JTextField.LEFT);
//			textfieldIPExternal.setCaretPosition(0);
			
			//Dimension dim = new Dimension(10, 15);
			//textfield.setPreferredSize(dim);
//			panel.add(textfieldIPExternal, cc.xy(4,iRow*2));
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
//			panel.setComponent(IConstantProgramIpWebOVPN.sCOMPONENT_TEXTFIELD, textfieldIPExternal);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			
			
			//GGFs Buttons zur Verfügung stellen.....
//			JButton buttonIpWeb2ini = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpWeb2iniOVPN actionIpWeb2iniOVPN = new ActionIpWeb2iniOVPN(objKernel, this);
//			buttonIpWeb2ini.addActionListener(actionIpWeb2iniOVPN);
//			panel.add(buttonIpWeb2ini, cc.xy(6,iRow*2));
//				
//			//Merke: Der Server baut die Internetseite basierend auf dem Ini Eintrag.
//			//       Der letzte Eintrag kommt dann aus der aktuellen Web-Version.
//			JButton buttonReadIPWeb = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON);
//			ActionIPWebRefreshOVPN actionIPRefreshWeb = new ActionIPWebRefreshOVPN(objKernel, this);
//			buttonReadIPWeb.addActionListener(actionIPRefreshWeb);
//			panel.add(buttonReadIPWeb, cc.xy(8,iRow*2));
//			
			bReturn = true;
		}//end main;
		return bReturn;
	}
	

	@Override
	public ArrayList<RowSpec> buildRowSpecs() {
		ArrayList<RowSpec>listReturn=new ArrayList<RowSpec>();
		main:{
			//geht nicht RowSpec rs = RowSpec.decode("5dlu, center:10dlu, 5dlu");
			RowSpec rsGap1 = this.buildRowSpecGap();
			listReturn.add(rsGap1);
			
			RowSpec rs1 = new RowSpec(RowSpec.CENTER,Sizes.dluX(5),0.5);
			listReturn.add(rs1);
			
			RowSpec rsGap2 = this.buildRowSpecGap(); //Neues Objekt bauen, sonst wird es nur in der Liste ersetzt und nicht erweitert.
			listReturn.add(rsGap2);				
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
			
			ColumnSpec cs3 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(10), 0);//DAS IST DIE SPALTE FÜR DEN BUTTON - KEIN WACHSTUM, ALSO 0
			listReturn.add(cs3);
			listReturn.add(csGap); 
			
			ColumnSpec cs4 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(100), 0.5 );
			listReturn.add(cs4);
			listReturn.add(csGap);
			
			ColumnSpec cs5 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(50), 0.5 );
			listReturn.add(cs5);
			listReturn.add(csGap);
			
			ColumnSpec cs6 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(50), 0.5 );
			listReturn.add(cs6);
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
		class ActionModuleChangeOVPN extends  AbstractKernelActionListenerCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
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
			
			class SwingWorker4ProgramModuleChangeOVPN extends KernelSwingWorker4UIZZZ{				
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
							try {
								logLineDate("label updatede ..... TODOGOON '" + stext + "'");
							} catch (ExceptionZZZ ez) {
								ez.printStackTrace();
							}
							
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

