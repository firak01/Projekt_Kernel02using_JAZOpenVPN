package use.openvpn;

import basic.zBasic.ExceptionZZZ;

public interface IMainOVPN extends IConfigMapper4TemplateUserOVPN,IApplicationUserOVPN,IMainConstantOVPN{
	public void logStatusString(String sStatus) throws ExceptionZZZ;
	public void addStatusString(String sStatus);
	public String getJarFilePathUsed();
	
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
	
	
	
//	public IConfigMapper4TemplateOVPN getConfigMapperObject();
//	public void setConfigMapperObject(IConfigMapper4TemplateOVPN objConfigMapper);
//	public IConfigMapperOVPN getConfigMapperObject();
//	public void setConfigMapperObject(IConfigMapperOVPN objConfigMapper);
}
