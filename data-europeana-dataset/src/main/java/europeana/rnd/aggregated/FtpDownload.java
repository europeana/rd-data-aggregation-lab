package europeana.rnd.aggregated;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpDownload {
	String outputFolder = null;
	String fileFormat = "XML";//XML|TTL

	String server = "download.europeana.eu";
    int port = 21;
    String user = "anonymous";
    String pass = "";
    protected FTPClient ftpClient ;
    protected String pathname;
    boolean downloadAll=false;
    
    public FtpDownload(String outputFolder, String fileFormat) {
		super();
		this.outputFolder = outputFolder;
		this.fileFormat = fileFormat;
		pathname = "/dataset/"+fileFormat;
	}

	protected static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }
	
	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String fileFormat = "XML";//XML|TTL

		if (args != null && args.length >= 1) {
				outputFolder = args[0];
				if (args.length >= 2) 
					fileFormat = args[1];
		}else {
			outputFolder = "c://users/nfrei/desktop/data/europeana_dataset";
		}

		FtpDownload ftpDown=new FtpDownload(outputFolder, fileFormat);
		ftpDown.download();
				
				
	}

	public void enableDownloadAllFile() {
		downloadAll=true;
	}
	
	public void download() {
		ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Operation failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = ftpClient.login(user, pass);
            showServerReply(ftpClient);
            if (!success) {
                System.out.println("Could not login to the server");
                return;
            } else {
                System.out.println("LOGGED IN SERVER");
            }
            
            String pathname = "/dataset/"+fileFormat;
			ftpClient.changeWorkingDirectory(pathname);
            showServerReply(ftpClient);
//            FTP.
            
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            FTPFile[] listFiles = ftpClient.listFiles();
            showServerReply(ftpClient);
            for(FTPFile f: listFiles) {
            	if(downloadAll || f.getName().endsWith(".zip")) {
            		if(!downloadFile(outputFolder, f))
            			break;
            	}
            }
    		System.out.println("downloaded "+listFiles.length+" files");
            
        } catch (IOException ex) {
            System.out.println("Oops! Something wrong happened");
            ex.printStackTrace();
        }
	}
	
	protected boolean downloadFile(String outputFolder, FTPFile f) throws IOException {
		System.out.println("downloading "+f.getName());
		
//		InputStream is = ftpClient.retrieveFileStream("/dataset/TTL/"+f.getName());
		FileOutputStream fos=new FileOutputStream(new File(outputFolder, f.getName()));
		while (!ftpClient.retrieveFile(pathname+"/"+f.getName(), fos)) {
			System.out.println( "Failed to download "+ pathname+"/"+f.getName()+" - Retrying...");
		}
//		IOUtils.copy(is, fos);
//		is.close();
		fos.close();
		return true;

	}
}
