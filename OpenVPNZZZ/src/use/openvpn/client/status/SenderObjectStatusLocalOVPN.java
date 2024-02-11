package use.openvpn.client.status;


import java.io.Serializable;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.abstractList.ArrayListUniqueZZZ;

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
public class SenderObjectStatusLocalOVPN implements ISenderObjectStatusLocalOVPN,Serializable{
	private static final long serialVersionUID = 8999783685575147532L;
	private IEventObjectStatusLocalOVPN eventPrevious=null;
	
	/* (non-Javadoc)
	 * @see use.via.client.module.export.ISenderEventComponentReset#fireEvent(basic.zKernelUI.component.model.KernelEventComponentSelectionResetZZZ)
	 */
	private ArrayListUniqueZZZ<IListenerObjectStatusLocalOVPN> listaLISTENER_REGISTERED = new ArrayListUniqueZZZ<IListenerObjectStatusLocalOVPN>();  //Das ist die Arrayliste, in welche  die registrierten Komponenten eingetragen werden 
																							  //wichtig: Sie muss private sein und kann nicht im Interace global definiert werden, weil es sonst nicht moeglich ist 
	
	
	public SenderObjectStatusLocalOVPN() throws ExceptionZZZ{
		super();
	}
	
	@Override                                                                                     //             mehrere Events, an verschiedenen Komponenten, unabhaengig voneinander zu verwalten.
	public final void fireEvent(IEventObjectStatusLocalOVPN event){	
		/* Die Abfrage nach getSource() funktioniert so mit dem Interface noch nicht....
		 * Auszug aus: KernelSenderComponentSelectionResetZZZ.fireEvent(....)
		if(event.getSource() instanceof ISenderSelectionResetZZZ){
			ISenderSelectionResetZZZ sender = (ISenderSelectionResetZZZ) event.getSource();
			for(int i = 0 ; i < sender.getListenerRegisteredAll().size(); i++){
				IListenerSelectionResetZZZ l = (IListenerSelectionResetZZZ) sender.getListenerRegisteredAll().get(i);
				System.out.println(ReflectCodeZZZ.getMethodCurrentName() + "# EventComponentSelectionResetZZZ by " + event.getSource().getClass().getName() + " fired: " + i);
				l.doReset(event);
			}
		}else{
			for(int i = 0 ; i < this.getListenerRegisteredAll().size(); i++){
				IListenerSelectionResetZZZ l = (IListenerSelectionResetZZZ) this.getListenerRegisteredAll().get(i);				
				System.out.println(ReflectCodeZZZ.getMethodCurrentName() + "# EventComponentSelectionResetZZZ by " + this.getClass().getName() + " - object (d.h. this - object) fired: " + i);
				l.doReset(event);
			}
		}
		*/
		
		main:{
			if(event==null)break main;
			
			//Dafür sorgen, dass der Event nur 1x geworfen wird, wenn der vorherige Event der gleich war.
			IEventObjectStatusLocalOVPN eventPrevious = this.getEventPrevious();
			if(eventPrevious!=null) {
				if(eventPrevious.equals(event))break main;
			}
			this.setEventPrevious(event);
			
			try {
				for(int i = 0 ; i < this.getListenerRegisteredAll().size(); i++){
					IListenerObjectStatusLocalOVPN l = (IListenerObjectStatusLocalOVPN) this.getListenerRegisteredAll().get(i);				
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + "# IListenerObjectStatusLocalSetOVPN by " + this.getClass().getName() + " - object (d.h. this - object) fired: " + (i+1));
					try {
						boolean bStatusLocalChanged = l.changedStatusLocal(event);
						if(bStatusLocalChanged) {
							System.out.println(ReflectCodeZZZ.getPositionCurrent() + "# IListenerObjectStatusLocalSetOVPN for " + l.getClass().getName() + " hat LocalStatusValue '" + event.getStatusValue() + "' fuer externen Event " + event.getStatusText() + "' gesetzt." );
						}					
					} catch (ExceptionZZZ ez) {					
						System.out.println(ReflectCodeZZZ.getPositionCurrent() + "# IListenerObjectStatusLocalSetOVPN by " + this.getClass().getName() + " throws Exception " + ez.getDetailAllLast() );					
					}
				}
			} catch (ExceptionZZZ e) {
				e.printStackTrace();
			}
			
		}//end main:
	}
	
	@Override
	public IEventObjectStatusLocalOVPN getEventPrevious() {
		return this.eventPrevious;
	}

	@Override
	public void setEventPrevious(IEventObjectStatusLocalOVPN event) {
		this.eventPrevious = event;
	}
	
	/* (non-Javadoc)
	 * @see use.via.client.module.export.ISenderEventComponentReset#removeSelectionResetListener(basic.zKernelUI.component.model.ISelectionResetListener)
	 */
	@Override
	public final void removeListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ{	
		this.getListenerRegisteredAll().remove(objEventListener);
	}

	@Override
	public void addListenerObjectStatusLocal(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ {	
		this.getListenerRegisteredAll().add(objEventListener);
	}

	@Override
	public final ArrayListUniqueZZZ<IListenerObjectStatusLocalOVPN> getListenerRegisteredAll() throws ExceptionZZZ{
		return listaLISTENER_REGISTERED;
	}
}

