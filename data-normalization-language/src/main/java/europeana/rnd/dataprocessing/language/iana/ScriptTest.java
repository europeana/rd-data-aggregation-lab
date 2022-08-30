package europeana.rnd.dataprocessing.language.iana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ScriptTest {

	
	public static void main(String[] args) throws Exception {
		Registry reg=new Registry();
		LangTagValidator validator=new LangTagValidator(reg);
		{
			FileInputStream is=new FileInputStream(new File("src/test/resources/subtags.txt"));
			BufferedReader reader=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			Subtag currentTag=null;
			for(String line = reader.readLine() ; line!=null ; line=reader.readLine() ) {
	//			String tag=line.substring(0, line.indexOf(','));
				String tag=line;
				ValidationReport report = validator.validate(tag);
				
				System.out.println(tag +" - "+
				report.isValid() + " - " + report.isPartiallyValid() + " - " + report.getValidTag());
		
				
			}
		}
		{
			System.out.println("\nNOT NORMALISABLE");
			FileInputStream is=new FileInputStream(new File("src/test/resources/not-normalisable.txt"));
			BufferedReader reader=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			Subtag currentTag=null;
			for(String line = reader.readLine() ; line!=null ; line=reader.readLine() ) {
				//			String tag=line.substring(0, line.indexOf(','));
				String tag=line;
				ValidationReport report = validator.validate(tag);
				
				System.out.println(tag +" - "+
						report.isValid() + " - " + report.isPartiallyValid() + " - " + report.getValidTag());
				
				
			}
		}
	}
}
