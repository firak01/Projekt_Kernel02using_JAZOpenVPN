package use.openvpn.client.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.EnumSet;
import java.util.HashMap;

import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZUserZZZ;
import basic.zKernel.process.AbstractProcessWatchRunnerZZZ;
import basic.zKernel.process.IProcessWatchRunnerZZZ;
import basic.zKernel.process.ProcessWatchRunnerZZZ;
import basic.zKernel.process.AbstractProcessWatchRunnerZZZ.STATUSLOCAL;
import basic.zKernel.status.EventObjectStatusLocalSetZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.status.EventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

/**This class receives the stream from a process, which was started by the ConfigStarterZZZ class.
 * This is necessary, because the process will only goon working, if the streams were "catched" by a target.
 * This "catching" will be done in a special thread (one Thread per process).  
 * @author 0823
 *
 */
public class ProcessWatchRunnerOVPN extends AbstractProcessWatchRunnerZZZ{	
	public ProcessWatchRunnerOVPN(IKernelZZZ objKernel, Process objProcess, int iNumber, String[] saFlag) throws ExceptionZZZ{
		super(objKernel, objProcess, iNumber, saFlag);
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
				//System.out.println("FGLTEST01");
				//Wichtig: Man muss wohl zuallererst den InputStream abgreifen, damit der Process weiterlaufen kann.
				this.writeOutputToLog();
				boolean bHasConnection = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.HASCONNECTION);
				if(bHasConnection) {
					sLog = "Connection wurde erstellt. Beende ProcessWatchRunner #"+this.getNumber();
					this.logLineDate(sLog);						
					break;
				}
				
				
				//System.out.println("FGLTEST02");
				this.writeErrorToLog();
				boolean bError = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.HASERROR);
				if(bError) break;

				//TODOGOON: Und Statt des FlagHandling localStatus verwenden...
				
				//Nach irgendeiner Ausgabe enden ist hier falsch, in einer abstrakten Klasse vielleicht richtig, quasi als Muster.
				//if(this.getFlag("hasOutput")) break;
				//System.out.println("FGLTEST03");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					ExceptionZZZ ez = new ExceptionZZZ("An InterruptedException happened: '" + e.getMessage() + "''", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				
				boolean bStopRequested = this.getFlag(IProcessWatchRunnerZZZ.FLAGZ.STOPREQUEST);
				if(bStopRequested) break;					
		}while(true);
		this.setFlag(IProcessWatchRunnerZZZ.FLAGZ.ENDED, true);
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
		
		int iProcess = this.getNumber();
		String sLog = " Process#: " + iProcess + " - sLine=" + sLine;
		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
		if(StringZZZ.contains(sLine,"TCP connection established with")) {
			this.setFlag(IProcessWatchRunnerZZZ.FLAGZ.HASCONNECTION, true);
			bReturn = true;
		}
		
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
				ProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
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
		
		@Override
		public boolean setStatusLocal(Enum objEnumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(objEnumStatusIn==null) {
					break main;
				}
			//return this.getStatusLocal(objEnumStatus.name());
			//Nein, trotz der Redundanz nicht machen, da nun der Event anders gefeuert wird, nämlich über das enum
			
		    //Merke: In anderen Klassen, die dieses Design-Pattern anwenden ist das eine andere Klasse fuer das Enum
		    ProcessWatchRunnerOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
			String sStatusName = enumStatus.name();
			bFunction = this.proofStatusLocalExists(sStatusName);															
			if(bFunction == true){
				
				//Setze das Flag nun in die HashMap
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				hmStatus.put(sStatusName.toUpperCase(), bStatusValue);
			
				//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
				//Dann erzeuge den Event und feuer ihn ab.
				//Merke: Nun aber ueber das enum			
				if(this.objEventStatusLocalBroker!=null) {
					IEventObjectStatusLocalSetZZZ event = new EventObjectStatusLocalSetZZZ(this,1,enumStatus, bStatusValue);
					this.objEventStatusLocalBroker.fireEvent(event);
				}			
				bFunction = true;								
			}										
		}	// end main:
		return bFunction;
		}
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//DIE INTERNE ENUM-KLASSE FUER STATUSLOCAL.
		//Merke1: Diese könnte auch in eine extra Klasse ausgelagert werden (z.B. um es in einer Datenbank mit Hibernate zu persistieren.
		//        Für die Auslagerung als extra Klasse, s.: EnumSetMappedTestTypeZZZ
		//Merke2: Ohne diese interne Enum-Klasse würde das Enum der abstrakten Elternklasse genommen werden
		//++++++++++++++++++++++++
			
		//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
			//ALIAS("Uniquename","Statusmeldung","Beschreibung, wird nicht genutzt....",)
			public enum STATUSLOCAL implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{	
				ISSTARTED("isstarted","ProcessWatchRunner",""),
				HASCONNECTION("hasconnection","ProcessWatchRunner ist mit dem Process verbunden",""),
				HASOUTPUT("hasoutput","Prozess hat Output",""),
				HASINPUT("hasinput","Prozess hat Input",""),
				ISSTOPPED("isended","ProcessWatchRunner ist beendet",""),
				HASERROR("haserror","Ein Fehler ist aufgetreten","");
											
			private String sAbbreviation,sStatusMessage,sDescription;

			//#############################################
			//#### Konstruktoren
			//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
			//In der Util-Klasse habe ich aber einen Workaround gefunden.
			STATUSLOCAL(String sAbbreviation, String sStatusMessage, String sDescription) {
			    this.sAbbreviation = sAbbreviation;
			    this.sStatusMessage = sStatusMessage;
			    this.sDescription = sDescription;
			}

			public String getAbbreviation() {
			 return this.sAbbreviation;
			}
			
			public String getStatusMessage() {
				 return this.sStatusMessage;
			}
			
			public EnumSet<?>getEnumSetUsed(){
				return STATUSLOCAL.getEnumSet();
			}

			/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
			@SuppressWarnings("rawtypes")
			public static <E> EnumSet getEnumSet() {
				
			 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
				//String sFilterName = "FLAGZ"; /
				//...
				//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
				
				//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
				Class<STATUSLOCAL> enumClass = STATUSLOCAL.class;
				EnumSet<STATUSLOCAL> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
				
				//Merke: In einer anderen Klasse, die dieses DesingPattern nutzt, befinden sich die Enums in einer anderen Klasse
				for(Object obj : ProcessWatchRunnerOVPN.class.getEnumConstants()){
					//System.out.println(obj + "; "+obj.getClass().getName());
					set.add((STATUSLOCAL) obj);
				}
				return set;
				
			}

			//TODO: Mal ausprobieren was das bringt
			//Convert Enumeration to a Set/List
			private static <E extends Enum<E>>EnumSet<E> toEnumSet(Class<E> enumClass,long vector){
				  EnumSet<E> set=EnumSet.noneOf(enumClass);
				  long mask=1;
				  for (  E e : enumClass.getEnumConstants()) {
				    if ((mask & vector) == mask) {
				      set.add(e);
				    }
				    mask<<=1;
				  }
				  return set;
				}

			//+++ Das könnte auch in einer Utility-Klasse sein.
			//the valueOfMethod <--- Translating from DB
			public static STATUSLOCAL fromAbbreviation(String s) {
			for (STATUSLOCAL state : values()) {
			   if (s.equals(state.getAbbreviation()))
			       return state;
			}
			throw new IllegalArgumentException("Not a correct abbreviation: " + s);
			}

			//##################################################
			//#### Folgende Methoden bring Enumeration von Hause aus mit. 
					//Merke: Diese Methoden können aber nicht in eine abstrakte Klasse verschoben werden, zum daraus Erben. Grund: Enum erweitert schon eine Klasse.
			@Override
			public String getName() {	
				return super.name();
			}

			@Override
			public String toString() {//Mehrere Werte mit # abtennen
			    return this.sAbbreviation+"="+this.sDescription;
			}

			@Override
			public int getIndex() {
				return ordinal();
			}

			//### Folgende Methoden sind zum komfortablen Arbeiten gedacht.
			@Override
			public int getPosition() {
				return getIndex()+1; 
			}

			@Override
			public String getDescription() {
				return this.sDescription;
			}
			//+++++++++++++++++++++++++
			}//End internal Enum Class
}//END class
