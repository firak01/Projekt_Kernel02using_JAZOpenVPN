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
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class FileFilterConfigOvpnTemplateOVPN extends AbstractFileFilterZZZ{
	public static String sPREFIX="template_";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
	
	public FileFilterConfigOvpnTemplateOVPN(String sOvpnContextServerOrClient, String[] saFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, saFlagControl);	
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateOVPN(String sOvpnContextServerOrClient, String sFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, sFlagControl);
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateOVPN(String sOvpnContextServerOrClient) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient);		
		OVPNFileFilterConfigOvpnTemplateNew_();
	} 
	public FileFilterConfigOvpnTemplateOVPN() throws ExceptionZZZ {
		super();		
		OVPNFileFilterConfigOvpnTemplateNew_();
	}
	private void OVPNFileFilterConfigOvpnTemplateNew_() throws ExceptionZZZ {
		this.setPrefix(ConfigFileTemplateOvpnOVPN.sFILE_TEMPLATE_PREFIX);
		this.setMiddle(this.getOvpnContext());
	}
	
	//##### GETTER / SETTER	
	public void setPrefix(String sPrefix) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sPrefix)) {
			super.setPrefix(FileFilterConfigOvpnTemplateOVPN.sPREFIX);
		}else {
			super.setPrefix(sPrefix);
		}
	}
	
	public void setMiddle(String sMiddle) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sMiddle)) {
			super.setMiddle(FileFilterConfigOvpnTemplateOVPN.sMIDDLE);
		}else {
			super.setMiddle(sMiddle);
		}
	}
	
	public void setSuffix(String sSuffix) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sSuffix)) {
			super.setSuffix(FileFilterConfigOvpnTemplateOVPN.sSUFFIX);
		}else {
			super.setSuffix(sSuffix);
		}
	}

				
	public void setEnding(String sEnding) throws ExceptionZZZ {
		if(StringZZZ.isEmpty(sEnding)) {
			super.setEnding(FileFilterConfigOvpnTemplateOVPN.sENDING);
		}else {
			super.setEnding(sEnding);
		}
	}
}//END class