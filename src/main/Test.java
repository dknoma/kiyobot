package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String regex = "!addexgfx (\\p{XDigit}+?) (\"(\\w*?|\\s*?)*\") ((\\w*?|\\s*?)+) (\\w+?) ((\\w*?|\\W*?)+)";
		String command = "!addexgfx 114 \"This is a description\" mountain true imgur.linkto.img";
		Matcher matcher = Pattern.compile(regex).matcher(command);
		if(matcher.matches()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				String member = matcher.group(i);
				if(!member.isEmpty()) {
					System.out.println(String.format("%d = \"%s\"", i, member));
				}
			}
		}
	}


}
