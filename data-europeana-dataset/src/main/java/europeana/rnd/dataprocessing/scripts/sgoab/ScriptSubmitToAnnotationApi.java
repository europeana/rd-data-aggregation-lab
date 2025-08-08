package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsReader;
import europeana.rnd.api.annotation.AnnotationApi;
//import europeana.sgoab.annotations.Annotation;
//import europeana.sgoab.annotations.ObjectDetectionAnnotations;

public class ScriptSubmitToAnnotationApi {
//	
//	public static class AnnotationIdsInEuropeana {
//		HashMap<String, String> sgoabToEuropeanaIds=new HashMap<String, String>();
//		File saveFile;
//		
//		public AnnotationIdsInEuropeana(File saveFile) throws IOException {
//			super();
//			this.saveFile = saveFile;
//			if(saveFile.exists())
//				load();
//		}
//		
//		private void load() throws IOException {
//			String csvStr = FileUtils.readFileToString(saveFile, StandardCharsets.UTF_8);
//			CSVParser parser=new CSVParser(new StringReader(csvStr), CSVFormat.DEFAULT);
//			for(CSVRecord rec: parser) {
//				sgoabToEuropeanaIds.put(rec.get(0), rec.get(1));
//			}
//			parser.close();
//		}
//
//		public void save() throws IOException {
//			StringBuilder sb=new StringBuilder();
//			CSVPrinter printer=new CSVPrinter(sb, CSVFormat.DEFAULT);
//			for(Entry<String, String> id : sgoabToEuropeanaIds.entrySet()) {
//				printer.printRecord(id.getKey(), id.getValue());
//			}
//			printer.close();
//			FileUtils.write(saveFile, sb, StandardCharsets.UTF_8);
//		}
//		
//		public void setAndSave(String sgoabUri, String europeanaId) throws IOException {
//			sgoabToEuropeanaIds.put(sgoabUri, europeanaId);
//			save();
//		}
//
//		public boolean contains(String sgoabUri) {
//			return sgoabToEuropeanaIds.containsKey(sgoabUri) && 
//					 ! sgoabToEuropeanaIds.get(sgoabUri).startsWith("ERROR");
//		}
//		
//		public String getEuropeanaId(String sgoabUri) {
//			return sgoabToEuropeanaIds.get(sgoabUri);
//		}
//	}
//	
//	
//	
//	public static void main(String[] args) throws Exception {
//		String saveFileStr="C:\\Users\\nfrei\\Desktop\\data\\sgoab\\enrichments-dump\\sgoab2europeana.csv";
//		String sourceFolderStr="C:\\Users\\nfrei\\Desktop\\data\\sgoab\\enrichments-dump\\filtered\\annotations-correct-only";
//
//		File saveFile=new File(saveFileStr);
//		File sourceFolder=new File(sourceFolderStr);
//
//		AnnotationIdsInEuropeana annoIdDb=new AnnotationIdsInEuropeana(saveFile);
//
//		AnnotationApi api=new AnnotationApi(AnnotationApi.BASE_URL_TEST, "apidemo", "apidemo");
//		
//		for(File choAnnoFile: sourceFolder.listFiles()) {
//			if(!choAnnoFile.getName().endsWith(".json"))
//				continue;
//			ObjectDetectionAnnotations annos=new ObjectDetectionAnnotations(choAnnoFile);
//			List<Annotation> annotations = annos.getAnnotations();
//			for(Annotation anno: annotations) {
//				if(annoIdDb.contains(anno.getUri())) {
//					System.out.println("Annotation already exists (skipping): "+anno.getUri());
//					continue;
//				}
//				europeana.rnd.api.annotation.Annotation annoCreatedApi;
//				try {
//					annoCreatedApi = api.create(new europeana.rnd.api.annotation.Annotation(anno.getJson()));
//					annoIdDb.setAndSave(anno.getUri(), annoCreatedApi.getUri());
//				} catch (IOException e) {
//					e.printStackTrace();
//					annoIdDb.setAndSave(anno.getUri(), "ERROR: "+e.getMessage());
//				}
//			}
//		}
//		
//	}

}
