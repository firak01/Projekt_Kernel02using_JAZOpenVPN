package use.openvpn.server.status;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;


/**TODO Kann raus, das nun alles auf ISenderObjectStatusBasicZZZ abzielt.
 * Dieses Interface enthaelt Methoden, die von den Klassen implementiert werden muessen, 
 * die den Kernel eigenen Event verwalten sollen.
 * 
 * @author lindhaueradmin
 *
 */
public interface ISenderObjectStatusLocalOVPN{
	public abstract void fireEvent(IEventObjectStatusLocalOVPN event);
	public abstract IEventObjectStatusLocalOVPN getEventPrevious();
	public abstract void setEventPrevious(IEventObjectStatusLocalOVPN event);
	
	public abstract void removeListenerObjectStatusLocalSet(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;
	public abstract void addListenerObjectStatusLocalSet(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;
	public abstract ArrayListUniqueZZZ<IListenerObjectStatusLocalOVPN> getListenerRegisteredAll() throws ExceptionZZZ;
}
