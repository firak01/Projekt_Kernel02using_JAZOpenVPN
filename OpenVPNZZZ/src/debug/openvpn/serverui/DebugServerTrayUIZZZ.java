package debug.openvpn.serverui;

import java.io.File;

import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.serverui.ServerTrayUIOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;

public class DebugServerTrayUIZZZ {

	/**TODO Describe what the method does
	 * @param args, 
	 *
	 * @return void
	 *
	 * javadoc created by: 0823, 11.08.2006 - 05:51:51
	 */
	public static void main(String[] args) {
		main:{
			KernelZZZ objKernel = null;		
			try {
				objKernel = new KernelZZZ("OVPN", "01", "",  "ZKernelConfig_OVPNServer_test.ini", (String)null);
				objKernel.getLogObject().WriteLineDate("TEST");
				 
				ServerMainOVPN objServer = new ServerMainOVPN(objKernel, null);//Das Backendobjekt, das pro Konfigurationsfile eine OVPN.exe Datei als Process startet.
				
				//Das TrayIcon ohne, das ein Backend-Objekt aufgebaut worden ist.
				ServerTrayUIOVPN objTray = new ServerTrayUIOVPN(objKernel, objServer, null);
				
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//+++ Hier wird das "Listen" simuliert.
				File objFile = new File("C:\\Programme\\OpenVPN\\config\\Template serverVPN1_TCP_443.ovpn_4_test");	
				ServerConfigStarterOVPN objConfig = new ServerConfigStarterOVPN(objKernel, objServer, objFile, null);
				
				objServer.addProcessStarter(objConfig);                                      //eben diese Datei
				//objTray.setServerBackendObject(objServer);                                //Damit wird ein Teil der .listen() Methode simuliert.
								
//				ServerMonitorRunnerZZZ objMonitor = new ServerMonitorRunnerZZZ(objKernel, objTray, objServer, null);
//				objTray.setMonitorObject(objMonitor);
//				Thread objThreadMonitor = new Thread(objMonitor);
//				objThreadMonitor.start();
			
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
	}

}
