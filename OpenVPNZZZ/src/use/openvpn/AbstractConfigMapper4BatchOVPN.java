package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4BatchOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4BatchOVPN{
	
	public AbstractConfigMapper4BatchOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateBatch) {
		super(objKernel, objMain);
		this.setFileConfigTemplateBatchUsed(fileTemplateBatch);
	}
}
