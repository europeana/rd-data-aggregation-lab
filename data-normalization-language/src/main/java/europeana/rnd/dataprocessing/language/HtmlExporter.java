package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ext.com.google.common.math.Stats;
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource.LangStatsOfItem;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource.LanguageStatsInClass;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMaps;

public class HtmlExporter {
	private static final DecimalFormat countFormat = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat percentFormat = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.UK));
	
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
			writeStatsOfSource(writer, stats.fromProvider, "Report about use of xml:lang in Europeana (data providers' data)", Source.PROVIDER, "Language_Provider_xml_lang");
			writeEnd(writer);	
			writer.close();
			
			htmlFile=new File(outFolder, "Language_EuropeanaDataReport.html");
			writer=new FileWriterWithEncoding(htmlFile, StandardCharsets.UTF_8);
			writeStart(writer);
			writeStatsOfSource(writer, stats.fromEuropeana,"Report about use of xml:lang in Europeana (Europeana's data)", Source.EUROPEANA, "Language_Europeana_xml_lang");
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
			for(Entry<String, Integer> val: stats.fromEuropeana.notNormalizableXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();

			txtFile=new File(outFolder, "Language_Europeana_xml_lang_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Normalisable xml:lang values in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.normalizableXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+","+stats.fromEuropeana.normalizableToXmlLang.get(val.getKey())+"\n");			
			}
			writer.close();

			txtFile=new File(outFolder, "Language_Europeana_xml_lang_all.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("All xml:lang values in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.allXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+","+stats.fromEuropeana.normalizableToXmlLang.get(val.getKey())+"\n");			
			}
			writer.close();
			
			
			
			txtFile=new File(outFolder, "Language_Provider_xml_lang_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable xml:lang values in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.notNormalizableXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			

			txtFile=new File(outFolder, "Language_Provider_xml_lang_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Normalisable xml:lang values in data from providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.normalizableXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+","+stats.fromProvider.normalizableToXmlLang.get(val.getKey())+"\n");			
			}
			writer.close();			

			txtFile=new File(outFolder, "Language_Provider_xml_lang_all.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("All xml:lang values in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.allXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			
		
			
			
			txtFile=new File(outFolder, "Language_Europeana_dc_language_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable dc:language values in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.notNormalizableDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();
			
			txtFile=new File(outFolder, "Language_Europeana_dc_language_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Normalisable dc:language values in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.normalizableDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+","+stats.fromEuropeana.normalizableToDcLanguage.get(val.getKey())+"\n");			
			}
			writer.close();

			txtFile=new File(outFolder, "Language_Europeana_dc_language_all.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("All dc:language values in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.allDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();
			
			
			
			txtFile=new File(outFolder, "Language_Provider_dc_language_not_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Not-normalisable dc:language values in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.notNormalizableDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			
			
			txtFile=new File(outFolder, "Language_Provider_dc_language_normalizable.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("Normalisable dc:language values in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.normalizableDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+","+stats.fromProvider.normalizableToDcLanguage.get(val.getKey())+"\n");			
			}
			writer.close();		
			
			txtFile=new File(outFolder, "Language_Provider_dc_language_all.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("All dc:language values in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.allDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			

//			write subtag examples
			txtFile=new File(outFolder, "Language_Europeana_xml_lang_subtags.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("xml:lang values with subtags in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.subtagsXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();
			
			txtFile=new File(outFolder, "Language_Provider_xml_lang_subtags.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("xml:lang values with subtags in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.subtagsXmlLang.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			

			txtFile=new File(outFolder, "Language_Europeana_dc_language_subtags.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("dc:language values with subtags in data from Europeana\n\n");
			for(Entry<String, Integer> val: stats.fromEuropeana.subtagsDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();
			
			txtFile=new File(outFolder, "Language_Provider_dc_language_subtags.txt");
			writer=new FileWriterWithEncoding(txtFile, StandardCharsets.UTF_8);
			writer.append("dc:language values with subtags in data from data providers\n\n");
			for(Entry<String, Integer> val: stats.fromProvider.subtagsDcLanguage.getCases()) {
				writer.append(val.getKey()+","+val.getValue()+"\n");			
			}
			writer.close();			
		}
	}

	
	private static void writeStatsOfSource(Appendable writer,  LanguageStatsFromSource stats, String title, Source source, String filenamePrefix) throws IOException {
		ArrayList<Resource> sortedClasses=new ArrayList<Resource>(stats.statsByClassAndField.keySet());
		
		//HACK: Aggregation and EuropeanaAggregation were not processed correctly on this first phase of extrcting language from the records
		if(source==Source.EUROPEANA)
			sortedClasses.remove(Ore.Aggregation);
		else if(source==Source.PROVIDER)
			sortedClasses.remove(Edm.EuropeanaAggregation);
		
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
		writer.append("<p>Explanation of the tables:</p>\r\n"
				+ "<ul>\r\n"				
				+ "<li>Class and Property - The statistics are grouped by the RDF class and property."
				+ "</li> \r\n"
				+ "<li>Statistics:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 20px;\">Total literals - only properties that contain a literal value are considered.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">Without xml:lang - properties with literal values that do not contain an xml:lang tag.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">With xml:lang - properties with literal values that contain an xml:lang tag. This statistic is broken down into the following:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 30px;\">in ISO639 - values that are already according to the ISO639 standards.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">normalizable - values that are not according to the ISO639 standards but the current language normalization of Metis is able to normalize.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">not normalizable - values that are not according to the ISO639 standards and the current language normalization of Metis is unable to normalize.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">with subtags -  values that are in ISO639 or are normalizable and contain subtags.</li>\r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "</ul>\r\n"

				+ "<p>Lists of values in xml:lang tags:</p>\r\n"
				+ "<ul>\r\n"
				+ "<li><a href=\""+filenamePrefix+"_all.txt\">all.txt</a> - a text file containing all the distinct values.</li>\r\n"
				+ "<li><a href=\""+filenamePrefix+"_not_normalizable.txt\">not_normalisable.txt</a> - a text file containing all the distinct values that the current language normalization of Metis is unable to normalize.</li>\r\n"
				+ "<li><a href=\""+filenamePrefix+"_normalizable.txt\">normalisable.txt</a> - a text file containing all the distinct values that the current language normalization of Metis is able to normalize along with and their normalised values.</li>\r\n"
				+ "<li><a href=\""+filenamePrefix+"_subtags.txt\">subtags.txt</a> - a text file containing all the distinct values that contain subtags.</li>\r\n"
				+ "</ul>\r\n"
				+ "</br>\r\n");
		
		
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Class</b></td><td><b>Property</b></td><td colspan=\"2\"><b>Statistics</b></td></tr>\n");
		for(Resource cls: sortedClasses) {
			LanguageStatsInClass statsForClass = stats.getStatsFor(cls);
			ArrayList<String> sortedProps=new ArrayList<String>(statsForClass.langTagStatsByField.keySet());
			Collections.sort(sortedProps);
			
			boolean first=true;
			for(String prop:sortedProps) {
				LangStatsOfItem statsOfItem = statsForClass.langTagStatsByField.get(prop);
				writer.append("<tr>\r\n");
				if(first) {
					writer.append("<td rowspan=\""+(sortedProps.size()*2)+"\" valign=\"top\">"+Edm.getPrefixedName(cls)+"</td>");
					first=false;
				}
				writer.append("<td valign=\"top\" rowspan=\"2\">"+prop+"</td>\r\n"
						+ "<td align=\"left\" valign=\"top\" rowspan=\"2\">"
						+ "Total literals: "+countFormat.format(statsOfItem.total()));
				writer.append("<td valign=\"top\">Without <i>xml:lang</i>: "+countFormat.format(statsOfItem.withoutLangTag)+ " ("+percentFormat.format(statsOfItem.withoutLangTagsPercent()) +"%)</td></tr>" );

				writeLangTabDetailTables(writer, statsOfItem);
			}
		}
		writer.append("</table>\n");
	}	

	private static void writeGlobalStats(Appendable writer,  LanguageStatsInDataset  stats, String title) throws IOException {
		writer.append("<h2>"+title+"</h2>\n");
		writer.append("<p>Explanation of the tables:</p>\r\n"
				+ "<ul>\r\n"
				+ "<li>Source:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 20px;\">Europeana - statements whose subject is the Europeana's ore:proxy, or a contextual entity referenced from Europeana's ore:Proxy.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">Provider - statements whose subject is an ore:proxy from any provider, or a contextual entity referenced from an ore:Proxy from any provider.</li>\r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "<li>Statistics:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 20px;\">Total literals - only properties that contain a literal value are considered.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">Without xml:lang - properties with literal values that do not contain an xml:lang tag.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">With xml:lang - properties with literal values that contain an xml:lang tag. This statistic is broken down into the following:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 30px;\">in ISO639 - values that are already according to the ISO639 standards.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">normalizable - values that are not according to the ISO639 standards but the current language normalization of Metis is able to normalize.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">not normalizable - values that are not according to the ISO639 standards and the current language normalization of Metis is unable to normalize.</li>\r\n"
				+ "<li style=\"margin-left: 30px;\">with subtags -  values that are in ISO639 or are normalizable and contain subtags.</li>\r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "<li>Files:\r\n"
				+ "<ul>\r\n"
				+ "<li style=\"margin-left: 20px;\">all.txt - a text file containing all the existing values and their number of occurrences.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">not_normalisable.txt - a text file containing all the distinct values that the current language normalization of Metis is unable to normalize.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">normalisable.txt - a text file containing all the distinct values that the current language normalization of Metis is able to normalize along with and their normalised values.</li>\r\n"
				+ "<li style=\"margin-left: 20px;\">subtags.txt - a text file containing all the distinct values that contain subtags.</li>\r\n"
				+ "</ul>\r\n"
				+ "</li> \r\n"
				+ "</ul>");

		writer.append("<h3>Usage of <i>xml:lang</i>:</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Source</b></td><td colspan=\"2\"><b>Statistics</b></td><td><b>Files</b></td></tr>\n");
		
		writeGlobalStatTableRow(writer, "Europeana", stats.fromEuropeana.calculateGlobalStatsLangTags(), "Language_Europeana_xml_lang");
		writeGlobalStatTableRow(writer, "Data provider", stats.fromProvider.calculateGlobalStatsLangTags(), "Language_Provider_xml_lang");

		writer.append("</table>\n");

		writer.append("<h3>Usage of <i>dc:language</i>:</h2>\n");
		writer.append("<table id=\"top\">\r\n"
				+ "<tr>\r\n"
				+ "<td><b>Source</b></td><td><b>Statistics</b></td><td><b>Files</b></td></tr>\n");
		writeGlobalStatTableRowDcLang(writer, "Europeana", stats.fromEuropeana.calculateGlobalStatsDcLanguage(), "Language_Europeana_dc_language");
		writeGlobalStatTableRowDcLang(writer, "Data provider", stats.fromProvider.calculateGlobalStatsDcLanguage(), "Language_Provider_dc_language");
		
		writer.append("</table>\n");
	}
	
	private static void writeGlobalStatTableRow(Appendable writer, String title, LangStatsOfItem statsOfItem, String filename) throws IOException {
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\" rowspan=\"2\">"+title+"</td>\r\n"
				+ "<td align=\"left\" valign=\"top\" rowspan=\"2\">"
				+ "Total literals: "+countFormat.format(statsOfItem.total()) + "</td>"
				+ "<td>Without <i>xml:lang</i>: "+countFormat.format(statsOfItem.withoutLangTag)+ " ("+percentFormat.format(statsOfItem.withoutLangTagsPercent()) +"%)" );
		writer.append("</td>\r\n"
				+ "<td rowspan=\"2\" valign=\"top\">");

		writer.append("<a href=\""+filename+"_all.txt\">all.txt</a>");			
		if(statsOfItem.cntNotNormalizable >0 || statsOfItem.cntSubtags>0 || statsOfItem.cntNormalizable>0) 
			writer.append("<br />");

		if(statsOfItem.cntNotNormalizable >0 || statsOfItem.cntSubtags>0 || statsOfItem.cntNormalizable>0) {
			if(statsOfItem.cntNotNormalizable >0) {
				writer.append("<a href=\""+filename+"_not_normalizable.txt\">not_normalizable.txt</a>");			
				if(statsOfItem.cntSubtags>0 || statsOfItem.cntNormalizable>0) 
					writer.append("<br />");
			}
			if(statsOfItem.cntNormalizable >0) {
				writer.append("<a href=\""+filename+"_normalizable.txt\">normalizable.txt</a>");			
				if(statsOfItem.cntSubtags>0) 
					writer.append("<br />");
			}
			if(statsOfItem.cntSubtags>0) {
				writer.append("<a href=\""+filename+"_subtags.txt\">subtags.txt</a>");						
			}
		} else
			writer.append("&nbsp;");			
		writer.append("</td></tr>\n");
		
		writeLangTabDetailTables(writer, statsOfItem);
	}
	
	private static void writeLangTabDetailTables(Appendable writer, LangStatsOfItem statsOfItem) throws IOException {	
		if(statsOfItem.withLangTags>0) {
			writer.append("<tr><td valign=\"top\">With <i>xml:lang</i>: "+countFormat.format(statsOfItem.withLangTags)+ " ("+percentFormat.format(statsOfItem.withLangTagsPercent()) +"%)");
			writer.append("<table style=\"margin-left: 15px; margin-top:5px;\">");
			if(statsOfItem.cntNormalized>0)
				writer.append("<tr><td>... in ISO639: </td><td>"+countFormat.format(statsOfItem.cntNormalized)+ " </td><td>"+percentFormat.format(statsOfItem.normalizedPercent()) +"%</td></tr>");
			if(statsOfItem.cntNormalizable>0)
				writer.append("<tr><td>... normalizable: </td><td>"+countFormat.format(statsOfItem.cntNormalizable)+ " </td><td>"+percentFormat.format(statsOfItem.normalizablePercent()) +"%</td></tr>");
			if(statsOfItem.cntNotNormalizable>0)
				writer.append("<tr><td>... not normalizable: </td><td>"+countFormat.format(statsOfItem.cntNotNormalizable)+ " </td><td>"+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%</td></tr>");
			writer.append("</table>");
			
			if(statsOfItem.cntNormalized>0) {
				writer.append("<table style=\"margin-left: 15px; margin-top:5px;\">");
				writer.append("<tr><td>... with subtags: </td><td>"+countFormat.format(statsOfItem.cntSubtags)+ " </td><td>"+percentFormat.format(statsOfItem.subtagsPercent()) +"%</td></tr>");
				writer.append("</table>");
			}
			
			writer.append("</td></tr>");
		} else {
			writer.append("<tr><td ></td></tr>");			
		}
	}	
	
	
	private static void writeGlobalStatTableRowDcLang(Appendable writer, String title, LangStatsOfItem statsOfItem, String filename) throws IOException {
		writer.append("<tr>\r\n");
		writer.append("<td valign=\"top\" rowspan=\"2\">"+title+"</td>\r\n"
				+ "<td align=\"left\" valign=\"top\" rowspan=\"2\">"
				+ "Total literals: "+countFormat.format(statsOfItem.total()));
		
		writer.append("<table style=\"margin-left: 15px; margin-top:5px;\">");
		if(statsOfItem.cntNormalized>0)
			writer.append("<tr><td>... in ISO639: </td><td>"+countFormat.format(statsOfItem.cntNormalized)+ " </td><td>"+percentFormat.format(statsOfItem.normalizedPercent()) +"%</td></tr>");
		if(statsOfItem.cntNormalizable>0)
			writer.append("<tr><td>... normalizable: </td><td>"+countFormat.format(statsOfItem.cntNormalizable)+ " </td><td>"+percentFormat.format(statsOfItem.normalizablePercent()) +"%</td></tr>");
		if(statsOfItem.cntNotNormalizable>0)
			writer.append("<tr><td>... not normalizable: </td><td>"+countFormat.format(statsOfItem.cntNotNormalizable)+ " </td><td>"+percentFormat.format(statsOfItem.notNormalizablePercent()) +"%</td></tr>");
		writer.append("</table>");
		
		if(statsOfItem.cntNormalized>0) {
			writer.append("<table style=\"margin-left: 15px; margin-top:5px;\">");
			writer.append("<tr><td>... with subtags: </td><td>"+countFormat.format(statsOfItem.cntSubtags)+ " </td><td>"+percentFormat.format(statsOfItem.subtagsPercent()) +"%</td></tr>");
			writer.append("</table>");
		}
		writer.append("</td></tr>");
		
		writer.append("</td><td rowspan=\"2\" valign=\"top\">");

		
		writer.append("<a href=\""+filename+"_all.txt\">all.txt</a>");			
		if(statsOfItem.cntNotNormalizable >0 || statsOfItem.cntSubtags>0 || statsOfItem.cntNormalizable>0) 
			writer.append("<br />");
		
		if(statsOfItem.cntNotNormalizable >0 || statsOfItem.cntSubtags>0 || statsOfItem.cntNormalizable>0) {
			if(statsOfItem.cntNotNormalizable >0) {
				writer.append("<a href=\""+filename+"_not_normalizable.txt\">not_normalizable.txt</a>");			
				if(statsOfItem.cntSubtags>0  || statsOfItem.cntNormalizable>0) 
					writer.append("<br />");
			}
			if(statsOfItem.cntNormalizable >0) {
				writer.append("<a href=\""+filename+"_normalizable.txt\">normalizable.txt</a>");			
				if(statsOfItem.cntSubtags>0) 
					writer.append("<br />");
			}
			if(statsOfItem.cntSubtags>0) {
				writer.append("<a href=\""+filename+"_subtags.txt\">subtags.txt</a>");
			}
		} else
			writer.append("&nbsp;");
		writer.append("</td></tr>\n");
	}
		
	
	private static void writeStart(Appendable writer) throws IOException {
		writer.append("<html>\r\n"
				+ "<head>\r\n"
				+ "<meta charset=\"UTF-8\">\r\n"
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
		writer.append("\n<p>(Report creation date: "+ new SimpleDateFormat("d MMM yyyy").format(new Date())+")</p>"
				+ "\n</body>\r\n"
				+ "</html>");
	}

	
}
