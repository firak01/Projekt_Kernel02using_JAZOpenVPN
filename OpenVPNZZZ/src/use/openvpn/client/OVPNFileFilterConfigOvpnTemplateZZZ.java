package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class OVPNFileFilterConfigOvpnTemplateZZZ extends AbstractOVPNFileFilterZZZ{
	public static String sPREFIX="template_";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
				
	public OVPNFileFilterConfigOvpnTemplateZZZ(String sOvpnContextServerOrClient) {
		super(sOvpnContextServerOrClient);
	} 
	public OVPNFileFilterConfigOvpnTemplateZZZ() {
		super();
	}
	//##### GETTER / SETTER	
	public void setPrefix(String sPrefix) {
		if(StringZZZ.isEmpty(super.getPrefix())) {
			super.setPrefix(OVPNFileFilterConfigOvpnTemplateZZZ.sPREFIX);
		}else {
			super.setPrefix(sPrefix);
		}
	}
	
	public void setMiddle(String sMiddle) {
		if(StringZZZ.isEmpty(super.getMiddle())) {
			super.setMiddle(OVPNFileFilterConfigOvpnTemplateZZZ.sMIDDLE);
		}else {
			super.setPrefix(sMiddle);
		}
	}
	
	public void setSuffix(String sSuffix) {
		if(StringZZZ.isEmpty(super.getSuffix())) {
			super.setSuffix(OVPNFileFilterConfigOvpnTemplateZZZ.sSUFFIX);
		}else {
			super.setSuffix(sSuffix);
		}
	}
				
	public void setEnding(String sEnding) {
		if(StringZZZ.isEmpty(super.getEnding())) {
			super.setEnding(OVPNFileFilterConfigOvpnTemplateZZZ.sENDING);
		}else {
			super.setEnding(sEnding);
		}
	}		
}//END class