package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public abstract class AbstractConfigMapperOVPN extends KernelUseObjectZZZ implements IConfigMapperOVPN, IMainUserOVPN{
	private IMainOVPN objMain = null;
		
	public AbstractConfigMapperOVPN(IKernelZZZ objKernel, IMainOVPN objMain) throws ExceptionZZZ {
		super(objKernel);
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
	
	/**TODO R�ckagebe der einzutragenden Zeile pro configurations Eintrag ALS MUSTER. TODO GOON: R�ckgabe in Form einer HashMap
	 * TODO GOON: Hashmap hat folgende Struktur. Liste(sConfigurationEntry)=sConfiigurationEntry + ' ' + die Werte ....
	 * @param sAlias
	 * @param sIP
	 * @param sProxyHost
	 * @param sProxyPort
	 * @param iProxyTimeout
	 * @return, 
	 *
	 * @return String[]
	 * 
	 * Die Ersetzung der Musterplatzhalter passiert in ClientMainZZZ.readTaskHashMap();
	 * Zudem muss ein RegEx Ausdruck bereitgestellt werden in ClientConfigUpdaterZZZ.getConfigRegExp();
	 *
	 * javadoc created by: 0823, 05.07.2006 - 08:34:38
	 */
	@Override
	public abstract HashMap<String,String> getConfigPattern() throws ExceptionZZZ;

}
