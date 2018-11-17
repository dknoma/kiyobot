package db.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public enum JDBCPostgresHandler {

	INSTANCE();

	private Connection dbConn;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, String> TABLE_NAMES = new HashMap<>();
	private static final Map<String, String> TABLE_PRIMARY_KEYS = new HashMap<>();

	JDBCPostgresHandler() {
	}

	/**
	 * Sets up the connection for JDBC to the database
	 * @param db db
	 * @param host host
	 * @param port port
	 * @param username un
	 * @param password pw
	 */
	public void setConnection(String db, String host, String port, String username, String password) {
		try {
			Class.forName("org.postgresql.Driver");
			LOGGER.debug("Got driver.");

			String dbURL = String.format("jdbc:postgresql://%1$s:%2$s/%3$s", host, port, db);
			LOGGER.debug("db: {}", dbURL);

			this.dbConn = DriverManager.getConnection(dbURL, username, password);
			LOGGER.info("Connected to db successfully!");
		} catch (java.lang.ClassNotFoundException e) {
			LOGGER.error("Class error has occurred: {},\n {}", e.getMessage(), e.getStackTrace());
		} catch (SQLException e) {
			LOGGER.error("An SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}

	/**
	 * Sets up the initial table
	 * @param tableName
	 * @throws SQLException
	 */
	public void setupTable(String tableName) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String updatedTableName = tableName.toUpperCase();
		String primaryKey = String.format("%s_ID", updatedTableName);
		sb.append("create table ");
		sb.append(updatedTableName).append(" ");
		sb.append("(").append(primaryKey).append(" integer NOT NULL");
//				"SUP_NAME varchar(40) NOT NULL, " +
//				"STREET varchar(40) NOT NULL, " +
//				"CITY varchar(20) NOT NULL, " +
//				"STATE char(2) NOT NULL, " +
//				"ZIP char(5), " +
		// adds the table and primary key to the respective maps
		TABLE_NAMES.put(updatedTableName, sb.toString());
		TABLE_PRIMARY_KEYS.put(updatedTableName, primaryKey);
	}

	/**
	 * Adds a key of type string to the table
	 * @param tableName
	 * @param key
	 * @param isVarchar
	 * @param chars
	 * @param notNull
	 */
	public void addStringKey(String tableName, String key, boolean isVarchar, int chars, boolean notNull) {
		tableName = tableName.toUpperCase();
		key = key.toUpperCase();
		String table = TABLE_NAMES.get(tableName);
		String nullable = notNull ? " NOT NULL" : "";
		String updatedTable;
		if(isVarchar) {
			updatedTable = String.format("%1$s, %2$s varchar(%3$s)%4$s", table, key, chars, nullable);
		} else {
			updatedTable = String.format("%1$s, %2$s char(%3$s)%4$s", table, key, chars, nullable);
		}
		TABLE_NAMES.replace(tableName, updatedTable);
	}

	/**
	 * Adds primary key to the table
	 * @param tableName name of table
	 */
	public void addPrimaryKey(String tableName) {
		tableName = tableName.toUpperCase();
		String primaryKey = TABLE_PRIMARY_KEYS.get(tableName);
		String table = TABLE_NAMES.get(tableName);
		String updatedTable = String.format("%1$s, PRIMARY KEY (%2$s)", table, primaryKey);
		TABLE_NAMES.replace(tableName, updatedTable);
	}

	/**
	 * Updates a table to have a foreign key if it
	 * @param tableName name of table
	 * @param referenceTableName name of reference table
	 */
	public void addForeignKey(String tableName, String referenceTableName) {
		tableName = tableName.toUpperCase();
		String foreignKey = TABLE_PRIMARY_KEYS.get(referenceTableName);
		referenceTableName = referenceTableName.toUpperCase();
		String updatedTable = String.format("%1$s, FOREIGN KEY (%2$s) REFERENCES %3$s (%4$s)",
				TABLE_NAMES.get(tableName), foreignKey, referenceTableName, foreignKey);
		TABLE_NAMES.replace(tableName, updatedTable);
	}

	/**
	 * Actually creates and puts it into the database
	 * @param tableName
	 * @throws SQLException
	 */
	public void createTable(String tableName) throws SQLException {
		tableName = tableName.toUpperCase();
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

	public String getTable(String tableName) {
		tableName = tableName.toUpperCase();
		return TABLE_NAMES.get(tableName);
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
