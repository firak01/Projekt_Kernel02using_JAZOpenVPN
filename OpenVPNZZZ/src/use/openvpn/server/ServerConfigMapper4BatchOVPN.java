package use.openvpn.server;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import use.openvpn.AbstractConfigMapper4BatchOVPN;
import use.openvpn.AbstractConfigMapper4TemplateOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainZZZ;

public class ServerConfigMapper4BatchOVPN extends AbstractConfigMapper4BatchOVPN{	
	public ServerConfigMapper4BatchOVPN(IKernelZZZ objKernel, ServerMainZZZ objServerMain) {
		super(objKernel, objServerMain);		
	}

	@Override
	public HashMap getConfigPattern() {
		HashMap hmReturn = new HashMap();
		main:{
			check:{		
			}
		
			//Die %xyz% Einträge sollen dann ersetzt werden.
			//Das Beispielergebnis: C:\Programme\OpenVPN\bin\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn
			hmReturn.put("line001", "%exeovpn% --pause-exit --config %templateovpn%");			
		}//END main
		return hmReturn;
	}
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public HashMap readTaskHashMap(File objFileTemplate) throws ExceptionZZZ{
		HashMap objReturn=new HashMap();
		main:{		
			if(objFileTemplate==null) {
				ExceptionZZZ ez = new ExceptionZZZ("Template file", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
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
			String sFileTemplateOvpn = objFileTemplate.getName();
			
			String stemp;
			HashMap<String,String> hmPattern = this.getConfigPattern();
			Set<String> setKey = hmPattern.keySet();
			for(String sKey : setKey) {
				
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
