package use.openvpn.serverui.component.tray;

import java.util.EnumSet;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;

public interface IServerTrayMenuZZZ {
	//#######################################################
		//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
		//### String fullName, String abbreviation
		//#######################################################
	    
		//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
		//ALIAS("Uniquename","Menuepunkt-Text","Beschreibung, wird nicht genutzt....",)
		public enum ServerTrayMenuTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
			END("end","Beenden",""),
			START("start","Starten",""),		
			LISTEN("listen","Auf Verbindung warten",""),
			PROTOCOL("protocol","Server Protokol ansehen",""),
			DETAIL("detail","PressAction",""),
			PAGE_IP_UPLOAD("page_ip_upload","IP Page hochladen",""),
			FTP_CREDENTIALS("ftp_credentials","FTP Anmeldedaten","");				
			
		private String sAbbreviation,sMenu,sDescription;

		//#############################################
		//#### Konstruktoren
		//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
		//In der Util-Klasse habe ich aber einen Workaround gefunden.
		ServerTrayMenuTypeZZZ(String sAbbreviation, String sMenu, String sDescription) {
		    this.sAbbreviation = sAbbreviation;
		    this.sMenu = sMenu;
		    this.sDescription = sDescription;
		}

		public String getAbbreviation() {
		 return this.sAbbreviation;
		}
		
		public String getMenu() {
			 return this.sMenu;
			}
		
		public EnumSet<?>getEnumSetUsed(){
			return ServerTrayMenuTypeZZZ.getEnumSet();
		}

		/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
		@SuppressWarnings("rawtypes")
		public static <E> EnumSet getEnumSet() {
			
		 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
			//String sFilterName = "FLAGZ"; /
			//...
			//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
			
			//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
			Class<ServerTrayMenuTypeZZZ> enumClass = ServerTrayMenuTypeZZZ.class;
			EnumSet<ServerTrayMenuTypeZZZ> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
			
			Enum[]objaEnum = (Enum[]) enumClass.getEnumConstants();
			for(Object obj : objaEnum){
				//System.out.println(obj + "; "+obj.getClass().getName());
				set.add((ServerTrayMenuTypeZZZ) obj);
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
		public static ServerTrayMenuTypeZZZ fromAbbreviation(String s) {
		for (ServerTrayMenuTypeZZZ state : values()) {
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
