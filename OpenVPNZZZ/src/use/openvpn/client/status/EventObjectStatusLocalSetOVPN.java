package use.openvpn.client.status;

import java.util.EventObject;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zKernel.status.EventObjectStatusLocalSetZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.server.ServerMainOVPN;

/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObjectStatusLocalSetOVPN extends EventObjectStatusLocalSetZZZ implements IEventObjectStatusLocalSetOVPN{
	private ClientMainOVPN.STATUSLOCAL objStatusEnum=null;
	private IApplicationOVPN objApplication=null;//Falls Änderungen auch das Backend-Application-Objekt betreffen, wird die aktuelle Version davon dem Event mitgegeben.
	                                             //Hier können dann beim Empfangen des Events die benoetigen Informationen ausgelesen werden.
	
	private String sStatusAbbreviation = null;
	
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 */
	public EventObjectStatusLocalSetOVPN(Object source, int iID,  String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);		
	}
	
	public EventObjectStatusLocalSetOVPN(Object source, int iID,  String sStatusAbbreviation, String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);
		this.sStatusAbbreviation = sStatusAbbreviation;
	}
	
	public EventObjectStatusLocalSetOVPN(Object source, int iID,  ClientMainOVPN.STATUSLOCAL objStatusEnum, boolean bStatusValue) {
		super(source,iID,"",bStatusValue);
		this.objStatusEnum=objStatusEnum;
	}
	
	
	//### Aus Interface
	@Override
	public ClientMainOVPN.STATUSLOCAL getStatusEnum() {
		return this.objStatusEnum;
	}
	
	@Override
	public IApplicationOVPN getApplicationObjectUsed() {
		return this.objApplication;
	}

	@Override
	public void setApplicationObjectUsed(IApplicationOVPN objApplication) {
		this.objApplication = objApplication;
	}

	
	//+++++++++++
	@Override
	public String getStatusAbbreviation(){
		if(this.objStatusEnum==null) {
			return this.sStatusAbbreviation;
		}else {
			return this.objStatusEnum.getAbbreviation();
		}
	}
	
	//++++++++++
	@Override
	public String getStatusText(){
		if(this.objStatusEnum==null) {
			return super.getStatusText();
		}else {
			return this.objStatusEnum.getStatusMessage();
		}
	}
	
	
	//++++++++
	@Override
	public ExceptionZZZ getExceptionObject() {
		return null;
	}

	@Override
	public void setExceptionObject(ExceptionZZZ objException) {
	}
}

