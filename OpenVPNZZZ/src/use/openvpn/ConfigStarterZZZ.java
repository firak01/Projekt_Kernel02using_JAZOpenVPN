package use.openvpn;

import java.io.File;
import java.io.IOException;



import basic.zKernel.KernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;


public class ConfigStarterZZZ extends KernelUseObjectZZZ{
	private File objFileConfig=null;
	private Process objProcess=null;
	private String sMyAlias = "-1";
	private boolean bFlagByBatch = false;
	String sOvpnContextClientOrServer=null;
	
	public ConfigStarterZZZ(IKernelZZZ objKernel, File objFile, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_(sMyAlias, objFile, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public ConfigStarterZZZ(IKernelZZZ objKernel, File objFile, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigStarterNew_("-1", objFile, saFlagControl);
	}
	
	private void ConfigStarterNew_(String sMyAlias, File objFile, String[] saFlagControl) throws ExceptionZZZ{
		main:{
				 
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ( stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							 
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}
				
				
				if(objFile==null){
					ExceptionZZZ ez = new ExceptionZZZ("File", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setFileConfig(objFile);
				this.sMyAlias = sMyAlias;
				
				String sOvpnContextClientOrServer = StringZZZ.left(objFile.getName(), "_");
				this.setOvpnContextUsed(sOvpnContextClientOrServer);
			}//End check
		}//END main
	}
	
	public Process requestStart() throws ExceptionZZZ{
		Process objReturn = null;
		main:{
			String sCommandConcrete=null;
			try {
				this.getLogObject().WriteLineDate("Trying to find OVPNExecutable.");
				File objFileExe = ConfigFileZZZ.findFileExe();
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
				
				String sCommandParameter = ConfigFileZZZ.readCommandParameter();
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
				sCommandConcrete = StringZZZ.replace(sCommand, "\"%1\"", this.getFileConfig().getPath());
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
					// DAS FUNKTIONIERT BEIM CLIENT
					this.getLogObject().WriteLineDate("Excecuting direkt '"+sCommandConcrete+"'");				
					objReturn = load.exec("cmd /c " + sCommandConcrete);
					//Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\client_itelligence.ovpn");
					//DAS GEHT: Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\client_itelligence.ovpn");
					
					//Irgendwie funktionierte das beim 1. Mal im Debugger... Hier vielleicht auf das TAP Interface warten?
//					try {
//						Thread.sleep(10000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					
					
				}else{
					//Das funktioniert das beim Server, indirekt über eine Batch starten
					//TODO GOON 20200205
					//0. Bestehende Batch Datei suchen und löschen
					000000000000
					
					//1. Batch File neu erstellen (wg. ggfs. anderen/neuen Code)					
					111111111111
					
					//2. Batch File starten
					ConfigChooserZZZ objPathConfig = new ConfigChooserZZZ(this.getKernelObject(), this.getOvpnContextUsed());				
					String sCommandBatch = objPathConfig.getDirectoryConfig()+ File.separator+"starter_"+ this.getFileConfig().getName() + ".bat";
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
		this.objProcess = objReturn;
		return objReturn;
	}
	
	/**Should destroy a process. But this does not work.
	 * A workaround used, is to kill all openvpn.exe - processes when the cientUI is closed/stopped. 
	 * @return void
	 *
	 * javadoc created by: 0823, 11.07.2006 - 12:55:49
	 */
	public void requestStop(){
		main:{
			Process objProcess = null;
			check:{
				objProcess = this.objProcess;
				if(objProcess==null) break main;
			}
		
			objProcess.destroy();
		}//END main
	}
	
	public boolean isProcessAlive(){
		boolean bReturn = false;
		main:{
			check:{
				if(this.objProcess==null) break main;				
			}//END check:
		
			try{
				//TODO GOON den exit status des Processes auch sicher abpr�fbar machen !!!
				//Merke: Einen Exit-Status abzurufen, wenn der Process noch l�uft, wirft eine IllegalThreadStateException
				this.objProcess.exitValue();
				return false;
			}catch(IllegalThreadStateException e){
				return true;
			}	
		
		}//END Main:
		return bReturn;
	}

	
	//###### GETTER  / SETTER
	public void setFileConfig(File objFile){
		this.objFileConfig = objFile;
	}
	public File getFileConfig(){
		return this.objFileConfig;
	}
	
	public Process getProcess(){
		return this.objProcess;
	}
	
	/**This is not the process id.
	 * It is just an alias, which was provided at the constructor.
	 * @return int
	 *
	 * javadoc created by: 0823, 28.07.2006 - 15:04:25
	 */
	public String getAlias(){
		return this.sMyAlias;
	}
	
	public String getOvpnContextUsed() {
		return this.sOvpnContextClientOrServer;
	}
	public void setOvpnContextUsed(String sOvpnContextClientOrServer) {
		this.sOvpnContextClientOrServer = sOvpnContextClientOrServer;
	}
	
	
//	###### FLAGS
	/* (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used: 
	- hasError
	- hasOutput
	- hasInput
	- stoprequested
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
							
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("bybatch")){
				bFunction = bFlagByBatch;
				break main;
			}
			/*
			else if(stemp.equals("hasoutput")){
				bFunction = bFlagHasOutput;
				break main;
			}else if(stemp.equals("hasinput")){
				bFunction = bFlagHasInput;
				break main;
			}else if(stemp.equals("stoprequested")){
				bFunction = bFlagStopRequested;
				break main;
			}
			*/
	
		}//end main:
		return bFunction;
	}

	/**
	 * @see zzzKernel.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 	- hasError
	- hasOutput
	- hasInput
	- stoprequested
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue){
		boolean bFunction = false;
		main:{			
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
		
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("bybatch")){
			bFlagByBatch = bFlagValue;
			bFunction = true;
			break main;
		}
		/*
		else if(stemp.equals("hasoutput")){
			bFlagHasOutput = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("hasinput")){
			bFlagHasInput = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("stoprequested")){
			bFlagStopRequested = bFlagValue;
			bFunction = true;
			break main;
		}
		*/
		}//END main:
		return bFunction;
	}	
}//END class
