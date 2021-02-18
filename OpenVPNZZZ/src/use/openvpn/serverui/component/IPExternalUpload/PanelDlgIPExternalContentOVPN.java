package use.openvpn.serverui.component.IPExternalUpload;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import use.openvpn.serverui.component.IPExternalUpload.ProgramIPContentOVPN;
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
public class PanelDlgIPExternalContentOVPN  extends KernelJPanelCascadedZZZ {	
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
		try{
		//Diese Panel ist Grundlage für diverse INI-Werte auf die über Buttons auf "Programname" zugegriffen wird.
		this.setFlagZ(IKernelProgramZZZ.FLAGZ.ISKERNELPROGRAM.name(), true);	
		
		//#############################################################################################
		//### Auslesen des bisher verwendeten ini-Eintrags. 
		//### Merke: Das wäre ggfs. der zuletzt ins Web gebrachte Wert.
		//20190123: Lies die zuvor eingegebene / ausgelesene IPAdresse aus der ini-Datei aus.
		String sIp = "";
				
		//Wichtige Informationen, zum Auslesen von Parametern aus der KernelConfiguration
		String sProgram; String sModule;
		sModule = this.getModuleName();
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
		
		//- - - - - 
		String sIpRouter="";
		if(StringZZZ.isEmpty(sIpRouter)){
			sIpRouter = "Enter or refresh";
		}
		//#######################################
		
		
		//##################################################################
		//### Definition des Masken UIs
		//###
		//Diese einfache Maske besteht aus 3 Zeilen und 6 Spalten. 
		//Es gibt außen einen Rand von jeweils einer Spalte/Zeile
		//Merke: gibt man pref an, so bewirkt dies, das die Spalte beim veraendern der Fenstergröße nicht angepasst wird, auch wenn grow dahinter steht.
		
		//Maske für die Serverkonfiguration, unterscheidet sich von der Maske für die Clientkonfiguration.
		//erster Parameter sind die Spalten/Columns (hier: vier 5dlu), als Komma getrennte Eintraege. .
		//zweiter Parameter sind die Zeilen/Rows (hier:  drei, immer mit "kleiner Zeile für zusätzlichen Abstand"), Merke: Wenn eine feste Laenge kuerzer ist als der Inhalt, dann wird der Inhalt als "..." dargestellt
		FormLayout layout = new FormLayout(
				"5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu, center:pref:grow(0.5),5dlu",         
				"5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu"); 				 
		this.setLayout(layout);              //!!! wichtig: Das layout muss dem Panel zugewiesen werden BEVOR mit constraints die Componenten positioniert werden.
		CellConstraints cc = new CellConstraints();
		
		JLabel labelRouter = new JLabel(IConstantProgramIpWebOVPN.sLABEL_TEXTFIELD);
		this.add(labelRouter, cc.xy(2,2));
		
		JLabel labelIni = new JLabel(IConstantProgramIpRouterOVPN.sLABEL_TEXTFIELD);
		this.add(labelIni, cc.xy(2,4));
			
		
		
		//--------------------------------------------------------------------
		JTextField textfieldIPExternal = new JTextField(sIp, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
		textfieldIPExternal.setHorizontalAlignment(JTextField.LEFT);
		textfieldIPExternal.setCaretPosition(0);
		//Dimension dim = new Dimension(10, 15);
		//textfield.setPreferredSize(dim);
		this.add(textfieldIPExternal, cc.xy(4,2));
		
		// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent(IConstantProgramIpWebOVPN.sCOMPONENT_TEXTFIELD, textfieldIPExternal);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		//- - - - - - - 
		JTextField textfieldIPRouter = new JTextField(sIpRouter, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
		textfieldIPRouter.setHorizontalAlignment(JTextField.LEFT);
		//textfieldIPRouter.setCaretPosition(0); //Cursorposition		
		//Dimension dim = new Dimension(10, 15);
		//textfield.setPreferredSize(dim);
		this.add(textfieldIPRouter, cc.xy(4,4));
		
		// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent(IConstantProgramIpRouterOVPN.sCOMPONENT_TEXTFIELD, textfieldIPRouter);  
		
		//- - - - - - - -
		JTextField textfieldWebCreate = new JTextField("", 20);
		textfieldWebCreate.setHorizontalAlignment(JTextField.LEFT);
		this.add(textfieldWebCreate, cc.xyw(4,8,3)); //Mehrere Spalten umfassend
		
		// Dieses Feld soll einer ggfs. Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent(IConstantProgramPageWebCreateOVPN.sCOMPONENT_TEXTFIELD, textfieldWebCreate);      
		
		
		
		//- - - - - - - -
		JTextField textfieldWebUpload = new JTextField("", 20);
		textfieldWebUpload.setHorizontalAlignment(JTextField.LEFT);
		this.add(textfieldWebUpload, cc.xyw(4,10,3)); //Mehrere Spalten umfassend
		
		// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent(IConstantProgramPageWebUploadOVPN.sCOMPONENT_TEXTFIELD, textfieldWebUpload);  
												
		//-------------------------------------------------------------------
		JButton buttonIpWeb2ini = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON_TO_INI);
		ActionIpWeb2iniOVPN actionIpWeb2iniOVPN = new ActionIpWeb2iniOVPN(objKernel, this);
		buttonIpWeb2ini.addActionListener(actionIpWeb2iniOVPN);
		this.add(buttonIpWeb2ini, cc.xy(6,2));
		
		//-------------------------------------------------------------------
		JButton buttonIpRouter2ini = new JButton(IConstantProgramIpRouterOVPN.sLABEL_BUTTON_TO_INI);
		ActionIpRouter2iniOVPN actionIpRouter2iniOVPN = new ActionIpRouter2iniOVPN(objKernel, this);
		buttonIpRouter2ini.addActionListener(actionIpRouter2iniOVPN);
		this.add(buttonIpRouter2ini, cc.xy(6,4));

		//-------------------------------------------------------------------
		//Merke: Der Server baut die Internetseite basierend auf dem Ini Eintrag.
		//       Der letzte Eintrag kommt dann aus der aktuellen Web-Version.
		JButton buttonReadIPWeb = new JButton(IConstantProgramIpWebOVPN.sLABEL_BUTTON);
		ActionIPRefreshOVPN actionIPRefreshWeb = new ActionIPRefreshOVPN(objKernel, this);
		buttonReadIPWeb.addActionListener(actionIPRefreshWeb);
		this.add(buttonReadIPWeb, cc.xy(8,2));
		
		JButton buttonReadIPRouter = new JButton(IConstantProgramIpRouterOVPN.sLABEL_BUTTON);
		ActionIPRefreshOVPN actionIPRefreshRouter = new ActionIPRefreshOVPN(objKernel, this);
		buttonReadIPRouter.addActionListener(actionIPRefreshRouter);
		this.add(buttonReadIPRouter, cc.xy(8,4));
		
		
		JButton buttonWriteIPRouter = new JButton("xxx");
		ActionIPRefreshOVPN actionWriteIPRouter = new ActionIPRefreshOVPN(objKernel, this);
		buttonWriteIPRouter.addActionListener(actionWriteIPRouter);
		this.add(buttonWriteIPRouter, cc.xy(8,6));
		
		
		JButton buttonGenerateIPPage = new JButton(IConstantProgramPageWebCreateOVPN.sLABEL_BUTTON);
		ActionPageWebCreateOVPN actionGenerateIPPage = new ActionPageWebCreateOVPN(objKernel, this);
		buttonGenerateIPPage.addActionListener(actionGenerateIPPage);
		this.add(buttonGenerateIPPage, cc.xy(8,8));

		JButton buttonUploadIPPage = new JButton(IConstantProgramPageWebUploadOVPN.sLABEL_BUTTON);
		ActionPageWebUploadOVPN actionUploadIPPage = new ActionPageWebUploadOVPN(objKernel, this);
		buttonUploadIPPage.addActionListener(actionUploadIPPage);
		this.add(buttonUploadIPPage, cc.xy(8,10));
		
		
		
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
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
	class ActionIpWeb2iniOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
		public ActionIpWeb2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
			super(objKernel, panelParent);			
		}
		
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
		
}//End class ...KErnelActionCascaded....
	
	
	
//	#######################################
	//Innere Klassen, welche eine Action behandelt	
class ActionIpRouter2iniOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
	public ActionIpRouter2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
		super(objKernel, panelParent);			
	}
	
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

	public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
		return true;
	}

	public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
	}			 							
	
	class SwingWorker4ProgramIpRouter2iniOVPN extends SwingWorker implements IObjectZZZ, IKernelUserZZZ{
		private IKernelZZZ objKernel;
		private LogZZZ objLog;
		private KernelJPanelCascadedZZZ panel;
		private String[] saFlag4Program;
						
		protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
		
		public SwingWorker4ProgramIpRouter2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
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
				ProgramIpRouter2iniOVPN objProg = new ProgramIpRouter2iniOVPN(objKernel, this.panel, this.saFlag4Program);
				objProg.reset();
				String sIp = objProg.getIpFromUi();
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

	
	
	
//	#######################################
	//Innere Klassen, welche eine Action behandelt
		class ActionIPRefreshOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionIPRefreshOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
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
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						ProgramIPContentOVPN objProg = new ProgramIPContentOVPN(objKernel, this.panel, this.saFlag4Program);						
						updateTextField(objProg,"Reading ...");
						
						//2. IP Auslesen von der Webseite										
						String sIp = objProg.getIpExternal();
												
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
		
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionPageWebCreateOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionPageWebCreateOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
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
			
			class SwingWorker4ProgramPageWebCreateOVPN extends SwingWorker implements IObjectZZZ, IKernelUserZZZ{
				private IKernelZZZ objKernel;
				private LogZZZ objLog;
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;
				
				private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
															// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
				
							
				protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
				
				public SwingWorker4ProgramPageWebCreateOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super();
					this.objKernel = objKernel;
					this.objLog = objKernel.getLogObject();
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
						boolean bSuccessWebUpload = objProgWebPageCreate.createPageWeb();
						
						//3. Diesen Wert wieder ins Label schreiben.
						if(bSuccessWebUpload) {
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
		
		
		
//		#######################################
		//Innere Klassen, welche eine Action behandelt	
		class ActionPageWebUploadOVPN extends  KernelActionCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
			public ActionPageWebUploadOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent){
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
			
			class SwingWorker4ProgramPageWebUploadOVPN extends SwingWorker implements IObjectZZZ, IKernelUserZZZ{
				private IKernelZZZ objKernel;
				private LogZZZ objLog;
				private KernelJPanelCascadedZZZ panel;
				private String[] saFlag4Program;
				
				private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
															// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
				
							
				protected ExceptionZZZ objException = null;    // diese Exception hat jedes Objekt
				
				public SwingWorker4ProgramPageWebUploadOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlag4Program){
					super();
					this.objKernel = objKernel;
					this.objLog = objKernel.getLogObject();
					this.panel = panel;
					this.saFlag4Program = saFlag4Program;					
				}
				
				//#### abstracte - Method aus SwingWorker
				public Object construct() {
					try{
						ProgramPageWebUploadOVPN objProgWebPageUpload = new ProgramPageWebUploadOVPN(objKernel, this.panel, this.saFlag4Program);
						
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						updateTextField(objProgWebPageUpload, "Uploading ...");
						
						//2. Hochladen der Webseite										
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
