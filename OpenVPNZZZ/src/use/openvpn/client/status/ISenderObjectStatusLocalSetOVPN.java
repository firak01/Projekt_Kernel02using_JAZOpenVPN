package use.openvpn.client.status;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;


/**Dieses Interface enthaelt Methoden, die von den Klassen implementiert werden muessen, 
 * die den Kernel eigenen Event verwalten sollen.
 * 
 * @author lindhaueradmin
 *
 */
public interface ISenderObjectStatusLocalSetOVPN{
	public abstract void fireEvent(IEventObjectStatusLocalSetOVPN event);
	public abstract IEventObjectStatusLocalSetOVPN getEventPrevious();
	public void setEventPrevious(IEventObjectStatusLocalSetOVPN event);
	
	public abstract void removeListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ;
	public abstract void addListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ;	
	public abstract ArrayListUniqueZZZ<IListenerObjectStatusLocalSetOVPN> getListenerRegisteredAll() throws ExceptionZZZ;
}
