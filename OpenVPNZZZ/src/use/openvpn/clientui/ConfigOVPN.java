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
		String sReturn=null;
		main:{
		//TODO 20191204: Diese ganze Methode muss in die abstrakte Klasse KernelConfigZZZ verschoben werden.	
		boolean bUseFormula = this.getFlagZ("useFormula");
			
		//20191031: Dieser Wert muss vom Programm verarbeitet/Übersetzt werden werden - wie ein ini-Datei Eintrag auch übersetzt würde.
		//return "<z:Null/>";//Merke '.' oder Leerstring '' = src Verzeichnis
		ReferenceZZZ<String> objsReturnValueExpressionSolved= new ReferenceZZZ<String>();
		ReferenceZZZ<String> objsReturnValueConverted= new ReferenceZZZ<String>();
		ReferenceZZZ<String> objsReturnValue= new ReferenceZZZ<String>();
		try {
			//TODO: Wenn das klappt eine statische Methode anbieten, bei der alle null-Parameter weggelassen werden können.
			int iConvertionType = KernelConfigEntryUtilZZZ.getValueExpressionSolvedAndConverted((FileIniZZZ) null, "<z:Null/>", bUseFormula, (HashMapCaseInsensitiveZZZ<String,String>) null, (String[]) null, objsReturnValueExpressionSolved, objsReturnValueConverted, objsReturnValue);
			sReturn = objsReturnValue.get();
		}catch(ExceptionZZZ ez){
			String sError = ez.getMessageLast();
			try {
				out.format("%s# Error thrown: %s%n", ReflectCodeZZZ.getPositionCurrent(),sError);
			} catch (ExceptionZZZ e) {
				String sErrorInt = ez.getMessageLast();
				out.format("%s# Error thrown: %s%n", "Holen der akutellen Position",sErrorInt);
				e.printStackTrace();
			}
		}
		}//end main
		return sReturn;
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