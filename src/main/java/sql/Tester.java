package sql;

import javafx.geometry.Pos;
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

	private static final String TODO = "Todo";
	private static final String TODO_ITEM = "TodoItem";
	private static final String CONFIG_FILE = "./config/sqlconfig.json";

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
		} catch (SQLException e) {
			e.printStackTrace();
		}

		int results = handler.insert("todo", "title", "First", String.class).executeUpdate();
		System.out.println(results);

//
//		JDBCEnum handlers = JDBCEnum.INSTANCE;
//		handlers.addJDBCHandler(TODO, new PostgresHandler());
////		handlers.addJDBCHandler(TODO_ITEM, new PostgresHandler());
//
//		JDBCHandler todo = handlers.getJDBCHandler(TODO);
////		JDBCHandler todoItem = handlers.getJDBCHandler(TODO_ITEM);
//
//		todo.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());
////		todoItem.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());
//
//		String descripiton = "description";
//		String title = "title";

//		try {
//			todo.setupTable(TODO, true);
//			todo.addStringKey(TODO, title, true, 40, true);
//			todo.createTable(TODO);
//			System.out.println("create todo table query: " + todo.getTable(TODO));
//
//			todo.setupTable(TODO_ITEM, true);
//			todo.addStringKey(TODO_ITEM, descripiton, true, 100, true);
//			todo.addForeignKey(TODO_ITEM, TODO);
//			todo.createTable(TODO_ITEM);
//			System.out.println("create todoitem table query: " + todo.getTable(TODO_ITEM));
////
////
////			todo.insertString(TODO, title, "JDBC TODOs");
////			todo.insertString(TODO_ITEM, descripiton, "Fix up all the things.", TODO, title, "JDBC TODOs");
////			todo.insertString(TODO_ITEM, descripiton, "Second todo.", TODO, title, "JDBC TODOs");
////			todo.insertString(TODO_ITEM, descripiton, "SO MANY TODOS.", TODO, title, "JDBC TODOs");
////
////			todo.insertString(TODO, title, "Second TODO List");
////			todo.insertString(TODO_ITEM, descripiton, "FIRST.", TODO, title, "Second TODO List");
////			todo.insertString(TODO_ITEM, descripiton, "Wow another list.", TODO, title, "Second TODO List");
////
////			todo.insertString(TODO, title, "Shortlist");
////			todo.insertString(TODO_ITEM, descripiton, "eh.", TODO, title, "Shortlist");
////			todo.insertString(TODO_ITEM, descripiton, "sttet.", TODO, title, "Shortlist");
//
//			ResultSet results = todo.select(TODO, "*");
//			while(results.next()) {
//				String out = results.getString(title);
//				System.out.println("title: " + out);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}

//		SQLModel test = new SQLModel("test");
//		test.addColumn("key", String.class, true);
	}
}
