package europeana.rnd.dataprocessing.dates.extraction;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MonthMultilingual {
	HashMap<Month, HashMap<Language, HashSet<String>>> monthStringsByLanguage=
			new HashMap<Month, HashMap<Language,HashSet<String>>>();	
	
	public MonthMultilingual() {
		for(Month month: Month.values()) {
			HashMap<Language, HashSet<String>> monthValues=new HashMap<Language, HashSet<String>>();
			monthStringsByLanguage.put(month, monthValues);
			for(Language l: Language.values()) {
				HashSet<String> langValues=new HashSet<String>();
				monthValues.put(l, langValues);
				langValues.add(month.getDisplayName(TextStyle.SHORT, l.getLocale()));
				langValues.add(month.getDisplayName(TextStyle.SHORT_STANDALONE, l.getLocale()));
				langValues.add(month.getDisplayName(TextStyle.FULL, l.getLocale()));
				langValues.add(month.getDisplayName(TextStyle.FULL_STANDALONE, l.getLocale()));
			}
		}
		
	}
	
	public Set<String> getAllMonthStrings() {
		Set<String> ret=new HashSet<String>();
		for(Month month: Month.values()) {
			HashMap<Language, HashSet<String>> monthValues=monthStringsByLanguage.get(month);
			for(Language l: Language.values()) {
				ret.addAll(monthValues.get(l));
			}
		}
		return ret;
	}

	public Set<String> getMonthStrings(Month month) {
		Set<String> ret=new HashSet<String>();
		HashMap<Language, HashSet<String>> monthValues=monthStringsByLanguage.get(month);
		for(Language l: Language.values()) {
			ret.addAll(monthValues.get(l));
		}
		return ret;
	}

	
	public Integer parse(String monthName) {
		for(Month month: Month.values()) {
			HashMap<Language, HashSet<String>> monthValues=monthStringsByLanguage.get(month);
			for(Language l: Language.values()) {
				if (monthValues.get(l).contains(monthName))
					return month.getValue();
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		new MonthMultilingual();
		for(Month month: Month.values()) {
			System.out.println("MONTH: "+month);
			for(Language l: Language.values()) {
				System.out.println("LANG: "+l);
				System.out.println(month.getDisplayName(TextStyle.SHORT, l.getLocale()));
				System.out.println(month.getDisplayName(TextStyle.SHORT_STANDALONE, l.getLocale()));
				System.out.println(month.getDisplayName(TextStyle.FULL, l.getLocale()));
				System.out.println(month.getDisplayName(TextStyle.FULL_STANDALONE, l.getLocale()));
			}
		}
	}
}
