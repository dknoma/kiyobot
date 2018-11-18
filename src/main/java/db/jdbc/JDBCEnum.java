package db.jdbc;

import java.util.HashMap;
import java.util.Map;

public enum JDBCEnum {

	INSTANCE();

	private static final Map<String, JDBCHandler> HANDLER_MAP = new HashMap<>();

	JDBCEnum() {
	}

	/**
	 * Adds a JDBC handler to the map
	 * @param dbName
	 * @param handler
	 */
	public void addJDBCHandler(String dbName, JDBCHandler handler) {
		HANDLER_MAP.put(dbName, handler);
	}

	/**
	 * Gets the JDBC handler from the map
	 * @param dbName
	 * @return
	 */
	public JDBCHandler getJDBCHandler(String dbName) {
		return HANDLER_MAP.get(dbName);
	}
}
