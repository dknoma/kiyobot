package main;

public class Regex {

	public static void main(String[] args) {
		String page = "<bob></bob><b></ada></>";
		String REGEX = "(?is)<b*?>";
		String newPage = page.replaceAll(REGEX, "");
		System.out.println(newPage);
	}
}
