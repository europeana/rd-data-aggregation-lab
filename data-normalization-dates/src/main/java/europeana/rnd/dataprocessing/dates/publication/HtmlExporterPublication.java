package europeana.rnd.dataprocessing.dates.publication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ext.com.google.common.math.Stats;

import europeana.rnd.dataprocessing.dates.MatchId;
import europeana.rnd.dataprocessing.dates.stats.Examples;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMaps;

public class HtmlExporterPublication {
	private static final DecimalFormat countFormat = new DecimalFormat("#,##0");
	private static final DecimalFormat percentFormat = new DecimalFormat("#,##0.0##");

	public static void export(DateExtractionStatisticsPublication stats, DateExtractionStatisticsPublication statsCoverageSubject, File outFolder) throws IOException {
		{
			File htmlFile=new File(outFolder, "MatchesByPattern.html");
			FileWriterWithEncoding writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByMatch(writer, stats.statsByMatch, "");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassProvider.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, stats.statsByClassProvider,"class", "(from provider's metadata)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassAndPropertyProvider.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, stats.statsByClassAndPropertyProvider,"class/property", "(from provider's metadata)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassEuropeana.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, stats.statsByClassEuropeana,"class","(from Europeana's metadata)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassAndPropertyEuropeana.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, stats.statsByClassAndPropertyEuropeana,"class/property","(from Europeana's metadata)");
			writeEnd(writer);	
			writer.close();
		}
		{
			File htmlFile=new File(outFolder, "MatchesByPatternCoverageSubject.html");
			FileWriterWithEncoding writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByMatch(writer, statsCoverageSubject.statsByMatch, "(dc:subject and dc:coverage)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassProviderCoverageSubject.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, statsCoverageSubject.statsByClassProvider,"class", "(dc:subject and dc:coverage from provider's metadata)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassAndPropertyProviderCoverageSubject.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, statsCoverageSubject.statsByClassAndPropertyProvider,"class/property", "(dc:subject and dc:coverage from provider's metadata)");
			writeEnd(writer);
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassEuropeanaCoverageSubject.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, statsCoverageSubject.statsByClassEuropeana,"class", "(dc:subject and dc:coverage from Europeana's metadata)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "MatchesByClassAndPropertyEuropeanaCoverageSubject.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsByCollection(writer, statsCoverageSubject.statsByClassAndPropertyEuropeana,"class/property", "(dc:subject and dc:coverage from Europeana's metadata)");
			writeEnd(writer);	
			writer.close();
		}

		
		//		htmlFile=new File(outFolder, "MatchesGlobal.html");
//		writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
//		writeStart(writer);
//		writeStatsGlobal(writer, stats.statsGlobal);
//		writeEnd(writer);	
//		writer.close();
	}
	
//	private static void writeStatsGlobal(Appendable writer, MapOfInts<MatchId> statsGlobal) throws IOException {
//		ArrayList<MatchId> matchIds=new ArrayList<MatchId>(statsGlobal.keySet());
//		Collections.sort(matchIds);
//		
//		writer.append("<table id=\"top\">\r\n"
//				+ "<tr>\r\n"
//				+ "<td><b>Pattern</b></td><td><b>Occurrences</b></td></tr>\n");
//		for(MatchId matchId: matchIds) {
//			writer.append("<tr>\r\n"
//					+ "<td>"+matchId.getLabel()+"</td>\r\n"
//					+ "<td>"+decimalFormat.format(statsGlobal.get(matchId))+"</td>\r\n"
//					+ "</tr>\n");
//		}
//		writer.append("</table>\n");
//	}
	
	private static void writeStatsByCollection(Appendable writer, MapOfMaps<String, PatternId, Examples> statsByCollection, String label, String title) throws IOException {
		ArrayList<String> cols=new ArrayList<String>(statsByCollection.keySet());
		Collections.sort(cols);
		
		//move proxy to the top
		ArrayList<String> proxyCols=new ArrayList<String>();
		for(int i=0; i<cols.size() ; i++) {
			String col=cols.get(i);
			if(col.startsWith("Proxy")) {
				cols.remove(i);
				proxyCols.add(col);
				i--;
			}
		}
		proxyCols.addAll(cols);
		cols=proxyCols;
		
		writer.append("<h2>Date normalization: occurrence of date patterns by "+label+"<br />"+title+"</h2>\n");
		
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>"+label+"</b></td><td>Pattern</td><td><b>Count</b></td><td></td></tr>\n");
		
		for(String col: cols) {
			Map<PatternId, Examples> matchStats = statsByCollection.get(col);
			boolean first=true;
			ArrayList<PatternId> matchIds=new ArrayList<PatternId>(matchStats.keySet());
			Collections.sort(matchIds, new Comparator<PatternId>() {
				@Override
				public int compare(PatternId o1, PatternId o2) {
					return matchStats.get(o2).totalFound - matchStats.get(o1).totalFound;
				}
			});
			int total=0;
			for(PatternId matchId: matchIds) {
				total+=matchStats.get(matchId).totalFound;
				writer.append("<tr>\r\n");
				if(first) {
					writer.append("<td id=\""+col+"\" rowspan=\""+(matchIds.size()+1)+"\" valign=\"top\">"+col+"</td>");
					first=false;
				}
				writer.append("<td>"+matchId.getLabel()+"</td>\r\n"
						+ "<td align=\"right\">"+countFormat.format(matchStats.get(matchId).totalFound)+"</td>\r\n"
						+ "<td>(<a href=\"#"+col+matchId+"\">see examples</a>)</td>\r\n"
						+ "</tr>\n");
			}
			writer.append("<td>Total</td>\r\n"
					+ "<td align=\"right\">"+countFormat.format(total)+"</td>\r\n"
					+ "<td> </td>\r\n"
					+ "</tr>\n");
			
		}
		writer.append("</table>\n");

		writer.append("<br />\n");
		for(String col: cols) {
			writer.append("<hr /><h2>Examples from "+label+" '"+col+"'</h2>");			
			Map<PatternId, Examples> matchStats = statsByCollection.get(col);
			ArrayList<PatternId> matchIds=new ArrayList<PatternId>(matchStats.keySet());
			Collections.sort(matchIds, new Comparator<PatternId>() {
				@Override
				public int compare(PatternId o1, PatternId o2) {
					return matchStats.get(o2).totalFound - matchStats.get(o1).totalFound;
				}
			});
			for(PatternId matchId: matchIds) {
				writer.append("<ul id=\""+col+matchId+"\"><li><b>"+matchId.getLabel()+" - "+
						matchStats.get(matchId).sample.size()+" examples out of "+
						countFormat.format(matchStats.get(matchId).totalFound)+" occurrences</b></li></ul>");
				boolean firstEx=true;
				for(String example: matchStats.get(matchId).sample) {
					if(firstEx)
						firstEx=false;
					else
						writer.append(", ");		
					writer.append("\'"+StringEscapeUtils.escapeHtml4(example)+"\'\n");
				}
				writer.append("<br /><a href=\"#"+col+"\">back to "+label+"</a><br />\n");
				writer.append("</p>");
			}
		}		
	}
	
	private static void writeStatsByMatch(Appendable writer, Map<PatternId, Examples> statsByMatch, String title) throws IOException {
		ArrayList<PatternId> matchIds=new ArrayList<PatternId>(statsByMatch.keySet());
		Collections.sort(matchIds, new Comparator<PatternId>() {
			@Override
			public int compare(PatternId o1, PatternId o2) {
				return statsByMatch.get(o2).totalFound - statsByMatch.get(o1).totalFound;
			}
		});
		
		double total=0;
		for(PatternId matchId: matchIds) 
			total+=statsByMatch.get(matchId).totalFound;

		writer.append("<h2>Date normalization: occurrence of date patterns across the whole dataset <br />"+title+"</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Pattern</b></td><td align=\"right\"><b>Count</b></td><td align=\"center\"><b>%</b></td><td></td></tr>\n");
		for(PatternId matchId: matchIds) {
			writer.append("<tr>\r\n"
					+ "<td>"+matchId.getLabel()+"</td>\r\n"
					+ "<td align=\"right\">"+countFormat.format(statsByMatch.get(matchId).totalFound)+"</td>\r\n"
					+ "<td align=\"right\">"+percentFormat.format((double)statsByMatch.get(matchId).totalFound / total * 100)+"%</td>\r\n"
					+ "<td>(<a href=\"#"+matchId+"\">see examples</a>)</td>\r\n"
					+ "</tr>\n");
		}
		writer.append("<tr>\r\n"
				+ "<td>Total</td>\r\n"
				+ "<td align=\"right\">"+countFormat.format(total)+"</td>\r\n"
				+ "<td align=\"right\"> </td>\r\n"
				+ "<td> </td>\r\n"
				+ "</tr>\n");		
		writer.append("</table>\n");

		writer.append("<br />\n");
		for(PatternId matchId: matchIds) {
			writer.append("<ul id=\""+matchId+"\"><li><b>"+matchId.getLabel()+" ("+statsByMatch.get(matchId).sample.size()+" examples out of "+countFormat.format(statsByMatch.get(matchId).totalFound)+" occurrences)</b></li></ul>");
			boolean firstEx=true;
			for(String example: statsByMatch.get(matchId).sample) {
				if(firstEx)
					firstEx=false;
				else
					writer.append(", ");						
				writer.append("\'"+StringEscapeUtils.escapeHtml4(example)+"\'\n");			
			}
			writer.append("<br /><a href=\"#top\">back to top</a><br />\n");
		}
	}
	
	private static void writeStart(Appendable writer) throws IOException {
		writer.append("<html>\r\n"
				+ "<head>\r\n"
				+ "<style>\r\n"
				+ "table {\r\n"
				+ "    border-collapse: collapse;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "td, th {\r\n"
				+ "    border: 1px solid black;\r\n"
				+ "    padding: 3px;\r\n"
				+ "}\r\n"
				+ "ul {\r\n"
				+ "  list-style-position: inside;\r\n"
				+ "  padding-left: 0;\r\n"
				+ "}"
				+ "</style>\r\n"
				+ "</head>\r\n"
				+ "<body>\n");
	}
	
	private static void writeEnd(Appendable writer) throws IOException {
		writer.append("\n</body>\r\n"
				+ "</html>");
	}

}
