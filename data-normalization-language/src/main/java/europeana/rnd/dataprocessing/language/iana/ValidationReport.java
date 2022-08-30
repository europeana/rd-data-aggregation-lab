package europeana.rnd.dataprocessing.language.iana;

public class ValidationReport {

	boolean valid;
	boolean partiallyValid;
	String validTag;
	
	public ValidationReport(boolean valid) {
		super();
		this.valid = valid;
		this.partiallyValid = valid;
	}
	
	public ValidationReport(boolean valid, boolean partiallyValid, String validTag) {
			super();
			this.valid = valid;
		this.partiallyValid = partiallyValid;
		this.validTag = validTag;
	}
	
	public boolean isValid() {
		return valid;
	}
	public boolean isPartiallyValid() {
		return partiallyValid;
	}
	public String getValidTag() {
		return validTag;
	}
	
		
}
