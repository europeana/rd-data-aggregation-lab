package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cleaner {
	public class CleanResult {
		CleanId cleanOperation;
		String cleanedValue;
		public CleanResult(CleanId cleanOperation, String cleanedValue) {
			super();
			this.cleanOperation = cleanOperation;
			this.cleanedValue = cleanedValue;
		}
		public CleanId getCleanOperation() {
			return cleanOperation;
		}
		public String getCleanedValue() {
			return cleanedValue;
		}
	}

	Pattern patInitialTextA=Pattern.compile("^\\s*[^\\s:]+:\\s*");
	Pattern patInitialTextB=Pattern.compile("^\\s*\\([^\\)]+\\)\\s*");
	Pattern patEndingText=Pattern.compile("\\s*\\(.+\\)\\s*$");
	Pattern patEndingDot=Pattern.compile("\\s*\\.\\s*$");
//	Pattern patEndingText=Pattern.compile("\\s*\\([^//)]+\\)\\s*$");
	Pattern patEndingSquareBracket=Pattern.compile("\\s*\\]\\s*$");
	Pattern patSquareBrackets=Pattern.compile("\\[([^\\]]+)\\]");
	Pattern patCa=Pattern.compile("(circa|CA\\.?|C\\.)\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patSquareBracketsAndCa=Pattern.compile("\\[(circa|CA\\.?|C\\.)\\s*([^\\]]+)\\]",Pattern.CASE_INSENSITIVE);
	
	Pattern patEndingTextSquareBrackets=Pattern.compile("\\s*\\[.+\\]\\s*$");
	
	public CleanResult clean1st(String value) {
		Matcher m = patInitialTextA.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.INITIAL_TEXT, m.replaceFirst(""));
		m = patInitialTextB.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.INITIAL_TEXT, m.replaceFirst(""));
		m = patEndingText.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.ENDING_TEXT, m.replaceFirst(""));
		m = patSquareBracketsAndCa.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.SQUARE_BRACKETS_AND_CIRCA, m.replaceAll("$2"));
		m = patSquareBrackets.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.SQUARE_BRACKETS, m.replaceAll("$1"));
		m = patCa.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.CIRCA, m.replaceAll(""));
		m = patEndingSquareBracket.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.SQUARE_BRACKET_END, m.replaceAll(""));
		m = patEndingDot.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.ENDING_TEXT, m.replaceFirst(""));
		return null;
	}
	public CleanResult clean2nd(String value) {
		Matcher m = patEndingTextSquareBrackets.matcher(value);
		if(m.find()) 
			return new CleanResult(CleanId.ENDING_TEXT, m.replaceFirst(""));
		return null;
	}
}
