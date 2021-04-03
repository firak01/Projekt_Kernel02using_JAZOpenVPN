package use.openvpn.server;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.AbstractConfigMapper4TemplateOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.IMainOVPN;

public class ServerConfigMapper4TemplateOVPN extends AbstractConfigMapper4TemplateOVPN{	
	public ServerConfigMapper4TemplateOVPN(IKernelZZZ objKernel, ServerMainZZZ objServerMain, File fileConfigTemplateOvpn) throws ExceptionZZZ {
		super(objKernel, (IMainOVPN) objServerMain, fileConfigTemplateOvpn);		
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
			//Der Server horcht, er muss nix zu einer Gegenstelle aufbauen: hmReturn.put("remote", "remote %ip%");
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
			//Der Server horcht, er muss nix zu einer Gegenstelle aufbauen, darum nicht: hmConfig.put("remote", "^remote ");
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
			HashMap<String,String> hmPattern = this.getConfigPattern();
			if(this.getFlag("useProxy")==true){	
				String sProxyLine = (String)hmPattern.get("http-proxy");
				if(sProxyLine!=null){
					stemp = StringZZZ.replace(sProxyLine, "%proxy%", this.getServerMainObject().getApplicationObject().getProxyHost());
					stemp = StringZZZ.replace(stemp, "%port%", this.getServerMainObject().getApplicationObject().getProxyPort());
					hmReturn.put("http-proxy", stemp);
					}
				
				String sProxyTimeoutLine = (String) hmPattern.get("http-proxy-timeout");
				if(sProxyTimeoutLine!=null){
					stemp = StringZZZ.replace(sProxyTimeoutLine, "%timeout%", "10");
					hmReturn.put("http-proxy-timeout", stemp);
				}	
			}//END "useProxy"
					
//          Den Sever zeihnet aus, dass er nur "horcht" und nicht aktiv mit einer Gegenstelle die Verbindung aufbaut
//			String sRemoteLine = (String)hmPattern.get("remote");
//			if(sRemoteLine!=null){
//				stemp = StringZZZ.replace(sRemoteLine, "%ip%", this.getServerMainObject().getApplicationObject().getIpRemote());					
//				objReturn.put("remote", stemp);
//			}	
			
			
			//20200123: Der Name der Certifier Dateien entspricht dem Namen der Maschine.
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
						sFileCertifier = sHostname.toUpperCase() + "_SERVER.crt";
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
						sFileKey = sHostname.toUpperCase() + "_SERVER.key";
					}					
				}
			}else { //###################################################
				sCertifierLine = (String)hmPattern.get("cert");				
				if(sCertifierLine!=null) {					
					sFileCertifier = "PAUL_HINDENBURG_SERVER.crt";													
				}
				
				//+++++++++++++
				sKeyLine = (String)hmPattern.get("key");
				if(sKeyLine!=null) {
					sFileKey = "PAUL_HINDENBURG_SERVER.key";									
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
			stemp = StringZZZ.replace(sIfconfigLine, "%vpnipremote%", this.getServerMainObject().getApplicationObject().getVpnIpRemote());
			stemp = StringZZZ.replace(stemp, "%vpniplocal%", this.getServerMainObject().getApplicationObject().getVpnIpLocal());
			hmReturn.put("ifconfig", stemp);
			
			//2020126: Einträge für dev-node, damit hier auch keine Fehlkonfiguration im OVPNTemplate möglich ist.
			String sDevNodeLine = (String)hmPattern.get("dev-node");
			stemp = StringZZZ.replace(sDevNodeLine, "%tapadapterusedlocal%", this.getServerMainObject().getApplicationObject().getTapAdapterUsed());
			hmReturn.put("dev-node", stemp);
		}//END main:
		return hmReturn;
	}
	
	//###### GETTER / SETTER
	public ServerMainZZZ getServerMainObject() {
		return (ServerMainZZZ) this.getMainObject();
	}
	public void setServerMainObject(ServerMainZZZ objServerMain) {
		this.setMainObject((IMainOVPN)objServerMain);
	}
	
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.getServerMainObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.getServerMainObject().setConfigChooserObject(objConfigChooser);
	}
	
	public ServerApplicationOVPN getApplicationObject() {
		return (ServerApplicationOVPN) this.getServerMainObject().getApplicationObject();
	}
	public void setApplicationObject(ServerApplicationOVPN objApplication) {
		this.getServerMainObject().setApplicationObject(objApplication);
	}
	
	
}
