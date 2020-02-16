package use.openvpn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import basic.zKernel.KernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;


public abstract class AbstractConfigStarterOVPN extends KernelUseObjectZZZ implements IConfigStarterOVPN, IMainUserOVPN{
	private IMainOVPN objMain = null;
	private File objFileConfig=null;
	private Process objProcess=null;
	private String sMyAlias = "-1";
	private boolean bFlagByBatch = false;
	String sOvpnContextClientOrServer=null;
	
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFile, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, sMyAlias, objFile, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFile, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, "-1", objFile, saFlagControl);
	}
	
	private void ConfigStarterNew_(IMainOVPN objMain, String sMyAlias, File objFile, String[] saFlagControl) throws ExceptionZZZ{
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
				
				if(objMain==null) {
					ExceptionZZZ ez = new ExceptionZZZ("MainApplicatonObject", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setMainObject(objMain);
				
				if(objFile==null){
					ExceptionZZZ ez = new ExceptionZZZ("File", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setFileConfig(objFile);
				this.sMyAlias = sMyAlias;
				
				String sOvpnContextClientOrServer = StringZZZ.left(objFile.getName(), "_");
				this.setOvpnContextUsed(sOvpnContextClientOrServer);
			}//End check
		}//END main
	}
	
		
	/**Should destroy a process. But this does not work.
	 * A workaround used, is to kill all openvpn.exe - processes when the cientUI is closed/stopped. 
	 * @return void
	 *
	 * javadoc created by: 0823, 11.07.2006 - 12:55:49
	 */
	public void requestStop(){
		main:{
			Process objProcess = null;
			check:{
				objProcess = this.objProcess;
				if(objProcess==null) break main;
			}
		
			objProcess.destroy();
		}//END main
	}
	
	public boolean isProcessAlive(){
		boolean bReturn = false;
		main:{
			check:{
				if(this.objProcess==null) break main;				
			}//END check:
		
			try{
				//TODO GOON den exit status des Processes auch sicher abpr�fbar machen !!!
				//Merke: Einen Exit-Status abzurufen, wenn der Process noch l�uft, wirft eine IllegalThreadStateException
				this.objProcess.exitValue();
				return false;
			}catch(IllegalThreadStateException e){
				return true;
			}	
		
		}//END Main:
		return bReturn;
	}

	
	//###### GETTER  / SETTER
	public IMainOVPN getMainObject() {
		return this.objMain;
	}
	public void setMainObject(IMainOVPN objMain) {
		this.objMain = objMain;
	}
	
	public void setFileConfig(File objFile){
		this.objFileConfig = objFile;
	}
	public File getFileConfig(){
		return this.objFileConfig;
	}
	
	public Process getProcess(){
		return this.objProcess;
	}
	public void setProcess(Process objProcess) {
		this.objProcess = objProcess;
	}
	
	/**This is not the process id.
	 * It is just an alias, which was provided at the constructor.
	 * @return int
	 *
	 * javadoc created by: 0823, 28.07.2006 - 15:04:25
	 */
	public String getAlias(){
		return this.sMyAlias;
	}
	
	public String getOvpnContextUsed() {
		return this.sOvpnContextClientOrServer;
	}
	public void setOvpnContextUsed(String sOvpnContextClientOrServer) {
		this.sOvpnContextClientOrServer = sOvpnContextClientOrServer;
	}
	
	
//	###### FLAGS
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
			if(stemp.equals("bybatch")){
				bFunction = bFlagByBatch;
				break main;
			}
			/*
			else if(stemp.equals("hasoutput")){
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
		if(stemp.equals("bybatch")){
			bFlagByBatch = bFlagValue;
			bFunction = true;
			break main;
		}
		/*
		else if(stemp.equals("hasoutput")){
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
		}//END main:
		return bFunction;
	}

	@Override
	public abstract Process requestStart() throws ExceptionZZZ;
	
	@Override
	public abstract ArrayList<String> computeBatchLines(File objFileTemplateOvpn) throws ExceptionZZZ;
}//END class
