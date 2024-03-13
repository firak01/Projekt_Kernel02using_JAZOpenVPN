package use.openvpn.clientui.component.tray;

import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
public interface IClientTrayMenuOVPN {
		//#######################################################
		//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
		//### String fullName, String abbreviation
		//#######################################################
	    
		//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
		//ALIAS("Uniquename","Menuepunkt-Text","Beschreibung, wird nicht genutzt....",)
		public enum ClientTrayMenuTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{		
			START("start","Starten",""),
			CONNECT("connect","Verbinden",""),
			PING("ping","Verbindung pruefen",""),
			PROTOCOL("protocol","Client Protokol ansehen",""),
			ADJUSTMENT("adjustment","Einstellungen",""),
			PAGE_IP_READ("page_ip_read","IP aus remote Page auslesen",""),		
			END("end","Beenden",""),
			DETAIL("detail","PressAction","");
							
		private String sAbbreviation,sMenu,sDescription;

		//#############################################
		//#### Konstruktoren
		//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
		//In der Util-Klasse habe ich aber einen Workaround gefunden.
		ClientTrayMenuTypeZZZ(String sAbbreviation, String sMenu, String sDescription) {
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
			return ClientTrayMenuTypeZZZ.getEnumSet();
		}

		/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
		@SuppressWarnings("rawtypes")
		public static <E> EnumSet getEnumSet() {
					
			//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
			Class<ClientTrayMenuTypeZZZ> enumClass = ClientTrayMenuTypeZZZ.class;
			EnumSet<ClientTrayMenuTypeZZZ> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
			
			Enum[]objaEnum = (Enum[]) enumClass.getEnumConstants();
			for(Object obj : objaEnum){		
				set.add((ClientTrayMenuTypeZZZ) obj);
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
		public static ClientTrayMenuTypeZZZ fromAbbreviation(String s) {
		for (ClientTrayMenuTypeZZZ state : values()) {
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
