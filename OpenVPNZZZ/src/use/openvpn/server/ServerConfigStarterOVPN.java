package use.openvpn.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import use.openvpn.AbstractConfigStarterOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.IConfigMapper4BatchOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.SetZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.FileTextWriterZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;


public class ServerConfigStarterOVPN extends AbstractConfigStarterOVPN{	
	public static final String sBATCH_STARTER_PREFIX="starter_";
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, objFileConfigOvpn, "0", saFlagControl);
	}
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileConfigOvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, objFileConfigOvpn, sMyAlias, saFlagControl);
	}
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain,  File objFileTemplateBatch, File objFileConfigOvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, objFileTemplateBatch, objFileConfigOvpn, sMyAlias, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, ServerMainZZZ objServer, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,(IMainOVPN) objServer, objFileTemplateBatch, objFileConfigOvpn, "-1", saFlagControl);
	}
			
	public Process requestStart() throws ExceptionZZZ{
		Process objReturn = null;
		main:{
			String sCommandConcrete=null;
			try {
				this.getLogObject().WriteLineDate("Trying to find OVPNExecutable.");
				File objFileExe = ConfigFileTemplateOvpnOVPN.findFileExe();
				if(objFileExe==null){
					ExceptionZZZ ez = new ExceptionZZZ( "Executabel associated with .ovpn can not be found.", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}else if(objFileExe.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ("Executabel associated with .ovpn does not exist: '"+objFileExe.getPath()+"'", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}else if(objFileExe.isFile()==false){
					ExceptionZZZ ez = new ExceptionZZZ("Executabel associated with .ovpn is not a file: '"+objFileExe.getPath()+"'", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.getLogObject().WriteLineDate("OVPNExecutable found");
				
				String sCommandParameter = ConfigFileTemplateOvpnOVPN.readCommandParameter();
				String sCommand = null;
				if(sCommandParameter!=null){
					if(sCommandParameter.equals("")){
						sCommand = objFileExe.getPath();
					}else{
						sCommand = objFileExe.getPath() + " " + sCommandParameter;
					}
				}else{
					sCommand = objFileExe.getPath();
				}
				
				
				//sCommandConcrete = StringZZZ.replace(sCommand, "%1", this.getFileConfig().getName());
				//sCommandConcrete = StringZZZ.replace(sCommand, "\"%1\"", this.getFileConfig().getName());
				sCommandConcrete = StringZZZ.replace(sCommand, "\"%1\"", this.getFileConfigOvpn().getPath());
				//System.out.println(sCommandConcrete);
				//load.exec("cmd.exe /K " +  sCommandConcrete);
			//	load.exec("cmd.exe /K C:\\Programme\\OpenVPN\\bin\\openvpn.exe"); // --pause-exit --config client_itelligence.ovpn");
				
				
				/* DAS LIEFERT WENIGSTESN EIEN AUSGABE ALLER FELENDEN PARAMETER
				Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe" );
			    BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );
			    for ( String s; (s = in.readLine()) != null; ){
			      System.out.println( s );
			    }
				*/
				Runtime load = Runtime.getRuntime();
				if (this.getFlag("byBatch")==false){			
					this.getLogObject().WriteLineDate("Excecuting direkt 'not implemented'");									
				}else{
					//Das funktioniert das beim Server, indirekt über eine Batch starten					
					//Name der zu verwendenden Batch Datei ausrechnen.
					String sBatch = this.computeBatchPath();
					
					//0. Bestehende Batch Datei suchen und löschen
					boolean  bBatchExists = FileEasyZZZ.exists(sBatch);
					if(bBatchExists) {
						this.getLogObject().WriteLineDate("Deleting existing batch file: '"+sBatch +"'.");
						boolean bSuccess = FileEasyZZZ.removeFile(sBatch);
						if(bSuccess) {
							this.getLogObject().WriteLineDate("Existing batch file successful deleted.");
						}else {
							this.getLogObject().WriteLineDate("Unable to delete existing batch.");
						}
					}
					
					File fileConfigOvpn = this.getFileConfigOvpn();

					//1. Batch File neu erstellen (wg. ggfs. anderen/neuen Code)				
					FileTextWriterZZZ objBatch = new FileTextWriterZZZ(sBatch);	 	//2020020208: es gibt jetzt den FileTextWriterZZZ im Kernel Projekt.				
					ArrayList<String> listaLine = this.computeBatchLines(this.getFileTemplateBatch(), fileConfigOvpn);
					for(String sLine : listaLine){
						objBatch.writeLine(sLine);
					}
					
					//2. Batch File starten
					ConfigChooserOVPN objPathConfig = new ConfigChooserOVPN(this.getKernelObject(), this.getOvpnContextUsed(), this.getServerObject().getApplicationObject());				
					String sCommandBatch = sBatch; //objPathConfig.getDirectoryConfig()+ File.separator+"starter_"+ this.getFileConfig().getName() + ".bat";
					this.getLogObject().WriteLineDate("Excecuting by Batch '"+ sCommandBatch +"'");				
					objReturn = load.exec("cmd /c " + sCommandBatch);
				}//END if
				
				
			} catch (IOException e) {
				String sError = "ReflectCodeZZZ.getPositionCurrent() + \": \" + IOException ('"+e.getMessage()+"') executing the commandline: '"+ sCommandConcrete +"'";
				System.out.println(sError);
				this.getLogObject().WriteLineDate(sError);
				ExceptionZZZ ez = new ExceptionZZZ(sError, iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			} 
		}
		this.setProcess(objReturn);
		return objReturn;
	}
	
	
	//### noch nicht im Interface
	public String computeBatchPath() throws ExceptionZZZ {
		String sReturn = null;
		main:{
			String sBatchName = this.computeBatchName();
			if(StringZZZ.isEmpty(sBatchName))break main;
			
			String sBatchDirectory = this.computeBatchDirectory();
			if(StringZZZ.isEmpty(sBatchDirectory))break main;
			
			sReturn = sBatchDirectory + File.separator+ sBatchName;
		}
		return sReturn;
	}
	public String computeBatchName() {
		String sReturn = null;
		main:{			
			File objFile = this.getFileConfigOvpn();
			if(objFile==null)break main;
			sReturn = sBATCH_STARTER_PREFIX + objFile.getName() + ".bat";
		}
		return sReturn;
	}
	public String computeBatchDirectory() throws ExceptionZZZ {
		return this.getServerObject().getConfigChooserObject().getDirectoryConfig().getAbsolutePath();
	}
	
	
	
	public ServerMainZZZ getServerObject() {
		return (ServerMainZZZ) this.getMainObject();
	}
	public void setServerObject(ServerMainZZZ objServer) {
		this.setMainObject((IMainOVPN) objServer);
	}
	
	public ServerConfigMapper4BatchOVPN getServerConfigMapper4BatchObject() throws ExceptionZZZ {
		return (ServerConfigMapper4BatchOVPN) this.getConfigMapperObject();		
	}
	public void setServerConfigMapper4BatchObject(ServerConfigMapper4BatchOVPN objConfigMapper) {
		this.setConfigMapperObject(objConfigMapper);
	}
	public IConfigMapper4BatchOVPN createConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4BatchOVPN objReturn = null;
		main:{
			File fileConfigTemplateBatch = this.getFileTemplateBatch();
			File fileConfigOvpn = this.getFileConfigOvpn();			
			objReturn = new ServerConfigMapper4BatchOVPN(this.getKernelObject(), this.getServerObject(), fileConfigTemplateBatch, fileConfigOvpn);			
		}//end main:
		return objReturn;		
	}

	
}//END class
