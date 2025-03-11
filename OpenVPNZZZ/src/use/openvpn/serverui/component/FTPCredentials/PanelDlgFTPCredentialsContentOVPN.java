package use.openvpn.serverui.component.FTPCredentials;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
import basic.zKernel.flag.IFlagZEnabledZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.IDebugUiZZZ;
import basic.zKernelUI.component.IPanelCascadedZZZ;
import basic.zKernelUI.component.AbstractKernelActionListenerCascadedZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelFormLayoutedZZZ;
import basic.zKernelUI.thread.KernelSwingWorker4UIZZZ;
import basic.zKernelUI.thread.KernelSwingWorkerZZZ;
import basic.zKernelUI.util.JTextFieldHelperZZZ;

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
public class PanelDlgFTPCredentialsContentOVPN  extends KernelJPanelFormLayoutedZZZ implements IKernelProgramZZZ,IConstantProgramFTPCredentialsOVPN{	
	public final int iROW_HEIGHT=20;
	public final int iROWGAP_HEIGHT=5;
	
	public final int iCOLUMNGAP_WIDTH=5;
	
	public final int iCOMPONENT_HEIGHT=25;
	public final int iCOMPONENT_WIDTH=200;
	/**
	 * DEFAULT Konstruktor, notwendig, damit man objClass.newInstance(); einfach machen kann.
	 *                                 
	 * lindhaueradmin, 23.07.2013
	 */
	public PanelDlgFTPCredentialsContentOVPN(){
		super();
	}
	public PanelDlgFTPCredentialsContentOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended) throws ExceptionZZZ {
		super(objKernel, dialogExtended);
		PanelDlgFTPCredentialsContentNew_();
	}
	public PanelDlgFTPCredentialsContentOVPN(IKernelZZZ objKernel, KernelJDialogExtendedZZZ dialogExtended,  HashMap<String, Boolean>hmFlagLocal, HashMap<String, Boolean>hmFlag) throws ExceptionZZZ{	
		super(objKernel, dialogExtended, hmFlagLocal, hmFlag);
		PanelDlgFTPCredentialsContentNew_();
	}
	
	private void PanelDlgFTPCredentialsContentNew_() {		
		String stemp; boolean btemp;
		try{
		//Diese Panel ist Grundlage für diverse INI-Werte auf die über Buttons auf "Programname" zugegriffen wird.
		
		stemp = IKernelProgramZZZ.FLAGZ.ISKERNELPROGRAM.name();
		btemp = this.setFlag(stemp, true);
		if(!btemp) {
			ExceptionZZZ ez = new ExceptionZZZ("Flag is not available '" + stemp + "'. Maybe an interface for this flag is not implemented", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}
						
		//##################################################################
		//### Definition des Masken UIs	
		this.initFormLayoutContent(); 		//Hiermit dann erst die Werte füllen.
		
		/* Das funktioniert nicht. Funktionalitaet des JGoodies-Framework. Warum ???
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
	
	private boolean createRowFTPUser(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			JLabel labelLocal = new JLabel(IConstantProgramFTPCredentialsOVPN.sLABEL_TEXTFIELD_USERNAME);
			Dimension dimLabel = new Dimension((iCOMPONENT_WIDTH/2), iCOMPONENT_HEIGHT);
			labelLocal.setPreferredSize(dimLabel);		
			this.add(labelLocal, cc.xy(2,iRow*2));			
			
			
			//Hier soll unterschieden werden zwischen einem absichtlich eingetragenenem Leersstring und nix.
			boolean bDefaultValue=false;
			if(StringZZZ.isEmpty(sDefaultValue)){
				sDefaultValue = this.sVALUE_TEXTFIELD_USERNAME_INITIAL;
				bDefaultValue=true;
			}
			
			JTextField textfieldFTPUsername = new JTextField(sDefaultValue,  0);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			textfieldFTPUsername.setHorizontalAlignment(JTextField.LEFT);

			//textfieldFTPPassword.setCaretPosition(0); //Cursorposition
			
			Dimension dim = new Dimension(iCOMPONENT_WIDTH, iCOMPONENT_HEIGHT);
			textfieldFTPUsername.setPreferredSize(dim);
			panel.add(textfieldFTPUsername, cc.xy(4,iRow*2));

			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_USERNAME, textfieldFTPUsername);  
				
			//Den bisherigen Inhalt des Textfelds markieren, so dass er beim Tippen sofort überschrieben werden kann.
			if(bDefaultValue) {
				JTextFieldHelperZZZ.markAndFocus(this, textfieldFTPUsername);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.				
			}
			
//			GGfs. weitere Spalten hinzufuegen
//			JButton buttonIpLocal2ini = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpLocal2iniOVPN actionIpLocal2iniOVPN = new ActionIpLocal2iniOVPN(objKernel, this);
//			buttonIpLocal2ini.addActionListener(actionIpLocal2iniOVPN);
//			this.add(buttonIpLocal2ini, cc.xy(6,iRow*2));
			
//			JButton buttonReadIPLocal = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON);
//			ActionIPLocalRefreshOVPN actionIpRefreshLocal = new ActionIPLocalRefreshOVPN(objKernel, this);
//			buttonReadIPLocal.addActionListener(actionIpRefreshLocal);
//			panel.add(buttonReadIPLocal, cc.xy(8,iRow*2));
			
			bReturn = true;
		}//end main;
		return bReturn;
	}
	
	private boolean createRowFTPPasswordDecrypted(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			Dimension dimLabel = new Dimension((iCOMPONENT_WIDTH/2), iCOMPONENT_HEIGHT);
			Dimension dim = new Dimension(iCOMPONENT_WIDTH, iCOMPONENT_HEIGHT);
			
			JLabel labelLocal = new JLabel(IConstantProgramFTPCredentialsOVPN.sLABEL_TEXTFIELD_PASSWORD_DECRYPTED);			
			labelLocal.setPreferredSize(dimLabel);	
			this.add(labelLocal, cc.xy(2,iRow*2));			
			
			boolean bDefaultValue=false;
			if(StringZZZ.isEmpty(sDefaultValue)){
				sDefaultValue = this.sVALUE_TEXTFIELD_PASSWORD_DECRYPTED_INITIAL;
				bDefaultValue=true;
			}

			JTextField textfieldFTPPassword = new JTextField(sDefaultValue, 0);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			textfieldFTPPassword.setHorizontalAlignment(JTextField.LEFT);

			//textfieldFTPPassword.setCaretPosition(0); //Cursorposition						
			textfieldFTPPassword.setPreferredSize(dim);
			panel.add(textfieldFTPPassword, cc.xy(4,iRow*2));
			
			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED, textfieldFTPPassword);  
			
			//Den bisherigen Inhalt des Textfelds markieren, so dass er beim Tippen sofort überschrieben werden kann.
			//if(bDefaultValue) { //!!! IMMER UND NICHT NUR BEIM DEFAULT
			JTextFieldHelperZZZ.markAndFocus(this, textfieldFTPPassword);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.				
			//}
				
			final JButton buttonFTPCredentials2ini = new JButton(IConstantProgramFTPCredentialsOVPN.sLABEL_BUTTON_TO_INI);			
			buttonFTPCredentials2ini.setPreferredSize(dim);
			
			ActionFTPCredentials2iniOVPN actionFTPCredentials2iniOVPN = new ActionFTPCredentials2iniOVPN(objKernel, this);
			buttonFTPCredentials2ini.addActionListener(actionFTPCredentials2iniOVPN);
			
			//Damit ggfs. ein abgeschnittener Buttontext komplett erscheint, etc.
			//buttonFTPCredentials2ini.setToolTipText("<html>Manchester<br>53.4800° N, 2.2400° W</html>");
			buttonFTPCredentials2ini.setToolTipText("<html>"+IConstantProgramFTPCredentialsOVPN.sLABEL_BUTTON_TO_INI+":<br>Password will be encoded<br>and written to the configuration ini file</html>");
			
			//Fuer eine "hover" Effekt auf dem Button
			buttonFTPCredentials2ini.addMouseListener(new java.awt.event.MouseAdapter() {
				    public void mouseEntered(java.awt.event.MouseEvent evt) {
				    	buttonFTPCredentials2ini.setBackground(Color.GREEN);
				    }
				
				    public void mouseExited(java.awt.event.MouseEvent evt) {
				    	buttonFTPCredentials2ini.setBackground(UIManager.getColor("control"));
				    }
			});
			
			
			panel.add(buttonFTPCredentials2ini, cc.xy(6,iRow*2));
				
				
//			GGfs. weitere Spalten hinzufuegen
//			JButton buttonIpLocal2ini = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpLocal2iniOVPN actionIpLocal2iniOVPN = new ActionIpLocal2iniOVPN(objKernel, this);
//			buttonIpLocal2ini.addActionListener(actionIpLocal2iniOVPN);
//			this.add(buttonIpLocal2ini, cc.xy(6,iRow*2));
			
//			JButton buttonReadIPLocal = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON);
//			ActionIPLocalRefreshOVPN actionIpRefreshLocal = new ActionIPLocalRefreshOVPN(objKernel, this);
//			buttonReadIPLocal.addActionListener(actionIpRefreshLocal);
//			panel.add(buttonReadIPLocal, cc.xy(8,iRow*2));
			
			bReturn = true;
		}//end main;
		return bReturn;
	}
	
	private boolean createRowFTPPasswordEncrypted(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			Dimension dimLabel = new Dimension((iCOMPONENT_WIDTH/2), iCOMPONENT_HEIGHT);
			Dimension dim = new Dimension(iCOMPONENT_WIDTH, iCOMPONENT_HEIGHT);
			
			JLabel labelLocal = new JLabel(IConstantProgramFTPCredentialsOVPN.sLABEL_LABEL_PASSWORD_ENCRYPTED);			
			labelLocal.setPreferredSize(dimLabel);	
			this.add(labelLocal, cc.xy(2,iRow*2));			
			
			boolean bDefaultValue=false;
			if(StringZZZ.isEmpty(sDefaultValue)){
				sDefaultValue = this.sVALUE_LABEL_PASSWORD_ENCRYPTED_INITIAL;
				bDefaultValue=true;
			}

			JLabel labelLocalEncrypted = new JLabel(sDefaultValue);			
			labelLocalEncrypted.setPreferredSize(dimLabel);	
			this.add(labelLocalEncrypted, cc.xy(2,iRow*2));			
			panel.add(labelLocalEncrypted, cc.xy(4,iRow*2));

			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			//panel.setComponent(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED, textfieldFTPPassword);  
			panel.setComponent(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_LABEL_PASSWORD_ENCRYPTED, labelLocalEncrypted);
			
			//Den bisherigen Inhalt des Textfelds markieren, so dass er beim Tippen sofort überschrieben werden kann.
			//if(bDefaultValue) { //!!! IMMER UND NICHT NUR BEIM DEFAULT
			//	JTextFieldHelperZZZ.markAndFocus(this, textfieldFTPPassword);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.				
			//}
			
//			GGfs. weitere Spalten hinzufuegen
//			JButton buttonIpLocal2ini = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpLocal2iniOVPN actionIpLocal2iniOVPN = new ActionIpLocal2iniOVPN(objKernel, this);
//			buttonIpLocal2ini.addActionListener(actionIpLocal2iniOVPN);
//			this.add(buttonIpLocal2ini, cc.xy(6,iRow*2));
			
//			JButton buttonReadIPLocal = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON);
//			ActionIPLocalRefreshOVPN actionIpRefreshLocal = new ActionIPLocalRefreshOVPN(objKernel, this);
//			buttonReadIPLocal.addActionListener(actionIpRefreshLocal);
//			panel.add(buttonReadIPLocal, cc.xy(8,iRow*2));
			
			bReturn = true;
		}//end main;
		return bReturn;
	}
	
	private boolean createRowMessage(KernelJPanelCascadedZZZ panel, CellConstraints cc, int iRow, String sDefaultValue) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			
			JLabel labelLocal = new JLabel(IConstantProgramFTPCredentialsOVPN.sLABEL_LABEL_MESSAGE);
			this.add(labelLocal, cc.xy(2,iRow*2));			
			
			
			//Hier soll unterschieden werden zwischen einem absichtlich eingetragenenem Leersstring und nix.
			boolean bDefaultValue=false;
			if(StringZZZ.isEmpty(sDefaultValue)){
				sDefaultValue = this.sVALUE_LABEL_MESSAGE_INITIAL;
				bDefaultValue=true;
			}
			
			JLabel labelMessage = new JLabel(sDefaultValue,  0);//Vorbelegen mit dem "alten" Wert aus der Ini-Datei
			labelMessage.setHorizontalAlignment(JTextField.LEFT);						
			//textfieldFTPPassword.setCaretPosition(0); //Cursorposition
			
			Dimension dim = new Dimension(iCOMPONENT_WIDTH, iCOMPONENT_HEIGHT);
			labelMessage.setPreferredSize(dim);
			panel.add(labelMessage, cc.xy(4,iRow*2));

			// Dieses Feld soll ggfs. einer Aktion in der Buttonleiste zur Verfügung stehen.
			//Als CascadedPanelZZZ, wird diese Componente mit einem Alias versehen und in eine HashMap gepackt.
			//Der Inhalt des Textfelds könnte dann beim O.K. Button in die ini-Datei gepackt werden.
			panel.setComponent(IConstantProgramFTPCredentialsOVPN.sCOMPONENT_LABEL_MESSAGE, labelMessage);  
				
			//Den bisherigen Inhalt des Textfelds markieren, so dass er beim Tippen sofort überschrieben werden kann.
//			if(bDefaultValue) {
//				JTextFieldHelperZZZ.markAndFocus(this, textfieldMessage);//Merke: Jetzt den Cursor noch verändern macht dies wieder rückgängig.				
//			}
			
//			GGfs. weitere Spalten hinzufuegen			
//			JButton buttonIpLocal2ini = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON_TO_INI);
//			ActionIpLocal2iniOVPN actionIpLocal2iniOVPN = new ActionIpLocal2iniOVPN(objKernel, this);
//			buttonIpLocal2ini.addActionListener(actionIpLocal2iniOVPN);
//			this.add(buttonIpLocal2ini, cc.xy(6,iRow*2));
			
//			JButton buttonReadIPLocal = new JButton(IConstantProgramIpLocalOVPN.sLABEL_BUTTON);
//			ActionIPLocalRefreshOVPN actionIpRefreshLocal = new ActionIPLocalRefreshOVPN(objKernel, this);
//			buttonReadIPLocal.addActionListener(actionIpRefreshLocal);
//			panel.add(buttonReadIPLocal, cc.xy(8,iRow*2));
			
			bReturn = true;
		}//end main;
		return bReturn;
	}
	
	
	//#### Interface IFormLayoutUserZZZ
	public ColumnSpec buildColumnSpecGap() {
		ColumnSpec cs = new ColumnSpec(Sizes.dluX(iCOLUMNGAP_WIDTH));
		return cs;
	}

	@Override
	public RowSpec buildRowSpecGap() {
		RowSpec rs = new RowSpec(Sizes.dluX(iROWGAP_HEIGHT));
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
			//zweiter Parameter sind die Zeilen/Rows (hier:  7), Merke: Wenn eine feste Laenge kuerzer ist als der Inhalt, dann wird der Inhalt als "..." dargestellt
			//FormLayout layout = new FormLayout(
			//		"5dlu, right:pref:grow(0.5), 5dlu:grow(0.5), left:50dlu:grow(0.5), 5dlu, center:pref:grow(0.5),5dlu, center:pref:grow(0.5),5dlu",         
			//		"5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu, center:10dlu, 5dlu"); 				 			
			//this.setLayout(layout);              //!!! wichtig: Das layout muss dem Panel zugewiesen werden BEVOR mit constraints die Componenten positioniert werden.
			//CellConstraints cc = new CellConstraints();
			//...		
			//this.createRowIpRouter(this, cc, 1, sIpRouter);							
			//...
			
			
			//Falls kein Debug Modus an ist, hiermit quasi eine Platzhalterzeile einfügen.
			if(!this.getFlag(IDebugUiZZZ.FLAGZ.DEBUGUI_PANELLABEL_ON.name())) {	
				RowSpec rsGap0 = this.buildRowSpecGap();
				listReturn.add(rsGap0);	
			}
			
			RowSpec rs1 = new RowSpec(RowSpec.CENTER,Sizes.dluX( iROW_HEIGHT),0.5);				
			listReturn.add(rs1);
			
			RowSpec rsGap1 = this.buildRowSpecGap();
			listReturn.add(rsGap1);	
			
			//++++++++++
			RowSpec rs2 = new RowSpec(RowSpec.CENTER,Sizes.dluX( iROW_HEIGHT),0.5);				
			listReturn.add(rs2);
			
			RowSpec rsGap2 = this.buildRowSpecGap();
			listReturn.add(rsGap2);
			
			
			//++++++++++
			RowSpec rs3 = new RowSpec(RowSpec.CENTER,Sizes.dluX( iROW_HEIGHT),0.5);				
			listReturn.add(rs3);
			
			RowSpec rsGap3 = this.buildRowSpecGap();
			listReturn.add(rsGap3);
			
			
			//++++++++++
			RowSpec rs4 = new RowSpec(RowSpec.CENTER,Sizes.dluX( iROW_HEIGHT),0.5);				
			listReturn.add(rs4);
			
			RowSpec rsGap4 = this.buildRowSpecGap();
			listReturn.add(rsGap4);
			
			//++++++++++
			RowSpec rs5 = new RowSpec(RowSpec.CENTER,Sizes.dluX( iROW_HEIGHT),0.5);				
			listReturn.add(rs5);
			
			RowSpec rsGap5 = this.buildRowSpecGap();
			listReturn.add(rsGap5);
			
//			//++++++++++
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
			
			ColumnSpec cs1 = new ColumnSpec(ColumnSpec.RIGHT, Sizes.dluX(50), 0.5 );//Spalte: Fuehrungstext
			listReturn.add(cs1);
			listReturn.add(csGap);
							
			ColumnSpec cs2 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(125), 0.5); //Spalte: Texteingabefeld
			listReturn.add(cs2);			
			listReturn.add(csGap);
			
			ColumnSpec cs3 = new ColumnSpec(ColumnSpec.LEFT, Sizes.dluX(50), 0.5 ); //Spalte: Button
			listReturn.add(cs3);
			listReturn.add(csGap);
			
			ColumnSpec cs4 = new ColumnSpec(ColumnSpec.CENTER, Sizes.dluX(20), 0.5 );
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
			String sFTPUser = "";String sPasswordDecrypted = "";String sPasswordEncrypted = "";
					
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

			//DARIN WIRD NACH DEM ALIASNAMEN GESUCHT, UND DER WERT  FÜR 'username' geholt.
			IKernelConfigSectionEntryZZZ objEntryUsername = objKernel.getParameterByProgramAlias(sModule, sProgram, this.sINI_PROPERTY_USERNAME);
			sFTPUser = objEntryUsername.getValue();
			
			//DARIN WIRD NACH DEM ALIASNAMEN GESUCHT, UND DER ENTSCHLUESSELTE WERT  FÜR 'password' geholt.		
			IKernelConfigSectionEntryZZZ objEntryPasswordDecrypted = objKernel.getParameterByProgramAlias(sModule, sProgram, this.sINI_PROPERTY_PASSWORD);
			sPasswordDecrypted = objEntryPasswordDecrypted.getValue();		
			if(StringZZZ.isEmpty(sPasswordDecrypted)){
				sPasswordDecrypted = IConstantProgramFTPCredentialsOVPN.sVALUE_TEXTFIELD_PASSWORD_DECRYPTED_INITIAL;
			}								
			//- - - - - 
						
			//DER VERSCHLUESSELTE WERT SOLL AUCH ZURUECKKOMMEN
			sPasswordEncrypted = objEntryPasswordDecrypted.getValueEncrypted();
			if(StringZZZ.isEmpty(sPasswordEncrypted)){
				sPasswordEncrypted = IConstantProgramFTPCredentialsOVPN.sVALUE_LABEL_PASSWORD_ENCRYPTED_INITIAL;
			}
									
			//- - - - - 
			
			
			switch(iRow) {
			case 1:
				int iRowFTPUser = this.computeContentRowNumberUsed(iRow);
				this.createRowFTPUser(this, cc, iRowFTPUser, sFTPUser);			
				break;
			case 2:
				int iRowFTPPasswordDecrypted = this.computeContentRowNumberUsed(iRow);
				this.createRowFTPPasswordDecrypted(this, cc, iRowFTPPasswordDecrypted, sPasswordDecrypted);			
				break;
			case 3:
				int iRowFTPPasswordEncrypted = this.computeContentRowNumberUsed(iRow);
				this.createRowFTPPasswordEncrypted(this, cc, iRowFTPPasswordEncrypted, sPasswordEncrypted);			
				break;
			case 4:	
				int iRowMessage = this.computeContentRowNumberUsed(iRow);
				this.createRowMessage(this, cc, iRowMessage, IConstantProgramFTPCredentialsOVPN.sVALUE_LABEL_MESSAGE_INITIAL);
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
	class ActionFTPCredentials2iniOVPN extends  AbstractKernelActionListenerCascadedZZZ{ //KernelUseObjectZZZ implements ActionListener{						
		public ActionFTPCredentials2iniOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panelParent) throws ExceptionZZZ{
			super(objKernel, panelParent);			
		}
		
		@Override
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

		@Override
		public boolean actionPerformQueryCustom(ActionEvent ae) throws ExceptionZZZ {
			return true;
		}

		@Override
		public void actionPerformPostCustom(ActionEvent ae, boolean bQueryResult) throws ExceptionZZZ {
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
					objProg.reset();
					String sUsername = objProg.getUsernameFromUi();
					logLineDate("Username from Local2ini'" + sUsername + "'");
					
					String sPasswordDecrypted = objProg.getPasswordFromUi();
					//Unverschluesseltes Kennwort nicht loggen!!! logLineDate("Password from Local2ini'" + sPassword + "'");
					
					updateMessage(objProg, "writing..."); //Schreibe einen anderen Text in das Feld...
					
					//2. Schreibe in die ini-Datei
					boolean bErg = objProg.writeCredentialsToIni(sUsername, sPasswordDecrypted);
									
					//3. Diesen Wert wieder ins Label zurückschreiben.
					updateMessage(objProg, "user and password encrypted written..."); //Schreibe einen anderen Text in das Feld...					
					String sPasswordEncodedWritten = objProg.getPasswordEncodedWritten();
					updateValue(objProg, sPasswordEncodedWritten);
					
					//4. Hier explizit nicht den Cache loeschen!!!
					//   Dann stehen nach einem CANCEL die Werte zur Verfuegung aus dem CACHE.
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

		@Override
		public void actionPerformCustomOnError(ActionEvent ae, ExceptionZZZ ez) {
			
		}
		
	}//End class ...KErnelActionCascaded....	
}

