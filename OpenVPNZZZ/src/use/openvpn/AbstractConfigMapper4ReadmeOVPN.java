package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4ReadmeOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4ReadmeOVPN{
	private File fileTemplateUsedReadme = null;
	
	public AbstractConfigMapper4ReadmeOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateReadme) {
		super(objKernel, objMain);
		this.setFileTemplateReadmeUsed(fileTemplateReadme);
	}
	
	@Override
	public File getFileTemplateReadmeUsed() {
		return this.fileTemplateUsedReadme;
	}
	
	@Override
	public void setFileTemplateReadmeUsed(File fileTemplateReadme) {
		this.fileTemplateUsedReadme = fileTemplateReadme;
	}
}
