package use.openvpn.client;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import use.openvpn.AbstractApplicationOVPN;
import use.openvpn.ApplicationCommonUtilOVPN;
import use.openvpn.IMainOVPN;

public class ClientApplicationOVPN  extends AbstractApplicationOVPN{
	private static final long serialVersionUID = 9137062917343733927L;
	private String sURL = null;	
	private String sIpURL = null;
	
	private String sIpRemote = null;

	//Ggf. ist dieser Wert aussagekräftiger als der Versuch über sIPVPN
	private String sPortRemoteScanned = null;
	private String sPortVpnScanned = null;
		
	public ClientApplicationOVPN(IKernelZZZ objKernel, ClientMainOVPN objClient) throws ExceptionZZZ {
		super(objKernel, objClient);		
	}
	
	
	
	/**Reads a port from the configuration-file. Default: Port 80.
	 * This port is used to check the connection. 
	 * @throws ExceptionZZZ, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:05:05
	 */
	public String readVpnPort2Check() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgVPNCheck","VPNPort2Check").getValue();
			if(StringZZZ.isEmpty(sReturn)) sReturn = KernelPingHostZZZ.sPORT2CHECK;			
		}//END main:
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
	
	/**Read from the configuration file the URL where the dynamic ip was written to.
	 * @throws ExceptionZZZ, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 11.07.2006 - 14:19:23
	 */
	public String readURL2Parse() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgIPReader","URL2Read").getValue();		
		}//END main:
		return sReturn;
	}
	
	
	/**Reads the dynamic IP from a URL (uses a html-parser therefore).
	 * Checks the necessarity of enabling a proxy and will enable the proxy.
	 * The proxy has to be configured in the kernel-configuration-file.
	* @return String, the IP found.
	* @throws ExceptionZZZ 
	* 
	* lindhaueradmin; 13.07.2006 09:12:43
	 */
	public String readIpURL() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			boolean bUseProxy = this.getFlag("useProxy");
			if(!bUseProxy) {	
				this.readProxyEnabled();
				sReturn = ApplicationCommonUtilOVPN.readIpURL(this.getKernelObject(), sURL);
			}else {			
				sReturn = ApplicationCommonUtilOVPN.readIpURL(this.getKernelObject(), sURL, this.getProxyHost(), this.getProxyPort());
			}
		}//end main:
		return sReturn;
	}
	
	/** Hier die Möglichkeit andere Quellen als nur die URL zu definiern
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 25.04.2023, 07:38:26
	 */
	public String readIpRemote() throws ExceptionZZZ{
		String sReturn = null;
		main:{						
			String sIpUsed = null;
			String sIpIni = this.getIpIni();
			if(StringZZZ.isEmpty(sIpIni)) {
				String sIpUrl = this.getIpURL();				
				sIpUsed = sIpUrl;
			}else {
				sIpUsed = sIpIni;
			}
			sReturn = sIpUsed;
		}//END main
		return sReturn;
	}
	
	//######################################################
	//### Getter / Setter
	public ClientMainOVPN getClientObject() {
		return (ClientMainOVPN) this.getMainObject();
	}
	public void setClientObject(ClientMainOVPN objClient) {
		this.setMainObject((IMainOVPN) objClient);
	}		
		
	public String getURL2Parse() throws ExceptionZZZ{
		if(this.sURL==null) {
			this.sURL = this.readURL2Parse();
		}
		return this.sURL;
	}
	public void setURL2Parse(String sURL) {
		this.sURL = sURL;
	}
	
	public String getIpURL() throws ExceptionZZZ{
		if(this.sIpURL==null) {
			this.sIpURL = this.readIpURL();
		}
		return this.sIpURL;
	}
	public void setIpURL(String sIpURL) {
		this.sIpURL = sIpURL;
	}
			
	public String getIpRemote() throws ExceptionZZZ{
		if(this.sIpRemote==null) {
			this.sIpRemote = this.readIpRemote();
		}
		return this.sIpRemote;
	}
	public void setIpRemote(String sIpRemote) {
		this.sIpRemote = sIpRemote;
	}
	
	/**Der Versuch anzugeben, �ber welchen Port die VPN-Verbindung erfolgreich war.
	 * @return String
	 *
	 * javadoc created by: 0823, 11.07.2006 - 17:29:48
	 */
	public String getRemotePortScanned(){
		return this.sPortRemoteScanned;
	}
	public void setRemotePortScanned(String sPortRemoteScanned) {
		this.sPortRemoteScanned = sPortRemoteScanned;
	}
		
	/**This is a string filled by a port-scanner, after the connection was established.
	 * This string is read out by the fronteend ui - class to set the status.
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 08:58:49
	 */
	public String getVpnPortScanned(){
		return this.sPortVpnScanned;
	}
	public void setVpnPortScanned(String sPortVpnScanned) {
		this.sPortVpnScanned = sPortVpnScanned;
	}
	
	/*STEHEN LASSEN: DIE PROBLEMATIK IST, DAS NICHT NACHVOLLZIEHBAR IST, �BER WELCHEN PORT DIE VPN-VERBINDUNG HERGESTELLT WURDE 
	 * Zumindest nicht PER PING-BEFEHL !!!
	 
	public String getVpnPortEstablished(){
		return this.sPortVPN;
	}
	*/
	
}
