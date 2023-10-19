package use.openvpn.server.status;

import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.server.ServerMainOVPN;

public interface IEventObjectStatusLocalSetOVPN extends IEventObjectStatusLocalSetZZZ{
	public ServerMainOVPN.STATUSLOCAL getStatusEnum();

	public IApplicationOVPN getApplicationObjectUsed(); 
	public void setApplicationObjectUsed(IApplicationOVPN objApplication);

	String getStatusAbbreviation();	
}
