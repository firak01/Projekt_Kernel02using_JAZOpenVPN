package use.openvpn.client;

import java.io.File;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import use.openvpn.ConfigFileOVPN;


/**This class changes the content of a configuration file.
 * It extends ConfigFileZZZ
 * @author 0823
 *
 */
public class ClientConfigFileZZZ extends ConfigFileOVPN {
	private String sRemoteIP=null;
	
	public ClientConfigFileZZZ(IKernelZZZ objKernel, File objFile, String[] saFlagControl) throws ExceptionZZZ {
		super(objKernel, objFile, saFlagControl);
	}
	
	public boolean isRemoteReachable() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			String sRemoteIP = this.getVpnIpRemote();
			String sRemotePort = this.getVpnPortRemote(); 
			if(sRemoteIP!=null && sRemotePort != null){
				KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
				try{				
					bReturn = objPing.ping(sRemoteIP, sRemotePort);
				}catch(ExceptionZZZ ez){
					//Keine Konsequenzz. Ich erwarte ja einen Fehler.
					//MERKE: Die Exception, welche die Methode throwen k�nnte kommt vom Konstruktor des KernelPingHost-Objekts.
					bReturn = false;
				}
			}
		}//END main:
		return bReturn;
	}
	
	
	public String readRemoteIp(){
		String sReturn = null;
		main:{
			sReturn = this.getProperties().getProperty("remote");			
		}
		return sReturn;
	}
	
	
	//### Getter / Setter	
	public String getRemoteIp(){
		if(this.sRemoteIP==null){
			this.sRemoteIP=this.readRemoteIp();			
		}
		return this.sRemoteIP;
	}

}
