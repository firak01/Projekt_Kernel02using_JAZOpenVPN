package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;

public interface IMainOVPN extends IProgramRunnableZZZ, IConfigMapper4TemplateUserOVPN,IApplicationUserOVPN,IMainConstantOVPN{
	public void logProtocol(String sMessage) throws ExceptionZZZ; //Nicht der Status, sondern eine Message fuers Log, etc.
	public void addProtocolString(String sMessage);
	public String getJarFilePathUsed();
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
}
