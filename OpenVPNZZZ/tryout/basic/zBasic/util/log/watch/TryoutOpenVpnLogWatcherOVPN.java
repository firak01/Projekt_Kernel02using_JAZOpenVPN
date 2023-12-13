package basic.zBasic.util.log.watch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.util.file.FileEasyZZZ;
import use.openvpn.server.TryoutOpenVpnStarterOVPN;

public class TryoutOpenVpnLogWatcherOVPN {

	public static void main(String[] args) {
		TryoutOpenVpnLogWatcherOVPN objWatch = new TryoutOpenVpnLogWatcherOVPN();
		objWatch.startServerProcessLogWacher();
	}
	
	/** Das klappt... man kann das LogFile auslesen,
	 *  welches immer weiter neu vom OVPN-Server gefüllt wird.
	 * @return
	 * @author Fritz Lindhauer, 10.12.2023, 16:04:55
	 */
	public boolean startServerProcessLogWacher() {
		boolean bReturn= false;
		main:{			
			BufferedReader br=null;
			try {
				String sLogDirectory =  "c:\\fglkernel\\kernellog\\ovpnServer";
				String sLogFile = "ovpn.log";
				String sLogFilePathTotal =	FileEasyZZZ.joinFilePathName(sLogDirectory, sLogFile);		
				File objFileLog = new File(sLogFilePathTotal);
								
				boolean bExists = false;
				do {
					bExists = FileEasyZZZ.exists(objFileLog);
					if(!bExists) {
						Thread.sleep(5000);
					}
				}while(!bExists);
				
				String sLine = null;
				InputStream objStream = new FileInputStream(objFileLog);
				br = new BufferedReader(new InputStreamReader(objStream));
                while (true){
                    sLine = br.readLine();
                    if(sLine!=null)
                    {
                        System.out.println(sLine);
                    }else{
                        Thread.sleep(100);
                    }
                }
			} catch (ExceptionZZZ e) {				
				e.printStackTrace();				
				
			} catch (InterruptedException e) {				
				e.printStackTrace();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(br!=null) {
					IOUtils.closeQuietly(br);
				}
	        }
		}//end main:
		return bReturn;
	}

}
