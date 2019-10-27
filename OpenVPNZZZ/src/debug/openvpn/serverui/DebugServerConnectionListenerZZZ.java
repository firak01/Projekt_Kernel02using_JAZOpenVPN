package debug.openvpn.serverui;

import java.io.File;

import use.openvpn.ConfigStarterZZZ;
import use.openvpn.server.ServerMainZZZ;
import use.openvpn.serverui.ServerConnectionListenerZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;

public class DebugServerConnectionListenerZZZ {
	/** TODO What the method does.
	 * @return void
	 * @param args 
	 *  
	 * lindhaueradmin; 13.07.2006 10:05:07
	 */
	public static void main(String[] args) {
		main:{
			KernelZZZ objKernel = null;		
			ConfigStarterZZZ objConfig = null;
			ServerConnectionListenerZZZ objListener = null;
			File objFile = null;
			try {
				objKernel = new KernelZZZ("OVPN", "01", "",  "ZKernelConfig_OVPNServer.ini", (String)null);
				objKernel.getLogObject().WriteLineDate("TEST");
				 
				//!!! Damit die lokale VPN-IP-Adresse aufgelöst werden kann, muss der entsprehcende Netzwerkadapter "verbunden" sein.
				objFile = new File("C:\\Programme\\OpenVPN\\config\\client_TCP_443.ovpn");
				//objFile = new File("C:\\Programme\\OpenVPN\\config\\serverVPN1_TCP_443.ovpn");				
				objConfig = new ConfigStarterZZZ(objKernel, objFile, null);
								
				objListener = new ServerConnectionListenerZZZ(objKernel, objConfig, null);
				objListener.start();
			
			} catch (ExceptionZZZ ez) {
				if(objKernel!=null){
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog!=null){
						objLog.WriteLineDate(ez.getDetailAllLast());
					}else{
						ez.printStackTrace();
					}				
				}else{
					ez.printStackTrace();
				}
			}//END Catch
			}//END main:
			System.out.println("finished everything");
	}//END main()
}
