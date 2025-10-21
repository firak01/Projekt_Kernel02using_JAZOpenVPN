package debug.openvpn.client;

import use.openvpn.client.ClientMainOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;

public class DebugClientMainZZZ {

	/**Debug class for ConfigMainZZZ
	 * @param args, 
	 *
	 * @return void
	 *
	 * javadoc created by: 0823, 10.07.2006 - 12:00:08
	 */
	public static void main(String[] args) {
		main:{
			KernelZZZ objKernel = null;		
			try {
				objKernel = new KernelZZZ("OVPN", "01", "",  "ZKernelConfig_OVPNClient.ini", (String)null);
				//objKernel.getLogObject().WriteLineDate("TEST");
				
				ClientMainOVPN objConfig = new ClientMainOVPN(objKernel, null);
				objConfig.startAsThread();
			

			} catch (ExceptionZZZ ez) {
				if(objKernel!=null){
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog!=null){								
						try {
							objLog.WriteLineDate(ez.getDetailAllLast());
						} catch (ExceptionZZZ e) {					
							e.printStackTrace();
							System.out.println(ez.getDetailAllLast());
						}
					}else {
						ez.printStackTrace();
						System.out.println(ez.getDetailAllLast());
					}
				}else{
					ez.printStackTrace();
					System.out.println(ez.getDetailAllLast());
				}
			}//END Catch
		}//END main:
		System.out.println("finished everything");
	}

}
