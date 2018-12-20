package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String addExgfxRegex = "!addexgfx (\\p{XDigit}+?) (\".*\") (\".+\") (\\w+?) (.+)";
		String getExgfxRegex = "!getexgfx (\\p{XDigit}+?)";
		String getAllExgfxRegex = "!getallexgfx";
//		String command = "!getexgfx 1A3";
		String command = "!addexgfx 13B \"Snowy mountain tileset; Trees, snow bunnies, blocks, logs\" \"mountain, snow\" true https://i.imgur.com/7HEGup3.png";
		Matcher matcher;
		if((matcher = Pattern.compile(addExgfxRegex).matcher(command)).matches()) {
			System.out.println("command: " + matcher.group());
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				System.out.println(String.format("%d = \"%s\"", i, member));
			}
		} else if((matcher = Pattern.compile(getExgfxRegex).matcher(command)).matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				System.out.println(String.format("%d = \"%s\"", i, member));
			}
		} else if((matcher = Pattern.compile(getAllExgfxRegex).matcher(command)).matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				System.out.println(String.format("%d = \"%s\"", i, member));
			}
		}
	}


}
