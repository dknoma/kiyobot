package diskiyord.event.message;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import diskiyord.util.JsonPacket;
import diskiyord.util.ObjectContainer;
import diskiyord.util.gateway.GatewayEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A WebsocketListener specifically for dealing with Message Create related events.
 * Useful for creating various bot commands.
 * @author dk
 */
public class MessageCreateListener extends WebSocketAdapter {

	private String messageContent;
	private String channelId;

	private static final Logger LOGGER = LogManager.getLogger();

	public MessageCreateListener() {
		this.messageContent = "";
		this.channelId = "";
	}

	@Override
	public void onTextMessage(WebSocket websocket, String message) throws Exception {
		LOGGER.info("onTextMessage: message={}", message);
		JsonPacket messagePacket = new JsonPacket(message);
		String event = messagePacket.get("t").asString();

		ObjectContainer<GatewayEvent> gatewayEvent = GatewayEvent.fromEvent(event);
		/*
		 * Must have gotten an illegal opcode
		 */
		if(!gatewayEvent.objectIsPresent()) {
			LOGGER.error("Received an unknown payload. \"op\"={} does not exist.", event);
			return;
		}

		switch(gatewayEvent.getObject()) {
			case MESSAGE_CREATE:
				this.messageContent = messagePacket.get("d").asPacket().get("content").asString();
				this.channelId = messagePacket.get("d").asPacket().get("channel_id").asString();
				break;
			default:
				LOGGER.debug("Nothing to see here...");
				break;
		}
	}

	/**
	 * Gets the message from the event
	 * @return message content
	 */
	public String getMessageContent() {
		return this.messageContent;
	}

	/**
	 * Gets the message from the event
	 * @return message content
	 */
	public String getChannelId() {
		return this.channelId;
	}
}
