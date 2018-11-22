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

	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
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

	/**
	 * Adds a SELECT query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	public String select(String value, String query) {
		return String.format("SELECT %1$s%2$s", value, query);
	}

	/**
	 * Adds a FROM query to a string
	 * @param location location of search
	 * @param query rest of query
	 * @return query
	 */
	public String from(String location, String query) {
		return String.format(" FROM %1$s%2$s", location, query);
	}

	/**
	 * Adds a WHERE query to a string
	 * @param value value
	 * @param typeOf Class type of query
	 * @param query rest of query
	 * @return query
	 */
	public <T> String where(String key, Object value, Class<T> typeOf, String query) {
		if(typeOf.equals(STRING)) {
			return String.format(" WHERE %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" WHERE %1$s=%2$s%3$s", key, value, query);
		}
	}

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param typeOf Class type of query
	 * @param query rest of query
	 * @return query
	 */
	public <T> String and(String key, Object value, Class<T> typeOf, String query) {
		if(typeOf.equals(STRING)) {
			return String.format(" AND %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" AND %1$s=%2$s%3$s", key, value, query);
		}
	}

	/**
	 * Adds ( query to a string
	 * @param outerQuery left side of query
	 * @param innerQuery right side of query
	 * @return query
	 */
	public String openParentheses(String outerQuery, String innerQuery) {
		return String.format("%1$s(%2$s", outerQuery, innerQuery);
	}

	/**
	 * Adds ) query to a string
	 * @param outerQuery left side of query
	 * @param innerQuery right side of query
	 * @return query
	 */
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
			if(classOfT.equals(STRING)) {
				stmt.setString(1, (String) value);
			} else if(classOfT.equals(INTEGER)) {
				stmt.setInt(1, (int) value);
			} else if(classOfT.equals(BOOLEAN)) {
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
//			LOGGER.debug("Executing query: {}", stmt.toString());
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
//			LOGGER.debug("Executing update: {}", stmt.toString());
			return stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return Integer.MIN_VALUE;
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
			if(classOfT.equals(STRING)) {
				statement.setString(index, (String) value);
			} else if(classOfT.equals(INTEGER)) {
				statement.setInt(index, (int) value);
			} else if(classOfT.equals(BOOLEAN)) {
				statement.setBoolean(index, (boolean) value);
			}
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
	}
}
