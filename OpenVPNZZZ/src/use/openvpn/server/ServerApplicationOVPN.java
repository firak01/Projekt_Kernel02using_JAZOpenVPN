package use.openvpn.server;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import use.openvpn.AbstractApplicationOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainZZZ;

public class ServerApplicationOVPN extends AbstractApplicationOVPN {
	
	public ServerApplicationOVPN(IKernelZZZ objKernel, ServerMainZZZ objServer) throws ExceptionZZZ {
		super(objKernel, (IMainOVPN) objServer);
		this.setServerObject(objServer);
	}
	
	
	
	//######################################################
		//### Getter / Setter
		public ServerMainZZZ getServerObject() {
			return (ServerMainZZZ) this.getMainObject();
		}
		public void setServerObject(ServerMainZZZ objServer) {
			this.setMainObject((IMainOVPN) objServer);
		}						
}
