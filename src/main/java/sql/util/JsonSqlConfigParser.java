package sql.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonSqlConfigParser {

	private String dbName;
	private String db;
	private String host;
	private String port;
	private String username;
	private String password;
	private Gson gson;
	private static final Logger LOGGER = LogManager.getLogger();

	public JsonSqlConfigParser() {
		this.dbName = "";
		this.db = "";
		this.host = "";
		this.port = "";
		this.username = "";
		this.password = "";
		this.gson = new Gson();
	}

	/**
	 * Parse config file
	 */
	public void parseConfig(String configFile) {
		try(BufferedReader br = Files.newBufferedReader(Paths.get(configFile),
				java.nio.charset.StandardCharsets.ISO_8859_1)) {

			LOGGER.info(configFile);
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
		if(obj.has("dbName") && obj.has("db") && obj.has("host")
				&& obj.has("port") && obj.has("username")
				&& obj.has("password")) {
			this.dbName = obj.get("dbName").getAsString();
			this.db = obj.get("db").getAsString();
			this.host = obj.get("host").getAsString();
			this.port = obj.get("port").getAsString();
			this.username = obj.get("username").getAsString();
			this.password = obj.get("password").getAsString();
		}
	}

	/**
	 * Get sql url
	 * @return sql
	 */
	public String getDbName() {
		return  this.dbName;
	}

	/**
	 * Get sql url
	 * @return sql
	 */
	public String getDb() {
		return  this.db;
	}

	/**
	 * Get sql url
	 * @return sql
	 */
	public String getHost() {
		return  this.host;
	}

	/**
	 * Get sql url
	 * @return sql
	 */
	public String getPort() {
		return  this.port;
	}

	/**
	 * Get username
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get password
	 * @return password
	 */
	public String getPassword() {
		return password;
	}
}