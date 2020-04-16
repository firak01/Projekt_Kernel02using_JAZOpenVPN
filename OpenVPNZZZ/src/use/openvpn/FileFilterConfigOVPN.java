package use.openvpn;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;

public class FileFilterConfigOVPN implements FilenameFilter {
	FileFilterEndingZZZ objFilterEnding;
	
	public FileFilterConfigOVPN(){
		objFilterEnding = new FileFilterEndingZZZ("ovpn");
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
		if(this.objFilterEnding.accept(objFileDir, sName)==false) break main;
		
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
			bReturn = this.objFilterEnding.accept(objFile);
		}//END main
		return bReturn;
	}
}//END class