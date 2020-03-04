package use.openvpn;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;

public interface IConfigMapperOVPN {
	public HashMap<String,String> getConfigPattern() throws ExceptionZZZ;
		
	public HashMapIterableKeyZZZ<String,String> readTaskHashMap() throws ExceptionZZZ;
}
