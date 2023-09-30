package use.openvpn.serverui;

import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;

public interface IServerTrayStatusMappedValueZZZ {
	//#######################################################
	//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
	//### String fullName, String abbreviation
	//#######################################################
//	    public enum STATUS{
//			NEW,STARTING,STARTED,LISTENING,CONNECTED,INTERRUPTED,STOPPED,ERROR
//		}
		//Ersetzt durch enum, die Bedeutung bleibt 
//		public static final int iSTATUS_NEW = 0;                       //Wenn das SystemTry-icon neu ist 
//		public static final int iSTATUS_STARTING = 1;               //Die OVPN-Konfiguration wird gesucht und die Processe werden mit diesen Konfigurationen gestartet.
//		public static final int iSTATUS_STARTED = 2;
//		public static final int iSTATUS_LISTENING = 3;               //Die OVPN-Processe laufen.
//		public static final int iSTATUS_CONNECTED = 4;            //Falls sich ein Client per vpn mit dem Server verbunden hat und erreichbar ist
//		public static final int iSTATUS_INTERRUPTED = 5;          //Falls der Client wieder nicht erreichbar ist. Das soll aber keine Fehlermeldung in dem Sinne sein, sondern nur anzeigen, dass mal ein Client verbunden war.
//		                                                                                      //Dies wird auch angezeigt, wenn z.B. die Netzwerkverbindung unterbrochen worden ist.
//		public static final int iSTATUS_STOPPED = 6; 				 //Wenn kein OVPN-Prozess mehr l�uft.
//		public static final int iSTATUS_ERROR = 7;
					
  //Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
  	//ALIAS("Uniquename","Menuepunkt-Text","Icon-Dateiname","Beschreibung, wird nicht genutzt....",)
  	public enum ServerTrayStatusTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
  		NEW("new","","Green Metallic_32.png", ""),
  		STARTING("starting","","pill-button-yellow_benji_01.png",""),
  		STARTED("started","","pill-button-green_benji_01.png",""),
  		LISTENING("listenig","","pill-button-seagreen_benji_01.png",""),
  		CONNECTED("connected","","pill-button-blue_benji_01.png",""),
  		INTERRUPTED("interrupted","","pill-button-purple_benji_01.png",""),
  		STOPPED("stopped","","pill-button-yellow_benji_01.png",""),
  		ERROR("error","","pill-button-red_benji_01.png","");
  		
  	private String sAbbreviation,sMenuText, sIconFileName, sDescription;

  	//#############################################
  	//#### Konstruktoren
  	//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
  	//In der Util-Klasse habe ich aber einen Workaround gefunden.
  	ServerTrayStatusTypeZZZ(String sAbbreviation, String sMenuText, String sIconFileName, String sDescription) {
  	    this.sAbbreviation = sAbbreviation;
  	    this.sMenuText = sMenuText;
  	    this.sIconFileName = sIconFileName;
  	    this.sDescription = sDescription;
  	}

  	
  	public String getAbbreviation() {
  	 return this.sAbbreviation;
  	}
  	
  	public String getMentuText() {
  		return this.sMenuText;
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
