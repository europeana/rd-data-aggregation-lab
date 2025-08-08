package europeana.rnd.dataprocessing.pid.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class PidScheme implements Comparable<PidScheme>{
  private String schemeId;
  private List<Pattern> matchingPatterns;
//  private Pattern canonicalPattern;
  private String canonicalPattern;
  private String resolvablePattern;
  
//  private String uri;
  private String title;
  private String seeAlso;
  private String organization;
  
	public PidScheme(String schemaId, String matchingPattern, String canonicalPattern, String resolvablePattern) {
    this(schemaId);
    this.matchingPatterns.add(Pattern.compile(matchingPattern, Pattern.CASE_INSENSITIVE));
    if (!StringUtils.isEmpty(canonicalPattern)) 
      this.canonicalPattern=canonicalPattern;
    if (!StringUtils.isEmpty(resolvablePattern)) 
      this.resolvablePattern=resolvablePattern;
//      this.canonicalPattern = Pattern.compile(canonicalPattern, Pattern.CASE_INSENSITIVE);
//      this.resolvablePattern = Pattern.compile(resolvablePattern, Pattern.CASE_INSENSITIVE);
  }

  /**
   * @param string
   */
  public PidScheme(String schemeId) {
    this.schemeId = schemeId;
    this.matchingPatterns = new ArrayList<>();
  }

  public String getCanonicalForm(String id) {
    id=id.trim();
    for(Pattern pat: matchingPatterns) {
      Matcher matcher=pat.matcher(id);
      if(matcher.matches()) {
        if(canonicalPattern==null)
          return id;
        for(int grp=1; grp<=matcher.groupCount(); grp++) {
          try {
            if(matcher.group(grp)!=null)
              canonicalPattern= canonicalPattern.replaceFirst("\\$"+grp, matcher.group(grp));
            else
              canonicalPattern= canonicalPattern.replaceFirst("\\$"+grp, "");
          } catch (Exception e) {
            e.printStackTrace();
            System.out.println(this.schemeId);
            System.out.println(id);
            System.out.println(canonicalPattern);
            System.out.println(pat.pattern());
          }
        }
        return canonicalPattern;
      }
    }
    return null;
  }

  public String getResolvableUrl(String id) {
    String canonical=getCanonicalForm(id);
    if(canonical==null)
      return null;
    if (resolvablePattern!=null) 
      return resolvablePattern.replaceAll("\\$0", canonical);
    return canonical;
  }

  /**
   * @param string
   */
  public void addMatchingPattern(String matchingPattern) {
    this.matchingPatterns.add(Pattern.compile(matchingPattern, Pattern.CASE_INSENSITIVE));
  }

  @Override
  public int compareTo(PidScheme o) {
    return schemeId.compareTo(o.schemeId);
  }
  
  /**
   * @return
   */
  public String getSchemeId() {
    return schemeId;
  }

  protected List<Pattern> getMatchingPatterns() {
    return matchingPatterns;
  }

  protected String getCanonicalPattern() {
    return canonicalPattern;
  }

  protected String getResolvablePattern() {
    return resolvablePattern;
  }

  protected String getSeeAlso() {
    return seeAlso;
  }

  protected void setSeeAlso(String seeAlso) {
    this.seeAlso = seeAlso;
  }

  protected String getTitle() {
    return title;
  }

  protected String getOrganization() {
    return organization;
  }

  protected void setSchemeId(String schemaId) {
    this.schemeId = schemaId;
  }

  protected void setCanonicalPattern(String canonicalPattern) {
    this.canonicalPattern = canonicalPattern;
  }

  protected void setResolvablePattern(String resolvablePattern) {
    this.resolvablePattern = resolvablePattern;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  protected void setOrganization(String organization) {
    this.organization = organization;
  } 
	
  @Override
  public boolean equals(Object obj) {
    return schemeId.equals(obj instanceof PidScheme ? ((PidScheme)obj).getSchemeId() : obj.toString());
  }
  
}
