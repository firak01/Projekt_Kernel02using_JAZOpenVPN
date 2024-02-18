package use.openvpn.server.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;
import use.openvpn.client.status.IEventObjectStatusLocalOVPN;

public interface IListenerObjectStatusLocalSetOVPN extends EventListener{
	public boolean changeStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevant(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByClass(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocal(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventRelevantByStatusLocalValue(IEventObjectStatusLocalOVPN eventStatusLocalSet) throws ExceptionZZZ;
}
