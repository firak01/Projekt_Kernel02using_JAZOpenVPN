package use.openvpn.client.status;

import use.openvpn.client.process.IProcessWatchRunnerOVPN;

public interface IEventObject4ProcessWatchRunnerStatusLocalOVPN extends IEventObjectStatusLocalOVPN{
	public IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum();
}
