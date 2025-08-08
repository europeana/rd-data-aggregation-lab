package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.RiotException;

import inescid.http.HttpRequestService;
import inescid.util.AccessException;
import inescid.util.GlobalSingleton;
import inescid.util.HttpRequestServiceSingleton;
import inescid.util.HttpUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

public class ScriptSgoabObjDetectionEvalSheetByQuery {
	
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
								JsonAnnotatedCho cho = new JsonAnnotatedCho(choUri, annotationObj);
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
