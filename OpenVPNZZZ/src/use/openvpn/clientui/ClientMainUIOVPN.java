package use.openvpn.clientui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.clientui.ConfigOVPN;
import use.openvpn.clientui.component.tray.ClientTrayUIOVPN;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.file.ini.IKernelCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelEncryptionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelExpressionIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJavaCallIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonArrayIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonIniSolverZZZ;
import basic.zKernel.file.ini.IKernelJsonMapIniSolverZZZ;
import basic.zKernel.file.ini.IKernelZFormulaIniZZZ;
import custom.zKernel.LogZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;

public class ClientMainUIOVPN implements IConstantZZZ {
	private IKernelZZZ objKernel=null;
	private ClientMainOVPN objClientMain = null;
	private ClientTrayUIOVPN objClientTray=null;
	
	/**Entry point for the OVPN-Client-Starter.
	 * @return void
	 * @param args 
	 * 
	 * lindhaueradmin; 08.07.2006 08:24:16
	 */
	public static void main(String[] args) {
		ClientMainUIOVPN objClient = new ClientMainUIOVPN();		
		objClient.start(args);
	}//END main()
	
	public boolean start(String[] saArg){
		boolean bReturn = false;
		main:{						
			try {
				//Parameter aus args auslesen
				String[]saFlag = {IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION_SOLVER.name(),IKernelZFormulaIniZZZ.FLAGZ.USEFORMULA.name(), IKernelZFormulaIniZZZ.FLAGZ.USEFORMULA_MATH.name(),IKernelJsonIniSolverZZZ.FLAGZ.USEJSON.name(),IKernelJsonArrayIniSolverZZZ.FLAGZ.USEJSON_ARRAY.name(),IKernelJsonMapIniSolverZZZ.FLAGZ.USEJSON_MAP.name(), IKernelEncryptionIniSolverZZZ.FLAGZ.USEENCRYPTION.name(),IKernelCallIniSolverZZZ.FLAGZ.USECALL.name(), IKernelJavaCallIniSolverZZZ.FLAGZ.USECALL_JAVA.name()};
				ConfigOVPN objConfig = new ConfigOVPN(saArg, saFlag);
				this.objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man über die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.
				
				//NUN DAS BACKEND-Handlebar machen
				this.objClientMain = new ClientMainOVPN(objKernel, null);
				
				ClientApplicationOVPN objApplication = new ClientApplicationOVPN(objKernel, this.objClientMain);
				this.objClientMain.setApplicationObject(objApplication);
											
				//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
				this.objClientTray = new ClientTrayUIOVPN(objKernel, this.objClientMain, (String[]) null);		
				bReturn = objClientTray.load();
				
				//Konfigurierbar: Beim Launch der Applikation schon starten
				boolean btemp = this.objClientMain.isStartingOnLaunch();
				if(btemp==false){							
					bReturn = true;
					break main;
				}
				bReturn = objClientTray.start();
				if(!bReturn) {
					bReturn = true;
					break main;
				}	
								
				//Konfigurierbar: Beim Starten schon connecten
				btemp = this.objClientMain.isConnectOnStart();
				if(btemp==true){
					bReturn = objClientTray.connect();
					break main;
				}else{
					bReturn = true;
					break main;
				}				
			} catch (ExceptionZZZ ez) {
				if(objKernel!=null){
					LogZZZ objLog = objKernel.getLogObject();
					if(objLog!=null){
						try {
							objLog.WriteLineDate(ez.getDetailAllLast());
						} catch (ExceptionZZZ ez2) {
							ez2.printStackTrace();
							System.out.println(ez2.getDetailAllLast());
							ez.printStackTrace();
							System.out.println(ez.getDetailAllLast());
						}
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
