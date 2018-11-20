package db.jdbc;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A model representing the structure and data in a SQL database
 *
 * Foreign key:
 * 	"table, <foreignid> int, FOREIGN KEY (<foreignid>) REFERENCES <foreigntable> (<foreignid>) ON DELETE CASCADE"
 */
public class SQLModel {

	private String modelName;
	private String primaryKey;
	private String foreignKey;
	private String query;
	private List<String> columns;
	private Map<String, Class> columnType;
	private Map<String, Boolean> columnCanBeNull;

	private static final String STRING = "string";
	private static final String INT = "int";
	private static final String BOOLEAN = "boolean";

	public SQLModel(String modelName) {
		this.modelName = modelName;
		this.primaryKey = String.format("%sid", this.modelName);
		this.foreignKey = "";
		this.query = "";
		this.foreignKey = "";
		this.columns = new ArrayList<>();
		this.columnType = new HashMap<>();
		this.columnCanBeNull = new HashMap<>();
	}

	public SQLModel(String modelName, SQLModel foreignTable) {
		this.modelName = modelName;
		this.primaryKey = String.format("%sid", this.modelName).toLowerCase();
		this.query = "";
		this.foreignKey = foreignTable.primaryKey;
		this.columns = new ArrayList<>();
		this.columnType = new HashMap<>();
		this.columnCanBeNull = new HashMap<>();
	}

	/**
	 * Adds a column to the table model
	 * @param key key
	 * @param isNotNull
	 */
	public <T> void addColumn(String key, Class<T> classOfT, boolean isNotNull) {
		this.columns.add(key);
		System.out.println("T: " + classOfT);
		this.columnType.put(key, classOfT);
		this.columnCanBeNull.put(key, isNotNull);
	}

	public void createTable(boolean autoIncrement) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("CREATE TABLE %1$s (%2$s SERIAL PRIMARY KEY", this.modelName, this.primaryKey));
		for(String key : this.columns) {
			Class classOfKey = this.columnType.get(key);
			if(classOfKey.equals(String.class)) {
				sb.append(String.format(", %1$s", key));
			} else if(classOfKey.equals(Integer.class)) {

			} else if(classOfKey.equals(Boolean.class)) {

			}
		}
//		sb.append("create table ");
//		sb.append(tableName).append(" ");
//		sb.append("(").append(primaryKey);
//		if(autoIncrement) {
//			sb.append(" SERIAL PRIMARY KEY");
//		}
	}

	public void newQuery() {
		this.query = "";
	}

	public void select() {
		this.query = String.format("%sSELECT", this.query);
	}
}
