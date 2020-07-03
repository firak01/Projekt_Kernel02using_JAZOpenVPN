package use.openvpn;

import basic.zBasic.ExceptionZZZ;

public interface IApplicationOVPN extends IMainUserOVPN{

	String getProxyHost();

	String getProxyPort();

	String getIpLocal() throws ExceptionZZZ;
	
	String getVpnIpLocal() throws ExceptionZZZ;

	String getTapAdapterUsed() throws ExceptionZZZ;

	String getVpnIpRemote() throws ExceptionZZZ;

}
