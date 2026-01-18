package use.openvpn.server;

import java.util.ArrayList;
import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusLocalZZZ;
import use.openvpn.IMainOVPN;
import use.openvpn.server.process.IServerThreadProcessWatchMonitorOVPN;
import use.openvpn.server.status.ISenderObjectStatusLocalUserOVPN;
import use.openvpn.serverui.component.tray.IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ;

public interface IServerMainOVPN extends IMainOVPN, ISenderObjectStatusLocalUserOVPN{
	//Spezielle ServerOVPN - Methoden
	public ArrayList<ServerConfigStarterOVPN> getServerConfigStarterList();
	public ServerConfigStarterOVPN getServerConfigStarter(int iPosition);
	
	//####################################
	public enum FLAGZ{
		DUMMY
	}

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Die StatusId für Stati, aus dieser Klasse selbst. Nicht die Stati der anderen Klassen.
	public static int iSTATUSLOCAL_GROUPID=0;
			
	//++++++++++++++++++++++++
	
	//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
	//ALIAS("Uniquename","Statusmeldung","Beschreibung, wird nicht genutzt....",)
	public enum STATUSLOCAL implements IEnumSetMappedStatusLocalZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
		ISSTARTNEW(iSTATUSLOCAL_GROUPID, "isstartnew","SERVER: Noch nicht gestartet.", ""),
		ISSTARTING(iSTATUSLOCAL_GROUPID, "isstarting","SERVER: Startet. Warte ggfs. auf Task.",""),		
		ISSTARTED(iSTATUSLOCAL_GROUPID, "isstarted","SEVER: OVPN Konfigurationen gebaut und Server gestartet.",""),
				
		ISLISTENERSTARTNEW(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "islistenernew","OVPN: Listener nicht gestartet.",""),		
		ISLISTENERSTARTING(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "islistenerstarting","OVPN: Listener started...",""),
		ISLISTENERSTARTED(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "islistenerstarted","OVPN: Listener wartet auf Verbindung.",""),
		ISLISTENERSTARTNO(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "islistenerstartno","OVPN: Listener ohne Verbindung beendet.",""),
				
		ISLISTENERCONNECTING(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "isconnectno","OVPN: Listener nicht verbunden.",""),
		ISLISTENERCONNECTED(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "isconnected","OVPN: Listener verbunden.",""),		
		ISLISTENERINTERRUPTED(IServerThreadProcessWatchMonitorOVPN.iSTATUSLOCAL_GROUPID, "isinterrupted","OVPN: Listener unterbrochen.",""),
		
		
		WATCHRUNNERSTARTED(iSTATUSLOCAL_GROUPID, "watchrunnerstarted","Thread zur Verbindungspruefung gestartet",""),
		ISSTOPPED(iSTATUSLOCAL_GROUPID, "isstopped","Server wurde gestoppt",""),
		HASERROR(iSTATUSLOCAL_GROUPID, "haserror","Ein Fehler ist aufgetreten","");
					
		private int iStatusGroupId;
		private String sAbbreviation,sStatusMessage,sDescription;
	
		//#############################################
		//#### Konstruktoren
		//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
		//In der Util-Klasse habe ich aber einen Workaround gefunden.
		STATUSLOCAL(int iStatusGroupId, String sAbbreviation, String sStatusMessage, String sDescription) {
			this.iStatusGroupId = iStatusGroupId;
		    this.sAbbreviation = sAbbreviation;
		    this.sStatusMessage = sStatusMessage;
		    this.sDescription = sDescription;
		}
	
		public int getStatusGroupId() {
			return this.iStatusGroupId;
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
