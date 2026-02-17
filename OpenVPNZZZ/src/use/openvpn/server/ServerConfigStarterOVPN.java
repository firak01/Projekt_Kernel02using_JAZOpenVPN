package use.openvpn.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import use.openvpn.AbstractConfigStarterOVPN;
import use.openvpn.ConfigChooserOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.IConfigMapper4BatchOVPN;
import use.openvpn.IConfigStarterOVPN;
import use.openvpn.IMainOVPN;
import use.openvpn.client.ClientMainOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.ArrayListUtilZZZ;
import basic.zBasic.util.abstractList.SetUtilZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zBasic.util.file.FileTextWriterZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;


public class ServerConfigStarterOVPN extends AbstractConfigStarterOVPN{	
	public static final String sBATCH_STARTER_PREFIX="starter_";
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, -1, objFileConfigOvpn, "0", saFlagControl);
	}
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, int iIndex, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, iIndex, objFileConfigOvpn, "0", saFlagControl);
	}
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, int iIndex, File objFileConfigOvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, iIndex, objFileConfigOvpn, sMyAlias, saFlagControl);
	}
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain,  int iIndex, File objFileTemplateBatch, File objFileConfigOvpn, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, iIndex, objFileTemplateBatch, objFileConfigOvpn, sMyAlias, saFlagControl);
	}
	
	/**Choose this constructor, if a you don't want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public ServerConfigStarterOVPN(IKernelZZZ objKernel, ServerMainOVPN objServer, int iIndex, File objFileTemplateBatch, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,(IMainOVPN) objServer, iIndex, objFileTemplateBatch, objFileConfigOvpn, "-1", saFlagControl);
	}
			
	public Process requestStart() throws ExceptionZZZ{
		Process objReturn = null;
		main:{
			String sLog = null;
			String sCommandConcrete=null;
			try {
				sLog = "Trying to find OVPNExecutable.";
				this.getLogObject().writeLineDate(sLog);
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
				sLog = "OVPNExecutable found";
				this.getLogObject().writeLineDate(sLog);
				
				//Vor dem Start - egal ob by_batch oder GUI - muss sichergestellt sein, dass das Log-Verzeichnis existiert.				
				//String sDirectoryPath="c:\\fglkernel\\kernellog\\ovpnServer";
				String sDirectoryPath=this.getServerObject().getApplicationObject().getDirectoryOvpnLog();
				sLog = ReflectCodeZZZ.getPositionCurrent() + " Using as LogDirectory: '" + sDirectoryPath + "'";//bybatch als Suchtag
				System.out.println(sLog);
				this.getLogObject().writeLineDate(sLog);
				
				boolean bCreated = FileEasyZZZ.createDirectory(sDirectoryPath);
				if(bCreated) {
					sLog = ReflectCodeZZZ.getPositionCurrent() + " Directory created: '" + sDirectoryPath + "'";//bybatch als Suchtag
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
				}
				
				
				boolean bByBatch = this.getFlag(IConfigStarterOVPN.FLAGZ.BY_BATCH.name());
				boolean bByOvpnGui = this.getFlag(IConfigStarterOVPN.FLAGZ.BY_OVPNGUI.name());
				//Falls nicht bei der GUI gestartet wird, die Kommandozeile errechnen
				
				if(!bByOvpnGui) {
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
					
					
					/* DAS LIEFERT WENIGSTESN EINE AUSGABE ALLER FEHLENDEN PARAMETER
					Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe" );
				    BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );
				    for ( String s; (s = in.readLine()) != null; ){
				      System.out.println( s );
				    }
					*/
				}

				
				
				if (bByBatch){															
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Executing by batch.";//bybatch als Suchtag
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
															
					
										
					//Das funktioniert das beim Server, indirekt über eine Batch starten					
					//Name der zu verwendenden Batch Datei ausrechnen.
					String sBatch = this.computeBatchPath();
					
					//0. Bestehende Batch Datei suchen und löschen
					boolean  bBatchExists = FileEasyZZZ.exists(sBatch);
					if(bBatchExists) {
						this.getLogObject().writeLineDate("Deleting existing batch file: '"+sBatch +"'.");
						boolean bSuccess = FileEasyZZZ.removeFile(sBatch);
						if(bSuccess) {
							this.getLogObject().writeLineDate("Existing batch file successful deleted.");
						}else {
							this.getLogObject().writeLineDate("Unable to delete existing batch.");
						}
					}
					
					File fileConfigOvpn = this.getFileConfigOvpn();

					//1. Batch File neu erstellen (wg. ggfs. anderen/neuen Code)
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Creating new batch file.";
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
					
					FileTextWriterZZZ objBatch = new FileTextWriterZZZ(sBatch);	 	//2020020208: es gibt jetzt den FileTextWriterZZZ im Kernel Projekt.				
					ArrayList<String> listaLine = this.computeBatchLines(this.getFileTemplateBatch(), fileConfigOvpn);
					for(String sLine : listaLine){
						objBatch.writeLine(sLine);
					}
					
					
					
					
					//2. Batch File starten					
					ConfigChooserOVPN objPathConfig = new ConfigChooserOVPN(this.getKernelObject(), this.getOvpnContextUsed(), this.getServerObject().getApplicationObject());				
					String sCommandBatch = sBatch; //objPathConfig.getDirectoryConfig()+ File.separator+"starter_"+ this.getFileConfig().getName() + ".bat";
					this.getLogObject().writeLineDate("Executing by Batch '"+ sCommandBatch +"'");				
					
					
					/*Verwendeter Code VOR Java 7 */
					///*
					Runtime load = Runtime.getRuntime();					
					objReturn = load.exec("cmd /c " + sCommandBatch);
					//*/
																												
				}else if(bByOvpnGui){
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Excecuting by ovpngui.";
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
					
					 /*Verwendeter Code ab Java 7*/
					
					 /*Loesungsanstz: Verwende den ProcessBuilder mit Argument-Strings...
					  * Die Parametername unterscheiden sich von den Parameternamen beim Batch/Kommandozeilenstart von openvpn.exe 
					  ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\OpenVPN\\bin\\openvpn.exe", "--config", "C:\\Users\\DATABASE\\OpenVPN\\config\\italy\\italy.ovpn", "--auth-user-pass", "C:\\Users\\DATABASE\\OpenVPN\\config\\italy\\italy.txt").redirectErrorStream(true);
            		  Process process = pb.start();
					  */	
					
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": creating procesbuilder arguments.";
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
					
					HashMap<String,String> hmArgument = this.computeProcessArgumentHashMap();
					if(hmArgument.isEmpty()) {
						sLog = ReflectCodeZZZ.getPositionCurrent() + ": Keine notwendigen HashMap Eintraege vorhanden. Breche ab.";
						System.out.println(sLog);
						this.getLogObject().writeLineDate(sLog);						
					}
					
					ArrayList<String>listaArgument = new ArrayList<String>();
					
					File objDirectoryExeOvpn = this.getMainObject().getConfigChooserObject().getDirectoryExe();
					String sPathTotal = FileEasyZZZ.joinFilePathName(objDirectoryExeOvpn, "openvpn-gui.exe");
					listaArgument.add(sPathTotal);
					
					Set<String> setKey = hmArgument.keySet();
					Iterator<String> itKey = setKey.iterator();
					while(itKey.hasNext()) {	
						String sKey = itKey.next();
						String sValue = hmArgument.get(sKey);
						listaArgument.add(sKey);
						listaArgument.add(sValue);
					}
					
					String[] saArgument = ArrayListUtilZZZ.toStringArray(listaArgument);
					
						
					//Merke: Das Konfigurationsverzeichnis muss hier auch wieder anders angegeben werden, ohne die angabe sucht er in einen auto-config Verzeichnis, oder so...					
					//ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--config_dir", "C:\\Programme\\OpenVPN\\config", "--connect", "server_TCP_4999.ovpn", "--log_dir", "c:\\fglkernel\\kernellog\\ovpn.log"); 
					ProcessBuilder pb = new ProcessBuilder(saArgument);
						 
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": starting process.";
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
					//Das gibt ueberhaupt keine Zeile mehr aus... 
					//objReturn = pb.inheritIO().start();
					objReturn = pb.start();
					
					 //### Weitere Entwicklung
					 //Mache einen Thread, der das Log beobachtet
					 //LogFileWatchRunnerZZZ
					
					//Merke: Bei OVPNGUI wird unterhalb dieses Verzeichnisses eine Log Datei mit dem Namen der in --connect angegebenen Datei angelegt.					
					//hmReturn.put("--log_dir", "c:\\fglkernel\\kernellog\\ovpnstarter");

					
					
					//Merke: Direkt kann hier nix abgefragt werden
//				     BufferedReader error = new BufferedReader(new InputStreamReader(objReturn.getErrorStream()));
//					 String errorString = error.readLine();
//					 while(error.readLine()!=null) {
//						 System.out.println("Error: " + errorString);
//						 errorString = error.readLine();
//					 }
//					 
//					 
//					 BufferedReader standard = new BufferedReader(new InputStreamReader(objReturn.getInputStream()));
//					 String outputString = standard.readLine();
//					 
//					 while(standard.readLine()!=null) {
//						 System.out.println("Standard: " + outputString);
//						 outputString = standard.readLine();
//					 }
					
				}else {
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Executing direct - " + sCommandConcrete;
					System.out.println(sLog);
					this.getLogObject().writeLineDate(sLog);
					
					//direkter Start der Befehlszeile
					/*Verwendeter Code VOR Java 7 */
					///*
					Runtime load = Runtime.getRuntime();					
					objReturn = load.exec("cmd /c " + sCommandConcrete);
					//*/										
				}//END if
			} catch (IOException e) {
				String sError = ReflectCodeZZZ.getPositionCurrent() + ": IOException ('"+e.getMessage()+"') executing the commandline: '"+ sCommandConcrete +"'";
				System.out.println(sError);
				this.getLogObject().writeLineDate(sError);
				ExceptionZZZ ez = new ExceptionZZZ(sError, iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			} 
		}//end main
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
	
	
	
	public ServerMainOVPN getServerObject() {
		return (ServerMainOVPN) this.getMainObject();
	}
	public void setServerObject(ServerMainOVPN objServer) {
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

	@Override
	public HashMap<String, String> computeProcessArgumentHashMap() throws ExceptionZZZ {
		//Argumentliste in Form von Argument = Wert
        //fuer z.B. ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--config_dir", "C:\\Programme\\OpenVPN\\config", "--connect", "server_TCP_4999.ovpn", "--log_dir", "c:\\fglkernel\\kernellog\\ovpn.log");
		
		HashMap<String,String> hmReturn = new HashMap<String, String>();
		main:{			
			ServerConfigMapper4BatchOVPN objMapper = this.getServerConfigMapper4BatchObject();
			
			
			//+++++++++++++++++++++++++
			//IConfigMapper4BatchOVPN objConfig = this.getConfigMapperObject();
			IMainOVPN objMain = this.getMainObject();
			ConfigChooserOVPN objConfig = objMain.getConfigChooserObject();
			File fileConfig = objConfig.getDirectoryConfig();
			
			String sDirectoryConfigOvpn = fileConfig.getAbsolutePath();
			hmReturn.put("--config_dir", sDirectoryConfigOvpn);
			
			//+++++++++++++++++++++++
			File fileConfigOvpn = objMapper.getFileConfigOvpnUsed();
			if(fileConfigOvpn==null) {
				ExceptionZZZ ez = new ExceptionZZZ("OVPN Config file", iERROR_PARAMETER_MISSING, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			String sFileConfigOvpn = fileConfigOvpn.getName();
			
			//String sPathConfigOvpnTotal = sDirectoryTemplateOvpn + File.separator + sFileConfigOvpn);			
			hmReturn.put("--connect", sFileConfigOvpn);
			
			
			//++++++++++++++++++++++++
			//TODOGOON20231206: Das Log-Verzeichnis muss doch irgendwo definiert werden....
			//Merke: Bei OVPNGUI wird unterhalb dieses Verzeichnisses eine Log Datei mit dem Namen der in --connect angegebenen Datei angelegt.
			//       Das Anlegen des Verzeichnisses passiert dann beim Server Start
			String sDirectoryLogPath=objMain.getApplicationObject().getDirectoryOvpnLog();								
			hmReturn.put("--log_dir", sDirectoryLogPath);
		}//end main:
		return hmReturn;		
	}
}//END class
