package db;

import db.jdbc.JDBCPostgresHandler;
import db.util.JsonSqlConfigParser;

import java.sql.SQLException;

public class Tester {

	public static void main(String[] args) {
		JsonSqlConfigParser parser = new JsonSqlConfigParser();
		parser.parseConfig();

		JDBCPostgresHandler sqlHandler = JDBCPostgresHandler.INSTANCE;

		sqlHandler.setConnection(parser.getDb(), parser.getHost(), parser.getPort(), parser.getUsername(), parser.getPassword());

		try {
			sqlHandler.setupTable("todo");
			sqlHandler.addPrimaryKey("todo");
			sqlHandler.addStringKey("todo", "description", true, 100, true);
			System.out.println("DADADLJ: " + sqlHandler.getTable("todo"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
