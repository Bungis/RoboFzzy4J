package me.fzzy.robofzzy4j

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

interface Command {

    val cooldownMillis: Long
    val description: String
    val votes: Boolean
    val usageText: String
    val allowDM: Boolean

    fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult

}