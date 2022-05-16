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
		Cases normalizable;
		HashMap<String, String> normalizableTo;
		Cases notNormalizable;
		Cases subtags;
		
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
		
		public LangStatsOfItem(Cases normalizable, Cases notNormalizable, Cases subtags, HashMap<String, String> normalizableTo) {
			this.notNormalizable=notNormalizable;
			this.normalizable = normalizable;
			this.normalizableTo = normalizableTo;
			this.subtags=subtags;
		}

		private void addSubtags(LangSubtagsAnalysis subTags, String input) {
			if(subTags.country)
				cntCountry++;
			if(subTags.variant)
				cntVariant++;
			if(subTags.script)
				cntScript++;
			if(subTags.extention)
				cntExtension++;
			if(subTags.subTag) {
				cntSubtags++;
				this.subtags.add(input);
			}
		}

		public void addCase(LangValue langValue, LangSubtagsAnalysis subTags, boolean isLangTag) {
			total++;
			if(isLangTag)
				withLangTags++;
			if(langValue.match.getNormalized()!=null) {
				if(langValue.match.getNormalized().equals(langValue.match.getInput())) 
					cntNormalized++;
				else {
					cntNormalizable++;
					normalizable.add(langValue.match.getInput());				
					normalizableTo.put(langValue.match.getInput(), langValue.match.getNormalized());				
				}
				addSubtags(subTags, langValue.match.getInput());
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
			return (double)cntNormalized / (double)(withLangTags==0 ? total : withLangTags)* 100;
		}

		public double normalizablePercent() {
			return (double)cntNormalizable / (double)(withLangTags==0 ? total : withLangTags) * 100;
		}
		public double notNormalizablePercent() {
			return (double)cntNotNormalizable / (double)(withLangTags==0 ? total : withLangTags) * 100;
		}
		public double subtagsPercent() {
			return (double)cntSubtags / (double)(withLangTags==0 ? total : withLangTags) * 100;
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
		Cases notNormalizableXmlLang;
		Cases normalizableXmlLang;
		HashMap<String, String> normalizableToXmlLang;
		HashMap<String, String> normalizableToDcLanguage;
		Cases subtagsXmlLang;
		Cases notNormalizableDcLanguage;
		Cases normalizableDcLanguage;
		Cases subtagsXmlDcLanguage;
		
		public LanguageStatsInClass(Cases normalizableXmlLang, Cases notNormalizableXmlLang, Cases subtagsXmlLang, Cases normalizableDcLanguage, Cases notNormalizableDcLanguage, Cases subtagsDcLanguage, HashMap<String, String> normalizableToXmlLang, HashMap<String,String> normalizableToDcLanguage) {
			this.notNormalizableXmlLang=notNormalizableXmlLang;
			this.normalizableXmlLang=normalizableXmlLang;
			this.subtagsXmlLang= subtagsXmlLang;
			this.notNormalizableDcLanguage= notNormalizableDcLanguage;
			this.normalizableDcLanguage= normalizableDcLanguage;
			this.subtagsXmlDcLanguage= subtagsDcLanguage;
			this.normalizableToXmlLang=normalizableToXmlLang;
			this.normalizableToDcLanguage=normalizableToDcLanguage;
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
				if (isLangTag)
					langStatsOfItem=new LangStatsOfItem(normalizableXmlLang, notNormalizableXmlLang, subtagsXmlLang, normalizableToXmlLang);
				else
					langStatsOfItem=new LangStatsOfItem(normalizableDcLanguage, notNormalizableDcLanguage, subtagsXmlDcLanguage, normalizableToDcLanguage);
				langTagStatsByField.put(field, langStatsOfItem);
			}
			return langStatsOfItem;
		}

		public void incrementWithoutLangTag(PropertyCount propCount) {
			LangStatsOfItem statOfItem = getLangStatsOfItem(propCount.property, langTagStatsByField, true);			
			statOfItem.incrementWithoutLangTag(propCount.count);
		}
	}
	
	public static class LanguageStatsFromSource {
		HashMap<Resource, LanguageStatsInClass> statsByClassAndField;
		Cases notNormalizableXmlLang;
		Cases normalizableXmlLang;
		Cases subtagsXmlLang;
		Cases notNormalizableDcLanguage;
		Cases normalizableDcLanguage;
		Cases subtagsDcLanguage;
		HashMap<String, String> normalizableToXmlLang;
		HashMap<String, String> normalizableToDcLanguage;
		
		public LanguageStatsFromSource() {
			statsByClassAndField=new HashMap<Resource, LanguageStatsInClass>();
			notNormalizableXmlLang=new Cases();
			normalizableXmlLang=new Cases();
			notNormalizableDcLanguage=new Cases();
			normalizableDcLanguage=new Cases();
			subtagsXmlLang=new Cases();
			subtagsDcLanguage=new Cases();
			normalizableToXmlLang=new HashMap<String, String>();
			normalizableToDcLanguage=new HashMap<String, String>();
		}
		


		private LanguageStatsInClass getClassEntry(Resource cls) {
			LanguageStatsInClass inClass = statsByClassAndField.get(cls);
			if(inClass==null) {
				inClass=new LanguageStatsInClass(normalizableXmlLang, notNormalizableXmlLang, subtagsXmlLang, normalizableDcLanguage, notNormalizableDcLanguage, subtagsDcLanguage, normalizableToXmlLang, normalizableToDcLanguage);
				statsByClassAndField.put(cls, inClass);
			}
			return inClass;
		}
		
		public LanguageStatsInClass getStatsFor(Resource cls) {
			return statsByClassAndField.get(cls);
		}

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
			LangStatsOfItem gStats=new LangStatsOfItem(null, null, null, null);
			for(LanguageStatsInClass statsForClass: statsByClassAndField.values()) {
				for(LangStatsOfItem statsOfItem : statsForClass.langTagStatsByField.values()) {
					gStats.add(statsOfItem);
				}
			}
			return gStats;
		}

		public LangStatsOfItem calculateGlobalStatsDcLanguage() {
			LangStatsOfItem gStats=new LangStatsOfItem(null, null, null, null);
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

	public String getDataset() {
		return dataset;
	}

}
