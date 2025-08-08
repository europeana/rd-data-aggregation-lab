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
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfMaps;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfSets;

public class IdStats {
	
	public static class MultiplePidStats {
		MapOfInts<String> byType=new MapOfInts<String>();
		Map<String, Examples<ExampleInResource>> examples=new HashMap<String, Examples<ExampleInResource>>();

		public void add(Set<DetectedPid> pids, String choUri) {
			HashSet<PidType> types=new HashSet<PidType>();
			for(DetectedPid pid: pids)
				types.add(pid.type);
			String key=getTypesKey(types);
			byType.incrementTo(key);
			Examples<ExampleInResource> typeExamples = examples.get(key);
			if(typeExamples==null) {
				typeExamples=new Examples(10);
				examples.put(key, typeExamples);
			}
			StringBuilder idsStr=new StringBuilder();
			for(DetectedPid p: pids) {
				idsStr.append(p.id).append(" ; ");
			}
			typeExamples.add(new ExampleInResource(idsStr.toString(), choUri));
		}

		private String getTypesKey(Set<PidType> types) {
			ArrayList<String> typesStrs=new ArrayList<String>();
			for(PidType type: types)
				typesStrs.add(type.name());
			Collections.sort(typesStrs);
			StringBuilder sb=new StringBuilder();
			boolean first=true;
			for(String typeStr: typesStrs) {
				if(first)
					first=false;
				else
					sb.append(", ");
				sb.append(typeStr);
			}
			return sb.toString();
		}
	}
	
    MapOfMapsOfInts<PidType, String> statsByTypeAndField=new MapOfMapsOfInts<PidType, String>();
    MapOfInts<PidType> statsByType=new MapOfInts<PidType>();
    MapOfInts<PidType> statsByTypeUnique=new MapOfInts<PidType>();
    long countUnique=0;
    long countWithoutPid=0;
    MapOfMapsOfInts<String, PidType> countWithPidByProviderAndType=new MapOfMapsOfInts<String, PidType>();
    MapOfSets<String, String> providersOfDataProvider=new MapOfSets<String, String>();
    MapOfInts<String> countWithoutPidByProvider=new MapOfInts<String>();
    
    MapOfIntsWithExamples<String,String> statsByDomainUrn=new MapOfIntsWithExamples<String,String>(20);
    MapOfIntsWithExamples<String,String> statsByDomainHandle=new MapOfIntsWithExamples<String,String>(20);
    MapOfIntsWithExamples<String,String> statsByDomainArk=new MapOfIntsWithExamples<String,String>(20);
    MapOfIntsWithExamples<String,String> statsByDomainPurl=new MapOfIntsWithExamples<String,String>(20);
//    MapOfInts<String> statsByDomainUrn=new MapOfInts<String>();
//    MapOfInts<String> statsByDomainHandle=new MapOfInts<String>();
//    MapOfInts<String> statsByDomainArk=new MapOfInts<String>();
//    MapOfInts<String> statsByDomainPurl=new MapOfInts<String>();
    
    MapOfInts<PidType> statsNonUrisByType=new MapOfInts<PidType>();
//	HashMap<PidType, Examples> examples=new HashMap<PidType, Examples>();
	MapOfMaps<PidType, String, Examples<ExampleInResource>> examples=new MapOfMaps<PidType, String, Examples<ExampleInResource>>();

	MultiplePidStats multiplePidStats=new MultiplePidStats();

	public IdStats() { 
		super();
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(PidType type: PidType.values()) {
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
		for(PidType type: PidType.values()) {
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
	
	public MapOfIntsWithExamples<String,String> getStatsByDomain(PidType pidType) {
		if(pidType==PidType.ARK) {
			return statsByDomainArk;
		} else if(pidType==PidType.HANDLE) {
			return statsByDomainHandle;
		} else if(pidType==PidType.PURL) {
			return statsByDomainPurl;
		} else if(pidType==PidType.URN) 
			return statsByDomainUrn;
		return null;
	}

	public void add(String choUri, DetectedPid pid) {
		statsByType.incrementTo(pid.type);
		if(!pid.id.toLowerCase().startsWith("http"))
			statsNonUrisByType.incrementTo(pid.type);
		statsByTypeAndField.incrementTo(pid.type, pid.field);
		Examples<ExampleInResource> examplesSet = examples.get(pid.type,pid.field);
		if(examplesSet==null) {
			examplesSet=new Examples(10);
			examples.put(pid.type, pid.field, examplesSet);
		}
		examplesSet.add(new ExampleInResource(pid.id, choUri));
		
		String domain=pid.type.getDomain(pid.id);
		if(domain!=null) {
			if(pid.type==PidType.ARK) {
				statsByDomainArk.addTo(domain, choUri);
			} else if(pid.type==PidType.HANDLE) {
				statsByDomainHandle.addTo(domain, choUri);
			} else if(pid.type==PidType.PURL) {
				statsByDomainPurl.addTo(domain, choUri);
			} else if(pid.type==PidType.URN) 
				statsByDomainUrn.addTo(domain, choUri);
		}
	}

	public void addNonUnique(Set<DetectedPid> pids, String choUri) {
		multiplePidStats.add(pids, choUri);
	}

	public void incrementRecordWithPid(String dataProvider, String provider, HashSet<DetectedPid> pids) {
		HashSet<PidType> pidTypes=new HashSet<PidType>();
		for(DetectedPid pid: pids)
			pidTypes.add(pid.type);		
		for(PidType tp: pidTypes)
			countWithPidByProviderAndType.incrementTo(dataProvider, tp);		
		providersOfDataProvider.put(dataProvider, provider);
		countUnique++;
	}
	public void incrementRecordWithoutPid(String dataProvider) {
		countWithoutPid++;
		countWithoutPidByProvider.incrementTo(dataProvider);
	}

	public void incrementType(PidType type) {
		statsByTypeUnique.incrementTo(type);
	}
	
}
