package marcodaniele.coppola.challenge;

import java.math.BigInteger;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoDB {
	private static final String NUMBER = "number";
	private static final String TOKEN = "token";
	private static final String BAG_OF_WORDS = "bagOfWords";
	private static final String END = "end";
	private static final String LINE = "line";
	private static final String SERVER_ID = "server_id";
	private static final String SERVERS = "servers";
	private String machine;
	private Integer port;

	private MongoClient mongoClient;
	private MongoDatabase database;

	public MongoDB(String machine, String port, String dbName) {

		if (machine.isEmpty()) {
			// TODO parameters
			this.machine = "";
			this.port = 27017;
		}

		else {
			this.machine = machine;
			if (port == null || port.isEmpty())
				// TODO parameters
				this.port = 27017;
			else {
				try {
					Integer.valueOf(port);
				} catch (NumberFormatException e) {
					System.err.println("Caught NumberFormatException: " + e.getMessage());

				}
				this.port = Integer.valueOf(port);

			}
		}

		System.out.println("Try connection with MongoDB instance: " + this.machine + ":" + this.port.toString());

		mongoClient = new MongoClient(this.machine, this.port);
		System.out.println("try to connect to mongodb [" + this.machine + ":" + this.port+"] on bd[" + dbName+"]");
		database = mongoClient.getDatabase(dbName);

		System.out.println("Goooooooodmornig mongoDB");

	}

	public BigInteger lastLine(String serverID) {

		MongoCollection<Document> servers = database.getCollection(SERVERS);

		FindIterable<Document> cursor = servers.find();
		MongoCursor<Document> iterator = cursor.iterator();

		BigInteger lastLine = BigInteger.ZERO;

		while (iterator.hasNext()) {
			Document server = iterator.next();

			BigInteger line = new BigInteger(server.getString(LINE));

			if (lastLine.compareTo(line) < 0) {
				lastLine = line;
			}

		}

		return lastLine;
	}

	public void newBlock(String serverID, BigInteger newLastLine) {
		MongoCollection<Document> servers = database.getCollection(SERVERS);

		// FindIterable<Document> cursor = servers.find();
		// MongoCursor<Document> iterator = cursor.iterator();

		Document server = new Document(SERVER_ID, serverID);
		server.append(LINE, newLastLine.toString());

		servers.insertOne(server);

	}

	public void writeBOW(String serverID, Map<String, Long> bagOfWords) {

		MongoCollection<Document> servers = database.getCollection(SERVERS);

		FindIterable<Document> cursor = servers.find();
		MongoCursor<Document> iterator = cursor.iterator();

		while (iterator.hasNext()) {
			
			Document i = iterator.next();
			
			if(i.containsKey(SERVER_ID) && i.getString(SERVER_ID).equals(serverID) && !(i.containsKey(END) ))
			{
				updateBagOfWords(bagOfWords);

				servers.updateOne(Filters.and(Filters.eq(LINE,i.get(LINE)), Filters.eq(SERVER_ID, serverID)), new Document("$set", new Document(END, "TRUE")));
				
			}
			
		}
	}

	private void updateBagOfWords(Map<String, Long> bagOfWords) {
		MongoCollection<Document> bowCollection = database.getCollection(BAG_OF_WORDS);
		
		
		//FindIterable<Document> bowCursor = bowCollection.find();

		for(String iWord : bagOfWords.keySet())
		{
			FindIterable<Document> words = bowCollection.find(Filters.eq(TOKEN,iWord));
			
			
			
			MongoCursor<Document> iterator = words.iterator();
			if(iterator.hasNext())
			{
				bowCollection.updateOne(Filters.eq(TOKEN,iWord), new Document("$inc", new Document(NUMBER, bagOfWords.get(iWord))));
				
			}
			else
			{
				Document newWord = new Document(TOKEN,iWord);
				newWord.append(NUMBER, bagOfWords.get(iWord));
				bowCollection.insertOne(newWord);
			}
			
		}
	}
	
	public void showBOW()
	{
		System.out.println("The following words were found on the file");
		MongoCollection<Document> bowCollection = database.getCollection(BAG_OF_WORDS);
		
		FindIterable<Document> bow = bowCollection.find();
		MongoCursor<Document> iterator = bow.iterator();
		
		while(iterator.hasNext())
		{
			Document word = iterator.next();
			System.out.println("the word '" + word.getString(TOKEN) + "' was found " + word.getLong(NUMBER));
			
		}
		
		System.out.println("\nGoodbye.");
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public void close() {
		mongoClient.close();
	}
}
