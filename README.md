# Kiyobot
> Java Discord Bot

This is a Discord bot implemented using Java.

# Bot Functionality
* Receive and send messages to channels in the Discord server the bot is present at.
* Receive commands and send messages to the channel these commands are called at.
   * eg. `!addexgfx, !getexgfx`
      * These commands will allow a user to add information to a database and get information back from that database; eg. PostgreSQL, MySQL
      * Example format: ```!addexgfx <filename> <artIsFinished> <description> <level type> <img link>```
      * These kinds of command would be useful for organizing project information in a workspace with other Discord users.
   * Meme commands, eg. `+kek`
      * This kind of command could choose a random image link from a pool of links which would have pictures of people laughing or have the word "kek" in them.
      * Commands for the LULz

## Example Bot

This is an example of how a simple bot can be created using this API. This bot will be able to read messages being sent to a server and respond if a certain command is in that message.

```Java
public class MessageBot {

    public static void main(String[] args) {
        // api token goes here. NOTE* Make sure to store it securely and not in any public repository.
        String token = "api_token";
        // Creates the api for the bot.
        DiskiyordApi api = DiskiyordApiBuilder.buildApi(token);
        // Adds an event listener specifically for detecting text messages being sent in a server.
        api.addMessageCreateListener(messageEvent -> {
            while(true) {
                String message = messageEvent.getMessageContent();
                // Can parse the message for arguments after the command; in this case it splits on 2 space characters
                String[] messageArgs = message.split(" {2}");
                String errorMessage = "";
                try {
                    // Using switch() for different possible commands.
                    switch (messageArgs[0]) {
                        // If someone types "!ping", the bot responds with "Pong!"
                        case "!ping":
                            messageEvent.getChannel().sendTextMessage("Pong!");
                            break;
                        default:
                            break;
                    }
                } catch(ArrayIndexOutOfBoundsException aiobe) {
                    messageEvent.getChannel().sendTextMessage(errorMessage);
                }
            }
        });
    }
}
```

# Revision History
## Version 0.1.0
* Basic library set up, allows bot to view and send messages to a channel.
## Version 0.0.0
* Repository setup.

## Contributions
Drew Noma - djknoma@gmail.com

[https://github.com/dknoma](https://github.com/dknoma)

Current Version 0.1.0
* Basic message viewing and sending.

# NOTES
> Since this is implementing a custom library, it may not be finished in time.

In the case that this custom library is not finished in time, an existing Discord Java library like Javacord or JDA will be used in its place.