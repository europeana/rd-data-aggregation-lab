/**
 * 
 */
package europeana.rnd.dataprocessing.pid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;

/**
 * @author Nuno Freire
 * @since 06/06/2025
 */
public class PidSchemes {
  
  public static final PidScheme ARK;
  public static final PidScheme HANDLE;
  public static final PidScheme DOI;
  
  public record PidMatchResult (PidScheme schema, String canonicalPid, String originalPid){} 

  static List<PidScheme> allSchemes=new ArrayList<PidScheme>();
  static {
    PidScheme ark = new PidScheme("ark", "https?://ark\\.bnf\\.fr/ark:(/?[0-9]+)(/[a-z0-9=~\\*\\+@_$%\\-]+)(/[a-z0-9=~\\*\\+@_$%\\-/\\.]+)?", "ark:$1$2$3", "https://n2t.net/$0");
    ark.addMatchingPattern("https?://gallica\\.bnf\\.fr/ark:(/?[0-9]+)(/[a-z0-9=~\\*\\+@_$%\\-]+)(/[a-z0-9=~\\*\\+@_$%\\-/\\.]+)?");
    ark.addMatchingPattern("https?://catalogue\\.bnf\\.fr/ark:(/?[0-9]+)(/[a-z0-9=~\\*\\+@_$%\\-]+)(/[a-z0-9=~\\*\\+@_$%\\-/\\.]+)?");
    ark.addMatchingPattern("https?://n2t\\.net/ark:(/?[0-9]+)(/[a-z0-9=~\\*\\+@_$%\\-]+)(/[a-z0-9=~\\*\\+@_$%\\-/\\.]+)?");
    ark.addMatchingPattern("ark:(/?[0-9]+)(/[a-z0-9=~\\*\\+@_$%\\-]+)(/[a-z0-9=~\\*\\+@_$%\\-/\\.]+)?");
    allSchemes.add(ark);
    ARK=ark;
    
    PidScheme doi = new PidScheme("doi", "doi:(10\\.[0-9][0-9\\.]*)/(.*)", "doi:$1/$2", "https://doi.org/$0");
    doi.addMatchingPattern("https?://doi\\.org/doi:(10\\.[0-9][0-9\\.]*)/(.*)");
    doi.addMatchingPattern("https?://dx\\.doi\\.org/doi:(10\\.[0-9][0-9\\.]*)/(.*)");
    doi.addMatchingPattern("https?://doi\\.org/(10\\.[0-9][0-9\\.]*)/(.*)");
    doi.addMatchingPattern("https?://dx\\.doi\\.org/(10\\.[0-9][0-9\\.]*)/(.*)");
    allSchemes.add(doi);
    DOI=doi;
    
    PidScheme handle = new PidScheme("hdl", "https?://hdl\\.handle\\.net/([0-9][\\.0-9]*/.*)", "hdl:$1", "https://hdl.handle.net/$0");
    handle.addMatchingPattern("hdl:([0-9][\\.0-9]*/.*)");
    allSchemes.add(handle);
    HANDLE=handle;
    
    PidScheme purlOrg = new PidScheme("purl", "(https?://purl\\.org/.+)", "", "");
    purlOrg.addMatchingPattern("(https?://www\\.purl\\.org/.+)");
    allSchemes.add(purlOrg);
    allSchemes.add(new PidScheme("purlBodleian", "(https?://purl\\.ox\\.ac\\.uk/.+)", "", ""));
    allSchemes.add(new PidScheme("purlPt", "(https?://purl\\.pt/.+)", "", ""));
    
    PidScheme urnDe = new PidScheme("urnNbnDe", "(urn:nbn:de:.*)", "$1", "https://nbn-resolving.org/$0");
    urnDe.addMatchingPattern("https?://nbn-resolving\\.de/(urn:nbn:de:.+)");
    urnDe.addMatchingPattern("https?://www\\.nbn-resolving\\.de/(urn:nbn:de:.+)");
    urnDe.addMatchingPattern("https?://mdz-nbn-resolving\\.de/(urn:nbn:de:.+)");
    allSchemes.add(urnDe);
       
    PidScheme urnCh = new PidScheme("urnNbnCh", "(urn:nbn:ch[:-].+)", "$1", "https://nbn-resolving.org/$0");
    urnCh.addMatchingPattern("https?://nbn-resolving.org/(urn:nbn:ch[:-].+)");
    urnCh.addMatchingPattern("https?://www\\.nbn-resolving.org/(urn:nbn:ch[:-].+)");
    allSchemes.add(urnCh);
    
    PidScheme urnHr = new PidScheme("urnNbnHr", "(urn:nbn:hr[:-].+)", "$1", "https://urn.nsk.hr/$0");
    urnHr.addMatchingPattern("https?://urn\\.nsk\\.hr/(urn:nbn:hr[:-].+)");
//    urnHr.addMatchingPattern("https?://nbn-resolving.org/(urn:nbn:hr:+)");
//    urnHr.addMatchingPattern("https?://www\\.nbn-resolving.org/(urn:nbn:hr:+)");
    allSchemes.add(urnHr);
    
    PidScheme urnFi = new PidScheme("urnNbnFi", "(urn:nbn:fi[:-].+)", "$1", "https://urn.fi/$0");
    urnFi.addMatchingPattern("https?://urn\\.fi/(urn:nbn:fi[:-].+)");
    urnFi.addMatchingPattern("https?://s1\\.doria\\.fi/(urn:nbn:fi[:-].+)");
//    urnFi.addMatchingPattern("https?://nbn-resolving.org/(urn:nbn:fi[:-]+)");
//    urnFi.addMatchingPattern("https?://www\\.nbn-resolving.org/(urn:nbn:fi[:-]+)");
    allSchemes.add(urnFi); 

    PidScheme urnAt = new PidScheme("urnNbnAt", "(urn:nbn:at[:-].*)", "$1", "https://resolver.obvsg.at/$0");
    urnAt.addMatchingPattern("https?://resolver\\.obvsg\\.at/(urn:nbn:at[:-].+)");
    urnAt.addMatchingPattern("https?://nbn-resolving\\.org/(urn:nbn:at[:-].+)");
    urnAt.addMatchingPattern("https?://www\\.nbn-resolving\\.org/(urn:nbn:at[:-].+)");    
    allSchemes.add(urnAt); 

    PidScheme urnNo = new PidScheme("urnNbnNo", "(urn:nbn:no[:-].+)", "$1", "https://urn.nb.no/$0");
    urnNo.addMatchingPattern("https?://urn\\.nb\\.no/(urn:nbn:no[:-].+)");
//    urnNo.addMatchingPattern("https?://nbn-resolving\\.org/(urn:nbn:no:.+)");
//    urnNo.addMatchingPattern("https?://www\\.nbn-resolving\\.org/(urn:nbn:no:.+)");    
    allSchemes.add(urnNo); 

    PidScheme urnSk = new PidScheme("urnNbnSk", "(urn:nbn:sk[:-].+)", "$1", "https://nbn.sk/$0");
    urnSk.addMatchingPattern("https?://nbn\\.sk/(urn:nbn:sk[:-].+)");
//    urnSk.addMatchingPattern("https?://nbn-resolving\\.org/(urn:nbn:sk[:-].+)");
//    urnSk.addMatchingPattern("https?://www\\.nbn-resolving\\.org/(urn:nbn:sk[:-].+)");    
    allSchemes.add(urnSk); 
    
    PidScheme urnNl = new PidScheme("urnNbnNl", "(urn:nbn:nl[:-].+)", "$1", "https://persistent-identifier.nl/$0");
    urnNl.addMatchingPattern("https?://www\\.persistent-identifier\\.nl/(urn:nbn:nl[:-].+)");
    urnNl.addMatchingPattern("https?://persistent-identifier\\.nl/(urn:nbn:nl[:-].+)");
    allSchemes.add(urnNl); 

    PidScheme urnCz = new PidScheme("urnNbnCz", "(urn:nbn:cz[:-].+)", "$1", "https://resolver.nkp.cz/$0");
    urnCz.addMatchingPattern("https?://resolver\\.nkp\\.cz/(urn:nbn:cz[:-].+)");
    allSchemes.add(urnCz); 
      
    PidScheme urnIt = new PidScheme("urnNbnIt", "(urn:nbn:it[:-].+)", "$1", "https://nbn.depositolegale.it/$0");
    urnIt.addMatchingPattern("https?://nbn\\.depositolegale\\.it/(urn:nbn:it[:-].+)");
    allSchemes.add(urnIt); 
    
    PidScheme urnSe = new PidScheme("urnNbnSe", "(urn:nbn:se[:-].+)", "$1", "https://urn.kb.se/resolve?urn=$0");
    urnSe.addMatchingPattern("https?://urn\\.kb\\.se/resolve?urn=(urn:nbn:se[:-].+)");
    allSchemes.add(urnSe); 

    PidScheme urnSi = new PidScheme("urnNbnSi", "(urn:nbn:si[:-].+)", "$1", "https://www.nbn.si/$0");
    urnSi.addMatchingPattern("https?://www\\.nbn\\.si/(urn:nbn:si[:-].+)");
    allSchemes.add(urnSi); 

  }
  
  
  public static PidMatchResult matchPidSchema(String id) {
    for(PidScheme schema: allSchemes) {
      String canonicalForm = schema.getCanonicalForm(id);
      if(canonicalForm!=null)
        return new PidMatchResult(schema, canonicalForm, id);
    }
    return null;
  }

  
  
  public static void main(String[] args) throws Exception {
    //write patterns to a csv file

    if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
      GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");       
    else
      GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");            
    String spreadsheetId = "1gNJi0vvtXVXeuyKBI_s8h2kU8ln4qsk0A4KPRbMBVwg";
    
    SheetsPrinter outSheet=new SheetsPrinter(spreadsheetId, "PID Schemes");
    
    outSheet.print("Schema ID", "matchingPattern",  "canonicalPattern", "resolvableUrlPattern");
    outSheet.println();
    for(PidScheme schema: allSchemes) {
      outSheet.print(schema.getSchemeId(), schema.getMatchingPatterns().get(0).pattern(), schema.getCanonicalPattern(), schema.getResolvablePattern());
      outSheet.println();
      for(int i=1; i<schema.getMatchingPatterns().size(); i++) {
        outSheet.print("", schema.getMatchingPatterns().get(i).pattern());
        outSheet.println();
      }
    }
    
    outSheet.close();
  }
}
