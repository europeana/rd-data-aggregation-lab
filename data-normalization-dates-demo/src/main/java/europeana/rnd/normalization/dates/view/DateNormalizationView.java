package europeana.rnd.normalization.dates.view;

import java.util.ArrayList;
import java.util.List;

import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.Source;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
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
		List<MatchView> values;
		
		public PropertyWithDates(String name, List<Match> values) {
			super();
			this.name = name;
			this.values = new ArrayList<MatchView>(values.size());
			for(Match m: values) {
				this.values.add(new MatchView(m));
			}
		}

		public String getName() {
			return name;
		}

		public List<MatchView> getValues() {
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
		if(! datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Ore.Proxy).isEmpty()) 
			classes.add(new ClassWithDates("ore:Proxy (Europeana)", datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Ore.Proxy)));
		if(! datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Ore.Proxy).isEmpty()) 
			classes.add(new ClassWithDates("ore:Proxy (Provider)", datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Ore.Proxy)));

		if(! datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.WebResource).isEmpty()) 
			classes.add(new ClassWithDates("edm:WebResource (Europeana)", datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.WebResource)));		
		if(! datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.WebResource).isEmpty()) 
			classes.add(new ClassWithDates("edm:WebResource (Provider)", datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.WebResource)));
		
		if(! datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.Agent).isEmpty()) 
			classes.add(new ClassWithDates("edm:Agent (Europeana)", datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.Agent)));		
		if(! datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.Agent).isEmpty()) 
			classes.add(new ClassWithDates("edm:Agent (Provider)", datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.Agent)));
		
		if(! datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.TimeSpan).isEmpty()) 
			classes.add(new ClassWithDates("edm:TimeSpan (Europeana)", datesInRecord.getValuesByFieldInClass(Source.EUROPEANA, Edm.TimeSpan)));		
		if(! datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.TimeSpan).isEmpty()) 
			classes.add(new ClassWithDates("edm:TimeSpan (Provider)", datesInRecord.getValuesByFieldInClass(Source.PROVIDER, Edm.TimeSpan)));

		return classes;
	}
}
