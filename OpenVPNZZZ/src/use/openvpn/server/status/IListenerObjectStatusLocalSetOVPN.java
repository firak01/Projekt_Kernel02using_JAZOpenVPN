package use.openvpn.server.status;

import java.util.EventListener;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalSetZZZ;

public interface IListenerObjectStatusLocalSetOVPN extends EventListener{
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ;	
}
