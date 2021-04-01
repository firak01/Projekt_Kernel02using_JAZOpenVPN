package use.openvpn.serverui;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelConfigZZZ;

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
		return "ZKernelConfig_OVPNServer.ini";
	}	

	public String getSystemNumberDefault() {
		return "02";
	}
}
