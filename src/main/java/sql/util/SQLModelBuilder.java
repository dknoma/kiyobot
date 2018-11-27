package sql.util;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.jdbc.JDBCEnum;
import sql.jdbc.PostgresHandler;
import sql.model.PostgreSQLModel;
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
	private boolean modelsCorrectlyFormatted;

	private static final String STRING = "STRING";
	private static final String INTEGER = "INTEGER";
	private static final String BOOLEAN = "BOOLEAN";
	private static final Logger LOGGER = LogManager.getLogger();

	public SQLModelBuilder() {
		this.gson = new Gson();
		this.models = new HashMap<>();
		this.modelsCorrectlyFormatted = false;
	}

	/**
	 * Finds all model files in the given directory and creates an array of their file names
	 * @param modelDirectory;
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
			if(fileName.endsWith(".json")) {
				try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName),
						java.nio.charset.StandardCharsets.ISO_8859_1)) {

					parseJson(br.readLine());

				} catch (IOException ioe) {
					LOGGER.error("Error occurred when reading the file, {},\n{}",
							ioe.getMessage(), ioe.getStackTrace());
					this.modelsCorrectlyFormatted = false;
					return;
				} catch (JsonSyntaxException jse) {
					LOGGER.error("MalformedJsonException; Make sure the models are formatted correctly: {},\n{}",
							jse.getMessage(), jse.getStackTrace());
					this.modelsCorrectlyFormatted = false;
					return;
				}
			} else {
				LOGGER.warn("A non-json file was found. All model files must be in .json format.");
			}
		}
		this.modelsCorrectlyFormatted = true;
	}

	/**
	 * Helper method to parse a string in json format
	 * @param json
	 */
	private void parseJson(String json) throws JsonSyntaxException {
		JsonObject obj = gson.fromJson(json, JsonObject.class);
		if(!obj.has("name")) {
			LOGGER.error("No model name was found.");
			return;
		}
		String name = obj.get("name").getAsString();
		SQLModel model = initializeModel(obj);
		addColumns(obj, model);
		model.createTableQuery();
		SQLModel copy = model.deepCopy();
		this.models.put(name, copy);
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
			return new PostgreSQLModel(name, autoIncrement, referenceTable);
		} else {
			return new PostgreSQLModel(name, autoIncrement);
		}
	}

	/**
	 * Finds the columns from the .json file and puts them into the SQLModel
	 * @param obj;
	 * @param model;
	 */
	private void addColumns(JsonObject obj, SQLModel model) {
		JsonArray columns = obj.get("columns").getAsJsonArray();
		for(JsonElement column : columns) {
			JsonObject key = column.getAsJsonObject();
			String keyName = key.get("key").getAsString();
			// attributes of the key
			JsonObject attributes = key.get("attributes").getAsJsonObject();
			boolean allowNull = attributes.has("allowNull") && attributes.get("allowNull").getAsBoolean();
			switch(attributes.get("type").getAsString()) {
				case STRING:
					boolean keyLengthIsVar = attributes.has("lengthIsVar")
							&& attributes.get("lengthIsVar").getAsBoolean();
					int length = attributes.get("length").getAsInt();
					if(attributes.has("defaultValue")) {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, keyLengthIsVar, length, true, attributes.get("defaultValue"));
					} else {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, keyLengthIsVar, length, false, "");
					}
					break;
				case INTEGER:
					if(attributes.has("defaultValue")) {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, Integer.class, true, attributes.get("defaultValue"));
					} else {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, Integer.class, false, "");
					}
					break;
				case BOOLEAN:
					if(attributes.has("defaultValue")) {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, Boolean.class,
								true, attributes.get("defaultValue"));
					} else {
						model.addColumn(keyName,
								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
								allowNull, Boolean.class, false, "");
					}
					break;
				default:
					LOGGER.error("There was an error in the format of the json file. Please try again.");
					return;
			}
//			if(attributes.get("type").getAsString().equals(STRING)) {
////				boolean keyLengthIsVar = attributes.has("lengthIsVar")
////						&& attributes.get("lengthIsVar").getAsBoolean();
////				int length = attributes.get("length").getAsInt();
////				if(attributes.has("defaultValue")) {
////					model.addColumn(keyName,
////							attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
////							allowNull, keyLengthIsVar, length, true, attributes.get("defaultValue"));
////				} else {
////					model.addColumn(keyName,
////							attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
////							allowNull, keyLengthIsVar, length, false, "");
////				}
//			} else {
//				if(attributes.get("type").getAsString().equals(INTEGER)) {
//					// attributes.has("isUnique") ? attributes.get("isUnique") : false
//					if(attributes.has("defaultValue")) {
//						model.addColumn(keyName,
//							attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
//							allowNull, Integer.class, true, attributes.get("defaultValue"));
//					} else {
//						model.addColumn(keyName,
//								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
//								allowNull, Integer.class, false, "");
//					}
//				} else if(attributes.get("type").getAsString().equals(BOOLEAN)){
//					if(attributes.has("defaultValue")) {
//						model.addColumn(keyName,
//								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
//								allowNull, Boolean.class,
//								true, attributes.get("defaultValue"));
//					} else {
//						model.addColumn(keyName,
//								attributes.has("isUnique") && attributes.get("isUnique").getAsBoolean(),
//								allowNull, Boolean.class, false, "");
//					}
//				} else {
//					LOGGER.error("There was an error in the format of the json file. Please try again.");
//					return;
//				}
//			}
		}
	}

	public String[] getModelFiles() {
		return this.modelFiles;
	}

	public Map<String, SQLModel> getCopyOfModels() {
		return this.models;
	}

	public boolean areModelsFormattedCorrectly() {
		return this.modelsCorrectlyFormatted;
	}
}
