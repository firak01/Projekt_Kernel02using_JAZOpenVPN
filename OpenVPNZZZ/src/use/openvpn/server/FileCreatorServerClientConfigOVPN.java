package use.openvpn.server;

import java.io.File;
import java.util.ArrayList;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.file.AbstractFileCreatorZZZ;
import basic.zKernel.IKernelZZZ;
import use.openvpn.IConfigMapper4ReadmeOVPN;
import use.openvpn.IConfigMapper4ReadmeUserOVPN;
import use.openvpn.IConfigMapper4ServerClientConfigOVPN;
import use.openvpn.IConfigMapper4ServerClientConfigUserOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.IMainUserOVPN;

public class FileCreatorServerClientConfigOVPN extends AbstractFileCreatorZZZ implements IConfigMapper4ServerClientConfigUserOVPN, IMainUserOVPN{	
	private IMainOVPN objMain=null;	
	private IConfigMapper4ServerClientConfigOVPN objConfigMapper=null;
	
	public FileCreatorServerClientConfigOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File fileTemplate, String sTargetPath) throws ExceptionZZZ {
		super(objKernel, fileTemplate, sTargetPath);
		FileCreatorClientConfig_(objMain);
	}
	private boolean FileCreatorClientConfig_(IMainOVPN objMain) {
		boolean bReturn = false;
		main:{
			this.setMainObject(objMain);			
			bReturn = true;
		}//end main
		return bReturn;
	}
	
	

	public ArrayList<String> computeLines(File fileConfigTemplateClientConfig) throws ExceptionZZZ {		
		ArrayList<String>listasReturn=new ArrayList<String>();
		main:{			
			IConfigMapper4ServerClientConfigOVPN objMapperReadme = this.getConfigMapperObject(); 
			HashMapIterableKeyZZZ<String, String>hmReadmeLines = objMapperReadme.readTaskHashMap();								
							
			for(String sKey : hmReadmeLines) {
				String sLine = hmReadmeLines.getValue(sKey);
				listasReturn.add(sLine);
			}				
		}
		return listasReturn;
	}
	
	@Override
	public IConfigMapper4ServerClientConfigOVPN getConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4ServerClientConfigOVPN objReturn = null;
		main:{		
		//1. Nachsehen, ob das Objekt als private gesetzt ist
		if(this.objConfigMapper==null) {
			//2. Falls nein, erzeugen.
			this.objConfigMapper = this.createConfigMapperObject();
		}
		
		objReturn = this.objConfigMapper;
		}//end main:
		return objReturn;
	}

	@Override
	public void setConfigMapperObject(IConfigMapper4ServerClientConfigOVPN objConfigMapperServerClientConfig) {
		this.objConfigMapper = objConfigMapperServerClientConfig;
	}
	
	public IConfigMapper4ServerClientConfigOVPN createConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4ServerClientConfigOVPN objReturn = null;
		main:{
			File fileConfigTemplateReadme = this.getTemplateFile();	
			IMainOVPN objMain = this.getMainObject();
			objReturn = new ServerConfigMapper4ServerClientConfigOVPN(this.getKernelObject(), objMain, fileConfigTemplateReadme);
		}//end main:
		return objReturn;		
	}
	
	//### Getter / Setter
	public IMainOVPN getMainObject() {
		return this.objMain;
	}
	public void setMainObject(IMainOVPN objMain) {
		this.objMain = objMain;
	}


}
