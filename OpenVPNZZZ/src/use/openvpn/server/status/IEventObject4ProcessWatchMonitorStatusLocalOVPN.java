package use.openvpn.server.status;

import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;

public interface IEventObject4ProcessWatchMonitorStatusLocalOVPN extends IEventObjectStatusLocalOVPN{
	public IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
}
