package sql;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.geometry.Pos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.jdbc.JDBCEnum;
import sql.jdbc.JDBCHandler;
import sql.jdbc.PostgresHandler;
import sql.jdbc.ResultSetHandler;
import sql.model.SQLModel;
import sql.util.JsonSqlConfigParser;
import sql.util.SQLModelBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class Tester {

	private static final Gson gson = new Gson();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
//	private static final String TODO = "Todo";
//	private static final String TODO_ITEM = "TodoItem";
	private static final String CONFIG_FILE = "./config/sqlconfig.json";
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		JsonSqlConfigParser parser = new JsonSqlConfigParser();
		parser.parseConfig(CONFIG_FILE);

		SQLModelBuilder builder = new SQLModelBuilder();
		builder.findModelFiles("./models");
		for(String s : builder.getModelFiles()) {
			System.out.println(s);
		}
		builder.readFiles();

		Map<String, SQLModel> models = builder.getCopyOfModels();
//		for(Map.Entry<String, SQLModel> entry : models.entrySet()) {
//			SQLModel copy = entry.getValue();
//			System.out.println(copy.toString());
//			copy.getQuery();
//		}

		String dbName = parser.getDbName();
		PostgresHandler pghandler = new PostgresHandler(models);
		JDBCEnum.addJDBCHandler(dbName, pghandler);

		JDBCHandler handler = JDBCEnum.getJDBCHandler(dbName);
		try {
			handler.setConnection(parser.getDb(), parser.getHost(), parser.getPort(),
					parser.getUsername(), parser.getPassword());
			handler.createTables();
			System.out.println(handler.getTable("todo"));
			System.out.println(handler.getTable("todoitem"));
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}

//		// insert todolists into table
//		// inserts and updated need executeUpdate as no data is returned
//		handler.executeUpdate(handler.insert("todo", "title", "First", STRING));
//		handler.executeUpdate(handler.insert("todo", "title", "Second", STRING));
//
//		// insert into first todolist
		handler.executeUpdate(handler.insert("todoitem", "content", "Finish single insert.",
				STRING, "todoid", 1, INTEGER, "completed", false, BOOLEAN));
		handler.executeUpdate(handler.insert("todoitem", "content", "Finish double insert.",
				STRING, "todoid", 1, INTEGER, "completed", false, BOOLEAN));
//
//		// insert into second todolist
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish JDBCHandler.",
//				STRING, "todoid", 2, INTEGER, "completed", false, BOOLEAN));

		// Called in one service; when user POSTs, service will send GET to other service to receive json results
		String referenceKey = "todoid";
		int referenceId = 1;
		String out = ResultSetHandler.getInfoFromReference(handler, referenceKey, referenceId);

		// This is the result that will be received by other service
		System.out.println(out);
		JsonObject obj = getInfoFromJson(out);
		String referencePrimaryKey = obj.get("primaryKey").getAsString();
		int id = obj.get("id").getAsInt();

		System.out.println(ResultSetHandler.getResultSetWithReference(handler, referencePrimaryKey, id));
		System.out.println(ResultSetHandler.getResultSetWithReference(handler, referencePrimaryKey, 2));
	}

	private static JsonObject getInfoFromJson(String json) {
		return gson.fromJson(json, JsonObject.class);
	}
}
