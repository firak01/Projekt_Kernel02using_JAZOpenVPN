package use.openvpn;

import java.io.File;

import basic.zBasic.ExceptionZZZ;

public interface IConfigStarterOVPN {
	public void requestStop();	
	public Process requestStart() throws ExceptionZZZ;
	
	public boolean isProcessAlive();
	public Process getProcess();		
	public void setProcess(Process objProcess);
	public String getAlias();//Ein Alias für die Proces id
	
	public void setFileConfig(File objFile);
	public File getFileConfig();
	public String getOvpnContextUsed();
	public void setOvpnContextUsed(String sOvpnContextClientOrServer);
		

}
