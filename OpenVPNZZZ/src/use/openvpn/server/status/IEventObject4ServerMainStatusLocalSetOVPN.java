package use.openvpn.server.status;

import use.openvpn.server.ServerMainOVPN;

public interface IEventObject4ServerMainStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public ServerMainOVPN.STATUSLOCAL getStatusEnum();
}
