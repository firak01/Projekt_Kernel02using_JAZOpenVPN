package use.openvpn.client.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;

public interface IListenerObjectStatusLocalOVPN extends EventListener{
	public boolean changedStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	
	//FGL20251023: Das ist wg. des IndexOfProcess jetzt wohl hier notwendig
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	
	
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ;
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnumStatusIn) throws ExceptionZZZ;
}

