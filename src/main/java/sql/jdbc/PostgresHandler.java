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

	private Connection dbConn;
	private Map<String, SQLModel> models;

	private static final Logger LOGGER = LogManager.getLogger();

	public PostgresHandler(Map<String, SQLModel> models) {
		this.models = models;
		try {
			Class.forName("org.postgresql.Driver");
			LOGGER.debug("Got driver.");
		} catch (ClassNotFoundException e) {
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
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
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
				LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}
	}

	@Override
	public Connection getConnection() {
		return dbConn;
	}

//	/**
//	 * Regular select from table
//	 * @return resultset of query
//	 */
//	public ResultSet select(String tableName, String column) {
//		String selectStmt = String.format("SELECT %1$s FROM %2$s", column, this.models.get(tableName).getModelName());
//		try {
//			//create a statement object
//			PreparedStatement stmt = this.dbConn.prepareStatement(selectStmt);
//			//execute a query, which returns a ResultSet object
//			return stmt.executeQuery();
//		} catch (SQLException e) {
//			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
//		}
//		return null;
//	}

//	public ResultSet selectFromInnerJoin(String table, String column, String otherTable, String key, String value,
//									  boolean valueIsInt, String otherKey, String otherValue, boolean otherValueIsInt) {
//		if(!valueIsInt) {
//			value = String.format("'%s'", value);
//		}
//		if(!otherValueIsInt) {
//			otherValue = String.format("'%s'", otherValue);
//		}
//		String selectStmt = String.format("SELECT %1$s FROM (%2$s INNER JOIN %3$s ON (%4$s=%5$s AND %6$s=%7$s))",
//				column, table, otherTable, key, value, otherKey, otherValue);
//		try {
//			//create a statement object
//			PreparedStatement stmt = this.dbConn.prepareStatement(selectStmt);
//			//execute a query, which returns a ResultSet object
//			return stmt.executeQuery();
//		} catch (SQLException e) {
//			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
//		}
//		return null;
//	}

	public String select(String value, String query) {
		return String.format("SELECT %1$s%2$s", value, query);
	}

	public String from(String location, String query) {
		return String.format(" FROM %1$s%2$s", location, query);
	}

	public <T> String where(String key, Object value, Class<T> typeOf, String query) {
		if(typeOf.equals(String.class)) {
			return String.format(" WHERE %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" WHERE %1$s=%2$s%3$s", key, value, query);
		}
	}

	public <T> String and(String key, Object value, Class<T> typeOf, String query) {
		if(typeOf.equals(String.class)) {
			return String.format(" AND %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" AND %1$s=%2$s%3$s", key, value, query);
		}
	}

//	public JDBCHandler select(String value) {
//		this.query += String.format("SELECT %s", value);
//		return this;
//	}
//
//	public JDBCHandler from(String location) {
//		this.query += String.format(" FROM %s", location);
//		return this;
//	}
//
//	public <T> JDBCHandler where(String key, Object value, Class<T> typeOf) {
//		if(typeOf.equals(String.class)) {
//			this.query += String.format(" WHERE %1$s='%2$s'", key, value);
//		} else {
//			this.query += String.format(" WHERE %1$s=%2$s", key, value);
//		}
//		return this;
//	}
//
//	public <T> JDBCHandler and(String key, Object value, Class<T> typeOf) {
//		if(typeOf.equals(String.class)) {
//			this.query += String.format(" AND %1$s='%2$s'", key, value);
//		} else {
//			this.query += String.format(" AND %1$s=%2$s", key, value);
//		}
//		return this;
//	}

	public String openParentheses(String outerQuery, String innerQuery) {
		return String.format("%1$s(%2$s", outerQuery, innerQuery);
	}

	public String closeParentheses(String outerQuery, String innerQuery) {
		return String.format("%1$s)%2$s", outerQuery, innerQuery);
	}

	/**
	 * Insert 1 value into the table
	 * @param tableName
	 * @param column
	 * @param value
	 * @param classOfT
	 * @param <T>
	 * @return this
	 */
	public <T> String insert(String tableName, String column, Object value, Class<T> classOfT) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(
					String.format("INSERT INTO %1$s (%2$s) VALUES (?)",
							this.models.get(tableName).getModelName(), column));
			if(classOfT.equals(String.class)) {
				stmt.setString(1, (String) value);
			} else if(classOfT.equals(Integer.class)) {
				stmt.setInt(1, (int) value);
			} else if(classOfT.equals(Boolean.class)) {
				stmt.setBoolean(1, (boolean) value);
			}
			return stmt.toString();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	/**
	 * Insert 2 values into the table
	 * @param tableName
	 * @param column1
	 * @param value1
	 * @param classOf1
	 * @param column2
	 * @param value2
	 * @param classOf2
	 * @param <T>
	 * @return this
	 */
	public <S, T> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
								  String column2, Object value2, Class<T> classOf2) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(
					String.format("INSERT INTO %1$s (%2$s, %3$s) VALUES (?, ?)",
							this.models.get(tableName).getModelName(), column1, column2));
			setStatementValue(stmt, 1, value1, classOf1);
			setStatementValue(stmt, 2, value2, classOf2);
			return stmt.toString();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	/**
	 * Insert 3 values into the table
	 * @param tableName
	 * @param column1
	 * @param value1
	 * @param classOf1
	 * @param column2
	 * @param value2
	 * @param classOf2
	 * @param column3
	 * @param value3
	 * @param classOf3
	 * @param <T>
	 * @return this
	 */
	public <S, T, U> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
								  String column2, Object value2, Class<T> classOf2,
								  String column3, Object value3, Class<U> classOf3) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(
					String.format("INSERT INTO %1$s (%2$s, %3$s, %4$s) VALUES (?, ?, ?)",
							this.models.get(tableName).getModelName(), column1, column2, column3));
			setStatementValue(stmt, 1, value1, classOf1);
			setStatementValue(stmt, 2, value2, classOf2);
			setStatementValue(stmt, 3, value3, classOf3);
			return stmt.toString();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	public ResultSet executeQuery(String query) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(query);
			//execute a query, which returns a ResultSet object
			LOGGER.debug("Executing query: {}", stmt.toString());
			return stmt.executeQuery();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	public int executeUpdate(String query) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(query);
			//execute a query, which returns a ResultSet object
			LOGGER.debug("Executing query: {}", stmt.toString());
			return stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return Integer.MIN_VALUE;
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
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
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
		String insertStmt = String.format("INSERT INTO %1$s (%2$s, %3$s) VALUES (?, (SELECT %3$s FROM %4$s WHERE %5$s='%6$s'))"
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

	public String getTable(String tableName) {
		return this.models.get(tableName).toString();
	}

	/**
	 * Sets the value of a prepared statement at the given index
	 * @param statement
	 * @param index
	 * @param value
	 * @param classOfT
	 * @param <T>
	 */
	private <T> void setStatementValue(PreparedStatement statement, int index, Object value, Class<T> classOfT) {
		try {
			if(classOfT.equals(String.class)) {
				statement.setString(index, (String) value);
			} else if(classOfT.equals(Integer.class)) {
				statement.setInt(index, (int) value);
			} else if(classOfT.equals(Boolean.class)) {
				statement.setBoolean(index, (boolean) value);
			}
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}
}
