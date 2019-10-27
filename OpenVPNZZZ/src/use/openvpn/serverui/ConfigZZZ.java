package use.openvpn.serverui;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelConfigZZZ;

public class ConfigZZZ  extends KernelConfigZZZ{
	public ConfigZZZ() throws ExceptionZZZ{
		super();
	}
	public ConfigZZZ(String[] saArg) throws ExceptionZZZ {
		super(saArg); 
	} 
	
	public String getApplicationKeyDefault() {
		return "OVPN"; 
	}

	public String getConfigDirectoryNameDefault() {
		return ".";
	}

	public String getConfigFileNameDefault() {
		return "ZKernelConfig_OVPNServer.ini";
	}

	public String getPatternStringDefault() {
		return "k:s:d:f:";
	}

	public String getSystemNumberDefault() {
		return "01";
	}
}
