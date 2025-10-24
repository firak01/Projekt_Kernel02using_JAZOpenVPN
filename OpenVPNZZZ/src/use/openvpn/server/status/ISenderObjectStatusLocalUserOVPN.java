package use.openvpn.server.status;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalUserZZZ;

//FGL20251022: Das nicht mehr verwenden, verlassen auf abstrakte Klassen
public interface ISenderObjectStatusLocalUserOVPN extends ISenderObjectStatusLocalUserZZZ{
	//public abstract ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ;
	public abstract void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender);
}
	