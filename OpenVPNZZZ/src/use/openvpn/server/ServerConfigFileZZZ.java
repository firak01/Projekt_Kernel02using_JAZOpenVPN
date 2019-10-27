package use.openvpn.server;

import java.io.File;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import use.openvpn.ConfigFileZZZ;

/**This class should make changes the content of a configuration file.
 * It extends ConfigFileZZZ.
 * 
 * But unlike ClientConfigFileZZZ, till now no changes are necessary.
 * @author 0823
 *
 */
public class ServerConfigFileZZZ extends ConfigFileZZZ{

	public ServerConfigFileZZZ(IKernelZZZ objKernel, File objFile, String[] saFlagControl) throws ExceptionZZZ {
		super(objKernel, objFile, saFlagControl);
	}

}
