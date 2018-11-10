package kiyobot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigArgParser {

	private String authTok;
	private Gson gson;
	private static final String CONFIG_FILE = "./config/config.json";
	private static final Logger LOGGER = LogManager.getLogger();

	public ConfigArgParser() {
		this.gson = new Gson();
	}

	/**
	 * Parse config file
	 */
	public void parseConfig() {
		try(BufferedReader br = Files.newBufferedReader(Paths.get(CONFIG_FILE),
				java.nio.charset.StandardCharsets.ISO_8859_1)) {

			LOGGER.info(CONFIG_FILE);
			parseJson(br.readLine());

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Helper method to parse a json file
	 * @param line - String to be parsed
	 */
	private void parseJson(String line) {
		JsonObject obj = gson.fromJson(line, JsonObject.class);
		if(obj.has("authTok")) {
			this.authTok = obj.get("authTok").getAsString();
		}
	}

	/**
	 * Get authorization token
	 * @return this.authTok
	 */
	public String getAuthTok() {
		return  this.authTok;
	}
}
