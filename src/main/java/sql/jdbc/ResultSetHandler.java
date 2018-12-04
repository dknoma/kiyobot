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
	public static String getInfoFromReference(JDBCHandler handler, String what, String referenceLocation,
											  String referenceKey, int referenceId) throws SQLException {
		ResultSet referenceResults = handler.executeQuery(
				handler.select(what,
						handler.from(referenceLocation,
								handler.where(referenceKey, referenceId, "")
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
	public static <T> ResultSet getResultSet(JDBCHandler handler, String what, String location, String key, T value) throws SQLException {
		String whereQuery;
		if(value.getClass().equals(STRING)) {
			whereQuery = handler.where(key, value, "");
		} else if(value.getClass().equals(INTEGER)) {
			whereQuery = handler.where(key, value, "");
		} else if(value.getClass().equals(BOOLEAN)) {
			whereQuery = handler.where(key, value, "");
		} else {
			whereQuery = "";
		}
		return handler.executeQuery(
				handler.select(what,
						handler.from(location,
								whereQuery
						)
				)
		);
	}


	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param what;
	 * @param location;
	 * @return json
	 */
	public static <T> ResultSet getResultSet(JDBCHandler handler, String what, String location) throws SQLException {
		return handler.executeQuery(
				handler.select(what,
						handler.from(location,
								""
						)
				)
		);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param key key
	 * @param value value
	 * @return json
	 */
	public static <T> ResultSet getDesiredColumns(JDBCHandler handler, String location, String key, T value,
												   String... columns) throws SQLException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < columns.length; i++) {
			sb.append(columns[i]);
			sb.append(",");
		}
		sb.deleteCharAt(sb.toString().length()-1);
		return handler.executeQuery(
				handler.select(sb.toString(),
						handler.from(location,
								handler.where(key, value, "")
						)
				)
		);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param key key
	 * @param value value
	 * @return json
	 */
	public static <T> String resultSetToString(JDBCHandler handler, String what, String location, String key, T value) throws SQLException {
		String whereQuery;
			if(value.getClass().equals(STRING)) {
				whereQuery = handler.where(key, value, "");
			} else if(value.getClass().equals(INTEGER)) {
				whereQuery = handler.where(key, value, "");
			} else if(value.getClass().equals(BOOLEAN)) {
				whereQuery = handler.where(key, value, "");
			} else {
				whereQuery = "";
			}
		ResultSet referenceResults = handler.executeQuery(
				handler.select(what,
						handler.from(location,
								whereQuery
						)
				)
		);
		return resultsToString(referenceResults);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @return json
	 */
	public static String findAll(JDBCHandler handler, String location) throws SQLException {
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from(location,""
						)
				)
		);
		return allResultsToString(referenceResults);
	}


	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @return json
	 */
	public static String findAll(JDBCHandler handler, String location, ColumnObject... where) throws SQLException {
		StringBuilder whereBuilder = new StringBuilder();
		String whereQuery;
		if(where[0].getClassOfT().equals(STRING)) {
			whereBuilder.append(handler.where(where[0].getKey(), where[0].getValue(), ""));
		} else if(where[0].getClassOfT().equals(INTEGER)) {
			whereBuilder.append(handler.where(where[0].getKey(), where[0].getValue(), ""));
		} else if(where[0].getClassOfT().equals(BOOLEAN)) {
			whereBuilder.append(handler.where(where[0].getKey(), where[0].getValue(), ""));
		} else {
			whereBuilder.append("");
		}
		for(int i = 1; i < where.length; i++) {
			if(where[i] == null) {
				break;
			}
			if(where[i].getClassOfT().equals(STRING)) {
				whereBuilder.append(handler.and(where[i].getKey(), where[i].getValue(), ""));
			} else if(where[i].getClassOfT().equals(INTEGER)) {
				whereBuilder.append(handler.and(where[i].getKey(), where[i].getValue(), ""));
			} else if(where[i].getClassOfT().equals(BOOLEAN)) {
				whereBuilder.append(handler.and(where[i].getKey(), where[i].getValue(), ""));
			} else {
				whereBuilder.append("");
			}
		}
		whereQuery = whereBuilder.toString();
//		System.out.println(whereQuery);
		ResultSet referenceResults = handler.executeQuery(
				handler.select("*",
						handler.from(location, whereQuery
						)
				)
		);
		return allResultsToString(referenceResults);
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param results query result set
	 * @return json
	 */
	public static String allResultsToString(ResultSet results) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			// resultset starts BEFORE first row, need to call next for all results
			String tableName = results.getMetaData().getTableName(1);
				sb.append(tableName.endsWith("x") ? String.format("\"%ses\":[", tableName)
						: String.format("\"%ss\":[", tableName));
			String primaryKey = results.getMetaData().getColumnName(1);
			while(results.next()) {
				sb.append("{");
				int resultCount = results.getMetaData().getColumnCount();
//				System.out.println(resultCount);
				for (int i = 1; i <= resultCount; i++) {
					String columnType = results.getMetaData().getColumnTypeName(i);
					String columnName = results.getMetaData().getColumnName(i);
					if(i == resultCount && columnName.equals(primaryKey)) {
						LOGGER.info("Duplicate primary key found. No need to put reference key.");
						int lastIndex = sb.toString().length()-1;
						sb.delete(lastIndex, lastIndex+1);
						break;
					}
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
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
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
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set
	 * @param results query result set
	 * @return json
	 */
	public static String resultsToString(ResultSet results) {
		StringBuilder sb = new StringBuilder();
		try {
			int numRows = 0;
			while(results.next()) {
				numRows++;
				sb.append("{");
				int resultCount = results.getMetaData().getColumnCount();
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
			if(numRows == 0) {
				return "{}";
			}
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Selects an item from an inner join of two tables on the specified comparisons
	 * @param handler;
	 * @param selects;
	 * @param leftJoin;
	 * @param rightJoin;
	 * @param comparisons;
	 * @return result set
	 * @throws SQLException
	 */
	public static ResultSet selectItemFromInnerJoinOn(JDBCHandler handler, String[] selects, String leftJoin, String rightJoin,
													  ColumnObject... comparisons) throws SQLException {
		StringBuilder sb = new StringBuilder(),
				sel = new StringBuilder();
		for(int i = 1; i < comparisons.length; i++) {
			sb.append(handler.and(comparisons[i].getKey(), comparisons[i].getValue(), ""));
		}
		for(int i = 0; i < selects.length; i++){
			sel.append(selects[i]);
			sel.append(",");
		}
		sel.deleteCharAt(sel.toString().length()-1);
		String select = sel.toString();
		String andQueries = sb.toString();
		// SELECT *
		String query = handler.select(select,
				// FROM ((
				handler.from(
						handler.openParentheses(
								handler.openParentheses(
										// * INNER JOIN *
										handler.innerJoin(leftJoin, rightJoin,
												// ON (*=* AND *=* AND ...)))
												handler.on(
														handler.openParentheses(comparisons[0].getKey()), comparisons[0].getValue(),
														andQueries +
																handler.closeParentheses(
																		handler.closeParentheses(
																				handler.closeParentheses("")
																		)
																)
												)
										)
								)
						)
						, "")
		);
		LOGGER.debug(query);
		return handler.executeQuery(query);
		// SELECT * FROM ((* INNER JOIN * ON (*=* AND *=* AND...)))
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param outerResult query result set
	 * @param innerResult query result set
	 * @return json
	 */
	public static String getResultsIncluding(ResultSet outerResult, ResultSet innerResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			while(outerResult.next()) {
				sb.append(resultSetToRawJson(outerResult));
			}
			if(!sb.toString().endsWith(",")) {
				sb.append(",");
			}
			String innerTableName = innerResult.getMetaData().getTableName(1);
			sb.append((innerTableName.endsWith("x") || innerTableName.endsWith("s")) ? String.format("\"%1$ses\":[", innerTableName)
					: String.format("\"%1$ss\":[", innerTableName));
			while(innerResult.next()) {
				sb.append(resultSetToJson(innerResult));
			}
			sb.append("]}");
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param outerResult query result set
	 * @param innerResult query result set
	 * @return json
	 */
	public static String getDesiredResultsIncluding(ResultSet outerResult, ResultSet innerResult, String excludeKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			while(outerResult.next()) {
				sb.append(resultSetToRawJsonExcluding(outerResult, excludeKey));
			}
			if(!sb.toString().endsWith(",")) {
				sb.append(",");
			}
			String innerTableName = innerResult.getMetaData().getTableName(1);
			sb.append((innerTableName.endsWith("x") || innerTableName.endsWith("s")) ? String.format("\"%1$ses\":[", innerTableName)
					: String.format("\"%1$ss\":[", innerTableName));
			while(innerResult.next()) {
				sb.append(resultSetToJson(innerResult));
			}
			sb.append("]}");
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param outerResult query result set
	 * @param innerResult query result set
	 * @return json
	 */
	public static String getResultsIncluding(ResultSet outerResult, ResultSet innerResult, String excludeKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			while(outerResult.next()) {
				sb.append(resultSetToRawJsonExcluding(outerResult, excludeKey));
			}
			if(!sb.toString().endsWith(",") && !sb.toString().endsWith("{")) {
				sb.append(",");
			}
			String innerTableName = innerResult.getMetaData().getTableName(1);
			sb.append((innerTableName.endsWith("x") || innerTableName.endsWith("s")) ? String.format("\"%1$ses\":[", innerTableName)
					: String.format("\"%1$ss\":[", innerTableName));
			while(innerResult.next()) {
				sb.append(resultSetToJson(innerResult));
			}
			sb.append("]}");
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param outerResult query result set
	 * @return json
	 */
	public static String getResultsIncludingExternalResults(ResultSet outerResult, String externalName, String externalResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		try {
			while(outerResult.next()) {
				sb.append(resultSetToRawJson(outerResult));
			}
			if(!sb.toString().endsWith(",")) {
				sb.append(",");
			}
			sb.append(String.format("\"%s\":[", externalName));
			sb.append(externalResults);
			sb.append("]}");
		} catch (SQLException e) {
			LOGGER.error("A SQL error has occurred: {},\n{}", e.getMessage(), e.getStackTrace());
		}
		return sb.toString();
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param result;
	 * @return json
	 */
	public static String getListFromResultSet(ResultSet result) throws SQLException {
		StringBuilder sb = new StringBuilder();
		while(result.next()) {
			sb.append(resultSetToJson(result));
		}
		return String.format("[%s]", sb.toString());
	}

	/**
	 * Returns string representation of the information in a result set in json format without outer curly brackets
	 * @param result;
	 * @return string
	 * @throws SQLException;
	 */
	private static String resultSetToRawJson(ResultSet result) throws SQLException {
		StringBuilder sb = new StringBuilder();
		int resultCount = result.getMetaData().getColumnCount();
		for (int i = 1; i <= resultCount; i++) {
			String columnType = result.getMetaData().getColumnTypeName(i);
			String columnName = result.getMetaData().getColumnName(i);
			if(isString(columnType)) {
				sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, result.getString(columnName)));
			} else if(isInt(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getInt(columnName)));
			} else if(isBoolean(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getBoolean(columnName)));
			}
			if(i < resultCount || !result.isLast()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns string representation of the information in a result set in json format without outer curly brackets
	 * @param result;
	 * @return string
	 * @throws SQLException;
	 */
	private static String resultSetToRawJsonExcept(ResultSet result, String except) throws SQLException {
		StringBuilder sb = new StringBuilder();
		int resultCount = result.getMetaData().getColumnCount();
		for (int i = 1; i <= resultCount; i++) {
			String columnType = result.getMetaData().getColumnTypeName(i);
			String columnName = result.getMetaData().getColumnName(i);
			if(isString(columnType)) {
				sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, result.getString(columnName)));
			} else if(isInt(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getInt(columnName)));
			} else if(isBoolean(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getBoolean(columnName)));
			}
			if(i < resultCount || !result.isLast()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns string representation of the information in a result set in json format without outer curly brackets
	 * @param result;
	 * @return string
	 * @throws SQLException;
	 */
	private static String resultSetToRawJsonExcluding(ResultSet result, String excludeKey) throws SQLException {
		StringBuilder sb = new StringBuilder();
		int resultCount = result.getMetaData().getColumnCount();
		for (int i = 1; i <= resultCount; i++) {
			String columnType = result.getMetaData().getColumnTypeName(i);
			String columnName = result.getMetaData().getColumnName(i);
			if (columnName.equals(excludeKey)) {
				continue;
			} else {
				if (isString(columnType)) {
					sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, result.getString(columnName)));
				} else if (isInt(columnType)) {
					sb.append(String.format("\"%1$s\":%2$s", columnName, result.getInt(columnName)));
				} else if (isBoolean(columnType)) {
					sb.append(String.format("\"%1$s\":%2$s", columnName, result.getBoolean(columnName)));
				}
			}
//			if(i < resultCount || !result.isLast()) {
				sb.append(",");
//			}
		}
		sb.deleteCharAt(sb.toString().length()-1);
		return sb.toString();
	}

	/**
	 * Returns string representation of the information in a result set in json format
	 * @param result;
	 * @return string
	 * @throws SQLException;
	 */
	private static String resultSetToJson(ResultSet result) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int resultCount = result.getMetaData().getColumnCount();
//				System.out.println(resultCount);
		for (int i = 1; i <= resultCount; i++) {
			String columnType = result.getMetaData().getColumnTypeName(i);
			String columnName = result.getMetaData().getColumnName(i);
//					System.out.println(String.format("i: %1$s, \ttype: %2$s",
//							results.getMetaData().getColumnName(i), columnType));
			if(isString(columnType)) {
				sb.append(String.format("\"%1$s\":\"%2$s\"", columnName, result.getString(columnName)));
			} else if(isInt(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getInt(columnName)));
			} else if(isBoolean(columnType)) {
				sb.append(String.format("\"%1$s\":%2$s", columnName, result.getBoolean(columnName)));
			}
			if(i < resultCount) {
				sb.append(",");
			}
		}
		if(!result.isLast()) {
			sb.append("},");
		} else {
			sb.append("}");
		}
		LOGGER.debug(sb.toString());
		return sb.toString();
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
	 * Returns whether or not a list is empty
	 * @param list;
	 * @return if list is empty
	 */
	public static boolean isListEmpty(String list) {
		return list.equals("[{}]") || list.equals("[]") || list.equals("");
	}

	/**
	 * Returns whether or not the sql column type is a valid Java type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isString(String columnType) {
		columnType = columnType.toLowerCase();
		return (columnType.equals("character") || columnType.equals("char") || columnType.equals("varchar")
				|| columnType.equals("longvarchar") || columnType.equals("text"));
	}

	/**
	 * Returns whether or not the sql column type is a valid Java Type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isInt(String columnType) {
		columnType = columnType.toLowerCase();
		return (columnType.equals("tinyint") || columnType.equals("smallint") || columnType.equals("int4")
				|| columnType.equals("int") || columnType.equals("serial") || columnType.equals("BIGINT")
				|| columnType.equals("bigint unsigned"));
	}

	/**
	 * Returns whether or not the sql column type is a valid Java Type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private static boolean isBoolean(String columnType) {
		columnType = columnType.toLowerCase();
		return (columnType.equals("bool") || columnType.equals("boolean"));
	}
}
