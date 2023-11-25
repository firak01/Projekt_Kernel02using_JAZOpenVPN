package use.openvpn;

import basic.zBasic.ExceptionZZZ;

public interface IApplicationOVPN extends IMainUserOVPN{

	String getProxyHost();

	String getProxyPort();

	String getIpIni() throws ExceptionZZZ;
	
	String getIpLocal() throws ExceptionZZZ;
	
	String getVpnIpLocal() throws ExceptionZZZ;

	String getTapAdapterUsed() throws ExceptionZZZ;

	String getVpnIpRemote() throws ExceptionZZZ;
	
	String getVpnIpRemoteEstablished() throws ExceptionZZZ;
	public void setVpnIpRemoteEstablished(String sVpnIp);

	String getVpnPortRemote() throws ExceptionZZZ;
}
