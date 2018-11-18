package db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JDBCHandler {

	/**
	 * Sets up the connection for JDBC to the database
	 * @param db db
	 * @param host host
	 * @param port port
	 * @param username un
	 * @param password pw
	 */
	public void setConnection(String db, String host, String port, String username, String password);

	/**
	 * Sets up the initial table
	 * @param tableName
	 * @throws SQLException
	 */
	public void setupTable(String tableName, boolean autoIncrement) throws SQLException;

	/**
	 * Adds a key of type string to the table
	 * @param tableName
	 * @param key
	 * @param isVarchar
	 * @param chars
	 * @param notNull
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
	 * @param tableName
	 * @throws SQLException
	 */
	public void createTable(String tableName) throws SQLException;

	/**
	 * Adds primary key to the table
	 * @param tableName name of table
	 */
	public void closeTable(String tableName);

	public String getTable(String tableName);

	public Connection getConnection();

	public ResultSet select(String table);

	public void insertString(String table, String key, String value);

	public void insertString(String table, String key, String value, String foreignTable, String type, String typeValue);
}
