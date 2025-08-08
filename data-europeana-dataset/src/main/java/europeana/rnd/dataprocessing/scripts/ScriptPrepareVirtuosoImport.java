package europeana.rnd.dataprocessing.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;

import europeana.rnd.aggregated.FtpDownload;

public class ScriptPrepareVirtuosoImport extends FtpDownload {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String tmpFolder = null;
		String maxDatasets = "-1";
		String maxRecords ="-1";

		if (args != null && args.length == 4) {
			tmpFolder = args[0];
			outputFolder = args[1];
			maxDatasets = args[2];
			maxRecords = args[3];			
		} else if (args == null || args.length == 0) {
			tmpFolder = "C:\\Users\\nfrei\\Desktop\\data\\europeana-dataset-virtuoso";
			outputFolder = "C:\\Users\\nfrei\\Desktop\\data\\virtuoso-import";
			maxDatasets = "5";
			maxRecords = "100";
			
			File aux=new File(tmpFolder);
			if(!aux.exists())
				aux.mkdir();
			aux=new File(outputFolder);
			if(!aux.exists())
				aux.mkdir();
		}
		
//		new ScriptPrepareVirtuosoImport(tmpFolder, outputFolder, Integer.parseInt(maxDatasets), Integer.parseInt(maxRecords))
//		.download();
		new ScriptPrepareVirtuosoImport(tmpFolder, outputFolder, Integer.parseInt(maxDatasets), Integer.parseInt(maxRecords))
		.download(
				"783"
//				,"709"
				);
		
	}
	
	public ScriptPrepareVirtuosoImport(String tmpFolder, String outputFolder, int maxDatasets, int maxRecords) {
		super(tmpFolder, "TTL");
		enableDownloadAllFile();
		this.maxDatasets = maxDatasets;
		this.maxRecords = maxRecords;
		this.outputFolder= outputFolder;
	}

	String outputFolder;
	int fileCnt=0;
	int maxDatasets=100;
	int maxRecords=100;
	
	@Override
	protected boolean downloadFile(String tmpFolder, FTPFile f) throws IOException {
		File tmpFile=new File(tmpFolder, f.getName());
		if(!tmpFile.exists())
			super.downloadFile(tmpFolder, f);
		
		if (!f.getName().endsWith(".zip"))
			return true;
		
		String datasetId=f.getName().substring(0, f.getName().indexOf('.'));
		
		FileUtils.write(new File(outputFolder, datasetId+".ttl.graph"), "http://data.europeana.eu/dataset/"+datasetId, StandardCharsets.UTF_8);	
		File datasetTtlFile=new File(outputFolder, datasetId+".ttl.gz");
		if(datasetTtlFile.exists())
			datasetTtlFile.delete();
		
		FileOutputStream datasetTtlFileStream=new FileOutputStream(datasetTtlFile);
		GZIPOutputStream gzipDatasetTtlStream=new GZIPOutputStream(datasetTtlFileStream);
		Writer writer=new OutputStreamWriter(gzipDatasetTtlStream, StandardCharsets.UTF_8);
		
		
		
		boolean firstRecord=true;
		int recIndex=0;
//		final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fos.toByteArray()));
		final ZipInputStream zip = new ZipInputStream(new FileInputStream(tmpFile));
		ZipEntry entry = zip.getNextEntry();
		while (entry != null) {
			String edmRdf="";
			try {
				edmRdf=IOUtils.toString(zip, StandardCharsets.UTF_8);
//					if(normalize)
//						edmRdf=Normalizer.normalize(edmRdf, Form.NFC);
				
				String[] lines = edmRdf.split("\n");
				for(String line: lines) {
					if(firstRecord || !line.startsWith("@prefix")) {
						writer.write(line);
						writer.write("\n");
					}
				}
				firstRecord=false;
			} catch (Throwable e) {
				throw e;
			}
			zip.closeEntry();
			entry = zip.getNextEntry();
			recIndex++;
			if(recIndex>=maxRecords)
				break;
		}
		zip.close();
		
		writer.close();
		gzipDatasetTtlStream.close();
		datasetTtlFileStream.close();
		
		fileCnt++;
		if(fileCnt>=maxDatasets)
			return false;		
		return true;
	}
}
	