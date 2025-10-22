package use.openvpn.server.status;

import use.openvpn.server.ServerMainOVPN;

public interface IEventObject4ServerMainStatusLocalSetOVPN extends IEventObjectStatusLocalOVPN{
	public ServerMainOVPN.STATUSLOCAL getStatusEnum();
}
