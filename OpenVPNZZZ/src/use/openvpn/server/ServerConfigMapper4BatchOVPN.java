package use.openvpn.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashtableIndexedZZZ;
import basic.zBasic.util.abstractList.SetZZZ;
import basic.zBasic.util.abstractList.VectorExtendedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;

import use.openvpn.AbstractConfigMapper4BatchOVPN;
import use.openvpn.AbstractConfigMapper4TemplateOVPN;
import use.openvpn.ConfigFileTemplateBatchOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainZZZ;

public class ServerConfigMapper4BatchOVPN extends AbstractConfigMapper4BatchOVPN{	
	public ServerConfigMapper4BatchOVPN(IKernelZZZ objKernel, ServerMainZZZ objServerMain, File fileTemplateBatch) {
		super(objKernel, objServerMain, fileTemplateBatch);		
	}

	@Override
	public HashMap<String,String> getConfigPattern() throws ExceptionZZZ {
		HashMap<String,String> hmReturn = new HashMap<String,String>();
		main:{			
			//Die %xyz% Einträge sollen dann ersetzt werden.
			//hmReturn.put("line001", "%exeovpn% --pause-exit --log c:\\\\fglkernel\\\\kernellog\\\\ovpn.log --config %templateovpn% > c:\\fglkernel\\kernellog\\ovpnStarter.log 2>&1");
		
			//Das Beispielergebnis: C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn
			//Merke: 2>&1 soll bewirken, dass sowohl die Standardausgabe als auch die Fehler in die gleiche Datei kommen
		    
		
			//Hole das Template für die Batch-Datei
			File fileTemplateBatch = this.getFileConfigTemplateBatchUsed();
					
			//Lies das Template ein, jede Zeile 1 Eintrag in der HashMap (ist damit anders als beim OVPN-Template, bei dem alle Zeile gegen einen RegEx-Ausdruck geprüft werden.)
			IKernelZZZ objKernel = this.getKernelObject();		
			ConfigFileTemplateBatchOVPN objBatchReader = new ConfigFileTemplateBatchOVPN(objKernel, fileTemplateBatch, null);
			hmReturn = objBatchReader.getLinesAsHashMap_StringString();
		}//END main
		return hmReturn;
	}
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public HashMap readTaskHashMap(File fileConfigTemplateOvpnIn) throws ExceptionZZZ{
		HashMap objReturn=new HashMap();
		main:{					
			if(fileConfigTemplateOvpnIn!=null) {
				this.setFileConfigTemplateOvpnUsed(fileConfigTemplateOvpnIn);
			}
			File fileConfigTemplateOvpn=this.getFileConfigTemplateOvpnUsed();
			if(fileConfigTemplateOvpn==null) {
				ExceptionZZZ ez = new ExceptionZZZ("OVPN Template file", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			
			
			//Merke: In der Batch Datei muss der Pfad zur neu erstellten Template-Datei stehen.
			/*
			 * REM so nicht C:\\Programme\\OpenVPN\\bin\\openvpnserv.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn
REM C:\\Programme\\OpenVPN\\bin\\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn
REM C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config ..\config\serverVPN1_TCP_443.ovpn
REM C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config testserverVPN1_TCP_443.ovpn
REM C:\Programme\OpenVPN\bin\openvpn.exe --help
REM C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config ..\\config\\serverVPN1_TCP_443.ovpn
C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn
pause
			 */
			
			
			File objDirectoryExeOvpn = this.getServerMainObject().getConfigChooserObject().getDirectoryExe();
			String sDirectoryExeOvpn = objDirectoryExeOvpn.getAbsolutePath();
			String sFileExeOvpn = sDirectoryExeOvpn+File.separator+"openvpn.exe";
			
			File objDirectoryTemplateOvpn = this.getServerMainObject().getConfigChooserObject().getDirectoryConfig();
			String sDirectoryTemplateOvpn = objDirectoryTemplateOvpn.getAbsolutePath();
			
			//Nur eine konkrete, für den Start der Batch verwendete Konfiguration hier behandeln
			String sFileTemplateOvpn = fileConfigTemplateOvpn.getName();
			
			String stemp;
			HashMap<String,String> hmPattern = this.getConfigPattern();
			Set<String> setKey = hmPattern.keySet();
			
			//Aber: Die Sortierung ist im Set nicht sichergestellt. Darum explizit sortieren.
			//List<Integer>numbersList = (List<Integer>) SetZZZ.sortAsInteger(setKey);						
			List<String>numbersList = (List<String>) SetZZZ.sortAsInteger(setKey);
			for(String sKey : numbersList) {
				String sLine = hmPattern.get(sKey);				
				stemp = StringZZZ.replace(sLine, "%exeovpn%", sFileExeOvpn);
				
				stemp = StringZZZ.replace(stemp, "%templateovpn%", sDirectoryTemplateOvpn + File.separator + sFileTemplateOvpn);
				
				
				objReturn.put(sKey, stemp);
			}
			
		}//END main:
		return objReturn;
	}
	
	//###### GETTER / SETTER
		public ServerMainZZZ getServerMainObject() {
			return (ServerMainZZZ) this.getMainObject();
		}
		public void setServerMainObject(ServerMainZZZ objServerMain) {
			this.setMainObject((IMainOVPN)objServerMain);
		}
		
}
