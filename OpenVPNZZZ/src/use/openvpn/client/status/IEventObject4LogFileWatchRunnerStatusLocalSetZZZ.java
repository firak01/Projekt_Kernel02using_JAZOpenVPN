package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;

public interface IEventObject4LogFileWatchRunnerStatusLocalSetZZZ extends IEventObjectStatusLocalSetZZZ{
	public IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL getStatusEnum();
}
