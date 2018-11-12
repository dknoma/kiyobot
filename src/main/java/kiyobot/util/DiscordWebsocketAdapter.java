package kiyobot.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import kiyobot.logger.WebsocketLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An extension of a Websocket adapter for Discord API connections.
 *
 * @author dk
 */
public class DiscordWebsocketAdapter extends WebSocketAdapter {

	private String wss;
	private Gson gson;
	private String hbPayload;

	private final AtomicReference<WebSocket> websocket = new AtomicReference<>();

	private static final int GATEWAY_VERSION = 6;
	private static final String ENCODING = "json";
	private static final double VERSION = 0.1;
	private static final String GET_URL = "https://www.discordapp.com/api/gateway";
	private static final Logger LOGGER = LogManager.getLogger();

	public DiscordWebsocketAdapter() {
		this.wss = "";
		this.gson = new Gson();
		this.hbPayload = "";
	}

	public void getWss() {
		try {
			URL url = new URL(GET_URL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", String.format("kiyobot (v%s)", VERSION));
			connection.setRequestProperty("Content-Type", "application/json");

			InputStream instream = connection.getInputStream();

			LOGGER.info("Status Code: {} {}", connection.getResponseCode(), connection.getResponseMessage());
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = reader.readLine();
			this.wss = parseJson(line, "url");
			LOGGER.debug(this.wss);
		} catch (MalformedURLException mue) {
			LOGGER.fatal("URL is malformed, {},\n{}", mue.getMessage(), mue.getStackTrace());
		} catch (IOException ioe) {
			LOGGER.fatal("Error has occured when attempting connection, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
		}
	}

	/**
	 * Tries to retrieve from the input json string, the value of a specific key.
	 * @param json - json string
	 * @param key - key to get value from
	 * @return value
	 */
	private String parseJson(String json, String key) {
		String value = "";
		JsonObject obj = gson.fromJson(json, JsonObject.class);
		if(obj.has(key)) {
			value = obj.get(key).getAsString();
		}
		return value;
	}

    /**
     * Connect to the cached websocket
     */
	public void connect() {
		String wssUri = String.format("%1$s/?v=%2$s&encoding=%3$s", this.wss, GATEWAY_VERSION, ENCODING);
        try {
        	// Create a WebSocketFactory instance.
			WebSocketFactory factory = new WebSocketFactory();
			try {
				factory.setSSLContext(SSLContext.getDefault());
			} catch (NoSuchAlgorithmException e) {
				LOGGER.warn("An error occurred while setting ssl context", e);
			}
			URI uri = new URI(wssUri);
			LOGGER.debug("URI: {}", uri);
			WebSocket ws = factory.createSocket(uri);
			this.websocket.set(ws);
			// Register a listener to receive WebSocket events.
			ws.addListener(this);
			ws.addListener(new WebsocketLogger());

			ws.connect();
        } catch (URISyntaxException use) {
			LOGGER.fatal("Error has occured in URI creation, {},\n{}", use.getMessage(), use.getStackTrace());
		} catch (IOException ioe) {
            LOGGER.fatal("Error has occured when attempting connection, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
        } catch (Exception e) {
			LOGGER.fatal("Error has occured starting WebSocketClient, {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}

	@Override
	public void onTextMessage(WebSocket websocket, String message) throws Exception {
		LOGGER.info("MESSAGE: {}", message);

		JsonObject obj = gson.fromJson(message, JsonObject.class);

		String op = obj.get("op").getAsString();

		switch(op) {
			case "10":
				JsonElement s = obj.get("s");
				LOGGER.debug("s is null: {}", s.isJsonNull());
				String seq = (!s.isJsonNull()) ? s.getAsString() : "null";

				String heartbeatInterval = obj.get("d").getAsJsonObject().get("heartbeat_interval").getAsString();
				LOGGER.info("heartbeat_interval: {}", heartbeatInterval);

				String heartbeat = String.format("{\"op\": 1, \"d\": %s}", seq);
				LOGGER.info("heartbeat: {}", heartbeat);

				websocket.sendText(heartbeat);
				break;
			case "11":
				LOGGER.info("Received 11");
//							websocket.disconnect();
				break;
		}
	}
}
