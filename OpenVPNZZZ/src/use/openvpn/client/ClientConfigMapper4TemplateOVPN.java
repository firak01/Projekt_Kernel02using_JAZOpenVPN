package use.openvpn.client;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import use.openvpn.AbstractConfigMapper4TemplateOVPN;
import use.openvpn.ConfigChooserOVPN;

public class ClientConfigMapper4TemplateOVPN extends AbstractConfigMapper4TemplateOVPN{	
	public ClientConfigMapper4TemplateOVPN(IKernelZZZ objKernel, ClientMainOVPN objClientMain, File fileTemplateOvpn) throws ExceptionZZZ {
		super(objKernel, objClientMain, fileTemplateOvpn);		
	}
	
	/**TODO R�ckagebe der einzutragenden Zeile pro configurations Eintrag ALS MUSTER. TODO GOON: R�ckgabe in Form einer HashMap
	 * TODO GOON: Hashmap hat folgende Struktur. Liste(sConfigurationEntry)=sConfiigurationEntry + ' ' + die Werte ....
	 * @param sAlias
	 * @param sIP
	 * @param sProxyHost
	 * @param sProxyPort
	 * @param iProxyTimeout
	 * @return, 
	 *
	 * @return String[]
	 * 
	 * Die Ersetzung der Musterplatzhalter passiert in ClientMainZZZ.readTaskHashMap();
	 * Zudem muss ein RegEx Ausdruck bereitgestellt werden in ClientConfigUpdaterZZZ.getConfigRegExp();
	 *
	 * javadoc created by: 0823, 05.07.2006 - 08:34:38
	 */
	public HashMap<String,String> getConfigPattern(){
		HashMap<String,String> hmReturn = new HashMap<String,String>();
		main:{
			check:{		
			}		
		
			//Die %xyz% Einträge sollen dann ersetzt werden.
			hmReturn.put("remote", "remote %ip%");
			hmReturn.put("http-proxy", "http-proxy %proxy% %port%");
			hmReturn.put("http-proxy-timeout", "http-proxy-timeout %timeout%");
			
			//20200123: Nun die verwendeten Key-Namen erstetzen
			hmReturn.put("cert", "cert %filecertifier%");
			hmReturn.put("key", "key %filekey%");
			
			//20200126: Die verwendte lokale und remote IP Adresse ersetzen
			hmReturn.put("ifconfig", "ifconfig %vpniplocal% %vpnipremote%");
			
			//202020126: Den verwendeten lokalen TAP Adapter setzen.
			hmReturn.put("dev-node", "dev-node %tapadapterusedlocal%");
		}//END main
		return hmReturn;
	}
	
	/**@param sAlias Aliaswert der OVPN Konfiguration, 
	 *               als Schlüssel für eine HashMap, mit der man an den regulären Ausdruck kommt, 
	 *               der für das Finden der Zeile in der OVPN Konfigurationsdatei verwendet wird.
	 * @return String-Wert des puren regulären Ausdrucks aus einer HashMap, in der Form liste(ConfigAusdruck) = "^" + saConfig[icount] + " ";  
	 * @throws ExceptionZZZ, 
	 *
	 * @return String[]
	 *
	 * javadoc created by: 0823, 05.07.2006 - 08:31:35
	 */
	public String getConfigRegExp(String sConfiguration) throws ExceptionZZZ{
		String sReturn = null;
		main:{		
			check:{
				if(sConfiguration==null)break main;
				if(sConfiguration.equals(""))break main;				
			}
		
			//Hashmap erstellen.
			//TODOGOON: Die Konfiguration der Hashmap an eine zentrale Stelle auslagern. Die Hashmap in einer Variablen speichern, so dass sie nur 1x erstellt werden muss.
			HashMap hmConfig = new HashMap();
			hmConfig.put("remote", "^remote ");
			hmConfig.put("http-proxy", "^http-proxy ");
			hmConfig.put("http-proxy-timeout", "^http-proxy-timeout ");
			
			//20200123: Key und certifier Datei mit dem Namen der Hostmaschine
			hmConfig.put("cert", "^cert ");
			hmConfig.put("key", "^key ");
		
			//20200126: Die verwendete lokale und remote IP Adresse ersetzen
			hmConfig.put("ifconfig", "^ifconfig ");
			
			//202020126: Den verwendeten lokalen TAP Adapter setzen.
			hmConfig.put("dev-node", "^dev-node ");
			
		//Hashmap auslesen
		sReturn = (String)hmConfig.get(sConfiguration);
		
		}//END main
		return sReturn;
		
	}
	
	/** Ersetze die in .getConfigPattern() definierten Platzhalter
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 23.01.2020, 10:07:16
	 */
	public HashMapIterableKeyZZZ<String, String> readTaskHashMap() throws ExceptionZZZ{
		HashMapIterableKeyZZZ<String, String> hmReturn=new HashMapIterableKeyZZZ<String, String>();
		main:{		
			String stemp;
			HashMap hmPattern = this.getConfigPattern();
			if(this.getFlag("useProxy")==true){	
				String sProxyLine = (String)hmPattern.get("http-proxy");
				if(sProxyLine!=null){
					stemp = StringZZZ.replace(sProxyLine, "%proxy%", this.getClientMainObject().getApplicationObject().getProxyHost());
					stemp = StringZZZ.replace(stemp, "%port%", this.getClientMainObject().getApplicationObject().getProxyPort());
					hmReturn.put("http-proxy", stemp);
					}
				
				String sProxyTimeoutLine = (String) hmPattern.get("http-proxy-timeout");
				if(sProxyTimeoutLine!=null){
					stemp = StringZZZ.replace(sProxyTimeoutLine, "%timeout%", "10");
					hmReturn.put("http-proxy-timeout", stemp);
				}	
			}//END "useProxy"
					
			String sRemoteLine = (String)hmPattern.get("remote");
			if(sRemoteLine!=null){
				stemp = StringZZZ.replace(sRemoteLine, "%ip%", ((ClientApplicationOVPN)this.getClientMainObject().getApplicationObject()).getIpRemote());					
				hmReturn.put("remote", stemp);
			}	
			
			
			//20200123: Der Name der Certifier Dateien entspricht dem CN-Namen der Maschine.
			//          Der CN-Name der Maschine entspricht dem Namen der Maschine + _CLIENT.
			//Beispiele:
			//cert C:\\Programme\\OpenVPN\\config\\HANNIBALDEV04VM_CLIENT.crt
			//key C:\\Programme\\OpenVPN\\config\\HANNIBALDEV04VM_CLIENT.key
			String sKeyLine=null; String sFileKey=null;
			String sCertifierLine=null; String sFileCertifier=null;
			if(!this.getFlag("useCertifierKeyGlobal")) {
				sCertifierLine = (String)hmPattern.get("cert");				
				if(sCertifierLine!=null) {
					//Hole den Wert aus der ini-Datei
					sFileCertifier = this.getApplicationObject().getCertifierConfiguredFilename();
									   					
					//Alternativ: Nimm einen Standardnamen
					if(StringZZZ.isEmpty(sFileCertifier)) {
						String sHostname = EnvironmentZZZ.getHostName();				
						sFileCertifier = sHostname.toUpperCase() + "_CLIENT.crt";														
					}
				}
				//+++++++++++++
				sKeyLine = (String)hmPattern.get("key");
				if(sKeyLine!=null) {
					//Hole den Wert aus der ini-Datei
					sFileKey = this.getApplicationObject().getKeyConfiguredFilename();
					
					//Alternativ: Nimm einen Standardnamen
					if(StringZZZ.isEmpty(sFileKey)) {
						String sHostname = EnvironmentZZZ.getHostName();
						sFileKey = sHostname.toUpperCase() + "_CLIENT.key";
					}
				}
			}else { //###################################################
				sCertifierLine = (String)hmPattern.get("cert");				
				if(sCertifierLine!=null) {					
					sFileCertifier = "PAUL_HINDENBURG_CLIENT.crt";													
				}
				
				//+++++++++++++
				sKeyLine = (String)hmPattern.get("key");
				if(sKeyLine!=null) {
					sFileKey = "PAUL_HINDENBURG_CLIENT.key";									
				}
			}
			if(sCertifierLine!=null) {
				stemp = StringZZZ.replace(sCertifierLine, "%filecertifier%", this.getConfigChooserObject().getDirectoryConfig()+ File.separator + sFileCertifier);
				stemp = StringZZZ.replace(stemp, "\\", "\\\\");//Die Verdoppelung der Backslashe wird von OVPN gewünscht, wg. Shell-Verwwendung
				hmReturn.put("cert", stemp);
			}
			if(sKeyLine!=null) {
				stemp = StringZZZ.replace(sKeyLine, "%filekey%", this.getConfigChooserObject().getDirectoryConfig()+ File.separator + sFileKey);
				stemp = StringZZZ.replace(stemp, "\\", "\\\\");//Die Verdoppelung der Backslashe wird von OVPN gewünscht, wg. Shell-Verwwendung
				hmReturn.put("key", stemp);
			}
			
			//20200126: Einträge für ifconfig, damit hier auch keine Fehlkonfiguration im OVPNTemplate möglich ist.
			String sIfconfigLine = (String)hmPattern.get("ifconfig");
			stemp = StringZZZ.replace(sIfconfigLine, "%vpnipremote%", this.getClientMainObject().getApplicationObject().getVpnIpRemote());
			stemp = StringZZZ.replace(stemp, "%vpniplocal%", ((ClientApplicationOVPN)this.getClientMainObject().getApplicationObject()).getVpnIpLocal());
			hmReturn.put("ifconfig", stemp);
			
			//2020126: Einträge für dev-node, damit hier auch keine Fehlkonfiguration im OVPNTemplate möglich ist.
			String sDevNodeLine = (String)hmPattern.get("dev-node");
			stemp = StringZZZ.replace(sDevNodeLine, "%tapadapterusedlocal%", this.getClientMainObject().getApplicationObject().getTapAdapterUsed());
			hmReturn.put("dev-node", stemp);
		}//END main:
		return hmReturn;
	}
	
	//###### GETTER / SETTER
	public ClientMainOVPN getClientMainObject() {
		return (ClientMainOVPN) this.getMainObject();
	}
	public void setClientMainObject(ClientMainOVPN objClientMain) {
		this.setMainObject(objClientMain);
	}
	
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getClientMainObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getClientMainObject().setConfigChooserObject(objConfigChooser);
	}
	
	public ClientApplicationOVPN getApplicationObject() {
		return (ClientApplicationOVPN) this.getClientMainObject().getApplicationObject();
	}
	public void setApplicationObject(ClientApplicationOVPN objApplication) {
		this.getClientMainObject().setApplicationObject(objApplication);
	}
	
	
}
