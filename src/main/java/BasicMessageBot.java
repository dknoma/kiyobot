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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
	private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
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

		PostgresHandler pghandler = new PostgresHandler(models);

		// Used if need multiple handlers for different database connections
//		String dbName = sqlparser.getDbName();
//		JDBCEnum.addJDBCHandler(dbName, pghandler);
//		JDBCHandler dbhandler = JDBCEnum.getJDBCHandler(dbName);

		// Connects the PostgreSQLhandler to the Postgres database
		pghandler.setConnection(sqlparser.getDb(), sqlparser.getHost(), sqlparser.getPort(),
				sqlparser.getUsername(), sqlparser.getPassword());

		// Diskiyord setup
		JsonConfigArgParser parser = new JsonConfigArgParser();
		parser.parseConfig();
		String botStuffId = parser.getBotStuff();
		DiskiyordApi api = DiskiyordApiBuilder.buildApi(parser.getAuthTok());

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
						//!addexgfx  ExGFX100  Test  test  false  img.link
						if(messageArgs.length != 6) {
							messageEvent.getChannel().sendTextMessage(errorMessage);
							break;
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
					case "!getevent":
						// get info on a specific event
						String eventId = messageArgs[1];
						getEvent(messageEvent, eventId);
						break;
					case "!getevents":
						// list of all events
						getEvents(messageEvent);
						break;
					case "!getuser":
						// Gets user info, including the info of all the events the user has tickets to
						String userId = messageArgs[1];
						getUser(messageEvent, userId);
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
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getEvent(MessageCreateListener messageEvent, String eventId) {
		try {
			URL userService = new URL(PROJECT4_PATH + "/events/" + eventId);
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				// Read the json line from the service.
				String line = reader.readLine();
				// Checks the response code from the events service
				switch(connection.getResponseCode()) {
					case 200:
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
						break;
					case 400:
						messageEvent.getChannel().sendTextMessage("Event not found.");
						break;
					default:
						int responseCode = connection.getResponseCode();
						messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
						break;
				}
			} catch(IOException ioe) {
				LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
			}
		} catch (IOException ioe) {
			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
		}
	}

	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getEvents(MessageCreateListener messageEvent) {
		try {
			URL userService = new URL(PROJECT4_PATH + "/events");
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				// Read the json line from the service.
				String line = reader.readLine();
				// Checks the response code from the events service
				switch(connection.getResponseCode()) {
					case 200:
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
						break;
					case 400:
						messageEvent.getChannel().sendTextMessage("Events not found.");
						break;
					default:
						int responseCode = connection.getResponseCode();
						messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
						break;
				}
			} catch(IOException ioe) {
				LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
			}
		} catch (IOException ioe) {
			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
		}
	}


	/**
	 * Connects to the website and performs the appropriate methods
	 * @param messageEvent;
	 */
	private static void getUser(MessageCreateListener messageEvent, String userId) {
		try {
			URL userService = new URL(PROJECT4_PATH + "/users/" + userId);
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				// Read the json line from the service.
				String line = reader.readLine();
				// Checks the response code from the events service
				switch(connection.getResponseCode()) {
					case 200:
						messageEvent.getChannel().sendTextMessage(prettyJson(line));
						break;
					case 400:
						messageEvent.getChannel().sendTextMessage("Event not found.");
						break;
					default:
						int responseCode = connection.getResponseCode();
						messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
						break;
				}
			} catch(IOException ioe) {
				LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
			}
		} catch (IOException ioe) {
			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
		}
	}

	/**
	 * Sends unkown command message to the channel
	 * @param messageEvent;
	 */
	private static void getCommands(MessageCreateListener messageEvent) {
		messageEvent.getChannel().sendTextMessage("!ping\n\t- A generic ping message. Please don't overuse.\n" +
				"!hewwo\n\t- What's this?\n!addexgfx  <filename>  <description>  <type>  <completed>  <imglink>\n" +
				"\t- Use this command to add information on an ExGFX file to the database.\n!getexgfx  <filename>\n" +
				"\t- Use this command to get back the information on an ExGFX file from the database.");
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
}
