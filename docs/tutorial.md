---
title: Tutorial
layout: markdown
---
# OTB Project Documentation

### Tutorial: How to Run a Bot

## Table of Contents
 - [Version](#version)
 - [Tutorial](#tutorial)
   - [Starting Out](#starting-out)
   - [Finding Your Installation Directory](#finding-your-installation-directory)
   - [Setting Your Account Information](#setting-your-account-information)
   - [Changing the Channel Join Setting](#changing-the-channel-join-setting)
     - [Running Chat Commands](#running-chat-commands)
     - [Modifying the Configuration File](#modifying-the-configuration-file)
   - [Wrapping Up](#wrapping-up)
   - [Other Notes](#other-notes)

## Version
Version 1.0

Corresponds to release 1.0.0

## Tutorial

#### Starting Out

Before being able to modify configurations or other data for the bot, you need to run it once so that it can set up all the files it needs. It will not actually connect to Twitch or Beam however, because it does not have your account information, so you can stop the bot after the window pops up. It may take several seconds for the window to pop up the first time you run the bot.

#### Finding Your Installation Directory

After running the bot once, it will have set up a directory to store data for the bot. The default installation directory is a folder called `.otbproject` in your home directory. On Windows, you can find your default installation directory by typing `%USERPROFILE%\.otbproject` into the address bar of Windows Explorer (which is the file explorer, and is not the same as Internet Explorer). I assume that if you are on Mac or Linux, you know where your home directory is.

After this point, all paths will be denoted within the `.otbproject` installation directory, and it is assumed that you have already determined that directory's location.

#### Setting Your Account Information

To set your account information, you need to modify the account configuration file. Depending what service you are using, you can find the account configuration file at `.otbproject/config/account-twitch.json` or `.otbproject/config/account-beam.json`. You can edit the file in any text editor (Notepad on Windows, for example). Set your username and passkey in the file, preferably while the bot is not running, and save the file. When you next run the bot, it will use that username and passkey to connect to Twitch or Beam. See the [config documentation](config-documentation.html#account) for more information about what passkey to use for each service.

#### Changing the Service

If you want the bot to connect to Beam, you need to modify the general configuration file, which you can find at `.otbproject/config/general-config.json`. In that file, set the service name to `"BEAM"`, and save the file. Preferably, this should be done while the bot is not running. See the [config documentation](config-documentation.html#general-config) for more inforation about changing which service is used.

#### Changing the Channel Join Setting

By default, the bot will join the channels of anyone who runs the `!join` command in the bot's channel. For a number of reasons, you may not want anyone to be able to use your bot. You can use a whitelist to decide which channels the bot can join by running commands in the bot's channel, or by modifying the bot configuration file.

###### Running Chat Commands

In order to run the configuration commands in the bot's channel, you must be logged in using the bot's account, or some other account which has been assigned a user level of super-moderator. For more information about assigning user levels, see the [chat documentation](chat-documentation.html#built-in-channel-commands).

To set the channel join mode to use a whitelist, run the command `!joinMode whitelist` in the bot's channel.

You can add channels to the whitelist using the command `!whitelist add <channel name>` (where `<channel name>` is the name of a channel), and remove channels from the whitelist using the command `!whitelist remove <channel name>`. The command `!whitelist list` will list the channels currently on the whitelist. More information about bot channel commands can be found in the [chat documentation](chat-documentation.html#built-in-bot-channel-commands).

###### Modifying the Configuration File

The bot configuration file can be found at `.otbproject/data/bot-channel/bot-config.json`. Change the `channelJoinSetting` from `"NONE"` to `"WHITELIST"`. Add any channels you want to be whitelisted inside the brackets in the `whitelist` field as a comma separated list. The channel names must be in quotes.

For example, the whitelist might look like the following:

```json
...
"whitelist" : [ "channel1", "channel2", "some_other_channel" ],
...
```

#### Wrapping Up

At this point, you should be able to run the program and have the bot run smoothly. You can find more information about commands that come with the bot and how to add custom commands in the [chat documentation](chat-documentation.html).

If you have any questions about the bot, feel free to leave them in the [issue tracker](../../../issues), or tweet at us <a href="https://twitter.com/OTBProject" target="_blank">@OTBProject</a>. Please use the issue tracker to report bugs or request features (140 characters really isn't enough).

#### Other Notes

This tutorial is geared towards users who are not familiar with terminals and might not be comfortable poking around config files on their own. The tutorial is not entirely accurate for users running the bot in a headless environment.
