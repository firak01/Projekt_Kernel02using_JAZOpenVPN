package use.openvpn.serverui.component.IPExternalUpload;


import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import use.openvpn.serverui.common.component.AbstractProgramIPContentOVPN;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.KernelUseObjectZZZ;
import basic.zKernel.component.AbstractKernelProgramZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;
import basic.zKernelUI.KernelUIZZZ;
import basic.zKernelUI.component.AbstractKernelProgramUIZZZ;
import basic.zKernelUI.component.KernelJDialogExtendedZZZ;
import basic.zKernelUI.component.KernelJFrameCascadedZZZ;
import basic.zKernelUI.component.KernelJPanelCascadedZZZ;

/**Vereinfacht den Zugriff auf die HTML-Seite, in der die externe IPAdresse des Servers bekannt gemacht wird. 
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwentet.
 * @author 0823
 *
 */
//20210222 Nutze abstrakt, Package use.openvpn.common
//         Mache dann ProgramIPContentWebOVPN und ProgramIPConententLocalOVPN extends AbstractProgramIPContenOVPN
public class ProgramIPContentLocalOVPN extends AbstractProgramIPContentOVPN implements IConstantProgramIpLocalOVPN{		
	public ProgramIPContentLocalOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
	}
		
	
	//### Getter / Setter

	public String readIpExternal() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			String sURL = this.getUrl2Read();
			if(StringZZZ.isEmpty(sURL)){
				ExceptionZZZ ez = new ExceptionZZZ("URL to read Ip from", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			
			String[] satemp = {"UseStream"};
			KernelReaderURLZZZ objReaderURL = new KernelReaderURLZZZ(objKernel, sURL,satemp, "");			
			if(this.getFlag("useProxy")==true){
				this.updateLabel("Proxy: Connecting ...");
				boolean btemp = this.readProxyEnabled();
				if(btemp){
					String sProxyHost = this.getIpProxy();
					String sProxyPort = this.getPortProxy();
					objReaderURL.setProxyEnabled(sProxyHost, sProxyPort);
					this.updateLabel("Proxy: Continue ...");
				}else{
					this.updateLabel("No proxy: Continue ...");
				}
			}
						
			//+++ Nachdem nun ggf. der Proxy aktiviert wurde, die Web-Seite versuchen auszulesen				
			//+++ Den IP-Wert holen aus dem HTML-Code der konfigurierten URL
			KernelReaderPageZZZ objReaderPage = objReaderURL.getReaderPage();
			KernelReaderHtmlZZZ objReaderHTML = objReaderPage.getReaderHTML();
			 
			//Nun alle input-Elemente holen und nach dem Namen "IPNr" suchen.
			TagTypeInputZZZ objTagTypeInput = new TagTypeInputZZZ(objKernel);			
			TagInputZZZ objTag = (TagInputZZZ) objReaderHTML.readTagFirstZZZ(objTagTypeInput, "IPNr");
			if(objTag!=null) {
				sReturn = objTag.readValue();  //Merke: Das Eintragen des Wertes wird der uebergeordneten Methode ueberlassen. 
			}else {
				this.updateLabel("No Tag found in Page: IPNr");
			}
		}//end main:
		this.setIpExternal(sReturn);
		return sReturn;
	}	
	
	
	/**Read from the configuration file a proxy which might be necessary to use AND enables the proxy for this application.
	 * Remember: This proxy is used to read the url (containing the ip adress)
	 *                  
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 14:20:24
	 */
	public boolean readProxyEnabled() throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			
		    //+++ Ggf. notwendige Proxy-Einstellung pr�fen.
			//Z.B. bei der itelligence bin ich hinter einem Proxy. Die auszulesende Seite ist aber im Web.
			String sProxyHost = this.getIpProxy();			
			if(!StringZZZ.isEmpty(sProxyHost)){		//Eine Proxy-Konfiguration ist nicht Pflicht		
				String sProxyPort = this.getPortProxy();
				
				//+++ Nun versuchen herauszufinden, ob der Proxy auch erreichbar ist und existiert. Nur nutzen, falls er existiert
				KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
				try{ //Hier soll nicht abgebrochen werden, wenn es nicht klappt. Lediglich ins Log soll etwas geschrieben werden.
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Trying to reach the proxy configured. '" + sProxyHost + " : " + sProxyPort +"'");									
					bReturn = objPing.ping(sProxyHost, sProxyPort);								
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Configured proxy reached. " + sProxyHost + " : " + sProxyPort +"'");
					bReturn = true;
					break main;
					
				}catch(ExceptionZZZ ez){
					objKernel.getLogObject().WriteLineDate("Will not use the proxy configured, because: " + ez.getDetailAllLast());
					ReportLogZZZ.write(ReportLogZZZ.DEBUG, "Configured proxy unreachable. " + sProxyHost + " : " + sProxyPort +"'. No proxy will be enabled.");
				}	
			}else{
				ReportLogZZZ.write(ReportLogZZZ.DEBUG, "No proxy configured.");								
			}//END 	if(sProxyHost!=null && sProxyHost.equals("")==false){		//Eine Proxy-Konfiguration ist nicht Pflicht		
		}//END main
		return bReturn;
	}
	
//	######### GetFlags - Handled ##############################################
	
	
// ######### Auftruf abstrakter Methodn ######################################	
	/**Aus dem Worker-Thread heraus wird ein Thread gestartet (der sich in die EventQueue von Swing einreiht.)
	* @param stext
	* 
	* lindhaueradmin; 17.01.2007 12:09:17
	 */
	public void updateLabel(String stext){
		super.updateLabel(sCOMPONENT_TEXTFIELD, stext);
	}	
}

