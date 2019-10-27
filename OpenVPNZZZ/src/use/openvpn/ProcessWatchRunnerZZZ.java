package use.openvpn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import basic.zKernel.KernelZZZ;

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
public class ProcessWatchRunnerZZZ extends KernelUseObjectZZZ implements Runnable{
	private Process objProcess=null; //Der externe process, der hierdurch "gemonitored" werden soll
	private int iNumber=0;
	public  boolean bEnded = false;
	private boolean bFlagHasError=false;
	private boolean bFlagHasOutput = false;
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
							   ExceptionZZZ ez = new ExceptionZZZ(stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							  
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
			this.getLogObject().WriteLineDate("ProcessWatchRunner #"+ this.getNumber() + " started.");
				//Solange laufen, bis irgendeine Form des Outputs zur�ckkommt
		do{
					this.writeOutputToLog();		//Man muss wohl erst den InputStream abgreifen, damit der Process weiterlaufen kann.
					if(this.getFlag("hasOutput")) break;
					
					//Auf jeden Fall wird erst danach die Verbindung zum "virtuellen Netzwerkadapter hergestellt"
					//Danach geht es aber nicht weiter.
					this.writeErrorToLog();
					if(this.getFlag("hasError")) break;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						ExceptionZZZ ez = new ExceptionZZZ("An InterruptedException happened: '" + e.getMessage() + "''", iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
		}while(true);
		this.bEnded = true;
		this.getLogObject().WriteLineDate("ProcessWatchRunner #"+ this.getNumber() + " ended.");
					
		}catch(ExceptionZZZ ez){
			this.getLogObject().WriteLineDate(ez.getDetailAllLast());
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
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
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
}//END class
