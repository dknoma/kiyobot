package jql.sql.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the information and queries from result sets
 * @author dk
 */
public enum SQLManager {

	INSTANCE();

	private static final Class<String> STRING = String.class;
	private static final Class<Integer> INTEGER = Integer.class;
	private static final Class<Boolean> BOOLEAN = Boolean.class;
	private static final Logger LOGGER = LogManager.getLogger();

	SQLManager() {

	}

	/**
	 * Inserts specified data into the desired table.
	 * @param handler;
	 * @param tableName;
	 * @param columns;
	 * @throws SQLException;
	 */
	public void insertIntoTable(JDBCHandler handler, String tableName, ColumnObject... columns) throws SQLException {
		handler.executeUpdate(handler.insert(tableName, columns));
	}

	/**
	 * Updates the columns of a specified key/value pair
	 * @param handler;
	 * @param tableName;
	 * @param whereKey;
	 * @param whereValue;
	 * @param setColumns;
	 * @param <T>;
	 * @throws SQLException;
	 */
	public <T> void updateWhere(JDBCHandler handler, String tableName, String whereKey, T whereValue, ColumnObject... setColumns) throws SQLException {
		handler.executeUpdate(handler.update(tableName,
				handler.set(handler.where(whereKey, whereValue, ""), setColumns)
				)
		);
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler JDBCHandler
	 * @param what JDBCHandler
	 * @param location JDBCHandler
	 * @param key key
	 * @param value value
	 * @return json
	 */
	public <T> ResultSet getResultSet(JDBCHandler handler, String what, String location, String key, T value) throws SQLException {
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
	 * @param what JDBCHandler
	 * @param location JDBCHandler
	 * @return json
	 */
	public ResultSet getResultSet(JDBCHandler handler, String what, String location, ColumnObject... columns) throws SQLException {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(ColumnObject column : columns) {
			String key = column.getKey();
			Object value = column.getValue();
			if(i == 0) {
				sb.append(handler.where(key, value, ""));
			} else {
				sb.append(handler.and(key, value, ""));
			}
			i++;
		}
		String whereQuery = sb.toString();
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
	public ResultSet getResultSet(JDBCHandler handler, String what, String location) throws SQLException {
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
	public <T> ResultSet getDesiredColumns(JDBCHandler handler, String location, String key, T value,
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
	 * Returns a json string representation of the data in the result set w/ reference table data
	 * @param results query result set
	 * @return json
	 */
	public String allResultsToString(ResultSet results) {
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
				for (int i = 1; i <= resultCount; i++) {
					String columnType = results.getMetaData().getColumnTypeName(i);
					String columnName = results.getMetaData().getColumnName(i);
					if(i == resultCount && columnName.equals(primaryKey)) {
						LOGGER.info("Duplicate primary key found. No need to put reference key.");
						int lastIndex = sb.toString().length()-1;
						sb.delete(lastIndex, lastIndex+1);
						break;
					}
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
	 * Returns a json string representation of the data in the result set
	 * @param handler JDBCHandler
	 * @param what JDBCHandler
	 * @param location JDBCHandler
	 * @param key key
	 * @param value value
	 * @return json
	 */
	public <T> String resultsToString(JDBCHandler handler, String what, String location, String key, T value) throws SQLException {
		ResultSet results = getResultSet(handler, what, location, key, value);
		if(results == null) {
			return "{}";
		}
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
	 * Returns a json string representation of the data in the result set
	 * @param results query result set
	 * @return json
	 */
	public String resultsToString(ResultSet results) {
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
	 * Returns a json string representation of the last row in the result set
	 * @param handler JDBCHandler
	 * @param what;
	 * @param location;
	 * @return json
	 */
	public String lastResultToString(JDBCHandler handler, String what, String location) throws SQLException {
		ResultSet results = getResultSet(handler, what, location);
		StringBuilder sb = new StringBuilder();
		try {
			int numRows = 0;
			if(results.isBeforeFirst()) {
				results.last();
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
		LOGGER.debug(sb.toString());
		return sb.toString();
	}

	/**
	 * Returns a json string representation of the last row in the result set
	 * @param handler JDBCHandler
	 * @param what;
	 * @param location;
	 * @param key;
	 * @param value;
	 * @return json
	 */
	public <T> String lastResultToString(JDBCHandler handler, String what, String location, String key, T value) throws SQLException {
		ResultSet results = getResultSet(handler, what, location, key, value);
		StringBuilder sb = new StringBuilder();
		try {
			int numRows = 0;
			if(results.isBeforeFirst()) {
				results.last();
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
	public ResultSet selectItemFromInnerJoinOn(JDBCHandler handler, String[] selects, String leftJoin, String rightJoin,
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
	public String getResultsIncluding(ResultSet outerResult, ResultSet innerResult) {
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
	public String getDesiredResultsIncluding(ResultSet outerResult, ResultSet innerResult, String excludeKey) {
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
	public String getResultsIncluding(ResultSet outerResult, ResultSet innerResult, String excludeKey) {
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
	 * @param handler;
	 * @param outerTable;
	 * @param outerKey;
	 * @param outerValue;
	 * @param outerTableParam;
	 * @param outerTableParamId;
	 * @param columns;
	 * @param excludeKey;
	 * @param innerColumns;
	 * @return json
	 */
	public <T> String getResultsIncluding(JDBCHandler handler, String outerTable, String outerKey, T outerValue,
										  String outerTableParam, String outerTableParamId, String[] columns,
										  String excludeKey, ColumnObject... innerColumns) throws SQLException {
		ResultSet outerResult = getDesiredColumns(handler, outerTable, outerKey, outerValue, columns);
		// Checks if user exists
		if(!outerResult.isBeforeFirst()) {
			// ResultSet must be empty, therefore no data is found and should immediately return
			return "";
		}
		String[] select = new String[1];
		select[0] = String.format("%1$s.%2$s", outerTableParam, outerTableParamId);
		// Selects <select items> from the results of an inner join on USER and TICKET on the specified column objects
		ResultSet innerResult = selectItemFromInnerJoinOn(handler, select, outerTable, outerTableParam, innerColumns);
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
	 * Gets a single json object  w/o any references
	 * @param handler;
	 * @param what;
	 * @param location;
	 * @return json
	 */
	public String getList(JDBCHandler handler, String what, String location) throws SQLException {
		ResultSet result = getResultSet(handler, what, location);
		StringBuilder sb = new StringBuilder();
		while(result.next()) {
			sb.append(resultSetToJson(result));
		}
		return String.format("[%s]", sb.toString());
	}

	/**
	 * Gets a single json object  w/o any references
	 * @param handler;
	 * @param what;
	 * @param location;
	 * @return json
	 */
	public String getList(JDBCHandler handler, String what, String location, ColumnObject... columns) throws SQLException {
		ResultSet result = getResultSet(handler, what, location, columns);
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
	private String resultSetToRawJson(ResultSet result) throws SQLException {
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
	private String resultSetToRawJsonExcluding(ResultSet result, String excludeKey) throws SQLException {
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
			sb.append(",");
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
	private String resultSetToJson(ResultSet result) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
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
	 * Returns whether or not a list is empty
	 * @param list;
	 * @return if list is empty
	 */
	public boolean isListEmpty(String list) {
		return list.equals("[{}]") || list.equals("[]") || list.equals("");
	}

	/**
	 * Method meant to check if a String representation of a resultset output is empty
	 * @param result;
	 * @return isempty
	 */
	public boolean resultIsNotEmpty(String result) {
		return result != null && !result.isEmpty() && !result.equals("{}") && !result.equals("[]") && !result.equals("{[]}");
	}

	/**
	 * Returns whether or not the sql column type is a valid Java type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private boolean isString(String columnType) {
		columnType = columnType.toLowerCase();
		return (columnType.equals("character") || columnType.equals("char") || columnType.equals("varchar")
				|| columnType.equals("longvarchar") || columnType.equals("text"));
	}

	/**
	 * Returns whether or not the sql column type is a valid Java Type
	 * @param columnType sql Type
	 * @return valid Java type
	 */
	private boolean isInt(String columnType) {
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
	private boolean isBoolean(String columnType) {
		columnType = columnType.toLowerCase();
		return (columnType.equals("bool") || columnType.equals("boolean"));
	}
}
