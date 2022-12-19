package europeana.rnd.dataprocessing.language;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class EntityTrackerOnMemory implements EntityTracker{
		static Pattern simplifyPattern=Pattern.compile("^https?://([^/]+)/(.*)"); 
		
		UnifiedSet<String> processed=new UnifiedSet<String>();
		Source detectFor;
		
		public EntityTrackerOnMemory(Source detectFor) {
			super();
			this.detectFor = detectFor;
		}

		public boolean contains(Source source, String uri) {
			if(detectFor != Source.ANY && source!=detectFor)
				return false;
			if(!uri.startsWith("http"))
				return false;
			return processed.contains(simplifyURI(uri));
		}
		
		public boolean add(Source source, String uri) {
			if(detectFor != Source.ANY && source!=detectFor)
				return false;
			if(!uri.startsWith("http"))
				return false;
			return processed.add(simplifyURI(uri));
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