package jql.sql.model;

/**
 * A model representing the structure and data in a SQL database.
 * Supports key types: STRING, INTEGER, BOOLEAN
 */
 public interface SQLModel {

	/**
	 * Adds a String key to the column to the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param  keyIsVar if string can have variable length
	 */
	 void addColumn(String key, boolean isUnique, boolean isNotNull, boolean keyIsVar,
						  int keyLength, boolean hasDefaultValue, Object defaultValue);

	/**
	 * Adds a non-String key to the column of the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param classOfT class of the key
	 */
	 <T> void addColumn(String key, boolean isUnique, boolean isNotNull, Class<T> classOfT,
							  boolean hasDefaultValue, Object defaultValue);

	/**
	 * Creates the query to create the table
	 */
	 void createTableQuery();

	/**
	 * Gets the model's query
	 * @return name
	 */
	 String getQuery();

	/**
	 * Gets the model's name
	 * @return name
	 */
	 String getModelName();

	/**
	 * Gets the model's primary key
	 * @return name
	 */
	 String getPrimaryKey();

	/**
	 * Gets the model's reference model name
	 * @return name
	 */
	 String getForeignModelName();

	/**
	 * Gets the model's reference model primary key
	 * @return name
	 */
	 String getForeignKey();

	/**
	 * Makes a deep copy of this model
	 * @return deep copy
	 */
	 SQLModel deepCopy();

	/**
	 * Returns a String representation of this model.
	 * @return a String
	 */
	@Override
	 String toString();
}
