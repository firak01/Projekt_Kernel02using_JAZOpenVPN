package use.openvpn;

import java.util.ArrayList;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public abstract class AbstractMainOVPN extends KernelUseObjectZZZ implements Runnable,IMainOVPN{
	private IApplicationOVPN objApplication = null;
	private ConfigChooserOVPN objConfigChooser = null;
	private IConfigMapper4TemplateOVPN objConfigMapper = null;
	//private IConfigMapperOVPN objConfigMapper = null;
	
	
	private String sStatusCurrent = null; //Hier�ber kann das Frontend abfragen, was gerade in der Methode "start()" so passiert.
	private ArrayList listaStatus = new ArrayList(); //Hier�ber werden alle gesetzten Stati, die in der Methode "start()" gesetzt wurden festgehalten.
	                                                                      //Ziel: Das Frontend soll so Infos im laufende Prozess per Button-Click abrufen k�nnen.

	
	public AbstractMainOVPN(IKernelZZZ objKernel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
	}
	
	/**Adds a line to the status arraylist PLUS writes a line to the kernel-log-file.
	 * Remark: The status arraylist is used to enable the frontend-client to show a log dialogbox.
	* @param sStatus 
	* 
	* lindhaueradmin; 13.07.2006 08:38:51
	 */
	public void logStatusString(String sStatus){
		if(sStatus!=null){
			this.addStatusString(sStatus);
			
			IKernelZZZ objKernel = this.getKernelObject();
			if(objKernel!= null){
				objKernel.getLogObject().WriteLineDate(sStatus);
			}
		}
	}
	
	/** Adds a line to the status arraylist. This status is used to enable the frontend-client to show a log dialogbox.
	 * Remark: This method does not write anything to the kernel-log-file. 
	* @param sStatus 
	* 
	* lindhaueradmin; 13.07.2006 08:34:56
	 */
	public void addStatusString(String sStatus){
		if(sStatus!=null){
			this.sStatusCurrent = sStatus;
			this.listaStatus.add(sStatus);
		}
	}
	
	
	//##### GETTER / SETTER
	
	/**This status is a type of "Log".
	 * This is the last entry.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public String getStatusStringCurrent(){
		return this.sStatusCurrent;
	}

	/**This status is a type of "Log".
	 * This are all entries.
	 * This is filled by ".addStatusString(...)"
	 * @return String
	 *
	 * javadoc created by: 0823, 17.07.2006 - 09:00:55
	 */
	public ArrayList getStatusStringAll(){
		return this.listaStatus;
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
