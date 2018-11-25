package sql.jdbc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

	private static final Gson GSON = new Gson();
	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
//	private static final String TODO = "Todo";
//	private static final String TODO_ITEM = "TodoItem";
	private static final Logger LOGGER = LogManager.getLogger();

	ResultSetHandler() {

	}

	/**
	 * Gets the name of the reference table, its primary key name, and the id itself
	 * @param handler JDBCHandler
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @return json
	 */
	public static String getInfoFromReference(JDBCHandler handler, String referenceLocation,
											  String referenceKey, int referenceId) {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from(referenceLocation,
								handler.where(referenceKey, referenceId, INTEGER, "")
						)
				)
		);
		return createReference(referenceResults);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param key key
	 * @param value value
	 * @return json
	 */
	public static <T> String getResultSet(JDBCHandler handler, String location, String key, Object value, Class<T> classOfT) {
		String whereQuery;
			if(classOfT.equals(STRING)) {
				whereQuery = handler.where(key, value, STRING, "");
			} else if(classOfT.equals(INTEGER)) {
				whereQuery = handler.where(key, value, INTEGER, "");
			} else if(classOfT.equals(BOOLEAN)) {
				whereQuery = handler.where(key, value, BOOLEAN, "");
			} else {
				whereQuery = "";
			}
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from(location,
								whereQuery
						)
				)
		);
		return getResults(referenceResults);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @return json
	 */
	public static String getAllResults(JDBCHandler handler, String location) {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from(location,""
						)
				)
		);
		return getResults(referenceResults);
	}

	/**
	 * Gets a json string representation of all results of the query; This query is meant to be used to get
	 * all values from the table of a specific referenceid
	 * @param handler JDBCHandler
	 * @param referenceKey reference table primary key
	 * @param referenceId reference table id
	 * @return json
	 */
	public static String getResultSetWithReference(JDBCHandler handler, String location, String referenceKey, int referenceId) {
		ResultSet results = handler.executeQuery(
				handler.select("*",
						handler.from(location,
								handler.where(referenceKey, referenceId, INTEGER, "")
						)
				)
		);
		return getAllResults(referenceKey, referenceId, results);
	}

	/**
	 * Returns the JsonObject representation of the input json
	 * @param json input
	 * @return string
	 */
	public static JsonObject getInfoFromJson(String json) {
		return GSON.fromJson(json, JsonObject.class);
	}

	/**
	 * Returns the primary key from the json input
	 * @param json input
	 * @return string
	 */
	public static String getTableNameFromJson(String json) {
		return GSON.fromJson(json, JsonObject.class).get("primaryKey").getAsString();
	}

	/**
	 * Returns the table id from the input json
	 * @param json input
	 * @return string
	 */
	public static int getTableIdFromJson(String json) {
		return GSON.fromJson(json, JsonObject.class).get("id").getAsInt();
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
	public static String getResults(ResultSet results) {
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
	public static String getAllResults(String referenceKey, int referenceId, ResultSet results) {
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
