package db;

import db.jdbc.JDBCEnum;
import db.jdbc.JDBCHandler;
import db.jdbc.PostgresHandler;
import db.util.JsonSqlConfigParser;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tester {

	private static final String TODO = "todo";

	public static void main(String[] args) {
		JsonSqlConfigParser parser = new JsonSqlConfigParser();
		parser.parseConfig();

		JDBCEnum handlers = JDBCEnum.INSTANCE;
		handlers.addJDBCHandler(TODO, new PostgresHandler());

		JDBCHandler todo = handlers.getJDBCHandler(TODO);

		todo.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());

		String descripiton = "description";


		try {
			todo.setupTable(TODO, true);
			todo.addPrimaryKey(TODO);
			todo.addStringKey(TODO, descripiton, true, 100, true);
//			atodo.closeTable(aTODO);
			todo.createTable(TODO);
//			System.out.println("DADADLJ: " + todo.getTable(TODO));

//			todo.insertString(TODO, descripiton, "This is a description. Fix pls.");
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
