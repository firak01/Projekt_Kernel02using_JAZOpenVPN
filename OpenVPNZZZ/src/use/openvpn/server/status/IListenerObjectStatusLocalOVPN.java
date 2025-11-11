package use.openvpn.server.status;

import java.util.EventListener;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
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
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public HashMap<IEnumSetMappedStatusZZZ, IEnumSetMappedStatusZZZ> createHashMapEnumSetForCascadingStatusLocalCustom();
	
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ; 
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ;
}
