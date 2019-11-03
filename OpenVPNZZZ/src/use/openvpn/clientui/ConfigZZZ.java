package use.openvpn.clientui;

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
		//20191031: Dieser Wert muss vom Programm verarbeitet/Übersetzt werden werden - wie ein ini-Datei Eintrag auch übersetzt würde.
		return "<z:Null/>";//Merke . oder Leerstring = src Verzeichnis
	}

	public String getConfigFileNameDefault() {
		return "ZKernelConfig_OVPNClient.ini";
	}

	public String getPatternStringDefault() {
		return "k:s:d:f:";
	}

	public String getSystemNumberDefault() {
		return "01";
	}
}