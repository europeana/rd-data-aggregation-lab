package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsReader;
//import europeana.rnd.dataprocessing.scripts.sgoab.ScriptFilterEnrichments.AnnotatedCho;
//import europeana.sgoab.annotations.ObjectDetectionAnnotations;

public class ScriptFilterEnrichments {
//	public static class AnnotatedCho{
//		public AnnotatedCho(String choUri) {
//			uri=choUri;
//		}
//		String uri;
//		ArrayList<String> tags=new ArrayList<String>();
//	}
//	
//	public static void main(String[] args) throws Exception {
//		if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
//			GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
//		else
//			GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");   
//		
//		String spreadsheetId="1Vl8g3Pozfc7el3DwkfixnhFN88vGK-_JQrGdz_s184c";
//		String sourceFolder="C:\\Users\\nfrei\\Desktop\\data\\sgoab\\enrichments-dump\\all";
//		String targetFolder="C:\\Users\\nfrei\\Desktop\\data\\sgoab\\enrichments-dump\\filtered";
//		File targetFolderCorrect=new File(targetFolder, "annotations-correct-only");
//		File targetFolderAll=new File(targetFolder, "annotations-all");
//		if(!targetFolderCorrect.exists())
//			targetFolderCorrect.mkdirs();
//		if(!targetFolderAll.exists())
//			targetFolderAll.mkdirs();
//		boolean onlyRelevant=true;
//		boolean includeAcceptable=false;		
//		
//		HashMap<String,String> sheets=new HashMap() {{
//			put("Paintings with IconClass1","B2:I226");
//			put("Paintings without IconClass1","B2:I321");
//			put("random images1","B2:I194");
//		}};
//		
//
//		ArrayList<AnnotatedCho> chos=new ArrayList<ScriptFilterEnrichments.AnnotatedCho>();
//		AnnotatedCho currentCho=null;
//		for(String sheetName : sheets.keySet()) {
//			String range=sheets.get(sheetName);
//			SheetsReader reader=new SheetsReader(spreadsheetId, sheetName, range);
//			for(List<Object> record: reader) {
//				String choUri=(String)record.get(0);
//				if(!StringUtils.isEmpty(choUri)) {
//					currentCho=new AnnotatedCho(choUri);
//					chos.add(currentCho);
//				}
//				
//				String tag=(String)record.get(2);
//				boolean correct=((String)record.get(5)).equals("TRUE");
//				boolean relevant=((String)record.get(6)).equals("TRUE");
//				boolean acceptable=record.size()>7 && !StringUtils.isEmpty(((String)record.get(7)));
//				
//				if(onlyRelevant && !relevant)
//					continue;
//				if(!correct && (!includeAcceptable || !acceptable) )
//					continue;
//				currentCho.tags.add(tag);
//			}
//		}
//		
//		
//		int totalAnnotations=0;
//		int filterTotalCho=0;
//		int filterTotalAnnotations=0;
//		
//		System.out.println(chos.size()+" chos");
////		File choListFile = new File(targetFolderCorrect, "cho-list.txt");
//		for(AnnotatedCho cho: chos) {
//			File choAnnoFile=new File(sourceFolder, URLEncoder.encode(cho.uri, "UTF-8")+".json");
//			if(choAnnoFile.exists()) {
//				ObjectDetectionAnnotations annos=new ObjectDetectionAnnotations(choAnnoFile);
//				annos.removeDuplicateAnnotations();
//				annos.changeToSemanticTags();
//				annos.saveTo(new File(targetFolderAll, choAnnoFile.getName()));
////				FileUtils.write(choListFile, cho.uri+"\n", StandardCharsets.UTF_8, true);
//				totalAnnotations+=annos.getSize();
//			}else {
//				System.out.println("WARN: CHO file not exists - "+choAnnoFile.getAbsolutePath());
//				continue;
//			}
//			
//			if(cho.tags.isEmpty()) {
//				System.out.println("WARN: CHO without tags - "+cho.uri);
//				continue;
//			}
//			ObjectDetectionAnnotations annos=new ObjectDetectionAnnotations(choAnnoFile);
//			annos.removeAnnotationsNotIn(new HashSet<String>(cho.tags));
//			annos.changeToSemanticTags();
//			annos.saveTo(new File(targetFolderCorrect, choAnnoFile.getName()));
////				FileUtils.write(choListFile, cho.uri+"\n", StandardCharsets.UTF_8, true);
//			filterTotalAnnotations+=cho.tags.size();
//			filterTotalCho++;
//		}
//		
//		System.out.println("Evaluated chos: "+ chos.size());
//		System.out.println("Total annotations: "+ totalAnnotations);
//		System.out.println("Total filtered chos: "+ filterTotalCho);
//		System.out.println("Total filtered annotations: "+ filterTotalAnnotations);
//		
//	}

}
