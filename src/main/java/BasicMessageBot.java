import diskiyord.api.DiskiyordApi;
import diskiyord.api.DiskiyordApiBuilder;
import diskiyord.event.error.MessageArgumentError;
import diskiyord.util.JsonConfigArgParser;


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
public class BasicMessageBot {

	public BasicMessageBot() {

	}

	public static void main(String[] args) {
		JsonConfigArgParser parser = new JsonConfigArgParser();
		parser.parseConfig();
		DiskiyordApi api = DiskiyordApiBuilder.buildApi(parser.getAuthTok());

		api.addMessageCreateListener(messageEvent -> {
			while(true) {
				String message = messageEvent.getMessageContent();
				String[] messageArgs = message.split(" {2}");
				String errorMessage = "";
				try {
					switch (messageArgs[0]) {
						case "!ping":
							messageEvent.getChannel().sendTextMessage("Pong!");
							break;
						case "!addtodo":
							errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
							String botOutput = String.format("Added TODO: %s.", messageArgs[1]);
							messageEvent.getChannel().sendTextMessage(botOutput);
							break;
						default:
							break;
					}
				} catch(ArrayIndexOutOfBoundsException aiobe) {
					messageEvent.getChannel().sendTextMessage(errorMessage);
				}
			}
		});
		System.out.println("Finished bot stuff?");
	}
}
