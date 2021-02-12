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
public class PanelDlgIPExternalContentOVPN  extends KernelJPanelCascadedZZZ{	
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
		
//		
//		KernelJDialogExtendedZZZ dialog = this.getDialogParent();
//		KernelJFrameCascadedZZZ frameParent = null;
//		//Hier nicht, da die Dialogbox schon ein Flag bekommen hat. this.setFlagZ(KernelJPanelCascadedZZZ.FLAGZ.COMPONENT_KERNEL_PROGRAM.name(), true);//Damit wird es zum PROGRAM
//				
//		
//		if(dialog==null){
//			frameParent = this.getFrameParent();									
//			sProgram = frameParent.getClass().getName(); //der Frame, in den dieses Panel eingebettet ist
//			sModule = KernelUIZZZ.searchModuleFirstConfiguredClassname(frameParent); 
//			if(StringZZZ.isEmpty(sModule)){
//				ExceptionZZZ ez = new ExceptionZZZ("No module configured for the parent frame/program: '" +  sProgram + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}		
//		}else{
//			System.out.println(ReflectCodeZZZ.getMethodCurrentName() + "# This is a dialog.....");
//			sModule = dialog.getModuleName();												  								 
//			if(StringZZZ.isEmpty(sModule)) {
//				sModule = this.getModuleName();
//			}
//			if(StringZZZ.isEmpty(sModule)){
//				ExceptionZZZ ez = new ExceptionZZZ("No module configured for this component '" + this.getClass().getName() + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}
//			
//			sProgram = dialog.getProgramName();
//			if(StringZZZ.isEmpty(sProgram)){
//				sProgram = this.getProgramName();
//			}
//			if(StringZZZ.isEmpty(sProgram)){
//				ExceptionZZZ ez = new ExceptionZZZ("No program '" + sProgram + "' configured for the module: '" +  sModule + "'", iERROR_CONFIGURATION_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
//				throw ez;
//			}							
//		}	
				
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
		
		JLabel labelRouter = new JLabel("Server IP (from the Web):");
		this.add(labelRouter, cc.xy(2,2));
		
		JLabel labelIni = new JLabel("Server IP (from the Router):");
		this.add(labelIni, cc.xy(2,4));
			
		
		
		//--------------------------------------------------------------------
		JTextField textfieldIPExternal = new JTextField(sIp, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
		textfieldIPExternal.setHorizontalAlignment(JTextField.LEFT);
		textfieldIPExternal.setCaretPosition(0);
		//Dimension dim = new Dimension(10, 15);
		//textfield.setPreferredSize(dim);
		this.add(textfieldIPExternal, cc.xy(4,2));
		
		// Dieses Feld soll einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds soll dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent("textIpContent", textfieldIPExternal);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		//- - - - - - - 
		JTextField textfieldIPRouter = new JTextField(sIpRouter, 20);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
		textfieldIPRouter.setHorizontalAlignment(JTextField.LEFT);
		//textfieldIPRouter.setCaretPosition(0); //Cursorposition		
		//Dimension dim = new Dimension(10, 15);
		//textfield.setPreferredSize(dim);
		this.add(textfieldIPRouter, cc.xy(4,4));
		
		// Dieses Feld soll einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds soll dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent("textIpRouter", textfieldIPRouter);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
		
		//- - - - - - - -
		JTextField textfieldWebUpload = new JTextField("", 20);
		textfieldWebUpload.setHorizontalAlignment(JTextField.LEFT);
		this.add(textfieldWebUpload, cc.xy(4,10));
		
		// Dieses Feld soll einer Aktion in der Buttonleiste zur Verfügung stehen.
		//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
		//Der Inhalt des Textfelds soll dann beim O.K. Button in die ini-Datei gepackt werden.
		this.setComponent("textWebUpload", textfieldWebUpload);      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		//-------------------------------------------------------------------
		JButton buttonIpWeb2ini = new JButton("to ini");
		ActionIpWeb2iniOVPN actionIpWeb2iniOVPN = new ActionIpWeb2iniOVPN(objKernel, this);
		buttonIpWeb2ini.addActionListener(actionIpWeb2iniOVPN);
		this.add(buttonIpWeb2ini, cc.xy(6,2));
		
		
		
		//-------------------------------------------------------------------
		//Merke: Der Server baut die Internetseite basierend auf dem Ini Eintrag.
		//       Der letzte Eintrag kommt dann aus der aktuellen Web-Version.
		JButton buttonReadIPWeb = new JButton("Refresh server ip from the web.");
		ActionIPRefreshOVPN actionIPRefreshWeb = new ActionIPRefreshOVPN(objKernel, this);
		buttonReadIPWeb.addActionListener(actionIPRefreshWeb);
		this.add(buttonReadIPWeb, cc.xy(8,2));
		
		JButton buttonReadIPRouter = new JButton("TODO; Refresh server ip from the router.");
		ActionIPRefreshOVPN actionIPRefreshRouter = new ActionIPRefreshOVPN(objKernel, this);
		buttonReadIPRouter.addActionListener(actionIPRefreshRouter);
		this.add(buttonReadIPRouter, cc.xy(8,4));
		
		
		JButton buttonWriteIPRouter = new JButton("TODO: Use Router Value.");
		ActionIPRefreshOVPN actionWriteIPRouter = new ActionIPRefreshOVPN(objKernel, this);
		buttonWriteIPRouter.addActionListener(actionWriteIPRouter);
		this.add(buttonWriteIPRouter, cc.xy(8,6));
		
		
		JButton buttonGenerateIPPage = new JButton("TODO: Generate lokal IP-Page.");
		ActionIPRefreshOVPN actionGenerateIPPage = new ActionIPRefreshOVPN(objKernel, this);
		buttonGenerateIPPage.addActionListener(actionGenerateIPPage);
		this.add(buttonGenerateIPPage, cc.xy(8,8));

		JButton buttonUploadIPPage = new JButton("Upload IP-Page.");
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
			
			private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
														// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
			
						
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
					
					updateTextField("writing...");
					boolean bErg = objProg.writeIpToIni(sIp);
					
					
					//3. Diesen Wert wieder ins Label schreiben.
					updateTextField(sIp);
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
			public void updateTextField(String stext){
				this.sText2Update = stext;
				
//				Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
				Runnable runnerUpdateLabel= new Runnable(){

					public void run(){
//						In das Textfeld den gefundenen Wert eintragen, der Wert ist ganz oben als private Variable deklariert			
						ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField 'textIpContent");				
						JTextField textField = (JTextField) panel.getComponent("textIpContent");					
						textField.setText(sText2Update);
						textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
					}
				};
				
				SwingUtilities.invokeLater(runnerUpdateLabel);	
				
//				In das Textfeld eintragen, das etwas passiert.								
				//JTextField textField = (JTextField) panelParent.getComponent("text1");					
				//textField.setText("Lese aktuellen Wert .....");
				
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
				
				private String sText2Update;    //Der Wert, der ins Label geschreiben werden soll. Jier als Variable, damit die intene Runner-Klasse darauf zugreifen kann.
															// Auch: Dieser Wert wird aus dem Web ausgelesen und danach in das Label des Panels geschrieben.
				
							
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
						updateTextField("Reading ...");
						
						//2. IP Auslesen von der Webseite
						ProgramIPContentOVPN objProg = new ProgramIPContentOVPN(objKernel, this.panel, this.saFlag4Program);					
						String sIp = objProg.getIpExternal();
						
						//3. Diesen Wert wieder ins Label schreiben.
						updateTextField(sIp);
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
				public void updateTextField(String stext){
					this.sText2Update = stext;
					
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld den gefundenen Wert eintragen, der Wert ist ganz oben als private Variable deklariert			
							ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField 'textIpContent");				
							JTextField textField = (JTextField) panel.getComponent("textIpContent");					
							textField.setText(sText2Update);
							textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
						}
					};
					
					SwingUtilities.invokeLater(runnerUpdateLabel);	
					
//					In das Textfeld eintragen, das etwas passiert.								
					//JTextField textField = (JTextField) panelParent.getComponent("text1");					
					//textField.setText("Lese aktuellen Wert .....");
					
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
						//1. Ins Label schreiben, dass hier ein Update stattfindet
						updateTextField("Uploading ...");
						
						//2. Hochladen der Webseite
						ProgramPageWebUploadOVPN objProgWebPageUpload = new ProgramPageWebUploadOVPN(objKernel, this.panel, this.saFlag4Program);					
						boolean bSuccessWebUpload = objProgWebPageUpload.uploadPageWeb();
						
						//3. Diesen Wert wieder ins Label schreiben.
						if(bSuccessWebUpload) {
							updateTextField("Upload ended with success.");
						}else {
							updateTextField("Upload not successful, details in log.");
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
				public void updateTextField(String stext){
					this.sText2Update = stext;
					
//					Das Schreiben des Ergebnisses wieder an den EventDispatcher thread �bergeben
					Runnable runnerUpdateLabel= new Runnable(){

						public void run(){
//							In das Textfeld den gefundenen Wert eintragen, der Wert ist ganz oben als private Variable deklariert			
							ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Writing '" + sText2Update + "' to the JTextField 'textWebUpload");				
							JTextField textField = (JTextField) panel.getComponent("textWebUpload");					
							textField.setText(sText2Update);
							textField.setCaretPosition(0);   //Das soll bewirken, dass der Anfang jedes neu eingegebenen Textes sichtbar ist.  
						}
					};
					
					SwingUtilities.invokeLater(runnerUpdateLabel);	
					
//					In das Textfeld eintragen, das etwas passiert.								
					//JTextField textField = (JTextField) panelParent.getComponent("text1");					
					//textField.setText("Lese aktuellen Wert .....");
					
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

