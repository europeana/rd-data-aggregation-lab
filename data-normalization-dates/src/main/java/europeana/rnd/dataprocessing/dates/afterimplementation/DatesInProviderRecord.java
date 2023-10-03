package europeana.rnd.dataprocessing.dates.afterimplementation;

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
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfLists;

public class DatesInProviderRecord {
	public enum Normalised { TRUE, FALSE, UNKNOWN };
	
	public static class DateLiteralValue {
		public Property property;
		public String value;
		public Normalised normalised;
		
		public DateLiteralValue(Property property, String value, Normalised normalised) {
			super();
			this.property = property;
			this.value = value;
			this.normalised = normalised;
		}

		public DateLiteralValue(JsonObject json) {
			property=Edm.getPropertyFromPrefixedName(json.getString("property"));
			value=json.getString("value");
			normalised=Normalised.valueOf(json.getString("normalised"));
		}
		
		public JsonObject toJson() {
			JsonObjectBuilder ret=Json.createObjectBuilder();
			ret.add("property", Json.createValue(Edm.getPrefixedName(property)));
			ret.add("value", Json.createValue(value));
			ret.add("normalised", Json.createValue(normalised.name()));
			return ret.build();
		}
	}
	
	public static class DateResourceValue {
		public Property property;
		public String begin;
		public String end;
		public String uri;
		
		public DateResourceValue(Property property) {
			super();
			this.property = property;
		}

		public DateResourceValue(JsonObject json) {
			property=Edm.getPropertyFromPrefixedName(json.getString("property"));
			if(json.containsKey("uri"))
				uri=json.getString("uri");
			if(json.containsKey("begin"))
				begin=json.getString("begin");
			if(json.containsKey("end"))
				end=json.getString("end");
		}

		public JsonObject toJson() {
			JsonObjectBuilder ret=Json.createObjectBuilder();
			ret.add("property", Json.createValue(Edm.getPrefixedName(property)));
			if(!StringUtils.isEmpty(uri))
				ret.add("uri", Json.createValue(uri));
			if(!StringUtils.isEmpty(begin))
				ret.add("begin", Json.createValue(begin));
			if(!StringUtils.isEmpty(end))
				ret.add("end", Json.createValue(end));
			return ret.build();
		}
	}
	
	String choUri;
	public ArrayList<DateLiteralValue> literals=new ArrayList<DatesInProviderRecord.DateLiteralValue>();
	public ArrayList<DateResourceValue> resources=new ArrayList<DatesInProviderRecord.DateResourceValue>();
	
	public DatesInProviderRecord(String choUri) {
		super();
		this.choUri = choUri;
	}

	public DatesInProviderRecord(JsonObject jv) {
		this(jv.getString("id"));
		JsonArray resourcesJson = jv.getJsonArray("literals");
		JsonArray literalsJson = jv.getJsonArray("resources");
		
		literalsJson.forEach(val -> {
			literals.add(new DateLiteralValue(val.asJsonObject()));
		});
		resourcesJson.forEach(val -> {
			resources.add(new DateResourceValue(val.asJsonObject()));
		});
	}

	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		JsonArrayBuilder litsJson=Json.createArrayBuilder();
		literals.forEach(lit ->{
			litsJson.add(lit.toJson());
		});
		ret.add("literals", litsJson.build());
		JsonArrayBuilder resourcesJson=Json.createArrayBuilder();
		resources.forEach(res ->{
			resourcesJson.add(res.toJson());
		});
		ret.add("resources", resourcesJson.build());
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}

	public boolean isEmpty() {
		return literals.isEmpty() && resources.isEmpty();
	}

	public void add(Property predicate, Literal asLiteral, Normalised normalised) {
		DateLiteralValue val=new DateLiteralValue(predicate, asLiteral.getString(), normalised);
		literals.add(val);
	}

	public void add(Property predicate, Resource timeSpan) {
		DateResourceValue val=new DateResourceValue(predicate);
		if(RdfUtil.getTypes(timeSpan).get(0).equals(Edm.TimeSpan)) {
			if(!StringUtils.isEmpty(timeSpan.getURI()) && timeSpan.getURI().startsWith("http"))
				val.uri=timeSpan.getURI();
			Statement edmBeginSt=timeSpan.getProperty(Edm.begin);
			if(edmBeginSt!=null)
				val.begin=edmBeginSt.getObject().asLiteral().getString();
			Statement edmEndSt=timeSpan.getProperty(Edm.end);
			if(edmEndSt!=null)
				val.end=edmEndSt.getObject().asLiteral().getString();
		}
	}
}
