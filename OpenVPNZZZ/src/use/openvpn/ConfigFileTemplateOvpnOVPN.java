package use.openvpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;


import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.AbstractKernelUseObjectZZZ;
import basic.zKernel.net.client.KernelPingHostZZZ;
import basic.zKernel.KernelZZZ;
import basic.zKernel.flag.IFlagZEnabledZZZ;

public class ConfigFileTemplateOvpnOVPN extends AbstractKernelUseObjectZZZ{
	public static String sFILE_TEMPLATE_PREFIX="template_";
	public static String sFILE_TEMPLATE_SUFFIX="";
	private File objFileConfig;
	private Properties objProperties = new Properties();
	private String sRemotePort=null;
	private String sTargetIP=null;
	private String sLocalIP = null;
	
	public ConfigFileTemplateOvpnOVPN(IKernelZZZ objKernel, File objFile, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigFileReaderNew_(objFile, saFlagControl);		
	}

	
	private void ConfigFileReaderNew_(File objFile, String[] saFlagControl) throws ExceptionZZZ{
		main:{
			
			try{		
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ(stemp, IFlagZEnabledZZZ.iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							  
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}
				
				
				if(objFile==null){
					ExceptionZZZ ez = new ExceptionZZZ("File", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}else{
					this.setFileConfig(objFile);
				}
			}//End check
			
			//Properties des files einlesen
			FileInputStream fin = new FileInputStream(objFile);
			this.getProperties().load(fin);
			}catch(IOException e){
				ExceptionZZZ ez = new ExceptionZZZ("File throws IOException: '" + e.getMessage() + "'", iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
			
	
		}//END main
	}
	
	
	public static String readCommandAssociatedRun() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			AssociationService service = new AssociationService();
			Association ass = service.getFileExtensionAssociation(".ovpn");
			if (ass==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "No filetype assoziated with: .ovpn", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else{
				//objKernel.getLogObject().WriteLineDate("Filetype .ovpn found. It seems that Open VPN is installed.");				
			}
			
			/*Nur zum Test: Ausgeben aller zur Verfuegung stehenden Actions
			List lAction = ass.getActionList();
			if(lAction.isEmpty()==true){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Action assoziated with: .ovpn", iERROR_PARAMETER_VALUE, ReflectionZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			Iterator objIterator = lAction.iterator();
			while(objIterator.hasNext()){
				Action objAction =  (Action)objIterator.next();
				System.out.print(objAction.getVerb() + "\t" + objAction.getDescription() + "\t" + objAction.getCommand() + "\n");
			}
			*/			
			/* Das Ergebnis: Liste aller Actions
			 * open	null	notepad.exe "%1"
			 * run	Start OpenVPN on this config file	"C:\Programme\OpenVPN\bin\openvpn.exe" --pause-exit --config "%1"
			 */
			
			//Aber es gibt ja viel tollere Methoden
			Action objAction = ass.getActionByVerb("run");
			if(objAction==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Action assoziated with: .ovpn, does have no 'verb=run'",iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			//Aus dieser Commandozeile den Pfad zur ausfuehrbaren Datei auslesen
			sReturn = objAction.getCommand();
		}//END main:
		return sReturn;
	}
	
	public static File findFileExe() throws ExceptionZZZ{
		File objReturn = null;
		main:{
			String sCommand = ConfigFileTemplateOvpnOVPN.readCommandAssociatedRun();
			int ileft = sCommand.indexOf("\"");
			int iright = sCommand.indexOf("\"", ileft+1);			
			String sFile = sCommand.substring(ileft+1, iright);
			if(sFile==null){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Action assoziated with: .ovpn, 'verb=run', command-line, 'No file found: null", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}else if(sFile.equals("")){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Action assoziated with: .ovpn, 'verb=run', command-line, 'No file found: empty string", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
			
			objReturn = new File(sFile);
			if(objReturn.isFile()==false){
				ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE + "Action assoziated with: .ovpn, 'verb=run', command-line, this is not a file !!!", iERROR_PARAMETER_VALUE, ReflectCodeZZZ.getMethodCurrentName(), "");
				throw ez;
			}
		}
		return objReturn;
	}
	
	public static String readCommandParameter() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			File objFileExe = ConfigFileTemplateOvpnOVPN.findFileExe();
			String sCommand = ConfigFileTemplateOvpnOVPN.readCommandAssociatedRun();
			
			//Nun den Executable - Part incl. Anfuehrungszeichen entfernen
			String sFile = "\"" + objFileExe.getPath() + "\"";
			sReturn = StringZZZ.replace(sCommand, sFile, "");
			sReturn = sReturn.trim();
		}
		return sReturn;
	}
	
	public static boolean isTemplate(String sFilename) {
		boolean bReturn = false;
		main:{
			//Template Dateinamen fangen mit dem vorangesetzten String an.
			if(sFilename.toLowerCase().startsWith(ConfigFileTemplateOvpnOVPN.sFILE_TEMPLATE_PREFIX)) bReturn = true;
		}
		return bReturn;
	}
	
	public String getVpnPortRemote(){
		if(this.sRemotePort==null){
			this.sRemotePort = this.readVpnPortRemote();
		}
		return this.sRemotePort;
	}
	
	public String readVpnPortRemote(){
		String sReturn = null;
		main:{
			sReturn = this.getProperties().getProperty("port");
		}
		return sReturn;
	}
	
	public String getVpnIpRemote() throws ExceptionZZZ{
		if(this.sTargetIP==null){
			this.sTargetIP = this.readVpnIpRemote();
		}
		return this.sTargetIP;
	}
	
	public String getVpnIpLocal() throws ExceptionZZZ{
		if(this.sLocalIP==null){
			this.sLocalIP = this.readVpnIpLocal();
		}
		return this.sLocalIP;
	}
	
	/**Reads the ip-adress of the other vpn-tunnel-end.
	 * @return String
	 *
	 * javadoc created by: 0823, 18.07.2006 - 09:24:22
	 * @throws ExceptionZZZ 
	 */
	public String readVpnIpRemote() throws ExceptionZZZ{
		String sReturn=null;
		main:{
			String sLine = this.getProperties().getProperty("ifconfig");
			sReturn = StringZZZ.word(sLine, " ", 2);			
		}//END main:
		return sReturn;
	}
	
	/**Reads the ip-adress of the local vpn-tunnel-end.
	 * @return String
	 *
	 * javadoc created by: 0823, 18.07.2006 - 09:24:22
	 * @throws ExceptionZZZ 
	 */
	public String readVpnIpLocal() throws ExceptionZZZ{
		String sReturn = null;
		main:{
			String sLine = this.getProperties().getProperty("ifconfig");
			sReturn = StringZZZ.word(sLine, " ", 1);			
		}//END main:
		return sReturn;
	}
	
	
	/**True, if the vpn-ip adress is reachable.
	 *If the Port-Parameter is left empty, the remote-port will be used, but this does not necessarily mean that THIS starter - object has started the openvpn.exe which has successfully worked.
	 * !!! Till now it is not possible to find out what configuration was successful !!!
	 * 
	 * @throws ExceptionZZZ, 
	 *
	 * @return boolean
	 *
	 * javadoc created by: 0823, 11.07.2006 - 17:40:58
	 */
	public boolean isVpnReachable(String sTargetPortIn) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{
			bReturn = this.isVpnReachable(null, sTargetPortIn);
		}//END main:
		return bReturn;
	}
	
	public boolean isVpnReachable(String sTargetIPIn, String sTargetPortIn) throws ExceptionZZZ{
		boolean bReturn =  false;
		main:{
			String sTargetIP = "";
			String sTargetPort = "";
			check:{
				if(StringZZZ.isEmpty(sTargetIPIn)){
					sTargetIP = this.getVpnIpRemote();
				}else{
					sTargetIP = sTargetIPIn;
				}
			
				if(StringZZZ.isEmpty(sTargetPortIn)){
					sTargetPort = this.getVpnPortRemote(); //Die Idee dahinter ist, das der remote Port zumindest erreichbar sein sollte. Theoretisch kann aber auch z.B. fix der Port 80 verwendet werden.
				}else{
					sTargetPort = sTargetPortIn;
				}
			}//END check
			
			if(sTargetIP!=null && sTargetPort != null){
				KernelPingHostZZZ objPing = new KernelPingHostZZZ(objKernel, null);
				try{
					//MERKE: HIER WIRD ZWAR EIN PORT ANGEGBEN, DIE VERBINDUNG ZUR GLEICHEN IP-ADRESSE KANN ABER AUCH �BER EINE ANDERE VPN-VERBINDUNG HERGESTELLT WORDEN SEIN.
					//            Es ist aber davon auszugehen, dass dieser Port zumindest frei ist. 
					bReturn = objPing.ping(sTargetIP, sTargetPort);
				}catch(ExceptionZZZ ez){
					//Keine Konsequenzz. Ich erwarte ja einen Fehler.
					//MERKE: Die Exception, welche die Methode throwen könnte kommt vom Konstruktor des KernelPingHost-Objekts.
					bReturn = false;
				}
			}
			
		}//END main
		return bReturn;
	}
	
	
	//#### GETTER / SETTER
	public void setFileConfig(File objFile){
		this.objFileConfig = objFile;
	}
	public File getFileConfig(){
		return this.objFileConfig;
	}
	public Properties getProperties(){
		return this.objProperties;
	}
}//END class
