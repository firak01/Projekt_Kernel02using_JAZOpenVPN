package use.openvpn.component;


import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import custom.zKernel.file.ini.FileIniZZZ;
import basic.zKernel.IKernelConfigSectionEntryZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.log.ReportLogZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
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
 * Wird im Button "IPExternal"-Refresh der Dialogbox Connect/IPExternall verwendet.
 * @author 0823
 *
 */
//20210222 Mache dies abstrakt, Package use.openvpn.common
//                   Mache dann ProgramIPContentWebOVPN, ProgramIPConententLocalOVPN extends AbstractProgramIPContenOVPN
public abstract class AbstractProgramIPContentOVPN extends AbstractKernelProgramUIZZZ{
	private String sURL2Read=null;
	private String sIPExternal = null;
	private String sIPProxy = null;
	private String sPortProxy = null;
	
	
	
	private boolean bFlagUseProxy = false;

	public AbstractProgramIPContentOVPN(IKernelZZZ objKernel, KernelJPanelCascadedZZZ panel, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel,panel,saFlagControl);
		main:{
			this.setPanelParent(panel);										
		}//END main
	}
		
	
	//### Getter / Setter
	public String getUrl2Read() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sURL2Read)){
			String stemp = this.readUrl2Read();
			this.sURL2Read = stemp;
		}
		return this.sURL2Read;
	}
	
	public String readUrl2Read() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "URL2Read");
			sReturn = objEntry.getValue();
		}
		return sReturn;
	}
	
	public String getIpProxy() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sIPProxy)){
			String sIP = this.readIpProxy();
			this.sIPProxy = sIP;
		}
		return this.sIPProxy;
	}
	public String readIpProxy() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "ProxyHost");
			sReturn = objEntry.getValue();
		}
		return sReturn;		
	}
	
	public String getPortProxy() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sPortProxy)){
			String sPort = this.readPortProxy();
			this.sPortProxy = sPort;
		}
		return this.sPortProxy;
	}
	public String readPortProxy() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			IKernelZZZ objKernel = this.getKernelObject();
			String sModule = this.getModuleName();
			String sProgram = this.getProgramName();
			IKernelConfigSectionEntryZZZ objEntry = objKernel.getParameterByProgramAlias(sModule, sProgram, "ProxyPort");
			sReturn = objEntry.getValue();
		}
		return sReturn;		
	}
	
	public void setIpExternal(String sIPExternal) {
		this.sIPExternal = sIPExternal;
	}
	public String getIpExternal() throws ExceptionZZZ{
		if(StringZZZ.isEmpty(this.sIPExternal)){
			String sIP = this.readIpExternal();
			this.sIPExternal = sIP;
		}
		return this.sIPExternal;
	}
	
	
	//#### METHIDEN ###############################################	
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
	/** (non-Javadoc)
	@see zzzKernel.basic.KernelObjectZZZ#getFlag(java.lang.String)
	Flags used:<CR>
	-  isConnected
	- useProxy
	- haserror
	 */
	public boolean getFlag(String sFlagName){
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.getFlag(sFlagName);
			if(bFunction==true) break main;
							
			//getting the flags of this object
			String stemp = sFlagName.toLowerCase();
			if(stemp.equals("useproxy")){
				bFunction = bFlagUseProxy;
				break main;			
				/*
			}else if(stemp.equals("isconnected")){
				bFunction = bFlagIsConnected;
				break main;
			}else if(stemp.equals("haserror")){				
				bFunction = bFlagHasError;
				break main;
			}else if(stemp.equals("portscanallfinished")){				
				bFunction = bFlagPortScanAllFinished;
				break main; 
				*/
			}
		}//end main:
		return bFunction;
	}
	
	


	/**
	 * @see AbstractKernelUseObjectZZZ.basic.KernelUseObjectZZZ#setFlag(java.lang.String, boolean)
	 * @param sFlagName
	 * Flags used:<CR>
	 * - isconnected
	 * - useproxy
	 * - haserror
	 * - PortScanAllFinished //das ist zusammen mit "isconnected" das Zeichen f�r den ConnectionMonitor des Frontends, das er starten darf. Grund: Die PortScans f�hren ggf. zu timeouts.
	 * @throws ExceptionZZZ 
	 */
	public boolean setFlag(String sFlagName, boolean bFlagValue) throws ExceptionZZZ{
		boolean bFunction = false;
		main:{
			if(StringZZZ.isEmpty(sFlagName)) break main;
			bFunction = super.setFlag(sFlagName, bFlagValue);
		if(bFunction==true) break main;
	
		//setting the flags of this object
		String stemp = sFlagName.toLowerCase();
		if(stemp.equals("useproxy")){
			bFlagUseProxy = bFlagValue;
			bFunction = true;			
			break main;
			/*
		}else if(stemp.equals("isconnected")){
			bFlagIsConnected = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("haserror")){
			bFlagHasError = bFlagValue;
			bFunction = true;
			break main;
		}else if(stemp.equals("portscanallfinished")){
			bFlagPortScanAllFinished = bFlagValue;
			bFunction = true;
			break main;
			*/
		}
		}//end main:
		return bFunction;
	}
}

