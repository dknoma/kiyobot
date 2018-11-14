package diskiyord.api;

import diskiyord.ws.DiscordWebsocketAdapter;

public class DiskiyordApiBuilder {


	public static DiskiyordApi buildApi(String token) {
		DiskiyordApi api = new DiskiyordApi(token);
		DiscordWebsocketAdapter websocketConnection = new DiscordWebsocketAdapter(api);
		api.setWebSocket(websocketConnection.getWebsocket().get());
		return api;
	}
}
