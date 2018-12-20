package kiyobot.message;

import com.google.gson.*;
import diskiyord.api.DiskiyordApi;
import diskiyord.event.error.MessageArgumentError;
import diskiyord.event.message.MessageCreateListener;
import jql.sql.jdbc.ColumnObject;
import jql.sql.jdbc.JDBCEnum;
import jql.sql.jdbc.JDBCHandler;
import jql.sql.jdbc.SQLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MessageEvent {

	INSTANCE();

	MessageEvent(){}

	private int PINGS = 0;

	private final Gson GSON = new Gson();
	private final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private final String EXGFX = "exgfx";
	private final String FILENAME = "filename";
	private final String DESCRIPTION = "description";
	private final String TYPE = "type";
	private final String COMPLETED = "completed";
	private final String IMG_LINK = "imglink";
	private final String ADD_EXGFX_REGEX = "!addexgfx (\\p{XDigit}+?) (\".*\") (\".+\") (\\w+?) (.+)";
	private final String GET_EXGFX_REGEX = "!getexgfx (\\p{XDigit}+?)";
	private final String GET_ALL_EXGFX_REGEX = "!getallexgfx";
	private final Logger LOGGER = LogManager.getLogger();

	/**
	 * Adds message listener to the api, which allows the bot to listen to Discord messages
	 * @param api - Diskiyord API class
	 * @param pghandler - JDBCHandler to handle all SQL queries
	 */
	public void listenOnMessage(DiskiyordApi api, JDBCHandler pghandler) {
		// Message listener
		api.addMessageCreateListener(messageEvent -> {
			String message = messageEvent.getMessageContent();
			JDBCHandler handler = JDBCEnum.INSTANCE.getJDBCHandler();
			Matcher matcher;
			if((matcher = Pattern.compile(ADD_EXGFX_REGEX).matcher(message)).matches()) {
				addExgfx(messageEvent, matcher);
			} else if((matcher = Pattern.compile(GET_EXGFX_REGEX).matcher(message)).matches()) {
				getExGFXInfo(messageEvent, matcher, handler);
			} else if(Pattern.compile(GET_ALL_EXGFX_REGEX).matcher(message).matches()) {
				getAllExGFX(messageEvent);
			} else if(Pattern.compile("!ping").matcher(message).matches()) {
				if(PINGS < 3) {
					messageEvent.getChannel().sendTextMessage("Pong!");
				} else if(PINGS >= 5) {
					messageEvent.getChannel().sendTextMessage("https://i.imgur.com/gOJdCJS.gif");
				} else {
					messageEvent.getChannel().sendTextMessage("...");
				}
				PINGS++;
			} else if(Pattern.compile("!hewwo").matcher(message).matches()) {
				messageEvent.getChannel().sendTextMessage("*notices command* OwO what's this?");
			}
//			String[] messageArgs = message.split(" {2}");
//			String errorMessage = "An error has occurred.";
//			try {
//				switch(messageArgs[0]) {
//					// Random commads
//					case "!ping":
//						if(PINGS < 3) {
//							messageEvent.getChannel().sendTextMessage("Pong!");
//						} else if(PINGS >= 5) {
//							messageEvent.getChannel().sendTextMessage("https://i.imgur.com/gOJdCJS.gif");
//						} else {
//							messageEvent.getChannel().sendTextMessage("...");
//						}
//						PINGS++;
//						break;
//					case "!hewwo":
//						PINGS = 0;
//						messageEvent.getChannel().sendTextMessage("*notices command* OwO what's this?");
//						break;
//					// Database commands
//					case "!addexgfx":
//						PINGS = 0;
//						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
//						//!addexgfx  <filename>  <description>  <type>  <completed>  <imglink>
//						addExgfx(messageEvent, pghandler, messageArgs, errorMessage);
//						break;
//					case "!getexgfx":
//						PINGS = 0;
//						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
//						//!getexgfx  <filename>
//						if(messageArgs.length != 2) {
//							messageEvent.getChannel().sendTextMessage(errorMessage);
//							break;
//						}
//						try {
//							int hexadecimal = Integer.parseInt(messageArgs[1], 16);
//							String hexString = Integer.toHexString(hexadecimal).toUpperCase();
//							String exgfxFilename = String.format("ExGFX%s", hexString);
//							LOGGER.debug("exgfx: {}", exgfxFilename);
//							JsonObject obj = GSON.fromJson(dbManager.resultsToString(pghandler, "*", EXGFX, FILENAME, exgfxFilename), JsonObject.class);
//							LOGGER.debug("obj: {}", obj);
//							String botOutput = getExGFXInfo(obj, exgfxFilename);
//							messageEvent.getChannel().sendTextMessage(botOutput);
//						} catch(NumberFormatException nfe) {
//							LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
//							messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
//						}
//						break;
//					case "!getallexgfx":
//						PINGS = 0;
//						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
//						//!getallexgfx
//						if(messageArgs.length != 1) {
//							messageEvent.getChannel().sendTextMessage(errorMessage);
//							break;
//						}
//						JsonArray jsonArray = GSON.fromJson(dbManager.getList(pghandler, "*", EXGFX), JsonArray.class);
//						LOGGER.debug("array: {}", jsonArray);
//						getAllExGFX(jsonArray, messageEvent);
//						break;
//					// Basic commads
//					case "!commands":
//						getCommands(messageEvent);
//						break;
//					default:
//						if(messageArgs[0].startsWith("!")) {
//							errorMessage = MessageArgumentError.UNKNOWN_COMMAND.getErrorMsg();
//							messageEvent.getChannel().sendTextMessage(errorMessage);
//						}
//						break;
//				}
//			} catch(ArrayIndexOutOfBoundsException aiobe) {
//				messageEvent.getChannel().sendTextMessage(errorMessage);
//			} catch (SQLException e) {
//				messageEvent.getChannel().sendTextMessage(String.format("SQL error: %1$s", e.getMessage()));
//			}
		});
	}

	/**
	 * Performs insert of exgfx to database
	 * @param messageEvent - MessageCreateListener, gets the message's channels
	 * @param matcher - Matcher that contains the group members of the input pattern
	 */
	private void addExgfx(MessageCreateListener messageEvent, Matcher matcher) {
		try {
			JDBCHandler handler = JDBCEnum.INSTANCE.getJDBCHandler();
			SQLManager dbManager = SQLManager.INSTANCE;
			int hexadecimal = Integer.parseInt(matcher.group(1), 16);
			String hexString = Integer.toHexString(hexadecimal).toUpperCase();
			String exgfxFilename = String.format("ExGFX%s", hexString);
			String description = matcher.group(2);
			String type = matcher.group(3);
			// If filename exists, send error message and return
			if(exgfxExists(handler, exgfxFilename)) {
				messageEvent.getChannel().sendTextMessage(String.format("Filename %s already exists in database.", exgfxFilename));
				return;
			}
			// Create columns to insert into the table
			ColumnObject[] columns = new ColumnObject[5];
			columns[0] = new ColumnObject<>(FILENAME, exgfxFilename);
			columns[1] = new ColumnObject<>(DESCRIPTION, description.substring(1, description.length() - 1));
			columns[2] = new ColumnObject<>(TYPE, type.substring(1, type.length() - 1));
			columns[3] = new ColumnObject<>(COMPLETED, Boolean.parseBoolean(matcher.group(4)));
			columns[4] = new ColumnObject<>(IMG_LINK, matcher.group(5));
			dbManager.insertIntoTable(handler, EXGFX, columns);
			messageEvent.getChannel().sendTextMessage("Data successfully added to the database!");
		} catch(NumberFormatException nfe) {
			LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
			messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
		} catch(SQLException e) {
			LOGGER.error("SQL error occured when trying to add ExGFX: {},\n{}", e.getMessage(), e.getCause());
			messageEvent.getChannel().sendTextMessage("Unable to add ExGFX file: " + e.getMessage());
		}
	}

	/**
	 * Finds the userid of the given username
	 *
	 * @param handler;
	 * @param fileName;
	 * @return userid
	 * @throws SQLException;
	 */
	private boolean exgfxExists(JDBCHandler handler, String fileName) throws SQLException {
		SQLManager dbManager = SQLManager.INSTANCE;
		// Don't have to get last result as this service needs unique filenames
		String result = dbManager.resultsToString(handler, "*", EXGFX, "filename", fileName);
		JsonObject obj = GSON.fromJson(result, JsonObject.class);
		return obj.has("filename") && obj.get("filename").getAsString().equals(fileName);
	}

	/**
	 * Turns a json string from db output into a readable string for the bot to output
	 * @param messageEvent - MessageCreateListener, gets the message's channels
	 * @param matcher - Matcher that contains the group members of the input pattern
	 */
	private void getExGFXInfo(MessageCreateListener messageEvent, Matcher matcher, JDBCHandler handler) {
		try {
			SQLManager dbManager = SQLManager.INSTANCE;
			String file = matcher.group(1);
			int hexadecimal = Integer.parseInt(file, 16);
			String hexString = Integer.toHexString(hexadecimal).toUpperCase();
			String exgfxFilename = String.format("ExGFX%s", hexString);
			LOGGER.debug("exgfx: {}", exgfxFilename);
			String json = dbManager.resultsToString(handler, "*", EXGFX, FILENAME, exgfxFilename);
			LOGGER.debug("json: {}", json);
			JsonObject obj = GSON.fromJson(json, JsonObject.class);
			LOGGER.debug("obj: {}", obj);
			String botOutput;
			if(obj == null || obj.isJsonNull() || obj.toString().equals("{}")) {
				botOutput = String.format("File %s does not exist :(", file);
			} else {
				botOutput = String.format("File: %1$s\nDescription: %2$s\nType: %3$s\nCompleted: %4$s\nImage Link: %5$s",
						obj.get(FILENAME), obj.get(DESCRIPTION), obj.get(TYPE), obj.get(COMPLETED), obj.get(IMG_LINK));
			}
			messageEvent.getChannel().sendTextMessage(botOutput);
		} catch(NumberFormatException nfe) {
			LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
			messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
		} catch(SQLException e) {
			LOGGER.error("SQL error occured when trying to find ExGFX: {},\n{}", e.getMessage(), e.getCause());
			messageEvent.getChannel().sendTextMessage("Unable to find ExGFX file: " + e.getMessage());
		}
	}

	/**
	 * Turns a json string from db output into a readable string for the bot to output
	 */
	private void getAllExGFX(MessageCreateListener messageEvent) {
		SQLManager dbManager = SQLManager.INSTANCE;
		JDBCHandler handler = JDBCEnum.INSTANCE.getJDBCHandler();
		try {
			JsonArray jsonArray = GSON.fromJson(dbManager.getList(handler, "*", EXGFX), JsonArray.class);
			if (jsonArray == null || jsonArray.isJsonNull() || dbManager.isListEmpty(jsonArray.toString())) {
				messageEvent.getChannel().sendTextMessage("Files do not exist :(");
			} else {
				for (int i = 0; i < jsonArray.size(); i++) {
					JsonObject obj = jsonArray.get(i).getAsJsonObject();
					messageEvent.getChannel().sendTextMessage(String.format("File: %1$s\nDescription: %2$s\nType: %3$s\nCompleted: %4$s\nImage Link: %5$s",
							obj.get(FILENAME), obj.get(DESCRIPTION), obj.get(TYPE), obj.get(COMPLETED), obj.get(IMG_LINK)));
				}
			}
		} catch(SQLException e) {
			LOGGER.error("SQL error occured when trying to find all ExGFX files: {},\n{}", e.getMessage(), e.getCause());
			messageEvent.getChannel().sendTextMessage("Unable to find ExGFX files: " + e.getMessage());
		}
	}

	/**
	 * Sends unkown command message to the channel
	 * @param messageEvent;
	 */
	private void getCommands(MessageCreateListener messageEvent) {
		messageEvent.getChannel().sendTextMessage("**General Bot Commands**\n------------------------\n" +
				"!ping\n\t- A generic ping message. Please don't overuse.\n" +
				"!hewwo\n\t- What's this?\n" +
				"**ExGFX Commands**\n------------------\n" +
				"!addexgfx  <filenumber>  <description>  <type>  <completed>  <imglink>\n" +
				"\t- Use this command to add information on an ExGFX file to the database.\n" +
				"\t- The file number must be in hexadecimal format.\n" +
				"\n!getexgfx  <filenumber>\n" +
				"\t- Use this command to get back the information on an ExGFX file from the database.\n" +
				"\t- The file number must be in hexadecimal format.\n" +
				"\n!getallexgfx\n" +
				"\t- Use this command to get back the information on all ExGFX files from the database.\n" +
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
