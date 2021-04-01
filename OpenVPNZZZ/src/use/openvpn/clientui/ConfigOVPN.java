package use.openvpn.clientui;

import static java.lang.System.out;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashMapCaseInsensitiveZZZ;
import basic.zBasic.util.datatype.calling.ReferenceZZZ;
import basic.zKernel.KernelConfigZZZ;
import basic.zKernel.config.KernelConfigEntryUtilZZZ;
import custom.zKernel.file.ini.FileIniZZZ;

public class ConfigOVPN  extends KernelConfigZZZ{
	public ConfigOVPN() throws ExceptionZZZ{
		super();
	}
	public ConfigOVPN(String[] saArg) throws ExceptionZZZ {
		super(saArg); 
	} 
	public ConfigOVPN(String[] saArg, String[] saFlagControl) throws ExceptionZZZ {
		super(saArg, saFlagControl); 
	} 
	public ConfigOVPN(String[] saArg, String sFlagControl) throws ExceptionZZZ {
		super(saArg, sFlagControl); 
	}
	
	
	public String getApplicationKeyDefault() {
		return "OVPN"; 
	}

	public String getConfigDirectoryNameDefault() {
		return ".";
	}
	
	public String getConfigFileNameDefault() {
		return "ZKernelConfig_OVPNClient.ini";
	}

	public String getSystemNumberDefault() {
		return "01";
	}
}