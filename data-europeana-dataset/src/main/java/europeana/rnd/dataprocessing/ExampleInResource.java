package europeana.rnd.dataprocessing;

public class ExampleInResource {
	String value;
	String uri;
	
	public ExampleInResource(String value, String uri) {
		super();
		this.value = value;
		this.uri = uri;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
