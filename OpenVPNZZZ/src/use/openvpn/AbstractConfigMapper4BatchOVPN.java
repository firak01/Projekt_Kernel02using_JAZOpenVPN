package use.openvpn;

import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;

public abstract class AbstractConfigMapper4BatchOVPN  extends AbstractConfigMapperOVPN implements IConfigMapper4BatchOVPN{

	public AbstractConfigMapper4BatchOVPN(IKernelZZZ objKernel, IMainOVPN objMain) {
		super(objKernel, objMain);		
	}

	@Override
	public abstract HashMap getConfigPattern();
}
