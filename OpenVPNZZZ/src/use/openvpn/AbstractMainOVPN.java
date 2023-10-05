package use.openvpn;

import java.util.ArrayList;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import use.openvpn.client.status.IEventObjectStatusLocalSetOVPN;

public abstract class AbstractMainOVPN extends KernelUseObjectZZZ implements Runnable,IMainOVPN{
	protected IApplicationOVPN objApplication = null;
	protected ConfigChooserOVPN objConfigChooser = null;
	protected IConfigMapper4TemplateOVPN objConfigMapper = null;
	//private IConfigMapperOVPN objConfigMapper = null;
	
	
	protected String sMainStatus = null; //Hierueber kann das Frontend abfragen, was gerade in der Methode "start()" so passiert.
	protected String sMainStatusPrevious = null;
	
	protected String sMessage = null; //wird als Protokoll verwendet
	protected ArrayList<String> listaMessage = new ArrayList<String>(); //Hierueber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
	                                                                   //Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen k�nnen.

	
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
}
