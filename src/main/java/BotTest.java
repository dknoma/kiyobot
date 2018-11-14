import com.google.gson.Gson;
import diskiyord.util.JsonConfigArgParser;
import diskiyord.ws.DiscordWebsocketAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
 *
 *
 */
public class BotTest {

	private static final String KIYO_GENERAL = "510555588414144554";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson gson = new Gson();

	public BotTest() {

	}

	public static void main(String[] args) {
		//testing JsonPacket
//		JsonPacket packet = new JsonPacket();
//		packet.put("op", GatewayOpcode.HEARTBEAT.getOpcode());
//		packet.put("d", "STRING");
//		packet.put("asfasfa", true);
//		System.out.println(packet.toString());

		//testing app
		JsonConfigArgParser parser = new JsonConfigArgParser();
		parser.parseConfig();
		DiscordWebsocketAdapter connection = new DiscordWebsocketAdapter(parser.getAuthTok());
		connection.getWss();
		connection.connect();
	}
}
