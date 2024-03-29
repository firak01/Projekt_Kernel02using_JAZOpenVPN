package use.openvpn.client.status;

import java.util.EventObject;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zKernel.status.AbstractEventObjectStatusLocalMessageReactZZZ;
import basic.zKernel.status.AbstractEventObjectStatusLocalZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.IProcessWatchRunnerOVPN;
import use.openvpn.client.process.ProcessWatchRunnerOVPN;
import use.openvpn.server.ServerMainOVPN;

/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObject4ProcessWatchRunnerStatusLocalOVPN  extends AbstractEventObjectStatusLocalMessageReactZZZ implements IEventObject4ProcessWatchRunnerStatusLocalOVPN, Comparable<IEventObject4ProcessWatchRunnerStatusLocalOVPN>{
	private IProcessWatchRunnerOVPN.STATUSLOCAL objStatusEnum=null;
	private IApplicationOVPN objApplication=null;//Falls Änderungen auch das Backend-Application-Objekt betreffen, wird die aktuelle Version davon dem Event mitgegeben.
	                                             //Hier können dann beim Empfangen des Events die benoetigen Informationen ausgelesen werden.
	private ClientConfigStarterOVPN objStarter=null;
	private int iIndex = -1;
	
	//Merke: Diese Strings sind wichtig für das Interface und kommen nicht aus der abstrakten Klasse
	private String sStatusAbbreviation=null;
	private String sStatusMessage=null;
	
	
	
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 */
	public EventObject4ProcessWatchRunnerStatusLocalOVPN(Object source, int iID,  String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);		
	}
	
	public EventObject4ProcessWatchRunnerStatusLocalOVPN(Object source, int iID,  String sStatusAbbreviation, String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);
		this.sStatusAbbreviation = sStatusAbbreviation;
	}
	
	public EventObject4ProcessWatchRunnerStatusLocalOVPN(Object source, int iID,  IProcessWatchRunnerOVPN.STATUSLOCAL objStatusEnum, boolean bStatusValue) {
		super(source,iID,"",bStatusValue);
		this.objStatusEnum=objStatusEnum;
	}
	
	//### Speziell für OVPN wichtig	
	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IEventObjectStatusLocalSetOVPN#setClientConfigStarterObjectUsed(use.openvpn.client.ClientConfigStarterOVPN)
	 */
	@Override
	public void setClientConfigStarterObjectUsed(ClientConfigStarterOVPN objStarter) {
		this.objStarter = objStarter;
	}
	
	@Override
	public ClientConfigStarterOVPN getClientConfigStarterObjectUsed() {
		return this.objStarter;
	}
	
	//### Aus Interface
	/* (non-Javadoc)
	 * @see basic.zKernel.status.AbstractEventObjectStatusLocalSetZZZ#getStatusEnum()
	 */
	@Override
	public IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum() {
		return this.objStatusEnum;
	}
	
	
	//### aus IEventObjectStatusLocalSetOVPN
	@Override
	public IApplicationOVPN getApplicationObjectUsed() {
		return this.objApplication;
	}

	@Override
	public void setApplicationObjectUsed(IApplicationOVPN objApplication) {
		this.objApplication = objApplication;
	}

	/* (non-Javadoc)
	 * @see use.openvpn.client.status.IEventObjectStatusLocalSetOVPN#getStatusAbbreviation()
	 */
	@Override
	public String getStatusAbbreviation(){
		if(this.objStatusEnum==null) {
			return this.sStatusAbbreviation;
		}else {
			return this.objStatusEnum.getAbbreviation();
		}
	}

	@Override
	public String getStatusMessage() {
		if(this.objStatusEnum==null) {
			return this.sStatusMessage;
		}else {
			return this.objStatusEnum.getStatusMessage();
		}
	}
	
	@Override
	public void setStatusMessage(String sStatusMessage) {
		this.sStatusMessage = sStatusMessage;
		
	}
	

	//### Aus dem Interface Comparable
	@Override
	public int compareTo(IEventObject4ProcessWatchRunnerStatusLocalOVPN o) {
		//Das macht lediglich .sort funktionsfähig und wird nicht bei .equals(...) verwendet.
		int iReturn = 0;
		main:{
			if(o==null)break main;
			
			String sTextToCompare = o.getStatusText();
			boolean bValueToCompare = o.getStatusValue();
			
			String sText = this.getStatusText();
			boolean bValue = this.getStatusValue();
			
			if(sTextToCompare.equals(sText) && bValueToCompare==bValue) iReturn = 1;		
			
		}
		return iReturn;
	}
	
   @Override 
   public boolean equals(Object aThat) {
     if (this == aThat) return true;
     if (!(aThat instanceof EventObject4ProcessWatchRunnerStatusLocalOVPN)) return false;
     EventObject4ProcessWatchRunnerStatusLocalOVPN that = (EventObject4ProcessWatchRunnerStatusLocalOVPN)aThat;
     
     String sNameToCompare = that.getStatusEnum().getName();
	 boolean bValueToCompare = that.getStatusValue();
		
	 String sName = this.getStatusEnum().getName();
	boolean bValue = this.getStatusValue();
     
	if(sNameToCompare.equals(sName) && bValueToCompare==bValue) return true;
		
     return false;     
   }

   /** A class that overrides equals must also override hashCode.*/
   @Override 
   public int hashCode() {
	   return this.getStatusText().hashCode();
   }
}
