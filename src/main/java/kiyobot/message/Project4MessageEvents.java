//package kiyobot.message;
//
//import com.google.gson.*;
//import diskiyord.api.DiskiyordApi;
//import diskiyord.event.error.MessageArgumentError;
//import diskiyord.event.message.MessageCreateListener;
//import jql.sql.jdbc.ColumnObject;
//import jql.sql.jdbc.JDBCHandler;
//import jql.sql.jdbc.SQLManager;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.sql.SQLException;
//
//public enum Project4MessageEvents {
//
//	INSTANCE();
//
//	Project4MessageEvents(){}
//
//	private final Gson GSON = new Gson();
//	private final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
//	private final int SC_OK = 200;
//	private final int SC_BAD_REQUEST = 400;
//	// URL of the tunnel connection to the Eventer service
//	private final String PROJECT4_PATH = "http://mcvm064.cs.usfca.edu:7070/api";
////	private final String PROJECT4_PATH = "http://127.0.0.1:9000/api";
//	private final Logger LOGGER = LogManager.getLogger();
//
//	/**
//	 * Adds message listener to the api, which allows the bot to listen to Discord messages
//	 * @param api - Diskiyord API class
//	 * @param pghandler - JDBCHandler to handle all SQL queries
//	 */
//	public void listenOnMessage(DiskiyordApi api, JDBCHandler pghandler) {
//		SQLManager dbManager = SQLManager.INSTANCE;
//		// Message listener
//		api.addMessageCreateListener(messageEvent -> {
//			String message = messageEvent.getMessageContent();
//			String[] messageArgs = message.split(" {2}");
//			String errorMessage = "An error has occurred.";
//			try {
//				switch(messageArgs[0]) {
//					// Project4 comands
//					case "!createevent":
//						// Gets user info, including the info of all the events the user has tickets to
//						String userid = messageArgs[1];
//						String eventname = messageArgs[2];
//						String maxTickets = messageArgs[3];
//						if(userid.isEmpty() || eventname.isEmpty() || maxTickets.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						createEvent(messageEvent, userid, eventname, maxTickets);
//						break;
//					case "!getevent":
//						// get info on a specific event
//						String eventid = messageArgs[1];
//						if(eventid.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						getEvent(messageEvent, eventid);
//						break;
//					case "!getevents":
//						// list of all events
//						getEvents(messageEvent);
//						break;
//					case "!createuser":
//						// Gets user info, including the info of all the events the user has tickets to
//						String username = messageArgs[1];
//						if(username.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						createUser(messageEvent, username);
//						break;
//					case "!getuser":
//						// Gets user info, including the info of all the events the user has tickets to
//						userid = messageArgs[1];
//						if(userid.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						getUser(messageEvent, userid);
//						break;
//					case "!purchasetickets":
//						// Gets user info, including the info of all the events the user has tickets to
//						eventid = messageArgs[1];
//						userid = messageArgs[2];
//						String numTickets = messageArgs[3];
//						if(userid.isEmpty() || eventid.isEmpty() || numTickets.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						purchaseTickets(messageEvent, eventid, userid, numTickets);
//						break;
//					case "!transfertickets":
//						// Gets user info, including the info of all the events the user has tickets to
//						eventid = messageArgs[1];
//						userid = messageArgs[2];
//						String targetuser = messageArgs[3];
//						numTickets = messageArgs[4];
//						if(userid.isEmpty() || eventid.isEmpty() || numTickets.isEmpty() || targetuser.isEmpty()) {
//							messageEvent.getChannel().sendTextMessage("Command is wrong. Make sure spacing is correct.");
//							return;
//						}
//						transferTickets(messageEvent, eventid, userid, targetuser, numTickets);
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
//			}
//		});
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void createEvent(MessageCreateListener messageEvent, String userId, String eventname, String numTickets) {
//		String userIdString;
//		String numTicketsString;
//		Object userid = userId;
//		Object numtickets = numTickets;
//		// Check if parameters are Strings or ints, and format the json body accordingly
//		try{
//			userid = Integer.parseInt(userId);
//			userIdString = "%1$d";
//		} catch(NumberFormatException nfe) {
//			userIdString = "\"%1$s\"";
//		}
//		try{
//			numtickets = Integer.parseInt(numTickets);
//			numTicketsString = "%3$d";
//		} catch(NumberFormatException nfe) {
//			numTicketsString = "\"%3$s\"";
//		}
//		postToService(messageEvent, PROJECT4_PATH + "/events/create",
//				"{\"userid\":" + userIdString +",\"eventname\":\"%2$s\",\"numtickets\":" + numTicketsString + "}",
//				userid, eventname, numtickets);
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void getEvent(MessageCreateListener messageEvent, String eventId) {
//		getToService(messageEvent, PROJECT4_PATH + "/events/" + eventId);
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void getEvents(MessageCreateListener messageEvent) {
//		getToService(messageEvent, PROJECT4_PATH + "/events");
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void createUser(MessageCreateListener messageEvent, String username) {
//		postToService(messageEvent, PROJECT4_PATH + "/users/create",
//				"{\"username\":\"%s\"}", username);
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void getUser(MessageCreateListener messageEvent, String userId) {
//		getToService(messageEvent, PROJECT4_PATH + "/users/" + userId);
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void purchaseTickets(MessageCreateListener messageEvent, String eventId, String userId, String tickets) {
//		String numTicketsString;
//		Object numtickets = tickets;
//		// Check if parameters are Strings or ints, and format the json body accordingly
//		try{
//			numtickets = Integer.parseInt(tickets);
//			numTicketsString = "%1$d";
//		} catch(NumberFormatException nfe) {
//			numTicketsString = "\"%1$s\"";
//		}
//		postToService(messageEvent, String.format("%1$s/events/%2$s/purchase/%3$s", PROJECT4_PATH, eventId, userId),
//				"{\"tickets\":" + numTicketsString +"}", numtickets);
//	}
//
//	/**
//	 * Connects to the website and performs the appropriate methods
//	 * @param messageEvent;
//	 */
//	private void transferTickets(MessageCreateListener messageEvent, String eventId, String userId,
//										String targetUser, String tickets) {
//		String eventIdString;
//		String targetUserString;
//		String numTicketsString;
//		Object eventid = eventId;
//		Object targetuser = targetUser;
//		Object numtickets = tickets;
//		// Check if parameters are Strings or ints, and format the json body accordingly
//		try{
//			eventid = Integer.parseInt(eventId);
//			eventIdString = "%1$d";
//		} catch(NumberFormatException nfe) {
//			eventIdString = "\"%1$s\"";
//		}
//		try{
//			numtickets = Integer.parseInt(tickets);
//			numTicketsString = "%2$d";
//		} catch(NumberFormatException nfe) {
//			numTicketsString = "\"%2$s\"";
//		}
//		try{
//			targetuser = Integer.parseInt(targetUser);
//			targetUserString = "%3$d";
//		} catch(NumberFormatException nfe) {
//			targetUserString = "\"%3$s\"";
//		}
//		postToService(messageEvent, String.format("%1$s/users/%2$s/tickets/transfer", PROJECT4_PATH, userId),
//				"{\"eventid\":" + eventIdString + ",\"tickets\":" + numTicketsString + ",\"targetuser\":" + targetUserString + "}",
//				eventid, numtickets, targetuser);
//	}
//
//	/**
//	 * Performs a GET to the service
//	 * @param messageEvent - bots listener
//	 * @param url - url of service
//	 */
//	private void getToService(MessageCreateListener messageEvent, String url) {
//		try {
//			LOGGER.debug("GET URL: {}", url);
//			URL userService = new URL(url);
//			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
//			connection.setRequestMethod("GET");
//			connection.setDoInput(true);
//			connection.connect();
//			switch(connection.getResponseCode()) {
//				case SC_OK:
//					LOGGER.debug("OK");
//					try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//						// Read the json line from the service.
//						String line = reader.readLine();
//						// Checks the response code from the events service
//						messageEvent.getChannel().sendTextMessage(prettyJson(line));
//					} catch(IOException ioe) {
//						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
//						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//					}
//					break;
//				case SC_BAD_REQUEST:
//					LOGGER.debug("Bad Request");
//					try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
//						// Read the json line from the service.
//						String line = reader.readLine();
//						// Checks the response code from the events service
//						messageEvent.getChannel().sendTextMessage(prettyJson(line));
//
//					} catch(IOException ioe) {
//						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
//						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//					} catch(JsonSyntaxException jse) {
//						messageEvent.getChannel().sendTextMessage(prettyJson("{\"message\":\"400 Bad request\"}"));
//					}
//					break;
//				default:
//					int responseCode = connection.getResponseCode();
//					messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
//					break;
//			}
//		} catch (IOException ioe) {
//			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//		}
//	}
//
//	/**
//	 * Performs a POST to the service
//	 * @param messageEvent - bots listener
//	 * @param url - url of service
//	 * @param jsonBodyFormat - Format of the json body to be used in String.format(...)
//	 * @param params - varargs to insert into the json body
//	 */
//	@SafeVarargs
//	private final <T> void postToService(MessageCreateListener messageEvent, String url,
//										 String jsonBodyFormat, T... params) {
//		try {
//			LOGGER.debug("POST URL: {}", url);
//			URL userService = new URL(url);
//			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
//			connection.setRequestMethod("POST");
//			connection.setDoOutput(true);
//			connection.setDoInput(true);
//			connection.setRequestProperty("Content-Type", "application/json");
//			OutputStream outstream = connection.getOutputStream();
//			outstream.write(String.format(jsonBodyFormat, params).getBytes());
//
//			switch (connection.getResponseCode()) {
//				case SC_OK:
//					LOGGER.debug("OK");
//					try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//						// Read the json line from the service.
//						String line = reader.readLine();
//						// Checks the response code from the events service
//						messageEvent.getChannel().sendTextMessage(prettyJson(line));
//					} catch(IOException ioe){
//						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
//						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//					}
//					break;
//				case SC_BAD_REQUEST:
//					LOGGER.debug("Bad Request");
//					try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
//						// Read the json line from the service.
//						String line = reader.readLine();
//						// Checks the response code from the events service
//						messageEvent.getChannel().sendTextMessage(prettyJson(line));
//					} catch(IOException ioe){
//						LOGGER.error("I/O error has occurred: {},\n{}", ioe.getMessage(), ioe.getStackTrace());
//						messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//					} catch(JsonSyntaxException jse) {
//						messageEvent.getChannel().sendTextMessage(prettyJson("{\"message\":\"400 Bad request\"}"));
//					}
//					break;
//				default:
//					int responseCode = connection.getResponseCode();
//					messageEvent.getChannel().sendTextMessage("Error=" + responseCode);
//					break;
//			}
//			outstream.flush();
//			outstream.close();
//		} catch (IOException ioe) {
//			messageEvent.getChannel().sendTextMessage(String.format("I/O error: %1$s", ioe.getMessage()));
//		}
//	}
//
//	/**
//	 * Prints pretty json
//	 * @param json;
//	 * @return pretty print
//	 */
//	private String prettyJson(String json) {
//		LOGGER.debug("json: {}", json);
//		JsonElement ele = GSON_PRETTY.fromJson(json, JsonElement.class);
//		String out = "";
//		if(ele.isJsonObject()) {
//			out = GSON_PRETTY.toJson(ele.getAsJsonObject());
//			System.out.println(out);
//		} else if(ele.isJsonArray()) {
//			out = GSON_PRETTY.toJson(ele.getAsJsonArray());
//			System.out.println(out);
//		} else if(ele.isJsonPrimitive()) {
//			out = GSON_PRETTY.toJson(ele.getAsJsonPrimitive());
//			System.out.println(out);
//		}
//		return out;
//	}
//
//	/**
//	 * Sends unkown command message to the channel
//	 * @param messageEvent;
//	 */
//	private void getCommands(MessageCreateListener messageEvent) {
//		messageEvent.getChannel().sendTextMessage("**General Bot Commands**\n------------------------\n" +
//				"!ping\n\t- A generic ping message. Please don't overuse.\n" +
//				"!hewwo\n\t- What's this?\n" +
//				"**ExGFX Commands**\n------------------\n" +
//				"!addexgfx  <filenumber>  <description>  <type>  <completed>  <imglink>\n" +
//				"\t- Use this command to add information on an ExGFX file to the database.\n" +
//				"\t- The file number must be in hexadecimal format.\n" +
//				"\n!getexgfx  <filenumber>\n" +
//				"\t- Use this command to get back the information on an ExGFX file from the database.\n" +
//				"\t- The file number must be in hexadecimal format.\n" +
//				"\n!getallexgfx\n" +
//				"\t- Use this command to get back the information on all ExGFX files from the database.\n" +
//				"**Eventer: An Event Ticket Service**\n" +
//				"------------------------------------\n" +
//				"!createevent  <userid>  <eventname>  <max_tickets>\n" +
//				"\t- Creates an event in the event service.\n" +
//				"!getevent  <eventid>\n\t- Gets a specific event from the event service.\n" +
//				"!getevents\n\t- Gets all events from the event service.\n" +
//				"!createuser  <username>\n\t- Creates a user in the user service.\n" +
//				"!getuser  <userid>\n\t- Gets a specific user from the user service.\n" +
//				"!purchasetickets  <eventid>  <userid>  <number_to_purchase>\n" +
//				"\t- Purchase a number of tickets to a specific event for a user from the Eventer service.\n" +
//				"!transfertickets  <eventid>  <userid>  <targetuser>  <number_to_purchase>\n" +
//				"\t- Transfer a number of tickets to a specific event from a user to another user from the Eventer service.\n"
//		);
//	}
//}
