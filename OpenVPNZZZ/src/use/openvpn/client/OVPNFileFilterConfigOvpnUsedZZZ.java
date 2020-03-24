package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class OVPNFileFilterConfigOvpnUsedZZZ extends AbstractOVPNFileFilterZZZ {
	public static String sPREFIX="";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
			
	public OVPNFileFilterConfigOvpnUsedZZZ(String sOvpnContextServerOrClient, String[] saFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, saFlagControl);
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	public OVPNFileFilterConfigOvpnUsedZZZ(String sOvpnContextServerOrClient, String sFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, sFlagControl);
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	public OVPNFileFilterConfigOvpnUsedZZZ(String sContextServerOrClient) throws ExceptionZZZ {
		super(sContextServerOrClient);
		OVPNFileFilterConfigOvpnUsedNew_();
	}
	public OVPNFileFilterConfigOvpnUsedZZZ() throws ExceptionZZZ{
		super();				
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	private void OVPNFileFilterConfigOvpnUsedNew_() {
		this.setPrefix(this.getOvpnContext());
	}
	
	//##### GETTER / SETTER	
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(OVPNFileFilterConfigOvpnUsedZZZ.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(OVPNFileFilterConfigOvpnUsedZZZ.sMIDDLE);
			}else {
				super.setMiddle(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(OVPNFileFilterConfigOvpnUsedZZZ.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(OVPNFileFilterConfigOvpnUsedZZZ.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}		
}//END class