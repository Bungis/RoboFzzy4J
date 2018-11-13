package me.fzzy.robofzzy4j.commands.help

import me.fzzy.robofzzy4j.Command
import me.fzzy.robofzzy4j.CommandResult
import me.fzzy.robofzzy4j.RoboFzzy
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

object Invite : Command {

    override val cooldownMillis: Long = 4 * 1000
    override val votes: Boolean = false
    override val description = "Gives you the invite link for the bot to add it to servers"
    override val usageText: String = "-invite"
    override val allowDM: Boolean = true

    override fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult {
        RequestBuffer.request { event.author.orCreatePMChannel.sendMessage("https://discordapp.com/oauth2/authorize?client_id=${RoboFzzy.cli.ourUser.longID}&scope=bot&permissions=306240") }
        return CommandResult.success()
    }

}