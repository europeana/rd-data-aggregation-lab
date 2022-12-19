package europeana.rnd.dataprocessing.dates.publication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import europeana.rnd.dataprocessing.dates.DatesExtractorHandler;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.DatesInRecord.DatesFromSource;

public class ScriptNormalizeDatesTestForPublication {

	File folder;
	DatesExtractorForPublicationHandler handler;
	RawDataExporter dataExporter;
	
	public ScriptNormalizeDatesTestForPublication(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.handler = new DatesExtractorForPublicationHandler(outputFolder);
		dataExporter=new RawDataExporter(outputFolder);
//		this.handlerAgents = new DatesAgentHandler(outputFolder);
	}

	public void process() throws IOException {
		if(folder.getName().endsWith(".zip")) {
			try (FileInputStream fis = new FileInputStream(folder);
	                BufferedInputStream bis = new BufferedInputStream(fis);
	                ZipInputStream zis = new ZipInputStream(bis)) {
				ZipEntry ze;
	            while ((ze = zis.getNextEntry()) != null) {
	                if(ze.isDirectory()) continue;
					System.out.println(ze.getName());
					processFile(zis);
	            }
			}
		} else {
			for(File innerFolder: folder.listFiles()) {
				if(innerFolder.isFile()) continue;
				if(!innerFolder.getName().startsWith("dates_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
				}
			}
		}
		handler.close();
		dataExporter.close();
	}


	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			String choUri = jv.getString("id");
			DatesInRecord record=new DatesInRecord(jv);
			filterValuesForProviderDataOnly(record);
			try {
				dataExporter.export(record);
				handler.handle(record);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	private void filterValuesForProviderDataOnly(DatesInRecord record) {
		record.fromEuropeana=new DatesFromSource();
	}

	public static void main(String[] args) throws Exception {
//		String sourceFolder = "c://users/nfrei/desktop/data/dates";
//		String sourceFolder = "c://users/nfrei/desktop/data/dates/test-set";
		String sourceFolder = "c://users/nfrei/desktop/data/dates/test-set/dates_export.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File sourceFolderObj=new File(sourceFolder);
		File outFolder=sourceFolder.endsWith(".zip") ? new File(sourceFolderObj.getParentFile(), "extraction-paper") : new File(sourceFolder+"/extraction-paper");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeDatesTestForPublication processor=new ScriptNormalizeDatesTestForPublication(sourceFolderObj, outFolder);
		processor.process();
	}
}
