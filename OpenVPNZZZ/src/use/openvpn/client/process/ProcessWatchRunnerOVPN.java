package use.openvpn.client.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.moduleExternal.process.watch.AbstractProcessWatchRunnerZZZ;
import basic.zBasic.util.moduleExternal.process.watch.IProcessWatchRunnerZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.status.IEventObjectStatusBasicZZZ;
import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import basic.zKernel.status.IListenerObjectStatusBasicZZZ;
import basic.zKernel.status.ISenderObjectStatusBasicZZZ;
import use.openvpn.client.ClientConfigStarterOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN;
import use.openvpn.client.status.EventObject4ProcessWatchRunnerStatusLocalOVPN;
import use.openvpn.client.status.EventObject4ProcessWatchStatusLocalOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IEventObject4ProcessWatchRunnerStatusLocalOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalOVPN;
import use.openvpn.client.status.SenderObjectStatusLocalOVPN;
import use.openvpn.client.process.IProcessWatchRunnerOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN.STATUSLOCAL;

/**This class receives the stream from a process, which was started by the ConfigStarterZZZ class.
 * This is necessary, because the process will only goon working, if the streams were "catched" by a target.
 * This "catching" will be done in a special thread (one Thread per process).  
 * @author 0823
 *
 */
public class ProcessWatchRunnerOVPN extends AbstractProcessWatchRunnerZZZ implements IProcessWatchRunnerOVPN, IEventBrokerStatusLocalSetUserOVPN{	
	private ISenderObjectStatusLocalOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	private ClientMainOVPN objMain = null;
	private ClientConfigStarterOVPN objClientConfigStarter = null; //Das Konfigurationsobjekt, dem der Start zugrundeliegt.

	public ProcessWatchRunnerOVPN(IKernelZZZ objKernel, Process objProcess, int iNumber) throws ExceptionZZZ{
		super(objKernel, objProcess, iNumber);
	}
	public ProcessWatchRunnerOVPN(IKernelZZZ objKernel, Process objProcess, int iNumber, String sFlag) throws ExceptionZZZ{
		super(objKernel, objProcess, iNumber, sFlag);
	}
	public ProcessWatchRunnerOVPN(IKernelZZZ objKernel, Process objProcess, int iNumber, String[] saFlag) throws ExceptionZZZ{
		super(objKernel, objProcess, iNumber, saFlag);
	}
	
	//### Speziel für OVPN
	@Override
	public ClientConfigStarterOVPN getClientConfigStarterObject() {
		return this.objClientConfigStarter;
	}
	@Override
	public void setClientConfigStarterObject(ClientConfigStarterOVPN objStarter) {
		this.objClientConfigStarter = objStarter;
	}
	
	//#####################################
	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner started for Process #"+ this.getNumber();
				System.out.println(sLog);
				this.logLineDate(sLog);
				
				//Solange laufen, bis ein Fehler auftritt oder eine Verbindung erkannt wird.
				do{
					//System.out.println("FGLTEST01");
					//Wichtig: Man muss wohl zuallererst den InputStream abgreifen, damit der Process weiterlaufen kann.
					this.writeOutputToLogPLUSanalyse();				
					boolean bHasConnection = this.getStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
					if(bHasConnection) {
						sLog = "Connection wurde erstellt. Beende ProcessWatchRunner #"+this.getNumber();
						this.logLineDate(sLog);						
						
						//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
						//Dann erzeuge den Event und feuer ihn ab.
						//Merke: Nun aber ueber das enum			
						if(this.objEventStatusLocalBroker!=null) {
							//IEventObjectStatusLocalSetZZZ event = new EventObjectStatusLocalSetZZZ(this,1,ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
							//TODOGOON20230914: Woher kommt nun das Enum? Es gibt ja kein konkretes Beispiel
							IEventObjectStatusLocalOVPN event = new EventObject4ProcessWatchStatusLocalOVPN(this,1,(IProcessWatchRunnerOVPN.STATUSLOCAL)null, true);
							event.setClientConfigStarterObjectUsed(this.getClientConfigStarterObject());
							this.objEventStatusLocalBroker.fireEvent(event);
						}		
						
						Thread.sleep(20);
						boolean bStopRequested = this.getFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP);//Merke: STOPREQUEST ist eine Anweisung.. bleibt also ein Flag und ist kein Status
						if( bStopRequested) break main;
						
						
						
					}
					
					
					//System.out.println("FGLTEST02");
					this.writeErrorToLog();				
					boolean bError = this.getStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
					if(bError) break;
	
					//Nach irgendeiner Ausgabe enden ist hier falsch, in einer abstrakten Klasse vielleicht richtig, quasi als Muster.
					//if(this.getFlag("hasOutput")) break;
					//System.out.println("FGLTEST03");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						ExceptionZZZ ez = new ExceptionZZZ("An InterruptedException happened: '" + e.getMessage() + "''", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					boolean bStopRequested = this.getFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP);//Merke: Das ist eine Anweisung und kein Status. Darum bleibt es beim Flag.
					if(bStopRequested) break;					
			}while(true);
			this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED,true);
			this.getLogObject().WriteLineDate("ProcessWatchRunner #"+ this.getNumber() + " ended.");
		
		
			bReturn = true;
		}//END main
		return bReturn;
	}
	
	public void setClientBackendObject(IClientMainOVPN objClientBackend){
		this.objMain = (ClientMainOVPN) objClientBackend;
	}
	public ClientMainOVPN getClientBackendObject(){
		return this.objMain;
	}
		
	//###### FLAGS
	//#######################################
	@Override
	public boolean getFlag(IProcessWatchRunnerOVPN.FLAGZ objEnumFlag) {
		return this.getFlag(objEnumFlag.name());
	}

	@Override
	public boolean setFlag(IProcessWatchRunnerOVPN.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
		return this.setFlag(objEnumFlag.name(), bFlagValue);
	}

	@Override
	public boolean[] setFlag(IProcessWatchRunnerOVPN.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
		boolean[] baReturn=null;
		main:{
			if(!ArrayUtilZZZ.isEmpty(objaEnumFlag)) {
				baReturn = new boolean[objaEnumFlag.length];
				int iCounter=-1;
				for(IProcessWatchRunnerOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
					iCounter++;
					boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
					baReturn[iCounter]=bReturn;
				}
				
				//!!! Ein mögliches init-Flag ist beim direkten setzen der Flags unlogisch.
				//    Es wird entfernt.
				this.setFlag(IFlagZUserZZZ.FLAGZ.INIT, false);
			}
		}//end main:
		return baReturn;
	}

	@Override
	public boolean proofFlagExists(IProcessWatchRunnerOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
		return this.proofFlagExists(objEnumFlag.name());
	}

	@Override
	public boolean proofFlagSetBefore(IProcessWatchRunnerOVPN.FLAGZ objEnumFlag)	throws ExceptionZZZ {
		return this.proofFlagSetBefore(objEnumFlag.name());
	}		

	
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
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("haserror")){
//				bFunction = bFlagHasError;
//				break main;
//			}else if(stemp.equals("hasconnection")) {
//				bFunction = bFlagHasConnection;
//				break main;
//			}
			
		}//end main:
		return bFunction;
	}

	/**
	 * @see AbstractKernelUseObjectZZZ.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
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
//		String stemp = sFlagName.toLowerCase();
//		if(stemp.equals("haserror")){
//			bFlagHasError = bFlagValue;
//			bFunction = true;
//			break main;
//		}else if(stemp.equals("hasconnection")) {
//			bFlagHasConnection = bFlagValue;
//			bFunction = true;
//			break main;
//		}

		}//end main:
		return bFunction;
	}

	//+++ aus IProcessWatchRunnerZZZ
	
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
	@Override
	public boolean analyseInputLineCustom(String sLine) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			int iProcess = this.getNumber();
			String sLog = ReflectCodeZZZ.getPositionCurrent() +  " Process#" + iProcess + ": sLine=" + sLine;		
			System.out.println(sLog);
			this.logLineDate(sLog);
			if(StringZZZ.contains(sLine,"TCP connection established")) {
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, false);
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, true);
				
				//Falls ein Abbruch nach der Verbindung gewuenscht wird, dies hier tun
				boolean bEndOnConnection = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTION);
				if(bEndOnConnection) {
					//Den Process selbst an dieser Stelle nicht beenden, sondern nur ein Flag setzten, auf das reagiert werden kann.
					boolean bStopRequested = this.setFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP, true);//Merke: STOPREQUEST ist eine Anweisung.. bleibt also ein Flag und ist kein Status
					
				}
				
				bReturn = true;
				break main;
			}else if(StringZZZ.contains(sLine,"Connection reset, restarting")) {
				//Beim Verbindungsverlust:
				//Sun Oct 29 07:35:45 2023 us=949123 Connection reset, restarting [-1]
				//Sun Oct 29 07:35:45 2023 us=949123 TCP/UDP: Closing socket
				//Sun Oct 29 07:35:45 2023 us=949123 SIGUSR1[soft,connection-reset] received, process restarting
				//Sun Oct 29 07:35:45 2023 us=949123 Restart pause, 5 second(s)
				//...
				//Sun Oct 29 07:35:50 2023 us=948995 Attempting to establish TCP connection with [AF_INET]192.168.3.116:4999 [nonblock]
				//Sun Oct 29 07:36:00 2023 us=948860 TCP: connect to [AF_INET]192.168.3.116:4999 failed, will try again in 5 seconds: Connection timed out (WSAETIMEDOUT)
				//Sun Oct 29 07:36:15 2023 us=949340 TCP: connect to [AF_INET]192.168.3.116:4999 failed, will try again in 5 seconds: Connection timed out (WSAETIMEDOUT)
				System.out.println(("TESTFGL PROCESS STRING ANALYSE 01: " + sLine));
				
			//TODOGOON20231106;//Debugge, warum der false - Wert nicht weitergegeben wird an Main
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, false);				
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, true);
				
				//Falls ein Abbruch nach der Verbindung gewuenscht wird, dies hier tun
				boolean bEndOnConnectionLost = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTIONLOST);
				if(bEndOnConnectionLost) {
					//Den Process selbst an dieser Stelle nicht beenden, sondern nur ein Flag setzten, auf das reagiert werden kann.
					boolean bStopRequested = this.setFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP, true);//Merke: STOPREQUEST ist eine Anweisung.. bleibt also ein Flag und ist kein Status
					
				}
				
				bReturn = true;
				break main;
			}else if(StringZZZ.contains(sLine,"Peer Connection Initiated with")) {
				//Nach Verbindungsverlust neu verbinden:
				//[HANNIBALDEV06VM_SERVER] Peer Connection Initiated with [AF_INET]192.168.3.116:4999
				System.out.println(("TESTFGL PROCESS STRING ANALYSE 02: " + sLine));
											
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, false);
				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, true);
				
				//Falls ein Abbruch nach der Verbindung gewuenscht worden waere, dies hier wieder rueckgaengig machen
				//Idee dahinter: Der Verbindungsverlust war so kurzfristig, der STOPREQUEST hat noch garnicht gezogen.
				boolean bEndOnConnectionLost = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.END_ON_CONNECTIONLOST);
				if(bEndOnConnectionLost) {
					//Den Process selbst an dieser Stelle nicht beenden, sondern nur ein Flag setzten, auf das reagiert werden kann.
					boolean bStopRequested = this.setFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP, false);//Merke: STOPREQUEST ist eine Anweisung.. bleibt also ein Flag und ist kein Status
					
				}
				
				bReturn = true;
				break main;
			}
		}//end main:
		return bReturn;
	}
	
	//###### GETTER / SETTER
	//sind eher in der Abstrakten Klasse
	
	//++++++ StatusLocal
		@Override
		public boolean getStatusLocal(Enum objEnumStatusIn) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(objEnumStatusIn==null) {
					break main;
				}
				
				//Merke: Bei einer anderen Klasse, die dieses DesingPattern nutzt, befindet sich der STATUSLOCAL in einer anderen Klasse
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) objEnumStatusIn;
				String sStatusName = enumStatus.name();
				if(StringZZZ.isEmpty(sStatusName)) break main;
											
				HashMap<String, Boolean> hmFlag = this.getHashMapStatusLocal();
				Boolean objBoolean = hmFlag.get(sStatusName.toUpperCase());
				if(objBoolean==null){
					bFunction = false;
				}else{
					bFunction = objBoolean.booleanValue();
				}
								
			}	// end main:
			
			return bFunction;	
		}
		

		/* (non-Javadoc)
		 * @see basic.zBasic.AbstractObjectWithStatusZZZ#setStatusLocal(java.lang.Enum, boolean)
		 */
		@Override 
		public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
								
				bFunction = this.offerStatusLocal(enumStatus, bStatusValue, null);
			}//end main:
			return bFunction;
		}
		
		@Override
		public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
			
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
			
			    bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, null, bStatusValue);
			    
			}//end main
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, bStatusValue, null);
			}//end main:
			return bReturn;
		}
		
		@Override 
		public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		
		//################################################
		//+++ aus IStatusLocalUserMessageZZZ			
		@Override 
		public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sMessage) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal(enumStatus, bStatusValue, sMessage);
			}//end main:
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sMessage, bStatusValue);
			}//end main:
			return bFunction;
		}
		
		@Override 
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue, String sMessage) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, bStatusValue, sMessage);
			}//end main:
			return bReturn;
		}				
		
		@Override 
		public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(iIndexOfProcess, enumStatus, null, bStatusValue);
			}//end main:
			return bReturn;
		}
		
		//++++++++++++++++++++++++++++++++++++++++++++++++
		@Override
		public boolean offerStatusLocal(Enum enumStatusIn, boolean bStatusValue, String sStatusMessage) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
			
			    bFunction = this.offerStatusLocal_(-1, enumStatus, sStatusMessage, bStatusValue);
			    
			}//end main
			return bFunction;
		}
		
		@Override
		public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
			
			    bFunction = this.offerStatusLocal_(iIndexOfProcess, enumStatus, sStatusMessage, bStatusValue);
			    
			}//end main
			return bFunction;
		}
		
		private boolean offerStatusLocal_(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) break main;
							
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				String sStatusName = enumStatus.name();
				bFunction = this.proofStatusLocalExists(sStatusName);															
				if(!bFunction){
					String sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner would like to fire event, but this status is not available: '" + sStatusName + "'";					
					this.logProtocolString(sLog);			
					break main;				
				}
					
				bFunction = this.proofStatusLocalValue(sStatusName, bStatusValue);
				if(!bFunction) {
					String sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner would like to fire event, but this status has not changed: '" + sStatusName + "'";					
					this.logProtocolString(sLog);
					break main;
				}
				
				
				
				//++++++++++++++++++++	
				//Setze den Status nun in die HashMap
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
				
				//Den enumStatus als currentStatus im Objekt speichern...
				//                   dito mit dem "vorherigen Status"
				//Setze nun das Enum, und damit auch die Default-StatusMessage
				String sStatusMessageToSet = null;
				if(StringZZZ.isEmpty(sStatusMessage)){
					if(bStatusValue) {
						sStatusMessageToSet = enumStatus.getStatusMessage();
					}else {
						sStatusMessageToSet = "NICHT " + enumStatus.getStatusMessage();
					}			
				}else {
					sStatusMessageToSet = sStatusMessage;
				}	
				String sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner verarbeite sStatusMessageToSet='" + sStatusMessageToSet + "'";
				this.logProtocolString(sLog);

				//Falls eine Message extra uebergeben worden ist, ueberschreibe...
				if(sStatusMessage!=null) {
					sStatusMessageToSet = sStatusMessage;
					sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner uebersteuere sStatusMessageToSet='" + sStatusMessage + "'";					
					this.logProtocolString(sLog);				
				}
				//Merke: Dabei wird die uebergebene Message in den speziellen "Ringspeicher" geschrieben, auch NULL Werte...
				this.offerStatusLocalEnum(enumStatus, bStatusValue, sStatusMessageToSet);
												
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.			
				if(this.getSenderStatusLocalUsed()==null) {
					sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner for Process would like to fire event '" + enumStatus.getAbbreviation() + "', but no objEventStatusLocalBroker available, any registered?";
					this.logProtocolString(sLog);		
					break main;
				}
				
				//Erzeuge fuer das Enum einen eigenen Event. Die daran registrierten Klassen koennen in einer HashMap definieren, ob der Event fuer sie interessant ist.		
				sLog = ReflectCodeZZZ.getPositionCurrent() + ": Erzeuge Event fuer '" + sStatusName + "', bValue='"+ bStatusValue + "', sMessage='"+sStatusMessage+"'";				
				this.logProtocolString(sLog);
				IEventObject4ProcessWatchRunnerStatusLocalOVPN event = new EventObject4ProcessWatchRunnerStatusLocalOVPN(this,1,enumStatus, bStatusValue);			
				event.setApplicationObjectUsed(this.getClientBackendObject().getApplicationObject());
				
				//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
				event.setClientConfigStarterObjectUsed(this.getClientConfigStarterObject());
				
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner for Process #"+ this.getNumber() + " fires event '" + enumStatus.getAbbreviation() + "'";
				this.logProtocolString(sLog);
				this.getSenderStatusLocalUsed().fireEvent(event);
						
				bFunction = true;								
			}// end main:
		return bFunction;
		}

		
		@Override
		public ISenderObjectStatusLocalOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
			if(this.objEventStatusLocalBroker==null) {
				//++++++++++++++++++++++++++++++
				//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
				ISenderObjectStatusLocalOVPN objSenderStatusLocal = new SenderObjectStatusLocalOVPN();
				this.objEventStatusLocalBroker = objSenderStatusLocal;
			}
			return this.objEventStatusLocalBroker;
		}

		@Override
		public void setSenderStatusLocalUsed(ISenderObjectStatusLocalOVPN objEventSender) {
			this.objEventStatusLocalBroker = objEventSender;
		}

		@Override
		public void registerForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().addListenerObjectStatusLocal(objEventListener);
		}

		@Override
		public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().removeListenerObjectStatusLocal(objEventListener);
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
		public void writeOutputToLogPLUSanalyse() throws ExceptionZZZ{	
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
						this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASOUTPUT, true);
						
						boolean bAny = this.analyseInputLineCustom(s);
										
						Thread.sleep(20);
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
		
		@Override
		public boolean writeErrorToLogWithStatus() throws ExceptionZZZ{
			boolean bReturn = false;
			main:{			
				try{
					bReturn = this.writeErrorToLog();
					if(!bReturn)break main;
					
				    this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR, true);
				    Thread.sleep(20);			

				} catch (InterruptedException e) {
					ExceptionZZZ ez = new ExceptionZZZ("InterruptedException happend: '"+ e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				bReturn = true;
			}//END Main:	
			return bReturn;
		}
		
		//########################
		//#######################################		
		@Override
		public boolean isStatusLocalRelevant(IEnumSetMappedStatusZZZ objEnum) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				//Merke: enumStatus hat class='class use.openvpn.client.process.IProcessWatchRunnerOVPN$STATUSLOCAL'				
				if(!(objEnum instanceof IProcessWatchRunnerOVPN.STATUSLOCAL) ){
					String sLog = ReflectCodeZZZ.getPositionCurrent()+": enumStatus wird wg. unpassender Klasse ignoriert.";
					System.out.println(sLog);
					//this.objMain.logMessageString(sLog);
					break main;
				}		
				bReturn = true;

			}//end main:
			return bReturn;
		}
}//END class
