package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalMessageZZZ;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;

public interface IEventObject4LogFileWatchRunnerStatusLocalZZZ extends IEventObjectStatusLocalOVPN{
	public IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
}
