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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfMapsOfInts;

public class IdStats {
    MapOfInts<PidType> statsByType=new MapOfInts<PidType>();
	HashMap<PidType, Examples> examples=new HashMap<PidType, Examples>();

	public IdStats() {
		super();
	}
	
	public void add(String id) {
		PidType pidType = PidType.getPidType(id);
		if(pidType!=null) {
			statsByType.incrementTo(pidType);
			Examples examplesSet = examples.get(pidType);
			if(examplesSet==null) {
				examplesSet=new Examples(1000);
				examples.put(pidType, examplesSet);
			}
			examplesSet.add(id);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(PidType type: PidType.values()) {
			Integer cnt=statsByType.get(type);
			if(cnt==null) 
				cnt=0;
			sb.append(type+"-"+cnt+"\n");
		}
		return sb.toString();
	}
}
