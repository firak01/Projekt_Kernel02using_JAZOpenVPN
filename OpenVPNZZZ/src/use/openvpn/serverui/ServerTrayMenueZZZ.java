package use.openvpn.serverui;

import java.io.Serializable;
import java.util.EnumSet;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;

//#####################################################
//20191123: Um die Enumeration herum eine Klasse bauen.
//            Diese Struktur hat den Vorteil, das solche Werte auch in einer Datenbank per Hibernate persistiert werden können.
//            Verwendet wird solch eine Struktur z.B. in der Defaulttext - Klasse des TileHexMapTHM Projekts
public class ServerTrayMenueZZZ implements Serializable{
	
	//Entsprechend der internen Enumeration
	//Merke: Die Enumeration dient der Festlegung der Defaultwerte. In den Feldern des Entities werden die gespeicherten Werte gehalten.
	private String sAbbreviation,sMenue,sDescription;
			
	public ServerTrayMenueZZZ(){		
	}
						
	public String getAbbreviation(){
		return this.sAbbreviation;
	}
	public void setAbbreviation(String sAbbreviation){
		this.sAbbreviation = sAbbreviation;
	}
	
	public String getMenue(){
		return this.sMenue;
	}
	public void setMenue(String sMenue){
		this.sMenue = sMenue;
	}

	//### Statische Methode (um einfacher darauf zugreifen zu können)
    public static Class getEnumClassStatic(){
    	try{
    		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Diese Methode muss in den daraus erbenden Klassen überschrieben werden.");
    	}catch(ExceptionZZZ ez){
			String sError = "ExceptionZZZ: " + ez.getMessageLast() + "+\n ThreadID:" + Thread.currentThread().getId() +"\n";			
			System.out.println(sError);
		}
    	return ServerTrayMenueTypeZZZ.class;    	
    }
	
	//#######################################################
	//### Eingebettete Enum-Klasse mit den Defaultwerten, diese Werte werden auch per Konstruktor übergeben.
	//### String fullName, String abbreviation
	//#######################################################
    
	//Merke: Obwohl fullName und abbr nicht direkt abgefragt werden, müssen Sie im Konstruktor sein, um die Enumeration so zu definieren.
	//ALIAS("Uniquename","Menuepunkt-Text","Beschreibung, wird nicht genutzt....",)
	public enum ServerTrayMenueTypeZZZ implements IEnumSetMappedZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
		END("end","Beenden",""),
		START("start","Starten",""),		
		LISTEN("listen","Auf Verbindung warten",""),
		LOG("log","Server Log ansehen",""),
		DETAIL("detail","PressAction",""),
		PAGE_IP_UPLOAD("page_ip_upload","IP Page hochladen",""),
		FTP_CREDENTIALS("ftp_credentials","FTP Anmeldedaten","");				
		
	private String sAbbreviation,sMenue,sDescription;

	//#############################################
	//#### Konstruktoren
	//Merke: Enums haben keinen public Konstruktor, können also nicht intiantiiert werden, z.B. durch Java-Reflektion.
	//In der Util-Klasse habe ich aber einen Workaround gefunden.
	ServerTrayMenueTypeZZZ(String sAbbreviation, String sMenue, String sDescription) {
	    this.sAbbreviation = sAbbreviation;
	    this.sMenue = sMenue;
	    this.sDescription = sDescription;
	}

	public String getAbbreviation() {
	 return this.sAbbreviation;
	}
	
	public String getMenue() {
		 return this.sMenue;
		}
	
	public EnumSet<?>getEnumSetUsed(){
		return ServerTrayMenueTypeZZZ.getEnumSet();
	}

	/* Die in dieser Methode verwendete Klasse für den ...TypeZZZ muss immer angepasst werden. */
	@SuppressWarnings("rawtypes")
	public static <E> EnumSet getEnumSet() {
		
	 //Merke: Das wird anders behandelt als FLAGZ Enumeration.
		//String sFilterName = "FLAGZ"; /
		//...
		//ArrayList<Class<?>> listEmbedded = ReflectClassZZZ.getEmbeddedClasses(this.getClass(), sFilterName);
		
		//Erstelle nun ein EnumSet, speziell für diese Klasse, basierend auf  allen Enumrations  dieser Klasse.
		Class<ServerTrayMenueTypeZZZ> enumClass = ServerTrayMenueTypeZZZ.class;
		EnumSet<ServerTrayMenueTypeZZZ> set = EnumSet.noneOf(enumClass);//Erstelle ein leeres EnumSet
		
		for(Object obj : ServerTrayMenueTypeZZZ.class.getEnumConstants()){
			//System.out.println(obj + "; "+obj.getClass().getName());
			set.add((ServerTrayMenueTypeZZZ) obj);
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
	public static ServerTrayMenueTypeZZZ fromAbbreviation(String s) {
	for (ServerTrayMenueTypeZZZ state : values()) {
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
