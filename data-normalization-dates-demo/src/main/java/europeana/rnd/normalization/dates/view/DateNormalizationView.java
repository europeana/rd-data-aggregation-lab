package europeana.rnd.normalization.dates.view;

import java.util.ArrayList;
import java.util.List;

import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import inescid.util.datastruct.MapOfLists;

public class DateNormalizationView {
	public class ClassWithDates {
		String name;
		MapOfLists<String, Match> valuesByField;
		
		public ClassWithDates(String name, MapOfLists<String, Match> valuesByField) {
			this.name = name;
			this.valuesByField=valuesByField;
		}

		public List<PropertyWithDates> getProperties(){
			List<PropertyWithDates> props=new ArrayList<DateNormalizationView.PropertyWithDates>();
			for(String property: valuesByField.keySet()) {
				props.add(new PropertyWithDates(property, valuesByField.get(property)));
			}
			return props;
		}

		public String getName() {
			return name;
		}
		
	}

	public class PropertyWithDates {
		String name;
		List<Match> values;
		
		public PropertyWithDates(String name, List<Match> values) {
			super();
			this.name = name;
			this.values = values;
		}

		public String getName() {
			return name;
		}

		public List<Match> getValues() {
			return values;
		}
	}
	
	DatesInRecord datesInRecord;
	
	public DateNormalizationView(DatesInRecord datesInRecord) {
		this.datesInRecord = datesInRecord;
	}
	
	public String getUri() {
		return datesInRecord.getChoUri();
	}
	
	public List<ClassWithDates> getClassesWithDates() {
		List<ClassWithDates> classes=new ArrayList<DateNormalizationView.ClassWithDates>();
		if(! datesInRecord.getValuesByFieldInEuropeanaProxy().isEmpty()) 
			classes.add(new ClassWithDates("ore:Proxy (Europeana)", datesInRecord.getValuesByFieldInEuropeanaProxy()));
		if(! datesInRecord.getValuesByFieldInProviderProxy().isEmpty()) 
			classes.add(new ClassWithDates("ore:Proxy (Provider)", datesInRecord.getValuesByFieldInProviderProxy()));
		if(! datesInRecord.getValuesByFieldInWebResources().isEmpty()) 
			classes.add(new ClassWithDates("edm:Web Resource", datesInRecord.getValuesByFieldInWebResources()));
		if(! datesInRecord.getValuesByFieldInAgents().isEmpty()) 
			classes.add(new ClassWithDates("foaf:Agent", datesInRecord.getValuesByFieldInAgents()));
		if(! datesInRecord.getValuesByFieldInTimeSpans().isEmpty()) 
			classes.add(new ClassWithDates("edm:TimeSpan", datesInRecord.getValuesByFieldInTimeSpans()));
		return classes;
	}
}
