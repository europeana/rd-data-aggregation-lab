package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inescid.util.datastruct.BigSet;

public class EntityTrackerOnDisk implements EntityTracker {
		static Pattern simplifyPattern=Pattern.compile("^https?://([^/]+)/(.*)"); 
		
		BigSet<String> processed;
		Source detectFor;
		
		public EntityTrackerOnDisk(File setFolder, Source detectFor) {
			processed=new BigSet<String>("EntityTrackerOnDisk",setFolder);
			this.detectFor = detectFor;
		}

		public boolean contains(Source source, String uri) {
			if(detectFor != Source.ANY && source!=detectFor)
				return false;
			if(!uri.startsWith("http"))
				return false;
			try {
				return processed.containsSynchronized(simplifyURI(uri));
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		
		public boolean add(Source source, String uri) {
			if(detectFor != Source.ANY && source!=detectFor)
				return false;
			if(!uri.startsWith("http"))
				return false;
			try {
				return processed.addSynchronized(simplifyURI(uri));
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		
		private static String simplifyURI(String uri) {
			Matcher m = simplifyPattern.matcher(uri);
			if(m.matches()) {
				return intToString(m.group(1).hashCode())+m.group(2);
			} else
				return uri;
		}
		
		private static String intToString(int i) {
			ByteBuffer b = ByteBuffer.allocate(4);
			b.putInt(i);
			return new String(b.array(), StandardCharsets.UTF_8);
		}
		
		public static void test() {
			String testURI="http://wikidataorg/123";
			System.out.println(testURI); 
			System.out.println(simplifyURI(testURI)); 
		}
	}