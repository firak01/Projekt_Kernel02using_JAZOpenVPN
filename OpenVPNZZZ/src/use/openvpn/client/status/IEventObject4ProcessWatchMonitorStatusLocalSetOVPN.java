package use.openvpn.client.status;

import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;

public interface IEventObject4ProcessWatchMonitorStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
	public String getStatusAbbreviation();
	public String getStatusMessage();
}
