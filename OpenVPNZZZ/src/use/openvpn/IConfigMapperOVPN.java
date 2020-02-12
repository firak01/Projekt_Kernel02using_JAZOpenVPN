package use.openvpn;

import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;

public interface IConfigMapperOVPN {
	public HashMap getConfigPattern();
	public String getConfigRegExp(String sConfiguration) throws ExceptionZZZ;
	public HashMap readTaskHashMap() throws ExceptionZZZ;
}
