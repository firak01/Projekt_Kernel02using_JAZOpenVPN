package use.openvpn.serverui;

import use.openvpn.serverui.ConfigOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IConstantZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import custom.zKernel.LogZZZ;

public class ServerMainUIZZZ implements IConstantZZZ {
			private IKernelZZZ objKernel=null;
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
						ConfigOVPN objConfig = new ConfigOVPN(saArg, "useFormula");
						//objKernel = new KernelZZZ("OVPN", "01", "",  "ZKernelConfig_OVPNServer.ini", (String)null);
						objKernel = new KernelZZZ(objConfig, (String) null); //Damit kann man �ber die Startparameter ein anders konfiguriertes Kernel-Objekt erhalten.
						//objKernel.getLogObject().WriteLineDate("TEST");
						
						//### 1. Voraussetzung: OpenVPN muss auf dem Rechner vorhanden sein. Bzw. die Dateiendung .ovpn ist registriert. 
						objServerTray = new ServerTrayUIZZZ(objKernel, (String[]) null);
						bReturn = objServerTray.load();
						
						//Konfigurierbar: Beim Starten schon connecten
						boolean btemp = this.isListenOnStart();
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
			
			public boolean isListenOnStart() throws ExceptionZZZ{
				boolean bReturn = false;
				main:{
					check:{
						if(this.objKernel==null) break main;				
					}//END check:
				
				//Das setzt voraus, das die Kernel-Konfigurationsdatei eine Modul-Section enth�lt, die wie der Application - Key aussieht. 
				String stemp = this.objKernel.getParameter("ListenOnStart").getValue();
				if(stemp==null) break main;
				if(stemp.equals("1")){
					bReturn = true;
				}
				}//END main
				return bReturn;
			}
			

}//END class
