package use.openvpn.server;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.AbstractFileFilterZZZ;
import basic.zBasic.util.file.FilenamePartFilterEndingZZZ;
import basic.zBasic.util.file.FilenamePartFilterMiddleZZZ;
import basic.zBasic.util.file.FilenamePartFilterPrefixZZZ;
import basic.zBasic.util.file.FilenamePartFilterSuffixZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class FileFilterServerClientConfigReadmeTemplateOVPN extends AbstractFileFilterZZZ{
	public static String sPREFIX="readme_";
	public static String sMIDDLE="_directory_";
	public static String sSUFFIX="_clientconfig";
	public static String sENDING="txt";		
		
	public FileFilterServerClientConfigReadmeTemplateOVPN() throws ExceptionZZZ {
		super("server"); //der Context ist halt server. Das ist Bestandteil des Templatenamens
	} 
	

	//##### GETTER / SETTER		
		public void setPrefix(String sPrefix) {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(FileFilterServerClientConfigReadmeTemplateOVPN.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(FileFilterServerClientConfigReadmeTemplateOVPN.sMIDDLE);
			}else {
				super.setPrefix(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(FileFilterServerClientConfigReadmeTemplateOVPN.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(FileFilterServerClientConfigReadmeTemplateOVPN.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}
}//END class