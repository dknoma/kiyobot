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
	 * Sets up the initial table
	 * @param tableName name
	 * @throws SQLException s
	 */
	public void setupTable(String tableName, boolean autoIncrement) throws SQLException;

	/**
	 * Adds a key of type string to the table
	 * @param tableName name
	 * @param key key
	 * @param isVarchar uses varchar or not
	 * @param chars # chars
	 * @param notNull can be null or not
	 */
	public void addStringKey(String tableName, String key, boolean isVarchar, int chars, boolean notNull);

	/**
	 * Adds primary key to the table
	 * @param tableName name of table
	 */
	public void addPrimaryKey(String tableName);

	/**
	 * Updates a table to have a foreign key if it
	 * @param tableName name of table
	 * @param referenceTableName name of reference table
	 */
	public void addForeignKey(String tableName, String referenceTableName);

	/**
	 * Actually creates and puts it into the database
	 * @param tableName name
	 * @throws SQLException s
	 */
	public void createTable(String tableName) throws SQLException;

	/**
	 * Adds primary key to the table
	 * @param tableName name of table
	 */
	public void closeTable(String tableName);

	public String getTable(String tableName);

	/**
	 * Gets the JDBC connection
	 * @return connection
	 */
	public Connection getConnection();

	/**
	 * Regular select from table
	 * @param table name
	 * @return resultset of query
	 */
	public ResultSet select(String table, String column);

	/**
	 * Inserts a string value into column key of a table
	 * @param table name
	 * @param key key
	 * @param value value
	 */
	public void insertString(String table, String key, String value);

	/**
	 * Insert string with foreign key into column key of a table
	 * @param table name
	 * @param key key
	 * @param value value
	 * @param foreignTable reference table
	 * @param type reference key
	 * @param typeValue reference name
	 */
	public void insertString(String table, String key, String value, String foreignTable, String type, String typeValue);
}
