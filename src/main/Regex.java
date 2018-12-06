package main;

public class Regex {

	public static void main(String[] args) {
		String test = "%1$s/events/purchase/%2$d";
//		String o = String.format(test, "a", 2);
//		Object[] s = {"a", 2};
		test(test, "a", 2);
	}

	private static void test(String test, Object... params){
		String o = String.format(test, params);
		System.out.println(o);
	}
}
