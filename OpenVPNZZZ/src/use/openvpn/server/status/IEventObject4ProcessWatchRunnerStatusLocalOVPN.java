package use.openvpn.server.status;

import use.openvpn.server.process.IProcessWatchRunnerOVPN;

public interface IEventObject4ProcessWatchRunnerStatusLocalOVPN extends IEventObjectStatusLocalOVPN{
	public IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum();
}
