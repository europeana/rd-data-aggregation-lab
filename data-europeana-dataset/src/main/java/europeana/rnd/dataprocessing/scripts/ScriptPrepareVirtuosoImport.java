package europeana.rnd.dataprocessing.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import europeana.rnd.aggregated.FtpDownload;
import europeana.rnd.dataprocessing.EuropeanaDatasetProcessor;
import europeana.rnd.dataprocessing.EuropeanaDatasetProcessorHandler;
import inescid.dataaggregation.data.model.CreativeCommons;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;

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
		
		new ScriptPrepareVirtuosoImport(tmpFolder, outputFolder, Integer.parseInt(maxDatasets), Integer.parseInt(maxRecords))
		.download();
		
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
	