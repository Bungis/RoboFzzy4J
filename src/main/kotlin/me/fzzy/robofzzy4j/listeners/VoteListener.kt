package me.fzzy.robofzzy4j.listeners

import me.fzzy.robofzzy4j.*
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuilder
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.NullPointerException
import java.net.URL
import java.util.regex.Pattern

class VoteListener {

    fun getVotes(message: IMessage): Int {
        val upvotes = message.getReactionByEmoji(ReactionEmoji.of("upvote", 445376322353496064L)).count
        val downvotes = message.getReactionByEmoji(ReactionEmoji.of("downvote", 445376330989830147L)).count - 1
        return upvotes - downvotes
    }

    fun sendForReview(message: IMessage) {
        if (message.guild.longID == memeId && !reviewIds.contains(message.longID)) {
            reviewIds.add(message.longID)
            lateinit var sent: IMessage
            if (message.attachments.size == 0) {
                if (!(message.content.toLowerCase().endsWith(".png")
                                || message.content.toLowerCase().endsWith(".jpg")
                                || message.content.toLowerCase().endsWith(".jpeg")
                                || message.content.toLowerCase().endsWith(".gif")
                                || message.content.toLowerCase().endsWith(".webm")))
                    return
            }
            RequestBuilder(cli).shouldBufferRequests(true).doAction {
                sent = if (message.attachments.size == 0) {
                    cli.getGuildByID(memeId).getChannelByID(reviewId).sendMessage(message.content)
                } else
                    cli.getGuildByID(memeId).getChannelByID(reviewId).sendMessage(message.attachments[0].url)
                true
            }.andThen {
                sent.addReaction(ReactionEmoji.of("upvote", 445376322353496064L))
                true
            }.andThen {
                sent.addReaction(ReactionEmoji.of("downvote", 445376330989830147L))
                true
            }.execute()
        }
    }

    @EventSubscriber
    fun onReactionAdd(event: ReactionAddEvent) {
        val guild = getGuild(event.guild.longID)
        if (guild != null && event.reaction != null) {
            var reacted = false
            try {
                reacted = event.reaction.getUserReacted(cli.ourUser)
            } catch (e: NullPointerException) {
            }
            if (reacted && event.user.longID != cli.ourUser.longID) {
                if (event.channel.longID != reviewId) {
                    if (System.currentTimeMillis() / 1000 - event.message.timestamp.epochSecond < 60 * 60 * 24) {
                        if (event.message.author.longID != event.user.longID) {
                            val score = guild.leaderboard.getOrDefault(event.author.longID, 0)
                            when (event.reaction.emoji.name) {
                                "upvote" -> {
                                    guild.leaderboard.setValue(event.author.longID, score + 1)
                                    guild.votes++
                                    if (getVotes(event.message) >= guild.getAverageVote()) {
                                        sendForReview(event.message)
                                    }
                                }
                                "downvote" -> {
                                    guild.leaderboard.setValue(event.author.longID, score - 1)
                                    guild.votes--
                                }
                            }
                        }
                    }
                } else {
                    if (event.reaction.emoji.name == "upvote") {
                        if (event.reaction.count >= 3) {
                            event.message.delete()
                            val pattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
                            lateinit var url: URL
                            if (event.message.attachments.size > 0) {
                                url = URL(event.message.attachments[0].url)
                            } else {
                                for (split in event.message.content.split(" ")) {
                                    val matcher = pattern.matcher(split)
                                    if (matcher.find()) {
                                        var urlString = split.substring(matcher.start(1), matcher.end()).replace(".webp", ".png").replace("//gyazo.com", "//i.gyazo.com")
                                        if (urlString.contains("i.gyazo.com") && !urlString.endsWith(".png")) {
                                            urlString += ".png"
                                        }
                                        if (urlString.contains("i.gyazo.com") && !urlString.endsWith(".jpg")) {
                                            urlString += ".jpq"
                                        }
                                        url = URL(urlString)
                                        break
                                    }
                                }
                            }
                            val fixedUrl = URL(url.toString().replace(".gifv", ".gif"))
                            var suffix = "jpg"
                            if (fixedUrl.toString().endsWith("webp") || fixedUrl.toString().endsWith("png"))
                                suffix = "png"
                            if (fixedUrl.toString().endsWith("gif"))
                                suffix = "gif"
                            if (fixedUrl.toString().endsWith("webm"))
                                suffix = "webm"

                            File("memes").mkdirs()
                            val fileName = "memes/${System.currentTimeMillis()}.$suffix"
                            try {
                                val openConnection = fixedUrl.openConnection()
                                openConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")
                                openConnection.connect()

                                val inputStream = BufferedInputStream(openConnection.getInputStream())
                                val outputStream = BufferedOutputStream(FileOutputStream(fileName))

                                for (out in inputStream.iterator()) {
                                    outputStream.write(out.toInt())
                                }
                                inputStream.close()
                                outputStream.close()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else
                        event.message.delete()
                }
            }
        }
    }

    @EventSubscriber
    fun onReactionRemove(event: ReactionRemoveEvent) {
        val guild = getGuild(event.guild.longID)
        if (guild != null) {
            if (System.currentTimeMillis() / 1000 - event.message.timestamp.epochSecond < 60 * 60 * 24) {
                if (event.reaction.getUserReacted(cli.ourUser) && event.user.longID != cli.ourUser.longID) {
                    if (event.message.author.longID != event.user.longID) {
                        val score = guild.leaderboard.getOrDefault(event.author.longID, 0)
                        when (event.reaction.emoji.name) {
                            "upvote" -> {
                                guild.leaderboard.setValue(event.author.longID, score - 1)
                                guild.votes--
                            }
                            "downvote" -> {
                                guild.leaderboard.setValue(event.author.longID, score + 1)
                                guild.votes++
                                if (getVotes(event.message) >= guild.getAverageVote()) {
                                    sendForReview(event.message)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}