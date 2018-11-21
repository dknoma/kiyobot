package sql.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A model representing the structure and data in a SQL database
 *
 * Foreign key:
 * 	"table, <foreignid> int, FOREIGN KEY (<foreignid>) REFERENCES <foreigntable> (<foreignid>) ON DELETE CASCADE"
 */
public class MySQLModel {

	private String modelName;
	private String primaryKey;
	private String foreignModelName;
	private String foreignKey;
	private String query;
	private boolean autoIncrement;
	private List<String> columns;
	private Map<String, Class> columnType;
	private Map<String, Boolean> columnCanBeNull;
	private Map<String, Boolean> keyIsVar;
	private Map<String, Integer> keyLengths;
	private Map<String, Boolean> hasDefaultValue;
	private Map<String, Object> defaultValues;

	private static final String INT = "INT";
	private static final String BOOLEAN = "BOOLEAN";
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Regular model w/o references to any foreign tables
	 * @param modelName name of model
	 * @param autoIncrement if primary key auto increments
	 */
	public MySQLModel(String modelName, boolean autoIncrement) {
		this.modelName = modelName;
		this.primaryKey = String.format("%sid", this.modelName);
		this.foreignModelName = "";
		this.foreignKey = "";
		this.query = "";
		this.autoIncrement = autoIncrement;
		this.columns = new ArrayList<>();
		this.columnType = new HashMap<>();
		this.columnCanBeNull = new HashMap<>();
		this.keyIsVar = new HashMap<>();
		this.keyLengths = new HashMap<>();
		this.hasDefaultValue = new HashMap<>();
		this.defaultValues = new HashMap<>();
	}

	/**
	 * Regular model w/ references to a foreign table
	 * @param modelName name of model
	 * @param autoIncrement if primary key auto increments
	 * @param foreignTableName reference table
	 */
	public MySQLModel(String modelName, boolean autoIncrement, String foreignTableName) {
		this.modelName = modelName;
		this.primaryKey = String.format("%sid", this.modelName).toLowerCase();
		this.foreignModelName = foreignTableName;
		this.foreignKey = String.format("%sid", foreignTableName).toLowerCase();
		this.query = "";
		this.autoIncrement = autoIncrement;
		this.columns = new ArrayList<>();
		this.columnType = new HashMap<>();
		this.columnCanBeNull = new HashMap<>();
		this.keyIsVar = new HashMap<>();
		this.keyLengths = new HashMap<>();
		this.hasDefaultValue = new HashMap<>();
		this.defaultValues = new HashMap<>();
	}

	/**
	 * Adds a String key to the column to the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param  keyIsVar if string can have variable length
	 */
	public void addColumn(String key, boolean isNotNull, boolean keyIsVar, int keyLength,
						  boolean hasDefaultValue, Object defaultValue) {
		this.columns.add(key);
		this.columnType.put(key, String.class);
		this.columnCanBeNull.put(key, isNotNull);
		this.keyIsVar.put(key, keyIsVar);
		this.keyLengths.put(key, keyLength);
		this.hasDefaultValue.put(key, hasDefaultValue);
		this.defaultValues.put(key, defaultValue);
	}

	/**
	 * Adds a non-String key to the column of the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param classOfT class of the key
	 */
	public <T> void addColumn(String key, boolean isNotNull, Class<T> classOfT,
							  boolean hasDefaultValue, Object defaultValue) {
		this.columns.add(key);
		this.columnType.put(key, classOfT);
		this.columnCanBeNull.put(key, isNotNull);
		this.hasDefaultValue.put(key, hasDefaultValue);
		this.defaultValues.put(key, defaultValue);
	}

	/**
	 * Creates the query to create the table
	 */
	public void createTableQuery() {
		StringBuilder sb = new StringBuilder();
		if(this.autoIncrement) {
			sb.append(String.format("CREATE TABLE %1$s (%2$s AUTO_INCREMENT PRIMARY KEY", this.modelName, this.primaryKey));
		} else {
			sb.append(String.format("CREATE TABLE %1$s (%2$s PRIMARY KEY", this.modelName, this.primaryKey));
		}
		// adds the keys to the query
		for(String key : this.columns) {
			Class classOfKey = this.columnType.get(key);
			if(classOfKey.equals(String.class)) {
				if(this.keyIsVar.get(key)) {
					sb.append(String.format(", %1$s VARCHAR(%2$s)", key, this.keyLengths.get(key)));
				} else {
					sb.append(String.format(", %1$s CHAR(%2$s)", key, this.keyLengths.get(key)));
				}
				if(this.columnCanBeNull.get(key)) {
					sb.append(" NOT NULL");
				}
			} else if(classOfKey.equals(Integer.class)) {
				sb.append(String.format(", %1$s %2$s", key, INT));
				if(this.hasDefaultValue.get(key)) {
					sb.append(String.format(" DEFAULT %s", this.defaultValues.get(key)));
				}
			} else if(classOfKey.equals(Boolean.class)) {
				sb.append(String.format(", %1$s %2$s", key, BOOLEAN));
				if(this.hasDefaultValue.get(key)) {
					sb.append(String.format(" DEFAULT %b", this.defaultValues.get(key)));
				}
			}
		}
		// if has foreign key, add to query
		if(this.foreignKey != null && !this.foreignKey.isEmpty()) {
			sb.append(String.format("%1$s int, FOREIGN KEY (%1$s) REFERENCES %2$s (%1$s) ON DELETE CASCADE",
					this.foreignKey, this.foreignModelName));
		}
		sb.append(")");
		this.query = sb.toString();
	}

	public String getQuery() {
		System.out.println(this.query);
		return this.query;
	}

	public String getModelName() {
		return modelName;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public String getForeignModelName() {
		return foreignModelName;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public void newQuery() {
		this.query = "";
	}

	public void select() {
		this.query = String.format("%sSELECT", this.query);
	}

//	public SQLModel deepCopy() {
//		MySQLModel model = new MySQLModel(this.modelName, this.autoIncrement);
//		model.query = this.query;
//		model.foreignKey = this.foreignKey;
//		model.foreignModelName = this.foreignModelName;
//		model.columnCanBeNull = this.columnCanBeNull;
//		model.columns.addAll(this.columns);
//		model.columnCanBeNull.putAll(this.columnCanBeNull);
//		model.columnType.putAll(this.columnType);
//		model.keyIsVar.putAll(this.keyIsVar);
//		model.keyLengths.putAll(this.keyLengths);
//		model.hasDefaultValue.putAll(this.hasDefaultValue);
//		model.defaultValues.putAll(this.defaultValues);
//		return model;
//	}

	/**
	 * Returns a String representation of this model.
	 * @return a String
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("{\"name\": %1$s, \"keys\": [", this.modelName));
		int i = 0;
		for(String key : this.columns) {
			sb.append(String.format("\"%1$s\"", key));
			if(i < this.columns.size() - 1) {
				sb.append(", ");
				i++;
			}
		}
		sb.append("]");
		if(!this.foreignKey.isEmpty()) {
			sb.append(String.format(", \"reference\": %1$s", this.foreignModelName));
			sb.append(String.format(", \"referenceid\": %1$s", this.foreignKey));
		}
		sb.append("}");
		return sb.toString();
	}
}
