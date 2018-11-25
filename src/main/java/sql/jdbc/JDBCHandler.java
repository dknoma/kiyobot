package sql.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JDBCHandler {

	/**
	 * Sets up the connection for JDBC to the database
	 * @param db sql
	 * @param host host
	 * @param port port
	 * @param username un
	 * @param password pw
	 */
	public void setConnection(String db, String host, String port, String username, String password);

	/**
	 * Actually creates and puts it into the database
	 * @throws SQLException;
	 */
	public void createTables() throws SQLException;

	/**
	 * Gets the JDBC connection
	 * @return connection
	 */
	public Connection getConnection();

	/**
	 * Adds a SELECT query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	public String select(String value, String query);

	/**
	 * Adds a FROM query to a string
	 * @param location location of search
	 * @param query rest of query
	 * @return query
	 */
	public String from(String location, String query);

	/**
	 * Adds a WHERE query to a string
	 * @param value value
	 * @param classOfT Class type of query
	 * @param query rest of query
	 * @return query
	 */
	public <T> String where(String key, Object value, Class<T> classOfT, String query);

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param classOfT Class type of query
	 * @param query rest of query
	 * @return query
	 */
	public <T> String and(String key, Object value, Class<T> classOfT, String query);

	/**
	 * Adds ( query to a string
	 * @param query;
	 * @return query
	 */
	public String openParentheses(String query);

	/**
	 * Adds ) query to a string
	 * @param query;
	 * @return query
	 */
	public String closeParentheses(String query);

	/**
	 * Insert values into the table
	 * @param tableName;
	 * @return this
	 */
	public <T> String insert(String tableName, ColumnObject<?>... columns);

	/**
	 * Adds ) query to a string
	 * @param table1 left side of query
	 * @param table2 right side of query
	 * @return query
	 */
	public String innerJoin(String table1, String table2, String query);

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param classOfT Class type of query
	 * @param query rest of query
	 * @return query
	 */
	public <T> String on(String key, Object value, Class<T> classOfT, String query);

	/**
	 * Executes a query on the db
	 * @param query;
	 * @return result set
	 */
	public ResultSet executeQuery(String query);

	/**
	 * Executes an update on the db
	 * @param query;
	 * @return result set
	 */
	public int executeUpdate(String query);

	/**
	 * Gets the table from the table name
	 * @param tableName;
	 * @return String representation
	 */
	public String getTable(String tableName);
}
