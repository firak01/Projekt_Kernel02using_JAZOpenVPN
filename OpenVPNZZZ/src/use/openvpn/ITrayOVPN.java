package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zKernelUI.component.tray.ITrayZZZ;
import use.openvpn.clientui.component.tray.ClientTrayStatusMappedValueOVPN;
import use.openvpn.serverui.component.tray.ServerTrayStatusMappedValueOVPN;
import use.openvpn.serverui.component.tray.IServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ;


public interface ITrayOVPN extends IMainUserOVPN, ITrayZZZ{
	//Durch TypeCasts jeweils gelöst
	//public boolean switchStatus(ServerTrayStatusMappedValueOVPN.ServerTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ;
	//public boolean switchStatus(ClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ;
}
