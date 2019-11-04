package kiyobot.util.gateway;

import kiyobot.util.ObjectContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum that contains all the opcodes defined by the Discord API at:
 *  https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#gateway-gateway-opcodes
 */
public enum GatewayOpcode {

	/**
	 * Instances of this enum that correspond to a given opcode:
	 *
	 * 0   Dispatch                 Receive         dispatches an event
	 * 1   Heartbeat                Send/Receive    used for ping checking
	 * 2   Identify                 Send            used for client handshake
	 * 3   Status Update            Send            used to update the client status
	 * 4   Voice State Update       Send            used to join/move/leave voice channels
	 * 6   Resume                   Send            used to resume a closed connection
	 * 7   Reconnect                Receive         used to tell clients to reconnect to the gateway
	 * 8   Request Guild Members    Send            used to request guild members
	 * 9   Invalid Session          Receive         used to notify client they have an invalid session id
	 * 10  Hello                    Receive         sent immediately after connecting, contains heartbeat and server debug information
	 * 11  Heartbeat ACK            Receive         sent immediately following a client heartbeat that was received
	 */
	DISPATCH(0),
	HEARTBEAT(1),
	IDENTIFY(2),
	STATUS_UPDATE(3),
	VOICE_STATE_UPDATE(4),
	VOICE_SERVER_PING(5),
	RESUME(6),
	RECONNECT(7),
	REQUEST_GUILD_MEMBERS(8),
	INVALID_SESSION(9),
	HELLO(10),
	HEARTBEAT_ACK(11);

	private static final Map<Integer, GatewayOpcode> INSTANCE_BY_OPCODE = new HashMap<>();
	private final int opcode;

	/*
	 * This static block that puts all instances of this enum into a Map<Integer, GatewayOpcode>
	 *
	 * This gets called after all instances have been created, as by virtue of being an enum;
	 *      enum values MUST be put before any static call, therefor will ALWAYS come before
	 *      any static calls/blocks
	 *
	 * forEach(instance -> INSTANCE_BY_OPCODE.put(instance.getOpcode(), instance) is equivalent to:
	 *
	 *      for(GatewayOpcode instance : GatewayOpcode.values()) {
	 *          INSTANCE_BY_OPCODE.put(instance.getOpcode(), instance)
	 *      }
	 */
	static {
		Arrays.asList(GatewayOpcode.values())
				.forEach(instance -> INSTANCE_BY_OPCODE.put(instance.getOpcode(), instance));
	}

	GatewayOpcode(int opcode) {
		this.opcode = opcode;
	}

	/**
	 * Gets the opcode of this instance.
	 */
	public int getOpcode() {
		return this.opcode;
	}

	/**
	 * Returns the GatewayOpcode instance of the given opcode
	 * @param opcode - opcode of the response
	 * @return GatewayOpcode
	 */
	public static ObjectContainer<GatewayOpcode> fromOpcode(int opcode) {
		return new ObjectContainer<>(INSTANCE_BY_OPCODE.get(opcode));
	}
}
