package europeana.rnd.dataprocessing.language;

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
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LangStatsOfItem;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsInClass;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMaps;

public class HtmlExporter {
	private static final DecimalFormat countFormat = new DecimalFormat("#,##0");
	private static final DecimalFormat percentFormat = new DecimalFormat("#,##0.##");

	public static void export(LanguageStatsInDataset stats, File outFolder) throws IOException {
		{
			File htmlFile;
			FileWriterWithEncoding writer;
//			File htmlFile=new File(outFolder, "Language_GlobalReport.html");
//			FileWriterWithEncoding writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
//			writeStart(writer);
//			writeStatsByMatch(writer, stats.statsByMatch, "");
//			writeEnd(writer);	
//			writer.close();
			
			htmlFile=new File(outFolder, "Language_ProvidersDataReport.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsOfSource(writer, stats.fromProvider, "Report about use of xml:lang in Europeana (data providers’ data)");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "Language_EuropeanaDataReport.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsOfSource(writer, stats.fromEuropeana,"Report about use of xml:lang in Europeana (Europeana's data)");
			writeEnd(writer);	
			writer.close();

			htmlFile=new File(outFolder, "Language_GlobalReport.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeGlobalStats(writer, stats,"Report about language data in Europeana");
			writeEnd(writer);	
			writer.close();
			
//			write not normalizable examples
			File txtFile=new File(outFolder, "Language_Europeana_xml_lang_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable xml:lang values in data from Europeana\n\n");
			for(String val: stats.fromEuropeana.notNormalizableXmlLang.getSample()) {
				writer.append(val+"\n");			
			}
			writer.close();
			
			txtFile=new File(outFolder, "Language_Provider_xml_lang_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable xml:lang values in data from data providers\n\n");
			for(String val: stats.fromProvider.notNormalizableXmlLang.getSample()) {
				writer.append(val+"\n");			
			}
			writer.close();			

			txtFile=new File(outFolder, "Language_Europeana_dc_language_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable dc:language values in data from Europeana\n\n");
			for(String val: stats.fromEuropeana.notNormalizableDcLanguage.getSample()) {
				writer.append(val+"\n");			
			}
			writer.close();
			
			txtFile=new File(outFolder, "Language_Provider_dc_language_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable dc:language values in data from data providers\n\n");
			for(String val: stats.fromProvider.notNormalizableDcLanguage.getSample()) {
				writer.append(val+"\n");			
			}
			writer.close();			
		}
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
	
	private static void writeStatsOfSource(Appendable writer,  LanguageStatsFromSource stats, String title) throws IOException {
		ArrayList<Resource> sortedClasses=new ArrayList<Resource>(stats.statsByClassAndField.keySet());
		Collections.sort(sortedClasses, new Comparator<Resource>() {
			@Override
			public int compare(Resource a, Resource b) {
				if(a.equals(Ore.Proxy))
					return Integer.MIN_VALUE;
				if(b.equals(Ore.Proxy))
					return Integer.MAX_VALUE;
				return a.getLocalName().compareTo(b.getLocalName());
			}
		});

		writer.append("<h2>"+title+"</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Class</b></td><td>Property</td><td><b>Statistics</b></td></tr>\n");
		for(Resource cls: sortedClasses) {
			LanguageStatsInClass statsForClass = stats.getStatsFor(cls);
			ArrayList<String> sortedProps=new ArrayList<String>(statsForClass.langTagStatsByField.keySet());
			Collections.sort(sortedProps);
			
			boolean first=true;
			for(String prop:sortedProps) {
				LangStatsOfItem statsOfItem = statsForClass.langTagStatsByField.get(prop);
				writer.append("<tr>\r\n");
				if(first) {
					writer.append("<td rowspan=\""+(sortedProps.size())+"\" valign=\"top\">"+Edm.getPrefixedName(cls)+"</td>");
					first=false;
				}
				writer.append("<td valign=\"top\">"+prop+"</td>\r\n"
						+ "<td align=\"left\">"
						+ "Total statements: "+countFormat.format(statsOfItem.total())
						+ "<br />Without <i>xml:lang</i>: "+countFormat.format(statsOfItem.withoutLangTag)+ " ("+percentFormat.format(statsOfItem.withoutLangTagsPercent()) +"%)" );
				if(statsOfItem.withLangTags>0) {
					writer.append("<br />With <i>xml:lang</i>: "+countFormat.format(statsOfItem.withLangTags)+ " ("+percentFormat.format(statsOfItem.withLangTagsPercent()) +"%)");
					if(statsOfItem.cntNormalized>0)
						writer.append("<br /> - With <i>xml:lang</i> in ISO639-1: "+countFormat.format(statsOfItem.cntNormalized)+ " ("+percentFormat.format(statsOfItem.normalizedPercent()) +"%)");
					if(statsOfItem.cntNormalizable>0)
						writer.append("<br /> - With <i>xml:lang</i> normalizable: "+countFormat.format(statsOfItem.cntNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
					if(statsOfItem.cntNotNormalizable>0)
						writer.append("<br /> - With <i>xml:lang</i> not normalizable: "+countFormat.format(statsOfItem.cntNotNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
					if(statsOfItem.cntSubtags>0)
						writer.append("<br /> - With <i>xml:lang</i> with subtags "+countFormat.format(statsOfItem.cntSubtags)+ " ("+percentFormat.format(statsOfItem.subtagsPercent()) +"%)");
				}						
				writer.append("</td>\r\n"
					+ "</tr>\n");
			}
		}
		writer.append("</table>\n");
	}	

	private static void writeGlobalStats(Appendable writer,  LanguageStatsInDataset  stats, String title) throws IOException {
		writer.append("<h2>"+title+"</h2>\n");

		writer.append("<h3>Usage of <i>xml:lang</i>:</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Source</b></td><td><b>Statistics</b></td><td><b>Files</b></td></tr>\n");

		LangStatsOfItem statsOfItem=stats.fromEuropeana.calculateGlobalStatsLangTags();
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\">Europeana</td>\r\n"
				+ "<td align=\"left\">"
				+ "Total statements: "+countFormat.format(statsOfItem.total())
				+ "<br />Without <i>xml:lang</i>: "+countFormat.format(statsOfItem.withoutLangTag)+ " ("+percentFormat.format(statsOfItem.withoutLangTagsPercent()) +"%)" );
		if(statsOfItem.withLangTags>0) {
			writer.append("<br />With <i>xml:lang</i>: "+countFormat.format(statsOfItem.withLangTags)+ " ("+percentFormat.format(statsOfItem.withLangTagsPercent()) +"%)");
			if(statsOfItem.cntNormalized>0)
				writer.append("<br /> - With <i>xml:lang</i> in ISO639-1: "+countFormat.format(statsOfItem.cntNormalized)+ " ("+percentFormat.format(statsOfItem.normalizedPercent()) +"%)");
			if(statsOfItem.cntNormalizable>0)
				writer.append("<br /> - With <i>xml:lang</i> normalizable: "+countFormat.format(statsOfItem.cntNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
			if(statsOfItem.cntNotNormalizable>0)
				writer.append("<br /> - With <i>xml:lang</i> not normalizable: "+countFormat.format(statsOfItem.cntNotNormalizable)+ " ("+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%)");
			if(statsOfItem.cntSubtags>0)
				writer.append("<br /> - With <i>xml:lang</i> with subtags "+countFormat.format(statsOfItem.cntSubtags)+ " ("+percentFormat.format(statsOfItem.subtagsPercent()) +"%)");
		}
		writer.append("</td>\r\n"
				+ "<td valign=\"top\">"
				+ (statsOfItem.cntNotNormalizable >0 ? "<a href=\"Language_Europeana_xml_lang_not_normalizable.txt\">Europeana_xml_lang_not_normalizable.txt</a>" : "&nbsp;" )
				+ "</td>\r\n"
			+ "</tr>\n");

		statsOfItem=stats.fromProvider.calculateGlobalStatsLangTags();
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\">Data provider</td>\r\n"
				+ "<td align=\"left\">"
				+ "Total statements: "+countFormat.format(statsOfItem.total())
				+ "<br />Without <i>xml:lang</i>: "+countFormat.format(statsOfItem.withoutLangTag)+ " ("+percentFormat.format(statsOfItem.withoutLangTagsPercent()) +"%)" );
		if(statsOfItem.withLangTags>0) {
			writer.append("<br />With <i>xml:lang</i>: "+countFormat.format(statsOfItem.withLangTags)+ " ("+percentFormat.format(statsOfItem.withLangTagsPercent()) +"%)");
			if(statsOfItem.cntNormalized>0)
				writer.append("<br /> - With <i>xml:lang</i> in ISO639-1: "+countFormat.format(statsOfItem.cntNormalized)+ " ("+percentFormat.format(statsOfItem.normalizedPercent()) +"%)");
			if(statsOfItem.cntNormalizable>0)
				writer.append("<br /> - With <i>xml:lang</i> normalizable: "+countFormat.format(statsOfItem.cntNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
			if(statsOfItem.cntNotNormalizable>0)
				writer.append("<br /> - With <i>xml:lang</i> not-normalizable: "+countFormat.format(statsOfItem.cntNotNormalizable)+ " ("+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%)");
			if(statsOfItem.cntSubtags>0)
				writer.append("<br /> - With <i>xml:lang</i> with subtags "+countFormat.format(statsOfItem.cntSubtags)+ " ("+percentFormat.format(statsOfItem.subtagsPercent()) +"%)");
		}						
		writer.append("</td>\r\n"
				+ "<td valign=\"top\">"
				+ (statsOfItem.cntNotNormalizable >0 ? "<a href=\"Language_Provider_xml_lang_not_normalizable.txt\">Provider_xml_lang_not_normalizable.txt</a>" : "&nbsp;" )
				+ "</td>\r\n"
			+ "</tr>\n");
		
		writer.append("</table>\n");


	
		writer.append("<h3>Usage of <i>dc:language</i>:</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Source</b></td><td><b>Statistics</b></td><td><b>Files</b></td></tr>\n");

		statsOfItem=stats.fromEuropeana.calculateGlobalStatsDcLanguage();
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\">Europeana</td>\r\n"
				+ "<td align=\"left\">"
				+ "Total statements: "+countFormat.format(statsOfItem.total()));
			if(statsOfItem.cntNormalized>0)
				writer.append("<br /> - With values in ISO639-1: "+countFormat.format(statsOfItem.cntNormalized)+ " ("+percentFormat.format(statsOfItem.normalizedPercent()) +"%)");
			if(statsOfItem.cntNormalizable>0)
				writer.append("<br /> - With normalizable values: "+countFormat.format(statsOfItem.cntNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
			if(statsOfItem.cntNotNormalizable>0)
				writer.append("<br /> - With not-normalizable values: "+countFormat.format(statsOfItem.cntNotNormalizable)+ " ("+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%)");
			if(statsOfItem.cntSubtags>0)
				writer.append("<br /> - With values containing subtags "+countFormat.format(statsOfItem.cntSubtags)+ " ("+percentFormat.format(statsOfItem.subtagsPercent()) +"%)");
			writer.append("</td>\r\n"
					+ "<td valign=\"top\">"
					+ (statsOfItem.cntNotNormalizable >0 ? "<a href=\"Language_Europeana_dc_language_not_normalizable.txt\">Europeana_dc_language_not_normalizable.txt</a>" : "&nbsp;" )
					+ "</td>\r\n"
				+ "</tr>\n");

		statsOfItem=stats.fromProvider.calculateGlobalStatsDcLanguage();
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\">Data provider</td>\r\n"
				+ "<td align=\"left\">"
				+ "Total statements: "+countFormat.format(statsOfItem.total()));
				if(statsOfItem.cntNormalized>0)
					writer.append("<br /> - With values in ISO639-1: "+countFormat.format(statsOfItem.cntNormalized)+ " ("+percentFormat.format(statsOfItem.normalizedPercent()) +"%)");
				if(statsOfItem.cntNormalizable>0)
					writer.append("<br /> - With normalizable values: "+countFormat.format(statsOfItem.cntNormalizable)+ " ("+percentFormat.format(statsOfItem.normalizablePercent()) +"%)");
				if(statsOfItem.cntNotNormalizable>0)
					writer.append("<br /> - With not normalizable values: "+countFormat.format(statsOfItem.cntNotNormalizable)+ " ("+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%)");
				if(statsOfItem.cntSubtags>0)
					writer.append("<br /> - With values containing subtags "+countFormat.format(statsOfItem.cntSubtags)+ " ("+percentFormat.format(statsOfItem.subtagsPercent()) +"%)");
		writer.append("</td>\r\n"
				+ "<td valign=\"top\">"
				+ (statsOfItem.cntNotNormalizable >0 ? "<a href=\"Language_Provider_dc_language_not_normalizable.txt\">Provider_dc_language_not_normalizable.txt</a>" : "&nbsp;")
				+ "</td>\r\n"
			+ "</tr>\n");
		
		writer.append("</table>\n");
		
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
