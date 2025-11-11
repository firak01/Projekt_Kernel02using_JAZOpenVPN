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
	public void fireEvent(IEventObjectStatusBasicZZZ event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEventPrevious(IEventObjectStatusBasicZZZ event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListenerObject(IListenerObjectStatusBasicZZZ objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListenerObject(IListenerObjectStatusBasicZZZ objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayListUniqueZZZ<IListenerObjectStatusBasicZZZ> getListenerRegisteredAll() throws ExceptionZZZ {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fireEvent(IEventObjectStatusLocalOVPN event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IEventObjectStatusLocalOVPN getEventPrevious() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEventPrevious(IEventObjectStatusLocalOVPN event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {
		// TODO Auto-generated method stub
		
	}
}

