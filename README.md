# PistonChat
[![Discord embed](https://discordapp.com/api/guilds/739784741124833301/embed.png)](https://discord.gg/CDrcxzH)


**An advanced chat plugin for survival/anarchy servers.**

## Features
* Green Text
* Private Messaging
* Auto Complete when you click on a players name just like on the server 2b2t.org
* Ignore Commands

## Commands

* /ignore: Ignore a players chat messages!
  permission: pistonchat.ignore
  aliases:
  block

* /whisper: Whisper to a player!
  permission: pistonchat.whisper
  aliases:
  tell
  w
  pm
  msg

* /reply: Reply to the last message someone sent to you!
  permission: pistonchat.reply
  aliases:
  r

* /last: Message the last person you messaged!
  permission: pistonchat.reply
  aliases:
  l

* /ignorelist: List all ignored players!
  permission: pistonchat.ignorelist

* /togglewhispering: Prevent getting whispered to!
  permission: pistonchat.togglewhispering
  aliases: toggletells

* /togglechat: Prevent getting chat messages!
  permission: pistonchat.togglechat

* /pistonchat: Main command!
  permission: pistonchat.command

## Permissions

* pistonchat.last: Permission for /last!

* pistonchat.reply: Permission for /reply!
* pistonchat.whisper: Permission for /whisper!

  children:

  pistonchat.playernamereply: true

  pistonchat.togglewhispering: true

* pistonchat.ignore: Permission for /ignore!

  children:

  pistonchat.ignorelist: true

* pistonchat.playernamereply: Make player names clickable!

* pistonchat.ignorelist: Permission for /ignorelist!
* pistonchat.togglewhispering: Permission for /togglewhispering!
* pistonchat.togglechat: Permission for /togglechat!
* pistonchat.command: Permission for /pistonchat!
* pistonchat.help: Permission for /pistonchat help!

  children:

  pistonchat.command true
* pistonchat.reload: Permission for /pistonchat reload!

  children:

  pistonchat.command: true
* pistonchat.GREEN: Just another chat color!
* pistonchat.BLUE: Just another chat color!
* pistonchat.RED: Just another chat color!
* pistonchat.AQUA: Just another chat color!
* pistonchat.GOLD: Just another chat color!
* pistonchat.YELLOW: Just another chat color!
* pistonchat.GRAY: Just another chat color!
* pistonchat.BLACK: Just another chat color!
* pistonchat.DARK_GREEN: Just another chat color!
* pistonchat.DARK_RED: Just another chat color!
* pistonchat.DARK_GRAY: Just another chat color!
* pistonchat.DARK_BLUE: Just another chat color!
* pistonchat.DARK_AQUA: Just another chat color!
* pistonchat.DARK_PURPLE: Just another chat color!
* pistonchat.LIGHT_PURPLE: Just another chat color!
* pistonchat.ITALIC: Just another chat color!
* pistonchat.UNDERLINE: Just another chat color!
* pistonchat.BOLD: Just another chat color!
* pistonchat.STRIKETHROUGH: Just another chat color!
* pistonchat.player: Default permissions for players!

  children:

  pistonchat.last: true

  pistonchat.reply: true

  pistonchat.whisper: true

  pistonchat.ignore: true

  pistonchat.togglechat: true

  pistonchat.help: true

  pistonchat.GREEN: true

  default: true
* pistonchat.admin: Default permissions for admins!

  children:

  pistonchat.reload: true

  default: op