package europeana.rnd.dataprocessing.language.detection;

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

import europeana.rnd.dataprocessing.language.Match;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

public class TextInRecord {
	
	public class TaggedText {
		Property property;
		String text;
		String lang;
		
		public TaggedText(Property prop, String text, String lang) {
			super();
			this.property=prop;
			this.text = text;
			this.lang = lang;
		}

		public TaggedText(JsonObject txtJson) {
			property=Edm.getPropertyFromPrefixedName(txtJson.getString("property"));
			text=txtJson.getString("text");
			if(txtJson.containsKey("lang"))
				lang=txtJson.getString("lang");
				
		}

		public Property getProperty() {
			return property;
		}

		public void setProperty(Property property) {
			this.property = property;
		}


		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public String getPropertyAsString() {
			return Edm.getPrefixedName(property);
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public JsonValue toJson() {
			JsonObjectBuilder txtBuilder=Json.createObjectBuilder();
			txtBuilder.add("property", getPropertyAsString());
			txtBuilder.add("text", getText());
			if(!StringUtils.isEmpty(getLang()))
				txtBuilder.add("lang", getLang());
			return txtBuilder.build();
		}
		
	}

	String choUri;
	String edmLanguage;
	List<TaggedText> text;
	
	public TextInRecord(String choUri) {
		super();
		this.choUri = choUri;
		text=new ArrayList<>();
	}

	public TextInRecord(JsonObject jv) {
		this(jv.getString("id"));
		edmLanguage=jv.getString("edmLanguage");
		text=new ArrayList<TaggedText>();
		if (jv.containsKey("text")) {
			JsonArray jaVals = jv.get("text").asJsonArray();
			for(JsonValue txtVal: jaVals) {
				JsonObject txtJson=txtVal.asJsonObject();
				text.add(new TaggedText(txtJson));  
			}	
		}
	}
	
	public JsonObject toJson() {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("id", Json.createValue(choUri));
		if(!StringUtils.isEmpty(choUri))
			ret.add("edmLanguage", Json.createValue(edmLanguage));
		JsonArrayBuilder textArray = Json.createArrayBuilder();
		for(TaggedText tt: text) {
			textArray.add(tt.toJson());
		}
		ret.add("text", textArray.build());
		return ret.build();
	}

	public String getChoUri() {
		return choUri;
	}
	
	
	public void addText(Property predicate, Literal literal) {
		text.add(new TaggedText(predicate, literal.getString(), literal.getLanguage()));
	}

	
	
	public String getEdmLanguage() {
		return edmLanguage;
	}

	public void setEdmLanguage(String edmLanguage) {
		this.edmLanguage = edmLanguage;
	}

	public List<TaggedText> getText() {
		return text;
	}

	
}
