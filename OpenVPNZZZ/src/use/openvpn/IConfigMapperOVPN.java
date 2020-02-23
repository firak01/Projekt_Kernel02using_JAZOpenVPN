package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;

public interface IConfigMapperOVPN {
	public HashMap<String,String> getConfigPattern() throws ExceptionZZZ;
	public void setFileConfigTemplateOvpnUsed(File fileTemplateOvpn);
	public File getFileConfigTemplateOvpnUsed();
	public void setFileConfigTemplateBatchUsed(File fileTemplateBatch);
	public File getFileConfigTemplateBatchUsed();
}
