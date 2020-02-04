package use.openvpn.server;

import java.io.File;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.ConfigChooserZZZ;

public class ServerConfigMapperOVPN extends KernelUseObjectZZZ{
	private ServerMainZZZ objServerMain = null;
		
	public ServerConfigMapperOVPN(IKernelZZZ objKernel, ServerMainZZZ objClientMain) {
		super(objKernel);
		this.setServerMainObject(objServerMain);		
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
	public static HashMap getConfigPattern(){
		HashMap hmReturn = new HashMap();
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
	
	/**TODO R�ckgabe des regul�ren Ausdrucks. TODOGOON: Dies sollte in Form einer HashMap passieren !!!
	 *  TODO GOON Hashmap in der Form liste(ConfigAusdruck) = "^" + saConfig[icount] + " ";
	 * @param sAlias
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return String[]
	 *
	 * javadoc created by: 0823, 05.07.2006 - 08:31:35
	 */
	public static String getConfigRegExp(String sConfiguration) throws ExceptionZZZ{
		String sReturn = null;
		main:{		
			check:{
				if(sConfiguration==null)break main;
				if(sConfiguration.equals(""))break main;				
			}
		
			//Hashmap erstellen. TODO GOON Dies an eine Stelle auslagern, so dass es nur einmal gemacht werden braucht.
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
	public HashMap readTaskHashMap() throws ExceptionZZZ{
		HashMap objReturn=new HashMap();
		main:{		
			String stemp;
			HashMap hmPattern = ServerConfigMapperOVPN.getConfigPattern();
			if(this.getFlag("useProxy")==true){	
				String sProxyLine = (String)hmPattern.get("http-proxy");
				if(sProxyLine!=null){
					stemp = StringZZZ.replace(sProxyLine, "%proxy%", this.getServerMainObject().getApplicationObject().getProxyHost());
					stemp = StringZZZ.replace(stemp, "%port%", this.getServerMainObject().getApplicationObject().getProxyPort());
					objReturn.put("http-proxy", stemp);
					}
				
				String sProxyTimeoutLine = (String) hmPattern.get("http-proxy-timeout");
				if(sProxyTimeoutLine!=null){
					stemp = StringZZZ.replace(sProxyTimeoutLine, "%timeout%", "10");
					objReturn.put("http-proxy-timeout", stemp);
				}	
			}//END "useProxy"
					
			String sRemoteLine = (String)hmPattern.get("remote");
			if(sRemoteLine!=null){
				stemp = StringZZZ.replace(sRemoteLine, "%ip%", this.getServerMainObject().getApplicationObject().getIpRemote());					
				objReturn.put("remote", stemp);
			}	
			
			
			//20200123: Der Name der Certifier Dateien entspricht dem Namen der Maschine.
			//Beispiele:
			//cert C:\\Programme\\OpenVPN\\config\\HANNIBALDEV04VM_CLIENT.crt
			//key C:\\Programme\\OpenVPN\\config\\HANNIBALDEV04VM_CLIENT.key
			String sKeyLine=null; String sFileKey=null;
			String sCertifierLine=null; String sFileCertifier=null;
			if(!this.getFlag("useCertifierKeyGlobal")) {
				String sHostname = EnvironmentZZZ.getHostName();
				sCertifierLine = (String)hmPattern.get("cert");				
				if(sCertifierLine!=null) {					
					sFileCertifier = sHostname.toUpperCase() + "_CLIENT.crt";									
				}
				
				//+++++++++++++
				sKeyLine = (String)hmPattern.get("key");
				if(sKeyLine!=null) {
					sFileKey = sHostname.toUpperCase() + "_CLIENT.key";
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
				objReturn.put("cert", stemp);
			}
			if(sKeyLine!=null) {
				stemp = StringZZZ.replace(sKeyLine, "%filekey%", this.getConfigChooserObject().getDirectoryConfig()+ File.separator + sFileKey);
				stemp = StringZZZ.replace(stemp, "\\", "\\\\");//Die Verdoppelung der Backslashe wird von OVPN gewünscht, wg. Shell-Verwwendung
				objReturn.put("key", stemp);
			}
			
			//20200126: Einträge für ifconfig, damit hier auch keine Fehlkonfiguration im OVPNTemplate möglich ist.
			String sIfconfigLine = (String)hmPattern.get("ifconfig");
			stemp = StringZZZ.replace(sIfconfigLine, "%vpnipremote%", this.getServerMainObject().getApplicationObject().getVpnIpRemote());
			stemp = StringZZZ.replace(stemp, "%vpniplocal%", this.getServerMainObject().getApplicationObject().getVpnIpLocal());
			objReturn.put("ifconfig", stemp);
			
			//2020126: Einträge für dev-node, damit hier auch keine Fehlkonfiguration im OVPNTemplate möglich ist.
			String sDevNodeLine = (String)hmPattern.get("dev-node");
			stemp = StringZZZ.replace(sDevNodeLine, "%tapadapterusedlocal%", this.getServerMainObject().getApplicationObject().getTapAdapterUsed());
			objReturn.put("dev-node", stemp);
		}//END main:
		return objReturn;
	}
	
	//###### GETTER / SETTER
	public ServerMainZZZ getServerMainObject() {
		return this.objServerMain;
	}
	public void setServerMainObject(ServerMainZZZ objServerMain) {
		this.objServerMain = objServerMain;
	}
	
	public ConfigChooserZZZ getConfigChooserObject() {
		return this.getServerMainObject().getConfigChooserObject();
	}
	public void setConfigChooserObject(ConfigChooserZZZ objConfigChooser) {
		this.getServerMainObject().setConfigChooserObject(objConfigChooser);
	}
	
	public ServerApplicationOVPN getApplicationObject() {
		return this.getServerMainObject().getApplicationObject();
	}
	public void setApplicationObject(ServerApplicationOVPN objApplication) {
		this.getServerMainObject().setApplicationObject(objApplication);
	}
	
	
}
