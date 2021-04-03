package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4ServerClientConfigOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4ServerClientConfigOVPN{
	private File fileTemplateUsed = null;
	
	public AbstractConfigMapper4ServerClientConfigOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplate) throws ExceptionZZZ {
		super(objKernel, objMain);
		this.setFileTemplateServerClientConfigUsed(fileTemplate);
	}
	
	@Override
	public File getFileTemplateServerClientConfigUsed() {
		return this.fileTemplateUsed;
	}
	
	@Override
	public void setFileTemplateServerClientConfigUsed(File fileTemplate) {
		this.fileTemplateUsed = fileTemplate;
	}
}
