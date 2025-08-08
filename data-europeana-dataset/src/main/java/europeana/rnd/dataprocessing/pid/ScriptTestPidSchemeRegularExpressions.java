package europeana.rnd.dataprocessing.pid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

import org.apache.commons.io.FileUtils;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import apiclient.google.GoogleApi;
import europeana.rnd.dataprocessing.ExampleInResource;
import europeana.rnd.dataprocessing.Examples;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import europeana.rnd.dataprocessing.pid.PidSchemes.PidMatchResult;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.datastruct.BigMap;
import inescid.util.datastruct.BigSet;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptTestPidSchemeRegularExpressions {
		
	File inputFile;
	Reporting reporting;

	class Reporting { 
	  File outFolder;
	  File unmatchedFile;
	  MapOfInts<String> reportedCasesCnt=new MapOfInts<String>();
	  int cntUnmatched=0;
	  
	  int cntRecords=0;
	  
	  HashSet<String> providerWithPids=new HashSet<String>();
	  HashSet<String> providerWithoutPids=new HashSet<String>();
    MapOfLists<PidScheme, String> formsSample=new MapOfLists<>();
    
	  public Reporting(File outFolder) throws IOException {
      this.outFolder = outFolder;
      unmatchedFile=new File(outFolder, "unmatched-pids.txt");
      FileUtils.write(unmatchedFile, "", StandardCharsets.UTF_8, false);
    }
	  
	  void reportMatched(String id, PidScheme schema) throws IOException {
	    if(!formsSample.containsKey(schema) || 
	        formsSample.get(schema).size()<3) {
	      formsSample.put(schema, id+ " ## "+ schema.getCanonicalForm(id)+ " ## "+ schema.getResolvableUrl(id)); 
	    }
	  }

    void reportUnmatched(String id) throws IOException {
      String prefix=id.substring(0, Math.min(10, id.length()-1));
      Integer cnt = reportedCasesCnt.get(prefix);
      if(cnt==null || cnt<3) {
        FileUtils.write(unmatchedFile, id+"\n", StandardCharsets.UTF_8, true);
        reportedCasesCnt.incrementTo(prefix);
      }
      cntUnmatched++;
	  }
    
    void reportRecordWithPid(String dataProvider) {
      providerWithoutPids.remove(dataProvider);
      providerWithPids.add(dataProvider);
      cntRecords++;
    }

    void reportRecordWithoutPid(String dataProvider) {
      if(!providerWithPids.contains(dataProvider))
        providerWithoutPids.add(dataProvider);
      cntRecords++;
    }
    
    void end() throws IOException {
      FileUtils.write(unmatchedFile, "Total unmatched: "+cntUnmatched+"\n", StandardCharsets.UTF_8, true);      
 
      StringBuilder sb=new StringBuilder();
      sb.append("\nPID forms;\n");
      for(PidScheme schema: formsSample.keySet()) {
        for(String pidFormsString: formsSample.get(schema)) {
          sb.append(schema.getSchemeId()).append(" - ").append(pidFormsString).append("\n");
        }
      }
      
      sb.append("\nPROVIDERS WIth PIDs\n");
      for(String dp: providerWithPids)
        sb.append(dp).append("\n");
      sb.append("\nPROVIDERS WIthout PIDs\n");
      for(String dp: providerWithoutPids)
        sb.append(dp).append("\n");
      
      sb.append("\nTotal records: ").append(cntRecords).append("\n");
      FileUtils.write(unmatchedFile, sb.toString(), StandardCharsets.UTF_8, true);      
    }
	}

	public ScriptTestPidSchemeRegularExpressions(File inputFile, File outFolder) throws IOException {
		super();
		this.inputFile = inputFile;
//		this.outFolder = outFolder;
		reporting=new Reporting(outFolder);
	}

	public void process() throws IOException {
		boolean TESTING=false;
		
		int fileCnt=0;
		if(inputFile.getName().endsWith(".zip")) {
			try (FileInputStream fis = new FileInputStream(inputFile);
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
			for(File innerFolder: inputFile.listFiles()) {
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
		reporting.end();
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
			
			record.remove(Edm.WebResource);//removing because these are handled in the props of the aggregation isShown...
			try {
				List<DetectedPid> allPids = record.getAllPids();
				if(!allPids.isEmpty()) {
				  reporting.reportRecordWithPid(record.dataProvider);
				  for(DetectedPid pid: allPids) {
 				    PidMatchResult matchedSchema = PidSchemes.matchPidSchema(pid.id);
 				    if(matchedSchema==null) {
 				      reporting.reportUnmatched(pid.id);
 				    }else
 				      reporting.reportMatched(pid.id, matchedSchema.schema());
				  }
				} else
				  reporting.reportRecordWithoutPid(record.dataProvider);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
//		String sourceFolderStr = "c://users/nfrei/desktop/data/pid";
		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids_export_202506.zip";
//		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids-export-sample.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "regex-test");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptTestPidSchemeRegularExpressions processor=new ScriptTestPidSchemeRegularExpressions(sourceFolder, outFolder);
		processor.process();
	}
	
	
}
