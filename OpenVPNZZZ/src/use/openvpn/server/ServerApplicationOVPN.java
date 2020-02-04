package use.openvpn.server;

import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.client.ClientMainZZZ;

public class ServerApplicationOVPN extends KernelUseObjectZZZ{
	private ServerMainZZZ objServer = null;
	
	public ServerApplicationOVPN(IKernelZZZ objKernel, ServerMainZZZ objServer) {
		super(objKernel);
		this.setServerObject(objServer);
	}
	
	//######################################################
		//### Getter / Setter
		public ServerMainZZZ getServerObject() {
			return this.objServer;
		}
		public void setServerObject(ServerMainZZZ objServer) {
			this.objServer = objServer;
		}
		
}
