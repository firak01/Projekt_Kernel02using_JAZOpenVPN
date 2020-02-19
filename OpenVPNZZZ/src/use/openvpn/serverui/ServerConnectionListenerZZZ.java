package use.openvpn.serverui;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.server.ServerConfigStarterOVPN;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.net.server.KernelServerTcpZZZ;

public class ServerConnectionListenerZZZ extends KernelServerTcpZZZ{
	private ServerConfigStarterOVPN objConfigStarter=null;   //dar�ber ist u.a. das OVPN-Konfigurations-File zu holen, aus dem man die IP und den Port auslesen kann.
	private ConfigFileTemplateOvpnOVPN objConfigFile = null;

		
	public ServerConnectionListenerZZZ(IKernelZZZ objKernel, ServerConfigStarterOVPN objConfigStarter, String[] saFlagControl) throws ExceptionZZZ {
		super(objKernel, saFlagControl);
		if(this.getFlag("init")==false){
			ServerConnectionListenerNew_(objConfigStarter);
		}
	}
	
	private void ServerConnectionListenerNew_(ServerConfigStarterOVPN objConfigStarter) throws ExceptionZZZ{
		try{
			String sHost=null;
			String sPort=null;
			File objFile = null;
			main:{
				check:{
					if(objConfigStarter==null){
						ExceptionZZZ ez = new ExceptionZZZ("ConfigStarterZZZ - Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					this.objConfigStarter = objConfigStarter;
					
					objFile = objConfigStarter.getFileConfig();
					if(objFile==null){
						ExceptionZZZ ez = new ExceptionZZZ("File - Object at ConfigStarterZZZ - Object", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
				}//END check
			
			//Nun das File-Object in ein Configuration-File-Objekt bringen. Dabei werden dann auch alle Properties zur Verf�gung gestellt.
			this.objConfigFile = new ConfigFileTemplateOvpnOVPN(this.getKernelObject(), objFile, null);
			sHost = this.objConfigFile.getVpnIpLocal();    //Auf welche lokale VPN-Verbindung soll geachtet werden
			
			//TODO GOON: Fehlermeldung java.net.BindException
			System.out.println(ReflectCodeZZZ.getMethodCurrentName()+"#"+sHost+" , kann der aufgel�st werden ????");			
			this.setHost(sHost);
			
			//Der Port, auf den zu h�ren ist, wird allerdings in der Kernel-Konfiguration definiert
			sPort = this.getKernelObject().getParameterByProgramAlias("OVPN", "ProgVPNCheck", "VPNPort2Check").getValue();
			this.setPort(sPort);
			
			}//END main
		}finally{
			
		}		
	}
	
	/* (non-Javadoc)
	 * @see basic.zKernel.net.server.KernelServerTcpZZZ#process()
	 */
	public boolean customProcess() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			this.getLogObject().WriteLineDate("Accepted by client " + this.getServerSocketObject().getInetAddress());
			//System.out.println("Accepted by client " + this.getServerSocketObject().getInetAddress());	
			System.out.println(ReflectCodeZZZ.getMethodCurrentName() + "# Accepted from server " + this.getServerSocketObject().getInetAddress() + " by client " + this.getSocketObjectCurrent().getRemoteSocketAddress());
			
			//TODOGOON: Der Client soll an den Server verschiedene Befehle senden d�rfen
			//z.B. beenden, restart, ....
			bReturn = true;
		}
		return bReturn;
	}
	
	/* (non-Javadoc)
	 * @see basic.zKernel.net.server.KernelServerTcpZZZ#customQueryProcess()
	 */
	public boolean customQueryProcess() throws ExceptionZZZ{
		boolean bReturn = false;
		
		ServerSocket objSockTemp=null;
		String sConnection = "";
		try{			
			main:{
				sConnection = "(" + this.getHostString() + ":" + this.getPortString();
				
				//Die Hauptaufgabe liegt darin einige Sekunden zu warten, damit die Netzwerkverrbindung die Chance hat zu starten und das Java ServerSocket-Objekt an diese Netzwerkadresse gebunden werden kann.
				String sSecondTimeOut = this.getKernelObject().getParameterByProgramAlias("OVPN", "ProgVPNCheck", "TapDeviceStartupTimeout").getValue();
				int iSecondTimeOut = 0;
				if(!StringZZZ.isEmpty(sSecondTimeOut)){
					 iSecondTimeOut = Integer.parseInt(sSecondTimeOut);
					 this.getLogObject().WriteLineDate("Waiting for network connection to come up." + sConnection +" for " + String.valueOf(iSecondTimeOut) + " Seconds  #" + ReflectCodeZZZ.getMethodCurrentName());
				}else{
					this.getLogObject().WriteLineDate("Waiting for network connection to come up." + sConnection +". No timeout-time configured." + ReflectCodeZZZ.getMethodCurrentName());
				}
				
				
				do{
					try{
						sConnection = "(" + this.getHostString() + ":" + this.getPortString();  //Wenn das neu Berechnet wird, dann kan theoretisch in der Schleife der Wert ge�ndert werden.
						objSockTemp = new ServerSocket(this.getPort(), BACKLOG, this.getHost());		
						
						//Wenn man hierhin kommt, dann ist alles o.k.
						this.getLogObject().WriteLineDate("Network connection seems to be available." + sConnection +"  #" + ReflectCodeZZZ.getMethodCurrentName());
						bReturn=true;
						break main;
						
					}catch(IOException e){
							//Keinen Fehler ausgeben, nur eine Sekunde warten
							this.getLogObject().WriteLineDate("Waiting for network connection to come up." + sConnection + " #" + ReflectCodeZZZ.getMethodCurrentName());
							Thread.sleep(1000);	
							iSecondTimeOut--;
					}									
					
				}while(iSecondTimeOut>0);
			}//END main
		} catch (InterruptedException e1) {			
			e1.printStackTrace();
			this.setFlag("HasError", true);
		}finally{
			if(objSockTemp!=null){
				try {
					objSockTemp.close();
				} catch (IOException e) {
					//Da wir f�r den tempor�ren "TestSocket" kein accept anwenden, wird wohl kein Fehler passieren im finally
					e.printStackTrace();
					this.setFlag("HasError", true);
				}
			}
		}//END finally
		return bReturn;
	}
	
	//#### Getter / Setter
	public ServerConfigStarterOVPN getStarterObject(){
		return this.objConfigStarter;
	}
	
	/**Reads the alias from the ConfigStarterZZZ object.
	 * @return String
	 *
	 * javadoc created by: 0823, 28.07.2006 - 15:49:06
	 */
	public String getAlias(){
		String sReturn = "";
		main:{
			check:{
				if(this.objConfigStarter==null) break main;
			}
		
			sReturn = this.objConfigStarter.getAlias();
		}//END main
		return sReturn;
	}
	
	public String getVpnIpRemote(){
		String sReturn = "";
		main:{
			check:{
				if(this.objConfigFile==null) break main;
			}		
			sReturn = this.objConfigFile.getVpnIpRemote();
		}//END main
		return sReturn;
	}
	
}//END class
