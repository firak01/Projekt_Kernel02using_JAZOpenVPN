package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zKernelUI.component.ITrayZZZ;
import use.openvpn.serverui.ServerTrayStatusMappedValueOVPN;
import use.openvpn.clientui.ClientTrayStatusMappedValueOVPN;
import use.openvpn.serverui.IServerTrayStatusMappedValueZZZ.ServerTrayStatusTypeZZZ;


public interface ITrayOVPN extends ITrayZZZ{
	public abstract IMainOVPN getMainObject();
	public void setMainObject(IMainOVPN objMain);	
	
	public boolean switchStatus(ServerTrayStatusMappedValueOVPN.ServerTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ;
	public boolean switchStatus(ClientTrayStatusMappedValueOVPN.ClientTrayStatusTypeZZZ enumSTATUS) throws ExceptionZZZ;
}
