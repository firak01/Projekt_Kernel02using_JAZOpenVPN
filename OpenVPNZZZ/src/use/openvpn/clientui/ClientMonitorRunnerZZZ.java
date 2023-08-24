package use.openvpn.clientui;

import use.openvpn.client.ClientApplicationOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.clientui.ClientTrayStatusMappedValueZZZ;
import use.openvpn.clientui.ClientTrayUIZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;

import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ClientMonitorRunnerZZZ extends KernelUseObjectZZZ implements Runnable,IListenerObjectStatusLocalSetOVPN{
	private ClientMainOVPN objMain = null;
	private ClientTrayUIZZZ objTray = null;
	
	private HashMap hmWatchRunnerStatus = new HashMap(); //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatus = new String("");            //Das wird hier gefuellt und kann vom Tray-Objekt bei Bedarf ausgelesen werden.
	private String sWatchRunnerStatusPrevious = new String("");    //den vorherigen Status festhalten, damit z.B. nicht immer wieder das Icon geholt wird.
	
	private OVPNConnectionWatchRunnerZZZ  objWatchRunner = null;
	private Thread objWatchThread = null;
	
	//private int iStatusSet = 0;  //Der Status, der schon im Tray gesetzt ist. Damit er nicht permanent neu gesetzt wird.
	private boolean bFlagConnectionRunnerStarted=false;
	
public ClientMonitorRunnerZZZ(IKernelZZZ objKernel, ClientTrayUIZZZ objTray, ClientMainOVPN objConfig, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	ConfigMonitorRunnerNew_(objTray, objConfig, saFlagControl);
}

private void ConfigMonitorRunnerNew_(ClientTrayUIZZZ objTray, ClientMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
	main:{
		
		check:{
	 		
			if(saFlagControl != null){
				String stemp; boolean btemp;
				for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
					stemp = saFlagControl[iCount];
					btemp = setFlag(stemp, true);
					if(btemp==false){ 								   
						   ExceptionZZZ ez = new ExceptionZZZ( stemp, IFlagZUserZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 
						   throw ez;		 
					}
				}
				if(this.getFlag("init")) break main;
			}
		
						
			this.objMain = objMain;
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
		try {
			check:{
				if(this.objMain==null) break main;
			}//END check:
		   
			boolean bCheck= false;
			String sLog = ReflectCodeZZZ.getPositionCurrent() + ": In run()-Schleife.";
		
			//TODOGOON20230820;//Eigentlich sollte das doch nicht mehr auf getFlag abfragen, oder?
			do{			
				System.out.println(sLog);
				this.getLogObject().WriteLineDate(sLog);
				
				
				if(objMain.getFlag("haserror")){
					//StatusString aendern
					String sWatchRunnerStatus = ClientTrayUIZZZ.getStatusStringByStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.ERROR);
					if(this.isStatusChanged(sWatchRunnerStatus)) {
						this.setStatusString(sWatchRunnerStatus);
						this.objTray.switchStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.ERROR);
					}					
					break main;
				}			
								
				bCheck = objMain.getFlag("isconnected");
				if(bCheck == true){											
					//Nun erst, nachdem man den Verbindungsaufbau best�tigt hat, die VPN-IP anpingen
					//Aber auch nur einen Thread starten !!!
					if(this.getFlag("ConnectionRunnerStarted")==false){ //DIe Theorie besagt, dass dies erst nach abschluss der Portscanns starten sollte, aber das dauert wohl zu lange. && objConfig.getFlag("PortScanAllFinished")==true){
						//Den Runner starten ....							
						String sIP = ((ClientApplicationOVPN)objMain.getApplicationObject()).getVpnIpRemote();
						
							String sPort = ((ClientApplicationOVPN)objMain.getApplicationObject()).readVpnPort2Check();									
							this.objWatchRunner = new OVPNConnectionWatchRunnerZZZ(this.getKernelObject(), sIP, sPort, null);
						
						this.objWatchThread = new Thread(objWatchRunner);
						this.objWatchThread.start();
						this.setFlag("ConnectionRunnerStarted", true);
					}//END if flagget "ConnectionRunnerStarted;
					
					//... das normale Connection-Image-Setzen.
					if(this.getStatusString().equalsIgnoreCase(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.CONNECTED.getName())){
						objTray.switchStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.CONNECTED);
					}		
				}//END if bCheck == true
					
				 if(this.getFlag("ConnectionRunnerStarted")){
						//Den Runner �berwachen....
						//System.out.println("wache");
						if(this.objWatchRunner.getFlag("ConnectionBroken")){
							//System.out.println("verbindung unterbrochen.");
							//... das Icon "Verbindung-Unterbrochen" setzen
							if(this.getStatusString().equalsIgnoreCase(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.INTERRUPTED.getName())){
								objTray.switchStatus(ClientTrayStatusMappedValueZZZ.ClientTrayStatusTypeZZZ.INTERRUPTED);								
								break main;
							}							
						}	
				 }else{
					 //System.out.println("wache noch nicht.....");
				 }//END if "ConnectionRunnerStarted"
				 
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
	
	
	public String getStatusString(){
		return this.sWatchRunnerStatus;
	}
	public void setStatusString(String sStatus) {
		
		main:{
			String sStatusPrevious = this.getStatusString();
			if(sStatus == null) {
				if(sStatusPrevious==null)break main;
			}
			
			if(!sStatus.equals(sStatusPrevious)) {
				String sStatusCurrent = this.getStatusString();
				this.sWatchRunnerStatus = sStatus;
				this.setStatusPrevious(sStatusCurrent);
			}
		}//end main:
	
	}
	
	public String getStatusPreviousString() {
		return this.sWatchRunnerStatusPrevious;
	}
	public void setStatusPrevious(String sStatusPrevious) {
		this.sWatchRunnerStatusPrevious = sStatusPrevious;
	}
	
	public boolean isStatusChanged(String sStatusString) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			if(sStatusString == null) {
				bReturn = this.getStatusString()==null;
				break main;
			}
			
			if(!sStatusString.equals(this.getStatusString())) {
				bReturn = true;
			}
		}//end main:
		if(bReturn) {
			String sLog = ReflectCodeZZZ.getPositionCurrent()+ ": Status changed to '"+sStatusString+"'";
			System.out.println(sLog);
		    this.getLogObject().WriteLineDate(sLog);			
		}
		return bReturn;
	}

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
	 * @throws ExceptionZZZ 
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
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

	//aus IListenerObjectStatusLocalSetZZZ
	@Override
	public boolean statusLocalChanged(IEventObjectStatusLocalSetOVPN eventStatusLocalSet) throws ExceptionZZZ {
		//Lies den Status (geworfen vom Backend aus)
		String sStatus = eventStatusLocalSet.getStatusText();
		
		//übernimm den Status
		this.setStatusString(sStatus);
		
		return true;
	}

}//END class
