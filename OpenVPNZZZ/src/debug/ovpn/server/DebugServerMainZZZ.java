package debug.ovpn.server;

import use.openvpn.client.ClientMainOVPN;
import use.openvpn.server.ServerMainOVPN;
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
				objKernel.getLogObject().writeLineDate("TEST");
				 
				ServerMainOVPN objConfig = new ServerMainOVPN(objKernel, null);
				objConfig.startAsThread();
			
			} catch (ExceptionZZZ ez) {
				if(objKernel==null){
					ez.printStackTrace();
					System.out.println(ez.getDetailAllLast());
				}else {
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog==null){
						ez.printStackTrace();
						System.out.println(ez.getDetailAllLast());
					}else {
						try {
							objLog.writeLineDate(ez.getDetailAllLast());
						} catch (ExceptionZZZ e) {						
							e.printStackTrace();
							System.out.println(e.getDetailAllLast());
						}							
					}
				}
			}//END Catch
			}//END main:
			System.out.println("finished everything");
	}//END main()

}//END class
