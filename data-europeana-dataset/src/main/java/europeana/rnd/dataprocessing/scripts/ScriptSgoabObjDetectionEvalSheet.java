package europeana.rnd.dataprocessing.scripts;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotException;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.http.HttpRequestService;
import inescid.util.AccessException;
import inescid.util.GlobalSingleton;
import inescid.util.HttpRequestServiceSingleton;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ScriptSgoabObjDetectionEvalSheet {
	
	public static class Annotation {
		public String label;
		public String confidence;
		public int x;
		public int y;
		public int w;
		public int h;
		public Annotation(String label) {
			this.label=label;
		}
		@Override
		public String toString() {
			return label+"("+confidence+")";
		}
	}

	public static class Cho {
		public String choUri;
		public ArrayList<Annotation> annotations=new ArrayList<ScriptSgoabObjDetectionEvalSheet.Annotation>();
		public Cho(String choUri, JsonObject annotationObj) {
			this.choUri = choUri;
			JsonArray classesJson = annotationObj.getJsonArray("classes");
			for(JsonValue classJson: classesJson) {
				annotations.add(new Annotation(((JsonString)classJson).getString()));
			}
			int idx=0;
			JsonArray scoresJson = annotationObj.getJsonArray("scores");
			for(JsonValue scoreJson: scoresJson) {
				annotations.get(idx).confidence=((JsonString)scoreJson).getString().trim();
				idx++;
			}
			idx=0;
			JsonArray bbxsJson = annotationObj.getJsonArray("bbxs");
			for(JsonValue bbxJson: bbxsJson) {
				annotations.get(idx).x=((JsonNumber)bbxJson.asJsonArray().get(0)).intValue();
				annotations.get(idx).y=((JsonNumber)bbxJson.asJsonArray().get(1)).intValue();
				annotations.get(idx).w=((JsonNumber)bbxJson.asJsonArray().get(2)).intValue();
				annotations.get(idx).h=((JsonNumber)bbxJson.asJsonArray().get(3)).intValue();
				idx++;
			}
		}
		
		@Override
		public String toString() {
			String ret= choUri +"[";
			for(Annotation anno: annotations) 
				ret+=anno+" ";
			ret+="]";
			return ret;
		}
	}

	static class EdmMetadata {
		Model edm;
		public EdmMetadata(String choUri) throws AccessException, InterruptedException, IOException {
			System.out.println(choUri);
			edm = RdfUtil.readRdfFromUri(choUri);
		}
		
		public List<String[]> getSubjects() {
			List<String[]> ret=new ArrayList<String[]>();
			StmtIterator subjects = EdmRdfUtil.listPropertyOfProxy(edm, Dc.subject, false);
			for(Statement st: subjects.toList()) {
				String sub=RdfUtil.getUriOrLiteralValue(st.getObject());
				if(sub.toLowerCase().startsWith("http://iconclass.org/")) 
					sub="https"+sub.substring(4);
				if(sub.toLowerCase().startsWith("https://iconclass.org/")) {
					String label=sub.substring("http://iconclass.org/".length()+1);
					try {
						Model model = RdfUtil.readRdfFromUri(sub+".rdf");
						Resource subjectRes = model.getResource(sub);
						if(subjectRes!=null) {
							Literal labelLit = RdfUtil.getLabelForResource(subjectRes, "en");
							if(labelLit!=null) 
								label=labelLit.getString();
						}
					} catch (Exception e) {
						System.err.println(sub);
						e.printStackTrace();
					}
					ret.add(new String[] {sub, label});						
				}
			}
			return ret;
		}
		
		public String getImageUrl() {
			RDFNode edmObj = EdmRdfUtil.getPropertyOfAggregation(edm, Edm.isShownBy);
			return RdfUtil.getUriOrLiteralValue(edmObj);
		}
	}
	
    static class SheetsFormWriter {
    	static {
    		GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
    	}
    	
		SheetsPrinter sheetsPrinter;
		
    	public SheetsFormWriter(String spreadsheetId, String sheetTitle) {
    		sheetsPrinter=new SheetsPrinter(spreadsheetId, sheetTitle);
		}
    	
    	public void init(String query) {
			sheetsPrinter.printRecord(
					"Object", "URI", "URL", "Detected class", "Confidence values/box", 
					"Correct and precise?", "Relevant?", "Merely acceptable, because", 
					"Missing relevant classes (from the SGoaB list)", "Notes", 
					"Iconclass tags available in the object's metadata (when applicable)", 
					 "", "Query - ", query
					);			
    	}
    	public void close() throws IOException {
    		sheetsPrinter.close();
    	}
    	
    	public void writeToEvaluationSheet(Cho cho, EdmMetadata edm) throws AccessException, InterruptedException, IOException {
//    		System.out.println("Writing to sheet: "+cho.toString());
    		MapOfLists<String, Annotation> groupedAnnotations=groupAnnotations(cho);

    		
    		boolean first=true;
    		for(String label: groupedAnnotations.keySet()) {
    			if(first) {
    				sheetsPrinter.print("=IMAGE(\""+edm.getImageUrl()+"\")", cho.choUri, edm.getImageUrl() );
    			} else
    				sheetsPrinter.print("", "", "" );
    			sheetsPrinter.print(label);
    			String confidences="";
    			for(Annotation anno: groupedAnnotations.get(label)) {
    				confidences+=anno.confidence+"("+anno.x+","+anno.y+","+anno.w+","+anno.h+") ";
    			}
    			sheetsPrinter.print(confidences.trim());
    			sheetsPrinter.print("", "", "", "", "");
    			if(first) {
	    			for(String[] subject: edm.getSubjects()) {
	    				sheetsPrinter.print("=HYPERLINK(\""+subject[0]+"\",\""+subject[1]+"\")");    				
	    			}
	    			first=false;
    			}    			
    			sheetsPrinter.println();
    		}
    	}

		private MapOfLists<String, Annotation> groupAnnotations(Cho cho) {
			MapOfLists<String, Annotation> groups=new MapOfLists<String, Annotation>();
			for(Annotation anno: cho.annotations) {
				groups.put(anno.label, anno);
			}			
			return groups;
		}
    }
	
	public static void main(String[] args) throws Exception {
		
		File sourceFolder=new File("C:\\Users\\nfrei\\Desktop\\data\\sgoab");
		
		//These query results are being obtained by the script europeana.rnd.api.search.scripts.ScriptGetUrisByQuery in project europeana-apis-client
		String[] queries=new String[]{
				"subject:\"http://iconclass.org\" AND edm_datasetName:\"90402_M_NL_Rijksmuseum\" AND has_media:TRUE AND proxy_dc_type:painting AND (proxy_dcterms_created:*11??* OR proxy_dcterms_created*12??* OR proxy_dcterms_created:*13??* OR proxy_dcterms_created:*14??* OR proxy_dcterms_created:*15??* OR proxy_dcterms_created:*16??* OR proxy_dcterms_created:*17??*) AND proxy_edm_type:IMAGE",
				"NOT(subject:\"http://iconclass.org\") AND edm_datasetName:\"90402_M_NL_Rijksmuseum\" AND has_media:TRUE AND proxy_dc_type:painting AND NOT (portrait OR portret) AND (proxy_dcterms_created:*11??* OR proxy_dcterms_created*12??* OR proxy_dcterms_created:*13??* OR proxy_dcterms_created:*14??* OR proxy_dcterms_created:*15??* OR proxy_dcterms_created:*16??* OR proxy_dcterms_created:*17??*) AND proxy_edm_type:IMAGE",
				"edm_datasetName:\"90402_M_NL_Rijksmuseum\" AND has_media:TRUE AND (proxy_dcterms_created:*11??* OR proxy_dcterms_created*12??* OR proxy_dcterms_created:*13??* OR proxy_dcterms_created:*14??* OR proxy_dcterms_created:*15??* OR proxy_dcterms_created:*16??* OR proxy_dcterms_created:*17??*) AND proxy_edm_type:IMAGE"
		};
		ArrayList<File> queryFiles=new ArrayList<File>();
		queryFiles.add(new File(sourceFolder, "URIs-SGoaB-evaluation-90402_M_NL_Rijksmuseum-query1.txt"));
		queryFiles.add(new File(sourceFolder, "URIs-SGoaB-evaluation-90402_M_NL_Rijksmuseum-query2.txt"));
		queryFiles.add(new File(sourceFolder, "URIs-SGoaB-evaluation-90402_M_NL_Rijksmuseum-query3.txt"));
		int maxSample=105;
		
		int queryCnt=1;
		for(File qFile: queryFiles) {
			SheetsFormWriter sheetsWriter=new SheetsFormWriter("1xnNh14tESiIAHEbelIKe_w0wH5AXy5Z9qDY3GN-cdQw", 
					"Evaluation"+queryCnt);
			sheetsWriter.init(queries[queryCnt-1]);
			boolean withIconclass=queryCnt==1 || queryCnt==3;
			queryCnt++;

			HashSet<String> uris=new HashSet<String>();
			for(String uri: FileUtils.readLines(qFile, StandardCharsets.UTF_8)) {
				if(!StringUtils.isEmpty(uri)) 
					uris.add(uri);
			}
			
			int sampleCnt=0;
			for(File annoFile: new File(sourceFolder, "enrichments-object-detection").listFiles()) {
				if(sampleCnt == maxSample) break;
				System.out.println("Reading "+annoFile.getName());
				FileReader reader = new FileReader(annoFile);
				JsonParser parser = Json.createParser(reader);
				
				while (parser.hasNext() && sampleCnt < maxSample) {
				   JsonParser.Event event = parser.next();
				   switch(event) {
				      case START_ARRAY:
				      case END_ARRAY:
				      case END_OBJECT:
				      case START_OBJECT:
				      case VALUE_FALSE:
				      case VALUE_NULL:
				      case VALUE_TRUE:
				      case VALUE_STRING:
				      case VALUE_NUMBER:
				    	  break;
				      case KEY_NAME:
				    	  String choUri="http://data.europeana.eu/item/"+parser.getString().substring(0, parser.getString().lastIndexOf('.')).replaceFirst("_", "/");
				    	  parser.next();
				    	  JsonObject annotationObj = parser.getObject();
				    	  if(uris.contains(choUri)) {
				    		  try {
								Cho cho = new Cho(choUri, annotationObj);
								if(!cho.annotations.isEmpty()) {
						    		EdmMetadata edm=new EdmMetadata(cho.choUri);
						    		
									if((withIconclass && !edm.getSubjects().isEmpty()) || (!withIconclass && edm.getSubjects().isEmpty())) {
										sheetsWriter.writeToEvaluationSheet(cho, edm);
									    sampleCnt++;
									}
								} else {
									System.out.println("No enrichments for "+choUri);
								}
							} catch (AccessException | InterruptedException | IOException e) {
								System.err.println("Error reading "+choUri+":\n"+e.getMessage());
								e.printStackTrace();
							}
				    	  }
				         break;
				   }
				}
				reader.close();
			}			
			sheetsWriter.close();
			System.out.println("Finished query "+(queryCnt-1)+" - "+sampleCnt+" items");
		}
	}


}
