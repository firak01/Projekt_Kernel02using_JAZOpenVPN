package use.openvpn.client.status;

import basic.zBasic.ExceptionZZZ;
import use.openvpn.server.status.ISenderObjectStatusLocalSetUserOVPN;

/** Interface muss von den Objekten implementiert werden, die den Event-Broker verwenden wollen, 
 *  um einen Event abzufeuern.
 *  Der Event-Broker verwaltet, dann die Objekte die auf den abgefeuerten Event hoeren.
 *  
 *  //ALSO: Die 2. Zeile ist nicht mehr notwendig, wenn das Registrieren (hier des FileIniZZZ Objects am Flag-Event klappt:
 *  //objKernelFGL.setFlag(IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION, false);
	//objKernelFGL.getFileConfigKernelIni().setFlag(IKernelExpressionIniSolverZZZ.FLAGZ.USEEXPRESSION, false);	
 *  
 *  
 *  Merke1: Die Objekte, die lediglich auf den Event "hoeren" brauchen dieses Interface nicht, es ist nur wichtig, wenn das Objekt auch einen Event feuern möchte (halt über den Broker) !!!
 *  Merke2: Der gleiche "Design Pattern" wird auch im UI - Bereich fuer Komponenten verwendet ( package basic.zKernelUI.component.model; ) 
 * @author lindhaueradmin
 *
 */
public interface IEventBrokerStatusLocalSetUserOVPN extends ISenderObjectStatusLocalSetUserOVPN {
	public void registerForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;
	public void unregisterForStatusLocalEvent(IListenerObjectStatusLocalOVPN objEventListener) throws ExceptionZZZ;
}

