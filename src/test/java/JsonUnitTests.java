import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import kiyobot.util.JsonPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Test;

public class JsonUnitTests {

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	public void testJsonPacketPutsToString() {
		String jsonHardcode = "{\"b\":true,\"s\":\"string stuff\",\"i\":10,\"data\":{\"f\":false}}";
		System.out.println(new JsonPacket(jsonHardcode).toString());
		LOGGER.info("jsonHardcode: {}", jsonHardcode);

		JsonPacket jsonPacket = new JsonPacket();
		jsonPacket.put("b", true);
		jsonPacket.put("s", "string stuff");
		jsonPacket.put("i", 10);
		JsonPacket data = new JsonPacket();
		data.put("f", false);
		jsonPacket.put("data", data);

		String packet = jsonPacket.toString();
		LOGGER.info("packet:       {}", packet);

		String message = "No match.";
		boolean match = true;
		int i = 0, j = 0;
		while(i < jsonHardcode.length() && j < packet.length()) {
			if(jsonHardcode.charAt(i) != packet.charAt(j)) {
				match = false;
				message = String.format("Characters didn't match: i=%1$d {%2$c}, j=%3$d {%4$c}",
						i, jsonHardcode.charAt(i), j, packet.charAt(j));
				break;
			}
			i++;
			j++;
		}
		assertTrue(message, match);
	}
}

