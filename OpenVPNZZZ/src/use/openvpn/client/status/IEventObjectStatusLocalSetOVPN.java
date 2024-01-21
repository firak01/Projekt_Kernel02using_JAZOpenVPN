package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalMessageSetZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;

public interface IEventObjectStatusLocalSetOVPN extends IEventObjectStatusLocalMessageSetZZZ{
	public IApplicationOVPN getApplicationObjectUsed(); 
	public void setApplicationObjectUsed(IApplicationOVPN objApplication);
	String getStatusAbbreviation();
		
	//Speziell für OVPN Client
	public void setClientConfigStarterObjectUsed(ClientConfigStarterOVPN clientConfigStarterOVPN);
	public  ClientConfigStarterOVPN getClientConfigStarterObjectUsed();	 
}
