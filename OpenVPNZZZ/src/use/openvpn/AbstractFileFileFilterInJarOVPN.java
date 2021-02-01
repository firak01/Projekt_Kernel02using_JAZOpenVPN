package use.openvpn;

import java.io.File;
import java.io.FilenameFilter;
import java.util.zip.ZipEntry;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ObjectZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.jar.AbstractFileFileFilterInJarZZZ;
import basic.zBasic.util.file.zip.FilenamePartFilterEndingZipZZZ;
import basic.zBasic.util.file.zip.FilenamePartFilterMiddleZipZZZ;
import basic.zBasic.util.file.zip.FilenamePartFilterPathZipZZZ;
import basic.zBasic.util.file.zip.FilenamePartFilterPrefixZipZZZ;
import basic.zBasic.util.file.zip.FilenamePartFilterSuffixZipZZZ;
import basic.zBasic.util.file.zip.ZipEntryFilter;
import basic.zKernel.flag.IFlagZZZ;
import basic.zUtil.io.IFileExpansionUserZZZ;
import basic.zUtil.io.IFileExpansionZZZ;

public abstract class AbstractFileFileFilterInJarOVPN extends AbstractFileFileFilterInJarZZZ implements ZipEntryFilter,IFileExpansionUserZZZ{
	protected String sOvpnContext="";
	
	//wg. des Interfaces IFileExpansionUserZZZ
	protected IFileExpansionZZZ objExpansion = null;
	
	
	public AbstractFileFileFilterInJarOVPN() throws ExceptionZZZ {
		this("");
	}		
	public AbstractFileFileFilterInJarOVPN(String sOvpnContextServerOrClient) throws ExceptionZZZ {
		super();
		AbstractOVPNFileFilterInJarNew_(sOvpnContextServerOrClient, null);
	} 
	public AbstractFileFileFilterInJarOVPN(String sOvpnContextServerOrClient, String sFlagControlIn) throws ExceptionZZZ {
		super();
		String[] saFlagControl = new String[1];
		saFlagControl[0] = sFlagControlIn;
		AbstractOVPNFileFilterInJarNew_(sOvpnContextServerOrClient, saFlagControl);
	}
	public AbstractFileFileFilterInJarOVPN(String sOvpnContextServerOrClient, String[] saFlagControlIn) throws ExceptionZZZ {
		super();
		AbstractOVPNFileFilterInJarNew_(sOvpnContextServerOrClient, saFlagControlIn);
	} 
	private void AbstractOVPNFileFilterInJarNew_(String sOvpnContextServerOrClient, String[] saFlagControlIn) throws ExceptionZZZ {
		String stemp; boolean btemp;
		main:{
		//setzen der übergebenen Flags	
		if(saFlagControlIn != null){
			for(int iCount = 0;iCount<=saFlagControlIn.length-1;iCount++){
				stemp = saFlagControlIn[iCount];
				btemp = setFlag(stemp, true);
				if(btemp==false){ 								   
					   ExceptionZZZ ez = new ExceptionZZZ( sERROR_FLAG_UNAVAILABLE + stemp, iERROR_FLAG_UNAVAILABLE, ReflectCodeZZZ.getMethodCurrentName(), ""); 
					   //doesn�t work. Only works when > JDK 1.4
					   //Exception e = new Exception();
					   //ExceptionZZZ ez = new ExceptionZZZ(stemp,iCode,this, e, "");
					   throw ez;		 
				}
			}
			}

		//+++ Falls das Debug-Flag gesetzt ist, muss nun eine Session �ber das Factory-Objekt erzeugt werden. 
		// Damit kann auf andere Datenbanken zugegriffen werden (z.B. im Eclipse Debugger)
		// Besser jedoch ist es beim Debuggen mit einem anderen Tool eine Notes-ID zu verwenden, die ein leeres Passwort hat.
		btemp = this.getFlag("init");
		if(btemp==true) break main;
		
		
		this.setOvpnContext(sOvpnContextServerOrClient);		

		//Die konkreten Ausprägungen können erst in der accept Methode gefüllt werden, mit den konkreten Werten.
		//Z.B. für Middle-Wert steht in der accept-Methode:
		//		this.objFilterMiddle.setCriterion(this.getMiddle());
		//      if(this.objFilterMiddle.accept(ze)==false) break main;
//		objFilterPath = new FilenamePartFilterPathZipZZZ();
//		objFilterPrefix = new FilenamePartFilterPrefixZipZZZ();
//		objFilterMiddle = new FilenamePartFilterMiddleZipZZZ();
//		objFilterSuffix = new FilenamePartFilterSuffixZipZZZ();
//		objFilterEnding = new FilenamePartFilterEndingZipZZZ();
		
		}//end main:		
	}
	
//	public boolean accept(ZipEntry ze) {
//		boolean bReturn=false;
//		main:{
//			if(ze==null) break main;				
//			
//			//Merke: Die Reihenfolge ist so gewählt, dass im Template Verzeichnis frühestmöglich ein "break main" erreicht wird.
//			
//			//Falls das Verzeichnis nicht passt	
//			if(!StringZZZ.isEmpty(this.getDirectoryPath())){
//				this.objFilterPath.setCriterion(this.getDirectoryPath());
//				if(this.objFilterPath.accept(ze)==false) break main;
//			}
//			
//			//Falls der OvpnContext nicht passt
//			this.objFilterMiddle.setCriterion(this.getMiddle());
//			if(this.objFilterMiddle.accept(ze)==false) break main;
//	
//			//Template-Dateinamen fangen eben mit einem bestimmten String an.
//			this.objFilterPrefix.setCriterion(this.getPrefix());
//			if(this.objFilterPrefix.accept(ze)==false) break main;
//								
//			//Falls die Endung nicht passt
//			this.objFilterEnding.setCriterion(this.getEnding());
//			if(this.objFilterEnding.accept(ze)==false) break main;
//					
//			//Falls das Suffix nicht passt
//			this.objFilterSuffix.setCriterion(this.getSuffix());
//			if(this.objFilterSuffix.accept(ze)==false) break main;
//												
//			bReturn = true;
//		}//END main:
//		return bReturn;		
//	}
	
	//##### GETTER / SETTER	
		public void setOvpnContext(String sContext) {
			this.sOvpnContext=sContext;
		}
		public String getOvpnContext() {
			return this.sOvpnContext;
		}
	
//		protected void setDirectoryPath(String sDirectoryPath) {
//			this.sDirectoryPath = sDirectoryPath;
//		}
//		protected String getDirectoryPath() {
//			if(StringZZZ.isEmpty(this.sDirectoryPath)) {
//				this.setDirectoryPath("");
//			}
//			return this.sDirectoryPath;
//		}
//		
//		protected void setPrefix(String sPrefix) {
//			this.sPrefix = sPrefix;
//		}
//		protected String getPrefix() {
//			if(StringZZZ.isEmpty(this.sPrefix)) {
//				this.setPrefix("");
//			}
//			return this.sPrefix;
//		}
//		
//		protected void setMiddle(String sMiddle) {
//			this.sMiddle = sMiddle;
//		}
//		protected String getMiddle() {
//			if(StringZZZ.isEmpty(this.sMiddle)) {
//				this.setMiddle("");
//			}
//			return this.sMiddle;
//		}
//
//		
//		protected void setSuffix(String sSuffix) {
//			this.sSuffix = sSuffix;
//		}
//		protected String getSuffix() {
//			if(StringZZZ.isEmpty(this.sSuffix)) {
//				this.setSuffix("");
//			}
//			return this.sSuffix;
//		}
//		
//		protected void setEnding(String sEnding){
//			this.sEnding = sEnding;
//		}
//		protected String getEnding() {
//			if(StringZZZ.isEmpty(this.sEnding)) {
//				this.setEnding("");
//			}
//			return this.sEnding;
//		}				
//		
//		public IFileExpansionZZZ getFileExpansionObject() {
//			return this.objExpansion;
//		}
//		public void setFileExpansionObject(IFileExpansionZZZ objFileExpansion) {
//			this.objExpansion = objFileExpansion;
//		}
}//END class