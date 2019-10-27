package use.openvpn;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.file.FileFilterSuffixZZZ;

public class OVPNFileFilterConfigZZZ implements FilenameFilter {
	FileFilterSuffixZZZ objFilterSuffix;
	
	public OVPNFileFilterConfigZZZ(){
		objFilterSuffix = new FileFilterSuffixZZZ("ovpn");
	} 
	/* (non-Javadoc)
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File objFileDir, String sName) {
		boolean bReturn=false;
		main:{
			check:{
				if(sName==null) break main;				
			}
			 
		//Falls die Endung nicht passt
		if(this.objFilterSuffix.accept(objFileDir, sName)==false) break main;
		
		//Weitere Einschränkungen sind erste einmal nicht bekannt.
		//if(! sName.toLowerCase().startsWith("zkernelconfig")) break main;
		bReturn = true;
		}//END main:
		return bReturn;		
	}
	public boolean accept(File objFile){
		boolean bReturn = false;
		main:{
			check:{
				if(objFile.isDirectory()) break main;
			}
			bReturn = this.objFilterSuffix.accept(objFile);
		}//END main
		return bReturn;
	}
}//END class