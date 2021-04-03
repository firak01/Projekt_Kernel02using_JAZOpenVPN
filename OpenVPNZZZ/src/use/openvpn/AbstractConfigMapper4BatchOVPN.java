package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4BatchOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4BatchOVPN{
	private File fileTemplateUsedBatch = null;
	private File fileConfigUsedOvpn = null;
	
	public AbstractConfigMapper4BatchOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateBatch, File fileConfigOvpn) throws ExceptionZZZ {
		super(objKernel, objMain);
		this.setFileTemplateBatchUsed(fileTemplateBatch);
		this.setFileConfigOvpnUsed(fileConfigOvpn);
	}
	
	@Override
	public File getFileTemplateBatchUsed() {
		return this.fileTemplateUsedBatch;
	}
	
	@Override
	public void setFileTemplateBatchUsed(File fileTemplateBatch) {
		this.fileTemplateUsedBatch = fileTemplateBatch;
	}
	
	@Override
	public File getFileConfigOvpnUsed() {
		return this.fileConfigUsedOvpn;
	}
	
	@Override
	public void setFileConfigOvpnUsed(File fileConfigOvpn) {
		this.fileConfigUsedOvpn = fileConfigOvpn;
	}
	
}
