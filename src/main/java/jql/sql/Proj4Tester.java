package jql.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Proj4Tester {

	private static final Gson gson = new Gson();
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		String out = "{\n\t\"username\": \"string\"\n}";
		JsonElement ele = gson.fromJson(out, JsonElement.class);
		String cleanedJson = "";
		if(ele.isJsonObject()) {
			cleanedJson = gson.toJson(ele.getAsJsonObject());
		} else if(ele.isJsonArray()) {
			cleanedJson = gson.toJson(ele.getAsJsonArray());
		} else if(ele.isJsonPrimitive()) {
			cleanedJson = gson.toJson(ele.getAsJsonPrimitive());
		}
		System.out.println(cleanedJson);

//		String purchaseRegex = "(?is)(/events)(/\\d*?)(/purchase)(/\\d*?)";
//
//		String test = "/events/2/purchase/44";
//		String[] split = test.split(purchaseRegex);
//		for(String s : split) {
//			System.out.println(s);
//		}


		// Use 127.0.0.1 if connecting through SSH tunnel, else jql.sql.cs.usfca.edu
		// mysql config
//		JsonSqlConfigParser mysqlParser = new JsonSqlConfigParser();
//		mysqlParser.parseConfig(MYSQL_CONFIG_FILE);
//		String mysqlModelDirectory = mysqlParser.getModelDirectory();
//		// postgres config
//		JsonSqlConfigParser postgresParser = new JsonSqlConfigParser();
//		postgresParser.parseConfig(POSTGRES_CONFIG_FILE);
//		String postgresModelDirectory = postgresParser.getModelDirectory();
//
//		// MySQL builder
//		SQLModelBuilder mysqlBuilder = new SQLModelBuilder();
//		mysqlBuilder.findModelFiles(mysqlModelDirectory);
//		mysqlBuilder.readFiles();
//		// Postgres builder
//		SQLModelBuilder postgresBuilder = new SQLModelBuilder();
//		postgresBuilder.findModelFiles(postgresModelDirectory);
//		postgresBuilder.readFiles();
//
//		if(!mysqlBuilder.areModelsFormattedCorrectly() || !postgresBuilder.areModelsFormattedCorrectly()) {
//			return;
//		}
	}
}
