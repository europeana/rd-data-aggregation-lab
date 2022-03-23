package europeana.rnd.dataprocessing.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class LanguageInRecord {
	public static class LanguageInClass {
		MapOfLists<String, Match> langTagValuesByField=new MapOfLists<String, Match>();
		MapOfInts<String> withoutLangTagByField= new MapOfInts<String>();
		MapOfLists<String, Match> propertyValuesByField=new MapOfLists<String, Match>();//in principle this will be used only for dc:language

		public JsonValue toJson() {
			JsonObjectBuilder valsObj=Json.createObjectBuilder();
			if(!langTagValuesByField.isEmpty()) {
				JsonObjectBuilder propsObj=Json.createObjectBuilder();
				for(String prop: langTagValuesByField.keySet()) {
					JsonArrayBuilder values=Json.createArrayBuilder();
					for(Match val: langTagValuesByField.get(prop)) 
						values.add(Json.createValue(val.getInput()));
					propsObj.add(prop, values.build());
				}
				valsObj.add("langTags", propsObj.build());
			}
			if(!withoutLangTagByField.isEmpty()) {
				JsonObjectBuilder propsObj=Json.createObjectBuilder();
				for(String prop: withoutLangTagByField.keySet()) 
					propsObj.add(prop, withoutLangTagByField.get(prop));
				valsObj.add("withoutLangTags", propsObj.build());
			}
			if(!propertyValuesByField.isEmpty()) {
				JsonObjectBuilder propsObj=Json.createObjectBuilder();
				for(String prop: propertyValuesByField.keySet()) {
					JsonArrayBuilder values=Json.createArrayBuilder();
					for(Match val: propertyValuesByField.get(prop)) 
						values.add(Json.createValue(val.getInput()));
					propsObj.add(prop, values.build());
				}
				valsObj.add("propertyValues", propsObj.build());
			}
			return valsObj.build();
		}
	}
	
	public static class DateValue {
		public String className;
		public String property;
		public Match match;
		
		public DateValue(String className, String property, Match match) {
			super();
			this.className = className;
			this.property = property;
			this.match = match;
		}
		
		@Override
		public String toString() {
			return "DateValue [className=" + className + ", property=" + property + ", match=" + match + "]";
		}
		
	}
	public static class LanguageFromSource {
		HashMap<Resource, LanguageInClass> valuesByClassAndField;
		public LanguageFromSource() {
			valuesByClassAndField=new HashMap<Resource, LanguageInClass>();
		}
		
		public void readValuesOfSourceFromJson(JsonObject jv) {
//			for(Resource cls: DatesHandler.edmClassesToProcess)
//				readValuesFromJson(jv, cls.getLocalName(), valuesByClassAndField.get(cls));
		}
		
		private void readValuesFromJson(JsonObject jv, String jsonField,
				MapOfLists<String, Match> valuesByFieldMap) {
//			JsonObject jsonClassObj = jv.getJsonObject(jsonField);
//			if(jsonClassObj!=null)
//				for(Entry<String, JsonValue> field: jsonClassObj.entrySet()) {
//					JsonArray valuesArr = field.getValue().asJsonArray();
//					for(Iterator<JsonValue> it=valuesArr.iterator() ; it.hasNext() ;) {
//						String val=((JsonString)it.next()).getString();
//						valuesByFieldMap.put(field.getKey(), new Match(val));
//					}
//				}
		}

		public void addTo(Resource cls, Property property, Literal value) {
			LanguageInClass classEntry = getClassEntry(cls);
			if (StringUtils.isEmpty(value.getLanguage())) {
				classEntry.withoutLangTagByField.incrementTo(Edm.getPrefixedName(property));
			} else {
				classEntry.langTagValuesByField.put(Edm.getPrefixedName(property), new Match(value.getLanguage()));				
			}
			if(property.equals(Dc.language)) 
				classEntry.propertyValuesByField.put(Edm.getPrefixedName(property), new Match(value.getValue().toString()) );
		}

//		public boolean isEmpty() {
//			for(Resource cls: DatesHandler.edmClassesToProcess)
//				if(! valuesByClassAndField.get(cls).isEmpty())
//					return false;
//			return true;
//		}

		public JsonObject toJson() {
			JsonObjectBuilder ret=Json.createObjectBuilder();
			for(Resource cls: valuesByClassAndField.keySet()) {
				ret.add(cls.getURI(), valuesByClassAndField.get(cls).toJson());
			}
			return ret.build();
		}

//		public List<Match> getAllValues() {
//			List<Match> vals=new ArrayList<>();
//			for(MapOfLists<String, Match> inResource: valuesByClassAndField.values()) {
//				vals.addAll(inResource.valuesOfAllLists());
//			}
//			return vals;
//		}
//		public List<DateValue> getAllValuesDetailed() {
//			ArrayList<DateValue> vals=new ArrayList<DateValue>();
//			for(Resource cls: DatesHandler.edmClassesToProcess) {
//				MapOfLists<String, Match> valuesByFieldMap = valuesByClassAndField.get(cls);
//				for(Entry<String, ArrayList<Match>> fieldResults : valuesByFieldMap.entrySet()) {
//					for(Match match: fieldResults.getValue()) 
//						vals.add(new DateValue(cls.getLocalName(), fieldResults.getKey(), match));
//				}
//			}
//			return vals;	
//		}

		private LanguageInClass getClassEntry(Resource cls) {
			LanguageInClass inClass = valuesByClassAndField.get(cls);
			if(inClass==null) {
				inClass=new LanguageInClass();
				valuesByClassAndField.put(cls, inClass);
			}
			return inClass;
		}
		
//		public List<Match> getValuesFor(Resource cls, String prop) {
//			List<Match> ret=null;
//			MapOfLists<String, Match> valuesByFieldMap = valuesByClassAndField.get(cls);
//			if(valuesByFieldMap!=null) 
//				ret=valuesByFieldMap.get(prop);
//			return ret==null ? Collections.emptyList() : ret;
//		}

		public LanguageInClass getValuesFor(Resource cls) {
			return valuesByClassAndField.get(cls);
		}
	}
	
	String choUri;
	LanguageFromSource fromProvider=new LanguageFromSource();
	LanguageFromSource fromEuropeana=new LanguageFromSource();
	
	public LanguageInRecord(String choUri) {
		super();
		this.choUri = choUri;
		fromProvider=new LanguageFromSource();
		fromEuropeana=new LanguageFromSource();
	}

	public LanguageInRecord(JsonObject jv) {
		this(jv.getString("id"));
		JsonObject fromProviderJson = jv.getJsonObject("fromProvider");
		JsonObject fromEuropeanaJson = jv.getJsonObject("fromEuropeana");
		fromProvider.readValuesOfSourceFromJson(fromProviderJson);
		fromEuropeana.readValuesOfSourceFromJson(fromEuropeanaJson);
	}

	public void addTo(Source src, Resource cls, Property property, Literal value) {
		switch (src) {
		case PROVIDER:
			fromProvider.addTo(cls, property, value);
			break;
		case EUROPEANA:
			fromEuropeana.addTo(cls, property, value);
			break;
		}
	}
	
//	public boolean isEmpty() {
//		return fromProvider.isEmpty() && fromProvider.isEmpty(); 
//	}
	
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
