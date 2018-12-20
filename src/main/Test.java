package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String addExgfxRegex = "!addexgfx (\\p{XDigit}+?) (\"(\\w+|\\s*|\\W*)+\") (\"(\\w+|\\s*?|\\W*)+\") (\\w+?) ((\\w+|\\W*)+)";
		String getExgfxRegex = "!getexgfx (\\p{XDigit}+?)";
		String getAllExgfxRegex = "!getallexgfx";
//		String command = "!getexgfx 1A3";
		String command = "!addexgfx FCA \"This is a, de? scription\" \"mountain, snow\" true imgur.linkto.img";
		Matcher matcher = Pattern.compile(addExgfxRegex).matcher(command);
//		if(matcher.matches()) {
//			for(int i = 1; i <= matcher.groupCount(); i++) {
//				String member = matcher.group(i);
//				System.out.println(String.format("%d = \"%s\"", i, member));
//			}
//		}
		if((matcher = Pattern.compile(addExgfxRegex).matcher(command)).matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				if(!member.isEmpty()) {
					System.out.println(String.format("%d = \"%s\"", i, member));
				}
			}
		} else if((matcher = Pattern.compile(getExgfxRegex).matcher(command)).matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				if(!member.isEmpty()) {
					System.out.println(String.format("%d = \"%s\"", i, member));
				}
			}
		} else if((matcher = Pattern.compile(getAllExgfxRegex).matcher(command)).matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				if(!member.isEmpty()) {
					System.out.println(String.format("%d = \"%s\"", i, member));
				}
			}
		}
	}


}
