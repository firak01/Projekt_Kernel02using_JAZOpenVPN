package use.openvpn.client.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.status.EventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

/**This class receives the stream from a process, which was started by the ConfigStarterZZZ class.
 * This is necessary, because the process will only goon working, if the streams were "catched" by a target.
 * This "catching" will be done in a special thread (one Thread per process).  
 * @author 0823
 *
 */
public class ProcessWatchRunnerZZZ extends KernelUseObjectZZZ implements Runnable, IEventBrokerStatusLocalSetUserOVPN{
	private Process objProcess=null; //Der externe process, der hierdurch "gemonitored" werden soll
	protected ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	
	private int iNumber=0;
	public  boolean bEnded = false;
	private boolean bFlagHasError=false;
	private boolean bFlagHasOutput = false;
	private boolean bFlagHasConnection = false;	
	private boolean bFlagHasInput = false;
	private boolean bFlagStopRequested = false;
	
	public ProcessWatchRunnerZZZ(IKernelZZZ objKernel, Process objProcess, int iNumber, String[] saFlag) throws ExceptionZZZ{
		super(objKernel);
		ProcessWatchRunnerNew_(objProcess, iNumber, saFlag);
	}
	
	private void ProcessWatchRunnerNew_(Process objProcess, int iNumber, String[] saFlagControl) throws ExceptionZZZ{
		
		main:{			
			check:{
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ(stemp, IFlagZUserZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							  
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}
								
				if(objProcess==null){
					ExceptionZZZ ez = new ExceptionZZZ("Process - Object", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
			}//END check
	
	this.objProcess = objProcess;
	this.iNumber = iNumber;
		}//END main:
	}
	
	public void run() {
		
		main:{
			try{
			check:{
				
			}//END check:
			String sLog = "ProcessWatchRunner #"+ this.getNumber() + " started.";
			this.logLineDate(sLog);
			
			//Solange laufen, bis ein Fehler auftritt oder eine Verbindung erkannt wird.
			do{
				this.writeErrorToLog();
				if(this.getFlag("hasError")) break;

				this.writeOutputToLog();		//Man muss wohl erst den InputStream abgreifen, damit der Process weiterlaufen kann.
				if(this.getFlag("hasConnection")) {
					sLog = "Connection wurde erstellt. Beende ProcessWatchRunner #"+this.getNumber();
					this.logLineDate(sLog);						
					break;
				}
				
				//TODOGOON: Diese Klasse als Abstracte Klasse anbieten, mit den einfachsten Flags.
				            //Dann ein ProzessWatchRunnerOVPN estellen
				            //und die run-Methode ueberschreiben.
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.
				//Merke: Nun aber ueber das enum			
				if(this.objEventStatusLocalBroker!=null) {
					IEventObjectStatusLocalSetOVPN event = new EventObjectStatusLocalSetOVPN(this,1,ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
					this.objEventStatusLocalBroker.fireEvent(event);
				}			
				
				//TODOGOON: Und Statt des FlagHandling localStatus verwenden...
				
				//Nach irgendeiner Ausgabe enden ist hier falsch, in einer abstrakten Klasse vielleicht richtig, quasi als Muster.
				//if(this.getFlag("hasOutput")) break;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					ExceptionZZZ ez = new ExceptionZZZ("An InterruptedException happened: '" + e.getMessage() + "''", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				
				if( this.getFlag("stoprequested")==true) break;					
		}while(true);
		this.bEnded = true;
		this.getLogObject().WriteLineDate("ProcessWatchRunner #"+ this.getNumber() + " ended.");
					
		}catch(ExceptionZZZ ez){
			try {
				this.getLogObject().WriteLineDate(ez.getDetailAllLast());
				System.out.println(ez.getDetailAllLast());
			} catch (ExceptionZZZ e) {
				System.out.println(ez.getDetailAllLast());
				e.printStackTrace();
			}
		}
		}//END main
	}
	
	public void writeErrorToLog() throws ExceptionZZZ{
		main:{			
			try{
			check:{
				if(this.objProcess==null){
					ExceptionZZZ ez = new ExceptionZZZ("Process-Object", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
			}//END check:
		   		
		    BufferedReader err = new BufferedReader(new InputStreamReader(objProcess.getErrorStream()) );
		    for ( String s; (s = err.readLine()) != null; ){
			      //System.out.println( s );
		    	this.getLogObject().WriteLine(this.getNumber() + "# ERROR: "+ s);
		    	this.setFlag("hasError", true);
		    	Thread.sleep(20);			
		    	if( this.getFlag("stoprequested")==true) break main;
			    }
			} catch (IOException e) {
				ExceptionZZZ ez = new ExceptionZZZ("IOException happend: '" + e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			} catch (InterruptedException e) {
				ExceptionZZZ ez = new ExceptionZZZ("InterruptedException happend: '"+ e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
		}//END Main:	
	}
	
	/** In dieser Methode werden die Ausgabezeilen eines Batch-Prozesses ( cmd.exe ) 
	 *  aus dem Standard - Output gelesen.
	 *  - Sie werden in das Kernel-Log geschrieben.
	 *  - Sie werden hinsichtlich bestimmter Schluesselsaetze analysiert,
	 *    um z.B. den erfolgreichen Verbindungsaufbau mitzubekommen.
	 *  
	 *  Merke: 
	 *  Merke1: Der aufgerufene OVPN-Prozess stellt irgendwann das schreiben ein
					//Das ist dann z.B. der letzte Eintrag
					//0#Sat Sep 02 07:39:48 2023 us=571873 NOTE: --mute triggered... 
					//Der wert wird in der OVPN-Konfiguration eingestellt, z.B.:
					//mute=20  
				
	 * Merke2: Wie über einen Erfolg benachrichtigen?
			   Wenn die Verbindung erstellt wird, steht folgendes im Log.
			   
TCP connection established with [AF_INET]192.168.3.116:4999
0#Sat Sep 02 12:53:10 2023 us=223095 TCPv4_CLIENT link local: [undef]
0#Sat Sep 02 12:53:10 2023 us=223095 TCPv4_CLIENT link remote: [AF_INET]192.168.3.116:4999
0#Sat Sep 02 12:53:10 2023 us=223095 TLS: Initial packet from [AF_INET]192.168.3.116:4999, sid=75fbf19d 73f20fdc
0#Sat Sep 02 12:53:10 2023 us=363726 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
0#Sat Sep 02 12:53:10 2023 us=363726 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV06VM_SERVER, name=HANNIBALDEV06VM, emailAddress=paul.hindenburg@mailinator.com\09
0#Sat Sep 02 12:53:10 2023 us=551235 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
0#Sat Sep 02 12:53:10 2023 us=551235 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
0#Sat Sep 02 12:53:10 2023 us=551235 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
0#Sat Sep 02 12:53:10 2023 us=551235 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
0#Sat Sep 02 12:53:10 2023 us=551235 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
0#Sat Sep 02 12:53:10 2023 us=551235 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
0#Sat Sep 02 12:53:10 2023 us=551235 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
0#Sat Sep 02 12:53:10 2023 us=551235 [HANNIBALDEV06VM_SERVER] Peer Connection Initiated with [AF_INET]192.168.3.116:4999
0#Sat Sep 02 12:53:13 2023 us=20060 SENT CONTROL [HANNIBALDEV06VM_SERVER]: 'PUSH_REQUEST' (status=1)
0#Sat Sep 02 12:53:13 2023 us=176313 PUSH: Received control message: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1'
0#Sat Sep 02 12:53:13 2023 us=176313 OPTIONS IMPORT: timers and/or timeouts modified
0#Sat Sep 02 12:53:13 2023 us=176313 OPTIONS IMPORT: --ifconfig/up options modified
0#Sat Sep 02 12:53:13 2023 us=176313 do_ifconfig, tt->ipv6=0, tt->did_ifconfig_ipv6_setup=0
0#Sat Sep 02 12:53:13 2023 us=176313 ******** NOTE:  Please manually set the IP/netmask of 'OpenVPN2' to 10.0.0.2/255.255.255.252 (if it is not already set)
0#Sat Sep 02 12:53:13 2023 us=176313 open_tun, tt->ipv6=0
0#Sat Sep 02 12:53:13 2023 us=176313 TAP-WIN32 device [OpenVPN2] opened: \\.\Global\{9B00449E-0F90-4137-A063-CEA05D846AD8}.tap
0#Sat Sep 02 12:53:13 2023 us=176313 TAP-Windows Driver Version 9.9 
0#Sat Sep 02 12:53:13 2023 us=176313 TAP-Windows MTU=1500
0#Sat Sep 02 12:53:13 2023 us=176313 Sleeping for 3 seconds...
2023-9-2_12_53: Thread # 0 not jet ended or has reported an error.
0#Sat Sep 02 12:53:16 2023 us=176370 Successful ARP Flush on interface [4] {9B00449E-0F90-4137-A063-CEA05D846AD8}
0#Sat Sep 02 12:53:17 2023 us=410769 TEST ROUTES: 0/0 succeeded len=0 ret=0 a=0 u/d=down
0#Sat Sep 02 12:53:17 2023 us=410769 Route: Waiting for TUN/TAP interface to come up...
0#Sat Sep 02 12:53:18 2023 us=645168 TEST ROUTES: 0/0 succeeded len=0 ret=1 a=0 u/d=up
0#Sat Sep 02 12:53:18 2023 us=645168 WARNING: this configuration may cache passwords in memory -- use the auth-nocache option to prevent this
0#Sat Sep 02 12:53:18 2023 us=645168 Initialization Sequence Completed
					 
	 *  
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 03.09.2023, 07:35:31
	 */
	public void writeOutputToLog() throws ExceptionZZZ{	
		main:{
			try{
			check:{
				if(this.objProcess==null){
					ExceptionZZZ ez = new ExceptionZZZ("Process-Object", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
			}//END check:
		
			BufferedReader in = new BufferedReader( new InputStreamReader(objProcess.getInputStream()) );
				for ( String s; (s = in.readLine()) != null; ){
				    //System.out.println( s );
					this.getLogObject().WriteLine(this.getNumber() +"#"+ s);
					this.setFlag("hasOutput", true);
					
					if(StringZZZ.contains(s,"TCP connection established with")) {
						this.setFlag("hasConnection", true);
					}
					
					Thread.sleep(20);					
					if( this.getFlag("stoprequested")==true) break main;
				}								
			} catch (IOException e) {
				ExceptionZZZ ez = new ExceptionZZZ("IOException happend: '" + e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			} catch (InterruptedException e) {
				ExceptionZZZ ez = new ExceptionZZZ("InterruptedException happend: '"+ e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
		}//END main:
	}
	
	/**TODO Ich weiss noch garnicht, was ich senden soll und wie es gehen kann.
	 * Ziel w�re es z.B. mit der Test F4 den Process herunterzufahren.
	 * @param sOut
	 * @throws ExceptionZZZ, 
	 *
	 * @return void
	 *
	 * javadoc created by: 0823, 07.07.2006 - 17:29:31
	 */
	public void sendStringToProcess(String sOut) throws ExceptionZZZ{
		main:{
			try{
			check:{
				if(this.objProcess==null){
					ExceptionZZZ ez = new ExceptionZZZ("Process-Object", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				if(sOut==null)  break main; //Absichltich keine Exception					
			}//END check:
		
			BufferedWriter out = new BufferedWriter( new OutputStreamWriter(objProcess.getOutputStream()) );
			out.write(sOut);
		
			this.getLogObject().WriteLineDate(this.getNumber() +"# STRING SEND TO PROCESS: "+ sOut);
			this.setFlag("hasInput", true);
			
			} catch (IOException e) {
				ExceptionZZZ ez = new ExceptionZZZ("IOException happend: '" + e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
		}//END main:
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
			if(stemp.equals("haserror")){
				bFunction = bFlagHasError;
				break main;
			}else if(stemp.equals("hasconnection")) {
				bFunction = bFlagHasConnection;
				break main;
			}else if(stemp.equals("hasoutput")){
				bFunction = bFlagHasOutput;
				break main;
			}else if(stemp.equals("hasinput")){
				bFunction = bFlagHasInput;
				break main;
			}else if(stemp.equals("stoprequested")){
				bFunction = bFlagStopRequested;
				break main;
			}
		}//end main:
		return bFunction;
	}

	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 	- hasError
	- hasOutput
	- hasInput
	- stoprequested
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
		if(stemp.equals("haserror")){
			bFlagHasError = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("hasconnection")) {
			bFlagHasConnection = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("hasoutput")){
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

		}//end main:
		return bFunction;
	}
	
	//###### GETTER / SETTER
	/**Returns the process - object passed as a parameter of the constructor 
	 * Hint: Therefore is no setter-method available
	 * @return Process
	 *
	 * javadoc created by: 0823, 06.07.2006 - 16:48:57
	 */
	public Process getProcessObject(){
		return this.objProcess;
	}
	
	/**Returns a number passed as a parameter of the constructor
	 * This number should allow the object to identify itself. E.g. when writing to the log.
	 * @return int
	 *
	 * javadoc created by: 0823, 06.07.2006 - 17:52:34
	 */
	public int getNumber(){
		return this.iNumber;
	}

	//### aus IEventBrokerStatusLocalSetUserOVPN
	@Override
	public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
		return this.objEventStatusLocalBroker;
	}

	@Override
	public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
		this.objEventStatusLocalBroker = objEventSender;
	}

	@Override
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}
}//END class
