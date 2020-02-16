package use.openvpn;

public interface IMainOVPN {
	public void logStatusString(String sStatus);
	public void addStatusString(String sStatus);
	
	public IApplicationOVPN getApplicationObject();
	public void setApplicationObject(IApplicationOVPN objApplication);
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
	
	public IConfigMapper4TemplateOVPN getConfigMapperObject();
	public void setConfigMapperObject(IConfigMapper4TemplateOVPN objConfigMapper);
	
}
