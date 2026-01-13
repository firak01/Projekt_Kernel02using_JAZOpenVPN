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
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public class FileFilterConfigBatchTemplateOVPN extends AbstractFileFilterZZZ{
	public static String sPREFIX="template_";
	public static String sMIDDLE="";
	public static String sSUFFIX="_starter";
	public static String sENDING="txt";		
		
	public FileFilterConfigBatchTemplateOVPN(String sOvpnContextServerOrClient) throws ExceptionZZZ {
		super(sOvpnContextServerOrClient);
	} 
	public FileFilterConfigBatchTemplateOVPN() throws ExceptionZZZ {
		super();
	}

	//##### GETTER / SETTER		
		public void setPrefix(String sPrefix) throws ExceptionZZZ {
			if(StringZZZ.isEmpty(sPrefix)) {
				super.setPrefix(FileFilterConfigBatchTemplateOVPN.sPREFIX);
			}else {
				super.setPrefix(sPrefix);
			}
		}
		
		
		public void setMiddle(String sMiddle) throws ExceptionZZZ {
			if(StringZZZ.isEmpty(sMiddle)) {
				super.setMiddle(FileFilterConfigBatchTemplateOVPN.sMIDDLE);
			}else {
				super.setPrefix(sMiddle);
			}
		}
		
		public void setSuffix(String sSuffix) throws ExceptionZZZ {
			if(StringZZZ.isEmpty(sSuffix)) {
				super.setSuffix(FileFilterConfigBatchTemplateOVPN.sSUFFIX);
			}else {
				super.setSuffix(sSuffix);
			}
		}
					
		public void setEnding(String sEnding) throws ExceptionZZZ {
			if(StringZZZ.isEmpty(sEnding)) {
				super.setEnding(FileFilterConfigBatchTemplateOVPN.sENDING);
			}else {
				super.setEnding(sEnding);
			}
		}
}//END class