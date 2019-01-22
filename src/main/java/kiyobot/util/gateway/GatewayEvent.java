package kiyobot.util.gateway;

import kiyobot.util.ObjectContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum GatewayEvent {

	READY("READY"),
	GUILD_CREATE("GUILD_CREATE"),
	MESSAGE_CREATE("MESSAGE_CREATE");

	private static final Map<String, GatewayEvent> INSTANCE_BY_EVENT = new HashMap<>();
	private final String event;

	static {
		Arrays.asList(GatewayEvent.values())
				.forEach(instance -> INSTANCE_BY_EVENT.put(instance.getEvent(), instance));
	}

	GatewayEvent(String event) {
		this.event = event;
	}

	/**
	 * Gets the name of this event
	 * @return event
	 */
	public String getEvent() {
		return this.event;
	}

	/**
	 * Returns the GatewayOpcode instance of the given opcode
	 * @param event - opcode of the response
	 * @return GatewayOpcode
	 */
	public static ObjectContainer<GatewayEvent> fromEvent(String event) {
		return new ObjectContainer<>(INSTANCE_BY_EVENT.get(event));
	}
}
