package use.openvpn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import use.openvpn.server.ServerConfigMapper4BatchOVPN;
import use.openvpn.server.ServerMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.abstractList.SetZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;


public abstract class AbstractConfigStarterOVPN extends KernelUseObjectZZZ implements IConfigStarterOVPN, IMainUserOVPN, IConfigMapper4BatchUserOVPN{
	private IMainOVPN objMain = null;
	private IConfigMapper4BatchOVPN objMapper4Batch = null;
	
	private File objFileConfigOvpn=null;
	private File objFileTemplateBatch=null;
	private Process objProcess=null;
	private String sMyAlias = "-1";
	private boolean bFlagByBatch = false;
	String sOvpnContextClientOrServer=null;
		
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileOConfigvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, sMyAlias, null, objFileOConfigvpn, saFlagControl);
	}
	
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileTemplateBatch, File objFileOConfigvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, sMyAlias, objFileTemplateBatch, objFileOConfigvpn, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, "-1", objFileTemplateBatch, objFileConfigOvpn, saFlagControl);
	}
	
	private void ConfigStarterNew_(IMainOVPN objMain, String sMyAlias, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
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
					ExceptionZZZ ez = new ExceptionZZZ("MainApplicationObject", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setMainObject(objMain);
				
				if(objFileConfigOvpn==null){
					ExceptionZZZ ez = new ExceptionZZZ("OvpnConfigurationFile", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setFileConfigOvpn(objFileConfigOvpn);
				
				if(objFileTemplateBatch==null){
					ExceptionZZZ ez = new ExceptionZZZ("BatchConfigurationFile", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setFileTemplateBatch(objFileTemplateBatch);	
				
				
				this.sMyAlias = sMyAlias;
				
				String sOvpnContextClientOrServer = StringZZZ.left(objFileConfigOvpn.getName(), "_");
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
	
	@Override
	public IConfigMapper4BatchOVPN getConfigMapperObject() {
		IConfigMapper4BatchOVPN objReturn = null;
		if(this.objMapper4Batch==null) {			
			objReturn = this.createConfigMapperObject();
			this.setConfigMapperObject(objReturn);		
		}
		return this.objMapper4Batch;	
	}
	
	public abstract IConfigMapper4BatchOVPN createConfigMapperObject();
		
	
	@Override
	public void setConfigMapperObject(IConfigMapper4BatchOVPN objConfigMapper) {
		this.objMapper4Batch = objConfigMapper;
	}
	
	public void setFileConfigOvpn(File objFileConfigOvpn){
		this.objFileConfigOvpn = objFileConfigOvpn;
	}
	public File getFileConfigOvpn(){
		return this.objFileConfigOvpn;
	}
	
		
	@Override
	public void setFileTemplateBatch(File objFileTemplateBatch) {
		this.objFileTemplateBatch = objFileTemplateBatch;
	}

	@Override
	public File getFileTemplateBatch() {
		return this.objFileTemplateBatch;
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
	
	//@Override
	//public abstract ArrayList<String> computeBatchLines(File objFileBatch, File objFileTemplateOvpn) throws ExceptionZZZ;
	@Override
	public ArrayList<String> computeBatchLines(File fileConfigTemplateBatch, File fileConfigTemplateOvpn) throws ExceptionZZZ {		
		ArrayList<String>listasReturn=new ArrayList<String>();
		main:{
			//ServerConfigMapper4BatchOVPN objMapperBatch = new ServerConfigMapper4BatchOVPN(this.getKernelObject(), this.getServerObject(), fileConfigTemplateBatch);
			IConfigMapper4BatchOVPN objMapperBatch = this.getConfigMapperObject(); //new ServerConfigMapper4BatchOVPN(this.getKernelObject(), this.getServerObject(), fileConfigTemplateBatch);
			HashMapIterableKeyZZZ<String, String>hmBatchLines = objMapperBatch.readTaskHashMap();								
			
			//111111111 TODO GOON 20200304: Mal sehen was die IterableKey HashMap leistet...
			//Aber: Die Sortierung ist im Set nicht sichergestellt. Darum explizit sortieren.
			//List<String>numbersList=(List<String>) SetZZZ.sortAsString(setBatchLineNumber);
//			Set<String> setBatchLineNumber = hmBatchLines.keySet();			
//			List<String>numbersList=(List<String>) SetZZZ.sortAsInteger(setBatchLineNumber);
//			for(String sLineNumber : numbersList) {
//				String sLine = hmBatchLines.get(sLineNumber);
//				listasReturn.add(sLine);
//			}	
			
			for(String sKey : hmBatchLines) {
				String sLine = hmBatchLines.getValue(sKey);
				listasReturn.add(sLine);
			}
			
		}
		return listasReturn;
	}	
	
}//END class
