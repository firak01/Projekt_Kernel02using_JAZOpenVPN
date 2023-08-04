package use.openvpn.server;

import basic.zKernel.status.IEventBrokerStatusLocalSetUserZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalSetZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;

public interface IServerMainOVPN extends IStatusLocalUserZZZ, ISenderObjectStatusLocalSetZZZ, IEventBrokerStatusLocalSetUserZZZ{
	public enum FLAGZ{
		DUMMY
	}

//  20230804: Jetzt in ServerMainZZZ verschoben als ServerMainStatusTypeZZZ interne enum-Klasse
//	public enum STATUSLOCAL{
//		ISLAUNCHED,ISSTARTING,ISSTARTED,ISLISTENING,WATCHRUNNERSTARTED,HASERROR
//	}
}
