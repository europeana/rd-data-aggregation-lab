package europeana.rnd.dataprocessing.pid;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

import europeana.rnd.dataprocessing.pid.PidSchemes.PidMatchResult;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
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

		public void remove(Resource cls) {
			valuesByClassAndField.remove(cls);
		}
	}
	
	public class DetectedPid {
		public DetectedPid(PidType type, PidScheme scheme, String id, String field) {
			this.type=type;
			this.scheme=scheme;
			this.id=id;
			this.field=field;
		}
		public String field;
		public String id;
		public PidType type;
		public PidScheme scheme;
		
		@Override
		public boolean equals(Object obj) {
			DetectedPid pid = (DetectedPid)obj;
			if(scheme==null)
			  return type==pid.type && type.getCanonicalForm(id).equals(type.getCanonicalForm(pid.id));
			return scheme.equals(pid.scheme) && scheme.getCanonicalForm(id).equals(scheme.getCanonicalForm(pid.id));
		}
		
		@Override
		public int hashCode() {
		  if(type!=null)
		    return type.getCanonicalForm(id).hashCode();
		  return scheme.getCanonicalForm(id).hashCode();
		}

		public String getCanonicalForm() {
			return type.getCanonicalForm(id);
		}
	}
	
	String choUri;
	String dataProvider;
	String provider;
	IdsFromSource fromProvider=new IdsFromSource();
	ContentTier contentTier;
	
	public IdsInRecord(String choUri) {
		super();
		this.choUri = choUri;
		fromProvider=new IdsFromSource();
	}

	public IdsInRecord(JsonObject jv) {
		this(jv.getString("id"));
		if(jv.containsKey("dataProvider"))
			dataProvider=jv.getString("dataProvider");
		if(jv.containsKey("provider"))
			provider=jv.getString("provider");
		if(jv.containsKey("contentTier"))
			contentTier=ContentTier.valueOf(jv.getString("contentTier"));
		JsonObject fromProviderJson = jv.getJsonObject("fromProvider");
		fromProvider.readValuesOfSourceFromJson(fromProviderJson);
	}

	public void addTo(Resource cls, Property property, String value) {
		fromProvider.addTo(cls, property, value);
	}
	
	
	public List<Entry<String, String>> getAllValues() {
		List<Entry<String, String>> vals=new ArrayList<>();
		Set<Entry<Resource, IdsInClass>> entrySet = fromProvider.valuesByClassAndField.entrySet();
		for(Entry<Resource, IdsInClass> inClass: entrySet) {
			for (Entry<String, ArrayList<String>> fieldResults : inClass.getValue().propertyValuesByField.entrySet()) {
				for(String val: fieldResults.getValue())
					vals.add(new AbstractMap.SimpleEntry<String, String>(fieldResults.getKey(), val));
			}
		}
		return vals;
	}
	
	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		ret.add("dataProvider", Json.createValue(dataProvider));
		ret.add("provider", Json.createValue(provider));
		if(contentTier!=null)
		  ret.add("contentTier", Json.createValue(contentTier.name()));
		ret.add("fromProvider", fromProvider.toJson());
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}

	public void remove(Resource cls) {
		fromProvider.remove(cls);
	}

	public List<DetectedPid> getAllPids() {
		List<DetectedPid> ret=new ArrayList<IdsInRecord.DetectedPid>();
		for(Entry<String, String> fieldAndId: getAllValues()) {
			String id=fieldAndId.getValue();
			String field=fieldAndId.getKey();
			PidType pidType = PidType.getPidType(id);
			if(pidType!=null) {
				ret.add(new DetectedPid(pidType, null, id, field));
			}
		}
		return ret;
	}

	public List<DetectedPid> getAllPidsUsingRegistry() {
	  List<DetectedPid> ret=new ArrayList<IdsInRecord.DetectedPid>();
	  for(Entry<String, String> fieldAndId: getAllValues()) {
	    String id=fieldAndId.getValue();
	    String field=fieldAndId.getKey();
      PidMatchResult matchedSchema = PidSchemes.matchPidSchema(id);
	    if(matchedSchema!=null) 
	      ret.add(new DetectedPid(null, matchedSchema.schema(),  id, field));
	  }
	  return ret;
	}
	
	public Set<PidType> getAllPidTypes() {
		Set<PidType> ret=new HashSet<PidType>();
		for(Entry<String, String> fieldAndId: getAllValues()) {
			String id=fieldAndId.getValue();
			PidType pidType = PidType.getPidType(id);
			if(pidType!=null) 
				ret.add(pidType);
		}
		return ret;
	}

	public Set<PidScheme> getAllPidShemes() {
	  Set<PidScheme> ret=new HashSet<>();
	  for(DetectedPid pid: getAllPidsUsingRegistry()) {
	    String id=pid.id;
	    if(pid.scheme!=null) 
	      ret.add(pid.scheme);
	  }
	  return ret;
	}

	public ContentTier getContentTier() {
		return contentTier;
	}

	public void setContentTier(ContentTier contentTier) {
		this.contentTier = contentTier;
	}

	public String getDataProvider() {
		return dataProvider;
	}

	public String getProvider() {
		return provider;
	}
	
	public void setDataProvider(String dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}
