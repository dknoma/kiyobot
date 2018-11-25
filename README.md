# Kiyobot, Diskiyord, & JQL
> A Java Discord Bot, Java Discord Library, and SQL Library

This is a Discord bot implemented using Java. This bot is a personal side project that is being updated over time. Things may be incomplete or design may not be 100% efficient, but they are being kept track of.
Diskiyord is the Java API I'm currently developing for my bot to interact with Discord's API. JQL is a SQL API I'm also developing that makes sending queries to a SQL database less time consuming, compared to raw SQL queries.
These APIs are being developed to learn more about software development and what goes on behind the scenes when using 3rd party libraries.

# Bot Functionality
* Receive and send messages to channels in the Discord server the bot is present at.
* Receive commands and send messages to the channel these commands are called at.
	* Can be used for meme commands, eg. `+kek`
		* This kind of command could choose a random image link from a pool of links which would have pictures of people laughing or have the word "kek" in them.
		* Commands for the LULz
	* Implemented commands:
		* `!addexgfx & !getexgfx`
            * These commands will allow a user to add information to a database and get information back from that database; eg. PostgreSQL, MySQL
            * Format:
                * ```!addexgfx  <filename>  <description>  <level type>  <artIsFinished>  <img link>```
                * ```!getexgfx  <filename>```
            * These kinds of command would be useful for organizing project information in a workspace with other Discord users.
		* `!ping`
			* Pings the bot, who responds with "Pong!".
			* Don't overuse the commands though ;)
		* `!commands`
			* Sends a message containing a list of the currently implemented commands to the current channel.

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
		});
	}
}
```

### Sample Commands

The following are some examples of commands you could make the bot do.

```Java
public class MessageBot {

	public static void main(String[] args) {
		// db setup
        JsonSqlConfigParser sqlparser = new JsonSqlConfigParser();
        sqlparser.parseConfig(SQL_CONFIG_FILE);
        String modelDirectory = sqlparser.getModelDirectory();

        SQLModelBuilder builder = new SQLModelBuilder();
        builder.findModelFiles(modelDirectory);
        builder.readFiles();
        if(!builder.areModelsFormattedCorrectly()) {
            return;
        }
        Map<String, SQLModel> models = builder.getCopyOfModels();
        PostgresHandler pghandler = new PostgresHandler(models);

        // Connects the PostgreSQLhandler to the Postgres database
        pghandler.setConnection(sqlparser.getDb(), sqlparser.getHost(), sqlparser.getPort(),
                sqlparser.getUsername(), sqlparser.getPassword());

		// api token goes here. NOTE* Make sure to store it securely and not in any public repository.
		String token = "api_token";
		// Creates the api for the bot.
		DiskiyordApi api = DiskiyordApiBuilder.buildApi(token);
		// Adds an event listener specifically for detecting text messages being sent in a server.
		api.addMessageCreateListener(messageEvent -> {
			String message = messageEvent.getMessageContent();
			// Can parse the message for arguments after the command; in this case it splits on 2 space characters
			String[] messageArgs = message.split(" {2}");
			String errorMessage = "";
			try {
				// Using switch() for different possible commands.
				switch (messageArgs[0]) {
					case "!addexgfx":
						errorMessage = MessageArgumentError.NOT_ENOUGH_ARGUMENTS.getErrorMsg();
						//!addexgfx  <filename>  <description>  <type>  <completed>  <imglink>
						if(messageArgs.length != 6) {
							messageEvent.getChannel().sendTextMessage(errorMessage);
							break;
						}
						ColumnObject[] columns = new ColumnObject[5];
						columns[0] = new ColumnObject<>(FILENAME, messageArgs[1], STRING);
						columns[1] = new ColumnObject<>(DESCRIPTION, messageArgs[2], STRING);
						columns[2] = new ColumnObject<>(TYPE, messageArgs[3], STRING);
						columns[3] = new ColumnObject<>(COMPLETED, Boolean.parseBoolean(messageArgs[4]), BOOLEAN);
						columns[4] = new ColumnObject<>(IMG_LINK, messageArgs[5], STRING);
						dbhandler.executeUpdate(dbhandler.insert(EXGFX, columns));

						messageEvent.getChannel().sendTextMessage("Data successfully added to the database!");
						break;
				}
			}
		}
	}
}
```

## SQLModel

The SQLModel class is meant to hold information about a particular table in a database. Its main use is to parse information from a .json file representing the desired model.
Rather than having to create a class each time a user wants to create a model, they only need to create a .json of the model(s) they want, and use the SQLModelBuilder to build those models. The SQLModelBuilder will look at all the files in "./models" and try to create models of all present .json files.

The following code is all the code needed to build the models.

```Java
public class MessageBot {
	public static void main(String[] args) {
		// Creates the model builder
		SQLModelBuilder builder = new SQLModelBuilder();
		// Locates all the .json model files in ./models
		builder.findModelFiles("./models");
		// Reads in those files and creates SQLModels from them
		builder.readFiles();
	}
}
```

The following is an example of how to setup a SQLModel so that the JDBCHandler can extract information and set up the appropriate table.

```JSON
{
	"name": "exgfx",
	"autoIncrement": true,
	"columns": [
		{
			"key": "filename",
			"attributes": {
				"type": "STRING",
				"length": 8,
				"lengthIsVar": true,
				"allowNull": false
			}
		},
		{
			"key": "description",
			"attributes": {
				"type": "STRING",
				"length": 100,
				"lengthIsVar": true,
				"allowNull": false
			  }
		},
		{
			"key": "type",
			"attributes": {
				"type": "STRING",
				"length": 40,
				"lengthIsVar": true,
				"allowNull": false
			}
		},
		{
			"key": "completed",
			"attributes": {
				"type": "BOOLEAN",
				"defaultValue": false
			 }
		}
	]
}
```

# Revision History
## Version 1.0.0
* __Kiyobot__:
	* A basic messaging bot.
	* It can read in commands from the channel, and output the corresponding message to that channel.
	* It can also interact with a MySQL or PostgreSQL database.
		* It uses the below SQLModeler API.
	* The user will also need to specify their bot's authorization token in a config.json file.
		* The user needs to create a Discord application before using this API or else the API does nothing.
		* [Read this first.](https://discordapp.com/developers/docs/intro)
		* [Link to making a Discord application.](https://discordapp.com/developers/applications)
		* NOTE: Please DO NOT store your token in a public location as it is against Discords policy (and will allow unintended users to use your bot!).
* __Diskiyord API__:
	* A basic java API that allows users to create their own basic messaging bot for Discord.
	* The user can make the bot respond to messages in the same channel, or make message output to a different channel if they need something like a debugging channel.
	* Requires an authorization token from Discord to make a bot with this API.
* __JQL API__:
	* A basic java API that allows users to interact more easily with SQL databases rather than do raw SQL queries.
	* Users will create .json files representing the the information of the table they would like to create, and these files will should be put into a models folder. This folder can be specified in the sqlconfig.json file or will default to `./models`.
	* NOTE: This version only allows users to create INTEGER primary keys. STRING primary keys are not yet supported.
## Version 0.1.0
* Basic library set up, allows bot to view and send messages to a channel.
## Version 0.0.0
* Repository setup.

## Contributions
Drew Noma - djknoma@gmail.com

[https://github.com/dknoma](https://github.com/dknoma)

Current Version 1.0.0
* Kiyobot, Diskiyord, and JSQLModeler

# NOTES
> ~~Since this is implementing a custom library, it may not be finished in time.~~

~~In the case that this custom library is not finished in time, an existing Discord Java library like Javacord or JDA will be used in its place.~~

API was developed on time!