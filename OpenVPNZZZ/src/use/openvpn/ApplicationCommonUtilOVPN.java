package use.openvpn;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.IKernelZZZ;
import basic.zKernel.html.TagInputZZZ;
import basic.zKernel.html.TagTypeInputZZZ;
import basic.zKernel.html.reader.KernelReaderHtmlZZZ;
import basic.zKernel.net.client.KernelReaderPageZZZ;
import basic.zKernel.net.client.KernelReaderURLZZZ;

public class ApplicationCommonUtilOVPN {

	/**Reads the dynamic IP from a URL (uses a html-parser therefore).
	 * Checks the necessarity of enabling a proxy and will enable the proxy.
	 * The proxy has to be configured in the kernel-configuration-file.
	* @return String, the IP found.
	* @throws ExceptionZZZ 
	* 
	* lindhaueradmin; 13.07.2006 09:12:43
	 */
	public static String readIpURL(IKernelZZZ objKernel, String sURL) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			if(StringZZZ.isEmpty(sURL)) break main;
			
			String[] satemp = {"UseStream"};
			KernelReaderURLZZZ objReaderURL = new KernelReaderURLZZZ(objKernel, sURL,satemp, "");
			
			sReturn = ApplicationCommonUtilOVPN.readIpURL(objReaderURL);									
		}//END main
		return sReturn;
	}
	
	public static String readIpURL(IKernelZZZ objKernel, String sURL, String sProxyHost, String sProxyPort) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			if(StringZZZ.isEmpty(sURL)) break main;
			
			String[] satemp = {"UseStream"};
			KernelReaderURLZZZ objReaderURL = new KernelReaderURLZZZ(objKernel, sURL,satemp, "");
			if(!StringZZZ.isEmpty(sProxyHost)) {
				objReaderURL.setProxyEnabled(sProxyHost, sProxyPort);
			}
			
			sReturn = ApplicationCommonUtilOVPN.readIpURL(objReaderURL);			
		}//end main:
		return sReturn;		
	}
	
	public static String readIpURL(KernelReaderURLZZZ objReaderURL) throws ExceptionZZZ{
		String sReturn = null;
		main:{
			if(objReaderURL==null)break main;
			
			//+++ Nachdem nun ggf. der Proxy aktiviert wurde, die Web-Seite versuchen auszulesen				
			//+++ Den IP-Wert holen aus dem HTML-Code der konfigurierten URL
			KernelReaderPageZZZ objReaderPage = objReaderURL.getReaderPage();
			KernelReaderHtmlZZZ objReaderHTML = objReaderPage.getReaderHTML();
			 
			//Nun alle input-Elemente holen und nach dem Namen "IPNr" suchen.
			IKernelZZZ objKernel = objReaderURL.getKernelObject();
			TagTypeInputZZZ objTagTypeInput = new TagTypeInputZZZ(objKernel);			
			TagInputZZZ objTag = (TagInputZZZ) objReaderHTML.readTagFirstZZZ(objTagTypeInput, "IPNr");
			if(objTag==null)break main;
			
			sReturn = objTag.readValue();
		}//end main:
		return sReturn;
	}
}
