package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public abstract class AbstractOVPNFileFilterZZZ implements FilenameFilter {
	FileFilterPrefixZZZ objFilterPrefix;
	FileFilterMiddleZZZ objFilterMiddle;
	FileFilterSuffixZZZ objFilterSuffix;	
	FileFilterEndingZZZ objFilterEnding;
	
	private String sOvpnContext="";
	
	private String sPrefix="";
	private String sMiddle="";
	private String sSuffix="";
	private String sEnding="";
				
	public AbstractOVPNFileFilterZZZ(String sOvpnContextServerOrClient) {
		this.setOvpnContext(sOvpnContextServerOrClient);
		
		this.setPrefix(ConfigFileTemplateOvpnOVPN.sFILE_TEMPLATE_PREFIX);
		this.setMiddle(this.getOvpnContext());
		
		objFilterPrefix = new FileFilterPrefixZZZ(this.getPrefix());
		objFilterMiddle = new FileFilterMiddleZZZ(this.getMiddle());
		objFilterSuffix = new FileFilterSuffixZZZ(this.getSuffix());
		objFilterEnding = new FileFilterEndingZZZ(this.getEnding());
	} 
	public AbstractOVPNFileFilterZZZ() {
		this("");
	}
	public boolean accept(File objFileDir, String sName) {
		boolean bReturn=false;
		main:{
			if(sName==null) break main;				
			
			//Merke: Die Reihenfolge ist so gewählt, dass im Template Verzeichnis frühestmöglich ein "break main" erreicht wird.
			
			//Falls der OvpnContext nicht passt
			if(this.objFilterMiddle.accept(objFileDir, sName)==false) break main;
	
			//Template-Dateinamen fangen eben mit einem bestimmten String an.
			if(this.objFilterPrefix.accept(objFileDir, sName)==false) break main;
								
			//Falls die Endung nicht passt
			if(this.objFilterEnding.accept(objFileDir, sName)==false) break main;
					
			//Falls das Suffix nicht passt
			if(this.objFilterSuffix.accept(objFileDir, sName)==false) break main;
			
			bReturn = true;
		}//END main:
		return bReturn;		
	}
	
	//##### GETTER / SETTER	
		public void setOvpnContext(String sContext) {
			this.sOvpnContext=sContext;
		}
		public String getOvpnContext() {
			return this.sOvpnContext;
		}
	
		public void setPrefix(String sPrefix) {
			this.sPrefix = sPrefix;
		}
		protected String getPrefix() {
			return this.sPrefix;
		}
		
		public void setMiddle(String sMiddle) {
			this.sMiddle = sMiddle;
		}
		protected String getMiddle() {
			return this.sMiddle;
		}
		
		public void setSuffix(String sSuffix) {
			this.sSuffix = sSuffix;
		}
		protected String getSuffix() {
			return this.sSuffix;
		}
		
		public void setEnding(String sEnding) {
			this.sEnding = sEnding;
		}
		protected String getEnding() {
			return this.sEnding;
		}
}//END class