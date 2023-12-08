package use.openvpn;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import basic.zBasic.ExceptionZZZ;
import use.openvpn.client.process.IClientThreadProcessWatchMonitorOVPN.FLAGZ;

public interface IConfigStarterOVPN {
	public enum FLAGZ{
		DUMMY,BY_BATCH, BY_OVPNGUI
	}
	
	boolean getFlag(FLAGZ objEnumFlag);
	boolean setFlag(FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ;
	boolean[] setFlag(FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ;
	boolean proofFlagExists(FLAGZ objEnumFlag) throws ExceptionZZZ;
	boolean proofFlagSetBefore(FLAGZ objEnumFlag) throws ExceptionZZZ;
	
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void requestStop();	
	public Process requestStart() throws ExceptionZZZ;
	
	public boolean isProcessAlive();
	public Process getProcess();		
	public void setProcess(Process objProcess);
	public String getAlias();//Ein Alias für die Proces id
	public void setAlias(String sAlias);
	public int getIndex();//Ein Index für die Konfiguration
	public void setIndex(int iIndex);
	
	public void setFileTemplateBatch(File objFileTemplateBatch);
	public File getFileTemplateBatch();
	
	public void setFileConfigOvpn(File objFileConfigOvpn);
	public File getFileConfigOvpn();
	public String getOvpnContextUsed();
	public void setOvpnContextUsed(String sOvpnContextClientOrServer);
	
	public ArrayList<String>computeBatchLines(File fileBatch, File fileConfigTemplateOvpn) throws ExceptionZZZ;
	
	public HashMap<String,String>computeProcessArgumentHashMap() throws ExceptionZZZ;
}
