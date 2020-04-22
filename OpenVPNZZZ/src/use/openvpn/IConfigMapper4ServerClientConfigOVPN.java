package use.openvpn;

import java.io.File;
import java.util.HashMap;

public interface IConfigMapper4ServerClientConfigOVPN extends IConfigMapperOVPN{
	public void setFileTemplateServerClientConfigUsed(File fileTemplateServerClientConfig);
	public File getFileTemplateServerClientConfigUsed();
}
