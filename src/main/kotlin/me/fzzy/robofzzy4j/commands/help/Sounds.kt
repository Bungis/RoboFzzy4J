package me.fzzy.robofzzy4j.commands.help

import me.fzzy.robofzzy4j.*
import me.fzzy.robofzzy4j.listeners.VoiceListener
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.audio.AudioPlayer
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

private const val SOUND_COOLDOWN: Long = 30 * 1000

class Sounds : Command {

    override val cooldownMillis: Long = 4 * 1000
    override val attemptDelete: Boolean = true
    override val description: String = "Shows all the sounds the bot can play in the voice channel"
    override val usageText: String = "-sounds"
    override val allowDM: Boolean = true

    override fun runCommand(event: MessageReceivedEvent, args: List<String>) {
        var all = "```"
        for (file in File("sounds").listFiles()) {
            all += "-${file.nameWithoutExtension}\n"
        }
        all += "```"
        RequestBuffer.request { event.message.author.orCreatePMChannel.sendMessage(all) }
    }

    private var cooldowns: HashMap<Long, Long> = hashMapOf()

    @EventSubscriber
    fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.guild != null) {
            if (event.message.content.startsWith(BOT_PREFIX)) {
                val audioDir = File("sounds").listFiles { file -> file.name.contains(event.message.content.substring(1)) }

                if (audioDir == null || audioDir.isEmpty())
                    return

                RequestBuffer.request {
                    try {
                        event.message.delete()
                    } catch (e: MissingPermissionsException) {
                    } catch (e: DiscordException) {
                    }
                }

                if (System.currentTimeMillis() - cooldowns.getOrDefault(event.author.longID, 0) > SOUND_COOLDOWN) {
                    if (cli.ourUser.getVoiceStateForGuild(event.guild).channel == null) {
                        cooldowns[event.author.longID] = System.currentTimeMillis()

                        println("${SimpleDateFormat("hh:mm:ss aa").format(Date(System.currentTimeMillis()))} - ${event.author.name}#${event.author.discriminator} playing sound: ${event.message.content}")
                        val userVoiceChannel = event.author.getVoiceStateForGuild(event.guild).channel
                        if (userVoiceChannel != null) {
                            VoiceListener.playTempAudio(userVoiceChannel, audioDir[0], false)
                        }
                    }
                } else {
                    val timeLeft = (SOUND_COOLDOWN / 1000) - ((System.currentTimeMillis() - cooldowns.getOrDefault(event.author.longID, System.currentTimeMillis())) / 1000)
                    val message = "${event.author.getDisplayName(event.guild)}! You are on cooldown for $timeLeft seconds."
                    RequestBuffer.request {
                        val msg = Funcs.sendMessage(event.channel, message)
                        if (msg != null)
                            CooldownMessage(timeLeft.toInt(), event.channel, event.author.getDisplayName(event.guild), msg).start()
                    }
                }
            }
        }
    }

}