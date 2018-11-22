package sql;

import javafx.geometry.Pos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.jdbc.JDBCEnum;
import sql.jdbc.JDBCHandler;
import sql.jdbc.PostgresHandler;
import sql.model.SQLModel;
import sql.util.JsonSqlConfigParser;
import sql.util.SQLModelBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class Tester {

	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final String TODO = "Todo";
	private static final String TODO_ITEM = "TodoItem";
	private static final String CONFIG_FILE = "./config/sqlconfig.json";
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		JsonSqlConfigParser parser = new JsonSqlConfigParser();
		parser.parseConfig(CONFIG_FILE);

		SQLModelBuilder builder = new SQLModelBuilder(parser);
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
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish single insert.",
//				STRING, "todoid", 1, INTEGER, "completed", false, BOOLEAN));
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish double insert.",
//				STRING, "todoid", 1, INTEGER, "completed", false, BOOLEAN));
//
//		// insert into second todolist
//		handler.executeUpdate(handler.insert("todoitem", "content", "Finish JDBCHandler.",
//				STRING, "todoid", 2, INTEGER, "completed", false, BOOLEAN));



		String referenceKey = "todoid";
		int referenceId = 1;
		printResults(handler, referenceKey, referenceId);
		printResults(handler, referenceKey, 2);
	}

	private static void printResults(JDBCHandler handler, String referenceKey, int referenceId) {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
				handler.from("todo",
				handler.where(referenceKey, referenceId, INTEGER, "")))
		);
		ResultSet results = handler.executeQuery(
				handler.select("*",
				handler.from("todoitem",
				handler.where(referenceKey, referenceId, INTEGER, "")))
		);
		getAllResults(referenceKey, referenceId, results);
	}

	private static void getAllResults(String referenceKey, int referenceId, ResultSet results) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		try {
			sb.append(String.format("\t\"%1$s\": %2$s\n", referenceKey, referenceId));
			// resultset starts BEFORE first row, need to call next for all results
			String tableName = results.getMetaData().getTableName(1);
			sb.append(String.format("\t\"%s\": [\n", tableName));
			while(results.next()) {
				sb.append("\t\t{\n");
				for (int i = 1; i < results.getMetaData().getColumnCount(); i++) {
					String columnType = results.getMetaData().getColumnTypeName(i);
					String columnName = results.getMetaData().getColumnName(i);
//					System.out.println(String.format("i: %1$s, \ttype: %2$s",
//							results.getMetaData().getColumnName(i), columnType));
					if(isString(columnType)) {
						sb.append(String.format("\t\t\t\"%1$s\": \"%2$s\"\n", columnName, results.getString(columnName)));
					} else if(isInt(columnType)) {
						sb.append(String.format("\t\t\t\"%1$s\": %2$s\n", columnName, results.getInt(columnName)));
					} else if(isBoolean(columnType)) {
						sb.append(String.format("\t\t\t\"%1$s\": %2$s\n", columnName, results.getBoolean(columnName)));
					}
				}
				if(!results.isLast()) {
					sb.append("\t\t},\n");
				} else {
					sb.append("\t\t}\n");
				}
			}
			sb.append("\t]\n");
			sb.append("}");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(sb.toString());
	}

	private static boolean isString(String columnType) {
		if(columnType.equals("character") || columnType.equals("char")
				|| columnType.equals("varchar") || columnType.equals("longvarchar")
				|| columnType.equals("text")) {
			return true;
		}
		return false;
	}

	private static boolean isInt(String columnType) {
		if(columnType.equals("tinyint") || columnType.equals("smallint")
				|| columnType.equals("int") || columnType.equals("serial")) {
			return true;
		}
		return false;
	}

	private static boolean isBoolean(String columnType) {
		if(columnType.equals("bool") || columnType.equals("boolean")) {
			return true;
		}
		return false;
	}
}
