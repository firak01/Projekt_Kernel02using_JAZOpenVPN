package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.server.ServerConfigMapper4TemplateOVPN;

public abstract class AbstractConfigMapper4TemplateOVPN extends AbstractConfigMapperOVPN implements IConfigMapper4TemplateOVPN{
	private IMainOVPN objMain = null;
		
	public AbstractConfigMapper4TemplateOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateOvpn) {
		super(objKernel, objMain);			
		this.setFileConfigTemplateOvpnUsed(fileTemplateOvpn);
	}
		
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

	
	
	
}
