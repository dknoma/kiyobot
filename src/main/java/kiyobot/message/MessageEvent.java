package kiyobot.message;

import com.google.gson.*;
import diskiyord.api.DiskiyordApi;
import diskiyord.event.error.MessageArgumentError;
import diskiyord.event.message.MessageCreateListener;
import jql.sql.jdbc.ColumnObject;
import jql.sql.jdbc.JDBCHandler;
import jql.sql.jdbc.SQLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.regex.Matcher;

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
	private final Logger LOGGER = LogManager.getLogger();

	/**
	 * Adds message listener to the api, which allows the bot to listen to Discord messages
	 * @param api - Diskiyord API class
	 * @param pghandler - JDBCHandler to handle all SQL queries
	 */
	public void listenOnMessage(DiskiyordApi api, JDBCHandler pghandler) {
		SQLManager dbManager = SQLManager.INSTANCE;
		// Message listener
		api.addMessageCreateListener(messageEvent -> {
			String message = messageEvent.getMessageContent();
			//TODO: regex for commands?
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
						try {
							int hexadecimal = Integer.parseInt(messageArgs[1], 16);
							String hexString = Integer.toHexString(hexadecimal).toUpperCase();
							String exgfxFilename = String.format("ExGFX%s", hexString);
							LOGGER.debug("exgfx: {}", exgfxFilename);
							JsonObject obj = GSON.fromJson(dbManager.resultsToString(pghandler, "*", EXGFX, FILENAME, exgfxFilename), JsonObject.class);
							LOGGER.debug("obj: {}", obj);
							String botOutput = getExGFXInfo(obj, exgfxFilename);
							messageEvent.getChannel().sendTextMessage(botOutput);
						} catch(NumberFormatException nfe) {
							LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
							messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
						}
						break;
					case "!getallexgfx":
						PINGS = 0;
						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
						//!getallexgfx
						if(messageArgs.length != 1) {
							messageEvent.getChannel().sendTextMessage(errorMessage);
							break;
						}
						JsonArray jsonArray = GSON.fromJson(dbManager.getList(pghandler, "*", EXGFX), JsonArray.class);
						LOGGER.debug("array: {}", jsonArray);
						getAllExGFX(jsonArray, messageEvent);
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
	 * @param messageEvent - MessageCreateListener, gets the message's channelxs
	 * @param pghandler - handler for SQL queries
	 * @param messageArgs - the parts of a command message
	 * @param errorMessage - error message
	 * @throws SQLException;
	 */
	private void addExgfx(MessageCreateListener messageEvent, JDBCHandler pghandler, String[] messageArgs, String errorMessage) throws SQLException{
		if(messageArgs.length != 6) {
			messageEvent.getChannel().sendTextMessage(errorMessage);
			return;
		}
		try {
			int hexadecimal = Integer.parseInt(messageArgs[1], 16);
			String hexString = Integer.toHexString(hexadecimal).toUpperCase();
			String exgfxFilename = String.format("ExGFX%s", hexString);
			// Can make Class to handle all exgfx related methods
			ColumnObject[] columns = new ColumnObject[5];
			columns[0] = new ColumnObject<>(FILENAME, exgfxFilename);
			columns[1] = new ColumnObject<>(DESCRIPTION, messageArgs[2]);
			columns[2] = new ColumnObject<>(TYPE, messageArgs[3]);
			columns[3] = new ColumnObject<>(COMPLETED, Boolean.parseBoolean(messageArgs[4]));
			columns[4] = new ColumnObject<>(IMG_LINK, messageArgs[5]);
			pghandler.executeUpdate(pghandler.insert(EXGFX, columns));
			messageEvent.getChannel().sendTextMessage("Data successfully added to the database!");
		} catch(NumberFormatException nfe) {
			LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
			messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
		}
	}

	/**
	 * Performs insert of exgfx to database
	 * @param messageEvent - MessageCreateListener, gets the message's channels
	 * @param pghandler - handler for SQL queries
	 * @param matcher - Matcher that contains the group members of the input pattern
	 * @throws SQLException;
	 */
	private void addExgfx(MessageCreateListener messageEvent, JDBCHandler pghandler, Matcher matcher) throws SQLException {
		// String regex = "!addexgfx (\\p{XDigit}+?) (\"(\\w*?|\\s*?)*\") ((\\w*?|\\s*?)+) (\\w+?) ((\\w*?|\\W*?)+)";
//		try {
//			int hexadecimal = Integer.parseInt(messageArgs[1], 16);
//			String hexString = Integer.toHexString(hexadecimal).toUpperCase();
//			String exgfxFilename = String.format("ExGFX%s", hexString);
//			// Can make Class to handle all exgfx related methods
//			ColumnObject[] columns = new ColumnObject[5];
//			columns[0] = new ColumnObject<>(FILENAME, exgfxFilename);
//			columns[1] = new ColumnObject<>(DESCRIPTION, messageArgs[2]);
//			columns[2] = new ColumnObject<>(TYPE, messageArgs[3]);
//			columns[3] = new ColumnObject<>(COMPLETED, Boolean.parseBoolean(messageArgs[4]));
//			columns[4] = new ColumnObject<>(IMG_LINK, messageArgs[5]);
//			pghandler.executeUpdate(pghandler.insert(EXGFX, columns));
//			messageEvent.getChannel().sendTextMessage("Data successfully added to the database!");
//		} catch(NumberFormatException nfe) {
//			LOGGER.warn("File number was not in hexadecimal. {},\n{}", nfe.getMessage(), nfe.getCause());
//			messageEvent.getChannel().sendTextMessage("File number was not in hexadecimal.");
//		}
	}

	/**
	 * Turns a json string from db output into a readable string for the bot to output
	 * @param obj;
	 * @return bot message
	 */
	private String getExGFXInfo(JsonObject obj, String fileNumber) {
		if(obj == null || obj.isJsonNull() || obj.toString().equals("{}")) {
			return String.format("File %s does not exist :(", fileNumber);
		} else {
			return String.format("File: %1$s\nDescription: %2$s\nType: %3$s\nCompleted: %4$s\nImage Link: %5$s",
					obj.get(FILENAME), obj.get(DESCRIPTION), obj.get(TYPE), obj.get(COMPLETED), obj.get(IMG_LINK));
		}
	}

	/**
	 * Turns a json string from db output into a readable string for the bot to output
	 * @param array;
	 */
	private void getAllExGFX(JsonArray array, MessageCreateListener messageEvent) {
		SQLManager dbManager = SQLManager.INSTANCE;
		if(array == null || array.isJsonNull() || dbManager.isListEmpty(array.toString())) {
			messageEvent.getChannel().sendTextMessage("Files do not exist :(");
		} else {
			for(int i = 0; i < array.size(); i++) {
				JsonObject obj = array.get(i).getAsJsonObject();
				messageEvent.getChannel().sendTextMessage(String.format("File: %1$s\nDescription: %2$s\nType: %3$s\nCompleted: %4$s\nImage Link: %5$s",
						obj.get(FILENAME), obj.get(DESCRIPTION), obj.get(TYPE), obj.get(COMPLETED), obj.get(IMG_LINK)));
			}
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
