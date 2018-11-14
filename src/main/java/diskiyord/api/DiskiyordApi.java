package diskiyord.api;

import com.neovisionaries.ws.client.WebSocket;
import diskiyord.event.message.MessageCreateListener;

import java.util.function.Consumer;

/**
 * API for this simple Diskiyord library.
 * @author dk
 */
public class DiskiyordApi {

	private WebSocket webSocket;
	private String token;

	public DiskiyordApi(String token) {
		this.token = token;
	}

	/**
	 * Adds a MessageCreateListener to the websocket
	 * @param consumer - consumer
	 */
	public void addMessageCreateListener(Consumer<MessageCreateListener> consumer) {
		MessageCreateListener messageCreateListener = new MessageCreateListener(this);
		this.webSocket.addListener(messageCreateListener);
		consumer.accept(messageCreateListener);
	}

	/**
	 * Sets the wss connection
	 * @param webSocket -
	 */
	public void setWebSocket(WebSocket webSocket) {
		this.webSocket = webSocket;
	}

	/**
	 * Gets the websocket
	 * @return websocket
	 */
	public WebSocket getWebSocket() {
		return this.webSocket;
	}

	/**
	 * Gets the token of this API
	 * @return token
	 */
	public String getToken() {
		return this.token;
	}
}
