package europeana.rnd.dataprocessing.language;

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

import europeana.rnd.dataprocessing.language.LanguageInRecord.LangValue;
import europeana.rnd.dataprocessing.language.LanguageInRecord.LanguageInClass;
import europeana.rnd.dataprocessing.language.LanguageInRecord.PropertyCount;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LangStatsOfItem;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsInClass;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class LanguageStatsInDataset {
	
	public static class LangStatsOfItem {
		Examples notNormalizable;
		
		int total=0;
		
		int cntNormalizable=0;
		int cntNotNormalizable=0;
		int cntNormalized=0;
		int cntCountry=0;
		int cntVariant=0;
		int cntScript=0;
		int cntExtension=0;
		int cntSubtags=0;
		
		int withLangTags=0;
		int withoutLangTag=0;
		
		public LangStatsOfItem(Examples notNormalizable) {
			this.notNormalizable=notNormalizable;
		}

		private void addSubtags(LangSubtagsAnalysis subTags) {
			if(subTags.country)
				cntCountry++;
			if(subTags.variant)
				cntVariant++;
			if(subTags.script)
				cntScript++;
			if(subTags.extention)
				cntExtension++;
			if(subTags.subTag)
				cntSubtags++;
		}

		public void addCase(LangValue langValue, LangSubtagsAnalysis subTags, boolean isLangTag) {
			total++;
			if(isLangTag)
				withLangTags++;
			if(langValue.match.getNormalized()!=null) {
				if(langValue.match.getNormalized().equals(langValue.match.getInput())) 
					cntNormalized++;
				else 
					cntNormalizable++;
				addSubtags(subTags);
			} else {
				cntNotNormalizable++;
				notNormalizable.add(langValue.match.getInput());				
			}
		}

		public int total() {
			return total;
		}

		public double withLangTagsPercent() {
			return (double)withLangTags / (double)total * 100;
		}

		public double withoutLangTagsPercent() {
			return (double)withoutLangTag / (double)total * 100;
		}

		public double normalizedPercent() {
			return (double)cntNormalized / (double)withLangTags * 100;
		}

		public double normalizablePercent() {
			return (double)cntNormalizable / (double)withLangTags * 100;
		}
		public double notNormalizablePercent() {
			return (double)cntNotNormalizable / (double)withLangTags * 100;
		}
		public double subtagsPercent() {
			return (double)cntSubtags / (double)withLangTags * 100;
		}

		public void incrementWithoutLangTag(int count) {
			withoutLangTag+=count;
			total+=count;
		}

		public void add(LangStatsOfItem other) {
			total+=other.total;
			cntNormalized+=other.cntNormalized;
			cntNormalizable+=other.cntNormalizable;
			cntNotNormalizable+=other.cntNotNormalizable;
			cntCountry+=other.cntCountry;
			cntVariant+=other.cntVariant;
			cntScript+=other.cntScript;
			cntExtension+=other.cntExtension;
			cntSubtags+=other.cntSubtags;
			withLangTags+=other.withLangTags;
			withoutLangTag+=other.withoutLangTag;
		}
	}
	
	public static class LanguageStatsInClass {
		Map<String, LangStatsOfItem> langTagStatsByField=new HashMap<String, LangStatsOfItem>();
		MapOfInts<String> withoutLangTagStatsByField= new MapOfInts<String>();
		Map<String, LangStatsOfItem> propertyStatsByField=new HashMap<String, LangStatsOfItem>();//in principle this will be used only for dc:language
		Examples notNormalizableXmlLang;
		Examples notNormalizableDcLanguage;
		
		public LanguageStatsInClass(Examples notNormalizable, Examples notNormalizableDcLanguage) {
			this.notNormalizableXmlLang=notNormalizable;
			this.notNormalizableDcLanguage= notNormalizableDcLanguage;
		}

		public void addLangTagCase(LangValue langValue, LangSubtagsAnalysis subTags) {
			LangStatsOfItem statOfItem = getLangStatsOfItem(langValue.property, langTagStatsByField, true);			
			statOfItem.addCase(langValue, subTags, true);
		}
		public void addPropertyCase(LangValue langValue, LangSubtagsAnalysis subTags) {
			LangStatsOfItem statOfItem = getLangStatsOfItem(langValue.property, propertyStatsByField, false);			
			statOfItem.addCase(langValue, subTags, false);
		}

		private LangStatsOfItem getLangStatsOfItem(String field,
				Map<String, LangStatsOfItem> langTagStatsByField, boolean isLangTag) {
			LangStatsOfItem langStatsOfItem = langTagStatsByField.get(field);
			if(langStatsOfItem==null) {
				langStatsOfItem=new LangStatsOfItem(isLangTag ? notNormalizableXmlLang : notNormalizableDcLanguage);
				langTagStatsByField.put(field, langStatsOfItem);
			}
			return langStatsOfItem;
		}

		public void incrementWithoutLangTag(PropertyCount propCount) {
			LangStatsOfItem statOfItem = getLangStatsOfItem(propCount.property, langTagStatsByField, true);			
			statOfItem.incrementWithoutLangTag(propCount.count);
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
	
	public static class LanguageStatsFromSource {
		HashMap<Resource, LanguageStatsInClass> statsByClassAndField;
		Examples notNormalizableXmlLang;
		Examples notNormalizableDcLanguage;
		
		public LanguageStatsFromSource() {
			statsByClassAndField=new HashMap<Resource, LanguageStatsInClass>();
			notNormalizableXmlLang=new Examples(5000);
			notNormalizableDcLanguage=new Examples(5000);
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
				inClass=new LanguageStatsInClass(notNormalizableXmlLang, notNormalizableDcLanguage);
				statsByClassAndField.put(cls, inClass);
			}
			return inClass;
		}
		
		public LanguageStatsInClass getStatsFor(Resource cls) {
			return statsByClassAndField.get(cls);
		}

//		public void add(Resource cls, LanguageInClass values) {
//			LanguageStatsInClass statsOfClass = getStatsFor(cls);
//			statsOfClass.add(values);
//		}

		public void addLangTagCase(LangValue langValue, LangSubtagsAnalysis subTags) {
			LanguageStatsInClass statsOfClass = getClassEntry(langValue.cls);
			statsOfClass.addLangTagCase(langValue, subTags);
		}
		public void addPropertyCase(LangValue langValue, LangSubtagsAnalysis subTags) {
			LanguageStatsInClass statsOfClass = getClassEntry(langValue.cls);
			statsOfClass.addPropertyCase(langValue, subTags);
		}

		public void incrementWithoutLangTag(PropertyCount propCount) {
			LanguageStatsInClass statsOfClass = getClassEntry(propCount.cls);
			statsOfClass.incrementWithoutLangTag(propCount);
		}

		public LangStatsOfItem calculateGlobalStatsLangTags() {
			LangStatsOfItem gStats=new LangStatsOfItem(null);
			for(LanguageStatsInClass statsForClass: statsByClassAndField.values()) {
				for(LangStatsOfItem statsOfItem : statsForClass.langTagStatsByField.values()) {
					gStats.add(statsOfItem);
				}
			}
			return gStats;
		}

		public LangStatsOfItem calculateGlobalStatsDcLanguage() {
			LangStatsOfItem gStats=new LangStatsOfItem(null);
			for(LanguageStatsInClass statsForClass: statsByClassAndField.values()) {
				for(LangStatsOfItem statsOfItem : statsForClass.propertyStatsByField.values()) {
					gStats.add(statsOfItem);
				}
			}
			return gStats;
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




}
