---
title: Chat Documentation
layout: markdown
---

# OTB Project Documentation

### Interacting with a Bot in Chat

## Table of Contents


- [Version](#version)
	- [Changelog](#changelog)
- [Commands](#commands)
	- [Built-in Channel Commands](#built-in-channel-commands)
	- [Built-in Bot-Channel Commands](#built-in-bot-channel-commands)
	- [Flags](#flags)
	- [User Levels](#user-levels)
	- [Aliases](#aliases)
	- [Built-in Aliases](#built-in-aliases)
	- [Command Responses](#command-responses)
- [Special Terms](#special-terms)
	- [Terms](#terms)
	- [Modifiers](#modifiers)
	- [Embedded Strings](#embedded-strings)
	- [Examples](#examples)

## Version

Version 0.2.0

#### Changelog

* 0.2.0
  - Release 1.0.0
  - Added descriptions of aliases and script commands that come with release
  - Added more terms to the parser
* 0.1.2
  - Special terms can contain spaces in defaults and other embedded strings
  - More special terms added
* 0.1.1
  - Changed syntax for special terms and defaults
* 0.1.0
  - Described special terms and their modifiers; gave examples of both

## Commands

Commands are words which the bot will respond to if they are the first word in a message. Commands often start with an exclamation mark, such as `!example`, but they can be any string of characters without a space in them. Some commands can tell the bot to perform an action, instead of just printing a message in response; these are known as "script commands".

#### Built-in Channel Commands

Each channel by default has script commands to add and remove commands and aliases, as well as script commands to do several other things. The built-in channel script commands are below. Flags and user levels are covered later.

| Command | Flags | Arguments | Description | ExecUL |
|:--------|:------|:----------|:------------|:-------|
|`!command` `add | new`|`ul`, `ma`|`<command>` `<response>`|Adds a command which did not previously exist. The command is the first space-delineated word (after any flags), and the response is everything following it.|Moderator|
|`!command` `set | edit`|`ul`, `ma`|`<command>` `<response>`|Sets the specified command with the given response, whether it existed before or not. The command is the first space-delineated word (after any flags), and the response is everything following it.|Moderator|
|`!command` `remove | delete | rm | del`||`<command>`|Removes the specified command.|Moderator|
|`!command` `list`|||Lists all commands (other than the built-in response commands) without their responses.|Moderator|
|`!command` `raw`||`<command>`|Prints the response for the specified command.|Moderator|
|`!command` `enable`||`<command>`|Enables the specified command.|Moderator|
|`!command` `disable`||`<command>`|Disables the specified command.|Moderator|
|`!alias-meta` `add | new`||`<alias>` `<command>`|Adds an alias which did not previously exist. The alias is the first space-delineated word, and the command is everything following it (may contain spaces).|Moderator|
|`!alias-meta` `set`||`<alias>` `<command>`|Sets the specified alias to the given command, whether it existed before or not. The alias is the first space-delineated word, and the command is everything following it (may contain spaces).|Moderator|
|`!alias-meta` `remove | delete | rm | del`||`<alias>`|Removes an alias.|Moderator|
|`!alias-meta` `list`|||Lists all aliases without their commands.|Moderator|
|`!alias-meta` `getCommand`||`<alias>`|Prrints the command to which an alias is aliased.|Moderator|
|`!alias-meta` `enable`||`<alias>`|Enables the specified alias.|Moderator|
|`!alias-meta` `disable`||`<alias>`|Disables the specified alias.|Moderator|
|`!setExecUL`||`<command>` `<user level>`|Sets the minimum user level required to execute the specified command.|Moderator|
|`!setMinArgs`||`<command>` `<min args>`|Sets the minimum number of arguments with which a command must be run. Cannot be negative.|Moderator|
|`!rename`||`<old command name>` `<new command name>`|Renames a command. A command with the new name cannot already exist.|Moderator|
|`!resetCount`||`<command>`|Resets the count of the specified command to 0.|Moderator|
|`!assignUserLevel`||`<user>` `<user level>`|Assigns the specified user level to the specified user.|Broadcaster|
|`!silence-meta` `on | true`|||Silences the bot in the channel. Commands which run scripts will still execute the scripts, but no responses will be printed in chat.|Super-moderator|
|`!silence-meta` `off | false`|||Unsilences the bot in the channel. Responses will be printed again.|Super-moderator|
|`!bot-enable-meta` `false`|||Disables the bot in the channel. It will not respond to any commands (both by running scripts and printing responses in chat) except the command to enable it.|Super-moderator|
|`!bot-enable-meta` `true`|||Enables the bot in the channel.|Super-moderator|
|`!leave`||`<bot name>`|Makes the bot leave the channel. Its name must be specified in case other bots are running in the channel (such as the Monstercat bot), and you want a different bot to leave.|Super-moderator|
|`!quote` `add | new`||`<quote text>`|Adds a new quote with the specified text. A quote with the same text cannot already exist.|Moderator|
|`!quote` `remove | delete | del | rm`||`<quote id>`|Removes the quote with the specified ID, if it exists.|Moderator|
|`!quote` `list | listids | ids`|||Lists the IDs of all quotes.|Moderator|
|`!quote` `getid`||`<quote text>`|Prints the ID of the quote with the given text, if it exists.|Moderator|

Additionally, there is also a `!getQuote` command which does not run a script. The special terms used in the response are explained later.

| Command | Response | ExecUL |
|:--------|:---------|:-------|
|`!getQuote`|[[quote{{[[arg1]]}}]]|Moderator|

#### Built-in Bot-Channel Commands

The bot's channel has its own script commands to perform actions specific to the bot, such as joining channels. With the exception of the `!at` and `!join` commands, which can be run by any user, commands in the bot's channel can only be run by users with a user level of super-moderator or higher. The built-in bot-channel script commands are below.

| Command | Flags | Arguments | Description |
|:--------|:------|:----------|:------------|
|`!at`||`<channel>` `<command>`|Runs the specified command in the specified channel, and prints the response in the bot's channel. Useful if you don't want to spam a channel's chat while it's streaming.|
|`!join`|||Joins the channel of the user who ran the command. Respects the join mode, as specified in the [config documentation](config-documentation.html#fields-2).|
|`!joinMode` `whitelist | blacklist | none`||`<mode>`|Sets the join mode to the mode specified. See the [config documentation](config-documentation.html#fields-2).|
|`!whitelist` `add`||`<channel>`|Adds the specified channel to the whitelist of channels which may be joined.|
|`!whitelist` `remove`||`<channel>`|Removes the specified channel from the whitelist of channels which may be joined.|
|`!whitelist` `list`|||Lists the channels in the channel join whitelist.|
|`!blacklist` `add`||`<channel>`|Adds the specified channel to the blacklist of channels which may not be joined.|
|`!blacklist` `remove`||`<channel>`|Removes the specified channel from the whitelist of channels which may not be joined.|
|`!blacklist` `list`|||Lists the channels in the channel join blacklist.|

#### Flags

Flags are optional arguments for a command which give more control over what the command does or how it executes. Any flags mentioned above are listed here.

| Flag | Usage | Option(s) | Description |
|:-----|:------|:----------|:------------|
|`ul`|`--ul=<option>`|A user level marker as described in the table below|Specifies the minimum user level required to execute a command.|
|`ma`|`--ma=<option>`|An integer greater than 0|Specifies the minimum number of arguments with which a command must be run.|

#### User Levels

The following are valid user levels to use with the `ul` flag.

| User Level | Markers |
|:-----------|:----------|
|Default|`default | def | none | any | all`|
|Subscriber|`subscriber | sub`|
|Regular|`regular | reg`|
|Moderator|`moderator | mod`|
|Super Moderator|`super-moderator | super_moderator | smod | sm`|
|Broadcaster|`broadcaster | bc`|

Additionally, there is the user level 'Ignored', which can be used with `!assignUserLevel`, but not with the `ul` flag. You can also reset a user's user level to whatever it would be using Twitch alone, as described below.

| User Level | Markers |
|:-----------|:----------|
|Ignored|`ignored | ig`|
|Reset|`reset | twitch`|

The user levels 'Subscriber', 'Moderator', and 'Broadcaster' cannot be assigned using `!assignUserLevel` because they are user properties determined by Twitch.

#### Aliases

An alias is a word which represents another word or group of words. Like commands, aliases are only processed by the bot if they are the first word of a message. However, an alias can expand into a command and parameters for the command, to make running certain commands easier. If an alias expands to something which is not a command, it won't do anything.

Aliases will not loop; they will only be expanded once. For example, if you alias `!foo` to `!bar` and `!bar` to `!foo test`, it will process the message `!foo example` into `!foo test example`, and then check if `!foo` is a command to run.

#### Built-in Aliases

| Alias | Command |
|:------|:--------|
|`!commands`|`!command`|
|`!addcom`|`!command add`|
|`!delcom`|`!command delete`|
|`!setcom`|`!command set`|
|`!editcom`|`!command set`|
|`!enable`|`!command enable`|
|`!disable`|`!command disable`|
|`!alias`|`!alias-meta add`|
|`!unalias`|`!alias-meta remove`|
|`!aliaslist`|`!alias-meta list`|
|`!resetUserLevel`|`!assignUserLevel reset`|
|`!silence`|`!silence-meta true`|
|`!unsilence`|`!silence-meta false`|
|`!bot-enable`|`!bot-enable-meta true`|
|`!enable-bot`|`!bot-enable-meta true`|
|`!bot-disable`|`!bot-enable-meta false`|
|`!disable-bot`|`!bot-enable-meta false`|
|`!quotes`|`!quote`|

#### Command Responses

Because script commands can have multiple outcomes, they send responses to chat as commands, not directly as messages. The commands which they use to respond are listed below. You can change the responses of the commands below to customize the responses of script commands. For example, if you don't like what the bot says when it creates a command, you can change it by changing the response to `~%command.set.success`.

| Command | Description | Default Response |
|:--------|:------------|:-----------------|
|`~%general:insufficient.args`|Some command or sub-command was not given enough arguments.|Insufficient arguments for command '[[arg1]]'[[ifarg2{{ when run with option(s): '[[fromarg2]]'}}]].|
|`~%general:insufficient.user.level`|A user running some command attempted to perform an action for which they did not have a high enough user level.|Insufficient user level [[ifarg2{{to perform action '[[fromarg2]]' }}]]in command '[[arg1]]'.|
|`~%general:invalid.arg`|An invalid argument was given for some command.|Invalid argument '[[arg2]]' in command '[[arg1]]'.|
|`~%general:invalid.flag`|An invalid flag was given for some command.|Invalid flag '[[arg2]]' in command '[[arg1]]'.|
|`~%command.general:does.not.exist`|A user attempted to run one of the subcommands of `!command` on a command which did not exist.|No such command '[[arg1]]'.|
|`~%command.add.already.exists`|A user attempted to add a command which already existed using `!command` `add | new`.|The command '[[arg1]]' already exists and cannot be added.|
|`~%command.set.success`|A command was successfully set.|Set command '[[arg1]]'.|
|`~%command.set.is.alias`|A command whose name is also in use as an alias was successfully set.|Set command '[[arg1]]'. Warning: '[[arg1]]' is an already an alias, so running it may not run the command.|
|`~%command.remove.success`|A command was successfully removed.|Removed command '[[arg1]]'.|
|`~%command.enabled`|A command was enabled.|Enabled command '[[arg1]]'.|
|`~%command.disabled`|A command was disabled.|Disabled command '[[arg1]]'.|
|`~%alias.general:does.not.exist`|A user attempted to run one of the subcommands of `!alias` on an alias which did not exist.|No such alias '[[arg1]]'.|
|`~%alias.add.already.exists`|A user attempted to add an alias which already existed using `!alias` `add | new`.|The alias '[[arg1]]' already exists and cannot be added.|
|`~%alias.success`|An alias was successfully set.|Set alias '[[arg1]]'.|
|`~%unalias.success`|An alias was successfully removed.|Unaliased '[[arg1]]'.|
|`~%alias.enabled`|An alias was enabled.|Enabled alias '[[arg1]]'.|
|`~%alias.disabled`|An alias was disabled.|Disabled alias '[[arg1]]'.|
|`~%command.set.exec.ul.success`|The minimum user level required to execute a command was successfully set.|Set exec user level for command '[[arg1]]'.|
|`~%command.set.min.args.success`|The minimum number of arguments with which a command must be executed was successfully set.|Set minimum arguments for command '[[arg1]]'.|
|`~%command.reset.count.success`|The count of a command was successfully reset to 0.|Reset count for command '[[arg1]]'.|
|`~%command.rename.success`|A command was successfully renamed.|Renamed '[[arg1]]' to '[[arg2]]'.|
|`~%command.rename.already.in.use`|A command could not be renamed because the new name given was already in use.|Cannot rename command to '[[arg1]]' - name is already in use.|
|`~%bot.unsilence.success`|The bot was successfully unsilenced.|Bot is no longer silenced.|
|`~%bot.enable.success`|The bot was successfully enabled.|Bot enabled.|
|`~%bot.leave.need.name`|Notifies the user running the `!leave` command that the bot's name must be specified to make it leave.|To remove the bot from your channel, please type "!leave <bot name>".|
|`~%bot.joined.channel`|Notifies users in a channel that the bot has joined the channel.|'Sup yo, is it aiight if I join yo channel?|
|`~%assign.user.level.success`|A user level was successfully assigned to a user.|Assigned user level to [[arg1]].|
|`~%reset.user.level.success`|A user's user level was reset.|Reset user level of [[arg1]] to be based on Twitch.|
|`~%assign.unsupported.user.level`|The user level specified in the `!assignUserLevel` command is a valid user level to have as the minumum user level for the execution of a command, but cannot be assigned because it is controlled by Twitch.|The user level provided is not supported because it is based on the user's status on Twitch.|
|`~%join.mode.set`|The join mode for the bot was successfully set.|Set join mode to: [[arg1]].|
|`~%whitelist.add.success`|A channel was added to the channel join whitelist.|Added '[[arg1]]' to the whitelist.|
|`~%whitelist.remove.success`|A channel was removed from the channel join whitelist.|Removed '[[arg1]]' from the whitelist.|
|`~%blacklist.add.success`|A channel was added to the channel join blacklist.|Added '[[arg1]]' to the blacklist.|
|`~%blacklist.remove.success`|A channel was removed from the channel join blacklist.|Removed '[[arg1]]' from the blacklist.|
|`~%quote.add.already.exists`|A quote with the exact text of the quote a user attepted to add is already in the quote database.|That quote already exists. Congrats on typing it exactly the same way.|
|`~%quote.add.success`|Successfully added a quote.|Added quote with ID '[[arg1]]'.|
|`~%quote.remove.success`|Successfully removed a quote.|Removed quote with ID '[[arg1]]'.|
|`~%quote.does.not.exist`|Some opperation could not be performed because the provided quote does not exist.|No such quote [[args]].|

##Special Terms

There are some special terms which can be used when creating a command to create a response which varies depending on how the command is run. Each special term is replaced by some value, as described in the following table. Some examples of special term usage are shown at the end of the section.

####Terms

| Term | Description |
|:-----|:------------|
|`[[user]]`|The name of the user who ran the command.|
|`[[args]]` or `[[args{{default}}]]`|All arguments with which the command was run (separated by spaces). If the command is run without arguments, and a default is given, the default is used. The default can contain another special term. If no default is given, it is replaced by an empty string (it is left blank).|
|`[[argN]]` or `[[argN{{default}}]]`|The `N`th argument, where `N` is a whole number greater than 0. If the command is run with fewer than `N` arguments, and a default is given, the default is used. The default can contain another special term. If no default is given, it is replaced by an empty string (it is left blank).|
|`[[fromargN]]` or `[[fromargN{{default}}]]`|All the arguments from the `N`th argument and on. As with `[[argN]]`, `N` must be a whole number. If `N` is 1, it is equivalent to `[[args]]`.|
|`[[numargs]]`|The number of arguments with which the command was run.|
|`[[channel]]`|The channel in which the command was run.|
|`[[count]]`|The number of times the command has been run since it was created or since it was last modified.|
|`[[quote]]` or `[[quote{{id}}]]`|A random quote from the quote database if no ID is given. If an ID is given, the quote with the specified ID (if it exists; an empty string if it does not exist or the ID is invalid).|
|`[[service]]`|The name of the service the bot is connected to ("Twitch" or "Beam").|
|`[[game]]`|The name of the game being played (as listed above the stream). Not yet implemented.|

####Modifiers

Modifiers can be used to modify the string with which the term is replaced. Modifiers are used in the following format:

```
[[term.modifier]]
[[term.modifier{{string}}]]
```

The modifiers are described in the following table.

| Modifier | Description |
|:---------|:------------|
|`lower`|The string is made entirely lowercase.|
|`upper`|The string is made entirely uppercase.|
|`first_cap`|The first letter of the string is made uppercase. All other letters are made lowercase.|
|`word_cap`|The first letter of each word (separated by a space) in the string is made uppercase. All other letters are made lowercase. Identical to first_cap for anything which is a single word (e.g. `[[user]]`, `[[argN]]`).|
|`first_cap_soft`|The first letter of the string is made uppercase. All other letters are not changed.|
|`word_cap_soft`|The first letter of each word (separated by a space)  in the string is made uppercase. All other letters are not changed. Identical to `first_cap_soft` for anything which is a single word (e.g. `[[user]]`, `[[argN]]`).|

####Embedded Strings

Some terms can contain embedded strings. The `{{default}}` in the `[[args]]` and `[[argN]]` terms is an example of an embedded string. Embedded strings are surrounded by `{{` and `}}`. Embedded strings can be useful for providing more information or options for a term, and are used extensively in more advanced and versatile terms.

If you are already confused at this point, you should probably skip this section and move on to the examples of term usage below.

The following are terms which are significantly more useful when given embedded strings, or not useful at all without them. If less than N embedded strings are provided, then any term which attempts to use the Nth embedded string will treat it as an empty string.

| Term | Description |
|:-----|:------------|
|`[[ifargs{{yes_args}}{{no_args}}]]`|Prints the first embedded string string if at least one argument was given. If no arguments were given, it prints the second embedded string. For either condition, if no string is supplied, it does nothing.|
|`[[ifargN{{N_args}}{{not_N_args}}]]`|Prints the first embedded string if at least `N` arguments were given. If less than `N` arguments were given, it prints the second embedded string. For either condition, ff no string is supplied, it does nothing. `N` must be greater than 0.|
|`[[foreach{{prepend}}{{append}}]]`|For each argument provided, prints the string specified as `prepend`, the argument, and then the string specified as `append`. If a modifier (described later) is used, it is applied to each argument (but not to `prepend` or `append`). If only one string is provided, it is `prepend`. If both are left out, the arguments are printed unmodified (and without spaces).|
|`[[equal{{compare1}}{{compare2}}{{same}}{{different}}]]`|If the first two embedded strings are the same, then it prints the third embedded string. If not, it prints the fourth embedded string. Not particularly useful if neither of the first two embedded strings are a term.|

####Examples

Here are some examples of special terms and modifiers:

**Commands**

| Name | Response |
|:-----|:---------|
|!hello|Hello [[arg1]]!|
|!poke|[[user]] pokes [[arg1{{someone}}]].|
|!hug|[[user.first_cap]] hugs [[args{{everyone}}]]!|
|!gc|[[user.upper]] FIRES THE GLITTER CANNON!|
|!riot|༼ つ ◕\_◕ ༽つ [[args.upper{{SHOUT}}]] OR RIOT ༼ つ ◕\_◕ ༽つ|
|!cookies|/me throws a cookie at [[arg1]] because master [[user]] said so! (Cookies given: [[count]])|
|!foo|[[arg1.lower]] [[arg1.upper]] [[arg1.word_cap]] [[arg1.word_cap_soft]]|
|!bar1|[[args.first_cap]]|
|!bar2|[[args.word_cap]]|
|!bar3|[[args.first_cap_soft]]|
|!bar4|[[args.word_cap_soft]]|
|!words|words [[arg1{{something}}]] words [[arg2{{[[arg1]]}}]] words [[arg3{{[[arg2{{[[arg1{{dunno}}]]}}]]}}]] words [[arg4{{[[arg2]]}}]] words|
|!multi|Watch [[foreach{{}}{{, }}]] and me at the same time! example.com/multistream/[[channel]]/[[foreach.lower{{}}{{/}}]]|
|!ifargs|A secret message appears if you run this command with args.[[ifargs{{ I mean, I suppose it's a secret, but it's not a very interesting one :P}}]]|
|!ifarg2|More args are [[ifarg2{{not }}]]needed. You need[[ifarg2{{ed}}]] 2 args.|

**Running the commands in chat**

All commands are run by a user named “fred” and responded to by a bot named “Bot” in the channel "the_lone_devil".

| Message in chat | Response |
|:----------------|:---------|
|fred: !hello bob|Bot: Hello bob!|
|fred: !hello|Bot: Hello !|
|fred: !hello bob and bill|Bot: Hello bob!|
|fred: !hello BOB|Bot: Hello BOB!|
|fred: !poke|Bot: fred pokes someone.|
|fred: !poke bob|Bot: fred pokes bob.|
|fred: !poke that guy|Bot: fred pokes that.|
|fred: !hug|Bot: Fred hugs everyone!|
|fred: !hug bob|Bot: Fred hugs bob!|
|fred: !hug everyone and their mother|Bot: Fred hugs everyone and their mother!|
|fred: !gc|Bot: FRED FIRES THE GLITTER CANNON!|
|fred: !gc someone|Bot: FRED FIRES THE GLITTER CANNON!|
|fred: !riot|Bot: ༼ つ ◕\_◕ ༽つ SHOUT OR RIOT ༼ つ ◕\_◕ ༽つ|
|fred: !riot eat cake|Bot: ༼ つ ◕\_◕ ༽つ EAT CAKE OR RIOT ༼ つ ◕\_◕ ༽つ|
|fred: !cookies|Bot throws a cookie at because master fred said so! (Cookies given: 1)|
|fred: !cookies bob|Bot throws a cookie at bob because master fred said so! (Cookies given: 2)|
|fred: !foo bAr|Bot: bar BAR Bar BAr|
|fred: !bar1 this is a TEST sentence.|Bot: This is a test sentence.|
|fred: !bar2 this is a TEST sentence.|Bot: This Is A Test Sentence.|
|fred: !bar3 this is a TEST sentence.|Bot: This is a TEST sentence.|
|fred: !bar4 this is a TEST sentence.|Bot: This Is A TEST Sentence.|
|fred: !words|Bot: words something words  words dunno words &nbsp;words|
|fred: !words one|Bot: words one words one words one words &nbsp;words|
|fred: !words one two|Bot: words one words two words two words two words|
|fred: !words one two three|Bot: words one words two words three words two words|
|fred: !words one two three four|Bot: words one words two words three words four words|
|fred: !multi|Bot: Watch me at the same time! example.com/multistream/the_lone_devil/|
|fred: !multi MaddiieManeater|Bot: Watch MaddiieManeater, and me at the same time! example.com/multistream/the_lone_devil/maddiiemaneater/|
|fred: !multi MaddiieManeater MKtheWorst|Bot: Watch MaddiieManeater, MKtheWorst, and me at the same time! example.com/multistream/the_lone_devil/maddiiemaneater/mktheworst/|
|fred: !ifargs|Bot: A secret message appears if you run this command with args.|
|fred: !ifargs some args|Bot: A secret message appears if you run this command with args. I mean, I suppose it's a secret, but it's not a very interesting one :P|
|fred: !ifarg2|Bot: More args are needed. You need 2 args.|
|fred: !ifarg2 one|Bot: More args are needed. You need 2 args.|
|fred: !ifarg2 one two|Bot: More args are not needed. You needed 2 args.|
