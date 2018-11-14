package diskiyord.logger;

import com.neovisionaries.ws.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class WebsocketLogger implements WebSocketListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onStateChanged(WebSocket webSocket, WebSocketState webSocketState) throws Exception {
        LOGGER.debug("onStateChanged: {}", webSocketState);
    }

    @Override
    public void onConnected(WebSocket webSocket, Map<String, List<String>> map) throws Exception {
        LOGGER.debug("onConnected: {}", map);
    }

    @Override
    public void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {
        LOGGER.debug("onConnectError: {}", e);
    }

    @Override
    public void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {
        LOGGER.debug("onDisconnected: webSocketFrame={}, webSocketFrame1={}, b={}", webSocketFrame, webSocketFrame1, b);
    }

    @Override
    public void onFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onFrame: {}", webSocketFrame);
    }

    @Override
    public void onContinuationFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onContinuationFrame: {}", webSocketFrame);
    }

    @Override
    public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onTextFrame: {}", webSocketFrame);
    }

    @Override
    public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onBinaryFrame: {}", webSocketFrame);
    }

    @Override
    public void onCloseFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onCloseFrame: {}", webSocketFrame);
    }

    @Override
    public void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onPingFrame: {}", webSocketFrame);
    }

    @Override
    public void onPongFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onPongFrame: {}", webSocketFrame);
    }

    @Override
    public void onTextMessage(WebSocket webSocket, String s) throws Exception {
        LOGGER.debug("onTextMessage: {}", s);
    }

    @Override
    public void onTextMessage(WebSocket webSocket, byte[] bytes) throws Exception {
        LOGGER.debug("onTextMessage: {}", bytes);
    }

    @Override
    public void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {
        LOGGER.debug("onBinaryMessage: {}", bytes);
    }

    @Override
    public void onSendingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onSendingFrame: {}", webSocketFrame);
    }

    @Override
    public void onFrameSent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onFrameSent: {}", webSocketFrame);
    }

    @Override
    public void onFrameUnsent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onFrameUnsent: {}", webSocketFrame);
    }

    @Override
    public void onThreadCreated(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
        LOGGER.debug("onThreadCreated: threadType={}, thread={}", threadType, thread);
    }

    @Override
    public void onThreadStarted(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
        LOGGER.debug("onThreadStarted: threadType={}, thread={}", threadType, thread);
    }

    @Override
    public void onThreadStopping(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
        LOGGER.debug("onThreadStopping: threadType={}, thread={}", threadType, thread);
    }

    @Override
    public void onError(WebSocket webSocket, WebSocketException e) throws Exception {
        LOGGER.debug("onError: exception={}", e);
    }

    @Override
    public void onFrameError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onFrameError: exception={}, webSocketFrame={}", e, webSocketFrame);
    }

    @Override
    public void onMessageError(WebSocket webSocket, WebSocketException e, List<WebSocketFrame> list) throws Exception {
        LOGGER.debug("onMessageError: exception={}, list={}", e, list);
    }

    @Override
    public void onMessageDecompressionError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {
        LOGGER.debug("onMessageDecompressionError: exception={}, bytes={}", e, bytes);
    }

    @Override
    public void onTextMessageError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {
        LOGGER.debug("onTextMessageError: exception={}, bytes={}", e, bytes);
    }

    @Override
    public void onSendError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {
        LOGGER.debug("onSendError: exception={}, webSocketFrame={}", e, webSocketFrame);
    }

    @Override
    public void onUnexpectedError(WebSocket webSocket, WebSocketException e) throws Exception {
        LOGGER.debug("onUnexpectedError: exception={}", e);
    }

    @Override
    public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {
        LOGGER.debug("handleCallbackError: throwable={}", throwable);
    }

    @Override
    public void onSendingHandshake(WebSocket webSocket, String s, List<String[]> list) throws Exception {
        LOGGER.debug("onSendingHandshake: s={}, list={}", s, list);
    }
}
