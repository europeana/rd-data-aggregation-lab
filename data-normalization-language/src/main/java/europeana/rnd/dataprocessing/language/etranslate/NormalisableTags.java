package europeana.rnd.dataprocessing.language.etranslate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalisableTags {

	public static Map<String, String> TAG_MAP=new HashMap<String, String>() {{
		put("eng", "en");
		put("fre", "fr");
		put("ger", "de");
		put("deu", "de");
		put("ita", "it");
		put("gre", "el");
		put("est", "et");
		put("hun", "hu");
		put("swe", "sv");
		put("nld", "nl");
		put("pol", "pl");
		put("slk", "sk");
		put("bul", "bg");
		put("fin", "fi");
		put("IS", "is");
		put("rus", "ru");
		put("NOR", "no");
		put("IT", "it");
		put("PL", "pl");
		put("FR", "fr");
		put("NL", "nl");
		put("hrv", "hr");
		put("LA", "la");
		put("slv", "sl");
		put("oci", "oc");
		put("slo", "sk");
		put("SI", "si");
		put("lat", "la");
		put("tur", "tr");
		put("jpn", "ja");
		put("EN", "en");
		put("ara", "ar");
		put("hbs", "sh");
		put("dut", "nl");
		put("spa", "es");
		put("chi", "zh");
		put("fra", "fr");
		put("lav", "lv");
		put("isl", "is");
		put("geo", "ka");
		put("ice", "is");
		put("lit", "lt");
		put("mac", "mk");
		put("ukr", "uk");
		put("rum", "ro");
		put("dan", "da");
		put("mkd", "mk");
		put("cze", "cs");
		put("urd", "ur");
		put("DE", "de");
		put("srp", "sr");
		put("por", "pt");
		put("alb", "sq");
		put("RU", "ru");
		put("nor", "no");
		put("Hani", "hni");
		put("arm", "hy");
		put("yid", "yi");
		put("el ", "el");
		put("ell", "el");
		put("vie", "vi");
		put("tir", "ti");
		put("ces", "cs");
		put("bre", "br");
		put("roh", "rm");
		put("epo", "eo");
		put("per", "fa");		
	}};
	
	private static Map<Pattern, String> PATTERN_MAP=new HashMap<Pattern, String>() {{
		for(Entry<String, String> lang: TAG_MAP.entrySet()) 
			put(Pattern.compile(" xml:lang\\s*=\\s*\""+lang.getKey()+"\""), " xml:lang=\""+lang.getValue()+"\"");			
	}};
	
	
	public static String applyPatterns(String edmXml) {
//		boolean changed=false;
		for(Entry<Pattern, String> lang: PATTERN_MAP.entrySet()) {
			Matcher m = lang.getKey().matcher(edmXml);
			if(m.find()) {
				edmXml = m.replaceAll(lang.getValue());
//				System.out.println("normalized: "+lang.getValue());
//				changed=true;
			}
		}
//		if(changed)
//			System.out.println(edmXml);
		return edmXml;
	}
	
	
	
}
