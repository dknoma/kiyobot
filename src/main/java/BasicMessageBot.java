import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import diskiyord.api.DiskiyordApi;
import diskiyord.api.DiskiyordApiBuilder;
import diskiyord.event.error.MessageArgumentError;
import diskiyord.event.message.MessageCreateListener;
import diskiyord.util.JsonConfigArgParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.jdbc.*;
import sql.model.SQLModel;
import sql.util.JsonSqlConfigParser;
import sql.util.SQLModelBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;


/**
 * Packets sent from the client to the Gateway API are encapsulated within a gateway payload
 * object and must have the proper opcode and data object set. The payload object can then be
 * serialized in the format of choice (see ETF/JSON), and sent over the websocket. Payloads to
 * the gateway are limited to a maximum of 4096 bytes sent, going over this will cause a
 * connection termination with error code 4002.
 *
 *  {
 *      "op": 0,
 *      "d": {},
 *      "s": 42,
 *      "t": "GATEWAY_EVENT_NAME"
 *  }
 *
 *  String.format(%[argument_index$][flags][width]conversion);
 *      %s - put string in
 *      %1$s - put the first string arg here
 *
 *
 */
public class BasicMessageBot {

	private static int PINGS = 0;

	private static final Gson GSON = new Gson();
	private static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final int SC_OK = 200;
	private static final int SC_BAD_REQUEST = 400;
	private static final String EXGFX = "exgfx";
	private static final String FILENAME = "filename";
	private static final String DESCRIPTION = "description";
	private static final String TYPE = "type";
	private static final String COMPLETED = "completed";
	private static final String IMG_LINK = "imglink";
	private static final String SQL_CONFIG_FILE = "./config/sqlconfig.json";
	private static final String PROJECT4_PATH = "http://127.0.0.1:9000/api";
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		// db setup
		JsonSqlConfigParser sqlparser = new JsonSqlConfigParser();
		sqlparser.parseConfig(SQL_CONFIG_FILE);
		String modelDirectory = sqlparser.getModelDirectory();

		SQLModelBuilder builder = new SQLModelBuilder();
		builder.findModelFiles(modelDirectory);
		builder.readFiles();

		if(!builder.areModelsFormattedCorrectly()) {
			return;
		}

		Map<String, SQLModel> models = builder.getCopyOfModels();
		JDBCHandler pghandler = new PostgresHandler(models);
		// Connects the PostgreSQLhandler to the Postgres database
		pghandler.setConnection(sqlparser.getDb(), sqlparser.getHost(), sqlparser.getPort(),
				sqlparser.getUsername(), sqlparser.getPassword());

		// Diskiyord setup
		JsonConfigArgParser parser = new JsonConfigArgParser();
		parser.parseConfig();
		// Used if need to have bot output to this specific channel
		String botStuffChannelId = parser.getBotStuff();
		DiskiyordApi api = DiskiyordApiBuilder.buildApi(parser.getAuthTok());
		// Adds a message listener
		listenOnMessage(api, pghandler);
	}

	/**
	 * Adds message listener to the api, which allows the bot to listen to Discord messages
	 * @param api - Diskiyord API class
	 * @param pghandler - JDBCHandler to handle all SQL queries
	 */
	private static void listenOnMessage(DiskiyordApi api, JDBCHandler pghandler) {
		// Message listener
		api.addMessageCreateListener(messageEvent -> {
			String message = messageEvent.getMessageContent();
			String[] messageArgs = message.split(" {2}");
			String errorMessage = "An error has occurred.";
			try {
				switch(messageArgs[0]) {
					// Random commads
					case "!ping":
						if(PINGS < 3) {
							messageEvent.getChannel().sendTextMessage("Pong!");
						} else if(PINGS >= 5) {
							messageEvent.getChannel().sendTextMessage("https://i.imgur.com/gOJdCJS.gif");
						} else {
							messageEvent.getChannel().sendTextMessage("...");
						}
						PINGS++;
						break;
					case "!hewwo":
						PINGS = 0;
						messageEvent.getChannel().sendTextMessage("*notices command* OwO what's this?");
						break;
					// Database commands
					case "!addexgfx":
						PINGS = 0;
						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
						//!addexgfx  <filename>  <description>  <type>  <completed>  <imglink>
						addExgfx(messageEvent, pghandler, messageArgs, errorMessage);
						break;
					case "!getexgfx":
						PINGS = 0;
						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
						//!getexgfx  <filename>
						if(messageArgs.length != 2) {
							messageEvent.getChannel().sendTextMessage(errorMessage);
							break;
						}
						JsonObject obj = GSON.fromJson(ResultSetHandler
								.resultSetToString(pghandler, "*", EXGFX, FILENAME, messageArgs[1]), JsonObject.class);
						String botOutput = getExGFXInfo(obj);
						messageEvent.getChannel().sendTextMessage(botOutput);
						break;
					// Project4 comands
					case "!createevent":
						// Gets user info, including the info of all the events the user has tickets to
						String userid = messageArgs[1];
						String eventname = messageArgs[2];
						String maxTickets = messageArgs[3];
						try {
							int userId = Integer.parseInt(userid);
							int numTickets = Integer.parseInt(maxTickets);
							createEvent(messageEvent, userId, eventname, numTickets);
						} catch(NumberFormatException nfe) {
							messageEvent.getChannel().sendTextMessage("User id and numtickets args must be integers.");
						}
						break;
					case "!getevent":
						// get info on a specific event
						String eventid = messageArgs[1];
						getEvent(messageEvent, eventid);
						break;
					case "!getevents":
						// list of all events
						getEvents(messageEvent);
						break;
					case "!createuser":
						// Gets user info, including the info of all the events the user has tickets to
						String username = messageArgs[1];
						createUser(messageEvent, username);
						break;
					case "!getuser":
						// Gets user info, including the info of all the events the user has tickets to
						userid = messageArgs[1];
						getUser(messageEvent, userid);
						break;
					case "!purchasetickets":
						// Gets user info, including the info of all the events the user has tickets to
						eventid = messageArgs[1];
						userid = messageArgs[2];
						String numTickets = messageArgs[3];
						try {
							int eventId = Integer.parseInt(eventid);
							int userId = Integer.parseInt(userid);
							int tickets = Integer.parseInt(numTickets);
							purchaseTickets(messageEvent, eventId, userId, tickets);
						} catch(NumberFormatException nfe) {
							messageEvent.getChannel().sendTextMessage("Event id, user id, and tickets args must be integers.");
						}
						break;
					case "!transfertickets":
						// Gets user info, including the info of all the events the user has tickets to
						eventid = messageArgs[1];
						userid = messageArgs[2];
						String targetuser = messageArgs[3];
						numTickets = messageArgs[4];
						try {
							int eventId = Integer.parseInt(eventid);
							int userId = Integer.parseInt(userid);
							int targetUser = Integer.parseInt(targetuser);
							int tickets = Integer.parseInt(numTickets);
							transferTickets(messageEvent, eventId, userId, targetUser, tickets);
						} catch(NumberFormatException nfe) {
							messageEvent.getChannel().sendTextMessage("Event id, user id, target user, and tickets args must be integers.");
						}
						break;
					// Basic commads
					case "!commands":
						getCommands(messageEvent);
						break;
					default:
						if(messageArgs[0].startsWith("!")) {
							errorMessage = MessageArgumentError.UNKNOWN_COMMAND.getErrorMsg();
							messageEvent.getChannel().sendTextMessage(errorMessage);
						}
						break;
				}
			} catch(ArrayIndexOutOfBoundsException aiobe) {
				messageEvent.getChannel().sendTextMessage(errorMessage);
			} catch (SQLException e) {
				messageEvent.getChannel().sendTextMessage(String.format("SQL error: %1$s", e.getMessage()));
			}
		});
	}

	/**
	 * Performs insert of exgfx to database
	 * @param messageEvent;
	 * @param pghandler;
	 * @param messageArgs;
	 * @param errorMessage;
	 * @throws SQLException;
	 */
	private static void addExgfx(MessageCreateListener messageEvent, JDBCHandler pghandler, String[] messageArgs, String errorMessage) throws SQLException{
		if(messageArgs.length != 6) {
			messageEvent.getChannel().sendTextMessage(errorMessage);
			return;
		}
		// Can make Class to handle all exgfx related methods
		ColumnObject[] columns = new ColumnObject[5];
		columns[0] = new ColumnObject<>(FILENAME, messageArgs[1]);
		columns[1] = new ColumnObject<>(DESCRIPTION, messageArgs[2]);
		columns[2] = new ColumnObject<>(TYPE, messageArgs[3]);
		columns[3] = new ColumnObject<>(COMPLETED, Boolean.parseBoolean(messageArgs[4]));
		columns[4] = new ColumnObject<>(IMG_LINK, messageArgs[5]);
		pghandler.executeUpdate(pghandler.insert(EXGFX, columns));
		messageEvent.getChannel().sendTextMessage("Data successfully added to the database!");
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void createEvent(MessageCreateListener messageEvent, int userId, String eventname, int numTickets) {
		postToService(messageEvent, PROJECT4_PATH + "/events/create",
				"{\"userid\":%1$d,\"eventname\":\"%2$s\",\"numtickets\":%3$d}",
				userId, eventname, numTickets);
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getEvent(MessageCreateListener messageEvent, String eventId) {
		getToService(messageEvent, PROJECT4_PATH + "/events/" + eventId);
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getEvents(MessageCreateListener messageEvent) {
		getToService(messageEvent, PROJECT4_PATH + "/events");
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void createUser(MessageCreateListener messageEvent, String username) {
		postToService(messageEvent, PROJECT4_PATH + "/users/create",
				"{\"username\":\"%s\"}", username);
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getUser(MessageCreateListener messageEvent, String userId) {
		getToService(messageEvent, PROJECT4_PATH + "/users/" + userId);
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void purchaseTickets(MessageCreateListener messageEvent, int eventId, int userId, int tickets) {
		postToService(messageEvent, String.format("%1$s/events/%2$d/purchase/%3$d", PROJECT4_PATH, eventId, userId),
				"{\"tickets\":%1$d}", tickets);
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void transferTickets(MessageCreateListener messageEvent, int eventId, int userId, int targetUser, int tickets) {
		postToService(messageEvent, String.format("%1$s/users/%2$d/tickets/transfer", PROJECT4_PATH, userId),
				"{\"eventid\":%1$d,\"tickets\":%2$d,\"targetuser\":%3$d}", eventId, tickets, targetUser);
	}

	/**
	 * Performs a GET to the service
	 * @param messageEvent - bots listener
	 * @param url - url of service
	 */
	private static void getToService(MessageCreateListener messageEvent, String url) {
		try {
			URL userService = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			switch(connection.getResponseCode()) {
				case SC_OK:
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
						// Read the json line from the service.
						String line = reader.readLine();
						// Checks the response code from the events service
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
					} catch(IOException ioe) {
						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
					}
					break;
				case SC_BAD_REQUEST:
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
						// Read the json line from the service.
						String line = reader.readLine();
						// Checks the response code from the events service
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
					} catch(IOException ioe) {
						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
					}
					break;
				default:
					int responseCode = connection.getResponseCode();
					messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
					break;
			}
		} catch (IOException ioe) {
			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
		}
	}

	/**
	 * Performs a POST to the service
	 * @param messageEvent - bots listener
	 * @param url - url of service
	 * @param jsonBodyFormat - Format of the json body to be used in String.format(...)
	 * @param params - varargs to insert into the json body
	 */
	@SafeVarargs
	private static <T> void postToService(MessageCreateListener messageEvent, String url,
										  String jsonBodyFormat, T... params) {
		try {
			URL userService = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			OutputStream outstream = connection.getOutputStream();
			outstream.write(String.format(jsonBodyFormat, params).getBytes());

			switch (connection.getResponseCode()) {
				case SC_OK:
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
						// Read the json line from the service.
						String line = reader.readLine();
						// Checks the response code from the events service
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
					} catch(IOException ioe){
						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
					}
					break;
				case SC_BAD_REQUEST:
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
						// Read the json line from the service.
						String line = reader.readLine();
						// Checks the response code from the events service
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
					} catch(IOException ioe){
						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
					}
					break;
				default:
					int responseCode = connection.getResponseCode();
					messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
					break;
			}
			outstream.flush();
			outstream.close();
		} catch (IOException ioe) {
			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
		}
	}

	/**
	 * Turns a json string from db output into a readable string for the bot to output
	 * @param obj;
	 * @return bot message
	 */
	private static String getExGFXInfo(JsonObject obj) {
		if(obj == null || obj.isJsonNull()) {
			return "File does not exist";
		} else {
			return String.format("File: %1$s\nDescription: %2$s\nType: %3$s\nCompleted: %4$s\nImage Link: %5$s",
					obj.get(FILENAME), obj.get(DESCRIPTION), obj.get(TYPE), obj.get(COMPLETED), obj.get(IMG_LINK));
		}
	}

	/**
	 * Prints pretty json
	 * @param json;
	 * @return pretty print
	 */
	private static String prettyJson(String json) {
		JsonElement ele = GSON_PRETTY.fromJson(json, JsonElement.class);
		String out = "";
		if(ele.isJsonObject()) {
			out = GSON_PRETTY.toJson(ele.getAsJsonObject());
			System.out.println(out);
		} else if(ele.isJsonArray()) {
			out = GSON_PRETTY.toJson(ele.getAsJsonArray());
			System.out.println(out);
		} else if(ele.isJsonPrimitive()) {
			out = GSON_PRETTY.toJson(ele.getAsJsonPrimitive());
			System.out.println(out);
		}
		return out;
	}

	/**
	 * Sends unkown command message to the channel
	 * @param messageEvent;
	 */
	private static void getCommands(MessageCreateListener messageEvent) {
		messageEvent.getChannel().sendTextMessage("**General Bot Commands**\n------------------------\n" +
				"!ping\n\t- A generic ping message. Please don't overuse.\n" +
				"!hewwo\n\t- What's this?\n" +
				"**ExGFX Commands**\n------------------\n" +
				"!addexgfx  <filename>  <description>  <type>  <completed>  <imglink>\n" +
				"\t- Use this command to add information on an ExGFX file to the database.\n!getexgfx  <filename>\n" +
				"\t- Use this command to get back the information on an ExGFX file from the database.\n" +
				"**Eventer: An Event Ticket Service**\n" +
				"------------------------------------\n" +
				"!createevent  <userid>  <eventname>  <max_tickets>\n" +
				"\t- Creates an event in the event service.\n" +
				"!getevent  <eventid>\n\t- Gets a specific event from the event service.\n" +
				"!getevents\n\t- Gets all events from the event service.\n" +
				"!createuser  <username>\n\t- Creates a user in the user service.\n" +
				"!getuser  <userid>\n\t- Gets a specific user from the user service.\n" +
				"!purchasetickets  <eventid>  <userid>  <number_to_purchase>\n" +
				"\t- Purchase a number of tickets to a specific event for a user from the Eventer service.\n" +
				"!transfertickets  <eventid>  <userid>  <targetuser>  <number_to_purchase>\n" +
				"\t- Transfer a number of tickets to a specific event from a user to another user from the Eventer service.\n"
		);
	}

}
