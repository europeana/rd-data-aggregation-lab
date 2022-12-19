package europeana.rnd.dataprocessing.uri;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;

public class UrisInRecord {
	public static class UrisInProperty {
		HashMap<Property, HashSet<String>> urisInProperty;
		public UrisInProperty() {
			urisInProperty=new HashMap<Property, HashSet<String>>();
		}
		
		public void readValuesFromJson(JsonObject jv) {
			for(Entry<String, JsonValue> field: jv.entrySet()) {
				Property cls=Edm.getPropertyFromPrefixedName(field.getKey());
				HashSet<String> urisOfClass = getPropertyEntry(cls);
				for( Iterator<JsonValue> it = field.getValue().asJsonArray().iterator(); it.hasNext() ; ) {
					urisOfClass.add(((JsonString)it.next()).getString());
				}
			}
		}

		public void addTo(Property prop, String uri) {
			HashSet<String> classEntry = getPropertyEntry(prop);
			classEntry.add(uri);
		}

		public JsonObject toJson() {
			JsonObjectBuilder ret=Json.createObjectBuilder();
			for(Property cls: urisInProperty.keySet()) {
				JsonArrayBuilder arrBuild=Json.createArrayBuilder();
				for(String uri: urisInProperty.get(cls)) 
					arrBuild.add(uri);
				ret.add(Edm.getPrefixedName(cls), arrBuild.build());
			}
			return ret.build();
		}

		private HashSet<String> getPropertyEntry(Property cls) {
			HashSet<String> inClass = urisInProperty.get(cls);
			if(inClass==null) {
				inClass=new HashSet<String>();
				urisInProperty.put(cls, inClass);
			}
			return inClass;
		}
		
		public HashSet<String> getValuesFor(Property cls) {
			return urisInProperty.get(cls);
		}

		public boolean isEmpty() {
			return urisInProperty.isEmpty();
		}

		public Set<String> getAllUrisInProperties(Set<Property> inProperties) {
			Set<String> uris=new HashSet<String>();
			for(Property prop : urisInProperty.keySet()) {
				if(inProperties.contains(prop)) {
					uris.addAll(urisInProperty.get(prop));
				}
			}
			return uris;
		}

		public Set<Property> propertySet() {
			return urisInProperty.keySet();
		}
	}

	
	public static class UrisFromSource {
		HashMap<Resource, UrisInProperty> urisByClass;
		public UrisFromSource() {
			urisByClass=new HashMap<Resource, UrisInProperty>();
		}
		
		public void readValuesOfSourceFromJson(JsonObject jv) {
			for(Entry<String, JsonValue> field: jv.entrySet()) {
				Resource cls=Edm.getResourceFromPrefixedName(field.getKey());
				UrisInProperty urisOfClass = getClassEntry(cls);
				urisOfClass.readValuesFromJson(field.getValue().asJsonObject());
			}
		}

		public void addTo(Resource cls, Property prop, String uri) {
			UrisInProperty classEntry = getClassEntry(cls);
			classEntry.addTo(prop, uri);
		}

		public JsonObject toJson() {
			JsonObjectBuilder ret=Json.createObjectBuilder();
			for(Resource cls: urisByClass.keySet()) {
				UrisInProperty uri=urisByClass.get(cls); 
				ret.add(Edm.getPrefixedName(cls), uri.toJson());
			}
			return ret.build();
		}

		private UrisInProperty getClassEntry(Resource cls) {
			UrisInProperty inClass = urisByClass.get(cls);
			if(inClass==null) {
				inClass=new UrisInProperty();
				urisByClass.put(cls, inClass);
			}
			return inClass;
		}
		
		public UrisInProperty getValuesFor(Resource cls) {
			return urisByClass.get(cls);
		}

		public boolean isEmpty() {
			return urisByClass.isEmpty();
		}
	}
	
	String choUri;
	UrisFromSource fromProvider=new UrisFromSource();
	UrisFromSource fromEuropeana=new UrisFromSource();
	
	public UrisInRecord(String choUri) {
		super();
		this.choUri = choUri;
		fromProvider=new UrisFromSource();
		fromEuropeana=new UrisFromSource();
	}

	public UrisInRecord(JsonObject jv) {
		this(jv.getString("id"));
		JsonObject fromProviderJson = jv.getJsonObject("fromProvider");
		JsonObject fromEuropeanaJson = jv.getJsonObject("fromEuropeana");
		fromProvider.readValuesOfSourceFromJson(fromProviderJson);
		fromEuropeana.readValuesOfSourceFromJson(fromEuropeanaJson);
	}

	public void addTo(Source src, Resource entity, Property prop) {
		Resource cls;
		if(entity.isURIResource()) {
			Statement typeSt = entity.getProperty(Rdf.type);
			if(typeSt==null || !typeSt.getObject().isURIResource())
				return;
			cls=typeSt.getObject().asResource();
		} else 
			return;
		if(!(cls.equals(Edm.Agent) || cls.equals(Edm.Place) || cls.equals(Edm.TimeSpan) || cls.equals(Skos.Concept)))
			return;

		String uri=entity.getURI();
		if(!uri.startsWith("http") && uri.contains("#")) {
			uri=uri.substring(uri.indexOf('#'));
//			System.out.println(entity.getURI());
		}		
		
		switch (src) {
		case PROVIDER:
			fromProvider.addTo(cls, prop, uri);
			break;
		case EUROPEANA:
			fromEuropeana.addTo(cls, prop, uri);
			break;
		}
	}
	
	public boolean isEmpty() {
		return fromProvider.isEmpty() && fromEuropeana.isEmpty(); 
	}
	
	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		ret.add("fromEuropeana", fromEuropeana.toJson());
		ret.add("fromProvider", fromProvider.toJson());
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}

//	public MapOfLists<String, Match> getValuesByFieldInProviderProxy() {
//		return valuesByFieldInProviderProxy;
//	}
//
//	public MapOfLists<String, Match> getValuesByFieldInEuropeanaProxy() {
//		return valuesByFieldInEuropeanaProxy;
//	}	
//	
//	public MapOfLists<String, Match> getValuesByFieldInWebResources() {
//		return valuesByFieldInWebResources;
//	}	
//	
//	public MapOfLists<String, Match> getValuesByFieldInAgents() {
//		return valuesByFieldInAgents;
//	}	
//	
//	public MapOfLists<String, Match> getValuesByFieldInTimeSpans() {
//		return valuesByFieldInTimeSpans;
//	}	
	

//	public List<Match> getAllValues(Source source) {
//		ArrayList<Match> vals=new ArrayList<Match>();
//		switch (source) {
//		case PROVIDER:
//			vals.addAll(fromProvider.getAllValues());
//			break;
//		case EUROPEANA:
//			vals.addAll(fromEuropeana.getAllValues());
//			break;
//		case ANY:
//			vals.addAll(fromProvider.getAllValues());
//			vals.addAll(fromEuropeana.getAllValues());
//			break;
//		}
//		return vals;
//	}
//	public List<DateValue> getAllValuesDetailed(Source source) {
//		ArrayList<DateValue> vals=new ArrayList<DateValue>();
//		switch (source) {
//		case PROVIDER:
//			vals.addAll(fromProvider.getAllValuesDetailed());
//			break;
//		case EUROPEANA:
//			vals.addAll(fromEuropeana.getAllValuesDetailed());
//			break;
//		case ANY:
//			vals.addAll(fromProvider.getAllValuesDetailed());
//			vals.addAll(fromEuropeana.getAllValuesDetailed());
//			break;
//		}
//		return vals;
//	}
//
//	public List<Match> getValuesFor(Source source, Resource cls, Property prop) {
//		ArrayList<Match> vals=new ArrayList<Match>();
//		switch (source) {
//		case PROVIDER:
//			vals.addAll(fromProvider.getValuesFor(cls, prop.getLocalName()));
//			break;
//		case EUROPEANA:
//			vals.addAll(fromEuropeana.getValuesFor(cls, prop.getLocalName()));
//			break;
//		case ANY:
//			vals.addAll(fromProvider.getValuesFor(cls, prop.getLocalName()));
//			vals.addAll(fromEuropeana.getValuesFor(cls, prop.getLocalName()));
//			break;
//		}
//		return vals;
//	}
//
//	public MapOfLists<String, Match> getValuesByFieldInClass(Source source, Resource cls) {
//		switch (source) {
//		case PROVIDER:
//			return fromProvider.getValuesFor(cls);
//		case EUROPEANA:
//			return fromEuropeana.getValuesFor(cls);
//		default:
//			throw new IllegalArgumentException("Source cannot be "+source);
//		}
//	}
	
	
}
