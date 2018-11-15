package diskiyord.api;

import com.neovisionaries.ws.client.WebSocket;
import diskiyord.event.message.MessageCreateListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * API for this simple Diskiyord library.
 * @author dk
 */
public class DiskiyordApi {

	private WebSocket webSocket;
	private String token;

	private final ExecutorService threadpool;

	private volatile boolean isRunning;

	private static final Logger LOGGER = LogManager.getLogger();

	public DiskiyordApi(String token) {
		this.token = token;
		this.threadpool = Executors.newFixedThreadPool(1);
		this.isRunning = true;
	}

	/**
	 * Adds a MessageCreateListener to the websocket
	 * @param consumer - consumer
	 */
	public void addMessageCreateListener(Consumer<MessageCreateListener> consumer) {
		MessageCreateListener messageCreateListener = new MessageCreateListener(this);
		this.webSocket.addListener(messageCreateListener);
		this.threadpool.execute(() -> {
			while(this.isRunning) {
				consumer.accept(messageCreateListener);
			}
		});
		LOGGER.info("Finished adding MessageCreateListener.");
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
