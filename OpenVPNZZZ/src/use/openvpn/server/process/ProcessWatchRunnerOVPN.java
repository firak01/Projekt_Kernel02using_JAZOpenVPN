package use.openvpn.server.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.moduleExternal.process.watch.AbstractProcessWatchRunnerZZZ;
import basic.zBasic.util.moduleExternal.process.watch.IProcessWatchRunnerZZZ;
import basic.zBasic.util.moduleExternal.process.watch.ProcessWatchRunnerZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.status.IEventObjectStatusLocalZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalZZZ;
import use.openvpn.server.ServerConfigStarterOVPN;
import use.openvpn.server.ServerMainOVPN;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.process.ClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadVpnIpPingerOVPN;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN.STATUSLOCAL;
import use.openvpn.server.IServerMainOVPN;
import use.openvpn.server.status.EventObject4ProcessWatchRunnerStatusLocalSetOVPN;
import use.openvpn.server.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.server.status.IEventObject4ProcessWatchMonitorStatusLocalSetOVPN;
import use.openvpn.server.status.IEventObject4ProcessWatchRunnerStatusLocalSetOVPN;
import use.openvpn.server.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.server.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.server.status.SenderObjectStatusLocalSetOVPN;

/**This class receives the stream from a process, which was started by the ConfigStarterZZZ class.
 * This is necessary, because the process will only goon working, if the streams were "catched" by a target.
 * This "catching" will be done in a special thread (one Thread per process).  
 * @author 0823
 *
 */
public class ProcessWatchRunnerOVPN extends AbstractProcessWatchRunnerZZZ implements IProcessWatchRunnerOVPN, IEventBrokerStatusLocalSetUserOVPN{	
	private ISenderObjectStatusLocalSetOVPN objEventStatusLocalBroker=null;//Das Broker Objekt, an dem sich andere Objekte regristrieren können, um ueber Aenderung eines StatusLocal per Event informiert zu werden.
	private ServerMainOVPN objMain = null;
	private ServerConfigStarterOVPN objServerConfigStarter = null; //Das Konfigurationsobjekt, dem der Start zugrundeliegt.

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
	public ServerConfigStarterOVPN getServerConfigStarterObject() {
		return this.objServerConfigStarter;
	}
	@Override
	public void setServerConfigStarterObject(ServerConfigStarterOVPN objStarter) {
		this.objServerConfigStarter = objStarter;
	}
	
	//#####################################
	public boolean start() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			String sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner started for Process #"+ this.getNumber();
			System.out.println(sLog);
			this.logLineDate(sLog);
			
			//Merke 20240127: Wenn das funktioniert nur wenn der Prozess auch Ausgaben in den Standard.Out schreibt.
			//                Der OVPN server macht das nicht. Daher kann hier maximal die porzessstartende Batch und ihre Ausgabe beobachtet werden.
			
			//Solange laufen, bis ein Fehler auftritt oder eine Verbindung erkannt wird.
			do{
				//System.out.println("FGLTEST01");
				//Wichtig: Man muss wohl zuallererst den InputStream abgreifen, damit der Process weiterlaufen kann.
				this.writeOutputToLogPLUSanalyse();				
				boolean bHasConnection = this.getStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
				if(bHasConnection) {
					sLog = "ProcessWatchRunner #"+this.getNumber()+"# Connection wurde erstellt.";
					this.logLineDate(sLog);						
					
					//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
					//Dann erzeuge den Event und feuer ihn ab.
					//Merke: Nun aber ueber das enum			
					if(this.objEventStatusLocalBroker!=null) {
						//IEventObjectStatusLocalSetZZZ event = new EventObjectStatusLocalSetZZZ(this,1,ClientMainOVPN.STATUSLOCAL.ISCONNECTED, true);
						//TODOGOON20230914: Woher kommt nun das Enum? Es gibt ja kein konkretes Beispiel
						IEventObjectStatusLocalSetOVPN event = new EventObject4ProcessWatchRunnerStatusLocalSetOVPN(this,1,(IProcessWatchRunnerOVPN.STATUSLOCAL)null, true);
						event.setServerConfigStarterObjectUsed(this.getServerConfigStarterObject());
						this.objEventStatusLocalBroker.fireEvent(event);
					}		
					
					Thread.sleep(20);
					boolean bStopRequested = this.getFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP);//Merke: STOPREQUEST ist eine Anweisung.. bleibt also ein Flag und ist kein Status
					if( bStopRequested) {
						sLog = "ProcessWatchRunner #"+this.getNumber() + "# Breche Schleife ab.";
						this.logLineDate(sLog);
						break main;
					}
				}
				
				
				//System.out.println("FGLTEST02");
				this.writeErrorToLog();				
				boolean bError = this.getStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASERROR);
				if(bError) break;
	
				//Nach irgendeiner Ausgabe enden ist hier falsch, in einer abstrakten Klasse vielleicht richtig, quasi als Muster.
				//if(this.getFlag("hasOutput")) break;
				//System.out.println("FGLTEST03");
				
				Thread.sleep(10);
				
				boolean bStopRequested = this.getFlag(IProgramRunnableZZZ.FLAGZ.REQUEST_STOP);//Merke: Das ist eine Anweisung und kein Status. Darum bleibt es beim Flag.
				if(bStopRequested) break;					
		}while(true);
		bReturn = true;
			
		this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.ISSTOPPED,true);
		this.getLogObject().WriteLineDate("ProcessWatchRunner #"+ this.getNumber() + " ended.");
		
		}//END main
		return bReturn;
	}
	
	public void setServerBackendObject(IServerMainOVPN objServerBackend){
		this.objMain = (ServerMainOVPN) objServerBackend;
	}
	public ServerMainOVPN getServerBackendObject(){
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
			   
Sat Nov 25 23:14:09 2023 us=715775 Current Parameter Settings:
Sat Nov 25 23:14:09 2023 us=715775   config = 'C:\Programme\OpenVPN\config\server_TCP_4999.ovpn'
Sat Nov 25 23:14:09 2023 us=715775   mode = 1
Sat Nov 25 23:14:09 2023 us=715775   show_ciphers = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   show_digests = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   show_engines = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   genkey = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   key_pass_file = '[UNDEF]'
Sat Nov 25 23:14:09 2023 us=715775   show_tls_ciphers = DISABLED
Sat Nov 25 23:14:09 2023 us=715775 Connection profiles [default]:
Sat Nov 25 23:14:09 2023 us=715775   proto = tcp-server
Sat Nov 25 23:14:09 2023 us=715775   local = '[UNDEF]'
Sat Nov 25 23:14:09 2023 us=715775   local_port = 4999
Sat Nov 25 23:14:09 2023 us=715775   remote = '[UNDEF]'
Sat Nov 25 23:14:09 2023 us=715775   remote_port = 4999
Sat Nov 25 23:14:09 2023 us=715775   remote_float = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   bind_defined = DISABLED
Sat Nov 25 23:14:09 2023 us=715775   bind_local = ENABLED
Sat Nov 25 23:14:09 2023 us=715775   connect_retry_seconds = 5
Sat Nov 25 23:14:09 2023 us=715775   connect_timeout = 10
Sat Nov 25 23:14:09 2023 us=715775 NOTE: --mute triggered...
Sat Nov 25 23:14:09 2023 us=715775 270 variation(s) on previous 20 message(s) suppressed by --mute
Sat Nov 25 23:14:09 2023 us=715775 OpenVPN 2.3.18 i686-w64-mingw32 [SSL (OpenSSL)] [LZO] [PKCS11] [IPv6] built on Sep 26 2017
Sat Nov 25 23:14:09 2023 us=715775 Windows version 5.1 (Windows XP) 32bit
Sat Nov 25 23:14:09 2023 us=715775 library versions: OpenSSL 1.0.2l  25 May 2017, LZO 2.10
Sat Nov 25 23:14:09 2023 us=809504 Diffie-Hellman initialized with 1024 bit key
Sat Nov 25 23:14:09 2023 us=809504 TLS-Auth MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sat Nov 25 23:14:09 2023 us=825125 Socket Buffers: R=[8192->8192] S=[8192->8192]
Sat Nov 25 23:14:09 2023 us=825125 do_ifconfig, tt->ipv6=0, tt->did_ifconfig_ipv6_setup=0
Sat Nov 25 23:14:09 2023 us=825125 ******** NOTE:  Please manually set the IP/netmask of 'OpenVPN1' to 10.0.0.1/255.255.255.252 (if it is not already set)
Sat Nov 25 23:14:09 2023 us=825125 open_tun, tt->ipv6=0
Sat Nov 25 23:14:09 2023 us=825125 TAP-WIN32 device [OpenVPN1] opened: \\.\Global\{1BB99DCA-6322-41E5-9CD1-1DBD6B1EBFF1}.tap
Sat Nov 25 23:14:09 2023 us=825125 TAP-Windows Driver Version 9.9 
Sat Nov 25 23:14:09 2023 us=825125 TAP-Windows MTU=1500
Sat Nov 25 23:14:09 2023 us=825125 Sleeping for 10 seconds...
Sat Nov 25 23:14:19 2023 us=822821 Successful ARP Flush on interface [4] {1BB99DCA-6322-41E5-9CD1-1DBD6B1EBFF1}
Sat Nov 25 23:14:19 2023 us=822821 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sat Nov 25 23:14:19 2023 us=822821 Listening for incoming TCP connection on [undef]
Sat Nov 25 23:14:19 2023 us=822821 TCPv4_SERVER link local (bound): [undef]
Sat Nov 25 23:14:19 2023 us=822821 TCPv4_SERVER link remote: [undef]
Sat Nov 25 23:14:19 2023 us=822821 MULTI: multi_init called, r=256 v=256
Sat Nov 25 23:14:19 2023 us=822821 MULTI: TCP INIT maxclients=1 maxevents=5
Sat Nov 25 23:14:19 2023 us=822821 Initialization Sequence Completed
Sat Nov 25 23:14:29 2023 us=70690 MULTI: multi_create_instance called
Sat Nov 25 23:14:29 2023 us=70690 Re-using SSL/TLS context
Sat Nov 25 23:14:29 2023 us=70690 LZO compression initialized
Sat Nov 25 23:14:29 2023 us=70690 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sat Nov 25 23:14:29 2023 us=70690 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sat Nov 25 23:14:29 2023 us=70690 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sat Nov 25 23:14:29 2023 us=70690 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sat Nov 25 23:14:29 2023 us=70690 Local Options hash (VER=V4): 'c0103fa8'
Sat Nov 25 23:14:29 2023 us=70690 Expected Remote Options hash (VER=V4): '69109d17'
Sat Nov 25 23:14:29 2023 us=70690 TCP connection established with [AF_INET]192.168.3.179:3888
Sat Nov 25 23:14:29 2023 us=70690 TCPv4_SERVER link local: [undef]
Sat Nov 25 23:14:29 2023 us=70690 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:3888
Sat Nov 25 23:14:30 2023 us=70460 192.168.3.179:3888 TLS: Initial packet from [AF_INET]192.168.3.179:3888, sid=4cbeb862 096bdf12
Sat Nov 25 23:14:30 2023 us=226674 192.168.3.179:3888 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sat Nov 25 23:14:30 2023 us=226674 192.168.3.179:3888 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sat Nov 25 23:14:30 2023 us=414130 192.168.3.179:3888 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:3888
Sat Nov 25 23:14:30 2023 us=414130 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sat Nov 25 23:14:30 2023 us=414130 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:3888
Sat Nov 25 23:14:30 2023 us=414130 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:3888: 10.0.0.2
Sat Nov 25 23:14:32 2023 us=538641 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 PUSH: Received control message: 'PUSH_REQUEST'
Sat Nov 25 23:14:32 2023 us=538641 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 send_push_reply(): safe_cap=940
Sat Nov 25 23:14:32 2023 us=538641 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)
Sun Nov 26 08:07:32 2023 us=690875 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 Connection reset, restarting [-1]
Sun Nov 26 08:07:32 2023 us=690875 HANNIBALDEV04VM_CLIENT/192.168.3.179:3888 SIGUSR1[soft,connection-reset] received, client-instance restarting
Sun Nov 26 08:07:32 2023 us=706500 TCP/UDP: Closing socket
Sun Nov 26 08:07:35 2023 us=612750 MULTI: multi_create_instance called
Sun Nov 26 08:07:35 2023 us=612750 Re-using SSL/TLS context
Sun Nov 26 08:07:35 2023 us=612750 LZO compression initialized
Sun Nov 26 08:07:35 2023 us=612750 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 26 08:07:35 2023 us=612750 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 26 08:07:35 2023 us=612750 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sun Nov 26 08:07:35 2023 us=612750 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sun Nov 26 08:07:35 2023 us=612750 Local Options hash (VER=V4): 'c0103fa8'
Sun Nov 26 08:07:35 2023 us=612750 Expected Remote Options hash (VER=V4): '69109d17'
Sun Nov 26 08:07:35 2023 us=612750 hTCP connection established wit [AF_INET]192.168.3.179:3937
Sun Nov 26 08:07:35 2023 us=612750 TCPv4_SERVER link local: [undef]
Sun Nov 26 08:07:35 2023 us=612750 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:3937
Sun Nov 26 08:07:36 2023 us=612750 192.168.3.179:3937 TLS: Initial packet from [AF_INET]192.168.3.179:3937, sid=2931882e 813014ac
Sun Nov 26 08:07:36 2023 us=722125 192.168.3.179:3937 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 26 08:07:36 2023 us=722125 192.168.3.179:3937 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sun Nov 26 08:07:36 2023 us=847125 192.168.3.179:3937 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:3937
Sun Nov 26 08:07:36 2023 us=847125 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sun Nov 26 08:07:36 2023 us=847125 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:3937
Sun Nov 26 08:07:36 2023 us=847125 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:3937: 10.0.0.2
Sun Nov 26 08:07:39 2023 us=253375 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 PUSH: Received control message: 'PUSH_REQUEST'
Sun Nov 26 08:07:39 2023 us=253375 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 send_push_reply(): safe_cap=940
Sun Nov 26 08:07:39 2023 us=253375 HANNIBALDEV04VM_CLIENT/192.168.3.179:3937 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)

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
//				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, false);
//				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, true);
				this.switchStatusLocalAllGroupTo(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
				
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
				
//				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, false);				
//			this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, true);
				this.switchStatusLocalAllGroupTo(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST);
				
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
											
//				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTIONLOST, false);
//				this.setStatusLocal(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION, true);
				this.switchStatusLocalAllGroupTo(IProcessWatchRunnerOVPN.STATUSLOCAL.HASCONNECTION);
				
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
								
				bFunction = this.offerStatusLocal(enumStatusIn, null, bStatusValue);
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
				
				bReturn = this.offerStatusLocal(enumStatus, null, bStatusValue);
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
		public boolean setStatusLocal(Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bFunction = this.offerStatusLocal(enumStatus, sMessage, bStatusValue);
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
		public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
				
				bReturn = this.offerStatusLocal(enumStatus, sMessage, bStatusValue);
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
		
		//++++++++++++++++++++++++++++++++++++++++++++++++++++
		@Override
		public boolean offerStatusLocal(Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(enumStatusIn==null) {
					break main;
				}
				
			    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
			    IProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (IProcessWatchRunnerOVPN.STATUSLOCAL) enumStatusIn;
			
			    bFunction = this.offerStatusLocal_(-1, enumStatusIn, sStatusMessage, bStatusValue);
			    
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
				String sStatusName = enumStatusIn.name();
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
				IEventObject4ProcessWatchRunnerStatusLocalSetOVPN event = new EventObject4ProcessWatchRunnerStatusLocalSetOVPN(this,1,enumStatus, bStatusValue);			
				event.setApplicationObjectUsed(this.getServerBackendObject().getApplicationObject());
				
				//das ClientStarterObjekt nun auch noch dem Event hinzufuegen
				event.setServerConfigStarterObjectUsed(this.getServerConfigStarterObject());
				
				sLog = ReflectCodeZZZ.getPositionCurrent() + " ProcessWatchRunner for Process #"+ this.getNumber() + " fires event '" + enumStatus.getAbbreviation() + "'";
				this.logProtocolString(sLog);				
				this.getSenderStatusLocalUsed().fireEvent(event);
						
				bFunction = true;								
			}// end main:
		return bFunction;
		}

		
		/* (non-Javadoc)
		 * @see use.openvpn.server.status.ISenderObjectStatusLocalSetUserOVPN#getSenderStatusLocalUsed()
		 */
		@Override
		public ISenderObjectStatusLocalSetOVPN getSenderStatusLocalUsed() throws ExceptionZZZ {
			if(this.objEventStatusLocalBroker==null) {
				//++++++++++++++++++++++++++++++
				//Nun geht es darum den Sender fuer Aenderungen am Status zu erstellen, der dann registrierte Objekte ueber Aenderung von Flags informiert
				ISenderObjectStatusLocalSetOVPN objSenderStatusLocal = new SenderObjectStatusLocalSetOVPN();
				this.objEventStatusLocalBroker = objSenderStatusLocal;
			}
			return this.objEventStatusLocalBroker;
		}

		@Override
		public void setSenderStatusLocalUsed(ISenderObjectStatusLocalSetOVPN objEventSender) {
			this.objEventStatusLocalBroker = objEventSender;
		}

		@Override
		public void registerForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().addListenerObjectStatusLocalSet(objEventListener);
		}

		@Override
		public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ {
			this.getSenderStatusLocalUsed().removeListenerObjectStatusLocalSet(objEventListener);
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
				   
Sun Nov 19 11:13:20 2023 us=331697 Current Parameter Settings:
Sun Nov 19 11:13:20 2023 us=331697   config = 'C:\Programme\OpenVPN\config\server_TCP_4999.ovpn'
Sun Nov 19 11:13:20 2023 us=331697   mode = 1
Sun Nov 19 11:13:20 2023 us=331697   show_ciphers = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   show_digests = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   show_engines = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   genkey = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   key_pass_file = '[UNDEF]'
Sun Nov 19 11:13:20 2023 us=331697   show_tls_ciphers = DISABLED
Sun Nov 19 11:13:20 2023 us=331697 Connection profiles [default]:
Sun Nov 19 11:13:20 2023 us=331697   proto = tcp-server
Sun Nov 19 11:13:20 2023 us=331697   local = '[UNDEF]'
Sun Nov 19 11:13:20 2023 us=331697   local_port = 4999
Sun Nov 19 11:13:20 2023 us=331697   remote = '[UNDEF]'
Sun Nov 19 11:13:20 2023 us=331697   remote_port = 4999
Sun Nov 19 11:13:20 2023 us=331697   remote_float = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   bind_defined = DISABLED
Sun Nov 19 11:13:20 2023 us=331697   bind_local = ENABLED
Sun Nov 19 11:13:20 2023 us=331697   connect_retry_seconds = 5
Sun Nov 19 11:13:20 2023 us=331697   connect_timeout = 10
Sun Nov 19 11:13:20 2023 us=331697 NOTE: --mute triggered...
Sun Nov 19 11:13:20 2023 us=331697 270 variation(s) on previous 20 message(s) suppressed by --mute
Sun Nov 19 11:13:20 2023 us=331697 OpenVPN 2.3.18 i686-w64-mingw32 [SSL (OpenSSL)] [LZO] [PKCS11] [IPv6] built on Sep 26 2017
Sun Nov 19 11:13:20 2023 us=331697 Windows version 5.1 (Windows XP) 32bit
Sun Nov 19 11:13:20 2023 us=331697 library versions: OpenSSL 1.0.2l  25 May 2017, LZO 2.10
Sun Nov 19 11:13:20 2023 us=425450 Diffie-Hellman initialized with 1024 bit key
Sun Nov 19 11:13:20 2023 us=425450 TLS-Auth MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 19 11:13:20 2023 us=425450 Socket Buffers: R=[8192->8192] S=[8192->8192]
Sun Nov 19 11:13:20 2023 us=425450 do_ifconfig, tt->ipv6=0, tt->did_ifconfig_ipv6_setup=0
Sun Nov 19 11:13:20 2023 us=425450 ******** NOTE:  Please manually set the IP/netmask of 'OpenVPN1' to 10.0.0.1/255.255.255.252 (if it is not already set)
Sun Nov 19 11:13:20 2023 us=425450 open_tun, tt->ipv6=0
Sun Nov 19 11:13:20 2023 us=425450 TAP-WIN32 device [OpenVPN1] opened: \\.\Global\{1BB99DCA-6322-41E5-9CD1-1DBD6B1EBFF1}.tap
Sun Nov 19 11:13:20 2023 us=425450 TAP-Windows Driver Version 9.9 
Sun Nov 19 11:13:20 2023 us=425450 TAP-Windows MTU=1500
Sun Nov 19 11:13:20 2023 us=425450 Sleeping for 10 seconds...
Sun Nov 19 11:13:30 2023 us=425770 Successful ARP Flush on interface [4] {1BB99DCA-6322-41E5-9CD1-1DBD6B1EBFF1}
Sun Nov 19 11:13:30 2023 us=425770 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 19 11:13:30 2023 us=425770 Listening for incoming TCP connection on [undef]
Sun Nov 19 11:13:30 2023 us=425770 TCPv4_SERVER link local (bound): [undef]
Sun Nov 19 11:13:30 2023 us=425770 TCPv4_SERVER link remote: [undef]
Sun Nov 19 11:13:30 2023 us=425770 MULTI: multi_init called, r=256 v=256
Sun Nov 19 11:13:30 2023 us=425770 MULTI: TCP INIT maxclients=1 maxevents=5
Sun Nov 19 11:13:30 2023 us=425770 Initialization Sequence Completed
Sun Nov 19 11:13:37 2023 us=441619 MULTI: multi_create_instance called
Sun Nov 19 11:13:37 2023 us=441619 Re-using SSL/TLS context
Sun Nov 19 11:13:37 2023 us=441619 LZO compression initialized
Sun Nov 19 11:13:37 2023 us=441619 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 19 11:13:37 2023 us=441619 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 19 11:13:37 2023 us=441619 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sun Nov 19 11:13:37 2023 us=441619 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sun Nov 19 11:13:37 2023 us=441619 Local Options hash (VER=V4): 'c0103fa8'
Sun Nov 19 11:13:37 2023 us=441619 Expected Remote Options hash (VER=V4): '69109d17'
Sun Nov 19 11:13:37 2023 us=457245 TCP connection established with [AF_INET]192.168.3.179:1683
Sun Nov 19 11:13:37 2023 us=457245 TCPv4_SERVER link local: [undef]
Sun Nov 19 11:13:37 2023 us=457245 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:1683
Sun Nov 19 11:13:38 2023 us=441651 192.168.3.179:1683 TLS: Initial packet from [AF_INET]192.168.3.179:1683, sid=c472dad5 e9c0b974
Sun Nov 19 11:13:38 2023 us=613532 192.168.3.179:1683 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 11:13:38 2023 us=613532 192.168.3.179:1683 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sun Nov 19 11:13:38 2023 us=785412 192.168.3.179:1683 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:1683
Sun Nov 19 11:13:38 2023 us=785412 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sun Nov 19 11:13:38 2023 us=785412 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:1683
Sun Nov 19 11:13:38 2023 us=785412 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:1683: 10.0.0.2
Sun Nov 19 11:13:41 2023 us=207365 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 PUSH: Received control message: 'PUSH_REQUEST'
Sun Nov 19 11:13:41 2023 us=207365 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 send_push_reply(): safe_cap=940
Sun Nov 19 11:13:41 2023 us=207365 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)
Sun Nov 19 11:17:53 2023 us=324827 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 Connection reset, restarting [-1]
Sun Nov 19 11:17:53 2023 us=324827 HANNIBALDEV04VM_CLIENT/192.168.3.179:1683 SIGUSR1[soft,connection-reset] received, client-instance restarting
Sun Nov 19 11:17:53 2023 us=324827 TCP/UDP: Closing socket
Sun Nov 19 19:23:13 2023 us=828252 MULTI: multi_create_instance called
Sun Nov 19 19:23:13 2023 us=828252 Re-using SSL/TLS context
Sun Nov 19 19:23:13 2023 us=828252 LZO compression initialized
Sun Nov 19 19:23:13 2023 us=828252 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 19 19:23:13 2023 us=828252 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 19 19:23:13 2023 us=828252 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sun Nov 19 19:23:13 2023 us=828252 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sun Nov 19 19:23:13 2023 us=828252 Local Options hash (VER=V4): 'c0103fa8'
Sun Nov 19 19:23:13 2023 us=828252 Expected Remote Options hash (VER=V4): '69109d17'
Sun Nov 19 19:23:13 2023 us=828252 TCP connection established with [AF_INET]192.168.3.179:2357
Sun Nov 19 19:23:13 2023 us=828252 TCPv4_SERVER link local: [undef]
Sun Nov 19 19:23:13 2023 us=828252 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:2357
Sun Nov 19 19:23:14 2023 us=828271 192.168.3.179:2357 TLS: Initial packet from [AF_INET]192.168.3.179:2357, sid=eb77cf8e 5609a9de
Sun Nov 19 19:23:15 2023 us=31400 192.168.3.179:2357 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:23:15 2023 us=31400 192.168.3.179:2357 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sun Nov 19 19:23:15 2023 us=203278 192.168.3.179:2357 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:2357
Sun Nov 19 19:23:15 2023 us=203278 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sun Nov 19 19:23:15 2023 us=203278 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:2357
Sun Nov 19 19:23:15 2023 us=203278 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:2357: 10.0.0.2
Sun Nov 19 19:23:17 2023 us=625200 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 PUSH: Received control message: 'PUSH_REQUEST'
Sun Nov 19 19:23:17 2023 us=625200 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 send_push_reply(): safe_cap=940
Sun Nov 19 19:23:17 2023 us=625200 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)
Sun Nov 19 19:33:53 2023 us=324156 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 Inactivity timeout (--inactive), exiting
Sun Nov 19 19:33:53 2023 us=324156 HANNIBALDEV04VM_CLIENT/192.168.3.179:2357 SIGTERM[soft,inactive] received, client-instance exiting
Sun Nov 19 19:33:53 2023 us=324156 TCP/UDP: Closing socket
Sun Nov 19 19:33:58 2023 us=308691 MULTI: multi_create_instance called
Sun Nov 19 19:33:58 2023 us=308691 Re-using SSL/TLS context
Sun Nov 19 19:33:58 2023 us=308691 LZO compression initialized
Sun Nov 19 19:33:58 2023 us=308691 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 19 19:33:58 2023 us=308691 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 19 19:33:58 2023 us=308691 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sun Nov 19 19:33:58 2023 us=308691 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sun Nov 19 19:33:58 2023 us=308691 Local Options hash (VER=V4): 'c0103fa8'
Sun Nov 19 19:33:58 2023 us=308691 Expected Remote Options hash (VER=V4): '69109d17'
Sun Nov 19 19:33:58 2023 us=308691 TCP connection established with [AF_INET]192.168.3.179:2365
Sun Nov 19 19:33:58 2023 us=308691 TCPv4_SERVER link local: [undef]
Sun Nov 19 19:33:58 2023 us=308691 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:2365
Sun Nov 19 19:33:59 2023 us=308723 192.168.3.179:2365 TLS: Initial packet from [AF_INET]192.168.3.179:2365, sid=0525fbad 75ba23c1
Sun Nov 19 19:33:59 2023 us=496229 192.168.3.179:2365 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:33:59 2023 us=496229 192.168.3.179:2365 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sun Nov 19 19:33:59 2023 us=652484 192.168.3.179:2365 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:2365
Sun Nov 19 19:33:59 2023 us=652484 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sun Nov 19 19:33:59 2023 us=652484 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:2365
Sun Nov 19 19:33:59 2023 us=652484 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:2365: 10.0.0.2
Sun Nov 19 19:34:01 2023 us=933807 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 PUSH: Received control message: 'PUSH_REQUEST'
Sun Nov 19 19:34:01 2023 us=933807 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 send_push_reply(): safe_cap=940
Sun Nov 19 19:34:01 2023 us=933807 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)
Sun Nov 19 19:36:30 2023 us=608903 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 Connection reset, restarting [-1]
Sun Nov 19 19:36:30 2023 us=608903 HANNIBALDEV04VM_CLIENT/192.168.3.179:2365 SIGUSR1[soft,connection-reset] received, client-instance restarting
Sun Nov 19 19:36:30 2023 us=608903 TCP/UDP: Closing socket
Sun Nov 19 19:38:32 2023 us=126238 MULTI: multi_create_instance called
Sun Nov 19 19:38:32 2023 us=126238 Re-using SSL/TLS context
Sun Nov 19 19:38:32 2023 us=126238 LZO compression initialized
Sun Nov 19 19:38:32 2023 us=126238 Control Channel MTU parms [ L:1544 D:1210 EF:40 EB:0 ET:0 EL:3 ]
Sun Nov 19 19:38:32 2023 us=126238 Data Channel MTU parms [ L:1544 D:1450 EF:44 EB:143 ET:0 EL:3 AF:3/1 ]
Sun Nov 19 19:38:32 2023 us=126238 Local Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_SERVER,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-server'
Sun Nov 19 19:38:32 2023 us=126238 Expected Remote Options String: 'V4,dev-type tun,link-mtu 1544,tun-mtu 1500,proto TCPv4_CLIENT,comp-lzo,cipher BF-CBC,auth SHA1,keysize 128,key-method 2,tls-client'
Sun Nov 19 19:38:32 2023 us=126238 Local Options hash (VER=V4): 'c0103fa8'
Sun Nov 19 19:38:32 2023 us=126238 Expected Remote Options hash (VER=V4): '69109d17'
Sun Nov 19 19:38:32 2023 us=126238 TCP connection established with [AF_INET]192.168.3.179:2390
Sun Nov 19 19:38:32 2023 us=126238 TCPv4_SERVER link local: [undef]
Sun Nov 19 19:38:32 2023 us=126238 TCPv4_SERVER link remote: [AF_INET]192.168.3.179:2390
Sun Nov 19 19:38:33 2023 us=110632 192.168.3.179:2390 TLS: Initial packet from [AF_INET]192.168.3.179:2390, sid=a1e37497 8132b731
Sun Nov 19 19:38:33 2023 us=251259 192.168.3.179:2390 VERIFY OK: depth=1, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=PAUL.HINDENBURG, name=PAUL.HINDENBURG, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:38:33 2023 us=251259 192.168.3.179:2390 VERIFY OK: depth=0, C=DE, ST=PREUSSEN, L=BERLIN, O=OpenVPN, OU=TEST, CN=HANNIBALDEV04VM_CLIENT, name=HANNIBALDEV04VM, emailAddress=paul.hindenburg@mailinator.com\09
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 Data Channel Encrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 Data Channel Encrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 Data Channel Decrypt: Cipher 'BF-CBC' initialized with 128 bit key
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 WARNING: INSECURE cipher with block size less than 128 bit (64 bit).  This allows attacks like SWEET32.  Mitigate by using a --cipher with a larger block size (e.g. AES-256-CBC).
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 Data Channel Decrypt: Using 160 bit message hash 'SHA1' for HMAC authentication
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 WARNING: cipher with small block size in use, reducing reneg-bytes to 64MB to mitigate SWEET32 attacks.
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 Control Channel: TLSv1.2, cipher TLSv1/SSLv3 DHE-RSA-AES256-GCM-SHA384, 1024 bit RSA
Sun Nov 19 19:38:33 2023 us=345011 192.168.3.179:2390 [HANNIBALDEV04VM_CLIENT] Peer Connection Initiated with [AF_INET]192.168.3.179:2390
Sun Nov 19 19:38:33 2023 us=345011 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 OPTIONS IMPORT: reading client specific options from: C:\Programme\OpenVPN\config\clientconnection\HANNIBALDEV04VM_CLIENT
Sun Nov 19 19:38:33 2023 us=345011 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 MULTI: Learn: 10.0.0.2 -> HANNIBALDEV04VM_CLIENT/192.168.3.179:2390
Sun Nov 19 19:38:33 2023 us=345011 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 MULTI: primary virtual IP for HANNIBALDEV04VM_CLIENT/192.168.3.179:2390: 10.0.0.2
Sun Nov 19 19:38:35 2023 us=688806 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 PUSH: Received control message: 'PUSH_REQUEST'
Sun Nov 19 19:38:35 2023 us=688806 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 send_push_reply(): safe_cap=940
Sun Nov 19 19:38:35 2023 us=688806 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 SENT CONTROL [HANNIBALDEV04VM_CLIENT]: 'PUSH_REPLY,ping 10,ping-restart 240,ifconfig 10.0.0.2 10.0.0.1' (status=1)
Sun Nov 19 19:47:51 2023 us=75880 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 Connection reset, restarting [-1]
Sun Nov 19 19:47:51 2023 us=75880 HANNIBALDEV04VM_CLIENT/192.168.3.179:2390 SIGUSR1[soft,connection-reset] received, client-instance restarting
Sun Nov 19 19:47:51 2023 us=75880 TCP/UDP: Closing socket
						 
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
		public boolean writeErrorToLog() throws ExceptionZZZ{
			boolean bReturn = false;
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
				    	if( this.getFlag("stoprequested")==true) break main;
					}
				} catch (IOException e) {
					ExceptionZZZ ez = new ExceptionZZZ("IOException happend: '" + e.getMessage() + "'", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				bReturn = true;
			}//END Main:	
			return bReturn;
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
			    	if( this.getFlag("stoprequested")==true) break main;
			
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
