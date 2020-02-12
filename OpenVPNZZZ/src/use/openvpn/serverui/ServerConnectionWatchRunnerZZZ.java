package use.openvpn.serverui;

import java.io.File;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.server.ServerConfigFileOVPN;
import use.openvpn.server.ServerConfigStarterOVPN;
import basic.zKernel.KernelZZZ;

/** !!! This will be no longer used. Use ServerConnectionListenerZZZ instead....
 * 
 * This class pings an ip and a port.
 * It provides some methods to read the connection status.
 * 
 *
 * @author 0823
 *
 */
public class ServerConnectionWatchRunnerZZZ extends KernelUseObjectZZZ implements Runnable{
	private ServerConfigFileOVPN objFileConfigServer = null;
	private ServerConfigStarterOVPN objProcessStarter = null;
	private String sTargetIP = "";
	private String sTargetPort = "";
	private boolean bFlagIsConnected = false;
	private boolean bFlagIsStarted = false;
	private boolean bFlagHasError = false;
	
	public ServerConnectionWatchRunnerZZZ(IKernelZZZ objKernel, ServerConfigStarterOVPN objProcessStarter, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ServerConnectionWatchRunnerNew_(objProcessStarter, saFlagControl);
	}
	
	private void ServerConnectionWatchRunnerNew_(ServerConfigStarterOVPN objProcessStarter, String[] saFlagControl) throws ExceptionZZZ{
		main:{
			check:{
				if(objProcessStarter==null){
					ExceptionZZZ ez = new ExceptionZZZ("ConfigStarterZZZ-Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
			}//END check
		}//END main:
	
		 //	+++ Aus dem File die IP und den Port auslesen.
		this.objProcessStarter = objProcessStarter;
		
		File objFileTemp = objProcessStarter.getFileConfig();
		if(objFileTemp==null){
			ExceptionZZZ ez = new ExceptionZZZ("File-Object in ConfigStarterZZZ-Object", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}
		this.objFileConfigServer = new ServerConfigFileOVPN(objKernel, objFileTemp, null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try{
			ServerConfigFileOVPN objConfigFile = this.getConfigFileHandlerObject();
			this.setFlag("isstarted", true);
			
			
			this.sTargetIP = objConfigFile.getVpnIpRemote();
			this.sTargetPort = this.readVpnPort2Check();
			this.getLogObject().WriteLineDate(this.getAlias() + "# Pinging every second to realize an established connection with '" + this.sTargetIP +":"+ this.sTargetPort + "'");
			do{
								
				boolean btemp = objConfigFile.isVpnReachable(this.sTargetIP, this.sTargetPort);
				this.setFlag("IsConnected", btemp);
				if(this.getFlag("isconnected")==false){
					this.getLogObject().WriteLineDate(this.getAlias() + "# Not currently connected with '" + this.sTargetIP +":"+ this.sTargetPort + "'");
				}else{
					this.getLogObject().WriteLineDate(this.getAlias() + "# Currently a connection established with '" + this.sTargetIP +":"+ this.sTargetPort + "'");
				}
				Thread.sleep(1000);
				
//				Warum in der Schleife ?     Die Idee ist, das man ggf. dann zur Laufzeit z.B. den Port �ndern kann. Soll beim Testen unterst�tzen.
				this.sTargetIP = objConfigFile.getVpnIpRemote();
				this.sTargetPort = this.readVpnPort2Check();
			
			}while(true);
		}catch(InterruptedException e){
			this.setFlag("HasError", true);
			e.printStackTrace();
		}catch(ExceptionZZZ ez){
			this.setFlag("HasError", true);
			System.out.println(ez.getDetailAllLast());
		}
	}
	
	/**Reads a port from the configuration-file. Default: Port 80.
	 * This port is used to check the connection. 
	 * Program used: ProgVPNCheck
	 * Parameter to configure there: VPNPort2Check
	 * 
	 * Remark:
	 * This configuration is used for both:
	 * The OVPNStarter-Client and the OVPNStarter-Server.
	 * @throws ExceptionZZZ, 
	 *
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:05:05
	 */
	public String readVpnPort2Check() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgVPNCheck","VPNPort2Check").getValue();
			if(sReturn==null){
				sReturn ="80";
			}else if(sReturn.equals("")){
				sReturn="80";
			}
		}//END main:
		return sReturn;
	}
	
	//####### Getter // Setter
	public ServerConfigFileOVPN getConfigFileHandlerObject(){
		return this.objFileConfigServer;		
	}
	
	public ServerConfigStarterOVPN getStarterObject(){
		return this.objProcessStarter;
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
				if(this.objProcessStarter==null) break main;
			}
		
			sReturn = this.objProcessStarter.getAlias();
		}//END main
		return sReturn;
	}
	
	public String getIp(){
		return this.sTargetIP;
	}
	
	public String getPort(){
		return this.sTargetPort;
	}
	
	

//	######### GetFlags - Handled ##############################################
	/** (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used:<CR>
	-  isConnected	
	- haserror
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
		
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("isstarted")){
				bFunction = bFlagIsStarted;
				break main;
			}else if(stemp.equals("isconnected")){
				bFunction = bFlagIsConnected;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}
		}//end main:
		return bFunction;
	}
	
	


	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 * - isconnected
	 * - haserror
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
	
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("isstarted")){
			bFlagIsStarted = bFlagValue;
			bFunction = true;
			break main;	
		}else if(stemp.equals("isconnected")){
				bFlagIsConnected = bFlagValue;
				bFunction = true;
				break main;				
		}else if(stemp.equals("haserror")){
			bFlagHasError = bFlagValue;
			bFunction = true;
			break main;	
		}
		}//end main:
		return bFunction;
	}
	

}
