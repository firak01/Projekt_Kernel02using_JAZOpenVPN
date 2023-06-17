package use.openvpn.serverui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainZZZ;
import use.openvpn.server.ServerApplicationOVPN;
import use.openvpn.server.ServerMainZZZ;
import use.openvpn.serverui.ConfigOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;
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

public class ServerMainUIZZZ implements IConstantZZZ, IConstantServerOVPN {
			private IKernelZZZ objKernel=null;
			private ServerMainZZZ objServerMain = null;
			private ServerTrayUIZZZ objServerTray=null;
			
			/**Entry point for the OVPN-Server-Starter.
			 * @return void
			 * @param args 
			 * 
			 * lindhaueradmin; 08.07.2006 08:24:16
			 */
			public static void main(String[] args) {
				ServerMainUIZZZ objServer = new ServerMainUIZZZ();
				objServer.start(args);
			}//END main()
			
			public boolean start(String[] saArg){
				boolean bReturn = false;
				main:{						
					try {
//						Parameter aus args auslesen
						String[]saFlag = {IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION.name(),IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA.name(), IKernelZFormulaIniSolverZZZ.FLAGZ.USEFORMULA_MATH.name(),IKernelJsonIniSolverZZZ.FLAGZ.USEJSON.name(),IKernelJsonArrayIniSolverZZZ.FLAGZ.USEJSON_ARRAY.name(),IKernelJsonMapIniSolverZZZ.FLAGZ.USEJSON_MAP.name(), IKernelEncryptionIniSolverZZZ.FLAGZ.USEENCRYPTION.name(),IKernelCallIniSolverZZZ.FLAGZ.USECALL.name(), IKernelJavaCallIniSolverZZZ.FLAGZ.USECALL_JAVA.name()};
						ConfigOVPN objConfig = new ConfigOVPN(saArg, saFlag);
						this.objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man ueber die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.

						//NUN DAS BACKEND-Handlebar machen
						this.objServerMain = new ServerMainZZZ(objKernel, null);
						
						ServerApplicationOVPN objApplication = new ServerApplicationOVPN(objKernel, this.objServerMain);
						this.objServerMain.setApplicationObject(objApplication);
						
						//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
						this.objServerTray = new ServerTrayUIZZZ(objKernel, (String[]) null);
						this.objServerTray.setServerBackendObject(this.objServerMain);
						bReturn = objServerTray.load();
						
						//Konfigurierbar: Beim Starten schon connecten
						boolean btemp = this.objServerMain.isListenOnStart();
						if(btemp==true){
							bReturn = objServerTray.listen();
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
