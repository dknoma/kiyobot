package diskiyord.rest;

import java.util.Map;

/**
 * Class that represents an HTTP request made by a client.
 * TODO: turn into a REST request rather than generic HTTP request. Dealing with wesockets, json for convenience
 * @author dk
 *
 */
public class RESTRequest {
	
	private String method;
	private String uri;
	private String httpProtocol;
	private Map<String, String> headers;
	private Map<String, String> body;
	
	public RESTRequest(String method, String uri, String httpProtocol, Map<String, String> headers, Map<String, String> body) {
		this.method = method;
		this.uri = uri;
		this.httpProtocol = httpProtocol;
		this.headers = headers;
		this.body = body;
	}
	
	/**
	 * Returns the request's method
	 * @return method
	 */
	public String getMethod() {
		return this.method;
	}
	
	/**
	 * Returns the request's uri
	 * @return uri
	 */
	public String getUri() {
		return this.uri;
	}
	
	/**
	 * Returns the requests http protocol version
	 * @return httpProtocol
	 */
	public String getHttpProtocol() {
		return this.httpProtocol;
	}
	
	/**
	 * Returns the value of the given key if it exists
	 * @param key - key
	 * @return value connected to the key
	 */
	public String getFromBody(String key) {
		return this.body.get(key);
	}
	
	/**
	 * String representation of the response that is being sent
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s ", this.method));
		sb.append(String.format("%s ", this.uri));
		sb.append(String.format("%s\r\n", this.httpProtocol));
		for(String header : this.headers.keySet()) {
			sb.append(String.format("%1$s: %2$s", header, this.headers.get(header)));
			sb.append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}
}
