package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;

public interface IMainOVPN extends IConfigMapper4TemplateUserOVPN,IApplicationUserOVPN,IMainConstantOVPN{
	public void logProtocolString(String sMessage) throws ExceptionZZZ; //Nicht der Status, sondern eine Message fuers Log, etc.
	public void addProtocolString(String sMessage);
	public String getJarFilePathUsed();
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
}
