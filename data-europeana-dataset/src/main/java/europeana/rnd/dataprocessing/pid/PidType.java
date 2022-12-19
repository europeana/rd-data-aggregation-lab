package europeana.rnd.dataprocessing.pid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PidType {
	PURL, DOI, ARK, HANDLE, URN;
	
	private static Pattern URL_PATTERN=Pattern.compile("https?://(?<host>[^/]+)(?<path>/.*)", Pattern.CASE_INSENSITIVE);
	
	public static PidType getPidType(String id ) {
		String idLc=id.toLowerCase();
		if(idLc.startsWith("http")) {
			Matcher matcher = URL_PATTERN.matcher(id);
			if(matcher.matches()) {
				String host=matcher.group("host").toLowerCase();
				if(host.equals("doi.org") || host.equals("dx.doi.org")) 
					return DOI;
				if(host.startsWith("handle.") || host.contains(".handle.")) 
					return HANDLE;					
				if(host.startsWith("purl.") || host.contains(".purl.")) 
					return PURL;
				String path=matcher.group("path").toLowerCase();
				if(path.startsWith("/urn:")) 
					return URN;
				if(path.startsWith("/ark:")) 
					return ARK;
			} else
				System.out.println("WARN: "+id);
		} else {
			if(idLc.startsWith("doi:")) 
				return DOI;
			if(idLc.startsWith("hdl:")) 
				return HANDLE;
			if(idLc.startsWith("ark:")) 
				return ARK;
			if(idLc.startsWith("urn:")) 
				return URN;
		}
		return null;
	}
	
}
