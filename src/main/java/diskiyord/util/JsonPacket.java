package diskiyord.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A convenience class for creating JSON formatted strings for output
 *
 * Format:
 * 	{
 * 	    "key": value,
 * 	    "anotherkey": {
 * 	        "JsonPackets": "can",
 * 	        "also": "be",
 * 	        "values": true
 * 	    }
 * 	}
 */
public class JsonPacket {

	private JsonObject object;
	private Gson gson;

	private static final Logger LOGGER = LogManager.getLogger();

	public JsonPacket() {
		this.object = new JsonObject();
		this.gson = new Gson();
	}

	public JsonPacket(String json) {
		this.object = new JsonObject();
		this.gson = new Gson();
		mapJson(json);
	}

	public JsonPacket(JsonObject json) {
		this.object = json;
		this.gson = new Gson();
	}

	/**
	 * Turns the given string into a JsonObject, and builds the mapping for the key/value pairs
	 * @param string -
	 */
	private void mapJson(String string) {
		 this.object = gson.fromJson(string, JsonObject.class);
	}

	/**
	 * Adds key/value pair to map
	 * @param key - key
	 * @param value - value
	 */
	public void put(String key, Object value) {
		JsonElement ele = gson.toJsonTree(value);
		if(ele.isJsonObject()) {
			JsonObject obj = ele.getAsJsonObject();
			ele = obj.get("object");
			LOGGER.debug("ele is a JsonObject = {}", ele.toString());
		}
		this.object.add(key, ele);
	}

	/**
	 * Gets a value from the given key if it exists
	 * @param key - key
	 */
	public PacketElement get(String key) {
		return new PacketElement(this.object.get(key));
	}

	@Override
	public String toString() {
		return this.object.toString();
	}

	/**
	 * Inner class
	 */
	public class PacketElement {

		private JsonElement ele;

		private PacketElement(JsonElement ele) {
			this.ele = ele;
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public boolean asBoolean() {
			return this.ele.getAsBoolean();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public byte asByte() {
			return this.ele.getAsByte();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public char asChar() {
			return this.ele.getAsCharacter();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public double asDouble() {
			return this.ele.getAsDouble();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public float asFloat() {
			return this.ele.getAsFloat();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public int asInt() {
			return this.ele.getAsInt();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public Number asNumer() {
			return this.ele.getAsNumber();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public JsonPacket asPacket() {
			return new JsonPacket(this.ele.getAsJsonObject());
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public String asString() {
			return this.ele.getAsString();
		}

		/**
		 * Get the JsonElement as an int
		 * @return int
		 */
		public short asShort() {
			return this.ele.getAsShort();
		}
	}
}
