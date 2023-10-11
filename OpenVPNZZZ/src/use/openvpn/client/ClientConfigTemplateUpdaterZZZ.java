package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import custom.zUtil.io.FileZZZ;
import use.openvpn.AbstractConfigTemplateUpdaterOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.IConfigMapperOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileTextParserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;

public class ClientConfigTemplateUpdaterZZZ extends AbstractConfigTemplateUpdaterOVPN {

//private FileInputStream filein=null;
// Die Properties erf�llen nicht meine Erwartungen           private Properties objProp = null;
private FileTextParserZZZ objParser = null;

	public ClientConfigTemplateUpdaterZZZ(IKernelZZZ objKernel, ClientMainOVPN objClient, ConfigChooserOVPN objConfigChooser, ClientConfigMapper4TemplateOVPN objConfigMapper, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objClient, objConfigChooser, objConfigMapper, saFlagControl);
	}
		
	//############# Getter / Setter
	public ClientMainOVPN getClientObject() {
		return (ClientMainOVPN) this.getMainObject();
	}
	public void setClientObject(ClientMainOVPN objClient) {
		this.setMainObject(objClient);
	}
	public ClientConfigMapper4TemplateOVPN getConfigMapperObject() {
		return (ClientConfigMapper4TemplateOVPN) this.getClientObject().getConfigMapperObject();
	}
	public void setConfigMapperObject(ClientConfigMapper4TemplateOVPN objConfigMapper) {
		this.getClientObject().setConfigMapperObject(objConfigMapper);
	}
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getClientObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getClientObject().setConfigChooserObject(objConfigChooser);
	}
}
