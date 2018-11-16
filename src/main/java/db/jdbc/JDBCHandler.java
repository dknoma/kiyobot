package db.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public enum JDBCHandler {

	INSTANCE();

	private Connection dbConn;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, String> TABLE_NAMES = new HashMap<>();
	private static final Map<String, String> TABLE_PRIMARY_KEYS = new HashMap<>();

	JDBCHandler() {
		try {
			Class.forName("org.postgresql.Driver");
		}
		catch (java.lang.ClassNotFoundException e) {
			System.out.println(String.format("Class error has occurred: %1$s,\n %2$s",
					e.getMessage(), e.getStackTrace().toString()));
		}
	}

	public void setupTable(String tableName, String... keysAndProperties) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String updatedTableName = tableName.toUpperCase();
		String primaryKey = String.format("%s_ID", updatedTableName);
		sb.append("create table ");
		sb.append(updatedTableName).append(" ");
		sb.append("(").append(primaryKey).append(" integer NOT NULL, ");
		for(String keyAndProperties : keysAndProperties) {
			sb.append(keyAndProperties).append(", ");
		}
		//TODO: make separate method to all table keys. make it a lot easier to add things
//				"SUP_NAME varchar(40) NOT NULL, " +
//				"STREET varchar(40) NOT NULL, " +
//				"CITY varchar(20) NOT NULL, " +
//				"STATE char(2) NOT NULL, " +
//				"ZIP char(5), " +
		sb.append("PRIMARY KEY (").append(primaryKey).append(")");
		// adds the table and primary key to the respective maps
		TABLE_NAMES.put(updatedTableName, sb.toString());
		TABLE_PRIMARY_KEYS.put(updatedTableName, primaryKey);
	}

	/**
	 * Updates a table to have a foreign key if it
	 * @param tableName
	 * @param referenceTableName
	 */
	public void addForeignKey(String tableName, String referenceTableName) {
		String foreignKey = TABLE_PRIMARY_KEYS.get(referenceTableName);
		referenceTableName = referenceTableName.toUpperCase();
		String updatedTable = String.format("%1$s, FOREIGN KEY (%2$s) REFERENCES %3$s (%4$s)",
				TABLE_NAMES.get(tableName), foreignKey, referenceTableName, foreignKey);
		TABLE_NAMES.replace(tableName, updatedTable);
	}

	public void createTable(String tableName) throws SQLException {
		String table = String.format("%s)", TABLE_NAMES.get(tableName));

		PreparedStatement stmt = null;
		try {
			stmt = dbConn.prepareStatement(table);
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error: {}", e.getMessage());
		} finally {
			if (stmt != null) { stmt.close(); }
		}
	}

	/**
	 * Sets up the connection for JDBC to the database
	 * @param url db url
	 * @param username un
	 * @param password pw
	 */
	public void setConnection(String url, String username, String password) {
		try {
			this.dbConn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			LOGGER.error("An SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}

	public ResultSet select(String table) {
		String selectStmt = String.format("SELECT * FROM %s", table);
		try {
			//create a statement object
			PreparedStatement stmt = null;
			stmt = this.dbConn.prepareStatement(selectStmt);
			//execute a query, which returns a ResultSet object
			ResultSet result = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
