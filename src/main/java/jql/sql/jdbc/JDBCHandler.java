package jql.sql.jdbc;

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
	void setConnection(String db, String host, String port, String username, String password);

	/**
	 * Actually creates and puts it into the database
	 * @throws SQLException;
	 */
	void createTables() throws SQLException;

	/**
	 * Gets the JDBC connection
	 * @return connection
	 */
	Connection getConnection();

	/**
	 * Adds a SELECT query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	String select(String value, String query);

	/**
	 * Adds a FROM query to a string
	 * @param location location of search
	 * @param query rest of query
	 * @return query
	 */
	String from(String location, String query);

	/**
	 * Adds a WHERE query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	<T> String where(String key, T value, String query);

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	<T> String and(String key, T value, String query);

	/**
	 * Adds ( query to a string
	 * @param query;
	 * @return query
	 */
	String openParentheses(String query);

	/**
	 * Adds ) query to a string
	 * @param query;
	 * @return query
	 */
	String closeParentheses(String query);

	/**
	 * Insert values into the table
	 * @param tableName;
	 * @param columns variable number of ColumnObjects
	 * @return this
	 */
	String insert(String tableName, ColumnObject... columns);

	/**
	 * Adds ) query to a string
	 * @param table1 left side of query
	 * @param table2 right side of query
	 * @return query
	 */
	String innerJoin(String table1, String table2, String query);

	/**
	 * Adds an AND query to a string
	 * @param key;
	 * @param value;
	 * @param query rest of query
	 * @return query
	 */
	<T> String on(String key, T value, String query);

	/**
	 * Adds an AND query to a string
	 * @param location;
	 * @param query rest of query
	 * @return query
	 */
	<T> String update(String location, String query);

	/**
	 * Adds an AND query to a string
	 * @param query rest of query
	 * @param columns variable number of columns
	 * @return query
	 */
	String set(String query, ColumnObject... columns);

	/**
	 * Executes a query on the db
	 * @param query;
	 * @return result set
	 */
	ResultSet executeQuery(String query) throws SQLException;

	/**
	 * Executes an update on the db
	 * @param query;
	 * @return result set
	 */
	int executeUpdate(String query) throws SQLException;

	/**
	 * Gets the table from the table name
	 * @param tableName;
	 * @return String representation
	 */
	String getTable(String tableName);

	/**
	 * Returns if the handler is connected or not
	 * @return if connected
	 */
	boolean isConnected();
}
