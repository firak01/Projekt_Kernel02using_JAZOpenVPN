package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;

public interface IMainOVPN extends IStatusLocalUserZZZ, IConfigMapper4TemplateUserOVPN,IApplicationUserOVPN,IMainConstantOVPN{
	public void logMessageString(String sMessage) throws ExceptionZZZ; //Nicht der Status, sondern eine Message fuers Log, etc.
	public void addMessageString(String sMessage);
	public String getJarFilePathUsed();
	
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
}
