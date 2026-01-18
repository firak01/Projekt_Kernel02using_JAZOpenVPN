package use.openvpn.server.status;

import java.util.EventListener;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusLocalZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalZZZ;
import use.openvpn.server.status.IEventObjectStatusLocalOVPN;

public interface IListenerObjectStatusLocalOVPN extends IListenerObjectStatusLocalZZZ { //EventListener{
	public boolean changeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
		
	//FGL20251023: Das ist wg. des IndexOfProcess jetzt wohl hier notwendig
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;	
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusLocalZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusLocalZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public HashMap<IEnumSetMappedStatusLocalZZZ, IEnumSetMappedStatusLocalZZZ> createHashMapEnumSetForCascadingStatusLocalCustom();
	
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ; 
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusLocalZZZ objEnumStatusIn) throws ExceptionZZZ;
}
