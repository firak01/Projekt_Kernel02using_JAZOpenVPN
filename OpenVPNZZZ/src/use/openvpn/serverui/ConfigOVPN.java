package use.openvpn.serverui;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.AbstractKernelConfigZZZ;
import basic.zKernel.net.client.ConfigHtmlTableHandlerZZZ;

public class ConfigOVPN  extends AbstractKernelConfigZZZ{
	private static String sPROJECT_NAME = "OpenVPNZZZ";
	private static String sPROJECT_PATH = "Project_Kernel02using_JAZOpenVPN";
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
	
	@Override
	public String getProjectName() {
		return ConfigOVPN.sPROJECT_NAME;
	}
	@Override
	public String getProjectDirectory() {
		return ConfigOVPN.sPROJECT_PATH;
	}

}
