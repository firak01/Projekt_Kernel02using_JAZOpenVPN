package use.openvpn.clientui;

import java.io.Serializable;
import java.util.EnumSet;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;

//#####################################################
//20191123: Um die Enumeration herum eine Klasse bauen.
//            Diese Struktur hat den Vorteil, das solche Werte auch in einer Datenbank per Hibernate persistiert werden können.
//            Verwendet wird solch eine Struktur z.B. in der Defaulttext - Klasse des TileHexMapTHM Projekts
public class ClientTrayStatusMappedValueZZZ implements Serializable{
	
	//Entsprechend der internen Enumeration
	//Merke: Die Enumeration dient der Festlegung der Defaultwerte. In den Feldern des Entities werden die gespeicherten Werte gehalten.
	private String sAbbreviation,sDescription;
			
	public ClientTrayStatusMappedValueZZZ(){		
	}
						
	public String getAbbreviation(){
		return this.sAbbreviation;
	}
	public void setAbbreviation(String sAbbreviation){
		this.sAbbreviation = sAbbreviation;
	}

	//### Statische Methode (um einfacher darauf zugreifen zu können)
    public static Class getEnumClassStatic(){
    	try{
    		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Diese Methode muss in den daraus erbenden Klassen überschrieben werden.");
    	}catch(ExceptionZZZ ez){
			String sError = "ExceptionZZZ: " + ez.getMessageLast() + "+\n ThreadID:" + Thread.currentThread().getId() +"\n";			
			System.out.println(sError);
		}
    	return ClientTrayStatusTypeZZZ.class;    	
    }
	
	//#######################################################
	//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
	//### String fullName, String abbreviation
	//#######################################################

	//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
	//ALIAS("Uniquename","Menuepunkt-Text","Icon-Dateiname","Beschreibung, wird nicht genutzt....",)
	public enum ClientTrayStatusTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
		NEW("new","","icons8-networking-64_black_bgGray.png", ""),			
		STARTING("starting","","icons8-networking-64_yellow.png",""),	
		STARTED("started","","icons8-networking-64_black_bgYellow.png",""),
		CONNECTING("connecting","","icons8-networking-64_blue.png",""),
		CONNECTED("connected","","icons8-networking-64_black_bgBlueLight.png",""),
		
		WATCHING("watching","","icons8-networking-64_greenLight.png",""),
		WATCHED("watched","","icons8-networking-64_black_bgGreen.png",""),
		
		INTERRUPTED("interrupted","","pill-button-purple_benji_01.png",""),
		STOPPED("stopped","","Green Metallic_32.png",""),		
		ERROR("error","","pill-button-red_benji_01.png",""),
		FAILED("failed","","pill-button-purple_benji_01.png","");
		
	private String sAbbreviation,sMenuText, sIconFileName, sDescription;

	//#############################################
	//#### Konstruktoren
	//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
	//In der Util-Klasse habe ich aber einen Workaround gefunden.
	ClientTrayStatusTypeZZZ(String sAbbreviation, String sMenuText, String sIconFileName, String sDescription) {
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
		
	}//End Class
