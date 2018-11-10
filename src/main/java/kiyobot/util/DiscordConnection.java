package kiyobot.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class DiscordConnection {

	private String wss;
	private Gson gson;

	private static final int GATEWAY_VERSION = 6;
	private static final String ENCODING = "json";
	private static final double VERSION = 0.1;
	private static final String GET_URL = "https://www.discordapp.com/api/gateway";
	private static final Logger LOGGER = LogManager.getLogger();

	public DiscordConnection() {
		this.wss = "";
		this.gson = new Gson();
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
		String websocket = String.format("%1$s/v=%2$s&encoding=%3$s", this.wss, GATEWAY_VERSION, ENCODING);
        try {
            URL url = new URL(websocket);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

//            WebSocketClient client = new WebSocketClient();

            connection.setDoInput(true);
            connection.connect();
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("User-Agent", String.format("kiyobot (v%s)", VERSION));
//            connection.setRequestProperty("Content-Type", "application/json");
			LOGGER.info("Status Code: {} {}", connection.getResponseCode(), connection.getResponseMessage());
            InputStream instream = connection.getInputStream();

            LOGGER.info("Status Code: {} {}", connection.getResponseCode(), connection.getResponseMessage());
        } catch (MalformedURLException mue) {
            LOGGER.fatal("URL is malformed, {},\n{}", mue.getMessage(), mue.getStackTrace());
        } catch (IOException ioe) {
            LOGGER.fatal("Error has occured when attempting connection, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
        }
	}
}
