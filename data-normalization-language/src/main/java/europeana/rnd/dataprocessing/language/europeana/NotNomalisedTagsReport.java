package europeana.rnd.dataprocessing.language.europeana;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.language.europeana.EuropeanaLangTagValueWriter.LangTagValue;

public class NotNomalisedTagsReport {
	
	public class ClassReport {
		HashMap<String, PropertyReport> properties=new HashMap<>();

		public void add(LangTagValue tagValue) {
			PropertyReport propReport = properties.get(tagValue.property);
			if(propReport==null) {
				propReport=new PropertyReport();
				properties.put(tagValue.property, propReport);
			}
			propReport.add(tagValue);
		}
		
	}

	public class PropertyReport {
		HashMap<String, TagValueReport> tags=new HashMap<>();

		public void add(LangTagValue tagValue) {
			TagValueReport tagReport = tags.get(tagValue.value);
			if(tagReport==null) {
				tagReport=new TagValueReport(tagValue.value);
				tags.put(tagValue.value, tagReport);
			}
			tagReport.add(tagValue);
		}
		
	}

	public class TagValueReport {
		String tagValue;
		HashSet<String> distinctResources=new HashSet<String>();
		HashSet<String> distinctEntities=new HashSet<String>();
		HashSet<String> distinctChos=new HashSet<String>();
		int occurrences=0;

		public TagValueReport(String tagValue) {
			super();
			this.tagValue = tagValue;
		}
		
		public void add(LangTagValue langTagValue) {
			distinctResources.add(langTagValue.text);
			distinctEntities.add(langTagValue.subjectUri);
			distinctChos.add(langTagValue.choUri);
			occurrences++;
		}
	}
	
	HashMap<String, ClassReport> classes=new HashMap<>();

	public void add(LangTagValue langTag) {
		ClassReport classReport = classes.get(langTag.rdfType);
		if(classReport==null) {
			classReport=new ClassReport();
			classes.put(langTag.rdfType, classReport);
		}
		classReport.add(langTag);
	}
}
