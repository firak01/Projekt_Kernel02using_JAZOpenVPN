package use.openvpn.serverui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.server.ServerApplicationOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.serverui.ConfigOVPN;
import use.openvpn.serverui.component.tray.ServerTrayUIOVPN;

import java.util.ArrayList;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;
import basic.zBasic.AbstractObjectWithFlagZZZ;
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
import basic.zKernel.flag.event.IEventObjectFlagZsetZZZ;
import basic.zKernel.flag.event.IListenerObjectFlagZsetZZZ;
import basic.zKernel.flag.event.ISenderObjectFlagZsetZZZ;
import custom.zKernel.LogZZZ;

public class ServerMainUIZZZ implements IConstantZZZ{
			private IKernelZZZ objKernel=null;
			private ServerMainOVPN objServerMain = null;
			private ServerTrayUIOVPN objServerTray=null;
			
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
						this.objServerMain = new ServerMainOVPN(objKernel, null);
						
						ServerApplicationOVPN objApplication = new ServerApplicationOVPN(objKernel, this.objServerMain);
						this.objServerMain.setApplicationObject(objApplication);
						
						//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
						this.objServerTray = new ServerTrayUIOVPN(objKernel, this.objServerMain, (String[]) null);
						bReturn = objServerTray.load();
												
						//Konfigurierbar: Beim Launch der Applikation schon starten
						boolean btemp = this.objServerMain.isStartingOnLaunch();
						if(btemp==false){							
							bReturn = true;
							break main;
						}
						bReturn = objServerTray.start();//Prüfe die startbedingungen ab.
						if(!bReturn) {
							bReturn = true;
							break main;
						}						
						
						//Konfigurierbar: Beim Starten schon connecten						
						 btemp = this.objServerMain.isListenOnStart();
						if(btemp==true){
							bReturn = objServerTray.listen();
							break main;
						}else{
							bReturn = true;
							break main;
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
