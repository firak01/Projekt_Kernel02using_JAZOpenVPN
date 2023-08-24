package use.openvpn.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.abstractList.HashMapZZZ;
import basic.zBasic.util.abstractList.HashtableIndexedZZZ;
import basic.zBasic.util.abstractList.SetZZZ;
import basic.zBasic.util.abstractList.VectorExtendedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;

import use.openvpn.AbstractConfigMapper4BatchOVPN;
import use.openvpn.AbstractConfigMapper4ReadmeOVPN;
import use.openvpn.AbstractConfigMapper4ServerClientConfigOVPN;
import use.openvpn.AbstractConfigMapper4TemplateOVPN;
import use.openvpn.ConfigFileTemplateBatchOVPN;
import use.openvpn.ConfigFileTemplateReadmeOVPN;
import use.openvpn.ConfigFileTemplateServerClientConfigOVPN;
import use.openvpn.IApplicationOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainOVPN;

public class ServerConfigMapper4ServerClientConfigOVPN extends AbstractConfigMapper4ServerClientConfigOVPN{	
	public ServerConfigMapper4ServerClientConfigOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplateReadme) throws ExceptionZZZ {
		super(objKernel, objMain, fileTemplateReadme);		
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
			File fileTemplateServerClientConfig = this.getFileTemplateServerClientConfigUsed();
			
			//Lies das Template ein, jede Zeile 1 Eintrag in der HashMap (ist damit anders als beim OVPN-Template, bei dem alle Zeile gegen einen RegEx-Ausdruck geprüft werden.)
			IKernelZZZ objKernel = this.getKernelObject();		
			ConfigFileTemplateServerClientConfigOVPN objBatchReader = new ConfigFileTemplateServerClientConfigOVPN(objKernel, fileTemplateServerClientConfig, null);
			hmReturn = objBatchReader.getLinesAsHashMap_StringString();
		}//END main
		return hmReturn;
	}
	
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public HashMapIterableKeyZZZ<String,String> readTaskHashMap() throws ExceptionZZZ{
		HashMapIterableKeyZZZ<String,String> hmReturn=new HashMapIterableKeyZZZ<String,String>();
		main:{							
			String stemp;
			HashMap<String,String> hmPattern = this.getConfigPattern();//Merke: Das scheint noch nicht sortiert zu sein, warum ? Eine normale HashMap ist nie sortiert....
						
			//Die Sortierung ist im Set nicht sichergestellt. Darum explizit sortieren und die Kernel-HashMap (iterierbar) verwenden
			//Nur so bleiben die Codezeilen in der passenden Reihenfolge.
			HashMapIterableKeyZZZ<String,Object> hmPatternSorted = HashMapZZZ.sortByKeyAsInteger_StringString(hmPattern);			
			for(String sKey : hmPatternSorted) {
				String sLine = (String) hmPattern.get(sKey);	
				
				//In jeder Zeile der Datei nun die definierten Platzhalter ersetzen.
				//Falls überhaupt nix zu ersetzen ist, die ganze Zeile übernehmen 
				stemp = sLine;
				
				//Die verwendeten Variablen müssen aber noch errechnet werden.
				//z.B. für die Zeile: ifconfig-push %VPN-IP_CLIENT% %VPN-IP_SERVER%
				IApplicationOVPN objApplication = this.getMainObject().getApplicationObject();
				
				String sVpnIpClient = objApplication.getVpnIpRemote(); //"vpnipClient woher nehmen";//mainObject ist vorhanden!!!
				String sVpnIpServer = objApplication.getVpnIpLocal(); //"vpnipServer woher nehmen";
				
				stemp = StringZZZ.replace(stemp, "%VPN-IP_CLIENT%", sVpnIpClient);				
				stemp = StringZZZ.replace(stemp, "%VPN-IP_SERVER%", sVpnIpServer);
												
				hmReturn.put(sKey, stemp);
			}
			
		}//END main:
		return hmReturn;
	}
	
	//###### GETTER / SETTER
		public ServerMainOVPN getServerMainObject() {
			return (ServerMainOVPN) this.getMainObject();
		}
		public void setServerMainObject(ServerMainOVPN objServerMain) {
			this.setMainObject((IMainOVPN)objServerMain);
		}
		
}
