package europeana.rnd.dataprocessing.scripts.sgoab;

public class Annotation {
	public String label;
	public String confidence;
	public int x;
	public int y;
	public int w;
	public int h;
	public Annotation(String label) {
		this.label=label;
	}
	@Override
	public String toString() {
		return label+"("+confidence+")";
	}
}