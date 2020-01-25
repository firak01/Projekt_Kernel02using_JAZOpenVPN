package use.openvpn.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import basic.zKernel.KernelZZZ;
import custom.zUtil.io.FileZZZ;
import use.openvpn.ConfigChooserZZZ;
import use.openvpn.ConfigFileZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zBasic.util.file.FileTextParserZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.KernelUseObjectZZZ;

public class ClientConfigUpdaterZZZ extends KernelUseObjectZZZ {
private ConfigChooserZZZ objConfigChooser = null;//Darin stehen die verwendeten Pfade zum Template Verzeichnis, Programverzeichnis, etc. 
private File objFileTemplate=null;
private File objFileUsed = null;
private HashMap hmLine = null;

//private FileInputStream filein=null;
// Die Properties erf�llen nicht meine Erwartungen           private Properties objProp = null;
private FileTextParserZZZ objParser = null;

	public ClientConfigUpdaterZZZ(IKernelZZZ objKernel, ConfigChooserZZZ objConfigChooser, HashMap hmLine, String[] saFlagControl) throws ExceptionZZZ{
		super(objKernel);
		ConfigUpdaterNew_(objConfigChooser, hmLine, saFlagControl);
	}
	
	private void ConfigUpdaterNew_(ConfigChooserZZZ objConfigChooser, HashMap hmLine, String[] saFlagControl) throws ExceptionZZZ{
		main:{
			
			//try{		
			check:{
		 		
				if(saFlagControl != null){
					String stemp; boolean btemp;
					for(int iCount = 0;iCount<=saFlagControl.length-1;iCount++){
						stemp = saFlagControl[iCount];
						btemp = setFlag(stemp, true);
						if(btemp==false){ 								   
							   ExceptionZZZ ez = new ExceptionZZZ(stemp, iERROR_FLAG_UNAVAILABLE, this, ReflectCodeZZZ.getMethodCurrentName()); 							  
							   throw ez;		 
						}
					}
					if(this.getFlag("init")) break main;
				}
				
				if(objConfigChooser==null) {
					ExceptionZZZ ez = new ExceptionZZZ("ConfigChooser-Object containing the paths of the new file.", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
				this.setConfigChooserObject(objConfigChooser);
				
				if(hmLine==null){
					ExceptionZZZ ez = new ExceptionZZZ("HashMap containing the updated lines.", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}else if(hmLine.isEmpty()){
					ExceptionZZZ ez = new ExceptionZZZ("HashMap containing the updated lines.", iERROR_PARAMETER_EMPTY, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}
			}//End check
			
			this.setHashMapLine(hmLine);	
			
			//PROPRITES HABEN DIE ERWARTUNGEN NICHT ERF�LLT
			//this.filein = new FileInputStream(this.getFileTemplate());
			//this.objProp = new Properties();
			//objProp.load(filein);
			
			/*
		}catch (FileNotFoundException e){
			ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE+e.getMessage(), iERROR_PARAMETER_VALUE, this, ReflectionZZZ.getMethodCurrentName(), "");
			throw ez;			
		}catch(IOException e){
			ExceptionZZZ ez = new ExceptionZZZ(sERROR_PARAMETER_VALUE+e.getMessage(), iERROR_PARAMETER_VALUE, this, ReflectionZZZ.getMethodCurrentName(), "");
			throw ez;	
			}
			*/
		}//END main
	}
	
	/**Set the template - file. This will enable a new internal parser-object.
	 * @param objFileTemplatein
	 * @throws ExceptionZZZ, 
	 *
	 * @return File, the new file which will not have the "Template" in its name.
	 *
	 * javadoc created by: 0823, 06.07.2006 - 09:35:42
	 */
	public File refreshFileUsed(File objFileTemplatein) throws ExceptionZZZ{
File objReturn = null;
main:{
	File objFileTemplate=null;
	check:{
		if(objFileTemplatein==null){
			objFileTemplate = this.getFileTemplate();
			if(objFileTemplate==null){
				ExceptionZZZ ez = new ExceptionZZZ("File Template", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
		}else{
			objFileTemplate = objFileTemplatein;
		}			
		if(objFileTemplate.isDirectory()==true){
			ExceptionZZZ ez = new ExceptionZZZ("File Template was expected to be a file, not a directory", iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
			throw ez;
		}
	}//END check
	
	//+++ DAS TEMPLATE FILE WECHSELN
	this.setFileTemplate(objFileTemplate);
	
	//+++ DEN INTERNEN PARSER AKTIVIEREN, BASIEREND AUF DEM NEUEN TEMPLATE FILE
	FileTextParserZZZ objParser = new FileTextParserZZZ(this.getFileTemplate(), (String[])null);
	this.objParser = objParser;

	//+++ DIE NEUE ZIELDATEI DEFINIEREN
	//Den "Template"-Anfang aus dem Dateinamen entfernen.
	String sName = objFileTemplate.getName();
	if(sName.toLowerCase().startsWith(ConfigFileZZZ.sFILE_TEMPLATE_PREFIX)){
		//TODO GOON: Methode entwickeln, welche unabh�ngig von Gro�-/Kleinschreibung arbeitet
		sName = StringZZZ.right(sName, ConfigFileZZZ.sFILE_TEMPLATE_PREFIX);
		sName = sName.trim();
	}
	
	//TODO GOON: 20200121: Das gilt nicht mehr. Nun ist das Verzeichnis für die neue Datei ein anderes. 
	//          Nämlich das Programmverzeichnis, der .exe.
	//Das bekommt man über das ConfigChooserZZZ-Objekt
	//TODO GOON: Das Objekt übergeben....
	// Die neuen Dateien befinden sich im gleichen Verzeichnis wie die Template-Dateien
	//File objFileDir = objFileTemplate.getParentFile();   11111111111111
	File objFileDir = this.getConfigChooserObject().getDirectoryConfig();			
	String sPath = objFileDir.getPath();
	
	// Ggf. wurden die alten Dateien nicht gel�scht, dann das Feature der "Dateinummerierung" verwenden.
	FileZZZ objFileExpander = new FileZZZ(sPath, sName, (String[])null);	
	String stemp = objFileExpander.PathNameTotalExpandedNextCompute();
	
	objReturn = new File(stemp);
	
}//END main:
		this.setFileUsed(objReturn);
		return objReturn;
	}
	
	/**This creates new configuration file and changes some entries:
	 * - remote IP-Adress
	 * - Proxy enabled/disabled
	 * - cert
	 * - key
	 * 
	* @return boolean
	* @param objFileNewin
	* @param sIP
	* @param bReplaceOnlyExisting, false=no new lines will be added, true=add new lines
	* Remark: Normally it should be false,
	* because a new line might can add a bug to this configuration.
	* E.g. If you have a configuration, which uses 'proto udp',
	* a line which wants to enable a http-proxy would be wrong.
	* 
	* @return
	* @throws ExceptionZZZ 
	* 
	* lindhaueradmin; 04.07.2006 10:21:39
	 */
	public boolean update(File objFileNewin, boolean bReplaceOnlyExisting) throws ExceptionZZZ{
		boolean bReturn = false;
		main:{	
			File objFileNew=null;
			try{
			check:{				
				if(objFileNewin==null){
					objFileNew = this.getFileUsed();
					if (objFileNew==null){
						ExceptionZZZ ez = new ExceptionZZZ("FileUsed", iERROR_PARAMETER_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
				}else{
					objFileNew = objFileNewin;
				}
				
				if(this.hmLine==null){
					ExceptionZZZ ez = new ExceptionZZZ("HashMapLine", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
					throw ez;
				}else if(this.hmLine.isEmpty()){
					//HIER KEINE FEHLERMELDUNG AUSGEBEN, SONDERN NUR MIT false BEENDEN LASSEN
					break main;
				}				
			}//END check
		
		
			//+++++++++++++++++++++++++++++++++++++
			//+++ Array aller zu setzenden Konfigurations-Werte
			Set objSet = hmLine.keySet();			
			String[] saConfig = new String[hmLine.size()];
			objSet.toArray(saConfig);
			
			//Die nicht gesetzten Werte sollen entfernt werden. 
			//Dazu erst einmal ein Array aller "setzbaren" - werte holen
			//Die dann gesetzten werte werden daraus entfernt.
			//Was �brig bleibt wird durch den Parser gel�scht.
			//TODO GOON: Methode aus einer HashMap eine KEyArray-List zu machen.
			HashMap hmAll = ClientConfigUpdaterZZZ.getConfigPattern();			
			Set objSet2 = hmAll.keySet();
			String[] saConfig2Remove = new String[hmAll.size()];
			objSet2.toArray(saConfig2Remove);
			
			ArrayList listaConfig2Remove = new ArrayList();
			for(int icount=0; icount < saConfig2Remove.length; icount++){
				listaConfig2Remove.add(saConfig2Remove[icount]);
			}
			
			
				//System.out.println(objProp.getProperty("remoterrr")); //Merke: Nicht vorhanden Properties auslesen ergibt null.

			//+++++++++++++++++++++++++++++++++++++++++++
			//+++ Die neuen Konfigurationsdateienn erstellen
			
			
				/* DIE PROPERTIES SIND ZWAR GUT ZUM AUSLESEN, ABER SCHLECHT BEIM SCHREIBEN
				String sRemote = this.objProp.getProperty("remote");
				if(sRemote!=null){
					this.objProp.setProperty("remote", sIP);
					bReturn = true;
				}
				*/
				for(int icount=0; icount<saConfig.length;icount++){
					String sConfig = saConfig[icount];
					String sExp = ClientConfigUpdaterZZZ.getConfigRegExp(sConfig);					
					if(sExp==null){
						ExceptionZZZ ez = new ExceptionZZZ("No regular expression available for the configuration '"+ sConfig + "'", iERROR_PROPERTY_MISSING, this, ReflectCodeZZZ.getMethodCurrentName());
						throw ez;
					}
					
					//Die NEUE Zeile haben wir ja schon im Objekt, als Wert der Hashmap
					String sLine = (String)hmLine.get(sConfig);
						
					//Die ALTE Zeile findet der interne Parser in der f�r ihn aktuellen Datei. 
					//Merke: Beim ersten Wert ist die aktuelle Datei die Template Datei.
					//           Bei allen Folgewerten ist die aktuelle Datei aber schon die Datei, in die beim ersten Wert geschrieben worden ist.
					//Merke2: Bei der Ersetzung mit dieser Parserklasse, basierend auf dem regul�ren Ausdruck, werden automatisch nur die Werte geschrieben, die schon vorhanden sind.
					org.apache.regexp.RE objRe = new org.apache.regexp.RE(sExp);	
					if(bReplaceOnlyExisting == true){
						    //FALL: ZEILE IN DER KONFIGURATION ERSETZEN																			
							int iLine=this.objParser.replaceLine(objFileNew, objRe, sLine);	
							if(iLine >= 1){
								//Diese Zeile nun anschliessen nicht mehr entfernen
								listaConfig2Remove.remove(saConfig[icount]);
							}
					}else{
							//Hier ggf. fehlende Eintr�ge anh�ngen
							//Ein einfaches Ersetzen geht nicht, (s. Merke2). Man mus nun vorher pr�fen, ob die Zeile vorhanden ist.
							boolean bExists = this.objParser.hasLine(objRe);
							if(bExists==true){
								//FALL: ZEILE IN DER KONFIGURATION ERSETZEN
								int iLine=this.objParser.replaceLine(objFileNew, objRe, sLine);	
								if(iLine >= 1){
									//Diese Zeile nun anschliessen nicht mehr entfernen
									listaConfig2Remove.remove(saConfig[icount]);
								}
							}else{
								//FALL: IM TEMPLATE NICHT EXISTIERENDE ZEILE EINTRAGEN. Z.B. PROXY-KONFIGURATION
								long lLine = this.objParser.appendLine(objFileNew, sLine);
								if(lLine >= 1){
									//Diese Zeile nun anschliessen nicht mehr entfernen
									listaConfig2Remove.remove(saConfig[icount]);
								}
							}
							
						}
						
						/* DAS L�SCHT ALLE KOMMENTARZEILEN UND SETZT GLEICHHEITSZEICHEN ZWISCHEN KONFIGURATIONS- UND WERTEEINTRAG
						this.objProp.setProperty("remote", sIP);
						bReturn = true;
						*/			
					}		//END for			
				
		
                //ALLE ANDEREN ZEILEN L�SCHEN, Falls in dem KonfigurationsTemplate z.B. ein Proxy konfiguriert ist, aber keine Proxy-Zeile gesetzt werden soll
				for(int icount=0; icount < listaConfig2Remove.size(); icount++){
					String stemp = (String)listaConfig2Remove.get(icount);
					String sConfig = ClientConfigUpdaterZZZ.getConfigRegExp(stemp);
					org.apache.regexp.RE objRe = new org.apache.regexp.RE(sConfig);
				    this.objParser.removeLine(objFileNew, objRe);
				   // if(itemp>= 1) bReturn = true; //Nicht mehr auf false zur�cksezten. Sobald etwas ersetzt wurde, bleibt der Returnwert auf true stehen.
				}//END for
			
				bReturn = true;
			} catch (org.apache.regexp.RESyntaxException e) {
				ExceptionZZZ ez = new ExceptionZZZ("Regular Expression: "+e.getMessage(), iERROR_PARAMETER_VALUE, this, ReflectCodeZZZ.getMethodCurrentName());
				throw ez;
			}
	
		}//End main
		return bReturn;
	}
	

/**TODO R�ckgabe des regul�ren Ausdrucks. TODOGOON: Dies sollte in Form einer HashMap passieren !!!
 *  TODO GOON Hashmap in der Form liste(ConfigAusdruck) = "^" + saConfig[icount] + " ";
 * @param sAlias
 * @return
 * @throws ExceptionZZZ, 
 *
 * @return String[]
 *
 * javadoc created by: 0823, 05.07.2006 - 08:31:35
 */
public static String getConfigRegExp(String sConfiguration) throws ExceptionZZZ{
	String sReturn = null;
	main:{		
		check:{
			if(sConfiguration==null)break main;
			if(sConfiguration.equals(""))break main;				
		}
	
		//Hashmap erstellen. TODO GOON Dies an eine Stelle auslagern, so dass es nur einmal gemacht werden braucht.
	HashMap hmConfig = new HashMap();
	hmConfig.put("remote", "^remote ");
	hmConfig.put("http-proxy", "^http-proxy ");
	hmConfig.put("http-proxy-timeout", "^http-proxy-timeout ");
	
		//20200123: Key und certifier Datei mit dem Namen der Hostmaschine
	hmConfig.put("cert", "^cert ");
	hmConfig.put("key", "^key ");
	
	//Hashmap auslesen
	sReturn = (String)hmConfig.get(sConfiguration);
	
	}//END main
	return sReturn;
	
}


/**TODO R�ckagebe der einzutragenden Zeile pro configurations Eintrag ALS MUSTER. TODO GOON: R�ckgabe in Form einer HashMap
 * TODO GOON: Hashmap hat folgende Struktur. Liste(sConfigurationEntry)=sConfiigurationEntry + ' ' + die Werte ....
 * @param sAlias
 * @param sIP
 * @param sProxyHost
 * @param sProxyPort
 * @param iProxyTimeout
 * @return, 
 *
 * @return String[]
 * 
 * Die Ersetzung der Musterplatzhalter passiert in ClientMainZZZ.readTaskHashMap();
 * Zudem muss ein RegEx Ausdruck bereitgestellt werden in ClientConfigUpdaterZZZ.getConfigRegExp();
 *
 * javadoc created by: 0823, 05.07.2006 - 08:34:38
 */
public static HashMap getConfigPattern(){
	HashMap hmReturn = new HashMap();
	main:{
		check:{		
		}
	
		//Die %xyz% Eintr�ge sollen dann ersetzt werden.
		hmReturn.put("remote", "remote %ip%");
		hmReturn.put("http-proxy", "http-proxy %proxy% %port%");
		hmReturn.put("http-proxy-timeout", "http-proxy-timeout %timeout%");
		
		//20200123: Nun die verwendeten Key-Namen erstetzen
		hmReturn.put("cert", "cert %filecertifier%");
		hmReturn.put("key", "key %filekey%");
	}//END main
	return hmReturn;
}
	
	
	//############# Getter / Setter
	public ConfigChooserZZZ getConfigChooserObject() {
		return this.objConfigChooser;
	}
	public void setConfigChooserObject(ConfigChooserZZZ objConfigChooser) {
		this.objConfigChooser = objConfigChooser;
	}
	public File getFileTemplate(){
		return this.objFileTemplate;
	}
	public void setFileTemplate(File objFile){
		this.objFileTemplate= objFile;
	}
	
	public File getFileUsed(){
		return this.objFileUsed;
	}
	public void setFileUsed(File objFile){
		this.objFileUsed = objFile;
	}
	public void setHashMapLine(HashMap hmLine){
		this.hmLine = hmLine;
	}
	public HashMap getHashMapLine(){
		return this.hmLine;
	}
	
	
}
