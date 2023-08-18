package use.openvpn.server;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import use.openvpn.AbstractApplicationOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainZZZ;

public class ServerApplicationOVPN extends AbstractApplicationOVPN {
	
	public ServerApplicationOVPN(IKernelZZZ objKernel, ServerMainOVPN objServer) throws ExceptionZZZ {
		super(objKernel, (IMainOVPN) objServer);
		this.setServerObject(objServer);
	}
	
	
	
	//######################################################
		//### Getter / Setter
		public ServerMainOVPN getServerObject() {
			return (ServerMainOVPN) this.getMainObject();
		}
		public void setServerObject(ServerMainOVPN objServer) {
			this.setMainObject((IMainOVPN) objServer);
		}						
}
