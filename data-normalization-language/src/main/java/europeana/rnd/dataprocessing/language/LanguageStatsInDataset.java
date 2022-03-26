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

import europeana.rnd.dataprocessing.language.LanguageInRecord.LanguageInClass;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class LanguageStatsInDataset {
	public static class LangStatsOfItem {
		long cntAll=0;
		long cntNormalizable=0;
		long cntNormalized=0;
		int cntCountry=0;
		int cntVariant=0;
		int cntScript=0;
		int cntExtension=0;
		int cntSubtags=0;
	}
	public static class LanguageStatsInClass {
		MapOfLists<String, LangStatsOfItem> langTagStatsByField=new MapOfLists<String, LangStatsOfItem>();
		MapOfInts<String> withoutLangTagStatsByField= new MapOfInts<String>();
		MapOfLists<String, LangStatsOfItem> propertyStatsByField=new MapOfLists<String, LangStatsOfItem>();//in principle this will be used only for dc:language

		public LanguageStatsInClass() {
		}

		public void add(LanguageInClass values) {
			for(Entry<String, ArrayList<Match>> entry : values.langTagValuesByField.entrySet()) {
				String field=entry.getKey();
				for(Match m: entry.getValue()) {
					LangStatsOfItem statsItem=getLangStatsOfItem(field, langTagStatsByField);
					
				}
			}
		}

		private LangStatsOfItem getLangStatsOfItem(String field,
				MapOfLists<String, LangStatsOfItem> langTagStatsByField2) {
			return null;
		}
		
//		public LanguageInClass(JsonObject jsonObj) {
//			if (jsonObj.containsKey("langTags")) {
//				JsonObject joLangTags = jsonObj.getJsonObject("langTags");
//				for(Entry<String, JsonValue> field: joLangTags.entrySet()) {
//					String prop=field.getKey();
//					JsonArray jaVals = field.getValue().asJsonArray();
//					for(JsonValue jv: jaVals) {
//						String val=((JsonString)jv).getString();
//						langTagValuesByField.put(prop, new Match(val));
//					}
//				}
//			}
//			if (jsonObj.containsKey("withoutLangTags")) {
//				JsonObject joWithoutLangTags = jsonObj.getJsonObject("withoutLangTags");
//				for(Entry<String, JsonValue> field: joWithoutLangTags.entrySet()) {
//					String prop=field.getKey();
//					Integer val=((JsonNumber)field.getValue()).intValue();
//					withoutLangTagByField.addTo(prop, val);
//				}
//			}
//			if (jsonObj.containsKey("propertyValues")) {
//				JsonObject joProps = jsonObj.getJsonObject("propertyValues");
//				for(Entry<String, JsonValue> field: joProps.entrySet()) {
//					String prop=field.getKey();
//					JsonArray jaVals = field.getValue().asJsonArray();
//					for(JsonValue jv: jaVals) {
//						String val=((JsonString)jv).getString();
//						propertyValuesByField.put(prop, new Match(val));
//					}
//				}
//			}
//		}
//
//
//		public JsonValue toJson() {
//			JsonObjectBuilder valsObj=Json.createObjectBuilder();
//			if(!langTagValuesByField.isEmpty()) {
//				JsonObjectBuilder propsObj=Json.createObjectBuilder();
//				for(String prop: langTagValuesByField.keySet()) {
//					JsonArrayBuilder values=Json.createArrayBuilder();
//					for(Match val: langTagValuesByField.get(prop)) 
//						values.add(Json.createValue(val.getInput()));
//					propsObj.add(prop, values.build());
//				}
//				valsObj.add("langTags", propsObj.build());
//			}
//			if(!withoutLangTagByField.isEmpty()) {
//				JsonObjectBuilder propsObj=Json.createObjectBuilder();
//				for(String prop: withoutLangTagByField.keySet()) 
//					propsObj.add(prop, withoutLangTagByField.get(prop));
//				valsObj.add("withoutLangTags", propsObj.build());
//			}
//			if(!propertyValuesByField.isEmpty()) {
//				JsonObjectBuilder propsObj=Json.createObjectBuilder();
//				for(String prop: propertyValuesByField.keySet()) {
//					JsonArrayBuilder values=Json.createArrayBuilder();
//					for(Match val: propertyValuesByField.get(prop)) 
//						values.add(Json.createValue(val.getInput()));
//					propsObj.add(prop, values.build());
//				}
//				valsObj.add("propertyValues", propsObj.build());
//			}
//			return valsObj.build();
//		}
	}
	
	public static class LanguageStat {
		public String className;
		public String property;
		public LangStatsOfItem stats;
		
		public LanguageStat(String className, String property, LangStatsOfItem match) {
			super();
			this.className = className;
			this.property = property;
			this.stats = match;
		}
		
		@Override
		public String toString() {
			return "DateValue [className=" + className + ", property=" + property + ", match=" + stats + "]";
		}
		
	}
	public static class LanguageStatsFromSource {
		HashMap<Resource, LanguageStatsInClass> statsByClassAndField;
		public LanguageStatsFromSource() {
			statsByClassAndField=new HashMap<Resource, LanguageStatsInClass>();
		}
		
//		public void readValuesOfSourceFromJson(JsonObject jv) {
//			for(Entry<String, JsonValue> field: jv.entrySet()) {
//				Resource cls=Edm.getResourceFromPrefixedName(field.getKey());
//				statsByClassAndField.put(cls, new LanguageStatsInClass(field.getValue().asJsonObject()));
//			}
//		}
		

		public void addTo(Resource cls, Property property, Literal value) {
			LanguageStatsInClass classEntry = getClassEntry(cls);
//			if (StringUtils.isEmpty(value.getLanguage())) {
//				classEntry.withoutLangTagByField.incrementTo(Edm.getPrefixedName(property));
//			} else {
//				classEntry.langTagValuesByField.put(Edm.getPrefixedName(property), new Match(value.getLanguage()));				
//			}
//			if(property.equals(Dc.language)) 
//				classEntry.propertyValuesByField.put(Edm.getPrefixedName(property), new Match(value.getValue().toString()) );
		}

//		public JsonObject toJson() {
//			JsonObjectBuilder ret=Json.createObjectBuilder();
//			for(Resource cls: statsByClassAndField.keySet()) {
//				ret.add(Edm.getPrefixedName(cls), statsByClassAndField.get(cls).toJson());
//			}
//			return ret.build();
//		}


		private LanguageStatsInClass getClassEntry(Resource cls) {
			LanguageStatsInClass inClass = statsByClassAndField.get(cls);
			if(inClass==null) {
				inClass=new LanguageStatsInClass();
				statsByClassAndField.put(cls, inClass);
			}
			return inClass;
		}
		
		public LanguageStatsInClass getStatsFor(Resource cls) {
			return statsByClassAndField.get(cls);
		}

		public void add(Resource cls, LanguageInClass values) {
			LanguageStatsInClass statsOfClass = getStatsFor(cls);
			statsOfClass.add(values);
		}
	}
	
	String dataset;
	LanguageStatsFromSource fromProvider=new LanguageStatsFromSource();
	LanguageStatsFromSource fromEuropeana=new LanguageStatsFromSource();
	
	public LanguageStatsInDataset(String dataset) {
		super();
		this.dataset = dataset;
		fromProvider=new LanguageStatsFromSource();
		fromEuropeana=new LanguageStatsFromSource();
	}

//	public LanguageStatsInDataset(JsonObject jv) {
//		this(jv.getString("id"));
//		JsonObject fromProviderJson = jv.getJsonObject("fromProvider");
//		JsonObject fromEuropeanaJson = jv.getJsonObject("fromEuropeana");
//		fromProvider.readValuesOfSourceFromJson(fromProviderJson);
//		fromEuropeana.readValuesOfSourceFromJson(fromEuropeanaJson);
//	}

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
	
//	public JsonObject toJson() {
//		JsonObjectBuilder ret=Json.createObjectBuilder();
//		ret.add("id", Json.createValue(dataset));
//		ret.add("fromEuropeana", fromEuropeana.toJson());
//		ret.add("fromProvider", fromProvider.toJson());
//		return ret.build();
//	}

	public String getDataset() {
		return dataset;
	}

	public void add(LanguageInRecord record) {
		for(Entry<Resource, LanguageInClass> entry : record.fromEuropeana.valuesByClassAndField.entrySet()) 
			fromEuropeana.add(entry.getKey(), entry.getValue());
		for(Entry<Resource, LanguageInClass> entry : record.fromProvider.valuesByClassAndField.entrySet()) 
			fromProvider.add(entry.getKey(), entry.getValue());
	}

}
