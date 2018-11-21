package sql.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A model representing the structure and data in a SQL database
 *
 * Foreign key:
 * 	"table, <foreignid> int, FOREIGN KEY (<foreignid>) REFERENCES <foreigntable> (<foreignid>) ON DELETE CASCADE"
 */
public interface SQLModel {

	/**
	 * Adds a String key to the column to the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param  keyIsVar if string can have variable length
	 */
	public void addColumn(String key, boolean isNotNull, boolean keyIsVar, int keyLength);

	/**
	 * Adds a non-String key to the column of the table
	 * @param key key
	 * @param isNotNull if key can be null
	 * @param classOfT class of the key
	 */
	public <T> void addColumn(String key, boolean isNotNull, Class<T> classOfT);

	/**
	 * Creates the query to create the table
	 */
	public void createTableQuery();

	public void getQuery();

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
