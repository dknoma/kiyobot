package diskiyord.event.message;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import diskiyord.api.DiskiyordApi;
import diskiyord.util.JsonPacket;
import diskiyord.util.ObjectContainer;
import diskiyord.util.channel.Channel;
import diskiyord.util.gateway.GatewayEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A WebsocketListener specifically for dealing with Message Create related events.
 * Useful for creating various bot commands.
 * @author dk
 */
public class MessageCreateListener extends WebSocketAdapter {

	private String messageContent;
	private String channelId;
	private AtomicReference<CountDownLatch> latch;

	private final DiskiyordApi api;

	private volatile boolean receivedMessage;

	private static final Logger LOGGER = LogManager.getLogger();

	public MessageCreateListener(DiskiyordApi api) {
		this.messageContent = "";
		this.channelId = "";
		this.receivedMessage = false;
		this.latch = new AtomicReference<>(new CountDownLatch(1));
		this.api = api;
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
				LOGGER.debug("Received a message.");
				this.messageContent = messagePacket.get("d").asPacket().get("content").asString();
				this.channelId = messagePacket.get("d").asPacket().get("channel_id").asString();
				LOGGER.debug("messageContent = {}", this.messageContent);
				this.receivedMessage = true;
				this.latch.get().countDown();
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
		System.out.println("Waiting for a message...");
		try {
			try {
				this.latch.get().await();
				System.out.println("Found a message.");
			} catch (InterruptedException e) {
				LOGGER.error("An interruption occurred, {},\n{}", e.getMessage(), e.getStackTrace());
			}
			return this.messageContent;
		} finally {
			this.receivedMessage = false;
			this.latch.set(new CountDownLatch(1));
		}
	}

	/**
	 * Gets a Channel object representing the current channel that the message is from
	 * @return message content
	 */
	public Channel getChannel() {
		return new Channel(this.channelId, this.api);
	}

	/**
	 * Gets a Channel object representing a specified channel from its id
	 * @return message content
	 */
	public Channel getChannel(String channelId) {
		return new Channel(channelId, this.api);
	}
}
