package sql.jdbc;

public class ColumnObject<T> {

	private String key;
	private Object value;
	private Class<T> classOfT;

	public ColumnObject(String key, Object value, Class<T> classOfT) {
		this.key = key;
		this.value = value;
		this.classOfT = classOfT;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public Class<T> getClassOfT() {
		return classOfT;
	}

	public String toString() {
		return String.format("{\"key\": %1$s, \"value\": %2$s, \"classType\": %3$s", this.key, this.value, this.classOfT);
	}
}
