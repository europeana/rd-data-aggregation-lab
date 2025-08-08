package europeana.rnd.dataprocessing.pid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import apiclient.google.GoogleApi;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptCheckDuplicatesInDump {
	Set<String> processedChos=new UnifiedSet<>();
	Set<String> datasetsWithDuplicates=new UnifiedSet<>();
	int dupsCount=0;
	
	File folder;

	public ScriptCheckDuplicatesInDump(File folder) {
		super();
		this.folder = folder;
	}

	public void process() throws IOException {
		boolean TESTING=false;
		
		int fileCnt=0;
		if(folder.getName().endsWith(".zip")) {
			try (FileInputStream fis = new FileInputStream(folder);
	                BufferedInputStream bis = new BufferedInputStream(fis);
	                ZipInputStream zis = new ZipInputStream(bis)) {
				ZipEntry ze;
	            while ((ze = zis.getNextEntry()) != null) {
	                if(ze.isDirectory()) continue;
	                if(ze.getName().endsWith("00.json")) {
	                	  System.out.print("Used memory (bytes): " + 
	                			  (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()));
	                	System.out.println(" -- "+ze.getName());
	                }
					processFile(zis);
					fileCnt++;
					if(TESTING && fileCnt>5000)
						break;
	            }
			}
		} else {
			for(File innerFolder: folder.listFiles()) {
				if(innerFolder.isFile()) continue;
				if(!innerFolder.getName().startsWith("ids_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
					fileCnt++;
					if(TESTING && fileCnt>5000)
						break;
				}
			}
		}
		
		System.out.println("datasets with duplicates");
		System.out.println(datasetsWithDuplicates);
		System.out.println("Dup records: "+dupsCount);
		System.out.println("Finished");
	}

	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		String prevDataset="";
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			IdsInRecord record=new IdsInRecord(jv);
			String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(record.choUri);
			if(!dataset.equals(prevDataset)) {
				processedChos.clear();
				prevDataset=dataset;
			}
			if(processedChos.contains(record.choUri)) {
				datasetsWithDuplicates.add(dataset);
				dupsCount++;
			}else
				processedChos.add(record.choUri);
		}
	}
	
	public static void main(String[] args) throws Exception {
//		String sourceFolderStr = "c://users/nfrei/desktop/data/pid";
		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids_export_20230227.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptCheckDuplicatesInDump processor=new ScriptCheckDuplicatesInDump(sourceFolder);
		processor.process();
	}
	
	
}
