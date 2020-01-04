import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TranslationFileReader {
	
	public static List<Translation> readTranslationsFromFile(String file) throws IOException {
		List<Translation> translations = new ArrayList<Translation>(17000);
		
		FileInputStream fIS = new FileInputStream(file);
		InputStreamReader isR = new InputStreamReader(fIS, Charset.forName("UTF-8"));
		StringBuilder sB = new StringBuilder();
		int letterCode = isR.read();
		
		while (letterCode != -1) {
			sB.append((char) letterCode);
			letterCode = isR.read();
		}
		
		isR.close();
		String inputString = sB.toString();
		String[] stringArray = inputString.split("\n");
		
		for (int i = 0; i < stringArray.length; i++) {
			String nextSubString = stringArray[i].trim();
			if (nextSubString.equals("") || nextSubString.startsWith("#")) {
				continue;
			}
			
			String oldID = nextSubString.substring(0, nextSubString.indexOf(':')).toLowerCase();
			String newID = nextSubString.substring(nextSubString.indexOf(':')+1);
			translations.add(new Translation(oldID, newID));
		}
		
		return translations;
	}
}
