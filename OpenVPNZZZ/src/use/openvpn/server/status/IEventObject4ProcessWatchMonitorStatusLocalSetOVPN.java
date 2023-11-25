package use.openvpn.server.status;

import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;

public interface IEventObject4ProcessWatchMonitorStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public IServerThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
}
