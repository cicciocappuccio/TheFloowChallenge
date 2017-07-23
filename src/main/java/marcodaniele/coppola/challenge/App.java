package marcodaniele.coppola.challenge;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class App {
	private static final String ID = "–id";
	private static final String MONGO = "–mongo";
	private static final String SOURCE = "–source";
	private static MongoDB mongoDB;

	public static void main(String[] args) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put(new String(SOURCE), null);
		parameters.put(new String(MONGO), null);
		parameters.put(new String(ID), null);

		// for (String p : parameters.keySet()) {

		for (int i = 0; i < args.length - 1; i++) {
			if ((args.length >= i + 1) && !parameters.containsKey(args[i + 1]) && parameters.containsKey(args[i])) {
				parameters.put(args[i], args[i + 1]);
			}
		}
		// }
		System.out.println(parameters);

		if (parameters.get(ID) == null) {
			parameters.put(ID, UUID.randomUUID().toString());

		}

		System.out.println(parameters.get(ID));

		String current;
		try {
			current = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir:" + current);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<String> mongoParameters = extractMongoParameter(parameters);
		System.out.println(mongoParameters);
		System.out.println(parameters.get(SOURCE).split("\\.")[0]);
		mongoDB = new MongoDB(mongoParameters.get(0), mongoParameters.get(1), parameters.get(SOURCE).split("\\.")[0]);
		try {

			boolean fileEnd = false;

			do {
				BigInteger lastLine = mongoDB.lastLine(parameters.get(ID));

				System.out.println(lastLine);
				System.out.println(parameters.get(ID));
				mongoDB.newBlock(parameters.get(ID), lastLine.add(new BigInteger("100")));

				Map<String, Long> bagOfWords = new HashMap<String, Long>();

				// TODO rimuovi
				// bagOfWords.put("parola", 4l);

				BigFileScanner scanner = new BigFileScanner(parameters.get(SOURCE));

				bagOfWords = scanner.scan(lastLine);

				if (bagOfWords == null) {

					System.out.println("ERRROR");
					// TODO qualcosa e' andato storto
				} else {

					mongoDB.writeBOW(parameters.get(ID), bagOfWords);

					if (bagOfWords.isEmpty()) {
						fileEnd = true;
					}

				}

			} while (!fileEnd);

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
			System.out.println(e.toString());
			e.printStackTrace();
		}

		finally {
			mongoDB.close();
		}
	}

	private static List<String> extractMongoParameter(HashMap<String, String> parameters) {
		List<String> mongoParameters = new ArrayList<String>();
		if (parameters.containsKey(MONGO)) {
			System.out.println();
			mongoParameters = new LinkedList<String>(Arrays.asList(parameters.get(MONGO).split(":")));

			if (mongoParameters.size() == 1) {
				mongoParameters.add("27017");
			}

		}
		return mongoParameters;
	}
}
