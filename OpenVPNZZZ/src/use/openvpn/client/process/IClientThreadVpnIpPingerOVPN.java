package use.openvpn.client.process;

import java.util.EnumSet;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.component.IProgramRunnableZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusLocalZZZ;
import basic.zKernel.component.IKernelModuleZZZ;
import use.openvpn.client.IClientMainUserOVPN;
import use.openvpn.serverui.component.tray.IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ;

public interface IClientThreadVpnIpPingerOVPN extends IKernelModuleZZZ, IClientMainUserOVPN, IProgramRunnableZZZ{
	public boolean isStatusLocalRelevant(IEnumSetMappedStatusLocalZZZ objEnumStatusIn) throws ExceptionZZZ;
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusLocalZZZ enumStatusIn, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public boolean setStatusLocalEnum(int iIndexOfProcess, IEnumSetMappedStatusLocalZZZ enumStatusIn, String sMessage, boolean bStatusValue) throws ExceptionZZZ;
	public boolean offerStatusLocal(int iIndexOfProcess, Enum enumStatusIn, String sStatusMessage, boolean bStatusValue) throws ExceptionZZZ;
	
	public enum FLAGZ{
		DUMMY,WAIT_FOR_CLIENTSTART, WAIT_FOR_CLIENTCONNECT, END_ON_CONNECTION
	}

	
	boolean getFlag(FLAGZ objEnumFlag) throws ExceptionZZZ;
	boolean setFlag(FLAGZ objEnumFlag, boolean bFlagValue) throws ExceptionZZZ;
	boolean[] setFlag(FLAGZ[] objaEnumFlag, boolean bFlagValue) throws ExceptionZZZ;
	boolean proofFlagExists(FLAGZ objEnumFlag) throws ExceptionZZZ;
	boolean proofFlagSetBefore(FLAGZ objEnumFlag) throws ExceptionZZZ;
	
	
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Die StatusId für Stati, aus dieser Klasse selbst. Nicht die Stati der anderen Klassen.
	public static int iSTATUSLOCAL_GROUPID=2;
			
	//++++++++++++++++++++++++
	
	//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
	//ALIAS("Uniquename","Statusmeldung","Beschreibung, wird nicht genutzt....",)
	public enum STATUSLOCAL implements IEnumSetMappedStatusLocalZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
		ISSTARTNEW(iSTATUSLOCAL_GROUPID,"isstartnew","PING: Nicht gestarted",""),
		ISSTARTING(iSTATUSLOCAL_GROUPID,"isstarting","PING: Startet...",""),		
		ISSTARTED(iSTATUSLOCAL_GROUPID,"isstarted","PING: Gestarted",""),
		
		ISCONNECTNEW(iSTATUSLOCAL_GROUPID,"isconnectnew","PING: Nicht verbunden",""),
		ISCONNECTING(iSTATUSLOCAL_GROUPID,"isconnecting","PING: Verbinde...",""),
		ISCONNECTED(iSTATUSLOCAL_GROUPID,"isconnected","PING: Verbunden",""),
		ISCONNECTNO(iSTATUSLOCAL_GROUPID,"isconnectno","PING: Nicht verbunden",""),
		
		ISSTOPPED(iSTATUSLOCAL_GROUPID,"isstopped","PING: Gestoppt",""),
		HASERROR(iSTATUSLOCAL_GROUPID,"haserror","PING: Fehler",""),
		HASCLIENTNOTSTARTING(iSTATUSLOCAL_GROUPID,"hasclientnotstarting","PING: Client nicht gestarted",""),
		HASCLIENTNOTSTARTED(iSTATUSLOCAL_GROUPID,"hasclientnotstarted","PING: Client nicht fertig mit Start. Wartet auf Process?",""),
		HASCLIENTNOTCONNECTED(iSTATUSLOCAL_GROUPID,"hasclientnotconnected","PING: Client nicht verbunden.","");
		
		private int iGroupId;
		private String sAbbreviation,sStatusMessage,sDescription;
	
		//#############################################
		//#### Konstruktoren
		//Merke: Enums haben keinen public Konstruktor, können also nicht instiantiiert werden, z.B. durch Java-Reflektion.
		//In der Util-Klasse habe ich aber einen Workaround gefunden.
		STATUSLOCAL(int iGroupId, String sAbbreviation, String sStatusMessage, String sDescription) {
			this.iGroupId = iGroupId;
		    this.sAbbreviation = sAbbreviation;
		    this.sStatusMessage = sStatusMessage;
		    this.sDescription = sDescription;
		}
	
		public int getStatusGroupId() {
			return this.iGroupId;
		}
		
		public String getAbbreviation() {
		 return this.sAbbreviation;
		}
		
		public String getStatusMessage() {
			 return this.sStatusMessage;
		}
		
		public EnumSet<?>getEnumSetUsed(){
			return STATUSLOCAL.getEnumSet();
		}
	
		/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
		@SuppressWarnings("rawtypes")
		public static <E> EnumSet getEnumSet() {
			
		 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
			//String sFilterName = "FLAGZ"; /
			//...
			//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
			
			//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
			Class<STATUSLOCAL> enumClass = STATUSLOCAL.class;
			EnumSet<STATUSLOCAL> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
			
			Enum[]objaEnum = (Enum[]) enumClass.getEnumConstants();
			for(Object obj : objaEnum){
				//System.out.println(obj + "; "+obj.getClass().getName());
				set.add((STATUSLOCAL) obj);
			}
			return set;
			
		}
	
		//TODO: Mal ausprobieren was das bringt
		//Convert Enumeration to a Set/List
		private static <E extends Enum<E>>EnumSet<E> toEnumSet(Class<E> enumClass,long vector){
			  EnumSet<E> set=EnumSet.noneOf(enumClass);
			  long mask=1;
			  for (  E e : enumClass.getEnumConstants()) {
			    if ((mask & vector) == mask) {
			      set.add(e);
			    }
			    mask<<=1;
			  }
			  return set;
			}
	
		//+++ Das könnte auch in einer Utility-Klasse sein.
		//the valueOfMethod <--- Translating from DB
		public static STATUSLOCAL fromAbbreviation(String s) {
		for (STATUSLOCAL state : values()) {
		   if (s.equals(state.getAbbreviation()))
		       return state;
		}
		throw new IllegalArgumentException("Not a correct abbreviation: " + s);
		}
	
		//##################################################
		//#### Folgende Methoden bring Enumeration von Hause aus mit. 
				//Merke: Diese Methoden können aber nicht in eine abstrakte Klasse verschoben werden, zum daraus Erben. Grund: Enum erweitert schon eine Klasse.
		@Override
		public String getName() {	
			return super.name();
		}
	
		@Override
		public String toString() {//Mehrere Werte mit # abtennen
		    return this.sAbbreviation+"="+this.sDescription;
		}
	
		@Override
		public int getIndex() {
			return ordinal();
		}
	
		//### Folgende Methoden sind zum komfortablen Arbeiten gedacht.
		@Override
		public int getPosition() {
			return getIndex()+1; 
		}
	
		@Override
		public String getDescription() {
			return this.sDescription;
		}
		//+++++++++++++++++++++++++
	}//End internal Class
}
