package sql.model;

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
	public void addColumn(String key, boolean isNotNull, boolean keyIsVar,
						  int keyLength, boolean hasDefaultValue, Object defaultValue);

	/**
	 * Adds a non-String key to the column of the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param classOfT class of the key
	 */
	public <T> void addColumn(String key, boolean isNotNull, Class<T> classOfT,
							  boolean hasDefaultValue, Object defaultValue);

	/**
	 * Creates the query to create the table
	 */
	public void createTableQuery();

	public String getQuery();

	public String getModelName();

	public String getPrimaryKey();

	public String getForeignModelName();

	public String getForeignKey();

	public void newQuery();

	public void select();

	public SQLModel deepCopy();

	/**
	 * Returns a String representation of this model.
	 * @return a String
	 */
	@Override
	public String toString();
}
