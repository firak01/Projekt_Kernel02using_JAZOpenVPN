package use.openvpn;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;

import use.openvpn.client.FileFilterConfigBatchTemplateOVPN;
import use.openvpn.client.FileFilterConfigOvpnTemplateInJarOVPN;
import use.openvpn.client.FileFilterConfigOvpnTemplateOVPN;
import use.openvpn.client.FileFilterConfigOvpnUsedOVPN;
import use.openvpn.server.FileFilterServerClientConfigReadmeTemplateOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.ArrayListZZZ;
import basic.zBasic.util.datatype.calling.ReferenceArrayZZZ;
import basic.zBasic.util.datatype.calling.ReferenceZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.ResourceEasyZZZ;
import basic.zBasic.util.file.jar.JarInfo;
import basic.zBasic.ReflectCodeZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.KernelZZZ;

public class ConfigChooserOVPN extends KernelUseObjectZZZ implements IApplicationUserOVPN{
	private IApplicationOVPN objApplication = null;
	private File objFileDirExe = null;
	private File objFileDirExeRoot = null;
	private File objFileDirTemplate = null;
	private String sOvpnContextClientOrServer=null;
	
	public ConfigChooserOVPN(IKernelZZZ objKernel, String sOvpnContextClientOrServer, IApplicationOVPN objApplication){
		super(objKernel);
		this.setOvpnContextUsed(sOvpnContextClientOrServer);
		this.setApplicationObject(objApplication);
	}
	
	public File findDirectoryExe() throws ExceptionZZZ{
		File objReturn = null;
		main:{
			//+++ Prüfen, ob nicht ein anderes Verzeichnis konfiguriert ist
			String sFile = objKernel.getParameterByProgramAlias("OVPN","ProgConfigHandler","LocalMachineDirectoryRoot").getValue();
			boolean bUseSearch = false;
			if(sFile==null){
				bUseSearch = true;
			}else if(sFile.equals("")){
				bUseSearch = true;
			}
			
			File objFileExe = null;
			if(bUseSearch == true){				
				//AUS DER KOMMANDOZEILE F�R DEN AUFRUF, DAS ROOT-VERZEICNIS DER APPLIKATION ERMITTELN
				objFileExe = ConfigFileTemplateOvpnOVPN.findFileExe();
			}else{
				objFileExe = new File(sFile);
			}//END if bUseSearch == true;
			
			//++++++++++++  Das Verzeichnis �berr�fen			
		
			if(objFileExe.exists()==false){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + sFile + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(objFileExe.isFile()==false){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + sFile + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			//Nun das Verzeichnis holen. Es wird erwartet, dass die configuration dort liegt. 
			objReturn = objFileExe.getParentFile();								
		}//END main
		return objReturn;
	}
	
	public File findDirectoryExeRoot() throws ExceptionZZZ{
		File objReturn = null;
		main:{
			File objFileExe = this.findDirectoryExe();
			objReturn = objFileExe.getParentFile();					
		}//END main
		return objReturn;
	}
	

	
	/**Finds All Configuration files. Just look at the ending "*.ovpn"
	 * @param objDirectoryin
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return File[]
	 *
	 * javadoc created by: 0823, 18.07.2006 - 08:46:24
	 */
	public File[] findFileConfigAll(File objDirectoryin) throws ExceptionZZZ{
		File[] objaReturn = null;
		main:{
			File objDirectory=null;
			check:{
				if(objDirectoryin==null){
					objDirectory = this.getDirectoryConfig();
					if(objDirectory==null){				
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to get the configuration directory.'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
				}else{
					objDirectory = objDirectoryin;
				}
								
				if(objDirectory.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Directory '" + objDirectory.getPath() + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objDirectory.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + objDirectory.getPath() + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}//End check
			
			//##############################################################
//			Alle Dateien auflisten, dazu aber einen FileFilter verwenden
			FileFilterConfigOVPN objFilterConfig = new FileFilterConfigOVPN();			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
	}
	
	
	/**Finds configuration files started with "'template * .ovpn'.
	 * This is usefull for the client-configuration - starter, because this template file is used to create the real configuration files (which then will e.g. get an updated 'remote' entry -line). 
	 * @param objDirectoryin
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return File[]
	 *
	 * javadoc created by: 0823, 18.07.2006 - 08:38:44
	 */
	public File[] findFileConfigOvpnTemplates() throws ExceptionZZZ{
		return this.findFileConfigOvpnTemplates(null);
	}
	public File[] findFileConfigOvpnTemplates(File objDirectoryin) throws ExceptionZZZ{
		File[] objaReturn = null;
		main:{
			File objDirectory=null;
			check:{
				if(objDirectoryin==null){
					objDirectory = this.getDirectoryTemplate();
					if(objDirectory==null){				
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to get the template directory.'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
				}else{
					objDirectory = objDirectoryin;
				}
								
				if(objDirectory.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Directory '" + objDirectory.getPath() + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(FileEasyZZZ.isJar(objDirectory)) {
					String sLog = "Directory for templates is in jar: '" + objDirectory.getAbsolutePath() + "'";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
				}else if(objDirectory.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + objDirectory.getPath() + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}//End check
			
			//##############################################################
//			//Alle Dateien auflisten, dazu aber einen FileFilter verwenden
			
			//START1: ZUM DEBUGGEN DES JAR INHALTS DEN NORMALFALL AUSKOMMENTIEREN UND IN ECLIPSE STARTEN (geschweifte Klammer untennicht vergessen)
			if(!FileEasyZZZ.isJar(objDirectory)) {
				//A) Normal				
				FileFilterConfigOvpnTemplateOVPN objFilterConfig = new FileFilterConfigOvpnTemplateOVPN(this.getOvpnContextUsed(), "REGARD_FILE_EXPANSION_LAST");			
				objaReturn = ResourceEasyZZZ.findFile(objDirectory, objFilterConfig);
			}else {
			//ENDE1: ZUM DEBUGGEN DES JAR INHALTS DEN NORMALFALL AUSKOMMENTIEREN UND IN ECLIPSE STARTEN
			
				//B) IN JAR Datei, das ist objDirectory
				//START2: ZUM DEBUGGEN DES JAR INHALTS DEN NORMALFALL EINKOMMENTIEREN UND IN ECLIPSE STARTEN
//				IApplicationOVPN objApplication = this.getApplicationObject();
//				IMainOVPN objMain = objApplication.getMainObject();
//				String sJarPath = objMain.getJarFilePathUsed();
//				File objJarAsDirectoryMock = new File(sJarPath);
//				objDirectory = objJarAsDirectoryMock;
				//ENDE2: ZUM DEBUGGEN DES JAR INHALTS DEN NORMALFALL EINKOMMENTIEREN UND IN ECLIPSE STARTEN
				
				FileFilterConfigOvpnTemplateInJarOVPN objFilterConfigInJar = new FileFilterConfigOvpnTemplateInJarOVPN(this.getOvpnContextUsed());
				String sDirTemplate = this.readDirectoryTemplatePath();				
				String sApplicationKey = this.getKernelObject().getApplicationKey();
				objaReturn = ResourceEasyZZZ.findFileInJar(objDirectory, sDirTemplate, objFilterConfigInJar, sApplicationKey);
			}
		}//End main		 		
		return objaReturn;
	}
	
	/**Finds configuration files started with "'template * .ovpn'.
	 * PLUS: The right ones for the given Context.
	 * This is usefull for the configuration - starter, because this template file is used to create the real batch files. 
	 * @param objDirectoryin
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return File[]
	 *
	 * javadoc created by: 0823, 18.07.2006 - 08:38:44
	 */
	public File[] findFileConfigBatchTemplates() throws ExceptionZZZ{
		return this.findFileConfigBatchTemplates(null);
	}
	public File[] findFileConfigBatchTemplates(File objDirectoryin) throws ExceptionZZZ{
		File[] objaReturn = null;
		main:{
			File objDirectory=null;
			check:{
				if(objDirectoryin==null){
					objDirectory = this.getDirectoryTemplate();
					if(objDirectory==null){				
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to get the template directory.'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
				}else{
					objDirectory = objDirectoryin;
				}
								
				if(objDirectory.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Directory '" + objDirectory.getPath() + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objDirectory.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + objDirectory.getPath() + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}//End check
			
			//##############################################################
//			Alle Dateien auflisten, dazu aber einen FileFilter verwenden
			FileFilterConfigBatchTemplateOVPN objFilterConfig = new FileFilterConfigBatchTemplateOVPN(this.getOvpnContextUsed());			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
	}
	/**Normalerweise gibt es nur 1 Batch-Starter Template für den ausgewählten Context.
	 * Das bekommt man hiermit.
	 * @return
	 * @throws ExceptionZZZ
	 * @author Fritz Lindhauer, 20.02.2020, 17:44:02
	 */
	public File findFileConfigBatchTemplateFirst() throws ExceptionZZZ{
		File objReturn = null;		
		main:{
			File[] objaFileTemplates = this.findFileConfigBatchTemplates();
			
			 //Ich weiss komplizierter als notwendig.
			 //Aber schon mal die Idee aus ReferenceArrayZZZ übernommen
			 //a) generisch die Sache anzugehen, b) aus dem Array mehrer Werte noch etwas Spezielles herauszufiltern.
			 if(objaFileTemplates.getClass().isArray()) {
    			//listaReferent.addAll(initialValue); GEHT NICHT SO
    			
    			//int iMax = Array.getLength(initialValue)-1;
    			//for(int i=0; i<=iMax;i++) {
    				Object objTemp = Array.get(objaFileTemplates, 0);
    				objReturn = (File)objTemp;
			 }
		}//end main:
		return objReturn;
	}
	
	/**Finds configuration files. Everything in the configuration directory with the ending .ovpn,
	 * but this method will not include the "Template * .ovpn" files.
	 * 
	 * @param objDirectoryin
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return File[]
	 *
	 * javadoc created by: 0823, 18.07.2006 - 08:44:26
	 */
	public File[] findFileConfigUsed() throws ExceptionZZZ{
		return this.findFileConfigUsed(null);
	}
	public File[] findFileConfigUsed(File objDirectoryin) throws ExceptionZZZ{
		File[] objaReturn = null;
		main:{
			File objDirectory=null;
			check:{
				if(objDirectoryin==null){
					objDirectory = this.getDirectoryConfig();
					if(objDirectory==null){				
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to get the configuration directory.'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
				}else{
					objDirectory = objDirectoryin;
				}
								
				if(objDirectory.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Directory '" + objDirectory.getPath() + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objDirectory.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + objDirectory.getPath() + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}//End check
			
			//##############################################################
//			Alle Dateien auflisten, dazu aber einen FileFilter verwenden
			FileFilterConfigOvpnUsedOVPN objFilterConfig = new FileFilterConfigOvpnUsedOVPN(this.getOvpnContextUsed(), "REGARD_FILE_EXPANSION_LAST");			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
	}
	
	public int removeFileConfigUsed(File objDirectoryin, ReferenceArrayZZZ<String> strStatusUpdate) throws ExceptionZZZ{
		int iReturn = -1;
		main:{
			File objDirectory=null;
			check:{
				if(objDirectoryin==null){
					objDirectory = this.getDirectoryConfig();
					if(objDirectory==null){				
							ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_MISSING + "Unable to get the configuration directory.'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
							throw ez;
						}
				}else{
					objDirectory = objDirectoryin;
				}
								
				if(objDirectory.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Directory '" + objDirectory.getPath() + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objDirectory.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The file '" + objDirectory.getPath() + "', was expected to be a file, not e.g. a directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}//End check
			
			//##############################################################
			iReturn = 0;
			File[] objaFileConfigUsed = this.findFileConfigUsed(objDirectory);
			if(objaFileConfigUsed==null){
//				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
//				throw ez;
				//this.logStatusString("No previously used file was found (null case). Nothing removed.");
				strStatusUpdate.add("No previously used file was found (null case). Nothing removed.");
				
			}else if(objaFileConfigUsed.length == 0){
//				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No configuration file (ending .ovpn) was found in the directory: '" + objChooser.readDirectoryConfigPath() + "'", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
//				throw ez;
				//this.logStatusString("No previously used file was found (0 case). Nothing removed.");
				strStatusUpdate.add("No previously used file was found (0 case). Nothing removed.");
				
			}else{
//				this.logStatusString(objaFileConfig.length + " configuration file(s) were found in the directory: '" + objChooser.readDirectoryConfigPath() + "'");  //Dar�ber kann dann ggf. ein Frontend den laufenden Process beobachten.
				//this.logStatusString("Trying to remove previously used file(s): " + objaFileConfigUsed.length);
				strStatusUpdate.add("Trying to remove previously used file(s): " + objaFileConfigUsed.length);
				for(int icount = 0; icount <= objaFileConfigUsed.length-1; icount++){
					boolean btemp = objaFileConfigUsed[icount].delete();
					if(btemp==true){
						iReturn=iReturn+1;
						//this.logStatusString( "File successfully removed: '" + objaFileConfigUsed[icount].getPath()+"'");		
						strStatusUpdate.add("File successfully removed: '" + objaFileConfigUsed[icount].getPath()+"'");
					}else{
						//this.logStatusString("Unable to remove file: '" + objaFileConfigUsed[icount].getPath()+"'");	
						String stemp = "Unable to remove file: '" + objaFileConfigUsed[icount].getPath()+"'";
						ExceptionZZZ ez = new ExceptionZZZ(stemp, iERROR_RUNTIME, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}					
				}//END for
			}
			
		}//End main
		return iReturn;
	}
	
	
	public String readDirectoryConfigPath() throws ExceptionZZZ{
		String sReturn = new String("");
		main:{
		
			File objDirRoot = this.getDirectoryExeRoot();
			if(objDirRoot==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive root directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			sReturn = objDirRoot.getPath();
			String sConfig = objKernel.getParameterByProgramAlias("OVPN","ProgConfigHandler","LocalMachineDirectoryChildConfig").getValue();			
			sReturn = sReturn + File.separator + sConfig;
		}//End main
		return sReturn;
	}
	
	public File getDirectoryConfig() throws ExceptionZZZ{
		File objReturn = null;
		main:{			
				String sDirConfig = this.readDirectoryConfigPath();
				if(sDirConfig==null){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive configuration directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(sDirConfig.equals("")){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive configuration directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}

				objReturn = new File(sDirConfig);
				if(objReturn==null){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The Path '" + sDirConfig + "', was not found (NULL was returned).", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				if(objReturn.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + sDirConfig + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objReturn.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The path '" + sDirConfig + "', was expected to be a directory, not e.g. a file.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
		}//End main
		return objReturn;		
	}
	
	public String readDirectoryTemplatePath() throws ExceptionZZZ{
		String sReturn = new String("");
		main:{		
			sReturn = objKernel.getParameterByProgramAlias("OVPN","ProgConfigHandler","DirectoryTemplate").getValue();						
		}//End main
		return sReturn;
	}
	
	public File findDirectoryTemplate() throws ExceptionZZZ{
		File objReturn = null;
		main:{			
				String sDirTemplate = this.readDirectoryTemplatePath();
				if(sDirTemplate==null){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive path for template directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				String sLogTEST = "TESETSTESSETSTSTESTSETSTETETSTT: sDirTemplate='" + sDirTemplate + "'";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLogTEST);
				
				objReturn = FileEasyZZZ.searchDirectory(sDirTemplate,true); //true=ggfs. in einer Jar-Datei suchen, falls vorher nicht gefunden.
				if(objReturn==null){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + sDirTemplate + "', was not found (NULL was returned).", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				if(objReturn.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + objReturn.getAbsolutePath() + "', does not exist (for '" + sDirTemplate + "').", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(FileEasyZZZ.isJar(objReturn)) {
					String sLog = "Directory for '" + sDirTemplate + "'is in jar: '" + objReturn.getAbsolutePath() + "'";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sLog);
				}else if(objReturn.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The path '" + objReturn.getAbsolutePath() + "'(for '" + sDirTemplate + "'),  was expected to be a directory, not e.g. a file.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
		}//End main
		return objReturn;		
	}
	
	
	
	//####### Getter / Setter
	public File getDirectoryExeRoot() throws ExceptionZZZ{
		if(this.objFileDirExeRoot==null){
			this.objFileDirExeRoot = this.findDirectoryExeRoot();
		}
		return this.objFileDirExeRoot;		
	}
	/** Private, da das Verzeichnis primär von der exe-Installation abhängt und nicht frei definiert werden kann.
	 * @param objDir
	 */
	private void setDirectoryExeRoot(File objDir){
		this.objFileDirExeRoot = objDir;
	}	
	public File getDirectoryExe() throws ExceptionZZZ{
		if(this.objFileDirExe==null){
			this.objFileDirExe = this.findDirectoryExe();
		}
		return this.objFileDirExe;		
	}
	
	/** Private, da das Verzeichnis primär von der exe-Installation abhängt und nicht frei definiert werden kann.
	 * @param objDir
	 */
	private void setDirectoryExe(File objDir){
		this.objFileDirExe = objDir;
	}
	
	public File getDirectoryTemplate() throws ExceptionZZZ{
		if(this.objFileDirTemplate==null){
			this.objFileDirTemplate = this.findDirectoryTemplate();		
		}
		return this.objFileDirTemplate;		
	}
	public void setDirectoryTemplate(File objDir){
		this.objFileDirTemplate = objDir;
	}
	
	public String getOvpnContextUsed() {
		return this.sOvpnContextClientOrServer;
	}
	public void setOvpnContextUsed(String sOvpnContextClientOrServer) {
		this.sOvpnContextClientOrServer = sOvpnContextClientOrServer;
	}

	@Override
	public IApplicationOVPN getApplicationObject() {
		return this.objApplication;
	}

	@Override
	public void setApplicationObject(IApplicationOVPN objApplication) {
		this.objApplication = objApplication;
	}
	
}
