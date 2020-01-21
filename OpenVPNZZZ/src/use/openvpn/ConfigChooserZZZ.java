package use.openvpn;

import java.io.File;

import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;

import use.openvpn.client.OVPNFileFilterConfigTemplateZZZ;
import use.openvpn.client.OVPNFileFilterConfigUsedZZZ;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.KernelZZZ;

public class ConfigChooserZZZ extends KernelUseObjectZZZ{
	private File objFileDirRoot = null;
	private File objFileDirTemplate = null;
	
	public ConfigChooserZZZ(IKernelZZZ objKernel){
		super(objKernel);
	}
	
	
	public File findDirectoryRoot() throws ExceptionZZZ{
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
				objFileExe = ConfigFileZZZ.findFileExe();
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
			String sDir = objFileExe.getParent();
			
			File objFileDir = new File(sDir);
			sDir = objFileDir.getParent();
			objReturn = new File(sDir);					
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
			OVPNFileFilterConfigZZZ objFilterConfig = new OVPNFileFilterConfigZZZ();			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
	}
	
	
	/**Finds configuration files started with "'Template * .ovpn'.
	 * This is usefull for the client-configuration - starter, because this template file is used to create the real configuration files (which then will e.g. get an updated 'remote' entry -line). 
	 * @param objDirectoryin
	 * @return
	 * @throws ExceptionZZZ, 
	 *
	 * @return File[]
	 *
	 * javadoc created by: 0823, 18.07.2006 - 08:38:44
	 */
	public File[] findFileConfigTemplate(File objDirectoryin) throws ExceptionZZZ{
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
			OVPNFileFilterConfigTemplateZZZ objFilterConfig = new OVPNFileFilterConfigTemplateZZZ();			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
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
			OVPNFileFilterConfigUsedZZZ objFilterConfig = new OVPNFileFilterConfigUsedZZZ();			
			objaReturn = objDirectory.listFiles(objFilterConfig);
			
		}//End main
		return objaReturn;
	}
	
	
	public String readDirectoryConfigPath() throws ExceptionZZZ{
		String sReturn = new String("");
		main:{
		
			File objDirRoot = this.getDirectoryRoot();
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
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive template directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}

				objReturn = FileEasyZZZ.searchDirectory(sDirTemplate);
				if(objReturn.exists()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + objReturn.getAbsolutePath() + "', does not exist (for '" + sDirTemplate + "').", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}else if(objReturn.isDirectory()==false){
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The path '" + objReturn.getAbsolutePath() + "'(for '" + sDirTemplate + "'),  was expected to be a directory, not e.g. a file.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
		}//End main
		return objReturn;		
	}
	
	//####### Getter / Setterr
	public File getDirectoryRoot() throws ExceptionZZZ{
		if(this.objFileDirRoot==null){
			return this.findDirectoryRoot();
		}else{
			return this.objFileDirRoot;
		}
	}
	public void setDirectoryRoot(File objDir){
		this.objFileDirRoot = objDir;
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
	
	
}
