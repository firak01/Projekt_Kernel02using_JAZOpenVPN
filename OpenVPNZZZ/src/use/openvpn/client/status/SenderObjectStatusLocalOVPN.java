package use.openvpn.client.status;


import java.io.Serializable;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;
import basic.zKernel.status.IEventObjectStatusBasicZZZ;
import basic.zKernel.status.IListenerObjectStatusBasicZZZ;
import basic.zKernel.status.IListenerObjectStatusLocalZZZ;
import basic.zKernel.status.SenderObjectStatusLocalZZZ;

/** Diese Klasse implementiert alles, was benoetigt wird, damit die eigenen Events "Flag hat sich geaendert" abgefeuert werden kann
 *  und auch von den Objekten, die hier registriert sind empfangen wird. Damit fungieren Objekte dieser Klasse als "EventBroker".
 *   
 *   Wichtig: Diese Klasse darf nicht final sein, damit sie von anderen Klassen geerbt werden kann.
 *               Die Methoden dieser Klasse sind allerdings final.
 *               
 *   Merke: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; )            
 * @author lindhaueradmin
 *
 */
public class SenderObjectStatusLocalOVPN extends SenderObjectStatusLocalZZZ implements ISenderObjectStatusLocalOVPN{
	
	public SenderObjectStatusLocalOVPN() throws ExceptionZZZ{
		super();
	}

	@Override
	public void fireEvent(IEventObjectStatusLocalOVPN event) {
		String sLog = "TODOGOON20251111 - Hier ggfs. den Code aus der passenden Methode vor der Umstellung auf Kernel-Klassen einfuegen";
		try {
			this.logProtocolStringWithPosition(sLog);
		} catch (ExceptionZZZ e) {		
			e.printStackTrace();
		}
	}

	@Override
	public IEventObjectStatusLocalOVPN getEventPrevious() {
		String sLog = "TODOGOON20251111 - Hier ggfs. den Code aus der passenden Methode vor der Umstellung auf Kernel-Klassen einfuegen";
		try {
			this.logProtocolStringWithPosition(sLog);
		} catch (ExceptionZZZ e) {		
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setEventPrevious(IEventObjectStatusLocalOVPN event) {
		String sLog = "TODOGOON20251111 - Hier ggfs. den Code aus der passenden Methode vor der Umstellung auf Kernel-Klassen einfuegen";
		try {
			this.logProtocolStringWithPosition(sLog);
		} catch (ExceptionZZZ e) {		
			e.printStackTrace();
		}
	}

	@Override
	public void removeListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		String sLog = "TODOGOON20251111 - Hier ggfs. den Code aus der passenden Methode vor der Umstellung auf Kernel-Klassen einfuegen";
		try {
			this.logProtocolStringWithPosition(sLog);
		} catch (ExceptionZZZ e) {		
			e.printStackTrace();
		}
	}

	@Override
	public void addListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		String sLog = "TODOGOON20251111 - Hier ggfs. den Code aus der passenden Methode vor der Umstellung auf Kernel-Klassen einfuegen";
		try {
			this.logProtocolStringWithPosition(sLog);
		} catch (ExceptionZZZ e) {		
			e.printStackTrace();
		}
	}
}

