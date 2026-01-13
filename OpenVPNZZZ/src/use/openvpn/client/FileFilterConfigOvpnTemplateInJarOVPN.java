package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.AbstractFileFilterZZZ;
import basic.zBasic.util.file.FilenamePartFilterEndingZZZ;
import basic.zBasic.util.file.FilenamePartFilterMiddleZZZ;
import basic.zBasic.util.file.FilenamePartFilterPrefixZZZ;
import basic.zBasic.util.file.FilenamePartFilterSuffixZZZ;
import basic.zBasic.util.file.jar.AbstractFileFileFilterInJarZZZ;
import use.openvpn.AbstractFileFileFilterInJarOVPN;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class FileFilterConfigOvpnTemplateInJarOVPN extends AbstractFileFileFilterInJarOVPN{
	public static String sDIRECTORY_PATH="template";
	public static String sPREFIX="template_";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
	
	public FileFilterConfigOvpnTemplateInJarOVPN(String sOvpnContextServerOrClient, String[] saFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, saFlagControl);	
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateInJarOVPN(String sOvpnContextServerOrClient, String sFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, sFlagControl);
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateInJarOVPN(String sOvpnContextServerOrClient) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient);		
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateInJarOVPN() throws ExceptionZZZ {
		super();		
		OVPNFileFilterConfigOvpnTemplateNew_();
	}
	private void OVPNFileFilterConfigOvpnTemplateNew_() {
		//Merke: Das Verzeichnis wird in einer "Nicht Jar Struktur nicht gebraucht"
		this.setPrefix(ConfigFileTemplateOvpnOVPN.sFILE_TEMPLATE_PREFIX);
		this.setMiddle(this.getOvpnContext());
	}
	
	//##### GETTER / SETTER	
	public void setDirectoryPath(String sDirectoyPath) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sDirectoyPath)) {
			super.setDirectoryPath(FileFilterConfigOvpnTemplateInJarOVPN.sDIRECTORY_PATH);
		}else {
			super.setDirectoryPath(sDirectoyPath);
		}
	}
	
	public void setPrefix(String sPrefix) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sPrefix)) {
			super.setPrefix(FileFilterConfigOvpnTemplateInJarOVPN.sPREFIX);
		}else {
			super.setPrefix(sPrefix);
		}
	}
	
	public void setMiddle(String sMiddle) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sMiddle)) {
			super.setMiddle(FileFilterConfigOvpnTemplateInJarOVPN.sMIDDLE);
		}else {
			super.setMiddle(sMiddle);
		}
	}
	
	public void setSuffix(String sSuffix) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sSuffix)) {
			super.setSuffix(FileFilterConfigOvpnTemplateInJarOVPN.sSUFFIX);
		}else {
			super.setSuffix(sSuffix);
		}
	}

				
	public void setEnding(String sEnding) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sEnding)) {
			super.setEnding(FileFilterConfigOvpnTemplateInJarOVPN.sENDING);
		}else {
			super.setEnding(sEnding);
		}
	}
}//END class