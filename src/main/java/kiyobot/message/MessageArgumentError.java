package kiyobot.message;

import kiyobot.util.ObjectContainer;
import kiyobot.util.gateway.GatewayEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum MessageArgumentError {

	NOT_ENOUGH_ARGUMENTS("Not enough arguments in command."),
	UNKNOWN_COMMAND("Unkown command. Use !commands for a list of possible commands.");

	private static final Map<String, GatewayEvent> INSTANCE_BY_ERROR = new HashMap<>();
	private final String errorMsg;

	static {
		Arrays.asList(GatewayEvent.values())
				.forEach(instance -> INSTANCE_BY_ERROR.put(instance.getEvent(), instance));
	}

	MessageArgumentError(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * Gets the name of this event
	 * @return event
	 */
	public String getErrorMsg() {
		return this.errorMsg;
	}

	/**
	 * Returns the GatewayOpcode instance of the given opcode
	 * @param errorMsg - opcode of the response
	 * @return GatewayOpcode
	 */
	public static ObjectContainer<GatewayEvent> fromEvent(String errorMsg) {
		return new ObjectContainer<>(INSTANCE_BY_ERROR.get(errorMsg));
	}
}
