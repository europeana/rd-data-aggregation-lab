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
import europeana.rnd.dataprocessing.ExampleInResource;
import europeana.rnd.dataprocessing.Examples;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.datastruct.BigMap;
import inescid.util.datastruct.BigSet;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptCheckDuplicatePids {
	
	public class RepeatedPidStats {
		MapOfInts<PidType> byType=new MapOfInts<PidType>();
		Map<PidType, Examples<ExampleInResource>> examples=new HashMap<PidType, Examples<ExampleInResource>>();

		MapOfInts<Integer> repetitionsStats=new MapOfInts<Integer>();
		Map<Integer, Examples<ExampleInResource>> repetitionExamples=new HashMap<Integer, Examples<ExampleInResource>>();
		
		public void calculate() {
			for(Entry<String, ArrayList<String>> entry: pidToDuplicateChos.entrySet()) {
				String pid = entry.getKey();
				PidType pidType = PidType.getPidType(pid);
				byType.incrementTo(pidType);
				
				Examples typeExamples = examples.get(pidType); 
				if(typeExamples==null) {
					typeExamples=new Examples(10);
					examples.put(pidType, typeExamples);
				}
				StringBuilder idsStr=new StringBuilder();
				ArrayList<String> choUris = entry.getValue();
				int cnt=0;
				for(String choUri: choUris) {
					idsStr.append(choUri).append(" ; ");
					cnt++;
					if(cnt==5) break;
				}
				typeExamples.add(new ExampleInResource(pid, idsStr.toString()));

				repetitionsStats.incrementTo(choUris.size());
				Examples<ExampleInResource> repExamples = repetitionExamples.get(choUris.size());
				if(repExamples==null) {
					repExamples=new Examples(10);
					repetitionExamples.put(choUris.size(), repExamples);
				}
				repExamples.add(new ExampleInResource(pid, idsStr.toString()));
			}
		}
	}
	
	Set<String> processedChos=new UnifiedSet<>();
	BigMap<String, String> pidToChoMap=null;
	
	MapOfLists<String, String> pidToDuplicateChos=new MapOfLists<String, String>(); 
	
	RepeatedPidStats stats=new RepeatedPidStats();
	
	File folder;
	String spreadsheetId;
	File mapStoreFolder;

	public ScriptCheckDuplicatePids(File folder, String spreadsheetId) {
		super();
		this.folder = folder;
		this.spreadsheetId = spreadsheetId;
		mapStoreFolder=new File(folder.getParentFile(), "duplicate-pids.store");
		pidToChoMap=new BigMap<String, String>("pidToChoMap", mapStoreFolder, true);
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
		
		RepeatedPidStats stats=new RepeatedPidStats();
		stats.calculate();
		
		System.out.println(pidToDuplicateChos.sizeOfAllLists()+" dups found");
		System.out.println("Finished");
		new SheetsDuplicatesReportWriter(spreadsheetId).write(pidToDuplicateChos, stats);
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
				System.out.println("WARN: Duplicate CHO: "+record.choUri);
				continue;
			}
			processedChos.add(record.choUri);
			
			record.remove(Edm.WebResource);//removing because these are handled in the props of the aggregation isShown...
			try {
//				System.out.println(record);
//				for(Entry<String, String> fieldAndId: record.getAllValues()) 
//					stats.add(fieldAndId.getValue(), record.getChoUri(), fieldAndId.getKey());
				List<DetectedPid> allPids = record.getAllPids();
				if(!allPids.isEmpty()) {
					HashSet<DetectedPid> uniques=new HashSet<IdsInRecord.DetectedPid>(allPids);
					for(DetectedPid pid: uniques) {
						String canonicalForm = pid.getCanonicalForm();
						if (pidToChoMap.containsKey(canonicalForm)) {
							pidToDuplicateChos.put(canonicalForm, record.choUri);
							if(!pidToDuplicateChos.get(canonicalForm).contains(pidToChoMap.get(canonicalForm)))
								pidToDuplicateChos.put(canonicalForm, pidToChoMap.get(canonicalForm));
//							System.out.println(canonicalForm+": "+pidToDuplicateChos.get(canonicalForm)+" - dups found");
							if(pidToDuplicateChos.size() % 1000 ==0)
								System.out.println(pidToDuplicateChos.size()+" dups found");
						} else
							pidToChoMap.put(canonicalForm, record.choUri);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
			GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		else
			GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		
//		String sourceFolderStr = "c://users/nfrei/desktop/data/pid";
		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids_export_20230227.zip";
		String spreadsheetId = "1vSynUDuGJhc7IfXxBMceyw2cYp0skGhmxeCGlSRF574";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
				if (args.length >= 2) 
					spreadsheetId = args[1];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptCheckDuplicatePids processor=new ScriptCheckDuplicatePids(sourceFolder, spreadsheetId);
		processor.process();
	}
	
	
}
