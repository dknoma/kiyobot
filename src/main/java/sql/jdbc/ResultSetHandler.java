package sql.jdbc;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the information and queries from result sets
 * TODO: make an interface; need a class for each database
 * @author dk
 */
public enum ResultSetHandler {

	INSTANCE();

	private static final Gson gson = new Gson();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final String TODO = "Todo";
	private static final String TODO_ITEM = "TodoItem";
	private static final Logger LOGGER = LogManager.getLogger();

	ResultSetHandler(){

	}

	/**
	 * Gets the name of the reference table, its primary key name, and the id itself
	 * @param handler JDBCHandler
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @return json
	 */
	public static String getInfoFromReference(JDBCHandler handler, String referenceKey, int referenceId) {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from("todo",
								handler.where(referenceKey, referenceId, INTEGER, "")
						)
				)
		);
		return createReference(referenceResults);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @return json
	 */
	public static String getResultSet(JDBCHandler handler, String referenceKey, int referenceId) {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from("todo",
								handler.where(referenceKey, referenceId, INTEGER, "")
						)
				)
		);
		return getResults(referenceResults);
	}

	/**
	 * Prints out all results of the query
	 * @param handler JDBCHandler
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @return json
	 */
	public static String getResultSetWithReference(JDBCHandler handler, String referenceKey, int referenceId) {
		ResultSet results = handler.executeQuery(
				handler.select("*",
						handler.from("todoitem",
								handler.where(referenceKey, referenceId, INTEGER, "")
						)
				)
		);
		String out = getAllResults(referenceKey, referenceId, results);
		return out;
	}

	/**
	 * Creates json containing the name of the table, the name of the primarykey, and the id itself
	 * @param results query result set
	 * @return json
	 */
	private static String createReference(ResultSet results) {
		StringBuilder sb = new StringBuilder();
		try {
			while(results.next()) {
				sb.append("{");
				String tableName = results.getMetaData().getTableName(1);
				sb.append(String.format("\"name\":\"%s\",", tableName));
				String columnName = results.getMetaData().getColumnName(1);
				sb.append(String.format("\"primaryKey\":\"%s\",", columnName));
				sb.append(String.format("\"id\":%s", results.getInt(columnName)));
				sb.append("}");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set
	 * @param results query result set
	 * @return json
	 */
	private static String getResults(ResultSet results) {
		StringBuilder sb = new StringBuilder();
		try {
			while(results.next()) {
				sb.append("{");
				int resultCount = results.getMetaData().getColumnCount();
//				System.out.println(resultCount);
				for (int i = 1; i <= resultCount; i++) {
					String columnType = results.getMetaData().getColumnTypeName(i);
					String columnName = results.getMetaData().getColumnName(i);
					if(isString(columnType)) {
						sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, results.getString(columnName)));
					} else if(isInt(columnType)) {
						sb.append(String.format("\"%1$s\":%2$s", columnName, results.getInt(columnName)));
					} else if(isBoolean(columnType)) {
						sb.append(String.format("\"%1$s\":%2$s", columnName, results.getBoolean(columnName)));
					}
					if(i < resultCount) {
						sb.append(",");
					}
				}
				if(!results.isLast()) {
					sb.append("},");
				} else {
					sb.append("}");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @param results query result set
	 * @return json
	 */
	private static String getAllResults(String referenceKey, int referenceId, ResultSet results) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			sb.append(String.format("\"%1$s\":%2$s,", referenceKey, referenceId));
			// resultset starts BEFORE first row, need to call next for all results
			String tableName = results.getMetaData().getTableName(1);
			sb.append(String.format("\"%s\":[", tableName));
			while(results.next()) {
				sb.append("{");
				int resultCount = results.getMetaData().getColumnCount();
//				System.out.println(resultCount);
				for (int i = 1; i <= resultCount; i++) {
					String columnType = results.getMetaData().getColumnTypeName(i);
					String columnName = results.getMetaData().getColumnName(i);
//					System.out.println(String.format("i: %1$s, \ttype: %2$s",
//							results.getMetaData().getColumnName(i), columnType));
					if(isString(columnType)) {
						sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, results.getString(columnName)));
					} else if(isInt(columnType)) {
						sb.append(String.format("\"%1$s\":%2$s", columnName, results.getInt(columnName)));
					} else if(isBoolean(columnType)) {
						sb.append(String.format("\"%1$s\":%2$s", columnName, results.getBoolean(columnName)));
					}
					if(i < resultCount) {
						sb.append(",");
					}
				}
				if(!results.isLast()) {
					sb.append("},");
				} else {
					sb.append("}");
				}
			}
			sb.append("]}");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Returns whether or not the sql column type is a valid Java type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isString(String columnType) {
		return (columnType.equals("character") || columnType.equals("char") || columnType.equals("varchar")
				|| columnType.equals("longvarchar") || columnType.equals("text"));
	}

	/**
	 * Returns whether or not the sql column type is a valid Java Type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isInt(String columnType) {
		return (columnType.equals("tinyint") || columnType.equals("smallint") || columnType.equals("int4")
				|| columnType.equals("int") || columnType.equals("serial"));
	}

	/**
	 * Returns whether or not the sql column type is a valid Java Type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isBoolean(String columnType) {
		return (columnType.equals("bool") || columnType.equals("boolean"));
	}
}
