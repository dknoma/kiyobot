package sql.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.model.SQLModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SQLModelBuilder {

	private String[] modelFiles;
	private Gson gson;
	private Map<String, SQLModel> models;

	private static final String STRING = "STRING";
	private static final String INTEGER = "INTEGER";
	private static final String BOOLEAN = "BOOLEAN";
	private static final Logger LOGGER = LogManager.getLogger();

	public SQLModelBuilder() {
		this.gson = new Gson();
		this.models = new HashMap<>();
	}

	/**
	 * Finds all model files in the given directory and creates an array of their file names
	 * @param modelDirectory
	 */
	public void findModelFiles(String modelDirectory) {
		//remove " from input, get input files
		Path input = Paths.get(modelDirectory);
		File dir = input.toFile();
		this.modelFiles = dir.list();
		if(this.modelFiles != null) {
			for (int i = 0; i < this.modelFiles.length; i++) {
				this.modelFiles[i] = modelDirectory + "/" + this.modelFiles[i];
			}
		}
	}

	/**
	 * Read in all the model files
	 */
	public void readFiles() {
		if(this.modelFiles == null || this.modelFiles.length <= 0) {
			LOGGER.error("No model files were found.");
			return;
		}
		for(String fileName : this.modelFiles) {
			try(BufferedReader br = Files.newBufferedReader(Paths.get(fileName), java.nio.charset.StandardCharsets.ISO_8859_1)) {

				parseJson(br.readLine());

			} catch (IOException ioe) {
				LOGGER.error("Error occurred when reading the file, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
			}
		}
	}

	/**
	 * Helper method to parse a string in json format
	 * @param json
	 */
	private void parseJson(String json) {
		JsonObject obj = gson.fromJson(json, JsonObject.class);
		if(!obj.has("name")) {
			LOGGER.error("No model name was found.");
			return;
		}
		SQLModel model = initializeModel(obj);
	}

	/**
	 * Initializes a model
	 * @param obj json
	 * @return SQLModel
	 */
	private SQLModel initializeModel(JsonObject obj) {
		String name = obj.get("name").getAsString();
		boolean autoIncrement = obj.has("autoIncrement") && obj.get("autoIncrement").getAsBoolean();
		if(obj.has("reference")) {
			String referenceTable = obj.get("reference").getAsString();
			return new SQLModel(name, autoIncrement, referenceTable);
		} else {
			return new SQLModel(name, autoIncrement);
		}
	}

	public String[] getModelFiles() {
		return this.modelFiles;
	}
}
