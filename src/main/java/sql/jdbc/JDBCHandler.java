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
	 * @throws SQLException s
	 */
	public void createTables() throws SQLException;

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
	 * @param type reference key
	 * @param typeValue reference name
	 */
	public void insertString(String table, String key, String value, String type, String typeValue);

	public String getQuery(String name);

	public void newQuery();

	public String getTable(String tableName);

	public JDBCHandler select(String value);

	public JDBCHandler from(String location);

	public <T> JDBCHandler where(String key, Object value, Class<T> typeOf);

	public <T> JDBCHandler insert(String tableName, String column, Object value, Class<T> classOfT);

	public <S, T> JDBCHandler insert(String tableName, String column1, Object value1, Class<S> classOf1,
								  String column2, Object value2, Class<T> classOf2);

	public <S, T, U> JDBCHandler insert(String tableName, String column1, Object value1, Class<S> classOf1,
								  String column2, Object value2, Class<T> classOf2,
								  String column3, Object value3, Class<U> classOf3);

	public ResultSet executeQuery();

	public int executeUpdate();
}
