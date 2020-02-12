package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import custom.zUtil.io.FileZZZ;
import use.openvpn.AbstractConfigUpdaterOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.ConfigFileOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileTextParserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ClientConfigUpdaterZZZ extends AbstractConfigUpdaterOVPN {

//private FileInputStream filein=null;
// Die Properties erf�llen nicht meine Erwartungen           private Properties objProp = null;
private FileTextParserZZZ objParser = null;

	public ClientConfigUpdaterZZZ(IKernelZZZ objKernel, ClientMainZZZ objClient, ConfigChooserOVPN objConfigChooser, ClientConfigMapperOVPN objConfigMapper, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objClient, objConfigChooser, objConfigMapper, saFlagControl);
	}
		
	//############# Getter / Setter
	public ClientMainZZZ getClientObject() {
		return (ClientMainZZZ) this.getMainObject();
	}
	public void setClientObject(ClientMainZZZ objClient) {
		this.setMainObject(objClient);
	}
	public ClientConfigMapperOVPN getConfigMapperObject() {
		return (ClientConfigMapperOVPN) this.getClientObject().getConfigMapperObject();
	}
	public void setConfigMapperObject(ClientConfigMapperOVPN objConfigMapper) {
		this.getClientObject().setConfigMapperObject(objConfigMapper);
	}
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getClientObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getClientObject().setConfigChooserObject(objConfigChooser);
	}
}
