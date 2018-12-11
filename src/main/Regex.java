package main;

public class Regex {

	public static void main(String[] args) {
		int x = 15;
		int result = getReconnectDelay(x);
		System.out.println(result);
//		String test = "%1$s/events/purchase/%2$d";
////		String o = String.format(test, "a", 2);
////		Object[] s = {"a", 2};
//		test(test, "a", 2);
	}

	private static int getReconnectDelay(int x) {
		return (int) Math.floor(2*x - (4*x/(Math.log(x) + 2)));
	}

	private static void test(String test, Object... params){
		String o = String.format(test, params);
		System.out.println(o);
	}
}
