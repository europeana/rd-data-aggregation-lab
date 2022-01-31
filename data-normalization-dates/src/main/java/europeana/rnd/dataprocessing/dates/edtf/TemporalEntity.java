package europeana.rnd.dataprocessing.dates.edtf;

public class TemporalEntity {

	public String serialize() {
		return new EdtfSerializer().serialize(this);
	}

	@Override
	public String toString() {
		return EdtfSerializer.serialize(this);
	}
}
