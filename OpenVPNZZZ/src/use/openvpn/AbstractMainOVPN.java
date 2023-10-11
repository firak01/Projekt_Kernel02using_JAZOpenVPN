package use.openvpn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.status.StatusLocalHelperZZZ;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.status.EventObjectStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;

public abstract class AbstractMainOVPN extends AbstractKernelUseObjectWithStatusZZZ implements Runnable,IMainOVPN{
	protected IApplicationOVPN objApplication = null;
	protected ConfigChooserOVPN objConfigChooser = null;
	protected IConfigMapper4TemplateOVPN objConfigMapper = null;
	
	
	protected String sMainStatus = null; //Hierueber kann das Frontend abfragen, was gerade in der Methode "start()" so passiert.
	protected String sMainStatusPrevious = null;
	
	protected String sMessage = null; //wird als Protokoll verwendet
	protected ArrayList<String> listaMessage = new ArrayList<String>(); //Hierueber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
		
	public AbstractMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
	}
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sMessage 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 * @throws ExceptionZZZ 
	 */
	public void logMessageString(String sMessage) throws ExceptionZZZ{
		if(sMessage!=null){
			this.addMessageString(sMessage);
			
			IKernelZZZ objKernel = this.getKernelObject();
			if(objKernel!= null){
				objKernel.getLogObject().WriteLineDate(sMessage);
			}
		}
	}
	
	/** Adds a line to the status arraylist. This status is used to enable the frontend-client to show a log dialogbox.
	 * Remark: This method does not write anything to the kernel-log-file. 
	* @param sMessage 
	* 
	* lindhaueradmin; 13.07.2006 08:34:56
	 */
	public void addMessageString(String sMessage){
		if(sMessage!=null){
			this.sMessage = sMessage;
			this.listaMessage.add(sMessage);
		}
	}
	
	
	//##### GETTER / SETTER
	public String getJarFilePathUsed() {
		return AbstractMainOVPN.sJAR_FILE_USED;
	}
	
	
	//#####################################################
	//### Verwalte den eigenen Status String...
	public String getStatusString(){
		return this.sMainStatus;
	}
	public void setStatusString(String sStatus) {
		
		main:{
			String sStatusPrevious = this.getStatusString();
			if(sStatus == null) {
				if(sStatusPrevious==null)break main;
			}
			
			if(!sStatus.equals(sStatusPrevious)) {
				String sStatusCurrent = this.getStatusString();
				this.sMainStatus = sStatus;
				this.setStatusPrevious(sStatusCurrent);
			}
		}//end main:
	
	}
	
	public String getStatusPreviousString() {
		return this.sMainStatusPrevious;
	}
	public void setStatusPrevious(String sStatusPrevious) {
		this.sMainStatusPrevious = sStatusPrevious;
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
	
	
	/**This status is a type of "Log".
	 * This are all entries.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public ArrayList getMessageStringAll(){
		return this.listaMessage;
	}
	

	@Override
	public abstract void run();

	@Override
	public IApplicationOVPN getApplicationObject() {
		return this.objApplication;
	}

	@Override
	public void setApplicationObject(IApplicationOVPN objApplication) {
		this.objApplication = objApplication;
	}

	@Override
	public ConfigChooserOVPN getConfigChooserObject() {
		return this.objConfigChooser;
	}

	@Override
	public void setConfigChooserObject(ConfigChooserOVPN objConfigChooser) {
		this.objConfigChooser = objConfigChooser;
	}

	@Override
	public IConfigMapper4TemplateOVPN getConfigMapperObject() {
		return this.objConfigMapper;
	}	
//	@Override
//	public IConfigMapperOVPN getConfigMapperObject() {
//		return this.objConfigMapper;
//	}

	@Override
	public void setConfigMapperObject(IConfigMapper4TemplateOVPN objConfigMapper) {
		this.objConfigMapper = objConfigMapper;
	}	
//	@Override
//	public void setConfigMapperObject(IConfigMapperOVPN objConfigMapper) {
//		this.objConfigMapper = objConfigMapper;
//	}
	
	
	//####### aus IStatusLocalUserZZZ
	@Override
	public HashMap<String, Boolean> getHashMapStatusLocal() {
		return this.hmStatusLocal;
	}

	@Override
	public void setHashMapStatusLocal(HashMap<String, Boolean> hmStatusLocal) {
		this.hmStatusLocal = hmStatusLocal;
	}
	
	//#####################################################
		//### IStatusLocalUserZZZ
		/** DIESE METHODEN MUSS IN ALLEN KLASSEN VORHANDEN SEIN - über Vererbung -, DIE IHREN STATUS SETZEN WOLLEN*/

		/* (non-Javadoc)
		 * @see basic.zKernel.status.IStatusLocalUserZZZ#getStatusLocal(java.lang.Enum)
		 */
		@Override
		public boolean getStatusLocal(Enum objEnumStatusIn) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(objEnumStatusIn==null) {
					break main;
				}
				
				ClientMainOVPN.STATUSLOCAL enumStatus = (STATUSLOCAL) objEnumStatusIn;
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
		abstract public boolean setStatusLocal(Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;

		@Override
		public boolean[] setStatusLocal(Enum[] objaEnumStatusIn, boolean bStatusValue) throws ExceptionZZZ {
			boolean[] baReturn=null;
			main:{
				if(!ArrayUtilZZZ.isEmpty(objaEnumStatusIn)) {
					baReturn = new boolean[objaEnumStatusIn.length];
					int iCounter=-1;
					for(Enum objEnumStatus:objaEnumStatusIn) {
						iCounter++;
						boolean bReturn = this.setStatusLocal(objEnumStatus, bStatusValue);
						baReturn[iCounter]=bReturn;
					}
				}
			}//end main:
			return baReturn;
		}

		

		@Override
		public boolean proofStatusLocalExists(Enum objEnumStatus) throws ExceptionZZZ {
			return this.proofStatusLocalExists(objEnumStatus.name());
		}
		
		@Override
		public boolean getStatusLocal(String sStatusName) throws ExceptionZZZ {
			boolean bFunction = false;
			main:{
				if(StringZZZ.isEmpty(sStatusName)) break main;
											
				HashMap<String, Boolean> hmStatus = this.getHashMapStatusLocal();
				Boolean objBoolean = hmStatus.get(sStatusName.toUpperCase());
				if(objBoolean==null){
					bFunction = false;
				}else{
					bFunction = objBoolean.booleanValue();
				}
								
			}	// end main:
			
			return bFunction;	
		}
		
		@Override
		abstract public boolean setStatusLocal(String sStatusName, boolean bStatusValue) throws ExceptionZZZ;
		
		@Override
		public boolean[] setStatusLocal(String[] saStatusName, boolean bStatusValue) throws ExceptionZZZ {
			boolean[] baReturn=null;
			main:{
				if(!StringArrayZZZ.isEmptyTrimmed(saStatusName)) {
					baReturn = new boolean[saStatusName.length];
					int iCounter=-1;
					for(String sStatusName:saStatusName) {
						iCounter++;
						boolean bReturn = this.setStatusLocal(sStatusName, bStatusValue);
						baReturn[iCounter]=bReturn;
					}
				}
			}//end main:
			return baReturn;
		}
		


		/**Gibt alle möglichen StatusLocal Werte als Array zurück. 
		 * @return
		 * @throws ExceptionZZZ 
		 */
		@Override
		public String[] getStatusLocal() throws ExceptionZZZ {
			String[] saReturn = null;
			main:{	
				saReturn = StatusLocalHelperZZZ.getStatusLocalDirectAvailable(this.getClass());				
			}//end main:
			return saReturn;
		}
		
		/**Gibt alle "true" gesetzten StatusLocal - Werte als Array zurück. 
		 * @return
		 * @throws ExceptionZZZ 
		 */
		@Override
		public String[] getStatusLocal(boolean bValueToSearchFor) throws ExceptionZZZ {
			return this.getStatusLocal_(bValueToSearchFor, false);
		}
		
		@Override
		public String[] getStatusLocal(boolean bValueToSearchFor, boolean bLookupExplizitInHashMap)throws ExceptionZZZ {
			return this.getStatusLocal_(bValueToSearchFor, bLookupExplizitInHashMap);
		}
		
		private String[]getStatusLocal_(boolean bValueToSearchFor, boolean bLookupExplizitInHashMap) throws ExceptionZZZ{
			String[] saReturn = null;
			main:{
				ArrayList<String>listasTemp=new ArrayList<String>();
				
				//FALLUNTERSCHEIDUNG: Alle gesetzten Status werden in der HashMap gespeichert. Aber die noch nicht gesetzten FlagZ stehen dort nicht drin.
				//                                  Diese kann man nur durch Einzelprüfung ermitteln.
				if(bLookupExplizitInHashMap) {
					HashMap<String,Boolean>hmStatus=this.getHashMapStatusLocal();
					if(hmStatus==null) break main;
					
					Set<String> setKey = hmStatus.keySet();
					for(String sKey : setKey){
						boolean btemp = hmStatus.get(sKey);
						if(btemp==bValueToSearchFor){
							listasTemp.add(sKey);
						}
					}
				}else {
					//So bekommt man alle Flags zurück, also auch die, die nicht explizit true oder false gesetzt wurden.						
					String[]saStatus = this.getStatusLocal();
					
					//20211201:
					//Problem: Bei der Suche nach true ist das egal... aber bei der Suche nach false bekommt man jedes der Flags zurück,
					//         auch wenn sie garnicht gesetzt wurden.
					//Lösung:  Statt dessen explitzit über die HashMap der gesetzten Werte gehen....						
					for(String sStatus : saStatus){
						boolean btemp = this.getStatusLocal(sStatus);
						if(btemp==bValueToSearchFor ){ //also 'true'
							listasTemp.add(sStatus);
						}
					}
				}
				saReturn = listasTemp.toArray(new String[listasTemp.size()]);
			}//end main:
			return saReturn;
		}
		
		@Override
		public boolean proofStatusLocalExists(String sStatusName) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(StringZZZ.isEmpty(sStatusName))break main;
				bReturn = StatusLocalHelperZZZ.proofStatusLocalDirectExists(this.getClass(), sStatusName);				
			}//end main:
			return bReturn;
		}
		
		@Override
		public boolean proofStatusLocalChanged(Enum objEnumStatus, boolean bValue) throws ExceptionZZZ {
			return this.proofStatusLocalChanged(objEnumStatus.name(), bValue);
		}

		@Override
		public boolean proofStatusLocalChanged(String sStatusName, boolean bValue) throws ExceptionZZZ {
			boolean bReturn = false;
			main:{
				if(StringZZZ.isEmpty(sStatusName))break main;
				
				HashMap<String,Boolean>hmStatusLocal = this.getHashMapStatusLocal();
				bReturn = StatusLocalHelperZZZ.proofStatusLocalChanged(hmStatusLocal, sStatusName, bValue);
				
			}//end main:
			return bReturn;
		}

	
}
