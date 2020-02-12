package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileOVPN;

public class OVPNFileFilterConfigTemplateZZZ implements FilenameFilter {
	FileFilterEndingZZZ objFilterEnding;
	FileFilterPrefixZZZ objFilterPrefix;
	private String sEnding="ovpn";
	private String sPrefix="";
	
	public OVPNFileFilterConfigTemplateZZZ(String sOvpnContextServerOrClient) {
		this.setOvpnContextPrefix(sOvpnContextServerOrClient);
		objFilterEnding = new FileFilterEndingZZZ(this.getEnding());
		objFilterPrefix = new FileFilterPrefixZZZ(this.getOvpnContextPrefix());
	} 
	public OVPNFileFilterConfigTemplateZZZ() {
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
				
		//Template-Dateinamen fangen eben mit einem bestimmten String an.
		if(!ConfigFileOVPN.isTemplate(sName)) break main;
		
		//Falls der Anfang nicht passt
		String sNameWithoutTemplate = StringZZZ.rightback(sName,ConfigFileOVPN.sFILE_TEMPLATE_PREFIX);
		if(this.objFilterPrefix.accept(objFileDir, sNameWithoutTemplate)==false) break main;
						
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
		
		public void setOvpnContextPrefix(String sPrefix) {
			this.sPrefix = sPrefix;
		}
		private String getOvpnContextPrefix() {
			return this.sPrefix;
		}
}//END class