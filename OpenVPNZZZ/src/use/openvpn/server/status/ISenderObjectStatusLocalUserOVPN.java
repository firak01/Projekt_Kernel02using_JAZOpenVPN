package use.openvpn.server.status;

import basic.zBasic.ExceptionZZZ;

public interface ISenderObjectStatusLocalUserOVPN {
	public abstract ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ;
	public abstract void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender);
}
	