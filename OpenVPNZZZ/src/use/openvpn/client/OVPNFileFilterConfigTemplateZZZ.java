package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.file.FileFilterSuffixZZZ;

public class OVPNFileFilterConfigTemplateZZZ implements FilenameFilter {
	FileFilterSuffixZZZ objFilterSuffix;
	
	public OVPNFileFilterConfigTemplateZZZ(){
		objFilterSuffix = new FileFilterSuffixZZZ("ovpn");
	} 
	public boolean accept(File objFileDir, String sName) {
		boolean bReturn=false;
		main:{
			check:{
				if(sName==null) break main;				
			}
			
		//Falls die Endung nicht passt
		if(this.objFilterSuffix.accept(objFileDir, sName)==false) break main;
		
		//Template-Dteinamen fangen eben damit an.
		//Grund der ganzen Aktion: Das Abspeichern mit Properties entfernt die kommentare
		if(!sName.toLowerCase().startsWith("template")) break main;
		bReturn = true;
		}//END main:
		return bReturn;		
	}
}//END class