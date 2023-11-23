package europeana.rnd.dataprocessing.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class LanguageInRecord {
	public static class LanguageInClass {
		MapOfLists<String, Match> langTagValuesByField=new MapOfLists<String, Match>();
		MapOfInts<String> withoutLangTagByField= new MapOfInts<String>();
		MapOfLists<String, Match> propertyValuesByField=new MapOfLists<String, Match>();//in principle this will be used only for dc:language

		public LanguageInClass() {
		}
		
		public LanguageInClass(JsonObject jsonObj) {
			if (jsonObj.containsKey("langTags")) {
				JsonObject joLangTags = jsonObj.getJsonObject("langTags");
				for(Entry<String, JsonValue> field: joLangTags.entrySet()) {
					String prop=field.getKey();
					JsonArray jaVals = field.getValue().asJsonArray();
					for(JsonValue jv: jaVals) {
						String val=((JsonString)jv).getString();
						langTagValuesByField.put(prop, new Match(val));
					}
				}
			}
			if (jsonObj.containsKey("withoutLangTags")) {
				JsonObject joWithoutLangTags = jsonObj.getJsonObject("withoutLangTags");
				for(Entry<String, JsonValue> field: joWithoutLangTags.entrySet()) {
					String prop=field.getKey();
					Integer val=((JsonNumber)field.getValue()).intValue();
					withoutLangTagByField.addTo(prop, val);
				}
			}
			if (jsonObj.containsKey("propertyValues")) {
				JsonObject joProps = jsonObj.getJsonObject("propertyValues");
				for(Entry<String, JsonValue> field: joProps.entrySet()) {
					String prop=field.getKey();
					JsonArray jaVals = field.getValue().asJsonArray();
					for(JsonValue jv: jaVals) {
						String val=((JsonString)jv).getString();
						propertyValuesByField.put(prop, new Match(val));
					}
				}
			}
		}


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
	
	public static class LangValue {
		public Resource cls;
		public String property;
		public Match match;
		public int withoutLangTag=0;
		
		public LangValue(Resource cls, String property, Match match, int withoutLangTag) {
			super();
			this.cls = cls;
			this.property = property;
			this.match = match;
			this.withoutLangTag = withoutLangTag;
		}
//		public LangValue(Resource cls, String property, Match match) {
//			super();
//			this.cls = cls;
//			this.property = property;
//			this.match = match;
//		}
		
		@Override
		public String toString() {
			return "DateValue [className=" + cls + ", property=" + property + ", match=" + match + "]";
		}		
	}
	public static class PropertyCount {
		public Resource cls;
		public String property;
		public int count;
		
		public PropertyCount(Resource cls, String property, int count) {
			super();
			this.cls = cls;
			this.property = property;
			this.count = count;
		}
		
		@Override
		public String toString() {
			return "PropertyCount [className=" + cls + ", property=" + property + ", count=" + count + "]";
		}
		
	}
	public static class LanguageFromSource {
		HashMap<Resource, LanguageInClass> valuesByClassAndField;
		public LanguageFromSource() {
			valuesByClassAndField=new HashMap<Resource, LanguageInClass>();
		}
		
		public void readValuesOfSourceFromJson(JsonObject jv) {
			for(Entry<String, JsonValue> field: jv.entrySet()) {
				Resource cls=Edm.getResourceFromPrefixedName(field.getKey());
				valuesByClassAndField.put(cls, new LanguageInClass(field.getValue().asJsonObject()));
			}

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
				ret.add(Edm.getPrefixedName(cls), valuesByClassAndField.get(cls).toJson());
			}
			return ret.build();
		}

//		public List<Match> getAllLangTagValues() {
//			List<Match> vals=new ArrayList<>();
//			for(LanguageInClass inResource: valuesByClassAndField.values()) {
//				vals.addAll(inResource.langTagValuesByField.valuesOfAllLists());
//			}
//			return vals;
//		}
		
		public List<LangValue> getAllLangTagValues() {
			List<LangValue> vals=new ArrayList<>();
			Set<Entry<Resource, LanguageInClass>> entrySet = valuesByClassAndField.entrySet();
			for(Entry<Resource, LanguageInClass> inClass: entrySet) {
				for (Entry<String, ArrayList<Match>> fieldResults : inClass.getValue().langTagValuesByField.entrySet()) {
					for (Match match: fieldResults.getValue()) 
						vals.add(new LangValue(inClass.getKey(), fieldResults.getKey(), match, getWithoutLangTagCount(inClass.getKey(), fieldResults.getKey())));
				}
			}
			return vals;
		}
		private int getWithoutLangTagCount(Resource cls, String property) {
			LanguageInClass languageInClass = valuesByClassAndField.get(cls);
			if(languageInClass==null)
				return 0;
			Integer cnt = languageInClass.withoutLangTagByField.get(property);
			return cnt==null ? 0 : cnt;
		}

		public List<PropertyCount> getAllCountWithoutLangTag() {
			List<PropertyCount> vals=new ArrayList<>();
			Set<Entry<Resource, LanguageInClass>> entrySet = valuesByClassAndField.entrySet();
			for(Entry<Resource, LanguageInClass> inClass: entrySet) {
				for (Entry<String, Integer> fieldResults : inClass.getValue().withoutLangTagByField.entrySet()) {
					vals.add(new PropertyCount(inClass.getKey(), fieldResults.getKey(), fieldResults.getValue()));
				}
			}
			return vals;
		}
		public List<LangValue> getAllPropertyValues() {
			List<LangValue> vals=new ArrayList<>();
			Set<Entry<Resource, LanguageInClass>> entrySet = valuesByClassAndField.entrySet();
			for(Entry<Resource, LanguageInClass> inClass: entrySet) {
				for (Entry<String, ArrayList<Match>> fieldResults : inClass.getValue().propertyValuesByField.entrySet()) {
					for (Match match: fieldResults.getValue()) 
						vals.add(new LangValue(inClass.getKey(), fieldResults.getKey(), match, 0));
				}
			}
			return vals;
		}

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
