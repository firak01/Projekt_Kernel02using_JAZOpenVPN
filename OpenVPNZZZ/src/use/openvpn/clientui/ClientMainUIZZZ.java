package use.openvpn.clientui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;

public class ClientMainUIZZZ implements IConstantZZZ {
	private IKernelZZZ objKernel=null;
	private ClientMainZZZ objClientMain = null;
	private ClientTrayUIZZZ objClientTray=null;
	
	/**Entry point for the OVPN-Client-Starter.
	 * @return void
	 * @param args 
	 * 
	 * lindhaueradmin; 08.07.2006 08:24:16
	 */
	public static void main(String[] args) {
		ClientMainUIZZZ objClient = new ClientMainUIZZZ();		
		objClient.start(args);
	}//END main()
	
	public boolean start(String[] saArg){
		boolean bReturn = false;
		main:{						
			try {
				//Parameter aus args auslesen
				ConfigOVPN objConfig = new ConfigOVPN(saArg, "useFormula");
				this.objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man über die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.
				
				//NUN DAS BACKEND-Handlebar machen
				this.objClientMain = new ClientMainZZZ(objKernel, null);
				
				ClientApplicationOVPN objApplication = new ClientApplicationOVPN(objKernel, this.objClientMain);
				this.objClientMain.setApplicationObject(objApplication);
				
				//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
				this.objClientTray = new ClientTrayUIZZZ(objKernel, this.objClientMain, (String[]) null);

				//Konfigurierbar: Beim Starten schon connecten
				boolean btemp = this.objClientMain.isConnectOnStart();
				if(btemp==true){
					bReturn = objClientTray.connect();
				}else{
					bReturn = true;
				}				
			} catch (ExceptionZZZ ez) {
				if(objKernel!=null){
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog!=null){
						objLog.WriteLineDate(ez.getDetailAllLast());
					}else{
						ez.printStackTrace();
						System.out.println(ez.getDetailAllLast());
					}				
				}else{
					ez.printStackTrace();
					System.out.println(ez.getDetailAllLast());
				}
			}//END Catch
			}//END main:
		System.out.println("finished starting trayicon.");
		return bReturn;			
	}
	
}//END class
