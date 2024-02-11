package use.openvpn.client.status;

import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;

public interface IEventObject4VpnIpPingerStatusLocalOVPN extends IEventObjectStatusLocalOVPN{
	public IClientThreadVpnIpPingerOVPN.STATUSLOCAL getStatusEnum();
}
