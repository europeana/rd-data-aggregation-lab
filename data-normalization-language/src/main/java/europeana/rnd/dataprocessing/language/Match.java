package europeana.rnd.dataprocessing.language;

public class Match {
	boolean hasCountry=false;
	boolean hasVariant=false;
	boolean hasScript=false;
	boolean hasExtension=false;
	boolean hasSubtags=false;
	String input;
	String normalized;
	
	public Match(String input, String extracted) {
		super();
		this.input = input;
		this.normalized = extracted;
	}
	
	public Match(String input) {
		this.input = input;		
	}

	public String getInput() {
		return input;
	}
	public String getNormalized() {
		return normalized;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setNormalized(String extracted) {
		this.normalized = extracted;
	}

	public boolean isHasCountry() {
		return hasCountry;
	}

	public void setHasCountry(boolean hasCountry) {
		this.hasCountry = hasCountry;
	}

	public boolean isHasVariant() {
		return hasVariant;
	}

	public void setHasVariant(boolean hasVariant) {
		this.hasVariant = hasVariant;
	}

	public boolean isHasScript() {
		return hasScript;
	}

	public void setHasScript(boolean hasScript) {
		this.hasScript = hasScript;
	}

	public boolean isHasExtension() {
		return hasExtension;
	}

	public void setHasExtension(boolean hasExtension) {
		this.hasExtension = hasExtension;
	}

	public boolean isHasSubtags() {
		return hasSubtags;
	}

	public void setHasSubtags(boolean hasSubtags) {
		this.hasSubtags = hasSubtags;
	}
}
