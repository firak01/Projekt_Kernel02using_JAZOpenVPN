package use.openvpn.serverui.component.tray;

import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import use.openvpn.clientui.component.tray.IClientTrayMenuOVPN;
import use.openvpn.serverui.component.tray.IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ;

public interface IServerTrayStatusMappedValueZZZ {
	//#######################################################
	//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
	//### String fullName, String abbreviation
	//#######################################################

	//Merke1: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
    //Merke2: Das ist kein IEnumSetMappedStatus. Ein einfache IEnumSetMapped reicht, da das hier lediglich als Container für den Icondateipfad dienen soll.
	//ALIAS("Uniquename","ClientTryMenuTypeZZZ. ...", "Icon-Dateiname","Beschreibung, wird nicht genutzt....",)
	
	//Noch zu verwendendes Icon: "icons8-web-camera-unfilled-100" "Green Metallic_32.png", "pill-button-yellow_benji_01.png", circle.png,pill-button-green_benji_01.png
  	public enum ServerTrayStatusTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
  		NEW("new",IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START,"icons8-web-camera-filled-100.png",""),  				  				
  		STARTING("starting",IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START,"circle.png",""),
  		STARTED("started", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN,"button-green_benji_park_01.png",""),
  		LISTENERSTARTNEW("listenerstartnew", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN,"button-green_benji_park_01.png",""),
  		LISTENERSTARTING("listenerstarting", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN,"pill-button-yellow_benji_01.png",""),  		
  		LISTENERSTARTED("listenerstarted", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN,"pill-button-seagreen_benji_01.png",""),
  	  		
  		CONNECTED("connected", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN ,"pill-button-blue_benji_01.png",""),  		
  		INTERRUPTED("interrupted", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.LISTEN ,"pill-button-purple_benji_01.png",""),
  		
  		STOPPED("stopped", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START,"pill-button-yellow_benji_01.png",""),
  		ERROR("error", IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ.START,"pill-button-red_benji_01.png",""),
  		
  		PREVIOUSEVENTRTYPE("previousentrytype",null,"","Ohne Bild. Wird genutzt, um in einem weiteren Lauf das passende rauszusuchen. Z.B. wenn nach dem Stop ein vorheriger Status verwendet werden soll");
  	
  		private IEnumSetMappedZZZ objEnum;
  		private String sAbbreviation, sIconFileName, sDescription;

  	//#############################################
  	//#### Konstruktoren
  	//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
  	//In der Util-Klasse habe ich aber einen Workaround gefunden.
  	ServerTrayStatusTypeZZZ(String sAbbreviation, IServerTrayMenuZZZ.ServerTrayMenuTypeZZZ objEnum, String sIconFileName, String sDescription) {
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
  		return ServerTrayStatusTypeZZZ.getEnumSet();
  	}

  	/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
  	@SuppressWarnings("rawtypes")
  	public static <E> EnumSet getEnumSet() {
  		
  	 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
  		//String sFilterName = "FLAGZ"; /
  		//...
  		//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
  		
  		//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
  		Class<ServerTrayStatusTypeZZZ> enumClass = ServerTrayStatusTypeZZZ.class;
  		EnumSet<ServerTrayStatusTypeZZZ> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
  		
  		for(Object obj : ServerTrayStatusTypeZZZ.class.getEnumConstants()){
  			//System.out.println(obj + "; "+obj.getClass().getName());
  			set.add((ServerTrayStatusTypeZZZ) obj);
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
  	public static ServerTrayStatusTypeZZZ fromAbbreviation(String s) {
  	for (ServerTrayStatusTypeZZZ state : values()) {
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
