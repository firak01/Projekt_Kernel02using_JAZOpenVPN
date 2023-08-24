package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.client.ClientMainOVPN;

public interface IEventObjectStatusLocalSetOVPN extends IEventObjectStatusLocalSetZZZ{
	public ClientMainOVPN.STATUSLOCAL getStatusEnum();
}
