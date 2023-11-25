package use.openvpn.server;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import use.openvpn.AbstractApplicationOVPN;
import use.openvpn.ApplicationCommonUtilOVPN;
import use.openvpn.IMainOVPN;

public class ServerApplicationOVPN extends AbstractApplicationOVPN {
	private static final long serialVersionUID = 5897449174489342306L;
	private String sURL = null;
	private String sIpURL = null;
	
	private String sIpRemote = null;
	
	public ServerApplicationOVPN(IKernelZZZ objKernel, ServerMainOVPN objServer) throws ExceptionZZZ {
		super(objKernel, (IMainOVPN) objServer);
		this.setServerObject(objServer);
	}
	
	
	
	//######################################################
	//### Getter / Setter
	public ServerMainOVPN getServerObject() {
		return (ServerMainOVPN) this.getMainObject();
	}
	public void setServerObject(ServerMainOVPN objServer) {
		this.setMainObject((IMainOVPN) objServer);
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
			sReturn = objKernel.getParameterByProgramAlias("OVPN","IP-ServerContext","URL2Read").getValue();		
		}//END main:
		return sReturn;
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
	
	
	public String getIpURL() throws ExceptionZZZ{
		if(this.sIpURL==null) {
			this.sIpURL = this.readIpURL();
		}
		return this.sIpURL;
	}
	public void setIpURL(String sIpURL) {
		this.sIpURL = sIpURL;
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
	
	
	
}
