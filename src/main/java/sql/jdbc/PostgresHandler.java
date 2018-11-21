package sql.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.model.SQLModel;

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

	private String query;
	private Connection dbConn;
	private Map<String, SQLModel> models;

	private static final Logger LOGGER = LogManager.getLogger();

	public PostgresHandler(Map<String, SQLModel> models) {
		this.query = "";
		this.models = models;
		try {
			Class.forName("org.postgresql.Driver");
			LOGGER.debug("Got driver.");
		} catch (java.lang.ClassNotFoundException e) {
			LOGGER.error("Class error has occurred: {},\n {}", e.getMessage(), e.getStackTrace());
		}
	}

	/**
	 * Sets up the connection for JDBC to the database
	 * @param db sql
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
			LOGGER.info("Connected to sql successfully!");
		} catch (SQLException e) {
			LOGGER.error("An SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}

	/**
	 * Actually creates and puts it into the database
	 * @throws SQLException
	 */
	public void createTables() throws SQLException {
		PreparedStatement stmt = null;
		for(Map.Entry<String, SQLModel> entry : this.models.entrySet()) {
			SQLModel model = entry.getValue();
			try {
				stmt = dbConn.prepareStatement(model.getQuery());
				stmt.executeUpdate();
			} catch (SQLException e) {
				LOGGER.error("Error: {}", e.getMessage());
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}
	}

	public void newQuery() {
		this.query = "";
	}

	@Override
	public Connection getConnection() {
		return dbConn;
	}

	/**
	 * Regular select from table
	 * @return resultset of query
	 */
	public ResultSet select(String tableName, String column) {
		String selectStmt = String.format("SELECT %1$s FROM %2$s", column, this.models.get(tableName).getModelName());
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

	public void select(String value) {
		this.query += String.format("SELECT %s", value);
	}

	public void from(String location) {
		this.query += String.format(" FROM %s", location);
	}

	public <T> void where(String key, Object value, Class<T> typeOf) {
		if(typeOf.equals(String.class)) {
			this.query += String.format(" WHERE %1$s='%2$s'", key, value);
		} else {
			this.query += String.format(" WHERE %1$s=%2$s", key, value);
		}
	}

	public ResultSet executeQuery() {
		try {
			try {
				//create a statement object
				PreparedStatement stmt = this.dbConn.prepareStatement(this.query);
				//execute a query, which returns a ResultSet object
				return stmt.executeQuery();
			} catch (SQLException e) {
				LOGGER.error("Error has occurred, {},\n{}", e.getMessage(), e.getStackTrace());
			}
			return null;
		} finally {
			newQuery();
		}
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
	 * @param key key
	 * @param value value
	 */
	public void insertString(String tableName, String key, String value) {
		String insertStmt = String.format("INSERT INTO %1$s (%2$s) VALUES (?)",
				this.models.get(tableName).getModelName(), key);
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
	 * @param key key
	 * @param value value
	 * @param type reference key
	 * @param typeValue reference name
	 */
	public void insertString(String tableName, String key, String value, String type, String typeValue) {
		SQLModel model = this.models.get(tableName);
		String insertStmt = String.format("INSERT INTO %1$s (%2$s, %3$s) VALUES (?, (SELECT %3$s from %4$s WHERE %5$s='%6$s'))"
				, model.getModelName(), key, model.getForeignKey(), model.getForeignKey(), type, typeValue);
		//TODO: this only works when in same sql, if different dbs would need to just pass foreign key itself
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

	public String getQuery(String tableName){
		return this.query;
	}

	public String getTable(String tableName) {
		return this.models.get(tableName).toString();
	}
}
