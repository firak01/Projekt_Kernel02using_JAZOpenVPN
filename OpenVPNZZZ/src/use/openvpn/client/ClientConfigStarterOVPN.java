package use.openvpn.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import basic.zKernel.KernelZZZ;
import use.openvpn.AbstractConfigStarterOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;
import use.openvpn.IConfigMapper4BatchOVPN;
import use.openvpn.IConfigStarterOVPN;
import use.openvpn.IMainOVPN;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;


public class ClientConfigStarterOVPN extends AbstractConfigStarterOVPN{
	public ClientConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objMain, File objFileConfigOvpn, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objMain, -1, objFileConfigOvpn, "0", saFlagControl);
	}
	public ClientConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objClient, int iIndex, File objFile, String sMyAlias, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objClient, iIndex, objFile, sMyAlias, saFlagControl);
	}
	
	/**Choose this constructor, if a you don�t want to use the .getNumber() - Method.
	 * 
	 * @param objKernel
	 * @param objFile
	 * @param saFlagControl
	 * @throws ExceptionZZZ
	 */
	public ClientConfigStarterOVPN(IKernelZZZ objKernel, IMainOVPN objClient, int iIndex, File objFile, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel, objClient, iIndex, objFile, "-1", saFlagControl);
	}
			
	public Process requestStart() throws ExceptionZZZ{
		Process objReturn = null;
		main:{
			String sLog = null;
			String sCommandConcrete=null;
			try {
				sLog = "Trying to find OVPNExecutable.";
				this.getLogObject().WriteLineDate(sLog);
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
				this.getLogObject().WriteLineDate(sLog);

				//Vor dem Start - egal ob by_batch oder GUI - muss sichergestellt sein, dass das Log-Verzeichnis existiert.				
				//String sDirectoryPath="c:\\fglkernel\\kernellog\\ovpnClient";
				String sDirectoryPath=this.getMainObject().getApplicationObject().getDirectoryOvpnLog();
				sLog = ReflectCodeZZZ.getPositionCurrent() + " Using as LogDirectory: '" + sDirectoryPath + "'";//bybatch als Suchtag
				System.out.println(sLog);
				this.getLogObject().WriteLineDate(sLog);
				
				boolean bCreated = FileEasyZZZ.createDirectory(sDirectoryPath);
				if(bCreated) {
					sLog = ReflectCodeZZZ.getPositionCurrent() + " Directory created: '" + sDirectoryPath + "'";//bybatch als Suchtag
					System.out.println(sLog);
					this.getLogObject().WriteLineDate(sLog);
				}

				//Anders als beim Server hier nur der "direkte" Weg OVPN zu starten.
				//Dann kommt die Ausgabe sogar in die Konsole und kann analysiert werden.
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
				
				
				/* DAS LIEFERT WENIGSTESN EIEN AUSGABE ALLER FEHLENDEN PARAMETER
				Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe" );
			    BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );
			    for ( String s; (s = in.readLine()) != null; ){
			      System.out.println( s );
			    }
				*/								
				if (this.getFlag(IConfigStarterOVPN.FLAGZ.BY_BATCH.name())){
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Excecuting by batch 'not implemented'.";//bybatch als Suchtag
					this.getLogObject().WriteLineDate(sLog);
				}else {
					// DAS FUNKTIONIERT BEIM CLIENT
					sLog = ReflectCodeZZZ.getPositionCurrent() + ": Executing direct - " + sCommandConcrete;
					this.getLogObject().WriteLineDate(sLog);
					
					Runtime load = Runtime.getRuntime();
					objReturn = load.exec("cmd /c " + sCommandConcrete);
					//Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\client_itelligence.ovpn");
					//DAS GEHT: Process p = load.exec( "cmd /c C:\\Programme\\OpenVPN\\bin\\openvpn.exe --pause-exit --config C:\\Programme\\OpenVPN\\config\\client_itelligence.ovpn");
					
					//Irgendwie funktionierte das beim 1. Mal im Debugger... Hier vielleicht auf das TAP Interface warten?
//					try {
//						Thread.sleep(10000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}					
				}//END if
				
				
			} catch (IOException e) {
				String sError = ReflectCodeZZZ.getPositionCurrent() + ": IOException ('"+e.getMessage()+"') executing the commandline: '"+ sCommandConcrete +"'";
				System.out.println(sError);
				this.getLogObject().WriteLineDate(sError);
				ExceptionZZZ ez = new ExceptionZZZ(sError, iERROR_RUNTIME, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			} 
		}
		this.setProcess(objReturn);
		return objReturn;
	}

	@Override
	public IConfigMapper4BatchOVPN createConfigMapperObject() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public HashMap<String, String> computeProcessArgumentHashMap() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return null;
	}
}//END class
