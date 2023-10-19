package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;

public interface IEventObject4ClientMainStatusLocalSetOVPN extends IEventObjectStatusLocalSetOVPN{
	public ClientMainOVPN.STATUSLOCAL getStatusEnum();
	public String getStatusAbbreviation();
	public String getStatusMessage();
}
