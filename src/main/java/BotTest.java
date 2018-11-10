import kiyobot.util.ConfigArgParser;
import kiyobot.util.DiscordConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Packets sent from the client to the Gateway API are encapsulated within a gateway payload
 * object and must have the proper opcode and data object set. The payload object can then be
 * serialized in the format of choice (see ETF/JSON), and sent over the websocket. Payloads to
 * the gateway are limited to a maximum of 4096 bytes sent, going over this will cause a
 * connection termination with error code 4002.
 *
 *  {
 *      "op": 0,
 *      "d": {},
 *      "s": 42,
 *      "t": "GATEWAY_EVENT_NAME"
 *  }
 *
 *  Kiyobot Testing - general: 510555588414144554
 *
 *  String.format(%[argument_index$][flags][width]conversion);
 *      %s - put string in
 */
public class BotTest {

	private static final String KIYO_GENERAL = "510555588414144554";
	private static final Logger LOGGER = LogManager.getLogger();

	public BotTest() {

	}

	public static void main(String[] args) {
		ConfigArgParser parser = new ConfigArgParser();
		parser.parseConfig();
		DiscordConnection connection = new DiscordConnection();
		connection.getWss();
		connection.connect();
	}
}
