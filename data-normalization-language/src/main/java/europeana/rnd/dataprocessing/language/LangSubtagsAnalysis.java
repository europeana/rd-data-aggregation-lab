package europeana.rnd.dataprocessing.language;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import europeana.rnd.dataprocessing.language.iana.LangTagValidator;

public class LangSubtagsAnalysis {
	
	boolean subTag=false;
	boolean country=false;
	boolean variant=false;
	boolean script=false;
	boolean extention=false;
	
	public LangSubtagsAnalysis(String langTag) {
		Locale loc=Locale.forLanguageTag(langTag);
		country=!StringUtils.isEmpty(loc.getCountry());
		variant=!StringUtils.isEmpty(loc.getVariant());
		script=!StringUtils.isEmpty(loc.getScript());
		extention=!loc.getExtensionKeys().isEmpty();
		subTag=country || variant || script || extention;
	}
	
}
