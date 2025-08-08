package europeana.rnd.dataprocessing.pid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PidType {
	PURL, DOI, ARK, HANDLE, URN;
	
	private static Pattern URL_PATTERN=Pattern.compile("https?://(?<host>[^/]+)(?<path>/.*)", Pattern.CASE_INSENSITIVE);
	private static Pattern ARK_DOMAIN_PATTERN=Pattern.compile("/?ark\\:/([^/]+)[/].*", Pattern.CASE_INSENSITIVE);
	private static Pattern URN_NBN_DOMAIN_PATTERN=Pattern.compile("/?urn\\:(nbn:[^\\:\\-]+)[\\:\\-].*", Pattern.CASE_INSENSITIVE);
	private static Pattern URN_DOMAIN_PATTERN=Pattern.compile("/?urn\\:([^\\:/]+)[\\:/].*", Pattern.CASE_INSENSITIVE);
	private static Pattern HANDLE_DOMAIN_PATTERN=Pattern.compile("/([^/\\.]+)[/\\.].*", Pattern.CASE_INSENSITIVE);
	
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
			} 
//			else
//				System.out.println("WARN: "+id);
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
	
	public String getCanonicalForm(String id) {
		String idLc=id.toLowerCase();
		if(idLc.startsWith("http")) {
			Matcher matcher = URL_PATTERN.matcher(id);
			if(matcher.matches()) {
				String path=matcher.group("path").toLowerCase();
				switch (this) {
				case ARK:
					String norm=path.substring(1).toLowerCase();
					if(norm.startsWith("ark:/"))
						norm="ark:"+norm.substring("ark:/".length());
					norm=norm.replaceAll("\\-", "");
					//TODO:
//					 Normalization of a received ARK for the purpose of octet-by-octet
//					   equality comparison with another ARK consists of the following steps.
//
//					   1.  The NMA part (eg, everything from an initial "https://" up to the
//					       first occurrence of "/ark:"), if present is removed.
//
//					Kunze & Bermès            Expires 20 April 2023                [Page 22]
//					Internet-Draft                     ARK                      October 2022
//
//					   2.  Any URI query string is removed (everything from the first
//					       literal '?' to the end of the string).
//
//					   3.  The first case-insensitive match on "ark:/" or "ark:" is
//					       converted to "ark:" (replacing any uppercase letters and removing
//					       any terminal '/').
//
//					   4.  Any uppercase letters in the NAAN are converted to lowercase.
//
//					   5.  In the string that remains, the two characters following every
//					       occurrence of '%' are converted to uppercase.  The case of all
//					       other letters in the ARK string must be preserved.
//
//					   6.  All hyphens are removed.  Implementors should be aware that non-
//					       ASCII hyphen-like characters (eg, U+2010 to U+2015) may arrive in
//					       the place of hyphens and, if they wish, remove them.
//
//					   7.  If normalization is being done as part of a resolution step, and
//					       if the end of the remaining string matches a known inflection,
//					       the inflection is noted and removed.
//
//					   8.  Structural characters (slash and period) are normalized: initial
//					       and final occurrences are removed, and two structural characters
//					       in a row (e.g., // or ./) are replaced by the first character,
//					       iterating until each occurrence has at least one non-structural
//					       character on either side.
//
//					   9.  If there are any components with a period on the left and a slash
//					       on the right, either the component and the preceding period must
//					       be moved to the end of the Name part or the ARK must be thrown
//					       out as malformed.
					return norm;
				case HANDLE:
					//TODO: see DOI
					return "hdl:"+path.substring(1).toLowerCase();					
				case URN:
					//TODO:
					//           1. normalize the case of the leading "urn:" token
//			           2. normalize the case of the NID
//			           3. normalizing the case of any %-escaping
					return path.substring(1).toLowerCase();					
				case DOI:
					//TODO
//					Every identifier, as used in DO-IRP, consists of two parts: its prefix and a unique suffix under the
//					prefix:
//						The prefix and suffix are separated by the ASCII character “/”. The collection of suffixes under a
//						prefix defines the local identifier space for that prefix. Each suffix must be unique under its local
//						identifier space. The combination of a unique prefix and suffix under that prefix ensures that any
//						identifier is unique within the context of the DO-IRP.
//						Prefixes are defined in a hierarchical fashion resembling a tree structure. Each node and leaf of the
//						tree is given a label that corresponds to a prefix segment. DO-IRP identifier prefixes are constructed
//						left to right, concatenating the labels from the root of the tree to the node that represents the
//						4
//						prefix. Each label is separated by the octet used for ASCII character “.” (0x2E). The dot “.” is known
//						as a delimiter.
//						The identifier syntax definition is described in ABNF notation below:
//						<Identifier> = <Prefix> "/" <Suffix>
//						<Prefix> = *(<Prefix> ".") <PrefixSegment>
//						<PrefixSegment> = 1*(%x00-2D / %x30-FF)
//						 ; any octets that map to UTF-8 encoded
//						; Unicode characters except
//						 ; octets '0x2E' and '0x2F' (which
//						 ; correspond to the ASCII characters '.',
//						 ; and '/').
//						<Suffix> = *(%x00-FF)
//						 ; any octets that map to UTF-8 encoded
//						 ; Unicode characters
//						Three examples of identifiers are 35.1234/abc, 35.1234/HQ, and 0.NA/35.1234 – these example
//						identifiers are hypothetical and are used in this documentation for illustration only.
//						Identifiers may consist of any printable characters from Unicode, which is the exact character set
//						defined by the Universal Character Set (UCS) of ISO/IEC 10646 [4]. The Unicode character set
//						encompasses most characters used in every major language written today. To allow compatibility
//						with most of the existing systems and to prevent ambiguity among different encodings, the DO-IRP
//						mandates UTF-8 [4] to be the only encoding used for identifiers. The UTF-8 encoding preserves any
//						ASCII encoded names so as to allow maximum compatibility with existing systems without causing
//						conflict.
//						Prefixes are case-insensitive (for ASCII letters), and, by default, identifiers are case sensitive.
//						However, any individual DO-IRP service may define its identifier space such that ASCII characters
//						within any identifier under that identifier space are case insensitive. Each identifier associated with
//						a digital object will resolve to an identifier record. A user that requests resolution of an identifier
//						will normally be provided with the identifier record. Most identifier records are intended to be
//						made publicly available, but some may be restricted at the discretion of those creating the
//						identifier records, in which case the resolution response should indicate explicitly that the identifier
//						record is not available.
//					
					return "doi:"+path.substring(1).toLowerCase();					
				case PURL:
					//TODO: protocol and domain to lowercase, http/https
					return id;
				}
			}
		} else {
			if(this == ARK) {
				String norm=id.toLowerCase();
				if(norm.startsWith("ark:/"))
					norm="ark:"+norm.substring("ark:/".length());
				norm=norm.replaceAll("\\-", "");
				return norm;
			} else
				return id.toLowerCase();
		}
		throw new RuntimeException("ERROR in URL: "+id);
	} 
	
	public String getDomain(String id ) {
		String idLc=id.toLowerCase(); 
		if(idLc.startsWith("http")) {
			Matcher matcher = URL_PATTERN.matcher(id);
			if(matcher.matches()) {
				String path=matcher.group("path").toLowerCase();
				switch (this) {
				case ARK:
					matcher=ARK_DOMAIN_PATTERN.matcher(path);
					if(matcher.matches()) 
						return matcher.group(1);
					break;
				case HANDLE:
					matcher=HANDLE_DOMAIN_PATTERN.matcher(path);
					if(matcher.matches()) 
						return matcher.group(1);
					break;
				case URN:
					matcher=URN_NBN_DOMAIN_PATTERN.matcher(path);
					if(matcher.matches()) 
						return matcher.group(1).toLowerCase();
					matcher=URN_DOMAIN_PATTERN.matcher(path);
					if(matcher.matches()) 
						return matcher.group(1).toLowerCase();
					else
						System.out.println("URN domain not matching: "+ path);
					break;
				case PURL:
					String host=matcher.group("host").toLowerCase();
					return host;
				case DOI:
					return null;
				}
			} else
				System.out.println("WARN: "+id);
		} else {
			Matcher matcher;
			switch (this) {
			case ARK:
				matcher=ARK_DOMAIN_PATTERN.matcher(id);
				if(matcher.matches()) 
					return matcher.group(1);
				break;
			case HANDLE:
				matcher=HANDLE_DOMAIN_PATTERN.matcher(id);
				if(matcher.matches()) 
					return matcher.group(1);
				break;
			case URN:
				matcher=URN_NBN_DOMAIN_PATTERN.matcher(id);
				if(matcher.matches()) 
					return matcher.group(1).toLowerCase();
				matcher=URN_DOMAIN_PATTERN.matcher(id);
				if(matcher.matches()) {
//					System.out.println(id+" -- "+matcher.group(1));
					return matcher.group(1).toLowerCase();
				}
				break;
			case DOI:
			case PURL:
				return null;
			}
		}
		return null;
	}
}
