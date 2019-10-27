package debug.ovpn.server;

import use.openvpn.client.ClientMainZZZ;
import use.openvpn.server.ServerMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;

public class DebugServerMainZZZ {

	/** TODO What the method does.
	 * @return void
	 * @param args 
	 *  
	 * lindhaueradmin; 13.07.2006 10:05:07
	 */
	public static void main(String[] args) {
		main:{
			KernelZZZ objKernel = null;		
			try {
				objKernel = new KernelZZZ("OVPN", "01", "",  "ZKernelConfig_OVPNServer.ini", (String)null);
				objKernel.getLogObject().WriteLineDate("TEST");
				 
				ServerMainZZZ objConfig = new ServerMainZZZ(objKernel, null);
				objConfig.start();
			
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

}//END class
