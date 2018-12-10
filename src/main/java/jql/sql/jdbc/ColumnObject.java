package jql.sql.jdbc;

public class ColumnObject<T> {

	private String key;
	private T value;
	private Class classOfT;

	public ColumnObject(String key, T value) {
		this.key = key;
		this.value = value;
		this.classOfT = value.getClass();
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	public Class getClassOfT() {
		return this.classOfT;
	}

	public String toString() {
		return String.format("{\"key\": %1$s, \"value\": %2$s, \"classType\": %3$s", this.key, this.value, this.classOfT);
	}
}
