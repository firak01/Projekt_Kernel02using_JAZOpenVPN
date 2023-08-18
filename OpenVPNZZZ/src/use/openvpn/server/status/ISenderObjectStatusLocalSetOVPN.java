package use.openvpn.server.status;

import java.util.ArrayList;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.status.IEventObjectStatusLocalSetZZZ;
import basic.zKernel.status.ISenderObjectStatusLocalSetZZZ;


/**Dieses Interface enthaelt Methoden, die von den Klassen implementiert werden muessen, 
 * die den Kernel eigenen Event verwalten sollen.
 * 
 * @author lindhaueradmin
 *
 */
public interface ISenderObjectStatusLocalSetOVPN{
	public abstract void fireEvent(IEventObjectStatusLocalSetOVPN event);

	public abstract void removeListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ;

	public abstract void addListenerObjectStatusLocalSet(IListenerObjectStatusLocalSetOVPN objEventListener) throws ExceptionZZZ;
	
	public abstract ArrayList<IListenerObjectStatusLocalSetOVPN> getListenerRegisteredAll() throws ExceptionZZZ;
}
