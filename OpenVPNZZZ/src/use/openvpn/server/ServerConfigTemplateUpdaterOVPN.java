package use.openvpn.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import custom.zUtil.io.FileZZZ;
import use.openvpn.AbstractConfigTemplateUpdaterOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientConfigMapper4TemplateOVPN;
import use.openvpn.client.ClientMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileTextParserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ServerConfigTemplateUpdaterOVPN extends AbstractConfigTemplateUpdaterOVPN {

//private FileInputStream filein=null;
// Die Properties erf�llen nicht meine Erwartungen           private Properties objProp = null;
private FileTextParserZZZ objParser = null;

	public ServerConfigTemplateUpdaterOVPN(IKernelZZZ objKernel, ServerMainOVPN objServer, ConfigChooserOVPN objConfigChooser, ServerConfigMapper4TemplateOVPN objConfigMapper, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, (IMainOVPN) objServer, objConfigChooser, objConfigMapper, saFlagControl);
	}	
	
	//############# Getter / Setter
	public ServerMainOVPN getServerObject() {
		return (ServerMainOVPN) this.getMainObject();
	}
	public void setServerObject(ServerMainOVPN objServer) {
		this.setMainObject((IMainOVPN)objServer);
	}
	public ServerConfigMapper4TemplateOVPN getConfigMapperObject() {
		return (ServerConfigMapper4TemplateOVPN) this.getServerObject().getConfigMapperObject();
	}
	public void setConfigMapperObject(ServerConfigMapper4TemplateOVPN objConfigMapper) {
		this.getServerObject().setConfigMapperObject(objConfigMapper);
	}
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getServerObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getServerObject().setConfigChooserObject(objConfigChooser);
	}	
}
