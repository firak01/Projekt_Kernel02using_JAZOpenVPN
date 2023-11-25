package use.openvpn.server.status;

import basic.zBasic.ExceptionZZZ;

public interface ISenderObjectStatusLocalSetUserOVPN {
	public abstract ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ;
	public abstract void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender);
}
	