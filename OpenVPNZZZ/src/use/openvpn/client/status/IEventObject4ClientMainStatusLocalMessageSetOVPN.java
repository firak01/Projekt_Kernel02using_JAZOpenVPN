package use.openvpn.client.status;

import use.openvpn.client.ClientMainOVPN;

public interface IEventObject4ClientMainStatusLocalMessageSetOVPN extends IEventObjectStatusLocalMessageOVPN{
	public ClientMainOVPN.STATUSLOCAL getStatusEnum();
}
