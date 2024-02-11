package use.openvpn.client.status;

import basic.zKernel.status.IEventObjectStatusLocalMessageZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;

public interface IEventObjectStatusLocalOVPN extends IEventObjectStatusLocalMessageZZZ{
	public IApplicationOVPN getApplicationObjectUsed(); 
	public void setApplicationObjectUsed(IApplicationOVPN objApplication);
	String getStatusAbbreviation();
		
	//Speziell für OVPN Client
	public void setClientConfigStarterObjectUsed(ClientConfigStarterOVPN clientConfigStarterOVPN);
	public  ClientConfigStarterOVPN getClientConfigStarterObjectUsed();	 
}
