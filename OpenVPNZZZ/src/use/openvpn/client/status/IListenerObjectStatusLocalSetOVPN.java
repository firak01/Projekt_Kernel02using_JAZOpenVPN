package use.openvpn.client.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;

public interface IListenerObjectStatusLocalSetOVPN extends EventListener{
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
}

