package db.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PostgresHandler implements JDBCHandler {

	private Connection dbConn;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, String> TABLE_NAMES = new HashMap<>();
	private static final Map<String, String> TABLE_PRIMARY_KEYS = new HashMap<>();

	public PostgresHandler() {
		try {
			Class.forName("org.postgresql.Driver");
			LOGGER.debug("Got driver.");
		} catch (java.lang.ClassNotFoundException e) {
			LOGGER.error("Class error has occurred: {},\n {}", e.getMessage(), e.getStackTrace());
		}
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
			// format: jdbc:postgresql://host:port/pathOrDatabaseName
			String dbURL = String.format("jdbc:postgresql://%1$s:%2$s/%3$s", host, port, db);
			this.dbConn = DriverManager.getConnection(dbURL, username, password);
			LOGGER.info("Connected to db successfully!");
		} catch (SQLException e) {
			LOGGER.error("An SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}

	/**
	 * Sets up the initial table
	 * @param tableName name
	 * @throws SQLException s
	 */
	public void setupTable(String tableName, boolean autoIncrement) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String primaryKey = String.format("%sid", tableName);
		sb.append("create table ");
		sb.append(tableName).append(" ");
		sb.append("(").append(primaryKey);
		if(autoIncrement) {
			sb.append(" SERIAL");
		}
		// adds the table and primary key to the respective maps
		TABLE_NAMES.put(tableName, sb.toString());
		TABLE_PRIMARY_KEYS.put(tableName, primaryKey);
	}

//	public void dropTable() {
//		"SELECT pg_terminate_backend(pg_stat_activity.pid)\n" +
//				"FROM pg_stat_activity\n" +
//				"WHERE pg_stat_activity.datname = 'TARGET_DB' -- ← change this to your DB\n" +
//				"  AND pid <> pg_backend_pid();";
//	}

	/**
	 * Adds a key of type string to the table
	 * @param tableName
	 * @param key
	 * @param isVarchar
	 * @param chars
	 * @param notNull
	 */
	public void addStringKey(String tableName, String key, boolean isVarchar, int chars, boolean notNull) {
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
		String foreignKey = TABLE_PRIMARY_KEYS.get(referenceTableName);
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

	public void closeTable(String tableName) {
		String table = TABLE_NAMES.get(tableName);
		String updatedTable = String.format("%s)", table);
		TABLE_NAMES.replace(tableName, updatedTable);
	}

	public String getTable(String tableName) {
		return TABLE_NAMES.get(tableName);
	}

	@Override
	public Connection getConnection() {
		return dbConn;
	}

	public ResultSet select(String table) {
		String selectStmt = String.format("SELECT * FROM %s", table);
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(selectStmt);
			//execute a query, which returns a ResultSet object
			return stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void insertString(String table, String key, String value) {
		String insertStmt = String.format("INSERT INTO %1$s (%2$s) VALUES (?)", table, key);
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(insertStmt);
			stmt.setString(1, value);
			//execute a query, which returns a ResultSet object
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
