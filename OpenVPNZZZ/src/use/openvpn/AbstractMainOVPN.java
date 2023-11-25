package use.openvpn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractArray.ArrayUtilZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectWithStatusListeningZZZ;
import basic.zKernel.status.StatusLocalHelperZZZ;
import use.openvpn.client.ClientMainOVPN;
import use.openvpn.client.IClientMainOVPN.STATUSLOCAL;
import use.openvpn.client.process.ClientThreadProcessWatchMonitorOVPN;
import use.openvpn.client.status.EventObject4ClientMainStatusLocalSetOVPN;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;

public abstract class AbstractMainOVPN extends AbstractKernelUseObjectWithStatusListeningZZZ implements Runnable,IMainOVPN {
	protected IApplicationOVPN objApplication = null;
	protected ConfigChooserOVPN objConfigChooser = null;
	protected IConfigMapper4TemplateOVPN objConfigMapper = null;
	
	protected String sProtocol= null; //wird als Protokoll verwendet
	protected ArrayList<String> listaProtocol = new ArrayList<String>(); //Hierueber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
		
	public AbstractMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
	}
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sProtocol 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 * @throws ExceptionZZZ 
	 */
	/* (non-Javadoc)
	 * @see use.openvpn.IMainOVPN#logProtocolString(java.lang.String)
	 */
	public void logProtocolString(String sProtocol) throws ExceptionZZZ{
		if(sProtocol!=null){
			this.addProtocolString(sProtocol);
			
			IKernelZZZ objKernel = this.getKernelObject();
			if(objKernel!= null){
				objKernel.getLogObject().WriteLineDate(sProtocol);
			}
		}
	}
	
	/** Adds a line to the status arraylist. This status is used to enable the frontend-client to show a log dialogbox.
	 * Remark: This method does not write anything to the kernel-log-file. 
	* @param sMessage 
	* 
	* lindhaueradmin; 13.07.2006 08:34:56
	 */
	public void addProtocolString(String sProtocol){
		if(sProtocol!=null){
			this.sProtocol = sProtocol;
			this.listaProtocol.add(sProtocol);
		}
	}
	
	
	//##### GETTER / SETTER
	public String getJarFilePathUsed() {
		return AbstractMainOVPN.sJAR_FILE_USED;
	}
	
	
	
	
	/**This status is a type of "Log".
	 * This are all entries.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public ArrayList getProtocolStringAll(){
		return this.listaProtocol;
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
		abstract public boolean setStatusLocalEnum(IEnumSetMappedStatusZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
					
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
		public String[] getStatusLocalAll() throws ExceptionZZZ {
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
					String[]saStatus = this.getStatusLocalAll();
					
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
		
		
		
//		@Override
//		public boolean proofStatusLocalChanged(Enum objEnumStatus, boolean bValue) throws ExceptionZZZ {
//			return this.proofStatusLocalChanged(objEnumStatus.name(), bValue);
//		}

//		@Override
//		public boolean proofStatusLocalChanged(String sStatusName, boolean bValue) throws ExceptionZZZ {
//			boolean bReturn = false;
//			main:{
//				if(StringZZZ.isEmpty(sStatusName))break main;
//								
//				HashMap<String,Boolean>hmStatusLocal = this.getHashMapStatusLocal();
//				bReturn = StatusLocalHelperZZZ.proofStatusLocalChanged(hmStatusLocal, sStatusName, bValue);
//				
//			}//end main:
//			return bReturn;
//		}
		
		
}
