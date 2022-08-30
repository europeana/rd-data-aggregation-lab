package europeana.rnd.dataprocessing.language.iana;

import java.util.ArrayList;
import java.util.List;

public class Subtag {
	
	public enum SubtagType { LANGUAGE, REGION, VARIANT, EXTLANG, SCRIPT, GRANDFATHERED, REDUNDANT };
	
	SubtagType type;
	String text;
	List<String> descriptions=new ArrayList<>();
	boolean deprecated=false;
	
	public SubtagType getType() {
		return type;
	}
	public void setType(SubtagType type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<String> getDescriptions() {
		return descriptions;
	}
	public void addDescription(String description) {
		descriptions.add(description);
	}
	public boolean isDeprecated() {
		return deprecated;
	}
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
}
