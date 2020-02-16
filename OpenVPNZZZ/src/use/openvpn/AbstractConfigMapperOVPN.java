package use.openvpn;

import java.util.HashMap;

import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public abstract class AbstractConfigMapperOVPN extends KernelUseObjectZZZ implements IConfigMapperOVPN, IMainUserOVPN{
	private IMainOVPN objMain = null;

	public AbstractConfigMapperOVPN(IKernelZZZ objKernel, IMainOVPN objMain) {
		this.setMainObject(objMain);
	}
		
	@Override
	public IMainOVPN getMainObject() {
		return this.objMain;
	}

	@Override
	public void setMainObject(IMainOVPN objMain) {
		this.objMain = objMain;
	}

	@Override
	public abstract HashMap getConfigPattern();

}
