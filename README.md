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

# Revision History
Change log below:

## Contributions
Drew Noma - djknoma@gmail.com

[https://github.com/dknoma](https://github.com/dknoma)

Version 0.0.0
* Created repository

# NOTES
> Since this is implementing a custom library, it may not be finished in time.

In the case that this custom library is not finished in time, an existing Discord Java library like Javacord or JDA will be used in its place.