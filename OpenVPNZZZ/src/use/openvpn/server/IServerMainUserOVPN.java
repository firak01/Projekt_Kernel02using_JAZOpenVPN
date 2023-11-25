package use.openvpn.server;

public interface IServerMainUserOVPN {
	public void setMainObject(IServerMainOVPN objClientBackend);
	public IServerMainOVPN getMainObject();
}
