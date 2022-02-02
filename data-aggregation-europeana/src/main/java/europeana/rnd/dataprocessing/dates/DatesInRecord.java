package europeana.rnd.dataprocessing.dates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;

import inescid.util.datastruct.MapOfLists;

public class DatesInRecord {
	private static final String FIELD_PROVIDER_PROXY="inProviderProxy";
	private static final String FIELD_EUROPEANA_PROXY="inEuropeanaProxy";
	private static final String FIELD_WEB_RESOURCES="inWebResources";
	private static final String FIELD_AGENTS="inAgents";
	private static final String FIELD_TIME_SPANS="inTimeSpans";
	
	String choUri;
	MapOfLists<String, String> valuesByFieldInProviderProxy;
	MapOfLists<String, String> valuesByFieldInEuropeanaProxy;
	MapOfLists<String, String> valuesByFieldInWebResources;
	MapOfLists<String, String> valuesByFieldInAgents;
	MapOfLists<String, String> valuesByFieldInTimeSpans;
	
	public DatesInRecord(String choUri) {
		super();
		this.choUri = choUri;
		valuesByFieldInEuropeanaProxy=new MapOfLists<String, String>();
		valuesByFieldInProviderProxy=new MapOfLists<String, String>();
		valuesByFieldInWebResources=new MapOfLists<String, String>();
		valuesByFieldInAgents=new MapOfLists<String, String>();
		valuesByFieldInTimeSpans=new MapOfLists<String, String>();
	}

	public DatesInRecord(JsonObject jv) {
		this(jv.getString("id"));
		readValuesFromJson(jv, FIELD_EUROPEANA_PROXY, valuesByFieldInEuropeanaProxy);
		readValuesFromJson(jv, FIELD_PROVIDER_PROXY, valuesByFieldInProviderProxy);
		readValuesFromJson(jv, FIELD_WEB_RESOURCES, valuesByFieldInWebResources);
		readValuesFromJson(jv, FIELD_AGENTS, valuesByFieldInAgents);
		readValuesFromJson(jv, FIELD_TIME_SPANS, valuesByFieldInTimeSpans);
	}


	public void addToEuropeanaProxy(Property property, Literal value) {
		valuesByFieldInEuropeanaProxy.put(property.getLocalName(), value.getValue().toString());
	}
	public void addToProviderProxy(Property property, Literal value) {
		valuesByFieldInProviderProxy.put(property.getLocalName(), value.getValue().toString());
	}
	public void addToWebResources(Property property, Literal value) {
		valuesByFieldInWebResources.put(property.getLocalName(), value.getValue().toString());
	}
	public void addToAgents(Property property, Literal value) {
		valuesByFieldInAgents.put(property.getLocalName(), value.getValue().toString());
	}
	public void addToTimeSpans(Property property, Literal value) {
		valuesByFieldInTimeSpans.put(property.getLocalName(), value.getValue().toString());
	}
	
	public boolean isEmpty() {
		return valuesByFieldInProviderProxy.isEmpty()
				&& valuesByFieldInEuropeanaProxy.isEmpty()
				&& valuesByFieldInWebResources.isEmpty()
				&& valuesByFieldInAgents.isEmpty()
				&& valuesByFieldInTimeSpans.isEmpty();
	}
	
	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		writeToJson(ret, valuesByFieldInProviderProxy, FIELD_PROVIDER_PROXY);
		writeToJson(ret, valuesByFieldInEuropeanaProxy, FIELD_EUROPEANA_PROXY);
		writeToJson(ret, valuesByFieldInAgents, FIELD_AGENTS);
		writeToJson(ret, valuesByFieldInWebResources, FIELD_WEB_RESOURCES);
		writeToJson(ret, valuesByFieldInTimeSpans, FIELD_TIME_SPANS);
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}

	public MapOfLists<String, String> getValuesByFieldInProviderProxy() {
		return valuesByFieldInProviderProxy;
	}

	public MapOfLists<String, String> getValuesByFieldInEuropeanaProxy() {
		return valuesByFieldInEuropeanaProxy;
	}	
	

	private void writeToJson(JsonObjectBuilder ret, MapOfLists<String, String> valuesByFieldMap,
			String jsonField) {
		if(!valuesByFieldMap.isEmpty()) {
			JsonObjectBuilder proxyValsObj=Json.createObjectBuilder();
			for(String prop: valuesByFieldMap.keySet()) {
				JsonArrayBuilder values=Json.createArrayBuilder();
				for(String val: valuesByFieldMap.get(prop)) {
					values.add(Json.createValue(val));
				}
				proxyValsObj.add(prop, values.build());
			}
			ret.add(jsonField,proxyValsObj.build());
		}
	}

	private void readValuesFromJson(JsonObject jv, String jsonField,
			MapOfLists<String, String> valuesByFieldMap) {
		JsonObject fromProviderProxyObj = jv.getJsonObject(jsonField);
		if(fromProviderProxyObj!=null)
			for(Entry<String, JsonValue> field: fromProviderProxyObj.entrySet()) {
				JsonArray valuesArr = field.getValue().asJsonArray();
				for(Iterator<JsonValue> it=valuesArr.iterator() ; it.hasNext() ;) {
//					System.out.println(it.next().getValueType());
					String val=((JsonString)it.next()).getString();
					valuesByFieldMap.put(field.getKey(), val);
				}
			}
	}

	public List<String> getAllValues() {
		ArrayList<String> vals=new ArrayList<String>();
		for(MapOfLists<String, String> inResource: new MapOfLists[] {
			valuesByFieldInProviderProxy, valuesByFieldInEuropeanaProxy,
			valuesByFieldInWebResources, valuesByFieldInAgents, valuesByFieldInTimeSpans}) {
			vals.addAll(inResource.valuesOfAllLists());
		}
		return vals;
	}
	
	
}
