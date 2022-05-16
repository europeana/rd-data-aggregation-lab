package europeana.rnd.dataprocessing.dates.edtf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil.Jena;

public class EdtfEdmSerializer {
	public static class EdtfLevel1Type extends BaseDatatype {
	    public static final String uri = "http://id.loc.gov/datatypes/edtf/EDTF-level1";
	    public static final RDFDatatype instance = new EdtfLevel1Type();

	    static {
	    	TypeMapper.getInstance().registerDatatype(instance);
	    }
	    
	    /** private constructor - single global instance */
	    private EdtfLevel1Type() {
	        super(uri);
	    }

	    /**
	     * Convert a value of this datatype out
	     * to lexical form.
	     */
	    public String unparse(Object value) {
	    	TemporalEntity r = (TemporalEntity) value;
	        return EdtfSerializer.serialize(r);
	    }

	    /**
	     * Parse a lexical form of this datatype to a value
	     * @throws DatatypeFormatException if the lexical form is not legal
	     */
	    public Object parse(String lexicalForm) throws DatatypeFormatException {
	    	try {
		    	TemporalEntity parsed = new EdtfParser().parse(lexicalForm);
		    	return parsed;
	        } catch (ParseException e) {
	            throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
	        }
	    }
	}

	public static Model serialize(TemporalEntity edtf) {
		Model m=Jena.createModel();
		String edtfString=EdtfSerializer.serialize(edtf);
		String uri;
		try {
			uri = "#"+URLEncoder.encode(edtfString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		Resource r=m.createResource(uri);
		r.addProperty(Skos.notation, edtfString, EdtfLevel1Type.instance);
		r.addProperty(Skos.prefLabel, edtfString, "zxx");
		if(edtf.isApproximate())
			r.addProperty(Skos.note, "approximate", "en");
		if(edtf.isUncertain())
			r.addProperty(Skos.note, "uncertain", "en");
		
		Instant firstDay=edtf.getFirstDay();
		Instant lastDay=edtf.getLastDay();
		if(firstDay!=null)
			r.addProperty(Edm.begin, EdtfSerializer.serialize(firstDay));
		if(lastDay!=null)
			r.addProperty(Edm.end, EdtfSerializer.serialize(lastDay));
		
	    return m;
	}

	private static String serializeInstant(Instant edtf) {
		StringBuffer buf=new StringBuffer();
		if(edtf.getDate()!=null) {
			if(edtf.getDate().isUnkown())
				buf.append("");
			else if(edtf.getDate().isUnspecified())
				buf.append("..");
			else {
				buf.append(serializeYear(edtf.getDate()));
				if(edtf.getDate().getYear()<-9999 || edtf.getDate().getYear()>9999)
					return buf.toString();
				if (edtf.getDate().getMonth()!=null && edtf.getDate().getMonth()>0) {
					buf.append("-").append(padInt(edtf.getDate().getMonth(), 2));
					if (edtf.getDate().getDay()!=null && edtf.getDate().getDay()>0) {
						buf.append("-").append(padInt(edtf.getDate().getDay(), 2));
					}
				}
				if(edtf.getDate().isApproximate() && edtf.getDate().isUncertain())
					buf.append("%");
				else if(edtf.getDate().isApproximate() )
					buf.append("~");
				else if(edtf.getDate().isUncertain() )
					buf.append("?");
			}
		}
		if(edtf.getTime()!=null && (edtf.getTime().getHour()!=0 || edtf.getTime().getMinute()!=0 || edtf.getTime().getSecond()!=0)) {
			buf.append("T").append(padInt(edtf.getTime().getHour(),2));
			if (edtf.getTime().getMinute()!=null) {
				buf.append(":").append(padInt(edtf.getTime().getMinute(),2));
				if (edtf.getTime().getSecond()!=null) {
					buf.append(":").append(padInt(edtf.getTime().getSecond(),2));
					if (edtf.getTime().getMillisecond()!=null) {
						buf.append(".").append(padInt(edtf.getTime().getMillisecond(),3));
					}
				}
			}
		}
		return buf.toString();
	}

	private static String serializeYear(Date date) {
		if(date.getYear()<-9999 || date.getYear()>9999)
			return "Y"+date.getYear();		
		String yearStr=padInt(Math.abs(date.getYear()), 4);
		String prefix=date.getYear()<0 ? "-" : "";
		if(date.getYearPrecision()!=null) {
			switch (date.getYearPrecision()) {
			case MILLENIUM:
				return prefix+yearStr.substring(0,1)+"XXX";
			case CENTURY:
				return prefix+yearStr.substring(0,2)+"XX";
			case DECADE:
				return prefix+yearStr.substring(0,3)+"X";
			} 
		}
		return prefix+yearStr;
	}
//	private String serializeTwoDigits(Integer number) {
//		assert number>=0 && number<100;
//		if(number<10)
//			return "0"+number;
//		return String.valueOf(number);
//	}
//	private String serializeFourDigits(Integer number) {
//		assert number>=10000 && number<10000;
//		switch (key) {
//		case value:
//			
//			break;
//
//		default:
//			break;
//		}
//		if(number<10)
//			return "0"+number;
//		return String.valueOf(number);
//	}

	
	private static String padInt(int value, int paddingLength) {
	    return String.format("%0" + paddingLength + "d", value);
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println(padInt(-1000, 4));
	}
}
