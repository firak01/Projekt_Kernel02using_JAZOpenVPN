package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import use.openvpn.server.ServerMainOVPN;

public class AbstractApplicationOVPN extends AbstractKernelUseObjectZZZ implements IApplicationOVPN{
	protected IMainOVPN objMain = null;
	
	protected String sProxyHost = null;
	protected String sProxyPort = null;	
	
	protected String sVpnPortRemote = null;
	protected String sVpnIpRemote = null;
	protected String sVpnIpRemoteEstablished = null; //die VpnIp mit der aktuell die Verbindung existiert (diese IP wird dann ggfs. auch mit ping ueberwacht)
	protected String sVpnIpLocal = null;
	protected String sIPLocal = null;
	protected String sIPIni = null;
	protected String sTapAdapterUsed = null;
	
	protected String sCertifierConfiguredFilename=null;	
	protected String sKeyConfiguredFilename=null;
	
	
	
	public AbstractApplicationOVPN(IKernelZZZ objKernel, IMainOVPN objMain) throws ExceptionZZZ {
		super(objKernel);
		this.setMainObject(objMain);
	}
	
	/**Read from the configuration file a proxy which might be necessary to use AND enables the proxy for this application.
	 * Remember: This proxy is used to read the url (containing the ip adress)
	 *                    AND
	 *                    The proxy is added to the open vpn configuration file(s) 
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 14:20:24
	 */
	public boolean readProxyEnabled() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			
		    //+++ Ggf. notwendige Proxy-Einstellung pr�fen.
			//Z.B. bei der itelligence bin ich hinter einem Proxy. Die auszulesende Seite ist aber im Web.
			IKernelConfigSectionEntryZZZ objEntryHost = objKernel.getParameterByProgramAlias("OVPN","ProgProxyHandler","ProxyHost");			
			if(objEntryHost.hasAnyValue()){		//Eine Proxy-Konfiguration ist nicht Pflicht
				this.sProxyHost = objEntryHost.getValue();	
				if(StringZZZ.isEmpty(this.sProxyHost)) {
					this.getMainObject().logProtocolString("Proxy host as empty configured.");
					break main;
				}
				
				
				IKernelConfigSectionEntryZZZ objEntryPort = objKernel.getParameterByProgramAlias("OVPN","ProgProxyHandler","ProxyPort");
				if(objEntryHost.hasAnyValue()){
					this.sProxyPort = objEntryPort.getValue();
					if(StringZZZ.isEmpty(this.sProxyPort)) {
						this.getMainObject().logProtocolString("Proxy port as empty configured.");
						break main;
					}
					
					//+++ Nun versuchen herauszufinden, ob der Proxy auch erreichbar ist und existiert. Nur nutzen, falls er existiert
					KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
					try{ //Hier soll nicht abgebrochen werden, wenn es nicht klappt. Lediglich ins Log soll etwas geschrieben werden.
						this.getMainObject().logProtocolString( "Trying to reach the proxy configured. '" + sProxyHost + " : " + sProxyPort +"'");									
						bReturn = objPing.ping(sProxyHost, sProxyPort);								
						this.getMainObject().logProtocolString("Configured proxy reached. " + sProxyHost + " : " + sProxyPort +"'");
										
					}catch(ExceptionZZZ ez){
						objKernel.getLogObject().WriteLineDate("Will not use the proxy configured, because: " + ez.getDetailAllLast());
						this.getMainObject().logProtocolString("Configured proxy unreachable. " + sProxyHost + " : " + sProxyPort +"'. No proxy will be enabled.");
					}	
				}else {
					this.getMainObject().logProtocolString("No proxy port configured.");
				}				
			}else{
				this.getMainObject().logProtocolString("No proxy host configured.");								
			}//END 	if(sProxyHost!=null && sProxyHost.equals("")==false){		//Eine Proxy-Konfiguration ist nicht Pflicht		
		}//END main
		this.setFlag("UseProxy", bReturn);
		return bReturn;
	}
	
	public String readVpnPortRemote() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgVPNCheck","VPNPort2Check").getValue();					
		}//END main:
		return sReturn;
	}
	
	public String readVpnIpRemote() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigValues","VpnIpRemote").getValue();					
		}//END main:
		return sReturn;
	}
	
	public String readVpnIpLocal() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigValues","VpnIpLocal").getValue();					
		}//END main:
		return sReturn;
	}
	
	public String readIpIni() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			////Z.B. [IP-ClientContext!02]
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","IP-ClientContext","IPExternal").getValue();					
		}//END main:
		return sReturn;
	}
	
	
	/** Read the used local IP.
	 * @return
	 * @throws ExceptionZZZ
	 */
	public String readIpLocal() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			sReturn = EnvironmentZZZ.getHostIp();
		}//END main
		this.sIPLocal = sReturn;
		return sReturn;
	}
	
	public String readTapAdapterUsed() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigValues","TapAdapterUsedLocal").getValue();					
		}//END main:
		return sReturn;
	}
	
	/**Read from the configured certifier filename.
	 * Remark: If this is empty a default filename containing the Hostname will be expected. E.g. HANNIBALDEV04VM_CLIENT.crt
	 * The file is to be expected in the OpenVPN Configuration directory: E.g. C:\Programme\OpenVPN\config
	 * @throws ExceptionZZZ, 
	 *
	 * @return String
	 */
	public String readCertifierConfiguredFilename() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigValues","CertifierFilename").getValue();		
		}//END main:
		return sReturn;
	}
	
	/**Read from the configured key filename.
	 * Remark: If this is empty a default filename containing the Hostname will be expected. E.g. HANNIBALDEV04VM_CLIENT.key
	 * The file is to be expected in the OpenVPN Configuration directory: E.g. C:\Programme\OpenVPN\config
	 * @throws ExceptionZZZ, 
	 *
	 * @return String
	 */
	public String readKeyConfiguredFilename() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigValues","KeyFilename").getValue();		
		}//END main:		
		return sReturn;
	}
	
	//###### GETTER  / SETTER
		public IMainOVPN getMainObject() {
			return this.objMain;
		}
		public void setMainObject(IMainOVPN objMain) {
			this.objMain = objMain;
		}
		
		public String getProxyHost(){
			//Werte werden in readProxyEnabled gesetzt
			return this.sProxyHost;
		}
		public void setProxyHost(String sProxyHost) {
			this.sProxyHost = sProxyHost;
		}
		public String getProxyPort(){
			//Werte werden in readProxyEnabled gesetzt
			return this.sProxyPort;
		}
		
		public String getVpnPortRemote() throws ExceptionZZZ {
			if(this.sVpnPortRemote==null) {
				this.sVpnPortRemote = this.readVpnPortRemote(); 
			}
			return this.sVpnPortRemote;
		}
		public void setVpnPortRemote(String sVpnPortRemote) {
			this.sVpnPortRemote = sVpnPortRemote;
		}
		
		public String getVpnIpRemote() throws ExceptionZZZ {
			if(this.sVpnIpRemote==null) {
				this.sVpnIpRemote = this.readVpnIpRemote(); 
			}
			return this.sVpnIpRemote;
		}
		public void setVpnIpRemote(String sVpnIpRemote) {
			this.sVpnIpRemote = sVpnIpRemote;
		}
				
		public String getVpnIpRemoteEstablished() throws ExceptionZZZ {			
			return this.sVpnIpRemoteEstablished;
		}
		public void setVpnIpRemoteEstablished(String sVpnIpRemote) {
			this.sVpnIpRemoteEstablished = sVpnIpRemote;
		}
		
		public String getVpnIpLocal() throws ExceptionZZZ {
			if(this.sVpnIpLocal==null) {
				this.sVpnIpLocal = this.readVpnIpLocal(); 
			}
			return this.sVpnIpLocal;
		}
		public void setVpnIpLocal(String sVpnIpLocal) {
			this.sVpnIpLocal = sVpnIpLocal;
		}
		
		public String getIpIni() throws ExceptionZZZ{
			if(StringZZZ.isEmpty(this.sIPIni)) {
				this.sIPIni = this.readIpIni();
			}
			return this.sIPIni;
		}
		public void setIpIni(String sIPini) {
			this.sIPIni=sIPini;
		}
		
		public String getIpLocal() throws ExceptionZZZ{
			if(this.sIPLocal==null) {
				this.sIPLocal = this.readIpLocal();
			}
			return this.sIPLocal;
		}
		public void setIpLocal(String sIPLocal) {
			this.sIPLocal = sIPLocal;
		}		
		
		public String getTapAdapterUsed() throws ExceptionZZZ {
			if(this.sTapAdapterUsed==null) {
				this.sTapAdapterUsed = this.readTapAdapterUsed(); 
			}
			return this.sTapAdapterUsed;
		}
		public void setTapAdapterUsed(String sTapAdapterUsed) {
			this.sTapAdapterUsed = sTapAdapterUsed;
		}
		
		public String getCertifierConfiguredFilename() throws ExceptionZZZ{
			if(this.sCertifierConfiguredFilename==null) {
				this.sCertifierConfiguredFilename = this.readCertifierConfiguredFilename();
			}
			return this.sCertifierConfiguredFilename;
		}
		
		public String getKeyConfiguredFilename() throws ExceptionZZZ{
			if(this.sKeyConfiguredFilename==null) {
				this.sKeyConfiguredFilename = this.readKeyConfiguredFilename();
			}
			return this.sKeyConfiguredFilename;
		}
}
