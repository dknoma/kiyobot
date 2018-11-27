package sql.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.model.SQLModel;

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
	@Override
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
		if(value.getClass().equals(STRING)) {
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
		if(value.getClass().equals(STRING)) {
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
	 * @param classOfT Class type of query
	 * @param query rest of query
	 * @return query
	 */
	@Override
	public <T> String on(String key, Object value, Class<T> classOfT, String query) {
		if (classOfT.equals(STRING)) {
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
		try {
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
			for (int i = 0; i < columns.length; i++) {
				sb.append("?");
				if (i < columns.length - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");
			PreparedStatement stmt = this.dbConn.prepareStatement(sb.toString());
			for(int i = 0; i < columns.length; i++) {
				if (columns[i].getClassOfT().equals(STRING)) {
					stmt.setString(i+1, (String) columns[i].getValue());
				} else if (columns[i].getClassOfT().equals(INTEGER)) {
					stmt.setInt(i+1, (int) columns[i].getValue());
				} else if (columns[i].getClassOfT().equals(BOOLEAN)) {
					stmt.setBoolean(i+1, (boolean) columns[i].getValue());
				}
			}
			LOGGER.debug(stmt.toString());
			return stmt.toString();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return null;
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
			PreparedStatement stmt = this.dbConn.prepareStatement(query);

			LOGGER.debug("debugging PostgresHandler: {}", stmt.toString());

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
			//create a statement object
			PreparedStatement stmt = this.dbConn.prepareStatement(query);
			//execute a query, which returns a ResultSet object
			return stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
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
	 * Sets the value of a prepared statement at the given index
	 * @param statement;
	 * @param index;
	 * @param value;
	 * @param classOfT;
	 * @param <T>;
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
