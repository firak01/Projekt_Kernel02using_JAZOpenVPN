package use.openvpn.server.status;

import use.openvpn.server.process.IProcessWatchRunnerOVPN;

public interface IEventObject4ProcessWatchRunnerStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum();
}
