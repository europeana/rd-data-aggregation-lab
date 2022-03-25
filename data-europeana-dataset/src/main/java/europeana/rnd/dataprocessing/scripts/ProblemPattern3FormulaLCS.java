package europeana.rnd.dataprocessing.scripts;

import org.apache.commons.text.similarity.LongestCommonSubsequence;

public class ProblemPattern3FormulaLCS {
	
	public static boolean isProblem(String title, String description) {
		double lengthDif = Math.abs(title.length() - description.length());
		double minimumLength = Math.min(title.length(), description.length());
		double lcs=new LongestCommonSubsequence().apply(title, description); 
		double score=lcs / minimumLength;
		return score >= 0.9 && lengthDif < 20;
	}
}
