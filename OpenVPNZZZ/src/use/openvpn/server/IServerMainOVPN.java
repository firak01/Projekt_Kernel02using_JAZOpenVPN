package use.openvpn.server;

import basic.zKernel.status.IEventBrokerStatusLocalSetUserZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalSetZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;
import use.openvpn.server.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalSetOVPN;

public interface IServerMainOVPN extends IStatusLocalUserZZZ, ISenderObjectStatusLocalSetOVPN, IEventBrokerStatusLocalSetUserOVPN{
	public enum FLAGZ{
		DUMMY
	}

//  20230804: Jetzt in ServerMainZZZ verschoben als ServerMainStatusTypeZZZ interne enum-Klasse
//	public enum STATUSLOCAL{
//		ISLAUNCHED,ISSTARTING,ISSTARTED,ISLISTENING,WATCHRUNNERSTARTED,HASERROR
//	}
}
