package db;

import db.jdbc.JDBCEnum;
import db.jdbc.JDBCHandler;
import db.jdbc.PostgresHandler;
import db.util.JsonSqlConfigParser;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tester {

	private static final String TODO = "Todo";
	private static final String TODO_ITEM = "TodoItem";

	public static void main(String[] args) {
		JsonSqlConfigParser parser = new JsonSqlConfigParser();
		parser.parseConfig();

		JDBCEnum handlers = JDBCEnum.INSTANCE;
		handlers.addJDBCHandler(TODO, new PostgresHandler());
//		handlers.addJDBCHandler(TODO_ITEM, new PostgresHandler());

		JDBCHandler todo = handlers.getJDBCHandler(TODO);
//		JDBCHandler todoItem = handlers.getJDBCHandler(TODO_ITEM);

		todo.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());
//		todoItem.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());

		String descripiton = "description";
		String title = "title";

		try {
			todo.setupTable(TODO, true);
			todo.addStringKey(TODO, title, true, 40, true);
			todo.createTable(TODO);
			System.out.println("create todo table query: " + todo.getTable(TODO));

			todo.setupTable(TODO_ITEM, true);
			todo.addStringKey(TODO_ITEM, descripiton, true, 100, true);
			todo.addForeignKey(TODO_ITEM, TODO);
			todo.createTable(TODO_ITEM);
			System.out.println("create todoitem table query: " + todo.getTable(TODO_ITEM));


//			todo.insertString(TODO, title, "JDBC TODOs");

//			todo.insertString(TODO_ITEM, descripiton, "Fix up all the things.", TODO, title, "JDBC TODOs");
//			todo.insertString(TODO_ITEM, descripiton, "Second todo.", TODO, title, "JDBC TODOs");
//			todo.insertString(TODO_ITEM, descripiton, "SO MANY TODOS.", TODO, title, "JDBC TODOs");

			todo.insertString(TODO, title, "Second TODO List");
			todo.insertString(TODO_ITEM, descripiton, "FIRST.", TODO, title, "Second TODO List");
			todo.insertString(TODO_ITEM, descripiton, "Wow another list.", TODO, title, "Second TODO List");

//			ResultSet results = todo.select(TODO);
//			while(results.next()) {
//				String out = results.getString(descripiton);
//				System.out.println("Result: " + out);
//			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
