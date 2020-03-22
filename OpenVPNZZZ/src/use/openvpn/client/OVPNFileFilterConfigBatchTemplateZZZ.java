package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class OVPNFileFilterConfigBatchTemplateZZZ extends AbstractOVPNFileFilterZZZ{
	public static String sPREFIX="template_";
	public static String sMIDDLE="";
	public static String sSUFFIX="_starter";
	public static String sENDING="txt";		
		
	public OVPNFileFilterConfigBatchTemplateZZZ(String sOvpnContextServerOrClient) {
		super(sOvpnContextServerOrClient);
	} 
	public OVPNFileFilterConfigBatchTemplateZZZ() {
		super();
	}

	//##### GETTER / SETTER		
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(OVPNFileFilterConfigBatchTemplateZZZ.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(OVPNFileFilterConfigBatchTemplateZZZ.sMIDDLE);
			}else {
				super.setPrefix(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(OVPNFileFilterConfigBatchTemplateZZZ.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(OVPNFileFilterConfigBatchTemplateZZZ.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}
}//END class