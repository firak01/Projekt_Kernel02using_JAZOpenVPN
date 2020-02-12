package use.openvpn;

public interface IMainOVPN {
	public void logStatusString(String sStatus);
	public void addStatusString(String sStatus);
	
	public IApplicationOVPN getApplicationObject();
	public void setApplicationObject(IApplicationOVPN objApplication);
	
	public ConfigChooserOVPN getConfigChooserObject();
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser);
	
	public IConfigMapperOVPN getConfigMapperObject();
	public void setConfigMapperObject(IConfigMapperOVPN objConfigMapper);
	
}
