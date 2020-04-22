package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4ReadmeOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4ReadmeOVPN{
	private File fileTemplateUsed = null;
	
	public AbstractConfigMapper4ReadmeOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplate) {
		super(objKernel, objMain);
		this.setFileTemplateReadmeUsed(fileTemplate);
	}
	

	
	@Override
	public File getFileTemplateReadmeUsed() {
		return this.fileTemplateUsed;
	}
	
	@Override
	public void setFileTemplateReadmeUsed(File fileTemplate) {
		this.fileTemplateUsed = fileTemplate;
	}
}
