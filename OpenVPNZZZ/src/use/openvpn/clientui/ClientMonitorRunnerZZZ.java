package use.openvpn.clientui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainZZZ;
import basic.zKernel.KernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ClientMonitorRunnerZZZ extends KernelUseObjectZZZ implements Runnable{
	private ClientMainZZZ objConfig = null;
	private ClientTrayUIZZZ objTray = null;
	private OVPNConnectionWatchRunnerZZZ  objWatchRunner = null;
	private Thread objWatchThread = null;
	
	private int iStatusSet = 0;  //Der Status, der schon im Tray gesetzt ist. Damit er nicht permanent neu gesetzt wird.
	private boolean bFlagConnectionRunnerStarted=false;
	
public ClientMonitorRunnerZZZ(IKernelZZZ objKernel, ClientTrayUIZZZ objTray, ClientMainZZZ objConfig, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ConfigMonitorRunnerNew_(objTray, objConfig, saFlagControl);
}

private void ConfigMonitorRunnerNew_(ClientTrayUIZZZ objTray, ClientMainZZZ objConfig, String[] saFlagControl) throws ExceptionZZZ{
	main:{
		
		check:{
	 		
			if(saFlagControl != null){
				String stemp; boolean btemp;
				for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
					stemp = saFlagControl[iCount];
					btemp = setFlag(stemp, true);
					if(btemp==false){ 								   
						   ExceptionZZZ ez = new ExceptionZZZ( stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
						   throw ez;		 
					}
				}
				if(this.getFlag("init")) break main;
			}
		
						
			this.objConfig = objConfig;
			this.objTray = objTray;
		}//End check

	}//END main
}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 *TODO: Die Fehler ins Log-Schreiben.
	 */
	public void run() {
		main:{
			check:{
				if(this.objConfig==null) break main;
			}//END check:
		   
	boolean bCheck= false;
		try {
			do{				
				bCheck = objConfig.getFlag("isconnected");
				if(bCheck == true){											
					//Nun erst, nachdem man den Verbindungsaufbau best�tigt hat, die VPN-IP anpingen
					//Aber auch nur einen Thread starten !!!
					if(this.getFlag("ConnectionRunnerStarted")==false){ //DIe Theorie besagt, dass dies erst nach abschluss der Portscanns starten sollte, aber das dauert wohl zu lange. && objConfig.getFlag("PortScanAllFinished")==true){
						//Den Runner starten ....							
						String sIP = ((ClientApplicationOVPN)objConfig.getApplicationObject()).getVpnIpRemote();
						
							String sPort = ((ClientApplicationOVPN)objConfig.getApplicationObject()).readVpnPort2Check();									
							this.objWatchRunner = new OVPNConnectionWatchRunnerZZZ(this.getKernelObject(), sIP, sPort, null);
						
						this.objWatchThread = new Thread(objWatchRunner);
						this.objWatchThread.start();
						this.setFlag("ConnectionRunnerStarted", true);
					}//END if flagget "ConnectionRunnerStarted;
					
					//... das normale Connection-Image-Setzen.
					if(this.iStatusSet!=ClientTrayUIZZZ.iSTATUS_CONNECTED){
						objTray.switchStatus(ClientTrayUIZZZ.iSTATUS_CONNECTED);
						this.iStatusSet = ClientTrayUIZZZ.iSTATUS_CONNECTED;
					}		
				}//END if bCheck == true
					
				 if(this.getFlag("ConnectionRunnerStarted")){
						//Den Runner �berwachen....
						//System.out.println("wache");
						if(this.objWatchRunner.getFlag("ConnectionBroken")){
							//System.out.println("verbindung unterbrochen.");
							//... das Icon "Verbindung-Unterbrochen" setzen
							if(this.iStatusSet!=ClientTrayUIZZZ.iSTATUS_INTERRUPTED){
								objTray.switchStatus(ClientTrayUIZZZ.iSTATUS_INTERRUPTED);
								this.iStatusSet = ClientTrayUIZZZ.iSTATUS_INTERRUPTED;
								break main;
							}							
						}	
				 }else{
					 //System.out.println("wache noch nicht.....");
				 }//END if "ConnectionRunnerStarted"
				 
				bCheck = objConfig.getFlag("haserror");
				if(bCheck == true){	
					if(this.iStatusSet!=ClientTrayUIZZZ.iSTATUS_ERROR){
						objTray.switchStatus(ClientTrayUIZZZ.iSTATUS_ERROR);	
						this.iStatusSet = ClientTrayUIZZZ.iSTATUS_ERROR;
						break  main;
					}
				}//END if bCheck == true
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(true);
		}catch(ExceptionZZZ ez){
			System.out.println(ez.getDetailAllLast());
		}
	}//END main:
	
	}//END run
	
	
	

	//###### FLAGS
	/* (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- hasError
	- hasOutput
	- hasInput
	- stoprequested
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
		
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("connectionrunnerstarted")){
				bFunction = bFlagConnectionRunnerStarted;
				break main;
			}
			
			/*else if(stemp.equals("hasoutput")){
				bFunction = bFlagHasOutput;
				break main;
			}else if(stemp.equals("hasinput")){
				bFunction = bFlagHasInput;
				break main;
			}else if(stemp.equals("stoprequested")){
				bFunction = bFlagStopRequested;
				break main;
			}
			*/
	
		}//end main:
		return bFunction;
	}

	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 	- ConnectionRunnerStarted.
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
		boolean bFunction = false;
		main:{			
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
		
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("connectionrunnerstarted")){
			bFlagConnectionRunnerStarted = bFlagValue;
			bFunction = true;
			break main;

		}
		/*else if(stemp.equals("hasoutput")){
			bFlagHasOutput = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("hasinput")){
			bFlagHasInput = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("stoprequested")){
			bFlagStopRequested = bFlagValue;
			bFunction = true;
			break main;
		}
		*/

		}//end main:
		return bFunction;
	}

}//END class
