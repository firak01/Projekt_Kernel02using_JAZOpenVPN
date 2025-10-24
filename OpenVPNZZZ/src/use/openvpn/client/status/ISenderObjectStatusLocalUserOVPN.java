package use.openvpn.client.status;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalUserZZZ;

public interface ISenderObjectStatusLocalUserOVPN extends ISenderObjectStatusLocalUserZZZ{
	//public abstract ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ;
	public abstract void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender);
}
	