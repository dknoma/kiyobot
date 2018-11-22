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

	public String getTable(String tableName);

	public String select(String value, String query);

	public String from(String location, String query);

	public <T> String where(String key, Object value, Class<T> typeOf, String query);

	public <T> String and(String key, Object value, Class<T> typeOf, String query);

	public <T> String insert(String tableName, ColumnObject<?>... columns);

//	public <T> String insert(String tableName, String column, Object value, Class<T> classOfT);
//
//	public <S, T> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
//								  String column2, Object value2, Class<T> classOf2);
//
//	public <S, T, U> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
//								   String column2, Object value2, Class<T> classOf2,
//								   String column3, Object value3, Class<U> classOf3);
//
//	public <S, T, U, V> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
//									  String column2, Object value2, Class<T> classOf2,
//									  String column3, Object value3, Class<U> classOf3,
//									  String column4, Object value4, Class<V> classOf4);
//
//	public <S, T, U, V, W> String insert(String tableName, String column1, Object value1, Class<S> classOf1,
//									  String column2, Object value2, Class<T> classOf2,
//									  String column3, Object value3, Class<U> classOf3,
//									  String column4, Object value4, Class<V> classOf4,
//									  String column5, Object value5, Class<W> classOf5);

	public String openParentheses(String outerQuery, String innerQuery);

	public String closeParentheses(String outerQuery, String innerQuery);

	public ResultSet executeQuery(String query);

	public int executeUpdate(String query);
}
