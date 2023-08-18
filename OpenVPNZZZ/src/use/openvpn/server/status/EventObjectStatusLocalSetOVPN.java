package use.openvpn.server.status;

import java.util.EventObject;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IObjectZZZ;
import basic.zKernel.status.EventObjectStatusLocalSetZZZ;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.ServerMainOVPN.STATUSLOCAL;
/** 
 * Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )  
 *        Dann erweitert die Event-Klasse aber EventObjekt.
 *  
 *  Merke2: Auch wenn hier nur normale Objekte verwendet weden, kann man in der FLAG-Verarbeitung bestimmt EventObject verwenden.
 *  
 * @author Fritz Lindhauer, 02.04.2023, 12:00:33  
 */
public class EventObjectStatusLocalSetOVPN extends EventObjectStatusLocalSetZZZ implements IEventObjectStatusLocalSetOVPN{
	private ServerMainOVPN.STATUSLOCAL objStatusEnum=null;
		
	/** In dem Konstruktor wird neben der ID dieses Events auch der identifizierende Name der neu gewaehlten Komponente �bergeben.
	 * @param source
	 * @param iID
	 * @param sComponentItemText, z.B. fuer einen DirectoryJTree ist es der Pfad, fuer eine JCombobox der Name des ausgew�hlten Items 
	 */
	public EventObjectStatusLocalSetOVPN(Object source, int iID,  String sStatusText, boolean bStatusValue) {
		super(source,iID,sStatusText,bStatusValue);
	}
	
	public EventObjectStatusLocalSetOVPN(Object source, int iID,  ServerMainOVPN.STATUSLOCAL objStatusEnum, boolean bStatusValue) {
		super(source,iID,"",bStatusValue);
		this.objStatusEnum=objStatusEnum;
	}
	
	@Override
	public ServerMainOVPN.STATUSLOCAL getStatusEnum() {
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
	
	@Override
	public ExceptionZZZ getExceptionObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExceptionObject(ExceptionZZZ objException) {
		// TODO Auto-generated method stub
		
	}

	
}

