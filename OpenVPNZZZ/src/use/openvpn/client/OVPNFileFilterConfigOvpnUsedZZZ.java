package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;

public class OVPNFileFilterConfigOvpnUsedZZZ extends AbstractOVPNFileFilterZZZ {
	public static String sPREFIX="";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
			
	public OVPNFileFilterConfigOvpnUsedZZZ(String sContextServerOrClient) {
		super(sContextServerOrClient);
	}
	public OVPNFileFilterConfigOvpnUsedZZZ(){
		super();
	} 
	
	//##### GETTER / SETTER	
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(super.getPrefix())) {
				super.setPrefix(OVPNFileFilterConfigOvpnUsedZZZ.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(super.getMiddle())) {
				super.setMiddle(OVPNFileFilterConfigOvpnUsedZZZ.sMIDDLE);
			}else {
				super.setPrefix(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(super.getSuffix())) {
				super.setSuffix(OVPNFileFilterConfigOvpnUsedZZZ.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(super.getEnding())) {
				super.setEnding(OVPNFileFilterConfigOvpnUsedZZZ.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}			
}//END class