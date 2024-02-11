package use.openvpn.client.status;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;


/**Dieses Interface enthaelt Methoden, die von den Klassen implementiert werden muessen, 
 * die den Kernel eigenen Event verwalten sollen.
 * 
 * @author lindhaueradmin
 *
 */
public interface ISenderObjectStatusLocalOVPN{
	public abstract void fireEvent(IEventObjectStatusLocalOVPN event);
	public abstract IEventObjectStatusLocalOVPN getEventPrevious();
	public void setEventPrevious(IEventObjectStatusLocalOVPN event);
	
	public abstract void removeListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;
	public abstract void addListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;	
	public abstract ArrayListUniqueZZZ<IListenerObjectStatusLocalOVPN> getListenerRegisteredAll() throws ExceptionZZZ;
}
