package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;

public class OVPNFileFilterConfigOvpnUsedZZZ implements FilenameFilter {
	private FileFilterEndingZZZ objFilterEnding;
	private FileFilterPrefixZZZ objFilterPrefix;
	private String sEnding="ovpn";
	private String sPrefix="";
	
	public OVPNFileFilterConfigOvpnUsedZZZ(String sContextServerOrClient) {
		this.setPrefix(sContextServerOrClient);
		objFilterEnding = new FileFilterEndingZZZ(this.getEnding());
		objFilterPrefix = new FileFilterPrefixZZZ(this.getPrefix());
	}
	public OVPNFileFilterConfigOvpnUsedZZZ(){
		this("");
	} 
	public boolean accept(File objFileDir, String sName) {
		boolean bReturn=false;
		main:{
			check:{
				if(sName==null) break main;				
			}
			
		//Falls die Endung nicht passt
		if(this.objFilterEnding.accept(objFileDir, sName)==false) break main;
		
		//Falls der Anfang nicht passt
		if(this.objFilterPrefix.accept(objFileDir, sName)==false) break main;
		
		//Template-Dateinamen fangen eben damit an.
		//Grund der ganzen Aktion: Das Abspeichern mit Properties entfernt die kommentare
		if(sName.toLowerCase().startsWith("template")) break main;
		bReturn = true;
		}//END main:
		return bReturn;		
	}
	
	//##### GETTER / SETTER
	public void setEnding(String sEnding) {
		this.sEnding = sEnding;
	}
	private String getEnding() {
		return this.sEnding;
	}
	
	public void setPrefix(String sPrefix) {
		this.sPrefix = sPrefix;
	}
	private String getPrefix() {
		return this.sPrefix;
	}
	
}//END class