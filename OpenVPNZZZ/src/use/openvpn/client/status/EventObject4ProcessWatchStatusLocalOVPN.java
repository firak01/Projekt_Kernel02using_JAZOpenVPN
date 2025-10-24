package use.openvpn.client.status;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.AbstractEventObjectStatusLocalZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.process.IProcessWatchRunnerOVPN;
import use.openvpn.client.process.IProcessWatchRunnerOVPN.STATUSLOCAL;

/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObject4ProcessWatchStatusLocalOVPN  extends AbstractEventObjectStatusLocalZZZ implements IEventObject4ProcessWatchRunnerStatusLocalOVPN{//FGL20251022: Das der abstrakten Klasse überlassen, Comparable<IEventObject4ProcessWatchMonitorStatusLocalOVPN>{
	private static final long serialVersionUID = 6090747816820720463L;
	private use.openvpn.client.process.IProcessWatchRunnerOVPN.STATUSLOCAL objStatusEnum=null;
	private IApplicationOVPN objApplication=null;//Falls Änderungen auch das Backend-Application-Objekt betreffen, wird die aktuelle Version davon dem Event mitgegeben.
	                                             //Hier können dann beim Empfangen des Events die benoetigen Informationen ausgelesen werden.
	
	ClientConfigStarterOVPN objStarter=null;
	
	//Merke: Diese Strings sind wichtig für das Interface und kommen nicht aus der abstrakten Klasse
	private String sStatusAbbreviation=null;
	private String sStatusMessage=null;
	
	private int iID=-1;
	
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 * @throws ExceptionZZZ 
	 */
	public EventObject4ProcessWatchStatusLocalOVPN(Object source, int iID,  String sStatusText, boolean bStatusValue) throws ExceptionZZZ {
		super(source,sStatusText,bStatusValue);		
		EventObject4ProcessWatchStatusLocal_(iID, null, null);
	}
	
	public EventObject4ProcessWatchStatusLocalOVPN(Object source, int iID,  String sStatusAbbreviation, String sStatusText, boolean bStatusValue) throws ExceptionZZZ {
		super(source,sStatusText,bStatusValue);
		EventObject4ProcessWatchStatusLocal_(iID, sStatusAbbreviation, null);
	}
	
	public EventObject4ProcessWatchStatusLocalOVPN(Object source, int iID,  IProcessWatchRunnerOVPN.STATUSLOCAL objStatusEnum, boolean bStatusValue) throws ExceptionZZZ {
		super(source,"",bStatusValue);
		EventObject4ProcessWatchStatusLocal_(iID, null, objStatusEnum);
	}
	
	private boolean EventObject4ProcessWatchStatusLocal_(int iID, String sStatusAbbreviation, STATUSLOCAL objStatusEnum) throws ExceptionZZZ{		
		this.iID = iID;
		this.sStatusAbbreviation = sStatusAbbreviation;
		this.objStatusEnum = objStatusEnum;
		return true;
	}
	
	//### speziell für OVPN
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
	public use.openvpn.client.process.IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum() {
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
//	//FGL20251022: Das der abstrakten Klasse überlassen
//	@Override
//	public int compareTo(IEventObject4ProcessWatchMonitorStatusLocalOVPN o) {
//		//Das macht lediglich .sort funktionsfähig und wird nicht bei .equals(...) verwendet.
//		int iReturn = 0;
//		main:{
//			if(o==null)break main;
//			
//			String sTextToCompare = o.getStatusText();
//			boolean bValueToCompare = o.getStatusValue();
//			
//			String sText = this.getStatusText();
//			boolean bValue = this.getStatusValue();
//			
//			if(sTextToCompare.equals(sText) && bValueToCompare==bValue) iReturn = 1;		
//			
//		}
//		return iReturn;
//	}
	
   @Override 
   public boolean equals(Object aThat) {
     if (this == aThat) return true;
     if (!(aThat instanceof EventObject4ProcessWatchStatusLocalOVPN)) return false;
     EventObject4ProcessWatchStatusLocalOVPN that = (EventObject4ProcessWatchStatusLocalOVPN)aThat;
     
     String sNameToCompare = that.getStatusEnum().getName();
	 boolean bValueToCompare = that.getStatusValue();
		
	 String sName = this.getStatusEnum().getName();
	boolean bValue = this.getStatusValue();
     
	if(sNameToCompare.equals(sName) && bValueToCompare==bValue) return true;
		
     return false;     
   }

   @Override
	public int getProcessID() {
		return this.iID;
	}
	
	@Override
	public void setProcessID(int iProcessID) {
		this.iID = iProcessID;
	}

// //FGL20251022: Das der abstrakten Klasse überlassen
//   /** A class that overrides equals must also override hashCode.*/
//   @Override 
//   public int hashCode() {
//	   return this.getStatusText().hashCode();
//   }


}

