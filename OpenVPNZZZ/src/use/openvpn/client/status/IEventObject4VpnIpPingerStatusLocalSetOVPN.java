package use.openvpn.client.status;

import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;

public interface IEventObject4VpnIpPingerStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public IClientThreadVpnIpPingerOVPN.STATUSLOCAL getStatusEnum();
}
