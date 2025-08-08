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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import apiclient.google.sheets.SheetsReader;
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

public class ScriptGeneratePidRegistryXML {
		
  public static void main(String[] args) throws Exception {
    if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
      GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");       
    else
      GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");            
    String spreadsheetId = "1VXW4UZJaz62zxomD15bieES7tvgQ3rF3PaS23Q7BgxA";
    
    SheetsReader inSheet=new SheetsReader(spreadsheetId, "Schemes", "A1:G100");

    int rowIdx=-1;
    PidScheme currentScheme=null;
    List<PidScheme> allSchemes=new ArrayList<PidScheme>();
    for(List<Object> row: inSheet) {
      rowIdx++; 
      if(rowIdx==0) continue; //skip header row
//      System.out.println("row "+row);
      if(!StringUtils.isEmpty((String)row.get(1))) {
        if(currentScheme!=null)   
          allSchemes.add(currentScheme);
        currentScheme=new PidScheme((String)row.get(1));
        currentScheme.setTitle((String)row.get(1));
        currentScheme.setSeeAlso((String)row.get(2));
        currentScheme.setOrganization((String)row.get(3));
        currentScheme.addMatchingPattern((String)row.get(4));
        if(row.size()>5 && !StringUtils.isEmpty((String)row.get(5)))
          currentScheme.setCanonicalPattern((String)row.get(5));
        if(row.size()>6 && !StringUtils.isEmpty((String)row.get(6)))
          currentScheme.setResolvablePattern((String)row.get(6));
      } else {
        currentScheme.addMatchingPattern((String)row.get(4));        
      }
      rowIdx++; 
    }
    
    StringBuilder sb=new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:rdfs=\"https://www.w3.org/TR/rdf-schema/#\">\n"); 
    for(PidScheme scheme: allSchemes) {
      sb.append("  ").append("<edm:PersistentIdentifierScheme rdf:about=\"TODO\">\n");
//      System.out.println(scheme.getSchemeId());
      sb.append("    ").append("<dcterms:title>"+scheme.getTitle()+"</dcterms:title>\n");
      if(!StringUtils.isEmpty(scheme.getOrganization()))
        sb.append("    ").append("<rdfs:seeAlso>"+scheme.getSeeAlso()+"</rdfs:seeAlso>\n");
      if(!StringUtils.isEmpty(scheme.getSeeAlso()))
          sb.append("    ").append("<doap:maintainer>"+scheme.getOrganization()+"</doap:maintainer>\n");
      if(!StringUtils.isEmpty(scheme.getCanonicalPattern()))
        sb.append("    ").append("<edm:pidCanonicalPattern>"+scheme.getCanonicalPattern()+"</edm:pidCanonicalPattern>\n");
      if(!StringUtils.isEmpty(scheme.getResolvablePattern()))
        sb.append("    ").append("<edm:pidResolvabelUrlPattern>"+scheme.getResolvablePattern()+"</edm:pidResolvabelUrlPattern>\n");      
      for(int i=1; i<scheme.getMatchingPatterns().size(); i++) {
        sb.append("    ").append("<edm:matchingPattern>"+scheme.getMatchingPatterns().get(i)+"</edm:matchingPattern>\n");              
      }
      sb.append("  ").append("</edm:PersistentIdentifierScheme>\n");
    }
    sb.append("</rdf:RDF>");    
    System.out.println(sb.toString());
  }
  
}