package jql.sql.jdbc;

public class ColumnObject<T> {

	private String key;
	private T value;
	private Class classOfValue;

	public ColumnObject(String key, T value) {
		this.key = key;
		this.value = value;
		this.classOfValue = value.getClass();
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	public Class getClassOfValue() {
		return this.classOfValue;
	}

	public String toString() {
		if(this.classOfValue.equals(String.class)) {
			return String.format("{\"key\": %1$s, \"value\": \"%2$s\", \"classtype\": \"%3$s\"", this.key, this.value, this.classOfValue);
		} else {
			return String.format("{\"key\": %1$s, \"value\": %2$s, \"classtype\": \"%3$s\"", this.key, this.value, this.classOfValue);
		}
	}
}
