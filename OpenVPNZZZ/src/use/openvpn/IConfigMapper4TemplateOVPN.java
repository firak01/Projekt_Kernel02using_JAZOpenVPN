package use.openvpn;

import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;

public interface IConfigMapper4TemplateOVPN extends IConfigMapperOVPN{	
	public String getConfigRegExp(String sConfiguration) throws ExceptionZZZ;
	public HashMap readTaskHashMap() throws ExceptionZZZ;
}
