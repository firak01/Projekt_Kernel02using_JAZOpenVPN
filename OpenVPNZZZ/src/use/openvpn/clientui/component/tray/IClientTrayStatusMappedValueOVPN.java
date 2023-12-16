package use.openvpn.clientui.component.tray;

import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;

public interface IClientTrayStatusMappedValueOVPN {
		//#######################################################
		//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
		//### String fullName, String abbreviation
		//#######################################################

		//Merke1: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
	    //Merke2: Das ist kein IEnumSetMappedStatus. Ein einfache IEnumSetMapped reicht, da das hier lediglich als Container für den Icondateipfad dienen soll.
		//ALIAS("Uniquename","ClientTryMenuTypeZZZ. ...", "Icon-Dateiname","Beschreibung, wird nicht genutzt....",)
		public enum ClientTrayStatusTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
			NEW("new",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.START, "icons8-networking-64_black_bgGray.png", ""),			
			STARTING("starting",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.START,"icons8-networking-64_yellow.png",""),	
			STARTED("started",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.START,"icons8-networking-64_black_bgYellow.png",""),
			CONNECTING("connecting",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.CONNECT,"icons8-networking-64_blue.png",""),
			CONNECTED("connected",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.CONNECT, "icons8-networking-64_black_bgBlueLight.png",""),
			
			PINGING("pinging",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.PING, "icons8-networking-64_magenta.png",""),
			PINGED("pinged",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.PING, "icons8-networking-64_black_bgMagentaDark.png",""),
			PINGCONNECTING("pingconnecting",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.PING, "icons8-networking-64_greenLight.png",""),
			PINGCONNECTED("pingconnected",IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.PING, "icons8-networking-64_black_bgGreen.png",""),
			PINGCONNECTNO("pingconnectno", IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ.PING, "pill-button-purple_benji_01.png",""),
			
			INTERRUPTED("interrupted",null,"pill-button-purple_benji_01.png",""),
			STOPPED("stopped",null,"Green Metallic_32.png",""),		
			ERROR("error",null,"pill-button-red_benji_01.png",""),
			FAILED("failed",null,"pill-button-purple_benji_01.png",""),
			
			PREVIOUSEVENTRTYPE("previouseventrytype",null,"","Ohne Bild. Wird genutzt, um in einem weiteren Lauf das passende rauszusuchen. Z.B. wenn nach dem Stop ein vorheriger Status verwendet werden soll");
		
			private IEnumSetMappedZZZ objEnum;
			private String sAbbreviation, sIconFileName, sDescription;

		//#############################################
		//#### Konstruktoren
		//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
		//In der Util-Klasse habe ich aber einen Workaround gefunden.
		ClientTrayStatusTypeZZZ(String sAbbreviation, IClientTrayMenuOVPN.ClientTrayMenuTypeZZZ objEnum, String sIconFileName, String sDescription) {			
		    this.sAbbreviation = sAbbreviation;
		    this.objEnum = objEnum;
		    this.sIconFileName = sIconFileName;
		    this.sDescription = sDescription;
		}

		
		public String getAbbreviation() {
		 return this.sAbbreviation;
		}
		
		public IEnumSetMappedZZZ getAccordingTrayMenuType(){
			return this.objEnum;
		}
			
		public String getIconFileName() {
			return this.sIconFileName;
		}
		
		public EnumSet<?>getEnumSetUsed(){
			return ClientTrayStatusTypeZZZ.getEnumSet();
		}

		/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
		@SuppressWarnings("rawtypes")
		public static <E> EnumSet getEnumSet() {
			
		 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
			//String sFilterName = "FLAGZ"; /
			//...
			//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
			
			//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
			Class<ClientTrayStatusTypeZZZ> enumClass = ClientTrayStatusTypeZZZ.class;
			EnumSet<ClientTrayStatusTypeZZZ> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
			
			for(Object obj : ClientTrayStatusTypeZZZ.class.getEnumConstants()){
				//System.out.println(obj + "; "+obj.getClass().getName());
				set.add((ClientTrayStatusTypeZZZ) obj);
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
		public static ClientTrayStatusTypeZZZ fromAbbreviation(String s) {
		for (ClientTrayStatusTypeZZZ state : values()) {
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
