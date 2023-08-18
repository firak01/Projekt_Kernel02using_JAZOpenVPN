package use.openvpn.server.status;

import basic.zBasic.IObjectZZZ;
import basic.zBasic.util.datatype.enums.EnumZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.server.ServerMainOVPN.STATUSLOCAL;

public interface IEventObjectStatusLocalSetOVPN extends IEventObjectStatusLocalSetZZZ{
	public ServerMainOVPN.STATUSLOCAL getStatusEnum();
}
