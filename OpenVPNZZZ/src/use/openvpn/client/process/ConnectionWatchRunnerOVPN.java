package use.openvpn.client.process;

import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;

/**This runner-class is used for a thread started by the thread clientMonitorRunner.
 * The new thread will permanently check the vpn-connection.
 * If the connection is lost, it will set a flag "ConnectionBroken", which will be recognized in the ClientMonitorRunner.run()-Method.
 * 
 * @author 0823
 *
 */
public class ConnectionWatchRunnerOVPN extends KernelUseObjectZZZ implements Runnable{
	private String sIP = null;
	private String sPort = null;
	
	private boolean bFlagConnectionBroken = false;
	
public ConnectionWatchRunnerOVPN(IKernelZZZ objKernel, String sIP, String sPort, String[] saFlagControl) throws ExceptionZZZ{
	super(objKernel);
	OVPNConnectionWatchRunnerNew_(sIP, sPort, saFlagControl);
}

private void OVPNConnectionWatchRunnerNew_(String sIP, String sPort, String[] saFlagControl) throws ExceptionZZZ{
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
			
			
			if(StringZZZ.isEmpty(sIP)){
				ExceptionZZZ ez = new ExceptionZZZ("IP", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			if(StringZZZ.isEmpty(sPort)){
				ExceptionZZZ ez = new ExceptionZZZ("Port", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
		}//END check

		this.sIP = sIP;
		this.sPort = sPort;
	}//END main:
}
	public void run() {
		main:{
		//In einer Endlosschleife die Verbindung permanent pr�fen
		try{
			//System.out.println(ReflectionZZZ.getMethodCurrentName()+ ": TEST, Ping Thread gestartet.");
			KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
			String sConnection = "";
			System.out.println("Ping Thread started. # " + ReflectCodeZZZ.getMethodCurrentName());
			do{ 
				boolean bResult = false;
				try{	
					sConnection = this.sIP + ":" + this.sPort;
					//this.getLogObject().WriteLineDate("Trying to reach: " + sConnection + " # " + ReflectionZZZ.getMethodCurrentName());
					bResult = objPing.ping(this.sIP, this.sPort);					
				}catch(ExceptionZZZ ez){
					//Diese Exception wurde wirklich erwartet. Darum nur zu testzwecken printen		
					//System.out.println(ReflectionZZZ.getMethodCurrentName()+ ": TEST, Ping Thread wurde mit folgender exception (innen) beendet: " + ez.getDetailAllLast());	
					this.setFlag("ConnectionBroken", true);
					break main;				
				} catch(Exception e){
					//Das ist die erwartete IOException
					this.setFlag("ConnectionBroken", true);
					break main;
				}
				if(bResult == false){
					this.getLogObject().WriteLineDate("Unable to reach: " + sConnection + " # " + ReflectCodeZZZ.getMethodCurrentName());
					this.setFlag("ConnectionBroken", true);					
					break main;
				}else{
					this.getLogObject().WriteLineDate("Succesfully reached: " + sConnection + " # " + ReflectCodeZZZ.getMethodCurrentName());
					Thread.sleep(10000);			
				}
			}while(true);
		
		}catch(ExceptionZZZ ez){
			//Eine nicht erwartetet Kernel-Exception
			System.out.println("Ping Thread wurde mit folgender exception (aussen) beendet: " + ez.getDetailAllLast() + " # " + ReflectCodeZZZ.getMethodCurrentName());	
			try {
				this.setFlag("ConnectionBroken", true);
			} catch (ExceptionZZZ e) {				
				e.printStackTrace();
			}
			break main;		
		}catch (InterruptedException e) {
			//Dies wird ebenfalls nicht erwartet
			e.printStackTrace();
			try {
				this.setFlag("ConnectionBroken", true);
			} catch (ExceptionZZZ e1) {				
				e1.printStackTrace();
			}
			break main;
		}
		
		}//END main:
		//System.out.println(ReflectionZZZ.getMethodCurrentName()+ ": TEST, Ping Thread beendet.");
	}//END run
	
	//###### FLAGS
	/** (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- ConnectionBroken
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
		
					
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("connectionbroken")){
				bFunction = bFlagConnectionBroken;
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
	 	- ConnectionBroken
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
		if(stemp.equals("connectionbroken")){
			this.bFlagConnectionBroken = bFlagValue;
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

}
