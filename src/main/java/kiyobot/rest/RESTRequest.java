package kiyobot.rest;

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
	 * @return
	 */
	public String getMethod() {
		return this.method;
	}
	
	/**
	 * Returns the request's uri
	 * @return
	 */
	public String getUri() {
		return this.uri;
	}
	
	/**
	 * Returns the requests http protocol version
	 * @return
	 */
	public String getHttpProtocol() {
		return this.httpProtocol;
	}
	
	/**
	 * Returns the value of the given key if it exists
	 * @param key
	 * @return
	 */
	public String getFromBody(String key) {
		String value = this.body.get(key);
		return value;
	}
	
	/**
	 * String representation of the response that is being sent
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.method + " ");
		sb.append(this.uri + " ");
		sb.append(this.httpProtocol + "\r\n");
		for(String header : this.headers.keySet()) {
			sb.append(header + ": " + this.headers.get(header));
			sb.append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}
}
