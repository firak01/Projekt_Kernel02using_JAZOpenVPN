package use.openvpn.client.status;

import use.openvpn.client.ClientMainOVPN;

public interface IEventObject4ClientMainStatusLocalMessageOVPN extends IEventObjectStatusLocalOVPN{
	public ClientMainOVPN.STATUSLOCAL getStatusEnum();
}
