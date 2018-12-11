package jql.sql.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonSqlConfigParser {

	private String configFile;
	private String modelDirectory;
	private String dbName;
	private String db;
	private String host;
	private String port;
	private String username;
	private String password;
	private boolean isPostgres;
	private boolean configIsCorrect;
	private Gson gson;
	private static final Logger LOGGER = LogManager.getLogger();

	public JsonSqlConfigParser() {
		this.configFile = "";
		this.modelDirectory = "";
		this.dbName = "";
		this.db = "";
		this.host = "";
		this.port = "";
		this.username = "";
		this.password = "";
		this.configIsCorrect = false;
		this.gson = new Gson();
	}

	/**
	 * Parse config file
	 */
	public void parseConfig(String[] args) {
		boolean correctArgs = false;
		boolean configExists = false;
		for(int i = 0; i < args.length; i++) {
			// Checks if the command line arguments are correct
			if(args[i].equals("-config") && args[i+1] != null) {
				this.configFile = args[i+1];
				correctArgs = true;
			}
		}
		// Checks if the config file exists
		if(Files.exists(Paths.get(this.configFile))) {
			configExists = true;
		}
		if(correctArgs && configExists) {
			try (BufferedReader br = Files.newBufferedReader(Paths.get(this.configFile),
					java.nio.charset.StandardCharsets.ISO_8859_1)) {

				LOGGER.info(configFile);
				parseJson(br.readLine());

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			LOGGER.fatal("Command line arguments were wrong or config file does not exist.");
		}
	}

	/**
	 * Helper method to parse a json file
	 * @param line - String to be parsed
	 */
	private void parseJson(String line) {
		JsonObject obj = gson.fromJson(line, JsonObject.class);
		if(obj.has("dbName") && obj.has("db")
				&& obj.has("host") && obj.has("port") && obj.has("username")
				&& obj.has("password")) {
			this.dbName = obj.get("dbName").getAsString();
			this.db = obj.get("db").getAsString();
			this.host = obj.get("host").getAsString();
			this.port = obj.get("port").getAsString();
			this.username = obj.get("username").getAsString();
			this.password = obj.get("password").getAsString();
			this.configIsCorrect = true;
		} else {
			this.configIsCorrect = false;
		}
		this.modelDirectory = obj.has("modelDirectory") ? obj.get("modelDirectory").getAsString() : "./models";
		this.isPostgres = obj.has("isPostgres") && obj.get("isPostgres").getAsBoolean();
	}

	/**
	 * Get directory of models
	 * @return sql
	 */
	public String getModelDirectory() {
		return  this.modelDirectory;
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

	/**
	 * Returns if the db is postgres or not
	 * @return ;
	 */
	public boolean isPostgres() {
		return isPostgres;
	}

	/**
	 * Returns if the config is setup correctly
	 * @return ;
	 */
	public boolean configIsCorrect() {
		return configIsCorrect;
	}
}
