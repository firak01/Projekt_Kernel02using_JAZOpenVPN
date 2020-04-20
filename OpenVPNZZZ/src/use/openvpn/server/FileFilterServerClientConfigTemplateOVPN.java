package use.openvpn.server;

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

public class FileFilterServerClientConfigTemplateOVPN extends AbstractOVPNFileFilterZZZ{
	public static String sPREFIX="readme_";
	public static String sMIDDLE="_directory_";
	public static String sSUFFIX="_clientconfig";
	public static String sENDING="txt";		
		
	public FileFilterServerClientConfigTemplateOVPN() throws ExceptionZZZ {
		super("server"); //der Context ist halt server. Das ist Bestandteil des Templatenamens
	} 
	

	//##### GETTER / SETTER		
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(FileFilterServerClientConfigTemplateOVPN.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(FileFilterServerClientConfigTemplateOVPN.sMIDDLE);
			}else {
				super.setPrefix(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(FileFilterServerClientConfigTemplateOVPN.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(FileFilterServerClientConfigTemplateOVPN.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}
}//END class