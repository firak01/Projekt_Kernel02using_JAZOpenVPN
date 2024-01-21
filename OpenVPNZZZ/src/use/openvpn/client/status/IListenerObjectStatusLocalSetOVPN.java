package use.openvpn.client.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;

public interface IListenerObjectStatusLocalSetOVPN extends EventListener{
	public boolean changedStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue2ChangeStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
}

