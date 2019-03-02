package me.fzzy.robofzzy4j.listeners

import me.fzzy.robofzzy4j.*
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.lang.NullPointerException
import java.util.regex.Matcher
import java.util.regex.Pattern

object MessageListener {

    val respongeMsgs = listOf(
            "no problem %name%",
            "np %name%",
            ":D",
            ":P"
    )

    @EventSubscriber
    fun onMessageReceived(event: MessageReceivedEvent) {
        if (Funcs.mentionsByName(event.message)) {
            for (msg in event.channel.getMessageHistory(7)) {
                if (msg.author.longID == RoboFzzy.cli.ourUser.longID) {
                    RequestBuffer.request {
                        MessageScheduler.sendTempMessage(RoboFzzy.DEFAULT_TEMP_MESSAGE_DURATION, event.channel, respongeMsgs[RoboFzzy.random.nextInt(respongeMsgs.size)].replace("%name%", event.author.getDisplayName(event.guild).toLowerCase()))
                    }
                    break
                }
            }
        }
        if (event.guild != null) {
            if (!event.author.isBot) {
                val guild = Guild.getGuild(event.guild.longID)
                val pattern = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)")
                val m: Matcher = pattern.matcher(event.message.content)
                if (m.find() || event.message.attachments.size > 0) {
                    guild.allowVotes(event.message)
                }
            }
        }
    }
}