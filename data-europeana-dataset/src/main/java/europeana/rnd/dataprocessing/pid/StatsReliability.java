package europeana.rnd.dataprocessing.pid;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.MapOfIntsWithExamples;
import europeana.rnd.dataprocessing.pid.IdStats.MultiplePidStats;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import inescid.util.datastruct.MapOfInts;

public class StatsReliability {
	
	HashSet<String> urnIanaNsid=new HashSet<String>() {{
		add("3gpp");
		add("3gpp2");
		add("adid");
		add("alert");
		add("bbf");
		add("broadband-forum-org");
		add("cablelabs");
		add("ccsds");
		add("cdx");
		add("cgi");
		add("clei");
		add("ddi");
		add("dev");
		add("dgiwg");
		add("dslforum-org");
		add("dvb");
		add("ebu");
		add("eic");
		add("eidr");
		add("epc");
		add("epcglobal");
		add("etsi");
		add("eurosystem");
		add("example");
		add("fdc");
		add("fipa");
		add("gdst");
		add("geant");
		add("globus");
		add("gsma");
		add("hbbtv");
		add("ieee");
		add("ietf");
		add("iptc");
		add("isan");
		add("isbn");
		add("iso");
		add("issn");
		add("itu");
		add("ivis");
		add("lei");
		add("lex");
		add("liberty");
		add("mace");
		add("mef");
		add("meta");
		add("mpeg");
		add("mrn");
		add("nato");
		add("nbn");
		add("nena");
		add("newsml");
		add("nfc");
		add("nzl");
		add("oasis");
		add("ogc");
		add("ogf");
		add("oid");
		add("oipf");
		add("oma");
		add("onem2m");
		add("onf");
		add("pin");
		add("publicid");
		add("pwid");
		add("reso");
		add("s1000d");
		add("schac");
		add("service");
		add("smpte");
		add("swift");
		add("tva");
		add("uci");
		add("ucode");
		add("uuid");
		add("web3d");
		add("xmlorg");
		add("xmpp");
	}};
	
	HashSet<String> resolvableUrns=new HashSet<String>() {{
		add("nbn:de");
		add("nbn:at");
		add("nbn:fi");
		add("nbn:no");
		add("nbn:nl");
		add("nbn:hr");
//		add("nbn:sk");//not working at www.nbn.sk/resolver/urn_resolver.html
		add("nbn:cz");//https://resolver.nkp.cz/
//		add("nbn:lv"); Could not find resolver
//		add("nbn:fr"); Could not find resolver
//		add("nbn:pt"); Could not find resolver
//		add("nbn:es"); Could not find resolver
//		add("nbn:se"); resolvable at http://nbn-resolving.de/
//		add("nbn:ch"); resolvable at http://nbn-resolving.de/
	}};
	
	
	Pattern arkPattern=Pattern.compile("ark:[0123456789bcdfghjkmnpqrstvwxz]{2,5}/[^\\s]+"); 
	//invalid NAANs: 12345 and 99999
	
	Pattern doiPattern=Pattern.compile("doi:10\\.[^/\\s]+/[^\\s]+"); 

	Pattern handlePattern=Pattern.compile("hdl:[^/\\s]+/[^\\s]+"); 
	
	MapOfIntsWithExamples<PidType, String> invalidByType=new MapOfIntsWithExamples<PidType, String>(50);
	MapOfIntsWithExamples<PidType, String> unresolvableByType=new MapOfIntsWithExamples<PidType, String>(50);
	MapOfIntsWithExamples<PidType, String> locationDependentByType=new MapOfIntsWithExamples<PidType, String>(50);
	MapOfIntsWithExamples<PidType, String> unknownPersistencePolicyByType=new MapOfIntsWithExamples<PidType, String>(50);
	MapOfIntsWithExamples<PidType, String> okByType=new MapOfIntsWithExamples<PidType, String>(50);

	MultiplePidStats multiplePidStats=new MultiplePidStats();
    MapOfInts<PidType> statsByTypeUnique=new MapOfInts<PidType>();
    long countUnique=0;
    long countWithoutPid=0;
// invalid
// location dependent
// unresolvable
// persistence policy unknown
// ok
	
	public boolean analise(String pid, PidType type) {
		String canonicalForm = type.getCanonicalForm(pid);
		boolean isValid=false;
		switch (type) {
		case ARK:
			isValid=arkPattern.matcher(canonicalForm).matches();
			if(isValid && (canonicalForm.startsWith("ark:12345/") || canonicalForm.startsWith("ark:99999/")))
				isValid=false;
			break;
		case DOI:
			isValid=doiPattern.matcher(canonicalForm).matches();
			break;
		case HANDLE:
			isValid=handlePattern.matcher(canonicalForm).matches();
			break;
		case PURL:
			isValid=true;
			break;
		case URN:
			String nsid = type.getDomain(pid);
			isValid=urnIanaNsid.contains(nsid);
			break;
		}
		if(!isValid) {
			invalidByType.addTo(type, pid);
			return false;
		}
		
		if(type==PidType.URN) {
			String domain = type.getDomain(canonicalForm);
			if(!pid.toLowerCase().startsWith("http")) {
				unresolvableByType.addTo(type, pid);
				return false;
			} else {
				if(!resolvableUrns.contains(domain)){
					unresolvableByType.addTo(type, pid);
					return false;					
				}
			}
			if(!domain.startsWith("nbn:")) {
				unknownPersistencePolicyByType.addTo(type, pid);
				return false;
			}
		}else if(type==PidType.PURL) {
			unknownPersistencePolicyByType.addTo(type, pid);
			return false;
		}		
		okByType.addTo(type, pid);
		return true;
	}

	public void addNonUnique(Set<DetectedPid> pids, String choUri) {
		multiplePidStats.add(pids, choUri);
	}
	
	public void incrementRecordWithPid() {
		countUnique++;
	}
	public void incrementRecordWithoutPid() {
		countWithoutPid++;
	}

	public void incrementType(PidType type) {
		statsByTypeUnique.incrementTo(type);
	}
	
}
