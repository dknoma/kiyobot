package diskiyord.util.channel;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import diskiyord.api.DiskiyordApi;
import diskiyord.util.JsonPacket;
import diskiyord.util.gateway.GatewayEvent;
import diskiyord.util.gateway.GatewayOpcode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

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
			LOGGER.debug(String.format("Sent message %s", reader.readLine()));
			reader.close();
			outstream.close();
		} catch (MalformedURLException mue) {
			LOGGER.error("Malformed URL, {},\n{}", mue.getMessage(), mue.getStackTrace());
		} catch (IOException ioe) {
			LOGGER.error("An error occurred when trying to connect to the url, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
		}
	}
}