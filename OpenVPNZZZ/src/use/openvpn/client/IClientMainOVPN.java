package use.openvpn.client;

import basic.zKernel.status.IStatusLocalUserZZZ;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;

public interface IClientMainOVPN extends IStatusLocalUserZZZ, IEventBrokerStatusLocalSetUserOVPN{
	public enum FLAGZ{
		LAUNCHONSTART, ENABLEPORTSCAN, USEPROXY, DUMMY
	}

//  In ClientMainZZZ verschoben als ClientMainStatusTypeZZZ interne enum-Klasse
//	public enum STATUSLOCAL{
//		ISLAUNCHED,ISSTARTING,ISSTARTED,ISLISTENING,WATCHRUNNERSTARTED,HASERROR
//	}
}
