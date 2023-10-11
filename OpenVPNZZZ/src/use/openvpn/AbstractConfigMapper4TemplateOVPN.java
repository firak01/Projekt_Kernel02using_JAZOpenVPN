package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.server.ServerConfigMapper4TemplateOVPN;

public abstract class AbstractConfigMapper4TemplateOVPN extends AbstractConfigMapperOVPN implements IConfigMapper4TemplateOVPN{
	private IMainOVPN objMain = null;
	private File fileTemplateUsedOvpn = null;
	
		
	public AbstractConfigMapper4TemplateOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateOvpn) throws ExceptionZZZ {
		super(objKernel, objMain);			
		this.setFileTemplateOvpnUsed(fileTemplateOvpn);
	}
		
	/**@param sAlias Aliaswert der OVPN Konfiguration, 
	 *               als Schlüssel für eine HashMap, mit der man an den regulären Ausdruck kommt, 
	 *               der für das Finden der Zeile in der OVPN Konfigurationsdatei verwendet wird.
	 * @return String-Wert des puren regulären Ausdrucks aus einer HashMap, in der Form liste(ConfigAusdruck) = "^" + saConfig[icount] + " ";  	
	 * @throws ExceptionZZZ, 
	 * javadoc created by: 0823, 05.07.2006 - 08:31:35
	 */
	public abstract String getConfigRegExp(String sConfiguration) throws ExceptionZZZ;
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public abstract HashMapIterableKeyZZZ<String, String> readTaskHashMap() throws ExceptionZZZ;
	
	//###### GETTER / SETTER
	@Override
	public File getFileTemplateOvpnUsed() {
		return this.fileTemplateUsedOvpn;
	}
	
	@Override
	public void setFileTemplateOvpnUsed(File fileTemplateOvpn) {
		this.fileTemplateUsedOvpn = fileTemplateOvpn;
	}
	
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
