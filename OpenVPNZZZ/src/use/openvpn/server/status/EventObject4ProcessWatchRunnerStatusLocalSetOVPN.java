package use.openvpn.server.status;

import use.openvpn.IApplicationOVPN;
import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.process.IProcessWatchRunnerOVPN;
import use.openvpn.server.process.IProcessWatchRunnerOVPN.STATUSLOCAL;

/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObject4ProcessWatchRunnerStatusLocalSetOVPN  extends AbstractEventObjectStatusLocalZZZ implements IEventObject4ProcessWatchRunnerStatusLocalSetOVPN, Comparable<IEventObject4ProcessWatchRunnerStatusLocalSetOVPN>{
	private STATUSLOCAL  objStatusEnum=null;
	private IApplicationOVPN objApplication=null;//Falls Änderungen auch das Backend-Application-Objekt betreffen, wird die aktuelle Version davon dem Event mitgegeben.
	                                             //Hier können dann beim Empfangen des Events die benoetigen Informationen ausgelesen werden.
	private ServerConfigStarterOVPN objStarter=null;
	private int iIndex = -1;
	
	//Merke: Diese Strings sind wichtig für das Interface und kommen nicht aus der abstrakten Klasse
	private String sStatusAbbreviation=null;
	private String sStatusMessage=null;
	
	
	
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 */
	public EventObject4ProcessWatchRunnerStatusLocalSetOVPN(Object source, int iID,  String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);		
	}
	
	public EventObject4ProcessWatchRunnerStatusLocalSetOVPN(Object source, int iID,  String sStatusAbbreviation, String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);
		this.sStatusAbbreviation = sStatusAbbreviation;
	}
	
	public EventObject4ProcessWatchRunnerStatusLocalSetOVPN(Object source, int iID,  STATUSLOCAL  objStatusEnum, boolean bStatusValue) {
		super(source,iID,"",bStatusValue);
		this.objStatusEnum=objStatusEnum;
	}
	
	//### Speziell für OVPN wichtig	
	@Override
	public void setServerConfigStarterObjectUsed(ServerConfigStarterOVPN objStarter) {
		this.objStarter = objStarter;
	}
	
	@Override
	public ServerConfigStarterOVPN getServerConfigStarterObjectUsed() {
		return this.objStarter;
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
	public String getStatusText(){
		if(this.objStatusEnum==null) {
			return this.sStatusText;
		}else {
			return this.objStatusEnum.name();
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
	
	

	//### Aus dem Interface Comparable
	@Override
	public int compareTo(IEventObject4ProcessWatchRunnerStatusLocalSetOVPN o) {
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
     if (!(aThat instanceof EventObject4ProcessWatchRunnerStatusLocalSetOVPN)) return false;
     EventObject4ProcessWatchRunnerStatusLocalSetOVPN that = (EventObject4ProcessWatchRunnerStatusLocalSetOVPN)aThat;
     
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

   //### Aus Interface
 	/* (non-Javadoc)
 	 * @see basic.zKernel.status.AbstractEventObjectStatusLocalSetZZZ#getStatusEnum()
 	 */
 	@Override
 	public STATUSLOCAL getStatusEnum() {
 		return this.objStatusEnum;
 	}
}
