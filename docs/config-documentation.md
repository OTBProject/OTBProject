---
title: Config Documentation
layout: markdown
---
# OTB Project Documentation

### Understanding and Modifying Configuration Files

## Table of Contents


- [Version](#version)
	- [Changelog](#changelog)
- [Warning](#warning)
- [Account](#account)
	- [Path](#path)
	- [Sample File](#sample-account-configuration-file)
	- [Fields](#fields)
- [General Config](#general-config)
 	- [Path](#path-1)
	- [Sample File](#sample-general-configuration-file)
	- [Fields](#fields-1)
- [Bot Config](#bot-config)
	- [Path](#path-2)
	- [Sample File](#sample-bot-configuration-file)
	- [Fields](#fields-2)
- [Channel Config](#channel-config)
	- [Path](#path-3)
	- [Sample File](#sample-channel-configuration-file)
	- [Fields](#fields-3)

## Version

Version 0.1.0

#### Changelog

* 0.1.0 - Account, general config, bot config, and channel config documentation added

## Warning

Be careful when modifying configuration files. If a configuration file is not in valid JSON format when it is loaded, it will be overwritten with the default configuration file. You can check whether JSON is formatted correctly <a href="http://jsonlint.com/" target="_blank">here</a>.

Additionally, you should not modify configuration files while the bot is running, because the bot may overwrite the changes (and it will not load the changes until it is restarted anyway).

## Account

#### Path

The account configuration file can be found at:
```
.otbproject/config/account.json
```

#### Sample Account Configuration File

```json
{
  "name" : "your_name_here",
  "passkey" : "your_passkey_here"
}
```

#### Fields

| Field | Description |
|:-----------|:------------|
|`name`|The name of the Twitch account being used for the bot.|
|`passkey`|For Twitch: an oauth token associated with the Twitch account. For Beam: the password for the Beam account (this may be changed to an oauth token at some point). You can generate an oauth token for your Twitch account <a href="http://twitchapps.com/tmi/" target="_blank">here</a>. (Note: generating a new oauth token voids previous tokens)|

## General Config

#### Path

The general configuration file can be found at:
```
.otbproject/config/general-config.json
```

#### Sample General Configuration File

```json
{
  "serviceName": "TWITCH",
  "portNumber" : 80,
  "ip_binding" : "0.0.0.0",
  "permanently_enabled_commands" : [ "!bot-enable-meta" ]
}
```

#### Fields

| Field | Description |
|:-----------|:------------|
|`serviceName`|The name of the service to connect to. Valid options are `"TWITCH"` AND `"BEAM"`.
|`portNumber`|Not currently in use.|
|`ip_binding`|Not currently in use.|
|`permanently_enabled_commands`|A list of commands which the bot will listen for even when it is disabled.|

## Bot Config

#### Path

The bot configuration file can be found at:
```
.otbproject/data/bot-channel/bot-config.json
```

#### Sample Bot Configuration File

```json
{
  "channelJoinSetting" : "NONE",
  "whitelist" : [ ],
  "blacklist" : [ ],
  "currentChannels" : [ ],
  "messageSendDelayInMilliseconds" : 1600
}
```

#### Fields

| Field | Description |
|:-----------|:------------|
|`channelJoinSetting`|Controls how the bot is allowed to join channels when the `!join` command is run in the bot's channel. Valid options for the `channeJoinSetting` are: `"NONE"`, `"WHITELIST"`, and `"BLACKLIST"`.<br>`"WHITELIST"` allows the bot to join only channels listed in the `whitelist`.<br>`"BLACKLIST"` prevents the bot from joining channels listed in the `blacklist`.<br>`"NONE"` allow the bot to join any channel.|
|`whitelist`|A list of channels the bot is allowed to join if the `channelJoinSetting` is `"WHITELIST"`|
|`blacklist`|A list of channels the bot is not allowed to join if the `channeJoinSetting` is `"BLACKLIST"`|
|`currentChannels`|The channels the bot was in when it was last shut down. These channels will be joined when the bot starts.|
|`messageSendDelayInMilliseconds`|The minimum delay in milliseconds between when the bot can send two messages in a channel. Be careful modifying this value, as the bot may send too many messages in too short a duration if it is not sufficiently high. If the bot sends too many messages too quickly, it may be IP banned from Twitch for 8 hours.|

## Channel Config

#### Path

The configuration file for a given channel can be found at:
```
.otbproject/data/channels/<channel name>/config.json
```

#### Sample Channel Configuration File

```json
{
  "commandCooldown" : 10,
  "userCooldowns" : {
    "ul_internal" : 0,
    "ul_broadcaster" : 0,
    "ul_super_moderator" : 0,
    "ul_moderator" : 0,
    "ul_regular" : 15,
    "ul_subscriber" : 30,
    "ul_default" : 30
  },
  "debug" : false,
  "enabled" : true,
  "queueLimits" : {
    "highPriorityLimit" : -1,
    "defaultPriorityLimit" : 5,
    "lowPriorityLimit" : 0
  }
}
```

#### Fields

| Field | Description |
|:-----------|:------------|
|`commandCooldown`|The time (in seconds) after a command is run before it can be run again. A value of `0` means no cooldown.|
|`userCooldowns`|The times (in seconds) after users of various user levels run commands before they can run another command. A value of `0` means no cooldown. See the table below.|
|`debug`|Whether or not debug commands and response commands will be printed in the channel. Set to `true` to print debug commands.|
|`enabled`|Whether or not the bot is enabled. Set to `true` to enable.|
|`queueLimits`|The number of messages queued to be sent in a channel after which messages of a given priority will be dropped (they will not be queued). See the table below.|

##### userCooldowns

| Field | Description |
|:-----------|:------------|
|`ul_internal`|The time (in seconds) after a user of the user level 'Internal' runs a command before they can run another command.|
|`ul_broadcaster`|The time (in seconds) after a user of the user level 'Broadcaster' runs a command before they can run another command.|
|`ul_super_moderator`|The time (in seconds) after a user of the user level 'Super-moderator' runs a command before they can run another command.|
|`ul_moderator`|The time (in seconds) after a user of the user level 'Moderator' runs a command before they can run another command.|
|`ul_regular`|The time (in seconds) after a user of the user level 'Regular' runs a command before they can run another command.|
|`ul_subscriber`|The time (in seconds) after a user of the user level 'Subscriber' runs a command before they can run another command.|
|`ul_default`|The time (in seconds) after a user of the user level 'Default' runs a command before they can run another command.|

##### queueLimits

| Field | Description |
|:-----------|:------------|
|`highPriorityLimit`|The number of messages queued to be sent in a channel after which messages of the priority 'High' will be dropped (they will not be queued).|
|`defaultPriorityLimit`|The number of messages queued to be sent in a channel after which messages of the priority 'Default' will be dropped (they will not be queued).|
|`lowPriorityLimit`|The number of messages queued to be sent in a channel after which messages of the priority 'Low' will be dropped (they will not be queued).|
