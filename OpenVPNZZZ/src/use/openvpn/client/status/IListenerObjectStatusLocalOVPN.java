package use.openvpn.client.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;

public interface IListenerObjectStatusLocalOVPN extends EventListener{
	public boolean changedStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
}

