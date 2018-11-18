package db.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A handler for PostgreSQL queries.
 *
 * NOTE: Postgres uses 'single quotes' for queries rather than "double quotes".
 *
 * @author dk
 */
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
		String primaryKey = String.format("%sid", tableName).toLowerCase();
		sb.append("create table ");
		sb.append(tableName).append(" ");
		sb.append("(").append(primaryKey);
		if(autoIncrement) {
			sb.append(" SERIAL PRIMARY KEY");
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
		String updatedTable = String.format("%1$s, %2$s int, FOREIGN KEY (%2$s) REFERENCES %3$s (%2$s) ON DELETE CASCADE",
				TABLE_NAMES.get(tableName), foreignKey, referenceTableName);
//		String updatedTable = String.format("%1$s, ADD CONSTRAINT %2$sconstr FOREIGN KEY (%3$s) REFERENCES %4$s (%5$s)",
//				TABLE_NAMES.get(tableName), tableName, foreignKey, referenceTableName, foreignKey);
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

	/**
	 * Regular select from table
	 * @param table name
	 * @return resultset of query
	 */
	public ResultSet select(String table, String column) {
		String selectStmt = String.format("SELECT %1$s FROM %2$s", column, table);
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

	public ResultSet selectFromInnerJoin(String table, String column, String otherTable, String key, String value,
									  boolean valueIsInt, String otherKey, String otherValue, boolean otherValueIsInt) {
		if(!valueIsInt) {
			value = String.format("'%s'", value);
		}
		if(!otherValueIsInt) {
			otherValue = String.format("'%s'", otherValue);
		}
		String selectStmt = String.format("SELECT %1$s FROM (%2$s INNER JOIN %3$s ON (%4$s=%5$s AND %6$s=%7$s))",
				column, table, otherTable, key, value, otherKey, otherValue);
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

	/**
	 * Regular select from table
	 * @param table name
	 * @return resultset of query
	 */
	public ResultSet select(String table, String column, String key, String value) {
		String selectStmt = String.format("SELECT %1$s FROM %2$s WHERE %3$s='%4$s'", column, table, key, value);
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

	/**
	 * Inserts a string value into column key of a table
	 * @param table name
	 * @param key key
	 * @param value value
	 */
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

	/**
	 * Insert string with foreign key into column key of a table
	 * TODO: postgres seems to like ' ' rather than " "
	 * @param table name
	 * @param key key
	 * @param value value
	 * @param foreignTable reference table
	 * @param type reference key
	 * @param typeValue reference name
	 */
	public void insertString(String table, String key, String value, String foreignTable, String type, String typeValue) {
		String foreignKey = TABLE_PRIMARY_KEYS.get(foreignTable);
		String insertStmt = String.format("INSERT INTO %1$s (%2$s, %3$s) VALUES (?, (SELECT %3$s from %4$s WHERE %5$s='%6$s'))"
				, table, key, foreignKey, foreignTable, type, typeValue);
		//TODO: this only works when in same db, if different dbs would need to just pass foreign key itself
		//(SELECT id from foo WHERE type='blue')
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
