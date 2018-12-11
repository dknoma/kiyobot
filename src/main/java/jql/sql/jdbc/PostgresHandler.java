package jql.sql.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jql.sql.model.SQLModel;

import java.sql.*;
import java.util.Map;

/**
 * A handler for PostgreSQL queries.
 *
 * NOTE: Postgres uses 'single quotes' for queries rather than "double quotes".
 *
 * @author dk
 */
public class PostgresHandler implements JDBCHandler {

	private boolean connected;
	private Connection dbConn;
	private Map<String, SQLModel> models;

	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final Logger LOGGER = LogManager.getLogger();

	public PostgresHandler(Map<String, SQLModel> models) {
		this.connected = false;
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
	@Override
	public void setConnection(String db, String host, String port, String username, String password) {
		try {
			// format: jdbc:postgresql://host:port/pathOrDatabaseName
			String dbURL = String.format("jdbc:postgresql://%1$s:%2$s/%3$s", host, port, db);
			this.dbConn = DriverManager.getConnection(dbURL, username, password);
			this.connected = true;
			LOGGER.info("Connected to sql successfully!");
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
			this.connected = false;
		}
	}

	/**
	 * Actually creates and puts it into the database
	 * @throws SQLException;
	 */
	@Override
	public void createTables() throws SQLException {
		PreparedStatement stmt = null;
		for(Map.Entry<String, SQLModel> entry : this.models.entrySet()) {
			SQLModel model = entry.getValue();
			try {
				stmt = dbConn.prepareStatement(model.getQuery());
				LOGGER.debug("TABLE: {}", stmt.toString());
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

	/**
	 * Gets the JDBC connection
	 * @return connection
	 */
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
	@Override
	public String select(String value, String query) {
		return String.format("SELECT %1$s%2$s", value, query);
	}

	/**
	 * Adds a FROM query to a string
	 * @param location location of search
	 * @param query rest of query
	 * @return query
	 */
	@Override
	public String from(String location, String query) {
		return String.format(" FROM %1$s%2$s", location, query);
	}

	/**
	 * Adds a WHERE query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	@Override
	public <T> String where(String key, T value, String query) {
		if (value.getClass().equals(STRING) && !value.toString().endsWith("id")) {
			return String.format(" WHERE %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" WHERE %1$s=%2$s%3$s", key, value, query);
		}
	}

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	@Override
	public <T> String and(String key, T value, String query) {
		if (value.getClass().equals(STRING) && !value.toString().endsWith("id")) {
			return String.format(" AND %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" AND %1$s=%2$s%3$s", key, value, query);
		}
	}

	/**
	 * Adds ( query to a string
	 * @param query;
	 * @return query
	 */
	@Override
	public String openParentheses(String query) {
		return String.format("(%s", query);
	}

	/**
	 * Adds ) query to a string
	 * @param query;
	 * @return query
	 */
	@Override
	public String closeParentheses(String query) {
		return String.format(")%s", query);
	}

	/**
	 * Adds ) query to a string
	 * @param table1 left side of query
	 * @param table2 right side of query
	 * @return query
	 */
	@Override
	public String innerJoin(String table1, String table2, String query) {
		return String.format("%1$s INNER JOIN %2$s%3$s", table1, table2, query);
	}

	/**
	 * Adds an AND query to a string
	 * @param value value
	 * @param query rest of query
	 * @return query
	 */
	@Override
	public <T> String on(String key, T value, String query) {
		if (value.getClass().equals(STRING) && !value.toString().endsWith("id")) {
			return String.format(" ON %1$s='%2$s'%3$s", key, value, query);
		} else {
			return String.format(" ON %1$s=%2$s%3$s", key, value, query);
		}
	}

	/**
	 * Insert values into the table
	 * @param tableName;
	 * @return this
	 */
	@Override
	public String insert(String tableName, ColumnObject... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("INSERT INTO %1$s (",
				this.models.get(tableName).getModelName()));
		for (int i = 0; i < columns.length; i++) {
			sb.append(columns[i].getKey());
			if (i < columns.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(") VALUES (");
		for(int i = 0; i < columns.length; i++) {
			if (columns[i].getClassOfValue().equals(STRING)) {
				sb.append(String.format("'%s'",columns[i].getValue().toString()));
			} else if (columns[i].getClassOfValue().equals(INTEGER)) {
				sb.append((int) columns[i].getValue());
			} else if (columns[i].getClassOfValue().equals(BOOLEAN)) {
				sb.append((boolean)columns[i].getValue());
			}
			if (i < columns.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Adds UPDATE query
	 * @param location target location of query
	 * @param query;
	 * @return query string
	 */
	@Override
	public String update(String location, String query) {
		return String.format("UPDATE %1$s%2$s", location, query);
	}

	/**
	 * Adds SET query
	 * @param query rest of query
	 * @param columns variable number of columns
	 * @return query string
	 */
	@Override
	public String set(String query, ColumnObject... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append(" SET ");
		for(ColumnObject column : columns) {
			if (column.getClassOfValue().equals(STRING) && !column.getValue().toString().endsWith("id")) {
				sb.append(String.format("%1$s='%2$s'", column.getKey(), column.getValue()));
			} else {
				sb.append(String.format("%1$s=%2$s", column.getKey(), column.getValue()));
			}
			sb.append(", ");
		}
		int lastColumnIndex = sb.toString().length() - 1;
		sb.delete(lastColumnIndex-1, lastColumnIndex+1);
		sb.append(query);
		return sb.toString();
	}

	/**
	 * Executes a query on the db
	 * @param query;
	 * @return result set
	 */
	@Override
	public ResultSet executeQuery(String query) {
		try {
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			LOGGER.trace("debugging PostgresHandler: {}", stmt.toString());
			//execute a query, which returns a ResultSet object
			return stmt.executeQuery();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	/**
	 * Executes an update on the db
	 * @param query;
	 * @return result set
	 */
	@Override
	public int executeUpdate(String query) {
		try {
			LOGGER.debug("update: {}", query);
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			//execute a query, which returns a ResultSet object
			return stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred when executing update: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return Integer.MIN_VALUE;
	}

	/**
	 * Gets the table from the table name
	 * @param tableName;
	 * @return String representation
	 */
	@Override
	public String getTable(String tableName) {
		return this.models.get(tableName).toString();
	}

	/**
	 * Returns if the handler is connected or not
	 * @return if connected
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}
}
