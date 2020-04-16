package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.AbstractOVPNFileFilterZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class FileFilterConfigOvpnUsedOVPN extends AbstractOVPNFileFilterZZZ {
	public static String sPREFIX="";
	public static String sMIDDLE="";
	public static String sSUFFIX="";
	public static String sENDING="ovpn";
			
	public FileFilterConfigOvpnUsedOVPN(String sOvpnContextServerOrClient, String[] saFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, saFlagControl);
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	public FileFilterConfigOvpnUsedOVPN(String sOvpnContextServerOrClient, String sFlagControl) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient, sFlagControl);
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	public FileFilterConfigOvpnUsedOVPN(String sContextServerOrClient) throws ExceptionZZZ {
		super(sContextServerOrClient);
		OVPNFileFilterConfigOvpnUsedNew_();
	}
	public FileFilterConfigOvpnUsedOVPN() throws ExceptionZZZ{
		super();				
		OVPNFileFilterConfigOvpnUsedNew_();
	} 
	private void OVPNFileFilterConfigOvpnUsedNew_() {
		this.setPrefix(this.getOvpnContext());
	}
	
	//##### GETTER / SETTER	
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(FileFilterConfigOvpnUsedOVPN.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(FileFilterConfigOvpnUsedOVPN.sMIDDLE);
			}else {
				super.setMiddle(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(FileFilterConfigOvpnUsedOVPN.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(FileFilterConfigOvpnUsedOVPN.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}		
}//END class