package kiyobot.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

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

//	private Map<String, Object> output;
	private JsonObject object;
	private Gson gson;

	private static final Logger LOGGER = LogManager.getLogger();

	public JsonPacket() {
//		this.output = new LinkedHashMap<>();
		this.object = new JsonObject();
		this.gson = new Gson();
	}

	public JsonPacket(String json) {
//		this.output = new LinkedHashMap<>();
		this.object = new JsonObject();
		this.gson = new Gson();
		mapJson(json);
	}

	/**
	 * Turns the given string into a JsonObject, and builds the mapping for the key/value pairs
	 * @param string -
	 */
	private void mapJson(String string) {
		 this.object = gson.fromJson(string, JsonObject.class);
//		JsonObject obj = gson.fromJson(string, JsonObject.class);
//		for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
//			this.output.put(entry.getKey(), entry.getValue().toString());
//		}
	}

	/**
	 * Adds key/value pair to map
	 * @param key -
	 * @param value -
	 */
	public void put(String key, Object value) {
//		this.output.put(key, value);
		JsonElement ele = gson.toJsonTree(value);
		JsonObject obj = new JsonObject();
		if(ele.isJsonObject()) {
			obj = ele.getAsJsonObject();
			ele = obj.get("object");
			System.out.println("ele is a JsonObject = " + ele.toString());
		}
//		System.out.println("key=" + key + ", value=" + ele);
		this.object.add(key, ele);
	}

	@Override
	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("{");
//		int i = 0;
//		for(Map.Entry<String, Object> pair : this.output.entrySet()) {
//			String key = pair.getKey();
//			Object value = pair.getValue();
//			System.out.println("value = " + value.toString());
//			sb.append(String.format("%1$s: %2$s", key, value));
//			if(++i < this.output.size()) {
//				sb.append(",");
//			}
//		}
//		sb.append("}");
//		JsonObject obj = gson.fromJson(sb.toString(), JsonObject.class);
//		return obj.;
		return this.object.toString();
	}
}
