package main;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Regex {

	public static void main(String[] args) {
		try {
			URL userService = new URL("http://mcvm064.cs.usfca.edu:7070/api/events/ 7");
			HttpURLConnection connection = (HttpURLConnection) userService.openConnection();
			System.out.println(userService);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
//		String[] split = "   9".split("  {2}");
//		for(String s : split) {
//			System.out.println("s: " + s);
//		}
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
