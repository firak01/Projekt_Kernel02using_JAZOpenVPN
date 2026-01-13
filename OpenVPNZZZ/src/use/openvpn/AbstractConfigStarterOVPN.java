package use.openvpn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZLocalEnabledZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ;
import basic.zKernel.flag.event.EventObjectFlagZsetZZZ;
import basic.zKernel.flag.event.IEventObjectFlagZsetZZZ;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN;
import use.openvpn.server.ServerConfigMapper4BatchOVPN;
import use.openvpn.server.ServerMainOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.abstractList.SetUtilZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;


public abstract class AbstractConfigStarterOVPN extends AbstractKernelUseObjectZZZ implements IConfigStarterOVPN, IMainUserOVPN, IConfigMapper4BatchUserOVPN{
	private IMainOVPN objMain = null;
	private IConfigMapper4BatchOVPN objMapper4Batch = null;
	
	private File objFileConfigOvpn=null;
	private File objFileTemplateBatch=null;
	private Process objProcess=null;
	private String sMyAlias = "-1";
	private int iIndex = -1;
	
	String sOvpnContextClientOrServer=null;
		
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, int iIndex, File objFileOConfigvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, sMyAlias, iIndex, null,objFileOConfigvpn, saFlagControl);
	}
	
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, int iIndex, File objFileTemplateBatch, File objFileOConfigvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, sMyAlias, iIndex, objFileTemplateBatch, objFileOConfigvpn, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public AbstractConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, int iIndex, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(objMain, "-1", iIndex, objFileTemplateBatch, objFileConfigOvpn, saFlagControl);
	}
	
	private void ConfigStarterNew_(IMainOVPN objMain, String sMyAlias, int iIndex, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		main:{
				 
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ( stemp, IFlagZEnabledZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							 
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
				
				
				//Falls nicht explizit das Flag gesetzt ist, wirf keinen Fehler. Übernimm dann trotzdem die Batchdatei - wenn übergeben -, was man hat dat hat man. 
				if(objFileTemplateBatch==null){
					//bybatch
					if(this.getFlag(IConfigStarterOVPN.FLAGZ.BY_BATCH.name())) {
						ExceptionZZZ ez = new ExceptionZZZ("BatchConfigurationFile", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
				}else{
					this.setFileTemplateBatch(objFileTemplateBatch);
				}
				
				
				this.sMyAlias = sMyAlias;
				this.iIndex = iIndex;
				
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
	public IConfigMapper4BatchOVPN getConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4BatchOVPN objReturn = null;
		if(this.objMapper4Batch==null) {			
			objReturn = this.createConfigMapperObject();
			this.setConfigMapperObject(objReturn);		
		}
		return this.objMapper4Batch;	
	}
	
	public abstract IConfigMapper4BatchOVPN createConfigMapperObject() throws ExceptionZZZ;
		
	
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
	public void setAlias(String sAlias) {
		this.sMyAlias = sAlias;
	}
	
	public int getIndex() {
		return this.iIndex;
	}
	public void setIndex(int iIndex) {
		this.iIndex = iIndex;
	}
	
	
	public String getOvpnContextUsed() {
		return this.sOvpnContextClientOrServer;
	}
	public void setOvpnContextUsed(String sOvpnContextClientOrServer) {
		this.sOvpnContextClientOrServer = sOvpnContextClientOrServer;
	}
	
	
	//###### FLAGS
	/* @see basic.zBasic.IFlagZZZ#getFlagZ(java.lang.String)
	 * 	 Weitere Voraussetzungen:
	 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
	 * - Innere Klassen muessen auch public deklariert werden.(non-Javadoc)
	 */
	public boolean getFlag(String sFlagName) throws ExceptionZZZ {
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
										
			HashMap<String, Boolean> hmFlag = this.getHashMapFlag();
			Boolean objBoolean = hmFlag.get(sFlagName.toUpperCase());
			if(objBoolean==null){
				bFunction = false;
			}else{
				bFunction = objBoolean.booleanValue();
			}
							
		}	// end main:
		
		return bFunction;	
	}
	
	//ALTE VERSION
	/* (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- hasError
	- hasOutput
	- hasInput
	- stoprequested
	 */
//	public boolean getFlag(String sFlagName){
//		boolean bFunction = false;
//		main:{
//			if(StringZZZ.isEmpty(sFlagName)) break main;
//			bFunction = super.getFlag(sFlagName);
//			if(bFunction==true) break main;
//							
//			//getting the flags of this object
//			String stemp = sFlagName.toLowerCase();
//			if(stemp.equals("bybatch")){
//				bFunction = bFlagByBatch;
//				break main;
//			}
//			/*
//			else if(stemp.equals("hasoutput")){
//				bFunction = bFlagHasOutput;
//				break main;
//			}else if(stemp.equals("hasinput")){
//				bFunction = bFlagHasInput;
//				break main;
//			}else if(stemp.equals("stoprequested")){
//				bFunction = bFlagStopRequested;
//				break main;
//			}
//			*/
//	
//		}//end main:
//		return bFunction;
//	}

/** DIESE METHODE MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHRE FLAGS SETZEN WOLLEN
 * Weitere Voraussetzungen:
 * - Public Default Konstruktor der Klasse, damit die Klasse instanziiert werden kann.
 * - Innere Klassen müssen auch public deklariert werden.
 * @param objClassParent
 * @param sFlagName
 * @param bFlagValue
 * @return
 * lindhaueradmin, 23.07.2013
 */
@Override
public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ {
	boolean bFunction = false;
	main:{
		if(StringZZZ.isEmpty(sFlagName)) {
			bFunction = true;
			break main;
		}
					
		bFunction = this.proofFlagExists(sFlagName);															
		if(bFunction == true){
			
			//Setze das Flag nun in die HashMap
			HashMap<String, Boolean> hmFlag = this.getHashMapFlag();
			hmFlag.put(sFlagName.toUpperCase(), bFlagValue);								
			
			//Falls irgendwann ein Objekt sich fuer die Eventbenachrichtigung registriert hat, gibt es den EventBroker.
			//Dann erzeuge den Event und feuer ihn ab.
			if(this.objEventFlagZBroker!=null) {
				IEventObjectFlagZsetZZZ event = new EventObjectFlagZsetZZZ(this,1,sFlagName.toUpperCase(), bFlagValue);
				this.objEventFlagZBroker.fireEvent(event);
			}
			
			bFunction = true;								
		}										
	}	// end main:
	
	return bFunction;	
}

//ALTE VERSION
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
//public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
//	boolean bFunction = false;
//	main:{			
//		if(StringZZZ.isEmpty(sFlagName)) break main;
//		bFunction = super.setFlag(sFlagName, bFlagValue);
//	if(bFunction==true) break main;
//	
//	//setting the flags of this object
//	String stemp = sFlagName.toLowerCase();
//	if(stemp.equals("bybatch")){
//		bFlagByBatch = bFlagValue;
//		bFunction = true;
//		break main;
//	}
//	/*
//	else if(stemp.equals("hasoutput")){
//		bFlagHasOutput = bFlagValue;
//		bFunction = true;
//		break main;
//	}else if(stemp.equals("hasinput")){
//		bFlagHasInput = bFlagValue;
//		bFunction = true;
//		break main;
//	}else if(stemp.equals("stoprequested")){
//		bFlagStopRequested = bFlagValue;
//		bFunction = true;
//		break main;
//	}
//	*/
//	}//END main:
//	return bFunction;
//}
	
		@Override
		public boolean getFlag(IConfigStarterOVPN.FLAGZ objEnumFlag) {
			return this.getFlag(objEnumFlag.name());
		}
		@Override
		public boolean setFlag(IConfigStarterOVPN.FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			return this.setFlag(objEnumFlag.name(), bFlagValue);
		}
		
		@Override
		public boolean[] setFlag(IConfigStarterOVPN.FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ {
			boolean[] baReturn=null;
			main:{
				if(!ArrayUtilZZZ.isNull(objaEnumFlag)) {
					baReturn = new boolean[objaEnumFlag.length];
					int iCounter=-1;
					for(IConfigStarterOVPN.FLAGZ objEnumFlag:objaEnumFlag) {
						iCounter++;
						boolean bReturn = this.setFlag(objEnumFlag, bFlagValue);
						baReturn[iCounter]=bReturn;
					}
					
					//!!! Ein mögliches init-Flag ist beim direkten setzen der Flags unlogisch.
					//    Es wird entfernt.
					this.setFlag(IFlagZEnabledZZZ.FLAGZ.INIT, false);
				}
			}//end main:
			return baReturn;
		}
		
		@Override
		public boolean proofFlagExists(IConfigStarterOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagExists(objEnumFlag.name());
		}	
		
		@Override
		public boolean proofFlagSetBefore(IConfigStarterOVPN.FLAGZ objEnumFlag) throws ExceptionZZZ {
			return this.proofFlagSetBefore(objEnumFlag.name());
		}
	
	//##########################################

	@Override
	public abstract Process requestStart() throws ExceptionZZZ;
	
	@Override
	public ArrayList<String> computeBatchLines(File fileConfigTemplateBatch, File fileConfigTemplateOvpn) throws ExceptionZZZ {		
		ArrayList<String>listasReturn=new ArrayList<String>();
		main:{			
			IConfigMapper4BatchOVPN objMapperBatch = this.getConfigMapperObject(); //new ServerConfigMapper4BatchOVPN(this.getKernelObject(), this.getServerObject(), fileConfigTemplateBatch);
			HashMapIterableKeyZZZ<String, String>hmBatchLines = objMapperBatch.readTaskHashMap();								
							
			for(String sKey : hmBatchLines) {
				String sLine = hmBatchLines.getValue(sKey);
				listasReturn.add(sLine);
			}
			
		}
		return listasReturn;
	}	
	
	@Override
	public abstract HashMap<String, String> computeProcessArgumentHashMap() throws ExceptionZZZ;
	
}//END class
