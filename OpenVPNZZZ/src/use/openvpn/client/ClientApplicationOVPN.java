package use.openvpn.client;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.machine.EnvironmentZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import use.openvpn.AbstractApplicationOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.IMainUserOVPN;

public class ClientApplicationOVPN  extends AbstractApplicationOVPN{
	private String sURL = null;	
	private String sIPRemote = null;

	//Ggf. ist dieser Wert aussagekräftiger als der Versuch über sIPVPN
	private String sPortRemoteScanned = null;
	private String sPortVpnScanned = null;

	private String sIPVPN = null;
	
	
	public ClientApplicationOVPN(IKernelZZZ objKernel, ClientMainZZZ objClient) {
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
		this.sURL = sReturn;
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
	public String readIpRemote() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			
			String[] satemp = {"UseStream"};
			KernelReaderURLZZZ objReaderURL = new KernelReaderURLZZZ(objKernel, sURL,satemp, "");
			this.readProxyEnabled();
			if(this.getFlag("useProxy")==true) objReaderURL.setProxyEnabled(this.getProxyHost(), this.getProxyPort());
			
			
			//+++ Nachdem nun ggf. der Proxy aktiviert wurde, die Web-Seite versuchen auszulesen				
			//+++ Den IP-Wert holen aus dem HTML-Code der konfigurierten URL
			KernelReaderPageZZZ objReaderPage = objReaderURL.getReaderPage();
			KernelReaderHtmlZZZ objReaderHTML = objReaderPage.getReaderHTML();
			 
			//Nun alle input-Elemente holen und nach dem Namen "IPNr" suchen.
			TagTypeInputZZZ objTagTypeInput = new TagTypeInputZZZ(objKernel);			
			TagInputZZZ objTag = (TagInputZZZ) objReaderHTML.readTagFirstZZZ(objTagTypeInput, "IPNr");
			sReturn = objTag.readValue();
		}//END main
		this.sIPRemote = sReturn;
		return sReturn;
	}
	
	//######################################################
	//### Getter / Setter
	public ClientMainZZZ getClientObject() {
		return (ClientMainZZZ) this.getMainObject();
	}
	public void setClientObject(ClientMainZZZ objClient) {
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
			
	public String getIpRemote() throws ExceptionZZZ{
		if(this.sIPRemote==null) {
			this.sIPRemote = this.readIpRemote();
		}
		return this.sIPRemote;
	}
	public void setIpRemote(String sIPRemote) {
		this.sIPRemote = sIPRemote;
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
	
	//Achtung: Im Gegensatz zu sIPRemote ist das f�r jede Konfiguration verschieden. Darf also nur dann gesetzt werden, wenn die Verbindung erfolgreich hergestellt wurde.
	public String getVpnIpEstablished(){
		return this.sIPVPN;
	}
	public void setVpnIpEstablished(String sIPVPN) {
		this.sIPVPN = sIPVPN;
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
