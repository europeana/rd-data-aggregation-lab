package europeana.rnd.dataprocessing.language.iana;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import europeana.rnd.dataprocessing.language.LangSubtagsAnalysis;
import europeana.rnd.dataprocessing.language.iana.Subtag.SubtagType;

public class LangTagValidator {
	class LangTag {
		String language;
		String script;
		String region;
		String variant;
		
		public String getTag() {
			return language +
					(script == null ? "" : "-"+script) +
					(region == null ? "" : "-"+region) +
					(variant == null ? "" : "-"+variant) ;
		}		
	}
	
	Registry registry;

	public LangTagValidator(Registry registry) {
		super();
		this.registry = registry;
	}
	
	public ValidationReport validate(String langTag) {
		Locale loc=Locale.forLanguageTag(langTag);
		if (!registry.containsSubTag(SubtagType.LANGUAGE, loc.getLanguage()))
			return new ValidationReport(false);
		boolean partiallyInvalid=false;
		LangTag validTag=new LangTag();
		validTag.language=loc.getLanguage();
		if (!StringUtils.isEmpty(loc.getCountry())) {
			if(registry.containsSubTag(SubtagType.REGION, loc.getCountry()))
				validTag.region=loc.getCountry();
			else
				partiallyInvalid=true;
		}
		if (!StringUtils.isEmpty(loc.getScript())) {
			if(registry.containsSubTag(SubtagType.SCRIPT, loc.getScript()))
				validTag.script=loc.getScript();
			else
				partiallyInvalid=true;
		}
		if (!StringUtils.isEmpty(loc.getVariant())) {
			if(registry.containsSubTag(SubtagType.VARIANT, loc.getVariant()))
				validTag.variant=loc.getVariant();
			else
				partiallyInvalid=true;
		}
		if(!partiallyInvalid)
			return new ValidationReport(true);
		return new ValidationReport(true, !partiallyInvalid, validTag.getTag());
	}
}
