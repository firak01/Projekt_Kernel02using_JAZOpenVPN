package use.openvpn.client.status;

import use.openvpn.client.process.IProcessWatchRunnerOVPN;

public interface IEventObject4ProcessWatchRunnerStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public IProcessWatchRunnerOVPN.STATUSLOCAL getStatusEnum();
	public String getStatusAbbreviation();
	public String getStatusMessage();
}
