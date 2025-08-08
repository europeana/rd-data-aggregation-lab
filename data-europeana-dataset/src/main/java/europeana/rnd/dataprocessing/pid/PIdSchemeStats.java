package europeana.rnd.dataprocessing.pid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.ExampleInResource;
import europeana.rnd.dataprocessing.Examples;
import europeana.rnd.dataprocessing.MapOfIntsWithExamples;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import europeana.rnd.dataprocessing.pid.PidSchemes.PidMatchResult;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfMaps;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfSets;

public class PIdSchemeStats {
	
    MapOfMapsOfInts<PidScheme, String> statsByTypeAndField=new MapOfMapsOfInts<PidScheme, String>();
    MapOfInts<PidScheme> statsByType=new MapOfInts<PidScheme>();
    MapOfInts<PidScheme> statsByTypeUnique=new MapOfInts<PidScheme>();
    long countUnique=0;
    long countWithoutPid=0;
    MapOfMapsOfInts<String, PidScheme> countWithPidByProviderAndType=new MapOfMapsOfInts<String, PidScheme>();
    MapOfSets<String, String> providersOfDataProvider=new MapOfSets<String, String>();
    MapOfInts<String> countWithoutPidByProvider=new MapOfInts<String>();
    
    MapOfIntsWithExamples<String,String> statsByDomainHandle=new MapOfIntsWithExamples<String,String>(20);
    MapOfIntsWithExamples<String,String> statsByDomainArk=new MapOfIntsWithExamples<String,String>(20);
    
    MapOfInts<PidScheme> statsNonUrisByType=new MapOfInts<PidScheme>();
	MapOfMaps<PidScheme, String, Examples<ExampleInResource>> examples=new MapOfMaps<PidScheme, String, Examples<ExampleInResource>>();

	public PIdSchemeStats() { 
		super();
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(PidScheme type: PidSchemes.allSchemes) {
			Integer cnt=statsByType.get(type);
			if(cnt==null) 
				cnt=0;
			sb.append(type+"-"+cnt+"\n");
			if(cnt!=0) {
				MapOfInts<String> statsByField = statsByTypeAndField.get(type);
				List<Entry<String, Integer>> sortedEntries = statsByField.getSortedEntries(Sort.BY_KEY_ASCENDING);
				for(Entry<String, Integer> fieldStat: sortedEntries)
					sb.append(" - "+fieldStat.getKey()+": "+fieldStat.getValue()+"\n");
//				sb.append(" Examples:\n");
//				for(Example e: examples.get(type).getSample())
//					sb.append("  - "+e+"\n");
			}
		}
		return sb.toString();
	}

	public MapOfInts<String> getStatsByField() {
		MapOfInts<String> statsByField=new MapOfInts<String>();
    for(PidScheme type: PidSchemes.allSchemes) {
			Integer cnt=statsByType.get(type);
			if(cnt==null) 
				cnt=0;
			if(cnt!=0) {
				List<Entry<String, Integer>> sortedEntries = statsByTypeAndField.get(type).getSortedEntries(Sort.BY_KEY_ASCENDING);
				for(Entry<String, Integer> fieldStat: sortedEntries)
					statsByField.addTo(fieldStat.getKey(), fieldStat.getValue());
			}
		}
		return statsByField;
	}
	
	public MapOfIntsWithExamples<String,String> getStatsByDomain(PidScheme pidType) {
		if(pidType.equals(PidSchemes.ARK)) 
			return statsByDomainArk;
		else	if(pidType.equals(PidSchemes.HANDLE)) 
			return statsByDomainHandle;
		return null;
	}

	public void add(String choUri, String dataField, DetectedPid pid) {
		statsByType.incrementTo(pid.scheme);
		if(!pid.id.toLowerCase().startsWith("http"))
			statsNonUrisByType.incrementTo(pid.scheme);
		statsByTypeAndField.incrementTo(pid.scheme, dataField);
		Examples<ExampleInResource> examplesSet = examples.get(pid.scheme, dataField);
		if(examplesSet==null) {
			examplesSet=new Examples(10);
			examples.put(pid.scheme, dataField, examplesSet);
		}
		examplesSet.add(new ExampleInResource(pid.id, choUri));
		
//		String domain=pid.type.getDomain(pid.id);
//		if(domain!=null) {
//			if(pid.type==PidType.ARK) {
//				statsByDomainArk.addTo(domain, choUri);
//			} else if(pid.type==PidType.HANDLE) {
//				statsByDomainHandle.addTo(domain, choUri);
//			} else if(pid.type==PidType.PURL) {
//				statsByDomainPurl.addTo(domain, choUri);
//			} else if(pid.type==PidType.URN) 
//				statsByDomainUrn.addTo(domain, choUri);
//		}
	}

	public void incrementRecordWithPid(String dataProvider, String provider, HashSet<DetectedPid> pids) {
		HashSet<PidScheme> pidTypes=new HashSet<>();
		for(DetectedPid pid: pids)
			pidTypes.add(pid.scheme);		
		for(PidScheme tp: pidTypes)
			countWithPidByProviderAndType.incrementTo(dataProvider, tp);		
		providersOfDataProvider.put(dataProvider, provider);
		countUnique++;
	}
	public void incrementRecordWithoutPid(String dataProvider) {
		countWithoutPid++;
		countWithoutPidByProvider.incrementTo(dataProvider);
	}

	public void incrementType(PidScheme type) {
		statsByTypeUnique.incrementTo(type);
	}
	
}
