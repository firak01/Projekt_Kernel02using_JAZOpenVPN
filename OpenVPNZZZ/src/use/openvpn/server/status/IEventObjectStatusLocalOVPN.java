package use.openvpn.server.status;

import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import use.openvpn.IApplicationOVPN;
import use.openvpn.server.ServerConfigStarterOVPN;

public interface IEventObjectStatusLocalOVPN extends IEventObjectStatusLocalZZZ{
	public IApplicationOVPN getApplicationObjectUsed(); 
	public void setApplicationObjectUsed(IApplicationOVPN objApplication);
	String getStatusAbbreviation();
	String getStatusMessage();	
	
	//Speziell für OVPN Client
	public void setServerConfigStarterObjectUsed(ServerConfigStarterOVPN serverConfigStarterOVPN);
	public  ServerConfigStarterOVPN getServerConfigStarterObjectUsed();
}
