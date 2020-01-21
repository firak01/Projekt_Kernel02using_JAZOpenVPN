package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileZZZ;

public class OVPNFileFilterConfigTemplateZZZ implements FilenameFilter {
	FileFilterEndingZZZ objFilterEnding;
	
	public OVPNFileFilterConfigTemplateZZZ(){
		objFilterEnding = new FileFilterEndingZZZ("ovpn");
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
		if(!ConfigFileZZZ.isTemplate(sName)) break main;
		bReturn = true;
		}//END main:
		return bReturn;		
	}
}//END class