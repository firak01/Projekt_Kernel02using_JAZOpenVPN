package use.openvpn.client;

import java.io.File;
import java.io.FilenameFilter;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.IFlagZZZ;
import basic.zBasic.ObjectZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileFilterEndingZZZ;
import basic.zBasic.util.file.FileFilterMiddleZZZ;
import basic.zBasic.util.file.FileFilterPrefixZZZ;
import basic.zBasic.util.file.FileFilterSuffixZZZ;
import basic.zUtil.io.IFileExpansionUserZZZ;
import basic.zUtil.io.IFileExpansionZZZ;
import use.openvpn.ConfigFileTemplateOvpnOVPN;

public abstract class AbstractOVPNFileFilterZZZ extends ObjectZZZ implements FilenameFilter, IFileExpansionUserZZZ{
	public enum FLAGZ{
		REGARD_FILE_EXPANSION_ALL, REGARD_FILE_EXPANSION_LAST;
	}
	protected FileFilterPrefixZZZ objFilterPrefix;
	protected FileFilterMiddleZZZ objFilterMiddle;
	protected FileFilterSuffixZZZ objFilterSuffix;	
	protected FileFilterEndingZZZ objFilterEnding;
	
	protected String sOvpnContext="";
	
	protected String sPrefix="";
	protected String sMiddle="";
	protected String sSuffix="";
	protected String sEnding="";
	
	//wg. des Interfaces IFileExpansionUserZZZ
	protected IFileExpansionZZZ objExpansion = null;
	
	
	public AbstractOVPNFileFilterZZZ() throws ExceptionZZZ {
		this("");
	}		
	public AbstractOVPNFileFilterZZZ(String sOvpnContextServerOrClient) throws ExceptionZZZ {
		super();
		AbstractOVPNFileFilterNew_(sOvpnContextServerOrClient, null);
	} 
	public AbstractOVPNFileFilterZZZ(String sOvpnContextServerOrClient, String sFlagControlIn) throws ExceptionZZZ {
		super();
		String[] saFlagControl = new String[1];
		saFlagControl[0] = sFlagControlIn;
		AbstractOVPNFileFilterNew_(sOvpnContextServerOrClient, saFlagControl);
	}
	public AbstractOVPNFileFilterZZZ(String sOvpnContextServerOrClient, String[] saFlagControlIn) throws ExceptionZZZ {
		super();
		AbstractOVPNFileFilterNew_(sOvpnContextServerOrClient, saFlagControlIn);
	} 
	private void AbstractOVPNFileFilterNew_(String sOvpnContextServerOrClient, String[] saFlagControlIn) throws ExceptionZZZ {
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
		
//Diese Angaben gelten eben nicht für alle FileFilter, darum nicht in dieser abstrakten Elternklasse verwenden.
//		this.setPrefix(ConfigFileTemplateOvpnOVPN.sFILE_TEMPLATE_PREFIX);
//		this.setMiddle(this.getOvpnContext());

//Auch die konkreten Ausprägungen können erst in der konkreten Kindklasse gefüllt werden.		
		objFilterPrefix = new FileFilterPrefixZZZ();
		objFilterMiddle = new FileFilterMiddleZZZ();
		objFilterSuffix = new FileFilterSuffixZZZ();
		objFilterEnding = new FileFilterEndingZZZ();
		
		}//end main:		
	}
	
	public boolean accept(File objFileDir, String sName) {
		boolean bReturn=false;
		main:{
			if(sName==null) break main;				
			
			//Merke: Die Reihenfolge ist so gewählt, dass im Template Verzeichnis frühestmöglich ein "break main" erreicht wird.
			
			//Falls der OvpnContext nicht passt
			this.objFilterMiddle.setCriterion(this.getMiddle());
			if(this.objFilterMiddle.accept(objFileDir, sName)==false) break main;
	
			//Template-Dateinamen fangen eben mit einem bestimmten String an.
			this.objFilterPrefix.setCriterion(this.getPrefix());
			if(this.objFilterPrefix.accept(objFileDir, sName)==false) break main;
								
			//Falls die Endung nicht passt
			this.objFilterEnding.setCriterion(this.getEnding());
			if(this.objFilterEnding.accept(objFileDir, sName)==false) break main;
					
			//Falls das Suffix nicht passt
			if(this.getFlag(FLAGZ.REGARD_FILE_EXPANSION_ALL.name()) || (this.getFlag(FLAGZ.REGARD_FILE_EXPANSION_LAST.name()))) {
				IFileExpansionZZZ objExpansion = this.getFileExpansionObject();
				
				//TODO GOON 20200324: Berücksichtigung der "FileExpansion" 
				if(this.getFlag(FLAGZ.REGARD_FILE_EXPANSION_ALL.name())){
					//Falls das Flag Regard_FILE_EXPANSION_ALL gesetzt ist:
					//... Nur prüfen, ob hinter dem Suffix ein "Zahlenwert steht".
					
				}else if(this.getFlag(FLAGZ.REGARD_FILE_EXPANSION_LAST.name())) {
					//Falls das Flag Regard_FILE_EXPANSION_LAST gesetzt ist:
					//... Rückwärts vom maximalen Wert zu 1 gehen und den ersten gefundenen Wert zurückgeben.
					//
				}												
			}else {
				this.objFilterSuffix.setCriterion(this.getSuffix());
				if(this.objFilterSuffix.accept(objFileDir, sName)==false) break main;
			}												
			bReturn = true;
		}//END main:
		return bReturn;		
	}
	
	//##### GETTER / SETTER	
		public void setOvpnContext(String sContext) {
			this.sOvpnContext=sContext;
		}
		public String getOvpnContext() {
			return this.sOvpnContext;
		}
	
		protected void setPrefix(String sPrefix) {
			this.sPrefix = sPrefix;
		}
		protected String getPrefix() {
			if(StringZZZ.isEmpty(this.sPrefix)) {
				this.setPrefix("");
			}
			return this.sPrefix;
		}
		
		protected void setMiddle(String sMiddle) {
			this.sMiddle = sMiddle;
		}
		protected String getMiddle() {
			if(StringZZZ.isEmpty(this.sMiddle)) {
				this.setMiddle("");
			}
			return this.sMiddle;
		}

		
		protected void setSuffix(String sSuffix) {
			this.sSuffix = sSuffix;
		}
		protected String getSuffix() {
			if(StringZZZ.isEmpty(this.sSuffix)) {
				this.setSuffix("");
			}
			return this.sSuffix;
		}
		
		protected void setEnding(String sEnding){
			this.sEnding = sEnding;
		}
		protected String getEnding() {
			if(StringZZZ.isEmpty(this.sEnding)) {
				this.setEnding("");
			}
			return this.sEnding;
		}
		
		public void setFileExpansionObject(IFileExpansionZZZ objFileExpansion) {
			this.objExpansion = objFileExpansion;
		}
		public IFileExpansionZZZ getFileExpansionObject() {
			return this.objExpansion;
		}
}//END class