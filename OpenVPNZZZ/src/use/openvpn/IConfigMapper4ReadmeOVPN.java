package use.openvpn;

import java.io.File;
import java.util.HashMap;

public interface IConfigMapper4ReadmeOVPN extends IConfigMapperOVPN{
	public void setFileTemplateReadmeUsed(File fileTemplateReadme);
	public File getFileTemplateReadmeUsed();
}
