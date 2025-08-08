package europeana.rnd.dataprocessing.pid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import apiclient.google.GoogleApi;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import inescid.dataaggregation.data.model.Edm;

public class ScriptReliabilityReport {

	File folder;
	String spreadsheetId;
	boolean includeTier0;
	
	StatsReliability stats=new StatsReliability();
	
	public ScriptReliabilityReport(File folder, String spreadsheetId, boolean includeTier0) {
		super();
		this.folder = folder;
		this.spreadsheetId = spreadsheetId;
		this.includeTier0 = includeTier0;
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
				if(!innerFolder.getName().startsWith("ids_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
				}
			}
		}
		System.out.println(stats);
		new SheetsReportWriter(spreadsheetId).writeReliability(stats);
	}

	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			IdsInRecord record=new IdsInRecord(jv);
			if(!includeTier0 && record.getContentTier()==ContentTier.Tier0)
				continue;
			record.remove(Edm.WebResource);//removing because these are handled in the props of teh aggregation isShown...
			try {
//				System.out.println(record);
//				for(Entry<String, String> fieldAndId: record.getAllValues()) 
//					stats.add(fieldAndId.getValue(), record.getChoUri(), fieldAndId.getKey());
				List<DetectedPid> allPids = record.getAllPids();
				List<DetectedPid> validPids = new ArrayList<IdsInRecord.DetectedPid>();
				for(DetectedPid pid: allPids) {
					boolean valid=stats.analise(pid.id, pid.type);
					if(valid)
						validPids.add(pid);
				}
				if(!validPids.isEmpty()) {
					stats.incrementRecordWithPid();
					HashSet<DetectedPid> uniques=new HashSet<IdsInRecord.DetectedPid>(allPids);
					if(uniques.size()>1)
						stats.addNonUnique(uniques, record.getChoUri());
					for(PidType type: record.getAllPidTypes()) {
						stats.incrementType(type);
					}
				}else
					stats.incrementRecordWithoutPid();
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
		boolean includeTier0=true;
		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids_export_20230227.zip";
		String spreadsheetId = includeTier0 ?  
				"1vSynUDuGJhc7IfXxBMceyw2cYp0skGhmxeCGlSRF574" :
					"1xX4T-RuE9epMoSD76u0urQLfl5MjyReb6g2lZWtyZHk";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
				if (args.length >= 2) {
					spreadsheetId = args[1];
					if (args.length >= 3) 
						includeTier0 = Boolean.valueOf(args[2]);
				}
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptReliabilityReport processor=new ScriptReliabilityReport(sourceFolder, spreadsheetId, includeTier0);
		processor.process();
	}
	
	

	
	
}
