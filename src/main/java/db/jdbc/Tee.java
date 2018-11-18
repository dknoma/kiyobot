package db.jdbc;

public class Tee {
	private static Tee ourInstance = new Tee();

	public static Tee getInstance() {
		return ourInstance;
	}

	private Tee() {
	}
}
