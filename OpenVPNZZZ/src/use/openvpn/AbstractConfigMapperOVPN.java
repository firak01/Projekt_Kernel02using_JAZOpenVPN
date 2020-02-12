package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.server.ServerConfigMapperOVPN;

public abstract class AbstractConfigMapperOVPN extends KernelUseObjectZZZ implements IConfigMapperOVPN, IMainUserOVPN{
	private IMainOVPN objMain = null;
		
	public AbstractConfigMapperOVPN(IKernelZZZ objKernel, IMainOVPN objMain) {
		super(objKernel);
		this.setMainObject(objMain);		
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
	public abstract HashMap getConfigPattern();
	
	
	/**TODO R�ckgabe des regul�ren Ausdrucks. TODOGOON: Dies sollte in Form einer HashMap passieren !!!
	 *  TODO GOON Hashmap in der Form liste(ConfigAusdruck) = "^" + saConfig[icount] + " ";
	 * @param sAlias
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return String[]
	 *
	 * javadoc created by: 0823, 05.07.2006 - 08:31:35
	 */
	public abstract String getConfigRegExp(String sConfiguration) throws ExceptionZZZ;
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public abstract HashMap readTaskHashMap() throws ExceptionZZZ;
	
	//###### GETTER / SETTER	
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getMainObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getMainObject().setConfigChooserObject(objConfigChooser);
	}
	
	public IApplicationOVPN getApplicationObject() {
		return this.getMainObject().getApplicationObject();
	}
	public void setApplicationObject(IApplicationOVPN objApplication) {
		this.getMainObject().setApplicationObject(objApplication);
	}

	@Override
	public IMainOVPN getMainObject() {
		return this.objMain;
	}

	@Override
	public void setMainObject(IMainOVPN objMain) {
		this.objMain = objMain;
	}
	
	
}
