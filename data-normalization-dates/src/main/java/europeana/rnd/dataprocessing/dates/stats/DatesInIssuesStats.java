package europeana.rnd.dataprocessing.dates.stats;

public class DatesInIssuesStats {
	int titlesCount=0;
	int issuesCount=0;
	int issuesWithDctermsIssued=0;
	int issuesWithDctermsIssuedNormalizable=0;
	@Override
	public String toString() {
		return "DatesInIssuesStats [titlesCount=" + titlesCount + ", issuesCount=" + issuesCount
				+ ", issuesWithDctermsIssued=" + issuesWithDctermsIssued + ", issuesWithDctermsIssuedNormalizable="
				+ issuesWithDctermsIssuedNormalizable + "]";
	}
	public double normalizablePercent() {
		return issuesCount==0 ? 0 : (double)issuesWithDctermsIssuedNormalizable/(double)issuesCount * 100;
	}		
}