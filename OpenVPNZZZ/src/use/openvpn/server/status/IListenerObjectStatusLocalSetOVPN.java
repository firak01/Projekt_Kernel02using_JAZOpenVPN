package use.openvpn.server.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;
import use.openvpn.server.status.IEventObjectStatusLocalSetOVPN;

public interface IListenerObjectStatusLocalSetOVPN extends EventListener{
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
	public boolean isEventStatusLocalRelevant(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;
}
