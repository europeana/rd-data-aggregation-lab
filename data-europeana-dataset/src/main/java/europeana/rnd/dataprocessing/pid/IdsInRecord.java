package europeana.rnd.dataprocessing.pid;

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
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfLists;

public class IdsInRecord {
	public static class IdsInClass {
		MapOfLists<String, String> propertyValuesByField=new MapOfLists<String, String>();

		public IdsInClass() {
		}
		
		public IdsInClass(JsonObject jsonObj) {
			if (jsonObj.containsKey("propertyValues")) {
				JsonObject joProps = jsonObj.getJsonObject("propertyValues");
				for(Entry<String, JsonValue> field: joProps.entrySet()) {
					String prop=field.getKey();
					JsonArray jaVals = field.getValue().asJsonArray();
					for(JsonValue jv: jaVals) {
						String val=((JsonString)jv).getString();
						propertyValuesByField.put(prop, val);
					}
				}
			}
		}


		public JsonValue toJson() {
			JsonObjectBuilder valsObj=Json.createObjectBuilder();
			if(!propertyValuesByField.isEmpty()) {
				JsonObjectBuilder propsObj=Json.createObjectBuilder();
				for(String prop: propertyValuesByField.keySet()) {
					JsonArrayBuilder values=Json.createArrayBuilder();
					for(String val: propertyValuesByField.get(prop)) 
						values.add(Json.createValue(val));
					propsObj.add(prop, values.build());
				}
				valsObj.add("propertyValues", propsObj.build());
			}
			return valsObj.build();
		}
	}
	
//	public static class LangValue {
//		public Resource cls;
//		public String property;
//		public Match match;
//		public int withoutLangTag=0;
//		
//		public LangValue(Resource cls, String property, Match match, int withoutLangTag) {
//			super();
//			this.cls = cls;
//			this.property = property;
//			this.match = match;
//			this.withoutLangTag = withoutLangTag;
//		}
////		public LangValue(Resource cls, String property, Match match) {
////			super();
////			this.cls = cls;
////			this.property = property;
////			this.match = match;
////		}
//		
//		@Override
//		public String toString() {
//			return "DateValue [className=" + cls + ", property=" + property + ", match=" + match + "]";
//		}		
//	}
//	public static class PropertyCount {
//		public Resource cls;
//		public String property;
//		public int count;
//		
//		public PropertyCount(Resource cls, String property, int count) {
//			super();
//			this.cls = cls;
//			this.property = property;
//			this.count = count;
//		}
//		
//		@Override
//		public String toString() {
//			return "PropertyCount [className=" + cls + ", property=" + property + ", count=" + count + "]";
//		}
//		
//	}
	public static class IdsFromSource {
		HashMap<Resource, IdsInClass> valuesByClassAndField;
		public IdsFromSource() {
			valuesByClassAndField=new HashMap<Resource, IdsInClass>();
		}
		
		public void readValuesOfSourceFromJson(JsonObject jv) {
			for(Entry<String, JsonValue> field: jv.entrySet()) {
				Resource cls=Edm.getResourceFromPrefixedName(field.getKey());
				valuesByClassAndField.put(cls, new IdsInClass(field.getValue().asJsonObject()));
			}

		}

		public void addTo(Resource cls, Property property, String value) {
			IdsInClass classEntry = getClassEntry(cls);
			classEntry.propertyValuesByField.put(Edm.getPrefixedName(property), value );
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
		
//		public List<LangValue> getAllPropertyValues() {
//			List<LangValue> vals=new ArrayList<>();
//			Set<Entry<Resource, IdsInClass>> entrySet = valuesByClassAndField.entrySet();
//			for(Entry<Resource, IdsInClass> inClass: entrySet) {
//				for (Entry<String, ArrayList<Match>> fieldResults : inClass.getValue().propertyValuesByField.entrySet()) {
//					for (Match match: fieldResults.getValue()) 
//						vals.add(new LangValue(inClass.getKey(), fieldResults.getKey(), match, 0));
//				}
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

		private IdsInClass getClassEntry(Resource cls) {
			IdsInClass inClass = valuesByClassAndField.get(cls);
			if(inClass==null) {
				inClass=new IdsInClass();
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

		public IdsInClass getValuesFor(Resource cls) {
			return valuesByClassAndField.get(cls);
		}
	}
	
	String choUri;
	IdsFromSource fromProvider=new IdsFromSource();
	
	public IdsInRecord(String choUri) {
		super();
		this.choUri = choUri;
		fromProvider=new IdsFromSource();
	}

	public IdsInRecord(JsonObject jv) {
		this(jv.getString("id"));
		JsonObject fromProviderJson = jv.getJsonObject("fromProvider");
		fromProvider.readValuesOfSourceFromJson(fromProviderJson);
	}

	public void addTo(Resource cls, Property property, String value) {
		fromProvider.addTo(cls, property, value);
	}
	
	public List<String> getAllValues() {
		List<String> vals=new ArrayList<>();
		Set<Entry<Resource, IdsInClass>> entrySet = fromProvider.valuesByClassAndField.entrySet();
		for(Entry<Resource, IdsInClass> inClass: entrySet) {
			for (Entry<String, ArrayList<String>> fieldResults : inClass.getValue().propertyValuesByField.entrySet()) {
				vals.addAll(fieldResults.getValue());
			}
		}
		return vals;
	}
	
	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		ret.add("fromProvider", fromProvider.toJson());
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}

}
