package europeana.rnd.dataprocessing.pid;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class PidScheme implements Comparable<PidScheme>{
  private String schemeId;
  private List<Pattern> matchingPatterns;
  private String canonicalPattern;
  private String resolvablePattern;
  
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
  }

  /**
   * @param string
   */
  public PidScheme(String schemeId) {
    this.schemeId = schemeId;
    this.matchingPatterns = new ArrayList<>();
  }

  /**
   * Checks if a PID string is valid by matching against the pattern expressions of this scheme
   * 
   * @param id A PID 
   * @return true if the id matches at least one of the patterns, false otherwise
   */
  public boolean isValid(String id) {
    id=id.trim();
    for(Pattern pat: matchingPatterns) {
      Matcher matcher=pat.matcher(id);
      if(matcher.matches()) 
        return true;
    }
    return false;
  }
  
  
  
  /**
   * Transforms a PID string into its canonical form. 
   * 
   * @param id a valid PID string of this scheme
   * @return the canonical form of the PID. If the PID is already in its canonical form, or this Scheme has not canonical form then the same PID is returned
   */
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


  /**
   * Transforms a PID string into a URL  
   * 
   * @param id a valid PID string of this scheme
   * @return a URL form of the PID. If the PID is already in it URL form, or this Scheme has no URL form then the same PID is returned
   */
  public String getResolvableUrl(String id) {
    if(!isValid(id))
      return null;
    if (StringUtils.startsWithIgnoreCase(id, "http://") || StringUtils.startsWithIgnoreCase(id, "https://")) 
      return id;
    String canonical=getCanonicalForm(id);
    if (resolvablePattern!=null) 
      return resolvablePattern.replaceAll("\\$0", canonical);
    return null; //there is not known resolver for this PID
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
