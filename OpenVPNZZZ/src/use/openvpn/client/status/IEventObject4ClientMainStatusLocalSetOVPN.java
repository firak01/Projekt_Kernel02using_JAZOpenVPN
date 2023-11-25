package use.openvpn.client.status;

import use.openvpn.client.ClientMainOVPN;

public interface IEventObject4ClientMainStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public ClientMainOVPN.STATUSLOCAL getStatusEnum();
}
