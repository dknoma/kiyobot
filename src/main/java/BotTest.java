import kiyobot.util.ConfigArgParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Packets sent from the client to the Gateway API are encapsulated within a gateway payload
 * object and must have the proper opcode and data object set. The payload object can then be
 * serialized in the format of choice (see ETF/JSON), and sent over the websocket. Payloads to
 * the gateway are limited to a maximum of 4096 bytes sent, going over this will cause a
 * connection termination with error code 4002.
 *
 *  {
 *      "op": 0,
 *      "d": {},
 *      "s": 42,
 *      "t": "GATEWAY_EVENT_NAME"
 *  }
 *
 *  Kiyobot Testing - general: 510555588414144554
 *
 *  String.format(%[argument_index$][flags][width]conversion);
 *      %s - put string in
 */
public class BotTest {

    private static final String GET_URL = "https://discordapp.com/gateway";
    private static final String kiyoGeneral = "510555588414144554";
    private static final Logger LOGGER = LogManager.getLogger();

    public BotTest() {

    }

    public static void main(String[] args) {
        ConfigArgParser parser = new ConfigArgParser();
        parser.parseConfig();
        String postURL = String.format("https://discordapp.com/api/channels/%s/messages", kiyoGeneral);
        System.out.println(postURL);
        String headers = String.format("{\"Authorization\": \"Bot %1$s\", " +
                "\"User-Agent\": \"kiyobot (http://some.url, v0.1)\", " +
                "\"Content-Type\": \"application/json\"}", parser.getAuthTok());

        try {

            URL url = new URL(GET_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream outstream = connection.getOutputStream();
            outstream.write(headers.getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
            }
//            outstream.write(bytes);
//            outstream.flush();

        } catch (MalformedURLException mue) {
            LOGGER.fatal("URL is malformed, {},\n{}", mue.getMessage(), mue.getStackTrace());
        } catch (IOException ioe) {
            LOGGER.fatal("Error has occured when attempting connection, {},\n{}", ioe.getMessage(), ioe.getStackTrace());
        }
    }
}
