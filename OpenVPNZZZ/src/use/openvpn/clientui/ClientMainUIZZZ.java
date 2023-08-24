package use.openvpn.clientui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.clientui.ConfigOVPN;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.file.ini.IKernelCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelEncryptionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelExpressionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJavaCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonArrayIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonMapIniSolverZZZ;
import basic.zKernel.file.ini.IKernelZFormulaIniSolverZZZ;
import custom.zKernel.LogZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;

public class ClientMainUIZZZ implements IConstantZZZ {
	private IKernelZZZ objKernel=null;
	private ClientMainOVPN objClientMain = null;
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
				String[]saFlag = {IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION.name(),IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA.name(), IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA_MATH.name(),IKernelJsonIniSolverZZZ.FLAGZ.USEJSON.name(),IKernelJsonArrayIniSolverZZZ.FLAGZ.USEJSON_ARRAY.name(),IKernelJsonMapIniSolverZZZ.FLAGZ.USEJSON_MAP.name(), IKernelEncryptionIniSolverZZZ.FLAGZ.USEENCRYPTION.name(),IKernelCallIniSolverZZZ.FLAGZ.USECALL.name(), IKernelJavaCallIniSolverZZZ.FLAGZ.USECALL_JAVA.name()};
				ConfigOVPN objConfig = new ConfigOVPN(saArg, saFlag);
				this.objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man über die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.
				
				//NUN DAS BACKEND-Handlebar machen
				this.objClientMain = new ClientMainOVPN(objKernel, null);
				
				ClientApplicationOVPN objApplication = new ClientApplicationOVPN(objKernel, this.objClientMain);
				this.objClientMain.setApplicationObject(objApplication);
				
				
				//20210402: Hier soll auch im Log die Verarbeitung des 'vererbenden' Kommandozeilenparameters -z ersichtlich sein.
				//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
				this.objClientTray = new ClientTrayUIZZZ(objKernel, this.objClientMain, (String[]) null);

				//Registriere das ClientTray-Objekt fuer Aenderungen an den ServerMain-Objekt-Flags. Das garantiert, das der Tray auch auf Änderungen der Flags reagiert, wenn ServerMain in einem anderen Thread ausgeführt wird.
				this.objClientMain.registerForFlagEvent(this.objClientTray);
				
				
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
