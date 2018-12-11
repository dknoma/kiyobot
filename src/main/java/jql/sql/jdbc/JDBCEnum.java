package jql.sql.jdbc;

import jql.sql.model.SQLModel;

import java.util.Map;

public enum JDBCEnum {

	INSTANCE();

	private static String DB_NAME;
	private static JDBCHandler HANDLER;
	private static Map<String, SQLModel> MODELS;

	JDBCEnum() {
	}

	/**
	 * Adds a JDBC handler to the map
	 * @param dbName;
	 * @param handler;
	 */
	public void addJDBCHandler(String dbName, JDBCHandler handler, Map<String, SQLModel> models) {
		DB_NAME = dbName;
		HANDLER = handler;
		MODELS = models;
	}

	/**
	 * Gets the JDBC handler from the map
	 * @return handler
	 */
	public String getDbName() {
		return DB_NAME;
	}

	/**
	 * Gets the JDBC handler from the map
	 * @return handler
	 */
	public JDBCHandler getJDBCHandler() {
		return HANDLER;
	}

	/**
	 * Gets the SQL models of this database
	 * @return models
	 */
	public Map<String, SQLModel> getModels() {
		return MODELS;
	}
}
