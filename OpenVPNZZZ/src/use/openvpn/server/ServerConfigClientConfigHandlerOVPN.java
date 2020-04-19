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
import use.openvpn.IConfigMapper4ReadmeOVPN;
import use.openvpn.IConfigMapper4ReadmeUserOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.IMainUserOVPN;
import use.openvpn.client.ClientMainZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.HashMapIterableKeyZZZ;
import basic.zBasic.util.abstractList.SetZZZ;
import basic.zBasic.util.datatype.string.StringArrayZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.FileTextWriterZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;


public class ServerConfigClientConfigHandlerOVPN extends KernelUseObjectZZZ implements IMainUserOVPN, IConfigMapper4ReadmeUserOVPN{	
	
	private IMainOVPN objMain=null;
	private IConfigMapper4ReadmeOVPN objConfigMapper=null;
	
	private File fileServerConfigClientTemplate = null;
	
	public ServerConfigClientConfigHandlerOVPN() {
		super();
	}
	public ServerConfigClientConfigHandlerOVPN(IKernelZZZ objKernel, IMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, saFlagControl);
		ServerConfigClientConfigHandler_(objMain, saFlagControl);
	}
	
	private boolean ServerConfigClientConfigHandler_(IMainOVPN objMain, String[] saFlagControl) throws ExceptionZZZ {
		boolean bReturn = false;
		main:{
			if(objMain==null) {
				ExceptionZZZ ez = new ExceptionZZZ( "MainObject (ServerMain) not passed.", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			this.setMainObject(objMain);
			
		}//end main:
		return bReturn;
	}
	
			
	public boolean execute() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			//
			/* Auszug aus der ini - Datei
			 * 
[OVPN!01_ConfigServerClientConfig];Werte werden in die mit Client-config-dir als Verzeichnis angegebenen Dateien (CN-Namen der jeweiligen Client) eingetragen, bzw. dafür verwendet.
DirectoryTemplate=<Z>[OVPN!01_Config]DirectoryTemplate</Z>
FileNameTemplate=template_server_clientconfig.txt

#Mehrere Werte durch Path-Separator getrennt. Die Hostnamen. Für jeden der hier definierten Namen eine Datei mit dem CN-Namen des Hosts (also plus _CLIENT) im TemplateVerzeichnis per Program erstellen.
ClientConfigHostname=HANNIBALDEV04VM
			 */
								
			//+++ Neuen Ordner für die Client-Konfigurations-Dateien "auf dem Server" erstellen.			
			File fileDirectoryServerClientConfig = this.getDirectoryServerClientConfig();						

			String sDirectoryClientConfigPath;
			if(fileDirectoryServerClientConfig!=null) {
				sDirectoryClientConfigPath = fileDirectoryServerClientConfig.getAbsolutePath();
				
				//Zuerst den Ordner samt Inhalt löschen (quasi die alte Konfiguration entfernen)											
				boolean bSuccess = FileEasyZZZ.removeDirectory(fileDirectoryServerClientConfig, true);//true= Entferne zuvor den Inhalt!!!
				if(!bSuccess) {											
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_RUNTIME + " unable to remove directory '" + sDirectoryClientConfigPath + "' (is there a file with the same name?)", iERROR_RUNTIME, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
			}else {
				sDirectoryClientConfigPath = null;
			}
			
			//Neue Konfigurationsdateien erstellen, sofern welche konfiguriert sind.
			String[] saClientConfig = objKernel.getParameterArrayStringByProgramAlias("OVPN","ProgConfigServerClientConfig","ServerClientConfigHostname");
			boolean bUseDirectoryClientConfig = true;
			boolean bUseClientConfig = true;
			if(StringArrayZZZ.isEmpty(saClientConfig)) {
				bUseClientConfig = false;
				bUseDirectoryClientConfig = false;
			}
														 
			if(bUseClientConfig && bUseDirectoryClientConfig) {								
				//1a. Ordner der Templatekonfiguration
				File fileDirectoryConfig = this.getDirectoryTemplate();			
				if(fileDirectoryConfig==null) {
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_RUNTIME + " unable to get template directory '", iERROR_RUNTIME, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				
				//2. Oben gelöschtes Verzeichnis neu erstellen: Den Ordner der ClientConfigurationen (auf dem Server) erstellen.
				boolean bSuccess = FileEasyZZZ.createDirectory(sDirectoryClientConfigPath);
				if(!bSuccess) {											
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_RUNTIME + " unable to create directory '" + sDirectoryClientConfigPath + "' (is there a file with the same name?)", iERROR_RUNTIME, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				
				//#####################################
				//+++ Erstelle 1 Readme Datei in dem neuen Verzeichnis
				//1. Readme-Datei hineinkopieren mit Text, z.B.: Das Verzeichnis wurde automatisch erstellt, aller Inhalt wird auch automatisch wieder gelöscht, bla bla
				
				//1b. Readme Schablone holen.
				File objFileTemplateReadme = this.getFileServerClientConfigReadmeTemplate();
														
				//1c. Hole den Dateinamen der ReadmeDatei.
				String sReadmePath = this.computeServerClientConfigReadmePath();
						
				//1d. Readme File neu erstellen (wg. ggfs. anderen/neuen Code)				
				FileTextWriterZZZ objReadme = new FileTextWriterZZZ(sReadmePath);	 	//2020020208: es gibt jetzt den FileTextWriterZZZ im Kernel Projekt.				
				ArrayList<String> listaLine = this.computeReadmeLines(objFileTemplateReadme);
				for(String sLine : listaLine){
					objReadme.writeLine(sLine);
				}
				
												
				//#####################################
				//+++ Erstelle mehrerer Konfigurationsdateien in dem neuen Verzeichnis			
				TODOGOON;
				
				
				//2a. Das Template für die ServerClientConfig Dateien holen
				String sFileTemplate = objKernel.getParameterByProgramAlias("OVPN","ProgConfigServerClientConfig","FileNameTemplate").getValue();
				
				//Die einzelnen Dateien erstellen - basierend auf einem Template.
				//5. In einer Schleife die Strings durchgehen und Dateien erstellen. Mapping Vorgehen.
				//TODO Nun einen Mapper nutzen, um die Dateien zu befüllen.
				//Neue Klasse ServerConfigMapper4ClientConfigOVPN

			}//end if(bUseClientConfig && bUseDirectoryClientConfig) {
		
		}
		return bReturn;
	}
	
	
	//### noch nicht im Interface
			
	
	//##### Komfort	
	public File getDirectoryTemplate() throws ExceptionZZZ {
		File objReturn = null;
		main:{
			IMainOVPN objMain = this.getMainObject();
			if(objMain==null) {
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PROPERTY_MISSING + " unable to get MainObject '", iERROR_PROPERTY_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			ConfigChooserOVPN objConfigChooser = objMain.getConfigChooserObject();
			if(objConfigChooser==null) {
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PROPERTY_MISSING + " unable to get ConfigChooserObject '", iERROR_PROPERTY_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			objReturn = objConfigChooser.getDirectoryTemplate();
		}//end main:
		return objReturn;
	}
	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++
	public File getFileServerClientConfigReadmeTemplate() throws ExceptionZZZ {
		File objReturn = null;
		main:{
			if(this.fileServerConfigClientTemplate==null) {
				File[] fileaTemplate = this.findFileServerClientConfigReadmeTemplates();
				if(fileaTemplate!=null) {
					this.fileServerConfigClientTemplate = fileaTemplate[0];
				}
			}
			objReturn = this.fileServerConfigClientTemplate;
		}
		return objReturn;
	}
	
	public File[]findFileServerClientConfigReadmeTemplates() throws ExceptionZZZ{
		return this.findFileServerClientConfigReadmeTemplates(null); 
	}
		public File[] findFileServerClientConfigReadmeTemplates(File objDirectoryin) throws ExceptionZZZ{
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
//				Alle Dateien auflisten, dazu aber einen FileFilter verwenden
				FileFilterServerClientConfigReadmeTemplateOVPN objFilterConfig = new FileFilterServerClientConfigReadmeTemplateOVPN();			
				objaReturn = objDirectory.listFiles(objFilterConfig);
				
			}//End main
			return objaReturn;
		}
		
		public File getDirectoryServerClientConfig() throws ExceptionZZZ{
			File objReturn = null;
			main:{			
					String sDirConfig = this.readDirectoryServerClientConfigPath();
					if(sDirConfig==null){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive configuration directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}else if(sDirConfig.equals("")){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive configuration directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}

					objReturn = new File(sDirConfig);
					if(objReturn.exists()==false){
//						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + sDirConfig + "', does not exist.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
//						throw ez;
						//Keinen Fehler werfen, kann ja noch erstellt werden...
						break main;
					}else if(objReturn.isDirectory()==false){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The path '" + sDirConfig + "', was expected to be a directory, not e.g. a file.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}
			}//End main
			return objReturn;		
		}
		
		public String readDirectoryServerClientConfigPath() throws ExceptionZZZ{
			String sReturn = new String("");
			main:{					
				String sDirectoryServerClientConfig = this.getKernelObject().getParameterByProgramAlias("OVPN","ProgConfigServerClientConfig","DirectoryServerClientConfig").getValue();
				if(StringZZZ.isEmpty(sDirectoryServerClientConfig)) break main;
				
				
				
				IMainOVPN objMain = this.getMainObject();
				if(objMain==null) {
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PROPERTY_MISSING + " unable to get MainObject '", iERROR_PROPERTY_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}
				
				ConfigChooserOVPN objConfigChooser = objMain.getConfigChooserObject();
				if(objConfigChooser==null) {
					ExceptionZZZ ez = new ExceptionZZZ(sERROR_PROPERTY_MISSING + " unable to get ConfigChooserObject '", iERROR_PROPERTY_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
					throw ez;
				}												
				String sDirectoryConfig = objConfigChooser.readDirectoryConfigPath();
				sReturn = FileEasyZZZ.joinFilePathName(sDirectoryConfig, sDirectoryServerClientConfig); 						
			}//End main
			return sReturn;
		}
		
		public File findDirectoryServerClientConfig() throws ExceptionZZZ{
			File objReturn = null;
			main:{			
					String sDirServerClientconfig = this.readDirectoryServerClientConfigPath();
					if(sDirServerClientconfig==null){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Unable to receive ServerClientConfig directory.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}

					objReturn = FileEasyZZZ.searchDirectory(sDirServerClientconfig);
					if(objReturn.exists()==false){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The directory '" + objReturn.getAbsolutePath() + "', does not exist (for '" + sDirServerClientconfig + "').", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}else if(objReturn.isDirectory()==false){
						ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "The path '" + objReturn.getAbsolutePath() + "'(for '" + sDirServerClientconfig + "'),  was expected to be a directory, not e.g. a file.", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
						throw ez;
					}
			}//End main
			return objReturn;		
		}
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++
		//### noch nicht im Interface
		public String computeServerClientConfigReadmePath() throws ExceptionZZZ {
			String sReturn = null;
			main:{
				String sReadmeName = this.computeServerClientConfigReadmeName();
				if(StringZZZ.isEmpty(sReadmeName))break main;
				
				File objReadmeDirectory = this.computeServerClientConfigReadmeDirectory();				
				sReturn = FileEasyZZZ.joinFilePathName(objReadmeDirectory, sReadmeName);
			}
			return sReturn;
		}
		public String computeServerClientConfigReadmeName() throws ExceptionZZZ {
			String sReturn = null;
			main:{			
				sReturn = "readme" + this.getKernelObject().getApplicationKey() + ".txt";
			}
			return sReturn;
		}
		public File computeServerClientConfigReadmeDirectory() throws ExceptionZZZ {
			return this.getDirectoryServerClientConfig();
		}
		
		public ArrayList<String> computeReadmeLines(File fileConfigTemplateReadme) throws ExceptionZZZ {		
			ArrayList<String>listasReturn=new ArrayList<String>();
			main:{			
			IConfigMapper4ReadmeOVPN objMapperReadme = this.getConfigMapperObject(); 
				HashMapIterableKeyZZZ<String, String>hmReadmeLines = objMapperReadme.readTaskHashMap();								
								
				for(String sKey : hmReadmeLines) {
					String sLine = hmReadmeLines.getValue(sKey);
					listasReturn.add(sLine);
				}				
			}
			return listasReturn;
		}	
	
	
	
	//###### Getter / Setter
	public ServerMainZZZ getServerObject() {
		return (ServerMainZZZ) this.getMainObject();
	}
	public void setServerObject(ServerMainZZZ objServer) {
		this.setMainObject((IMainOVPN) objServer);
	}
	
	
	

	public IConfigMapper4ReadmeOVPN createConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4ReadmeOVPN objReturn = null;
		main:{
			File fileConfigTemplateReadme = this.getFileServerClientConfigReadmeTemplate();			
			objReturn = new ServerConfigMapper4ReadmeOVPN(this.getKernelObject(), this.getServerObject(), fileConfigTemplateReadme);
		}//end main:
		return objReturn;		
	}

	public IConfigMapper4ReadmeOVPN getConfigMapperObject() throws ExceptionZZZ {
		IConfigMapper4ReadmeOVPN objReturn = null;
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
	public void setConfigMapperObject(IConfigMapper4ReadmeOVPN objConfigMapper) {
		// TODO Auto-generated method stub
		
	}

	//###### GETTER  / SETTER
	public IMainOVPN getMainObject() {
		return this.objMain;
	}
	public void setMainObject(IMainOVPN objMain) {
		this.objMain = objMain;
	}

	
}//END class
