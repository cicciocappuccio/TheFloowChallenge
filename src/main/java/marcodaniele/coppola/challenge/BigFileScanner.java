package marcodaniele.coppola.challenge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigFileScanner {
	private Scanner sc;
	private FileInputStream inputStream;
	private String fileName;

	public BigFileScanner(String fileName) {
		super();

		this.fileName = fileName;

	}

	public Map<String, Long> scan(BigInteger lineNumber) {

		Map<String, Long> bagOfWords = new HashMap<String, Long>();

		try {
			inputStream = new FileInputStream(fileName);
			sc = new Scanner(inputStream, "UTF-8");

			// lineNumber.subtract(BigInteger.ONE);
			while (lineNumber.compareTo(BigInteger.ZERO) > 0 && sc.hasNextLine()) {
				lineNumber = lineNumber.subtract(BigInteger.ONE);
				sc.nextLine();
			}
			Scanner lineScanner = null;
			while (sc.hasNextLine()) {

				String line = sc.nextLine();

				try {
					lineScanner = new Scanner(line);

					while (lineScanner.hasNext()) {
						String token = lineScanner.next();
						
						Pattern p = Pattern.compile("^[a-zA-Z0-9]*$");

				        Matcher matcher = p.matcher(token);
						
				        if(matcher.matches())
				        {
				        	//matcher.group();
				        	System.out.printf("group: %s%n", matcher.group());
				        }
						if (!(token.startsWith("<") || token.endsWith(">"))) {

							Long occurenceOfToken = bagOfWords.get(token);

							if (occurenceOfToken == null)
								occurenceOfToken = 1l;
							else
								occurenceOfToken = occurenceOfToken + 1;

							bagOfWords.put(token, occurenceOfToken);
						}
					}
				} catch (NoSuchElementException ex) {
					bagOfWords = null;
					ex.printStackTrace();
				} catch (Exception e) {
					bagOfWords = null;
					e.printStackTrace();
				} finally {
					lineScanner.close();
				}
			}

			if (sc.ioException() != null) {
				throw sc.ioException();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			bagOfWords = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			bagOfWords = null;
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					bagOfWords = null;
					e.printStackTrace();
				}
			}
			if (sc != null) {
				sc.close();
			}

		}

		return bagOfWords;
	}

}
