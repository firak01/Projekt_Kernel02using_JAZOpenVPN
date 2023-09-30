package use.openvpn.clientui;

import java.io.Serializable;
import java.util.EnumSet;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import use.openvpn.clientui.IClientTrayMenuZZZ.ClientTrayMenuTypeZZZ;

//#####################################################
//20191123: Um die Enumeration herum eine Klasse bauen.
//            Diese Struktur hat den Vorteil, das solche Werte auch in einer Datenbank per Hibernate persistiert werden können.
//            Verwendet wird solch eine Struktur z.B. in der Defaulttext - Klasse des TileHexMapTHM Projekts
public class ClientTrayMenuZZZ implements Serializable{
	
	//Entsprechend der internen Enumeration
	//Merke: Die Enumeration dient der Festlegung der Defaultwerte. In den Feldern des Entities werden die gespeicherten Werte gehalten.
	private String sAbbreviation,sMenu,sDescription;
			
	public ClientTrayMenuZZZ(){		
	}
						
	public String getAbbreviation(){
		return this.sAbbreviation;
	}
	public void setAbbreviation(String sAbbreviation){
		this.sAbbreviation = sAbbreviation;
	}
	
	public String getMenu(){
		return this.sMenu;
	}
	public void setMenu(String sMenu){
		this.sMenu = sMenu;
	}

	//### Statische Methode (um einfacher darauf zugreifen zu können)
    public static Class getEnumClassStatic(){
    	try{
    		System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Diese Methode muss in den daraus erbenden Klassen überschrieben werden.");
    	}catch(ExceptionZZZ ez){
			String sError = "ExceptionZZZ: " + ez.getMessageLast() + "+\n ThreadID:" + Thread.currentThread().getId() +"\n";			
			System.out.println(sError);
		}
    	return ClientTrayMenuTypeZZZ.class;    	
    }
	
	
	}//End Class
