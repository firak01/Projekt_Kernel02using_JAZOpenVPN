package use.openvpn.client.status;

import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;

public interface IEventObject4ProcessWatchMonitorStatusLocalOVPN extends IEventObjectStatusLocalOVPN{
	public IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
}
