package diskiyord.util.channel;

import diskiyord.api.DiskiyordApi;
import diskiyord.util.JsonPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A Channel object that represents a specfic Discord channel. Allows messages to be sent to the channel.
 * @author dk
 */
public class Channel {

	private String channelId;

	private final String channelURL;
	private final DiskiyordApi api;

	private static final Logger LOGGER = LogManager.getLogger();

	public Channel(String channelId, DiskiyordApi api) {
		this.channelId = channelId;
		this.api = api;
		this.channelURL = String.format("https://discordapp.com/api/channels/%s/messages", channelId);
	}

	/**
	 * Creates a JsonPacket for the message being sent to the channel
	 * @param message;
	 */
	public void sendTextMessage(String message) {
		try {
			JsonPacket requestBodyPacket = new JsonPacket();
			requestBodyPacket.put("content", message);
			requestBodyPacket.put("tts", false);

			URL url = new URL(this.channelURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", String.format("Bot %s", this.api.getToken()));
			connection.setRequestProperty("User-Agent", "Kiyobot");
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream outstream = connection.getOutputStream();
			outstream.write(requestBodyPacket.toString().getBytes());
			outstream.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			LOGGER.trace(String.format("Sent message %s", reader.readLine()));
			reader.close();
			outstream.close();
		} catch (MalformedURLException mue) {
			LOGGER.error("Malformed URL, {},\n{}", mue.getMessage(), mue.getStackTrace());
		} catch (IOException ioe) {
			LOGGER.error("An error occurred when trying to connect to the url, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
		}
	}

	/**
	 * Gets the id of the channel
	 * @return channel id
	 */
	public String getChannelId() {
		return channelId;
	}
}
