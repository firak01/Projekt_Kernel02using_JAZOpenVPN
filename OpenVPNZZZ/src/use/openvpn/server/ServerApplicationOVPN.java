package use.openvpn.server;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import use.openvpn.client.ClientMainZZZ;

public class ServerApplicationOVPN extends KernelUseObjectZZZ{
	private ServerMainZZZ objServer = null;
	
	private String sProxyHost = null;
	private String sProxyPort = null;	
	private String sVpnIpRemote = null;
	private String sVpnIpLocal = null;
	private String sTapAdapterUsed = null;
	
	public ServerApplicationOVPN(IKernelZZZ objKernel, ServerMainZZZ objServer) {
		super(objKernel);
		this.setServerObject(objServer);
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
			this.sProxyHost = objKernel.getParameterByProgramAlias("OVPN","ProgProxyHandler","ProxyHost").getValue();
			if(sProxyHost!=null && sProxyHost.trim().equals("")==false){		//Eine Proxy-Konfiguration ist nicht Pflicht		
				sProxyPort = objKernel.getParameterByProgramAlias("OVPN","ProgProxyHandler","ProxyPort").getValue();
				
				//+++ Nun versuchen herauszufinden, ob der Proxy auch erreichbar ist und existiert. Nur nutzen, falls er existiert
				KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
				try{ //Hier soll nicht abgebrochen werden, wenn es nicht klappt. Lediglich ins Log soll etwas geschrieben werden.
					this.getServerObject().logStatusString( "Trying to reach the proxy configured. '" + sProxyHost + " : " + sProxyPort +"'");									
					bReturn = objPing.ping(sProxyHost, sProxyPort);								
					this.getServerObject().logStatusString("Configured proxy reached. " + sProxyHost + " : " + sProxyPort +"'");
									
				}catch(ExceptionZZZ ez){
					objKernel.getLogObject().WriteLineDate("Will not use the proxy configured, because: " + ez.getDetailAllLast());
					this.getServerObject().logStatusString("Configured proxy unreachable. " + sProxyHost + " : " + sProxyPort +"'. No proxy will be enabled.");
				}	
			}else{
				this.getServerObject().logStatusString("No proxy configured.");								
			}//END 	if(sProxyHost!=null && sProxyHost.equals("")==false){		//Eine Proxy-Konfiguration ist nicht Pflicht		
		}//END main
		this.setFlag("UseProxy", bReturn);
		return bReturn;
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
	
	//######################################################
		//### Getter / Setter
		public ServerMainZZZ getServerObject() {
			return this.objServer;
		}
		public void setServerObject(ServerMainZZZ objServer) {
			this.objServer = objServer;
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
		
		public String getVpnIpRemote() throws ExceptionZZZ {
			if(this.sVpnIpRemote==null) {
				this.sVpnIpRemote = this.readVpnIpRemote(); 
			}
			return this.sVpnIpRemote;
		}
		public void setVpnIpRemote(String sVpnIpRemote) {
			this.sVpnIpRemote = sVpnIpRemote;
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
		public String getTapAdapterUsed() throws ExceptionZZZ {
			if(this.sTapAdapterUsed==null) {
				this.sTapAdapterUsed = this.readTapAdapterUsed(); 
			}
			return this.sTapAdapterUsed;
		}
		public void setTapAdapterUsed(String sTapAdapterUsed) {
			this.sTapAdapterUsed = sTapAdapterUsed;
		}
		
}
