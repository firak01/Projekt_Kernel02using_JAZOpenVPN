package use.openvpn.clientui;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.AbstractKernelConfigZZZ;

public class ConfigOVPN<T>  extends AbstractKernelConfigZZZ<T>{
	private static final long serialVersionUID = 4513436714382846739L;
	
	//#################################################
	//Merke: Die Konstanten sind meist nicht final, damit sie von der konkreten Konfiguration
	//       ueberschrieben werden koennen.
	//       Final sind die fuer den Kernel selbst wichtige Konstanten
	
	//#####################################################################
	//####### Reflektion zum Gesamtprojekt
	static String sPROJECT_DIRECTORY = "Project_Kernel02using_JAZOpenVPN";
	static String sPROJECT_NAME = "OpenVPNZZZ";
	
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
	
	@Override
	public String getProjectName() {
		return ConfigOVPN.sPROJECT_NAME;
	}
	@Override
	public String getProjectDirectory() {
		return ConfigOVPN.sPROJECT_DIRECTORY;
	}
}