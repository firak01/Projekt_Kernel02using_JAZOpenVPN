package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import use.openvpn.serverui.component.IPExternalUpload.ProgramIPContentWebOVPN;
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
import basic.zKernel.component.IKernelModuleZZZ;
import basic.zKernel.component.IKernelProgramZZZ;
import basic.zKernel.flag.IFlagUserZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.IPanelCascadedZZZ;
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

//Das hat hier eigentlich nichts zu suchen. TODOGOON: Auch wenn das klappt, eine andere Projektstruktur anbieten.
//wg Fehler: import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchException;

/**Das Panel, was im "BorderLayout.CENTER" des entprechenden Dialogs angezeigt werden soll.
 * Merke: Die Buttons OK / Cancel werden durch die DialogBox-Extended-Klasse in den BorderLayout.SOUTH der Dialogbox gesetzt.
 * 
 * @author 0823
 *
 */
public class PanelDlgIPExternalContentOVPN  extends KernelJPanelFormLayoutedZZZ implements IKernelProgramZZZ{	
	/**
	 * DEFAULT Konstruktor, notwendig, damit man objClass.newInstance(); einfach machen kann.
	 *                                 
	 * lindhaueradmin, 23.07.2013
	 */
	public PanelDlgIPExternalContentOVPN(){
		super();
	}
	public PanelDlgIPExternalContentOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended) throws ExceptionZZZ {
		super(objKernel, dialogExtended);
		String stemp; boolean btemp;
		try{
		//Diese Panel ist Grundlage für diverse INI-Werte auf die über Buttons auf "Programname" zugegriffen wird.
		stemp = IKernelProgramZZZ.FLAGZ.ISKERNELPROGRAM.name();
		btemp = this.setFlagZ(stemp, true);
		if(!btemp) {
			ExceptionZZZ ez = new ExceptionZZZ("Flag is not available '" + stemp + "'. Maybe an interface for this flag is not implemented", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}
						
		//##################################################################
		//### Definition des Masken UIs	
		this.initFormLayoutContent(); 		//Hiermit dann erst die Werte füllen.
		
		/* Das funktioniert nicht. Funktionalit�t des JGoodies-Framework. Warum ???
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.addLabel("Externe IP Adresse des Servers");
		JTextField textfield = new JTextField("noch automatisch zu f�llen");
		builder.add(textfield, cc.xy(3,2));
		*/	
		} catch (ExceptionZZZ ez) {					
			System.out.println(ez.getDetailAllLast()+"\n");
			ez.printStackTrace();
			ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());			
		}
	}//END Konstruktor
	
	private boolean createRowIpWeb(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			JLabel labelRouter = new JLabel(IConstantProgramIpWebOVPN.sLABEL_TEXTFIELD);
			panel.add(labelRouter, cc.xy(2,iRow*2));
			
			JTextField textfieldIPExternal = new JTextField(sDefaultValue, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			textfieldIPExternal.setHorizontalAlignment(JTextField.LEFT);
			textfieldIPExternal.setCaretPosition(0);
			//Dimension dim = new Dimension(10, 15);
			//textfield.setPreferredSize(dim);
			panel.add(textfieldIPExternal, cc.xy(4,iRow*2));
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramIpWebOVPN.sCOMPONENT_TEXTFIELD, textfieldIPExternal);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			
			
			JButton buttonIpWeb2ini = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON_TO_INI);
			ActionIpWeb2iniOVPN actionIpWeb2iniOVPN = new ActionIpWeb2iniOVPN(objKernel, this);
			buttonIpWeb2ini.addActionListener(actionIpWeb2iniOVPN);
			panel.add(buttonIpWeb2ini, cc.xy(6,iRow*2));
				
			//Merke: Der Server baut die Internetseite basierend auf dem Ini Eintrag.
			//       Der letzte Eintrag kommt dann aus der aktuellen Web-Version.
			JButton buttonReadIPWeb = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON);
			ActionIPWebRefreshOVPN actionIPRefreshWeb = new ActionIPWebRefreshOVPN(objKernel, this);
			buttonReadIPWeb.addActionListener(actionIPRefreshWeb);
			panel.add(buttonReadIPWeb, cc.xy(8,iRow*2));
			
		}//end main;
		return bReturn;
	}
	
	
	private boolean createRowUploadPage(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			
			//- - - - - - - -
			JTextField textfieldWebUpload = new JTextField(sDefaultValue, 20);
			textfieldWebUpload.setHorizontalAlignment(JTextField.LEFT);
			panel.add(textfieldWebUpload, cc.xyw(4,iRow*2,3)); //Mehrere Spalten umfassend
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramPageWebUploadOVPN.sCOMPONENT_TEXTFIELD, textfieldWebUpload);  
													
			
			
			JButton buttonUploadIPPage = new JButton(IConstantProgramPageWebUploadOVPN.sLABEL_BUTTON);
			ActionPageWebUploadOVPN actionUploadIPPage = new ActionPageWebUploadOVPN(objKernel, this);
			buttonUploadIPPage.addActionListener(actionUploadIPPage);
			panel.add(buttonUploadIPPage, cc.xy(8,iRow*2));						
			
		}//end main;
		return bReturn;
	}
	
	private boolean createRowIpLocal(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			JLabel labelLocal = new JLabel(IConstantProgramIpLocalOVPN.sLABEL_TEXTFIELD);
			this.add(labelLocal, cc.xy(2,iRow*2));			
			
			JTextField textfieldIPLocal = new JTextField(sDefaultValue, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			textfieldIPLocal.setHorizontalAlignment(JTextField.LEFT);
			//textfieldIPRouter.setCaretPosition(0); //Cursorposition		
			//Dimension dim = new Dimension(10, 15);
			//textfield.setPreferredSize(dim);
			panel.add(textfieldIPLocal, cc.xy(4,iRow*2));
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramIpLocalOVPN.sCOMPONENT_TEXTFIELD, textfieldIPLocal);  
				
			
			JButton buttonIpLocal2ini = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON_TO_INI);
			ActionIpLocal2iniOVPN actionIpLocal2iniOVPN = new ActionIpLocal2iniOVPN(objKernel, this);
			buttonIpLocal2ini.addActionListener(actionIpLocal2iniOVPN);
			this.add(buttonIpLocal2ini, cc.xy(6,iRow*2));
			
			JButton buttonReadIPLocal = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON);
			ActionIPLocalRefreshOVPN actionIpRefreshLocal = new ActionIPLocalRefreshOVPN(objKernel, this);
			buttonReadIPLocal.addActionListener(actionIpRefreshLocal);
			panel.add(buttonReadIPLocal, cc.xy(8,iRow*2));
			
		}//end main;
		return bReturn;
	}
	
	
	private boolean createRowGeneratePage(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			
			JTextField textfieldWebCreate = new JTextField("", 20);
			textfieldWebCreate.setHorizontalAlignment(JTextField.LEFT);
			panel.add(textfieldWebCreate, cc.xyw(4,iRow*2,3)); //Mehrere Spalten umfassend
			
			// Dieses Feld soll einer ggfs. Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramPageWebCreateOVPN.sCOMPONENT_TEXTFIELD, textfieldWebCreate);      
			
			
			
			JButton buttonGenerateIPPage = new JButton(IConstantProgramPageWebCreateOVPN.sLABEL_BUTTON);
			ActionPageWebCreateOVPN actionGenerateIPPage = new ActionPageWebCreateOVPN(objKernel, this);
			buttonGenerateIPPage.addActionListener(actionGenerateIPPage);
			panel.add(buttonGenerateIPPage, cc.xy(8,iRow*2));
			
		}//end main;
		return bReturn;
	}
	
	private boolean createRowIpRouter(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			JLabel labelRouter = new JLabel(IConstantProgramIpRouterOVPN.sLABEL_TEXTFIELD);
			panel.add(labelRouter, cc.xy(2,iRow*2));
			
			JTextField textfieldIPRouter = new JTextField(sDefaultValue, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			textfieldIPRouter.setHorizontalAlignment(JTextField.LEFT);
			//textfieldIPRouter.setCaretPosition(0); //Cursorposition		
			//Dimension dim = new Dimension(10, 15);
			//textfield.setPreferredSize(dim);
			panel.add(textfieldIPRouter, cc.xy(4,iRow*2));
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramIpRouterOVPN.sCOMPONENT_TEXTFIELD, textfieldIPRouter);  
			
			
			JButton buttonIpRouter2ini = new JButton(IConstantProgramIpRouterOVPN.sLABEL_BUTTON_TO_INI);
			ActionIpRouter2iniOVPN actionIpRouter2iniOVPN = new ActionIpRouter2iniOVPN(objKernel, this);
			buttonIpRouter2ini.addActionListener(actionIpRouter2iniOVPN);
			panel.add(buttonIpRouter2ini, cc.xy(6,iRow*2));
			
			JButton buttonReadIPRouter = new JButton(IConstantProgramIpRouterOVPN.sLABEL_BUTTON);
			ActionIPWebRefreshOVPN actionIPRefreshRouter = new ActionIPWebRefreshOVPN(objKernel, this);
			buttonReadIPRouter.addActionListener(actionIPRefreshRouter);
			panel.add(buttonReadIPRouter, cc.xy(8,iRow*2));
			
			
			
		}//end main;
		return bReturn;
	}
	
	
	//#### Interface IFormLayoutUserZZZ
	public ColumnSpec buildColumnSpecGap() {
		ColumnSpec cs = new ColumnSpec(Sizes.dluX(5));
		return cs;
	}

	public RowSpec buildRowSpecGap() {
		RowSpec rs = new RowSpec(Sizes.dluX(5));
		return rs;
	}

	@Override
	public ArrayList<RowSpec> buildRowSpecs() {		
		//"5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu"
		ArrayList<RowSpec>listReturn=new ArrayList<RowSpec>();
		main:{
			//geht nicht RowSpec rs = RowSpec.decode("5dlu, center:10dlu, 5dlu");
			
			
			//##################################################################
			//### Definition des Masken UIs			
			//erster Parameter sind die Spalten/Columns (hier: vier 5dlu), als Komma getrennte Eintraege. .
			//zweiter Parameter sind die Zeilen/Rows (hier:  7), Merke: Wenn eine feste L�nge k�rzer ist als der Inhalt, dann wird der Inhalt als "..." dargestellt
			//FormLayout layout = new FormLayout(
			//		"5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu, center:pref:grow(0.5),5dlu",         
			//		"5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu"); 				 			
			//this.setLayout(layout);              //!!! wichtig: Das layout muss dem Panel zugewiesen werden BEVOR mit constraints die Componenten positioniert werden.
			//CellConstraints cc = new CellConstraints();
			//...		
			//this.createRowIpRouter(this, cc, 1, sIpRouter);							
			//...
			
			RowSpec rs1 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
			listReturn.add(rs1);
			
			RowSpec rsGap1 = this.buildRowSpecGap();
			listReturn.add(rsGap1);	
			
			//++++++++++
			RowSpec rs2 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
			listReturn.add(rs2);
			
			RowSpec rsGap2 = this.buildRowSpecGap();
			listReturn.add(rsGap2);
			
			
			//++++++++++
			RowSpec rs3 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
			listReturn.add(rs3);
			
			RowSpec rsGap3 = this.buildRowSpecGap();
			listReturn.add(rsGap3);
			
			
			//++++++++++
			RowSpec rs4 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
			listReturn.add(rs4);
			
			RowSpec rsGap4 = this.buildRowSpecGap();
			listReturn.add(rsGap4);
			
			//++++++++++
			RowSpec rs5 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
			listReturn.add(rs5);
			
			RowSpec rsGap5 = this.buildRowSpecGap();
			listReturn.add(rsGap5);
			
			//++++++++++
//			RowSpec rs6 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
//			listReturn.add(rs6);
//			
//			RowSpec rsGap6 = this.buildRowSpecGap();
//			listReturn.add(rsGap6);
//			
//			
//			//++++++++++
//			RowSpec rs7 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
//			listReturn.add(rs7);
//			
//			RowSpec rsGap7 = this.buildRowSpecGap();
//			listReturn.add(rsGap7);
//			
//			//++++++++++
//			RowSpec rs8 = new RowSpec(RowSpec.CENTER,Sizes.dluX(20),0.5);				
//			listReturn.add(rs8);
//			
//			RowSpec rsGap8 = this.buildRowSpecGap();
//			listReturn.add(rsGap8);
			
			
		}//end main:
		return listReturn;
		
	}

	@Override
	public ArrayList<ColumnSpec> buildColumnSpecs() {
		ArrayList<ColumnSpec>listReturn=new ArrayList<ColumnSpec>();
		main:{
			//ColumnSpec cs = ColumnSpec.decode("5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu");
			
			//##################################################################
			//### Definition des Masken UIs
			//###
			//Diese einfache Maske besteht nur aus 1 Zeile und 4 Spalten. 
			//Es gibt außen einen Rand von jeweils einer Spalte/Zeile
			//Merke: gibt man pref an, so bewirkt dies, das die Spalte beim ver�ndern der Fenstergröße nicht angepasst wird, auch wenn grow dahinter steht.
			
			//erster Parameter sind die Spalten/Columns (hier: vier 5dlu), als Komma getrennte Eintraege. .
			//zweiter Parameter sind die Zeilen/Rows (hier:  drei), Merke: Wenn eine feste L�nge k�rzer ist als der Inhalt, dann wird der Inhalt als "..." dargestellt
			//FormLayout layout = new FormLayout(
			//		"5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu, center:pref:grow(0.5),5dlu",         
			//		"5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu"); 				 			
			
			ColumnSpec csGap = this.buildColumnSpecGap();
			listReturn.add(csGap);
			
			ColumnSpec cs1 = new ColumnSpec(ColumnSpec.RIGHT, Sizes.dluX(100), 0.5 );
			listReturn.add(cs1);
			listReturn.add(csGap);
							
			ColumnSpec cs2 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(50), 0.5);
			listReturn.add(cs2);
			
			listReturn.add(csGap);
			
			ColumnSpec cs3 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(100), 0.5 );
			listReturn.add(cs3);
			listReturn.add(csGap);
			
			ColumnSpec cs4 = new ColumnSpec(ColumnSpec.CENTER, Sizes.dluX(30), 0.5 );
			listReturn.add(cs4);				
			listReturn.add(csGap);
			
		}//end main
		return listReturn;			
	}
	
	@Override
	public boolean fillRowContent(CellConstraints cc, int iRow) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			//#############################################################################################
			//### Auslesen des bisher verwendeten ini-Eintrags. 
			//### Merke: Das wäre ggfs. der zuletzt ins Web gebrachte Wert.
			//20190123: Lies die zuvor eingegebene / ausgelesene IPAdresse aus der ini-Datei aus.
			String sIp = "";
					
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

			//DARIN WIRD NACH DEM ALIASNAMEN 'IP_CONTEXT' GESUCHT, UND DER WERT  FÜR 'IPExternal' geholt.
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "IPExternal");
			sIp = objEntry.getValue();
					
			//TODO GOON 20190124: Hier soll unterschieden werden zwischen einem absichtlich eingetragenenem Leersstring und nix.
			if(StringZZZ.isEmpty(sIp)){
				sIp = "Enter or refresh";
			}
			
			//- - - - - 
			String sIpLocal="";
			if(StringZZZ.isEmpty(sIpLocal)){
				sIpLocal = "Enter or refresh";
			}
			
			//- - - - - 
			String sIpRouter="";
			if(StringZZZ.isEmpty(sIpRouter)){
				sIpRouter = "Enter or refresh";
			}
			
		
			switch(iRow) {
			case 1:
				this.createRowIpRouter(this, cc, 1, sIpRouter);			
				break;
			case 2:
				this.createRowGeneratePage(this, cc, 2, "Generate page");			
				break;
			case 3:
				this.createRowIpLocal(this, cc, 3, sIpLocal);			
				break;
			case 4:
				this.createRowUploadPage(this, cc, 4, "Upload generated page");			
				break;
			case 5:
				this.createRowIpWeb(this, cc, 5, sIp);
				break;	
			default:
				//Keinen Fehler werfen, da diese Methode in einer Schleife ausgeführt wird.
				//Rückgabewert false ist dann der Abbruch der Schleife
				
//				ExceptionZZZ ez = new ExceptionZZZ("Row not defined for " + iRow + "'", iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
				
				bReturn = false;
				break main;
			}
			
			bReturn = true;
		}
		return bReturn;



	}
	
		
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
	class ActionIpWeb2iniOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
		public ActionIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
			super(objKernel, panelParent);			
		}
		
		@Override
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

		@Override
		public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
			return true;
		}

		@Override
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
					logLineDate("Ip from Program Web2ini'" + sIp + "'");
										
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
						logLineDate("Textfield updated with'" + stext + "'");						
						objProg.updateLabel(stext);
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

		@Override
		public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) {
			
		}
		
}//End class ...KErnelActionCascaded....
	
	
//	#######################################
	//Innere Klassen, welche eine Action behandelt	
class ActionIpRouter2iniOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
	public ActionIpRouter2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
		super(objKernel, panelParent);			
	}
	
	@Override
	public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//		try {
		ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'IpWeb2ini'");
											
		String[] saFlag = null; //{"useProxy"};					
		KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																
		SwingWorker4ProgramIpRouter2iniOVPN worker = new SwingWorker4ProgramIpRouter2iniOVPN(objKernel, panelParent, saFlag);
		worker.start();  
		
	/*} catch (ExceptionZZZ ez) {				
		this.getLogObject().WriteLineDate(ez.getDetailAllLast());
		ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());
	}	*/
		
		return true;
	}

	@Override
	public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
		return true;
	}

	@Override
	public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
	}			 							
	
	class SwingWorker4ProgramIpRouter2iniOVPN extends KernelSwingWorkerZZZ{
		private KernelJPanelCascadedZZZ panel;
		private String[] saFlag4Program;
						
		protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
		
		public SwingWorker4ProgramIpRouter2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
			super(objKernel);
			this.panel = panel;
			this.saFlag4Program = saFlag4Program;					
		}
		
		//#### abstracte - Method aus SwingWorker
		public Object construct() {
			try{
				//1. IP Auslesen von der Webseite
				ProgramIpRouter2iniOVPN objProg = new ProgramIpRouter2iniOVPN(objKernel, this.panel, this.saFlag4Program);
				objProg.reset();
				String sIp = objProg.getIpFromUi();
				logLineDate("Ip from Program Router2ini'" + sIp + "'");
								
				updateTextField(objProg, "writing..."); //Schreibe einen anderen Text in das Feld...
				
				//2. Schreibe in die ini-Datei
				boolean bErg = objProg.writeIpToIni(sIp);
								
				//3. Diesen Wert wieder ins Label zurückschreiben.
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
		public void updateTextField(final ProgramIpRouter2iniOVPN objProg, final String stext){
						
//			Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
			Runnable runnerUpdateLabel= new Runnable(){

				public void run(){
//					In das Textfeld eintragen, das etwas passiert.
					logLineDate("Textfield updated with '" + stext + "'");					
					objProg.updateLabel(stext);					 
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

	@Override
	public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) {
		
	}
	
}//End class ...KErnelActionCascaded....

	
	
//	#######################################
	//Innere Klassen, welche eine Action behandelt	
class ActionIpLocal2iniOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
	public ActionIpLocal2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
		super(objKernel, panelParent);			
	}
	
	@Override
	public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//		try {
		ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'IpLocal2ini'");
											
		String[] saFlag = null; //{"useProxy"};					
		KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																
		SwingWorker4ProgramIpLocal2iniOVPN worker = new SwingWorker4ProgramIpLocal2iniOVPN(objKernel, panelParent, saFlag);
		worker.start();  
		
	/*} catch (ExceptionZZZ ez) {				
		this.getLogObject().WriteLineDate(ez.getDetailAllLast());
		ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());
	}	*/
		
		return true;
	}

	@Override
	public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
		return true;
	}

	@Override
	public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
	}			 							
	
	class SwingWorker4ProgramIpLocal2iniOVPN extends KernelSwingWorkerZZZ{		
		private KernelJPanelCascadedZZZ panel;
		private String[] saFlag4Program;
		
		public SwingWorker4ProgramIpLocal2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
			super(objKernel);
			this.panel = panel;
			this.saFlag4Program = saFlag4Program;					
		}
		
		//#### abstracte - Method aus SwingWorker
		public Object construct() {
			try{
				//1. IP Auslesen von der Webseite
				ProgramIpLocal2iniOVPN objProg = new ProgramIpLocal2iniOVPN(objKernel, this.panel, this.saFlag4Program);
				objProg.reset();
				String sIp = objProg.getIpFromUi();
				logLineDate("Ip from Local2ini'" + sIp + "'");
				
				updateTextField(objProg, "writing..."); //Schreibe einen anderen Text in das Feld...
				
				//2. Schreibe in die ini-Datei
				boolean bErg = objProg.writeIpToIni(sIp);
								
				//3. Diesen Wert wieder ins Label zurückschreiben.
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
		public void updateTextField(final ProgramIpLocal2iniOVPN objProg, final String stext){
						
//			Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
			Runnable runnerUpdateLabel= new Runnable(){

				public void run(){
//					In das Textfeld eintragen, das etwas passiert.
					logLineDate("Textfield updated with '" + stext + "'");					
					objProg.updateLabel(stext);					 
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

	@Override
	public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) {
		
	}
	
}//End class ...KErnelActionCascaded....

	
	
	
//	#######################################
	//Innere Klassen, welche eine Action behandelt
		class ActionIPWebRefreshOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionIPWebRefreshOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
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
			
			class SwingWorker4ProgramIPContentOVPN extends KernelSwingWorkerZZZ{				
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;			
							
				protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
				
				public SwingWorker4ProgramIPContentOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super(objKernel);
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct() {
					try{
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						ProgramIPContentWebOVPN objProg = new ProgramIPContentWebOVPN(objKernel, this.panel, this.saFlag4Program);						
						updateTextField(objProg,"Reading ...");
						
						//2. IP Auslesen von der Webseite										
						String sIp = objProg.getIpExternal();
						logLineDate("Ip from Program ContentWeb '" + sIp + "'");						
												
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
				public void updateTextField(final ProgramIPContentWebOVPN objProg, final String stext){					
					
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld eintragen, das etwas passiert.	
							logLineDate("Textfield updated with '" + stext + "'");							
							objProg.updateLabel(stext);					 
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
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt
			class ActionIPLocalRefreshOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
				public ActionIPLocalRefreshOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
					super(objKernel, panelParent);			
				}
				
				public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//					try {
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
				
				class SwingWorker4ProgramIPContentOVPN extends KernelSwingWorkerZZZ {					
					private KernelJPanelCascadedZZZ panel;
					private String[] saFlag4Program;			
					
					public SwingWorker4ProgramIPContentOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
						super(objKernel);						
						this.panel = panel;
						this.saFlag4Program = saFlag4Program;					
					}
					
					//#### abstracte - Method aus SwingWorker
					public Object construct() {
						try{
							//1. Ins Label schreiben, dass hier ein Update stattfindet
							ProgramIPContentLocalOVPN objProg = new ProgramIPContentLocalOVPN(objKernel, this.panel, this.saFlag4Program);						
							updateTextField(objProg,"Reading ...");
							
							//2. IP Auslesen von der Webseite										
							String sIp = objProg.getIpExternal();
							logLineDate("Ip from Program IPContentLocal '" + sIp + "'");
							
													
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
					public void updateTextField(final ProgramIPContentLocalOVPN objProg, final String stext){					
						
//						Das Schreiben des Ergebnisses wieder an den EventDispatcher thread uebergeben
						Runnable runnerUpdateLabel= new Runnable(){

							public void run(){
//								In das Textfeld eintragen, das etwas passiert.
								logLineDate("Textfield updated with '" + stext + "'");								
								objProg.updateLabel(stext);					 
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
			
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionPageWebCreateOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionPageWebCreateOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
				super(objKernel, panelParent);			
			}
			
			public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//				try {
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'PageWeb-Create'");
													
				String[] saFlag = null;//{"useProxy"};					
				KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																		
				SwingWorker4ProgramPageWebCreateOVPN worker = new SwingWorker4ProgramPageWebCreateOVPN(objKernel, panelParent, saFlag);
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
			
			class SwingWorker4ProgramPageWebCreateOVPN extends KernelSwingWorkerZZZ implements IObjectZZZ, IKernelUserZZZ{
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;				
				private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
															// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
				
				public SwingWorker4ProgramPageWebCreateOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super(objKernel);
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct() {
					try{
						ProgramPageWebCreateOVPN objProgWebPageCreate = new ProgramPageWebCreateOVPN(objKernel, this.panel, this.saFlag4Program);
						
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						updateTextField(objProgWebPageCreate, "Creating ...");
						
						//2. Hochladen der Webseite									
						logLineDate("Creating PageWeb.");						
						boolean bSuccessWebCreated = objProgWebPageCreate.createPageWeb();
						
						//3. Diesen Wert wieder ins Label schreiben.
						if(bSuccessWebCreated) {
							updateTextField(objProgWebPageCreate,"Creation ended with success.");
						}else {
							updateTextField(objProgWebPageCreate,"Creation not successful, details in log.");
						}
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
				public void updateTextField(final ProgramPageWebCreateOVPN objProg, final String stext){
									
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld eintragen, das etwas passiert.	
							logLineDate("Textfield updated with '" + stext + "'");							
							objProg.updateLabel(stext);
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
		
		
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionPageWebUploadOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionPageWebUploadOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
				super(objKernel, panelParent);			
			}
			
			public boolean actionPerformCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
//				try {
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Performing action: 'PageWeb-Upload'");
													
				String[] saFlag = {"useProxy"};					
				KernelJPanelCascadedZZZ panelParent = (KernelJPanelCascadedZZZ) this.getPanelParent();
																		
				SwingWorker4ProgramPageWebUploadOVPN worker = new SwingWorker4ProgramPageWebUploadOVPN(objKernel, panelParent, saFlag);
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
			
			class SwingWorker4ProgramPageWebUploadOVPN extends KernelSwingWorkerZZZ {
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;				
				private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
															// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
				
				public SwingWorker4ProgramPageWebUploadOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super(objKernel);
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct(){
					try{
						ProgramPageWebUploadOVPN objProgWebPageUpload = new ProgramPageWebUploadOVPN(objKernel, this.panel, this.saFlag4Program);
						
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						updateTextField(objProgWebPageUpload, "Uploading ...");
						
						//2. Hochladen der Webseite						
						logLineDate("Uploading WebPage.");						
						boolean bSuccessWebUpload = objProgWebPageUpload.uploadPageWeb();
						
						
						//3. Diesen Wert wieder ins Label schreiben.
						if(bSuccessWebUpload) {
							updateTextField(objProgWebPageUpload,"Upload ended with success.");
						}else {
							updateTextField(objProgWebPageUpload,"Upload not successful, details in log.");
						}
					}catch(ExceptionZZZ ez){
						System.out.println(ez.getDetailAllLast());
						ReportLogZZZ.write(ReportLogZZZ.ERROR, ez.getDetailAllLast());					
					}
//					catch (JSchException jsche) {
//						System.out.println(jsche.getMessage());
//					}
					return "all done";
				}
				
				/**Aus dem Worker-Thread heraus wird ein Thread gestartet (der sich in die EventQueue von Swing einreiht.)
				 *  Entspricht auch ProgramIPContext.updateLabel(..)
				* @param stext
				* 
				* lindhaueradmin; 17.01.2007 12:09:17
				 */
				public void updateTextField(final ProgramPageWebUploadOVPN objProg, final String stext){
									
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld eintragen, das etwas passiert.	
							logLineDate("Textfield updated with '" + stext + "'");							
							objProg.updateLabel(stext);
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

