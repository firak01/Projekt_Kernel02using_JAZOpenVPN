package use.openvpn.client;

import java.util.ArrayList;
import java.util.EnumSet;

import basic.zBasic.util.abstractEnum.IEnumSetMappedStatusZZZ;
import basic.zBasic.util.abstractEnum.IEnumSetMappedZZZ;
import basic.zKernel.net.client.IMainZZZ;
import basic.zKernel.status.IStatusLocalUserZZZ;
import use.openvpn.IMainOVPN;
import use.openvpn.client.status.IEventBrokerStatusLocalSetUserOVPN;
import use.openvpn.client.status.IListenerObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetOVPN;
import use.openvpn.client.status.ISenderObjectStatusLocalSetUserOVPN;
import use.openvpn.clientui.IClientTrayMenuZZZ.ClientTrayMenuTypeZZZ;

public interface IClientMainOVPN extends IMainOVPN, ISenderObjectStatusLocalSetUserOVPN{
	//Spezielle ClientOVPN - Methoden
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterList();
	public ArrayList<ClientConfigStarterOVPN> getClientConfigStarterRunningList();
	
	//#################################
	public enum FLAGZ{
		LAUNCHONSTART, CONNECTONSTART, ENABLEPORTSCAN, USEPROXY, DUMMY
	}
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Merke: Das enum ist im Interface besser aufgehoben, als z.B. in einer internen enum-Klasse.
		
	//Merke: Alle Properties des enum müssen im Konstruktor sein, um die Enumeration so zu definieren.
	//ALIAS("Uniquename","Statusmeldung","Beschreibung, wird nicht genutzt....",)
	
	//Merke: Die Message aus anderen Modulen sollte uebernommen werden.
	//       Die Meldung hier wuerde damit dann ueberschrieben. Daher ist hier solch eine Meldung ueberfluessig, ggfs. ein Fallback.
	public enum STATUSLOCAL implements IEnumSetMappedStatusZZZ{//Folgendes geht nicht, da alle Enums schon von einer Java BasisKlasse erben... extends EnumSetMappedBaseZZZ{
		ISSTARTNEW(5, "isstartnew", "CLIENT: Nicht gestarted (ClientMain.STATUSLOCAL)",""),
		ISSTARTING(5, "isstarting","CLIENT: Startet...(ClientMain.STATUSLOCAL)",""),
		ISSTARTED(5, "isstarted","CLIENT: Gestartet (ClientMain.STATUSLOCAL)",""),	
		
		ISCONNECTNEW(5, "isconnectnew","OVPN: Nicht gestartet4",""),
		ISCONNECTING(5, "isconnecting","OVPN: Startet...5",""),
		ISCONNECTED(5, "isconnected","OVPN: Verbunden6",""),
		ISCONNECTINTERUPTED(5, "isconnectinterrupted","OVPN: Verbingungsunterbrechung6b",""),
		
		ISPINGNEW(5, "ispingnew","PING: Thread nicht gestartet7",""),
		ISPINGSTARTING(5, "ispingstarting","PING: Thread startet...8",""),
		ISPINGSTARTED(5, "ispingstarted","PING: Thread gestartet9",""),
		ISPINGCONNECTNEW(5, "ispingconnectnew","PING: Veringungsaufbau nicht gestartet10",""),
		ISPINGCONNECTING(5, "ispingconnecting","PING: Verbinde...11",""),
		ISPINGCONNECTED(5, "ispingconnected","PING: Verbunden12",""),
		ISPINGSTOPPED(5, "ispingstopped","PING: Thread gestoppt13",""),
		HASPINGERROR(5, "haspingerror","PING: Fehler, s. Log14",""),
		
		
		PortScanAllFinished(5, "portscanallfinished","xyz Fragezeichen (ClientMain.STATUSLOCAL)",""),
		
		ISSTOPPED(5, "isstopped","CLIENT: Gestoppt",""),
		HASERROR(5, "haserror","Ein Fehler ist aufgetreten. Details dazu im Log. (ClientMain.STATUSLOCAL)","");
					
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
			
			for(Object obj : ClientTrayMenuTypeZZZ.class.getEnumConstants()){
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
