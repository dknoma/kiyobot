package kiyobot.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

	private Map<String, Object> output;
	private Gson gson;

	public JsonPacket() {
		this.output = new LinkedHashMap<>();
		this.gson = new Gson();
	}

	public JsonPacket(String json) {
		mapJson(json);
	}

	private void mapJson(String string) {
		JsonObject obj = gson.fromJson(string, JsonObject.class);
		for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			this.output.put(entry.getKey(), entry.getValue().toString());
		}
	}

	/**
	 * Adds key/value pair to map
	 * @param key -
	 * @param value -
	 */
	public void put(String key, Object value) {
		this.output.put(key, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int i = 0;
		for(Map.Entry<String, Object> pair : this.output.entrySet()) {
			i++;
			String key = pair.getKey();
			Object value = pair.getValue();
			if(value instanceof String) {
				sb.append(String.format("\"%1$s\": \"%2$s\"", key, value));
			} else {
				sb.append(String.format("\"%1$s\": %2$s", key, value));
			}
			if(i < this.output.size()) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
