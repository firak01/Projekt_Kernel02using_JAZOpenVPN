package use.openvpn.server.status;

import use.openvpn.IApplicationOVPN;
import use.openvpn.server.IServerMainOVPN;
import use.openvpn.server.IServerMainOVPN.STATUSLOCAL;
import use.openvpn.server.ServerConfigStarterOVPN;;

/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObjectStatusLocalSetOVPN_kann_geloescht_werden extends  AbstractEventObjectStatusLocalZZZ implements IEventObjectStatusLocalSetOVPN, Comparable<IEventObjectStatusLocalSetOVPN>{
	private STATUSLOCAL objStatusEnum=null;	
	private IApplicationOVPN objApplication=null;//Falls Änderungen auch das Backend-Application-Objekt betreffen, wird die aktuelle Version davon dem Event mitgegeben.
	                                             //Hier können dann beim Empfangen des Events die benoetigen Informationen ausgelesen werden.
	private String sStatusAbbreviation = null;
	
	
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 */
	public EventObjectStatusLocalSetOVPN_kann_geloescht_werden(Object source, int iID,  String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);
	}
	
	public EventObjectStatusLocalSetOVPN_kann_geloescht_werden(Object source, int iID,  STATUSLOCAL objStatusEnum, boolean bStatusValue) {
		super(source,iID,"",bStatusValue);
		this.objStatusEnum=objStatusEnum;
	}
	
	
	//### Aus Interface
	/* (non-Javadoc)
	 * @see basic.zKernel.status.AbstractEventObjectStatusLocalSetZZZ#getStatusEnum()
	 */
	@Override
	public STATUSLOCAL getStatusEnum() {
		return this.objStatusEnum;
	}
	
	@Override
	public String getStatusText(){
		if(this.objStatusEnum==null) {
			return super.getStatusText();
		}else {
			return this.objStatusEnum.getStatusMessage();
		}
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

	@Override
	public String getStatusAbbreviation(){
		if(this.objStatusEnum==null) {
			return this.sStatusAbbreviation;
		}else {
			return this.objStatusEnum.getAbbreviation();
		}
	}
	
	//### Aus dem Interface Comparable
	@Override
	public int compareTo(IEventObjectStatusLocalSetOVPN o) {
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
     if (!(aThat instanceof EventObjectStatusLocalSetOVPN_kann_geloescht_werden)) return false;
     EventObjectStatusLocalSetOVPN_kann_geloescht_werden that = (EventObjectStatusLocalSetOVPN_kann_geloescht_werden)aThat;
     
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

@Override
public String getStatusMessage() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void setServerConfigStarterObjectUsed(ServerConfigStarterOVPN clientConfigStarterOVPN) {
	// TODO Auto-generated method stub
	
}

@Override
public ServerConfigStarterOVPN getServerConfigStarterObjectUsed() {
	// TODO Auto-generated method stub
	return null;
}

	
}

